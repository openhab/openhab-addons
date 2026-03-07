/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.automation.java223.internal.strategy.jarloader;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test for creating JarClassLoader and adding JAR files with static resources to it.
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public class JarClassLoaderTest {

    @TempDir
    @Nullable
    Path tempDir;

    @Test
    public void testGetResourceAsStream() throws IOException {
        assert (tempDir != null);
        Path jarPath = tempDir.resolve("test.jar");
        createJar(jarPath, "test.txt", "Hello World");

        JarClassLoader jarClassLoader = new JarClassLoader(getClass().getClassLoader());
        jarClassLoader.addJar(jarPath);

        try (InputStream is = jarClassLoader.getResourceAsStream("test.txt")) {
            assertNotNull(is, "Resource 'test.txt' should be found");
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals("Hello World", content);
        }
    }

    private void createJar(Path jarPath, String entryName, String content) throws IOException {
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarPath.toFile()))) {
            JarEntry entry = new JarEntry(entryName);
            jos.putNextEntry(entry);
            jos.write(content.getBytes(StandardCharsets.UTF_8));
            jos.closeEntry();
        }
    }
}
