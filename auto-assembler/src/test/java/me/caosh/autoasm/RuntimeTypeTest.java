package me.caosh.autoasm;

import com.google.common.base.MoreObjects;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author shuhaoc@qq.com
 * @date 2018/1/13
 */
public class RuntimeTypeTest {
    private AutoAssembler autoAssembler = AutoAssemblers.getDefault();

    @Test
    public void test() throws Exception {
        TestConditionOrder testConditionOrder = new TestConditionOrder();
        FirstExternalProperties externalProperties = new FirstExternalProperties();
        externalProperties.setX(123);
        testConditionOrder.setExternalProperties(externalProperties);

        TestConditionOrderDTO testConditionOrderDTO = autoAssembler.assemble(testConditionOrder, TestConditionOrderDTO.class);
        assertEquals(((FirstExternalPropertiesDTO) testConditionOrderDTO.getExternalProperties()).getX(),
                ((FirstExternalProperties) testConditionOrder.getExternalProperties()).getX());

        TestConditionOrder disassemble = autoAssembler.disassemble(testConditionOrderDTO, TestConditionOrder.class);
        assertEquals(((FirstExternalProperties) disassemble.getExternalProperties()).getX(),
                ((FirstExternalPropertiesDTO) testConditionOrderDTO.getExternalProperties()).getX());
    }

    @Test
    public void testSameTypeUsingInterface() throws Exception {
        TestConditionOrder testConditionOrder = new TestConditionOrder();
        FirstExternalProperties externalProperties = new FirstExternalProperties();
        externalProperties.setX(123);
        testConditionOrder.setExternalProperties(externalProperties);

        TestConditionOrder assemble = autoAssembler.assemble(testConditionOrder, TestConditionOrder.class);
        assertEquals(assemble, testConditionOrder);

        TestConditionOrder disassemble = autoAssembler.disassemble(assemble, TestConditionOrder.class);
        assertEquals(disassemble, assemble);
    }

    public static class TestConditionOrder {
        private ExternalProperties externalProperties;

        public ExternalProperties getExternalProperties() {
            return externalProperties;
        }

        public void setExternalProperties(ExternalProperties externalProperties) {
            this.externalProperties = externalProperties;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestConditionOrder that = (TestConditionOrder) o;

            return externalProperties != null ? externalProperties.equals(that.externalProperties) : that.externalProperties == null;
        }

        @Override
        public int hashCode() {
            return externalProperties != null ? externalProperties.hashCode() : 0;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(TestConditionOrder.class)
                    .add("externalProperties", externalProperties)
                    .toString();
        }
    }

    public interface ExternalProperties {
    }

    public static class FirstExternalProperties implements ExternalProperties {
        private Integer x;

        public Integer getX() {
            return x;
        }

        public void setX(Integer x) {
            this.x = x;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FirstExternalProperties that = (FirstExternalProperties) o;

            return x != null ? x.equals(that.x) : that.x == null;
        }

        @Override
        public int hashCode() {
            return x != null ? x.hashCode() : 0;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(FirstExternalProperties.class)
                    .add("x", x)
                    .toString();
        }
    }

    public static class TestConditionOrderDTO {
        private ExternalPropertiesDTO externalProperties;

        public ExternalPropertiesDTO getExternalProperties() {
            return externalProperties;
        }

        public void setExternalProperties(ExternalPropertiesDTO externalProperties) {
            this.externalProperties = externalProperties;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestConditionOrderDTO that = (TestConditionOrderDTO) o;

            return externalProperties != null ? externalProperties.equals(that.externalProperties) : that.externalProperties == null;
        }

        @Override
        public int hashCode() {
            return externalProperties != null ? externalProperties.hashCode() : 0;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(TestConditionOrderDTO.class)
                    .add("externalProperties", externalProperties)
                    .toString();
        }
    }

    @RuntimeType({FirstExternalPropertiesDTO.class})
    public interface ExternalPropertiesDTO {
    }

    @MappedClass(FirstExternalProperties.class)
    public static class FirstExternalPropertiesDTO implements ExternalPropertiesDTO {
        private Integer x;

        public Integer getX() {
            return x;
        }

        public void setX(Integer x) {
            this.x = x;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FirstExternalPropertiesDTO that = (FirstExternalPropertiesDTO) o;

            return x != null ? x.equals(that.x) : that.x == null;
        }

        @Override
        public int hashCode() {
            return x != null ? x.hashCode() : 0;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(FirstExternalPropertiesDTO.class)
                    .add("x", x)
                    .toString();
        }
    }
}
