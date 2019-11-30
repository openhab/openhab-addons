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
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests module loading.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@RunWith(MockitoJUnitRunner.class)
public class ModuleTest {
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    AbstractFolder root;
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    AbstractFolder rootnm;
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    AbstractFolder sub1;
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    AbstractFolder sub1nm;
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    AbstractFolder sub1sub1;
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    AbstractFolder nmsub1;
    private Context engine;
    private Module require;
    private String rootPath = "src/test/resources/" + getClass().getPackage().getName().replace(".", "/");

    @Before
    public void before() {
        when(root.getPath()).thenReturn("/");
        when(root.getFolder("node_modules")).thenReturn(Optional.of(rootnm));
        when(root.getFolder("sub1")).thenReturn(Optional.of(sub1));
        when(root.getParent()).thenReturn(Optional.empty());
        when(root.tryReadFile("file1.js")).thenReturn(Optional.of("exports.file1 = 'file1';"));
        when(root.tryReadFile("file2.json")).thenReturn(Optional.of("{ \"file2\": \"file2\" }"));
        when(rootnm.getPath()).thenReturn("/node_modules/");
        when(rootnm.getParent()).thenReturn(Optional.of(root));
        when(rootnm.tryReadFile("nmfile1.js")).thenReturn(Optional.of("exports.nmfile1 = 'nmfile1';"));
        when(rootnm.getFolder("nmsub1")).thenReturn(Optional.of(nmsub1));
        when(nmsub1.tryReadFile("nmsub1file1.js")).thenReturn(Optional.of("exports.nmsub1file1 = 'nmsub1file1';"));
        when(nmsub1.getParent()).thenReturn(Optional.of(rootnm));
        when(sub1.getPath()).thenReturn("/sub1/");
        when(sub1.getParent()).thenReturn(Optional.of(root));
        when(sub1.getFolder("sub1")).thenReturn(Optional.of(sub1sub1));
        when(sub1.getFolder("node_modules")).thenReturn(Optional.of(sub1nm));
        when(sub1.tryReadFile("sub1file1.js")).thenReturn(Optional.of("exports.sub1file1 = 'sub1file1';"));
        when(sub1nm.getPath()).thenReturn("/sub1/node_modules/");
        when(sub1nm.tryReadFile("sub1nmfile1.js")).thenReturn(Optional.of("exports.sub1nmfile1 = 'sub1nmfile1';"));
        when(sub1sub1.getPath()).thenReturn("/sub1/sub1/");
        when(sub1sub1.tryReadFile("sub1sub1file1.js"))
                .thenReturn(Optional.of("exports.sub1sub1file1 = 'sub1sub1file1';"));

        engine = createContext();

        require = Require.enable(engine, root, Collections.emptyList());
    }

    private static Context createContext() {
        return Context.newBuilder("js").allowExperimentalOptions(true).allowAllAccess(true)
                .option("js.syntax-extensions", "true").option("js.nashorn-compat", "true")
                .option("js.ecmascript-version", "2020").build();
    }

    private String loadFromAndGet(String module, String property) {
        return require.require(module).getMember(property).asString();
    }

    @Test
    public void itCanLoadSimpleModules() {
        assertEquals("file1", loadFromAndGet("./file1.js", "file1"));
    }

    @Test
    public void itCanLoadSimpleJsonModules() {
        assertEquals("file2", loadFromAndGet("./file2.json", "file2"));
    }

    @Test
    public void itCanLoadModulesFromSubFolders() {
        assertEquals("sub1file1", loadFromAndGet("./sub1/sub1file1.js", "sub1file1"));
    }

    @Test
    public void itCanLoadModulesFromSubFoldersInNodeModules() {
        assertEquals("nmsub1file1", loadFromAndGet("nmsub1/nmsub1file1.js", "nmsub1file1"));
    }

    @Test
    public void itCanLoadModulesFromSubSubFolders() {
        assertEquals("sub1sub1file1", loadFromAndGet("./sub1/sub1/sub1sub1file1.js", "sub1sub1file1"));
    }

    @Test
    public void itCanLoadModulesFromParentFolders() {
        when(sub1.tryReadFile("sub1file1.js"))
                .thenReturn(Optional.of("exports.sub1file1 = require('../file1').file1;"));
        assertEquals("file1", loadFromAndGet("./sub1/sub1file1.js", "sub1file1"));
    }

    @Test
    public void itCanGoUpAndDownInFolders() {
        when(sub1.tryReadFile("sub1file1.js"))
                .thenReturn(Optional.of("exports.sub1file1 = require('../file1').file1;"));
        assertEquals("file1", loadFromAndGet("./sub1/../sub1/sub1file1.js", "sub1file1"));
    }

    @Test
    public void itCanGoUpAndDownInNodeModulesFolders() {
        assertEquals("nmsub1file1", loadFromAndGet("nmsub1/../nmsub1/nmsub1file1.js", "nmsub1file1"));
    }

    @Test
    public void itCanLoadModulesSpecifyingOnlyTheFolderWhenPackageJsonHasAMainFile() {
        Folder dir = mock(AbstractFolder.class, Answers.CALLS_REAL_METHODS);
        when(dir.tryReadFile("package.json")).thenReturn(Optional.of("{ \"main\": \"foo.js\" }"));
        when(dir.tryReadFile("foo.js")).thenReturn(Optional.of("exports.foo = 'foo';"));
        when(root.getFolder("dir")).thenReturn(Optional.of(dir));
        assertEquals("foo", loadFromAndGet("./dir", "foo"));
    }

    @Test
    public void itCanLoadModulesSpecifyingOnlyTheFolderWhenPackageJsonHasAMainFilePointingToAFileInSubDirectory() {
        Folder dir = mock(AbstractFolder.class, Answers.CALLS_REAL_METHODS);
        Folder lib = mock(AbstractFolder.class, Answers.CALLS_REAL_METHODS);
        when(dir.tryReadFile("package.json")).thenReturn(Optional.of("{ \"main\": \"lib/foo.js\" }"));
        when(dir.getFolder("lib")).thenReturn(Optional.of(lib));
        when(lib.tryReadFile("foo.js")).thenReturn(Optional.of("exports.foo = 'foo';"));
        when(root.getFolder("dir")).thenReturn(Optional.of(dir));
        assertEquals("foo", loadFromAndGet("./dir", "foo"));
    }

    @Test
    public void itCanLoadModulesSpecifyingOnlyTheFolderWhenPackageJsonHasAMainFilePointingToASubDirectory() {
        Folder dir = mock(AbstractFolder.class, Answers.CALLS_REAL_METHODS);
        Folder lib = mock(AbstractFolder.class, Answers.CALLS_REAL_METHODS);
        when(root.getFolder("dir")).thenReturn(Optional.of(dir));
        when(dir.getFolder("lib")).thenReturn(Optional.of(lib));
        when(dir.tryReadFile("package.json")).thenReturn(Optional.of("{\"main\": \"./lib\"}"));
        when(lib.tryReadFile("index.js")).thenReturn(Optional.of("exports.foo = 'foo';"));
        assertEquals("foo", loadFromAndGet("./dir", "foo"));
    }

    @Test
    public void itCanLoadModulesSpecifyingOnlyTheFolderWhenPackageJsonHasAMainFilePointingToAFileInSubDirectoryReferencingOtherFilesInThisDirectory() {
        Folder dir = mock(AbstractFolder.class, Answers.CALLS_REAL_METHODS);
        Folder lib = mock(AbstractFolder.class, Answers.CALLS_REAL_METHODS);
        when(dir.tryReadFile("package.json")).thenReturn(Optional.of("{ \"main\": \"lib/foo.js\" }"));
        when(dir.getFolder("lib")).thenReturn(Optional.of(lib));
        when(lib.tryReadFile("foo.js")).thenReturn(Optional.of("exports.bar = require('./bar');"));
        when(lib.tryReadFile("bar.js")).thenReturn(Optional.of("exports.bar = 'bar';"));
        when(root.getFolder("dir")).thenReturn(Optional.of(dir));
        assertEquals("bar", require.require("./dir").getMember("bar").getMember("bar").asString());
    }

    @Test
    public void itCanLoadModulesSpecifyingOnlyTheFolderWhenIndexJsIsPresent() {
        Folder dir = mock(AbstractFolder.class, Answers.CALLS_REAL_METHODS);
        when(dir.tryReadFile("index.js")).thenReturn(Optional.of("exports.foo = 'foo';"));
        when(root.getFolder("dir")).thenReturn(Optional.of(dir));
        assertEquals("foo", loadFromAndGet("./dir", "foo"));
    }

    @Test
    public void itCanLoadModulesSpecifyingOnlyTheFolderWhenIndexJsIsPresentEvenIfPackageJsonExists() {
        Folder dir = mock(AbstractFolder.class, Answers.CALLS_REAL_METHODS);
        when(dir.tryReadFile("package.json")).thenReturn(Optional.of("{ }"));
        when(dir.tryReadFile("index.js")).thenReturn(Optional.of("exports.foo = 'foo';"));
        when(root.getFolder("dir")).thenReturn(Optional.of(dir));
        assertEquals("foo", loadFromAndGet("./dir", "foo"));
    }

    @Test
    public void itUsesNodeModulesOnlyForNonPrefixedNames() {
        assertEquals("nmfile1", loadFromAndGet("nmfile1", "nmfile1"));
    }

    @Test
    public void itFallbacksToNodeModulesWhenUsingPrefixedName() {
        assertEquals("nmfile1", loadFromAndGet("./nmfile1", "nmfile1"));
    }

    @Test(expected = PolyglotException.class)
    public void itDoesNotUseModulesOutsideOfNodeModulesForNonPrefixedNames() {
        require.require("file1.js");
    }

    @Test
    public void itUsesNodeModulesFromSubFolderForSubRequiresFromModuleInSubFolder() {
        when(sub1.tryReadFile("sub1file1.js"))
                .thenReturn(Optional.of("exports.sub1nmfile1 = require('sub1nmfile1').sub1nmfile1;"));
        assertEquals("sub1nmfile1", loadFromAndGet("./sub1/sub1file1", "sub1nmfile1"));
    }

    @Test
    public void itLooksAtParentFoldersWhenTryingToResolveFromNodeModules() {
        when(sub1.tryReadFile("sub1file1.js")).thenReturn(Optional.of("exports.nmfile1 = require('nmfile1').nmfile1;"));
        assertEquals("nmfile1", loadFromAndGet("./sub1/sub1file1", "nmfile1"));
    }

    @Test
    public void itCanUseDotToReferenceToTheCurrentFolder() {
        assertEquals("file1", loadFromAndGet("./file1.js", "file1"));
    }

    @Test
    public void itCanUseDotAndDoubleDotsToGoBackAndForward() {
        assertEquals("file1", loadFromAndGet("./sub1/.././sub1/../file1.js", "file1"));
    }

    @Test
    public void thePathOfModulesContainsNoDots() {
        when(root.tryReadFile("file1.js")).thenReturn(Optional.of("exports.path = module.filename"));
        assertEquals("/file1.js", loadFromAndGet("./sub1/.././sub1/../file1.js", "path"));
    }

    @Test
    public void itCanLoadModuleIfTheExtensionIsOmitted() {
        assertEquals("file1", loadFromAndGet("./file1", "file1"));
    }

    @Test(expected = PolyglotException.class)
    public void itThrowsAnExceptionIfFileDoesNotExists() {
        require.require("./invalid");
    }

    @Test(expected = PolyglotException.class)
    public void itThrowsAnExceptionIfSubFileDoesNotExists() {
        require.require("./sub1/invalid");
    }

    @Test(expected = PolyglotException.class)
    public void itThrowsEnExceptionIfFolderDoesNotExists() {
        require.require("./invalid/file1.js");
    }

    @Test(expected = PolyglotException.class)
    public void itThrowsEnExceptionIfSubFolderDoesNotExists() {
        require.require("./sub1/invalid/file1.js");
    }

    @Test(expected = PolyglotException.class)
    public void itThrowsAnExceptionIfTryingToGoAboveTheTopLevelFolder() {
        // We need two ".." because otherwise the resolving attempts to load from "node_modules" and
        // ".." validly points to the root folder there.
        require.require("../../file1.js");
    }

    @Test
    public void theExceptionThrownForAnUnknownFileCanBeCaughtInJavaScriptAndHasTheProperCode() {
        String code = engine
                .eval("js", "(function() { try { require('./invalid'); } catch (ex) { return ex.code; } })();")
                .asString();
        assertEquals("MODULE_NOT_FOUND", code);
    }

    @Test
    public void rootModulesExposeTheExpectedFields() {
        Value module = engine.eval("js", "module");
        Value exports = engine.eval("js", "exports");
        Value main = engine.eval("js", "require.main");

        AssertUtils.assertValueEquals(exports, module.getMember("exports"));
        assertEquals(new ArrayList(), module.getMember("children").as(ArrayList.class));
        assertEquals("<main>", module.getMember("filename").asString());
        assertEquals("<main>", module.getMember("id").asString());
        assertTrue(module.getMember("loaded").asBoolean());
        assertTrue(module.getMember("parent").isNull());
        assertNotNull(exports);
        AssertUtils.assertValueEquals(module, main);
    }

    @Test
    public void topLevelModulesExposeTheExpectedFields() {
        when(root.tryReadFile("file1.js")).thenReturn(Optional.of(
                "exports._module = module; exports._exports = exports; exports._main = require.main; exports._filename = __filename; exports._dirname = __dirname;"));

        Value top = engine.eval("js", "module");
        Value module = engine.eval("js", "require('./file1')._module");
        Value exports = engine.eval("js", "require('./file1')._exports");
        Value main = engine.eval("js", "require('./file1')._main");

        AssertUtils.assertValueEquals(exports, module.getMember("exports"));
        assertEquals(new ArrayList(), module.getMember("children").as(ArrayList.class));
        assertEquals("/file1.js", module.getMember("filename").asString());
        assertEquals("/file1.js", module.getMember("id").asString());
        assertTrue(module.getMember("loaded").asBoolean());
        AssertUtils.assertValueEquals(top, module.getMember("parent"));
        assertNotNull(exports);
        AssertUtils.assertValueEquals(top, main);

        assertEquals("file1.js", exports.getMember("_filename").asString());
        assertEquals("", exports.getMember("_dirname").asString());
    }

    @Test
    public void subModulesExposeTheExpectedFields() {
        when(sub1.tryReadFile("sub1file1.js")).thenReturn(Optional.of(
                "exports._module = module; exports._exports = exports; exports._main = require.main; exports._filename = __filename; exports._dirname = __dirname"));

        Value top = engine.eval("js", "module");
        Value module = engine.eval("js", "require('./sub1/sub1file1')._module");
        Value exports = engine.eval("js", "require('./sub1/sub1file1')._exports");
        Value main = engine.eval("js", "require('./sub1/sub1file1')._main");

        AssertUtils.assertValueEquals(exports, module.getMember("exports"));
        assertEquals(new ArrayList(), module.getMember("children").as(ArrayList.class));
        assertEquals("/sub1/sub1file1.js", module.getMember("filename").asString());
        assertEquals("/sub1/sub1file1.js", module.getMember("id").asString());
        assertTrue(module.getMember("loaded").asBoolean());
        AssertUtils.assertValueEquals(top, module.getMember("parent"));
        assertNotNull(exports);
        AssertUtils.assertValueEquals(top, main);

        assertEquals("sub1file1.js", exports.getMember("_filename").asString());
        assertEquals("/sub1", exports.getMember("_dirname").asString());
    }

    @Test
    public void subSubModulesExposeTheExpectedFields() {
        when(sub1sub1.tryReadFile("sub1sub1file1.js")).thenReturn(
                Optional.of("exports._module = module; exports._exports = exports; exports._main = require.main;"));

        Value top = engine.eval("js", "module");
        Value module = engine.eval("js", "require('./sub1/sub1/sub1sub1file1')._module");
        Value exports = engine.eval("js", "require('./sub1/sub1/sub1sub1file1')._exports");
        Value main = engine.eval("js", "require('./sub1/sub1/sub1sub1file1')._main");

        AssertUtils.assertValueEquals(exports, module.getMember("exports"));
        assertEquals(new ArrayList(), module.getMember("children").as(ArrayList.class));
        assertEquals("/sub1/sub1/sub1sub1file1.js", module.getMember("filename").asString());
        assertEquals("/sub1/sub1/sub1sub1file1.js", module.getMember("id").asString());
        assertTrue(module.getMember("loaded").asBoolean());
        AssertUtils.assertValueEquals(top, module.getMember("parent"));
        assertNotNull(exports);
        AssertUtils.assertValueEquals(top, main);
    }

    @Test
    public void requireInRequiredModuleYieldExpectedParentAndChildren() {
        when(root.tryReadFile("file1.js"))
                .thenReturn(Optional.of("exports._module = module; exports.sub = require('./sub1/sub1file1');"));
        when(sub1.tryReadFile("sub1file1.js")).thenReturn(Optional.of("exports._module = module;"));

        Value top = engine.eval("js", "module");
        Value module = engine.eval("js", "require('./file1')._module");
        Value subModule = engine.eval("js", "require('./file1').sub._module");

        assertTrue(top.getMember("parent").isNull());
        AssertUtils.assertValueEquals(top, module.getMember("parent"));
        AssertUtils.assertValueEquals(module, subModule.getMember("parent"));
        AssertUtils.assertValueEquals(module, (top.getMember("children")).getArrayElement(0));
        AssertUtils.assertValueEquals(subModule, (module.getMember("children")).getArrayElement(0));
        assertEquals(new ArrayList(), subModule.getMember("children").as(ArrayList.class));
    }

    @Test
    public void loadedIsFalseWhileModuleIsLoadingAndTrueAfter() {
        when(root.tryReadFile("file1.js"))
                .thenReturn(Optional.of("exports._module = module; exports._loaded = module.loaded;"));

        Value top = engine.eval("js", "module");
        Value module = engine.eval("js", "require('./file1')._module");
        boolean loaded = engine.eval("js", "require('./file1')._loaded").asBoolean();

        assertTrue(top.getMember("loaded").asBoolean());
        assertFalse(loaded);
        assertTrue(module.getMember("loaded").asBoolean());
    }

    @Test
    public void loadingTheSameModuleTwiceYieldsTheSameObject() {
        Value first = engine.eval("js", "require('./file1');");
        Value second = engine.eval("js", "require('./file1');");
        AssertUtils.assertValueEquals(first, second);
    }

    @Test
    public void loadingTheSameModuleFromASubModuleYieldsTheSameObject() {
        when(root.tryReadFile("file2.js")).thenReturn(Optional.of("exports.sub = require('./file1');"));
        Value first = engine.eval("js", "require('./file1');");
        Value second = engine.eval("js", "require('./file2').sub;");
        AssertUtils.assertValueEquals(first, second);
    }

    @Test
    public void loadingTheSameModuleFromASubPathYieldsTheSameObject() {
        when(sub1.tryReadFile("sub1file1.js")).thenReturn(Optional.of("exports.sub = require('../file1');"));
        Value first = engine.eval("js", "require('./file1');");
        Value second = engine.eval("js", "require('./sub1/sub1file1').sub;");
        AssertUtils.assertValueEquals(first, second);
    }

    @Test
    public void scriptCodeCanReplaceTheModuleExportsSymbol() {
        when(root.tryReadFile("file1.js")).thenReturn(Optional.of("module.exports = { 'foo': 'bar' }"));
        assertEquals("bar", engine.eval("js", "require('./file1').foo;").asString());
    }

    @Test
    public void itIsPossibleToRegisterGlobalVariablesForAllModules() {
        engine.getBindings("js").putMember("bar", "bar");
        when(root.tryReadFile("file1.js")).thenReturn(Optional.of("exports.foo = function() { return bar; }"));
        assertEquals("bar", engine.eval("js", "require('./file1').foo();").asString());
    }

    @Test
    public void engineScopeVariablesAreVisibleDuringModuleLoad() {
        engine.getBindings("js").putMember("bar", "bar");
        when(root.tryReadFile("file1.js"))
                .thenReturn(Optional.of("var found = bar == 'bar'; exports.foo = function() { return found; }"));
        assertTrue(engine.eval("js", "require('./file1').foo();").asBoolean());
    }

    @Test
    public void itCanLoadModulesFromModulesFromModules() {
        when(root.tryReadFile("file1.js")).thenReturn(Optional.of("exports.sub = require('./file2.js');"));
        when(root.tryReadFile("file2.js")).thenReturn(Optional.of("exports.sub = require('./file3.js');"));
        when(root.tryReadFile("file3.js")).thenReturn(Optional.of("exports.foo = 'bar';"));

        assertEquals("bar", engine.eval("js", "require('./file1.js').sub.sub.foo").asString());
    }

    // Check for https://github.com/coveo/nashorn-commonjs-modules/issues/2
    @Test
    public void itCanCallFunctionsNamedGetFromModules() {
        when(root.tryReadFile("file1.js")).thenReturn(Optional.of("exports.get = function(foo) { return 'bar'; };"));

        assertEquals("bar", engine.eval("js", "require('./file1.js').get(123, 456)").asString());
    }

    // This one failed on more recent ones too
    @Test
    public void anotherCheckForIssueNumber3() {
        when(root.tryReadFile("file1.js")).thenReturn(Optional.of(
                "var a = require('./file2'); function b() {}; b.prototype = Object.create(a.prototype, {});"));
        when(root.tryReadFile("file2.js")).thenReturn(
                Optional.of("module.exports = a; function a() {}; a.prototype = Object.create(Object.prototype, {})"));
        require = Require.enable(engine, root, Collections.emptyList());
        engine.eval("js", "require('./file1');");
    }

    // Check for https://github.com/coveo/nashorn-commonjs-modules/issues/4
    @Test
    public void itSupportOverwritingExportsWithAString() {
        when(root.tryReadFile("file1.js")).thenReturn(Optional.of("module.exports = 'foo';"));
        assertEquals("foo", engine.eval("js", "require('./file1.js')").asString());
    }

    // Check for https://github.com/coveo/nashorn-commonjs-modules/issues/4
    @Test
    public void itSupportOverwritingExportsWithAnInteger() {
        when(root.tryReadFile("file1.js")).thenReturn(Optional.of("module.exports = 123;"));
        assertEquals(123, engine.eval("js", "require('./file1.js')").asInt());
    }

    // Checks for https://github.com/coveo/nashorn-commonjs-modules/pull/14

    @Test
    public void itCanShortCircuitCircularRequireReferences() {
        File file = new File(rootPath + "/test4/cycles");
        FilesystemFolder root = FilesystemFolder.create(file, "UTF-8");
        require = Require.enable(engine, root, Collections.emptyList());
        engine.eval("js", "require('./main.js')");
    }

    @Test
    public void itCanShortCircuitDeepCircularRequireReferences() {
        File file = new File(rootPath + "/test4/deep");
        FilesystemFolder root = FilesystemFolder.create(file, "UTF-8");
        require = Require.enable(engine, root, Collections.emptyList());
        engine.eval("js", "require('./main.js')");
    }

    // Checks for https://github.com/coveo/nashorn-commonjs-modules/issues/15

    @Test
    public void itCanDefinePropertiesOnExportsObject() {
        when(root.tryReadFile("file1.js"))
                .thenReturn(Optional.of("Object.defineProperty(exports, '__esModule', { value: true });"));
        engine.eval("js", "require('./file1.js')");
    }

    @Test
    public void itIncludesFilenameInException() {
        when(root.tryReadFile("file1.js"))
                .thenReturn(Optional.of("\n\nexports.foo = function() { throw \"bad thing\";};"));
        try {
            engine.eval("js", "require('./file1').foo();");
            fail("should throw exception");
        } catch (PolyglotException e) {
            for (StackTraceElement ste : e.getStackTrace()) {
                assert ste.getFileName() != null;
                if (ste.getFileName().contains("file1.js")) {
                    return;
                }
            }

            //last option is that the filename is in the message
            assertTrue(e.getMessage() + " contains file1.js", e.getMessage().contains("file1.js"));
        }
    }

    // Checks for https://github.com/coveo/nashorn-commonjs-modules/issues/22

    @Test
    public void itCanLoadModulesWhoseLastLineIsAComment() {
        when(root.tryReadFile("file1.js")).thenReturn(Optional.of("exports.foo = \"bar\";\n// foo"));
        assertEquals("bar", engine.eval("js", "require('./file1.js').foo").asString());
    }

    @Test
    public void itCanLoadModulesFromSuppliedLibFolders() {

        Folder libdir = mock(AbstractFolder.class, Answers.CALLS_REAL_METHODS);
        when(libdir.getParent()).thenReturn(Optional.empty());
        require = Require.enable(engine, root, Collections.singletonList(libdir));

        when(libdir.tryReadFile("lib1.js")).thenReturn(Optional.of("exports.foo = \"baz\";"));

        assertEquals("baz", engine.eval("js", "require('lib1').foo").asString());
    }

    @Test
    public void itCanLoadModulesFromSuppliedLibFoldersInOrder() {

        Folder libdir1 = mock(AbstractFolder.class, Answers.CALLS_REAL_METHODS);
        when(libdir1.getParent()).thenReturn(Optional.empty());
        Folder libdir2 = mock(AbstractFolder.class, Answers.CALLS_REAL_METHODS);
        require = Require.enable(engine, root, Arrays.asList(libdir1, libdir2));

        when(libdir1.tryReadFile("lib1.js")).thenReturn(Optional.of("exports.foo = \"baz1\";"));

        assertEquals("baz1", engine.eval("js", "require('lib1.js').foo").asString());
    }
}
