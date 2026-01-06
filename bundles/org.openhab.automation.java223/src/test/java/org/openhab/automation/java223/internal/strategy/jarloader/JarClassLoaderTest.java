package org.openhab.automation.java223.internal.strategy.jarloader;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class JarClassLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    public void testGetResourceAsStream() throws IOException {
        Path jarPath = tempDir.resolve("test.jar");
        createJar(jarPath, "test.txt", "Hello World");

        JarClassLoader jarClassLoader = new JarClassLoader(getClass().getClassLoader());
        jarClassLoader.addJar(jarPath);

        try (InputStream is = jarClassLoader.getResourceAsStream("test.txt")) {
            assertNotNull(is, "Resource 'test.txt' should be found");
            String content = new String(is.readAllBytes());
            assertEquals("Hello World", content);
        }
    }

    private void createJar(Path jarPath, String entryName, String content) throws IOException {
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarPath.toFile()))) {
            JarEntry entry = new JarEntry(entryName);
            jos.putNextEntry(entry);
            jos.write(content.getBytes());
            jos.closeEntry();
        }
    }
}
