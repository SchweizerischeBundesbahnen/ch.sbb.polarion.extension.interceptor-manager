package ch.sbb.polarion.extension.interceptor_manager.model;

import ch.sbb.polarion.extension.interceptor_manager.settings.HookModel;
import ch.sbb.polarion.extension.interceptor_manager.util.HookManifestUtils;
import ch.sbb.polarion.extension.interceptor_manager.util.PropertiesUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class ActionHookTest {

    private MockedStatic<HookManifestUtils> manifestUtilsStaticMock;

    @BeforeEach
    void setUp() {
        manifestUtilsStaticMock = mockStatic(HookManifestUtils.class);
        manifestUtilsStaticMock.when(() -> HookManifestUtils.getHookVersion(any())).thenReturn("1.0");
    }

    @AfterEach
    void tearDown() {
        manifestUtilsStaticMock.close();
    }

    @Test
    public void testSelectors() {

        assertEquals("value", new TestHook("someProperty=value").getSettingsValue("someProperty"));
        assertEquals("", new TestHook("someProperty.*=value").getSettingsValue("someProperty"));

        assertEquals("value", new TestHook("someProperty.project1.selector1=value").getSettingsValue("someProperty", "project1", "selector1"));
        assertEquals("", new TestHook("someProperty.project1.selector1=value").getSettingsValue("someProperty", "project1", "selector2"));

        assertEquals("value", new TestHook("someProperty.project1.*=value").getSettingsValue("someProperty", "project1", "selector1"));
        assertEquals("value", new TestHook("someProperty.project1.*=value").getSettingsValue("someProperty", "project1", "selector2"));

        assertEquals("", new TestHook("someProperty.project1.selector1=value").getSettingsValue("someProperty", "project2", "selector1"));
        assertEquals("value", new TestHook("someProperty.*.selector1=value").getSettingsValue("someProperty", "project2", "selector1"));

        assertEquals("", new TestHook("someProperty.project1.selector1=value").getSettingsValue("someProperty", "project2", "selector2"));
        assertEquals("value", new TestHook("someProperty.*.*=value").getSettingsValue("someProperty", "project2", "selector2"));

        assertEquals("", new TestHook("someProperty2.project1.selector1=value").getSettingsValue("someProperty", "project1", "selector1"));
        assertEquals("value", new TestHook("*.project1.selector1=value").getSettingsValue("someProperty2", "project1", "selector1"));

        assertEquals("", new TestHook("someProperty1.project1.selector1=value").getSettingsValue("someProperty1", "project2", "selector1"));
        assertEquals("value", new TestHook("*.*.selector1=value").getSettingsValue("someProperty1", "project2", "selector1"));

        assertEquals("value", new TestHook("*.*.*=value").getSettingsValue("someProperty1", "project2", "selector42"));
        assertEquals("value", new TestHook("*.*.*=value").getSettingsValue("", "project2", "selector42"));
        assertEquals("value", new TestHook("*.*.*=value").getSettingsValue("", null, null));
        assertEquals("", new TestHook("*.*.*=value").getSettingsValue("", (String) null));
        assertEquals("", new TestHook("*.*.*=value").getSettingsValue(""));
        assertEquals("", new TestHook("*.*=value").getSettingsValue("", null, null));
        assertEquals("", new TestHook("*=value").getSettingsValue("", null, null));
    }

    @Test
    public void testCommaSeparatedValues() {
        assertTrue(new TestHook("someProperty.*=value1,value2,value3").isCommaSeparatedSettingsHasItem("value2", "someProperty", "project1"));
        assertFalse(new TestHook("wrongCase.*=value1,value2,value3").isCommaSeparatedSettingsHasItem("Value2", "wrongCase", "project1"));
        assertTrue(new TestHook("withSpaces.*=value1, value2, value3").isCommaSeparatedSettingsHasItem("value2", "withSpaces", "project2"));
        assertFalse(new TestHook("someProperty.*=value1,value2,value3").isCommaSeparatedSettingsHasItem("value4", "someProperty", "project1"));
        assertFalse(new TestHook("containsNull=value1,null,value3").isCommaSeparatedSettingsHasItem(null, "containsNull"));
        assertFalse(new TestHook("noNull=value1,value2").isCommaSeparatedSettingsHasItem(null, "noNull"));
        assertTrue(new TestHook("allProperty=*").isCommaSeparatedSettingsHasItem("value", "allProperty"));
        assertTrue(new TestHook("allProperty=*").isCommaSeparatedSettingsHasItem(null, "allProperty"));
    }

    @Test
    public void testBoolean() {
        assertTrue(new TestHook("someProperty=true").getSettingsValueAsBoolean("someProperty"));
        assertTrue(new TestHook("someProperty=TRUE").getSettingsValueAsBoolean("someProperty"));
        assertTrue(new TestHook("someProperty=  True").getSettingsValueAsBoolean("someProperty"));
        assertFalse(new TestHook("someProperty=false").getSettingsValueAsBoolean("someProperty"));
        assertNull(new TestHook("anotherProperty=true").getSettingsValueAsBoolean("someProperty"));
        assertFalse(new TestHook("someProperty=someNonBooleanString").getSettingsValueAsBoolean("someProperty"));
        assertNull(new TestHook("someProperty=").getSettingsValueAsBoolean("someProperty"));
    }

    @Test
    public void testInt() {
        assertEquals(4, new TestHook("someProperty=4").getSettingsValueAsInt("someProperty"));
        assertEquals(-42, new TestHook("someProperty= -42").getSettingsValueAsInt("someProperty"));
        assertThrows(NumberFormatException.class, () -> new TestHook("someProperty=1 200").getSettingsValueAsInt("someProperty"));
        assertThrows(NumberFormatException.class, () -> new TestHook("someProperty=someString").getSettingsValueAsInt("someProperty"));
        assertThrows(NumberFormatException.class, () -> new TestHook("someProperty=0.1").getSettingsValueAsInt("someProperty"));
    }

    @Test
    public void testList() {
        assertEquals(List.of("1", "2", "3"), new TestHook("someProperty=1,2,3").getSettingsValueAsList("someProperty"));
        // we must preserve explicit empty values, clients may filter them if they are not needed in theirs scenario
        assertEquals(List.of("1", "2", "3", "", "", "", ""), new TestHook("someProperty=1, 2   ,3,,,,  , ").getSettingsValueAsList("someProperty"));
        assertEquals(List.of(), new TestHook("someProperty=").getSettingsValueAsList("someProperty"));
        assertEquals(List.of(), new TestHook("").getSettingsValueAsList("someProperty"));
    }

    @Test
    public void testGetWithSelectors() {
        assertEquals(linkedMap("someProperty", "4"), new TestHook("someProperty=4").getSettingsValuesWithSelector("someProperty"));
        assertEquals(linkedMap("*", "4"), new TestHook("*=4").getSettingsValuesWithSelector("someProperty"));
        assertEquals(linkedMap(), new TestHook("someProperty=4").getSettingsValuesWithSelector("someProperty2"));
        assertEquals(linkedMap(), new TestHook("someProperty2.someSelector=4").getSettingsValuesWithSelector("someProperty2"));

        TestHook hook = new TestHook(PropertiesUtils.build(
                "someValue.someProject.additionalSelector", "1",
                "someValue.someProject.*", "2",
                "someValue.*.additionalSelector", "3",
                "someValue.*.*", "4",
                "*.someProject.additionalSelector", "5",
                "*.someProject.*", "6",
                "*.*.additionalSelector", "7",
                "*.*.*", "8"
        ));
        assertEquals(linkedMap("someValue.someProject.additionalSelector", "1"), hook.getSettingsValuesWithSelector("someValue", "someProject", "additionalSelector"));
        assertEquals(linkedMap("someValue.someProject.*", "2"), hook.getSettingsValuesWithSelector("someValue", "someProject", "additionalSelector2"));
        assertEquals(linkedMap("someValue.*.additionalSelector", "3"), hook.getSettingsValuesWithSelector("someValue", null, "additionalSelector"));
        assertEquals(linkedMap("someValue.*.*", "4"), hook.getSettingsValuesWithSelector("someValue", null, "qwe"));
        assertEquals(linkedMap("*.someProject.additionalSelector", "5"), hook.getSettingsValuesWithSelector("someValue2", "someProject", "additionalSelector"));
        assertEquals(linkedMap("*.someProject.*", "6"), hook.getSettingsValuesWithSelector("someValue2", "someProject", "qwe"));
        assertEquals(linkedMap("*.*.additionalSelector", "7"), hook.getSettingsValuesWithSelector("someValue2", "qwe", "additionalSelector"));
        assertEquals(linkedMap("*.*.*", "8"), hook.getSettingsValuesWithSelector("someValue2", "qwe", "qwe2"));
        assertEquals(linkedMap("*.*.*", "8"), hook.getSettingsValuesWithSelector("", null, null));

        // property name MUST have proper comma-separated entries count: {selectors count} + 1, otherwise it is treated as a completely different
        assertEquals(linkedMap(), hook.getSettingsValuesWithSelector("someValue"));
        assertEquals(linkedMap(), hook.getSettingsValuesWithSelector("someValue", "someProject"));
        assertEquals(linkedMap(), hook.getSettingsValuesWithSelector("someValue", "someProject", "additionalSelector", "oneMore"));

        assertEquals(linkedMap(
                "someValue.someProject.additionalSelector", "1",
                "someValue.someProject.*", "2",
                "someValue.*.additionalSelector", "3",
                "someValue.*.*", "4",
                "*.someProject.additionalSelector", "5",
                "*.someProject.*", "6",
                "*.*.additionalSelector", "7",
                "*.*.*", "8"
        ), hook.getSettingsValuesWithSelector(true, "someValue", "someProject", "additionalSelector"));

        assertEquals(linkedMap(
                "someValue.someProject.*", "2",
                "someValue.*.*", "4",
                "*.someProject.*", "6",
                "*.*.*", "8"
        ), hook.getSettingsValuesWithSelector(true, "someValue", "someProject", "additionalSelector2"));

        assertEquals(linkedMap(
                "someValue.*.*", "4",
                "*.*.*", "8"
        ), hook.getSettingsValuesWithSelector(true, "someValue", "someProject2", "additionalSelector2"));

        assertEquals(linkedMap("*.*.*", "8"), hook.getSettingsValuesWithSelector(true, "someValue2", "someProject2", "additionalSelector2"));

        assertEquals(linkedMap(), hook.getSettingsValuesWithSelector(true, "someValue2", "someProject2", "additionalSelector2", "oneMore"));
    }

    private LinkedHashMap<String, String> linkedMap(String... values) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i++) {
            if (i % 2 == 0) {
                map.put(values[i], values[i + 1]);
            }
        }
        return map;
    }

    private static class TestHook extends ActionHook {

        private final String settingsProperties;

        public TestHook(String settingsProperties) {
            super(ItemType.WORKITEM, ActionType.SAVE, "description");
            this.settingsProperties = settingsProperties;
        }

        @Override
        public HookModel loadSettings(boolean forceUpdate) {
            return new HookModel(true, "1.0", settingsProperties);
        }

        @Override
        public @NotNull HookExecutor getExecutor() {
            return new HookExecutor() {
                // just empty executor
            };
        }

        @Override
        public String getDefaultSettings() {
            return "";
        }
    }
}