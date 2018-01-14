package me.caosh.autoasm;

import com.google.common.base.Converter;
import me.caosh.autoasm.converter.ConverterMapping;
import me.caosh.autoasm.converter.DefaultConverterMapping;
import me.caosh.autoasm.handler.*;
import me.caosh.autoasm.util.PropertyFindResult;
import me.caosh.autoasm.util.PropertyUtils;
import me.caosh.autoasm.util.ReflectionUtils;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 自动装载器，自动完成domain object与pojo之间或pojo之间的转换
 *
 * @author caosh/shuhaoc@qq.com
 * @date 2018/1/10
 */
public class AutoAssembler {
    private static final String CLASS = "class";

    private ReadHandler assembleReadHandler;
    private ReadHandler disassembleReadHandler;
    private PropertyFinder disassemblePropertyFinder;
    private ConverterMapping converterMapping;

    public AutoAssembler() {
        this(new DefaultConverterMapping());
    }

    AutoAssembler(ConverterMapping converterMapping) {
        this.assembleReadHandler = new ReadHandlerChain(
                new FieldMappingAssembleReadHandler(),
                new ReflectionReadHandler()
        );
        this.disassembleReadHandler = new ReadHandlerChain(
                new ReflectionReadHandler(),
                new FieldMappingDisassembleReadHandler()
        );
        this.disassemblePropertyFinder = new FieldMappingDisassemblePropertyFinder();
        this.converterMapping = converterMapping;
    }

    /**
     * 将source对象转换装载为targetClass的实例对象
     * <p>
     * 1. targetClass需要支持无参构造
     * 2. source对象的所有getters提供的属性值将被赋值给targetClass创建的实例对象的setters方法
     * 3. 赋值时支持基本的类型转换
     *
     * @param sourceObject 源对象
     * @param targetClass  目标类信息
     * @param <T>          目标类型
     * @return 目标对象，不可能为空
     */
    public <S, T> T assemble(S sourceObject, Class<T> targetClass) {
        T targetObject = ReflectionUtils.newInstance(targetClass);

        PropertyDescriptor[] targetPropertyDescriptors = BeanUtils.getPropertyDescriptors(targetClass);
        for (PropertyDescriptor targetPropertyDescriptor : targetPropertyDescriptors) {
            Method writeMethod = targetPropertyDescriptor.getWriteMethod();
            if (writeMethod != null) {
                String propertyName = targetPropertyDescriptor.getName();
                FieldMapping fieldMapping = getFieldMapping(propertyName, writeMethod);
                Object value = assembleReadHandler.read(fieldMapping, sourceObject, propertyName);
                if (value != null) {
                    Class<?> propertyType = targetPropertyDescriptor.getPropertyType();
                    Object convertedValue = convertValueOnAssembling(value, propertyType);
                    PropertyUtils.setProperty(targetPropertyDescriptor, targetObject, convertedValue);
                }
            }
        }
        return targetObject;
    }

    /**
     * 将targetObject反装载为targetObject实例对象
     * <p>
     * 1. sourceClass需要支持无参构造
     * 2. target对象的所有getters提供的属性值将被赋值给sourceClass创建的实例对象的setters方法
     * 3. 赋值时支持基本的类型转换
     *
     * @param targetObject 目标对象
     * @param sourceClass  源类型class
     * @param <S>          源类型
     * @param <T>          目标类型
     * @return 源对象
     */
    public <S, T> S disassemble(T targetObject, Class<S> sourceClass) {
        S sourceObject = ReflectionUtils.newInstance(sourceClass);

        Class<?> targetClass = targetObject.getClass();
        PropertyDescriptor[] targetPropertyDescriptors = BeanUtils.getPropertyDescriptors(targetClass);
        for (PropertyDescriptor targetPropertyDescriptor : targetPropertyDescriptors) {
            Method readMethod = targetPropertyDescriptor.getReadMethod();
            if (readMethod != null) {
                String propertyName = targetPropertyDescriptor.getName();
                FieldMapping fieldMapping = getFieldMapping(propertyName, readMethod);
                Object value = disassembleReadHandler.read(fieldMapping, targetObject, propertyName);
                if (value != null) {
                    PropertyFindResult propertyFindResult = disassemblePropertyFinder.findPropertyDescriptor(
                            sourceObject, propertyName, fieldMapping);
                    if (propertyFindResult != null) {
                        PropertyDescriptor propertyDescriptor = propertyFindResult.getPropertyDescriptor();
                        Class<?> propertyType = propertyDescriptor.getPropertyType();
                        Class<?> targetPropertyType = targetPropertyDescriptor.getPropertyType();
                        Object convertedValue = convertValueOnDisassembling(value, targetPropertyType, propertyType);
                        PropertyUtils.setProperty(propertyDescriptor, propertyFindResult.getOwnObject(), convertedValue);
                    }
                }
            }
        }
        return sourceObject;
    }

    private FieldMapping getFieldMapping(String propertyName, Method accessorMethod) {
        if (CLASS.equals(propertyName)) {
            // 每个对象都有一个class属性，不处理
            return null;
        }

        Field declaredField;
        try {
            declaredField = accessorMethod.getDeclaringClass().getDeclaredField(propertyName);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Get declared field (" + propertyName + ") from property declaring class <"
                    + accessorMethod.getDeclaringClass().getSimpleName() + "> failed", e);
        }

        return declaredField.getAnnotation(FieldMapping.class);
    }

    /**
     * 进行字段转换
     *
     * @param value              转换前字段值
     * @param targetPropertyType 目标类型
     * @return 转换后字段值
     */
    private Object convertValueOnAssembling(Object value, Class<?> targetPropertyType) {
        if (targetPropertyType.isInstance(value)) {
            return value;
        }

        Converter converter = converterMapping.find(value.getClass(), targetPropertyType);
        if (converter != null) {
            return converter.convert(value);
        }

        Convertible convertible = targetPropertyType.getAnnotation(Convertible.class);
        if (convertible != null) {
            return assemble(value, targetPropertyType);
        }
        RuntimeType runtimeType = targetPropertyType.getAnnotation(RuntimeType.class);
        if (runtimeType != null) {
            Class<?>[] subClasses = runtimeType.value();
            for (Class<?> subClass : subClasses) {
                MappedClass mappedClass = subClass.getAnnotation(MappedClass.class);
                if (mappedClass == null) {
                    throw new IllegalArgumentException("Runtime type subclass should be annotated with @MappedClass");
                }
                if (mappedClass.value().isInstance(value)) {
                    return assemble(value, subClass);
                }
            }
        }
        throw new IllegalArgumentException("Type mismatch and cannot convert: " + value.getClass().getSimpleName()
                + " to " + targetPropertyType.getSimpleName());
    }

    private Object convertValueOnDisassembling(Object value, Class<?> targetPropertyType, Class<?> expectedPropertyType) {
        if (expectedPropertyType.isInstance(value)) {
            return value;
        }

        Converter converter = converterMapping.find(value.getClass(), expectedPropertyType);
        if (converter != null) {
            return converter.convert(value);
        }

        Convertible convertible = targetPropertyType.getAnnotation(Convertible.class);
        if (convertible != null) {
            return disassemble(value, expectedPropertyType);
        }
        throw new IllegalArgumentException("Type mismatch and cannot convert: " + value.getClass().getSimpleName()
                + " to " + expectedPropertyType.getSimpleName());
    }
}
