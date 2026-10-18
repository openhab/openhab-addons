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
package org.openhab.automation.jsscripting;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openhab.core.automation.module.script.ScriptEngineFactory.CONTEXT_KEY_DEPENDENCY_LISTENER;
import static org.openhab.core.automation.module.script.ScriptEngineFactory.CONTEXT_KEY_ENGINE_IDENTIFIER;
import static org.openhab.core.automation.module.script.ScriptEngineFactory.CONTEXT_KEY_EXTENSION_ACCESSOR;
import static org.openhab.core.automation.module.script.internal.handler.AbstractScriptModuleHandler.CONTEXT_KEY_MODULE_TYPE_ID;

import java.util.Map;
import java.util.Objects;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.openhab.core.automation.module.script.ScriptExtensionAccessor;
import org.openhab.core.automation.module.script.internal.handler.ScriptActionHandler;

/**
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class OpenhabGraalJSScriptEngineTest extends GraalJSOSGiTest {
    private static final String SCRIPT_FILENAME = "itest.js";
    private static final String RULE_UID = "itest";
    private static final String ENGINE_IDENTIFIER = "itest-engine";

    private @Mock @NonNullByDefault({}) ScriptExtensionAccessor scriptExtensionAccessor;

    private @NonNullByDefault({}) ScriptEngine scriptEngine;

    OpenhabGraalJSScriptEngineTest() {
        super(Map.of("injectionEnabledV2", 0, "injectionCachingEnabled", false));
    }

    @Override
    @BeforeEach
    public void beforeEach() throws Exception {
        super.beforeEach();

        scriptEngine = Objects.requireNonNull(scriptEngineFactory.createScriptEngine(SCRIPT_TYPE),
                "Failed to create script engine for script type: " + SCRIPT_TYPE);
        ScriptContext context = scriptEngine.getContext();
        context.setAttribute(CONTEXT_KEY_ENGINE_IDENTIFIER, ENGINE_IDENTIFIER, ScriptContext.ENGINE_SCOPE);
        context.setAttribute(CONTEXT_KEY_EXTENSION_ACCESSOR, scriptExtensionAccessor, ScriptContext.ENGINE_SCOPE);
        context.setAttribute(CONTEXT_KEY_DEPENDENCY_LISTENER, jsDependencyTracker.getTracker(ENGINE_IDENTIFIER),
                ScriptContext.ENGINE_SCOPE);
    }

    @Override
    @AfterEach
    public void afterEach() throws Exception {
        if (scriptEngine instanceof AutoCloseable closeable) {
            closeable.close();
        }
        scriptEngine = null;

        super.afterEach();
    }

    private void setFileContext() {
        ScriptContext context = scriptEngine.getContext();
        context.setAttribute("javax.script.filename", SCRIPT_FILENAME, ScriptContext.ENGINE_SCOPE);
    }

    private void setScriptActionContext() {
        ScriptContext context = scriptEngine.getContext();
        context.setAttribute(CONTEXT_KEY_MODULE_TYPE_ID, ScriptActionHandler.TYPE_ID, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("ruleUID", RULE_UID, ScriptContext.ENGINE_SCOPE);
    }

    @Test
    public void evaluatesBasicExpressions() throws ScriptException {
        setFileContext();

        // Boolean
        assertTrue((Boolean) scriptEngine.eval("(false && true) || true"));
        assertFalse((Boolean) scriptEngine.eval("(true || false) && false"));
        // Arithmetic
        assertEquals(10, ((Number) scriptEngine.eval("5 + 5")).intValue());
        assertEquals(2.5, ((Number) scriptEngine.eval("5 / 2")).doubleValue(), 0.001);
        // Comparisons
        assertTrue((Boolean) scriptEngine.eval("'apple' !== 'orange'"));
        assertTrue((Boolean) scriptEngine.eval("10 >= 10"));
    }

    @Test
    public void loadsOpenhabJsLibrary() {
        setFileContext();

        String script = """
                const { utils } = require('openhab');
                if (!utils.OPENHAB_JS_VERSION) {
                  throw new Error('OPENHAB_JS_VERSION is undefined/null');
                }
                """;
        assertDoesNotThrow(() -> scriptEngine.eval(script));
    }

    @Test
    public void throwsOnScriptLoadingInvalidLibrary() {
        setFileContext();

        String script = "const { foo } = require('bar');";
        assertThrows(ScriptException.class, () -> scriptEngine.eval(script));
    }

    @Test
    public void throwsScriptExceptionOnJsError() {
        setFileContext();

        String script = "throw new Error('JS Error');";
        assertThrows(ScriptException.class, () -> scriptEngine.eval(script));
    }

    @Test
    public void wrapperAllowsForLetConstInScriptActions() {
        setScriptActionContext();

        String script = """
                        const foo = 'Hello';
                        const bar = 'World';
                        const result = `${foo} ${bar}`;
                """;

        // if let/const are allowed in script actions, we can execute scripts containing let/const more than once
        assertDoesNotThrow(() -> scriptEngine.eval(script));
        assertDoesNotThrow(() -> scriptEngine.eval(script));
    }

    @Test
    public void wrapperAllowsForReturnInScriptActions() {
        setScriptActionContext();

        String script = "return true;";
        assertDoesNotThrow(() -> scriptEngine.eval(script));
    }
}
