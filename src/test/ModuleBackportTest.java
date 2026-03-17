package test;

import j9compat.Module;
import j9compat.ModuleBackport;
import j9compat.ModuleDescriptor;
import j9compat.ModuleLayer;

import static test.BackportTestRunner.*;

/**
 * Tests for module backports.
 */
public final class ModuleBackportTest {

    static void run() {
        section("ModuleBackport");

        Module module = ModuleBackport.getModule(ModuleBackportTest.class);
        assertTrue(module != null, "ModuleBackport.getModule: returns module");
        assertTrue(!module.isNamed(), "ModuleBackport.getModule: unnamed module");
        assertEquals(ModuleLayer.boot().unnamedModule(), module,
                "ModuleBackport.getModule: boot unnamed module");

        ModuleDescriptor descriptor = ModuleDescriptor.newModule("demo")
                .requires("java.base")
                .exports("demo.pkg")
                .build();
        assertEquals("demo", descriptor.name(), "ModuleDescriptor builder: name set");
        assertTrue(descriptor.requires().size() == 1,
                "ModuleDescriptor builder: requires populated");
    }
}
