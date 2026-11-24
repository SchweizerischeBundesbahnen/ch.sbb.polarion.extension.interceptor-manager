package ch.sbb.polarion.extension.interceptor_manager.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HookJarUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void fileWhichIsNotAJarIsReportedAsSuch() throws IOException {
        File file = Files.writeString(tempDir.resolve("not-a-jar.jar"), "this is not a jar").toFile();

        IOException e = assertThrows(IOException.class, () -> HookJarUtils.loadJarReturnHookClass(file));
        assertEquals("Jar file " + file.getPath() + " has no MANIFEST, or is not a jar file", e.getMessage());
    }

    @Test
    void jarWithoutManifestIsReportedAsSuch() throws IOException {
        File file = tempDir.resolve("no-manifest.jar").toFile();
        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file))) {
            zip.putNextEntry(new ZipEntry("readme.txt"));
            zip.write("no manifest here".getBytes());
            zip.closeEntry();
        }

        IOException e = assertThrows(IOException.class, () -> HookJarUtils.loadJarReturnHookClass(file));
        assertEquals("Jar file " + file.getPath() + " has no MANIFEST, or is not a jar file", e.getMessage());
    }

    @Test
    void jarWithoutMainClassIsReportedAsSuch() throws IOException {
        File file = tempDir.resolve("no-main-class.jar").toFile();
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (JarOutputStream jar = new JarOutputStream(new FileOutputStream(file), manifest)) {
            jar.putNextEntry(new ZipEntry("readme.txt"));
            jar.write("no Main-Class here".getBytes());
            jar.closeEntry();
        }

        ClassNotFoundException e = assertThrows(ClassNotFoundException.class, () -> HookJarUtils.loadJarReturnHookClass(file));
        assertEquals("Jar file " + file.getPath() + " does not contain Main-Class attribute in the MANIFEST", e.getMessage());
    }
}
