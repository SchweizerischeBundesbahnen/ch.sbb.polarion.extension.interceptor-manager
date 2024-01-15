package ch.sbb.polarion.extension.interceptor.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
