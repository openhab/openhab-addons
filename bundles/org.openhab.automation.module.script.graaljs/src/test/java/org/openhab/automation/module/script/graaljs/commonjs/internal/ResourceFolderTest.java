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

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.openhab.automation.module.script.graaljs.commonjs.internal.AssertUtils.assertAbsent;
import static org.openhab.automation.module.script.graaljs.commonjs.internal.AssertUtils.assertPresent;

/**
 * Tests resource access for module loader.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
public class ResourceFolderTest {
  private ResourceFolder root =
      ResourceFolder.create(
          getClass().getClassLoader(), getClass().getPackage().getName().replace(".","/") + "/test1", "UTF-8");

  @Test
  public void rootFolderHasTheExpectedProperties() {
    assertEquals("/", root.getPath());
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
    assertEquals("/subdir/", sub.getPath());
    assertSame(root, sub.getParent().get());
    Folder subsub = sub.getFolder("subsubdir").get();
    assertEquals("/subdir/subsubdir/", subsub.getPath());
    assertSame(sub, subsub.getParent().get());
  }

  @Test
  public void getFolderNeverReturnsNullBecauseItCannot() {
    assertPresent(root.getFolder("subdir"));
    assertPresent(root.getFolder("invalid"));
  }

  @Test
  public void getFileCanBeUsedOnSubFolderIfFileExist() {
    assertTrue(root.getFolder("subdir").get().tryReadFile("bar.js").get().contains("bar"));
  }

  @Test
  public void resourceFolderWorksWhenUsedForReal() {
    Context engine = createContext();
    Require.enable(engine, root, Collections.emptyList());
    assertEquals("spam", engine.eval("js", "require('./foo').bar.spam.spam").asString());
  }

  static Context createContext(){
    return Context.newBuilder("js")
            .allowExperimentalOptions(true)
            .allowAllAccess(true)
            .option("js.syntax-extensions", "true")
            .option("js.nashorn-compat", "true")
            .option("js.ecmascript-version", "2020").build();
  }
}
