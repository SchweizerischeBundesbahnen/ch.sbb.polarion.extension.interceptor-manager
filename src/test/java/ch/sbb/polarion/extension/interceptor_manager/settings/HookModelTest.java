package ch.sbb.polarion.extension.interceptor_manager.settings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class HookModelTest {

    @Test
    void modelsWithTheSameContentAreEqual() {
        assertEquals(new HookModel(true, "1.0", "projects=*"), new HookModel(true, "1.0", "projects=*"));
        assertEquals(new HookModel(true, "1.0", "projects=*").hashCode(), new HookModel(true, "1.0", "projects=*").hashCode());
    }

    @Test
    void modelsWithDifferentContentAreNotEqual() {
        assertNotEquals(new HookModel(true, "1.0", "projects=*"), new HookModel(false, "1.0", "projects=*"));
        assertNotEquals(new HookModel(true, "1.0", "projects=*"), new HookModel(true, "1.0", "projects=p1"));
    }
}
