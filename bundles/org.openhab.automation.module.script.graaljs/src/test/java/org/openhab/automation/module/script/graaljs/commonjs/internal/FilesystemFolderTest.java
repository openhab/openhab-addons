/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.automation.module.script.graaljs.commonjs.internal;

import org.graalvm.polyglot.Context;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.openhab.automation.module.script.graaljs.commonjs.internal.AssertUtils.assertAbsent;

/**
 * Tests filesystem access for module loader.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
public class FilesystemFolderTest {
    private File file = new File(
            "src/test/resources/" + getClass().getPackage().getName().replace(".", "/") + "/test1");
    private FilesystemFolder root = FilesystemFolder.create(file, "UTF-8");
    private File subfile = new File(file, "subdir");
    private File subsubfile = new File(subfile, "subsubdir");
    private String rootPath = file.getAbsolutePath().substring(0, file.getAbsolutePath().indexOf(File.separator));

    @Test
    public void rootFolderHasTheExpectedProperties() {
        assertTrue(root.getPath().startsWith(rootPath));
        assertTrue(root.getPath().endsWith(file.getPath() + File.separator));
        assertAbsent(root.getParent());
    }

    @Test
    public void getFileReturnsTheContentOfTheFileWhenItExists() {
        assertTrue(root.tryReadFile("foo.js").get().contains("foo"));
    }

    @Test
    public void getFileReturnsNullWhenFileDoesNotExists() {
        assertAbsent(root.tryReadFile("invalid"));
    }

    @Test
    public void getFolderReturnsAnObjectWithTheExpectedProperties() {
        Folder sub = root.getFolder("subdir").get();
        assertTrue(sub.getPath().startsWith(rootPath));
        assertTrue(sub.getPath().endsWith(subfile.getPath() + File.separator));
        assertSame(root, sub.getParent().get());
        Folder subsub = sub.getFolder("subsubdir").get();
        assertTrue(subsub.getPath().startsWith(rootPath));
        assertTrue(subsub.getPath().endsWith(subsubfile.getPath() + File.separator));
        assertSame(sub, subsub.getParent().get());
    }

    @Test
    public void getFolderReturnsNullWhenFolderDoesNotExist() {
        assertAbsent(root.getFolder("invalid"));
    }

    @Test
    public void getFileCanBeUsedOnSubFolderIfFileExist() {
        assertTrue(root.getFolder("subdir").get().tryReadFile("bar.js").get().contains("bar"));
    }

    @Test
    public void filesystemFolderWorksWhenUsedForReal() throws Throwable {
        Context engine = createContext();
        Require.enable(engine, root, Collections.emptyList());
        assertEquals("spam", engine.eval("js", "require('./foo').bar.spam.spam").asString());
    }

    static Context createContext() {
        return Context.newBuilder("js").allowExperimentalOptions(true).allowAllAccess(true)
                .option("js.syntax-extensions", "true").option("js.nashorn-compat", "true")
                .option("js.ecmascript-version", "2020").build();
    }
}
