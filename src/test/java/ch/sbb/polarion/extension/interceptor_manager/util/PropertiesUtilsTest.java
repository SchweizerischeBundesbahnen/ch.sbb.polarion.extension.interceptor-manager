package ch.sbb.polarion.extension.interceptor_manager.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class PropertiesUtilsTest {

    @Test
    public void testBuild() {
        assertEquals(PropertiesUtils.build("a", "b", "c", "d"), "a=b" + System.lineSeparator() + "c=d");
    }

    @Test
    public void testBuildWithDescription() {
        assertEquals(PropertiesUtils.buildWithDescription("desc1", "a", "b", "desc2", "c", "d"), "# desc1" + System.lineSeparator() + "a=b" + System.lineSeparator() + "# desc2" + System.lineSeparator() + "c=d" + System.lineSeparator());
    }

    @Test
    public void testSelectorsCombinations() {
        assertEquals(List.of(), PropertiesUtils.generateSelectorsCombinations());
        assertEquals(List.of("someValue", "*"), PropertiesUtils.generateSelectorsCombinations("someValue"));
        assertEquals(List.of("null", "*"), PropertiesUtils.generateSelectorsCombinations((String) null));

        assertEquals(List.of(
                "someValue.someProject",
                "someValue.*",
                "*.someProject",
                "*.*"
        ), PropertiesUtils.generateSelectorsCombinations("someValue", "someProject"));

        assertEquals(List.of(
                "someValue.someProject.additionalSelector",
                "someValue.someProject.*",
                "someValue.*.additionalSelector",
                "someValue.*.*",
                "*.someProject.additionalSelector",
                "*.someProject.*",
                "*.*.additionalSelector",
                "*.*.*"
        ), PropertiesUtils.generateSelectorsCombinations("someValue", "someProject", "additionalSelector"));

        assertEquals(List.of(
                "someValue.someProject.additionalSelector.fourthSelector",
                "someValue.someProject.additionalSelector.*",
                "someValue.someProject.*.fourthSelector",
                "someValue.someProject.*.*",
                "someValue.*.additionalSelector.fourthSelector",
                "someValue.*.additionalSelector.*",
                "someValue.*.*.fourthSelector",
                "someValue.*.*.*",
                "*.someProject.additionalSelector.fourthSelector",
                "*.someProject.additionalSelector.*",
                "*.someProject.*.fourthSelector",
                "*.someProject.*.*",
                "*.*.additionalSelector.fourthSelector",
                "*.*.additionalSelector.*",
                "*.*.*.fourthSelector",
                "*.*.*.*"
        ), PropertiesUtils.generateSelectorsCombinations("someValue", "someProject", "additionalSelector", "fourthSelector"));
    }
}
