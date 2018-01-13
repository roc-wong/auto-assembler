package me.caosh.autoasm;

import com.google.common.base.MoreObjects;
import org.joda.time.MonthDay;
import org.joda.time.YearMonth;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author caosh/shuhaoc@qq.com
 * @date 2018/1/10
 */
public class BasicTest {
    private final AutoAssembler autoAssembler = new AutoAssembler();

    @Test
    public void testBasic() throws Exception {
        TestBasicObject testBasicObject = new TestBasicObject();
        testBasicObject.setId(12);
        testBasicObject.setName("ccc");
        testBasicObject.setSetterOnly("domain has setter only");
        // transient value, DTO does not have
        testBasicObject.setTransientValue(233);
        // general field no converted
        testBasicObject.setYearMonth(YearMonth.now());

        TestDTO testDTO = autoAssembler.assemble(testBasicObject, TestDTO.class);

        assertEquals(testDTO.getId(), testBasicObject.getId());
        assertEquals(testDTO.getName(), testBasicObject.getName());
        assertNull(testDTO.getSetterOnly());
        assertNull(testDTO.getNullIgnored());
        assertEquals(testDTO.getYearMonth(), testBasicObject.getYearMonth());
    }

    @Test
    public void testBasicDisassemble() throws Exception {
        TestBasicObject testBasicObject = new TestBasicObject();
        testBasicObject.setId(12);
        testBasicObject.setName("ccc");
        testBasicObject.setYearMonth(YearMonth.now());

        TestDTO testDTO = autoAssembler.assemble(testBasicObject, TestDTO.class);
        TestBasicObject disassembled = autoAssembler.disassemble(testDTO, TestBasicObject.class);
        assertEquals(disassembled, testBasicObject);
    }

    @Test
    public void testConst() throws Exception {
        TestBasicObject domainObject = new TestBasicObject();
        // no use
        domainObject.setConstInt(341);

        TestConstDTO testConstDTO = autoAssembler.assemble(domainObject, TestConstDTO.class);

        assertEquals(testConstDTO.getConstString(), "abc");
        assertEquals(testConstDTO.getConstInt(), Integer.valueOf(342));

        TestBasicObject disassembled = autoAssembler.disassemble(testConstDTO, TestBasicObject.class);
        assertEquals(disassembled.getConstInt(), testConstDTO.getConstInt());
    }

    public static class TestBasicObject {
        private Integer id;
        private String name;
        private String setterOnly;
        private transient Integer transientValue;
        private YearMonth nullIgnored;
        private YearMonth yearMonth;
        private Integer constInt;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setSetterOnly(String setterOnly) {
            this.setterOnly = setterOnly;
        }

        public Integer getTransientValue() {
            return transientValue;
        }

        public void setTransientValue(Integer transientValue) {
            this.transientValue = transientValue;
        }

        public YearMonth getNullIgnored() {
            return nullIgnored;
        }

        public YearMonth getYearMonth() {
            return yearMonth;
        }

        public void setYearMonth(YearMonth yearMonth) {
            this.yearMonth = yearMonth;
        }

        public Integer getConstInt() {
            return constInt;
        }

        public void setConstInt(Integer constInt) {
            this.constInt = constInt;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestBasicObject that = (TestBasicObject) o;

            if (id != null ? !id.equals(that.id) : that.id != null) return false;
            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            if (setterOnly != null ? !setterOnly.equals(that.setterOnly) : that.setterOnly != null) return false;
            if (transientValue != null ? !transientValue.equals(that.transientValue) : that.transientValue != null)
                return false;
            if (nullIgnored != null ? !nullIgnored.equals(that.nullIgnored) : that.nullIgnored != null) return false;
            if (yearMonth != null ? !yearMonth.equals(that.yearMonth) : that.yearMonth != null) return false;
            return constInt != null ? constInt.equals(that.constInt) : that.constInt == null;
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (setterOnly != null ? setterOnly.hashCode() : 0);
            result = 31 * result + (transientValue != null ? transientValue.hashCode() : 0);
            result = 31 * result + (nullIgnored != null ? nullIgnored.hashCode() : 0);
            result = 31 * result + (yearMonth != null ? yearMonth.hashCode() : 0);
            result = 31 * result + (constInt != null ? constInt.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(TestBasicObject.class).omitNullValues()
                    .add("id", id)
                    .add("name", name)
                    .add("setterOnly", setterOnly)
                    .add("transientValue", transientValue)
                    .add("nullIgnored", nullIgnored)
                    .add("yearMonth", yearMonth)
                    .add("constInt", constInt)
                    .toString();
        }
    }

    public static class TestDTO extends BaseDTO {
        private String name;
        private String setterOnly;
        private MonthDay nullIgnored;
        private YearMonth yearMonth;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSetterOnly() {
            return setterOnly;
        }

        public void setSetterOnly(String setterOnly) {
            this.setterOnly = setterOnly;
        }

        public MonthDay getNullIgnored() {
            return nullIgnored;
        }

        public void setNullIgnored(MonthDay nullIgnored) {
            this.nullIgnored = nullIgnored;
        }

        public YearMonth getYearMonth() {
            return yearMonth;
        }

        public void setYearMonth(YearMonth yearMonth) {
            this.yearMonth = yearMonth;
        }
    }

    public static class TestConstDTO {
        @FieldMapping("abc")
        private String constString;
        @FieldMapping("342")
        private Integer constInt;

        public String getConstString() {
            return constString;
        }

        public void setConstString(String constString) {
            this.constString = constString;
        }

        public Integer getConstInt() {
            return constInt;
        }

        public void setConstInt(Integer constInt) {
            this.constInt = constInt;
        }
    }
}
