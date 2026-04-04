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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openhab.core.automation.module.script.ScriptEngineFactory.CONTEXT_KEY_DEPENDENCY_LISTENER;
import static org.openhab.core.automation.module.script.ScriptEngineFactory.CONTEXT_KEY_ENGINE_IDENTIFIER;
import static org.openhab.core.automation.module.script.ScriptEngineFactory.CONTEXT_KEY_EXTENSION_ACCESSOR;

import java.io.Closeable;
import java.util.Map;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.core.automation.module.script.ScriptExtensionAccessor;

/**
 * @author Florian Hotze - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class OpenhabGraalJSScriptEngineTest extends GraalJSOSGiTest {
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

        scriptEngine = scriptEngineFactory.createScriptEngine(SCRIPT_TYPE);
        ScriptContext context = scriptEngine.getContext();
        context.setAttribute(CONTEXT_KEY_ENGINE_IDENTIFIER, ENGINE_IDENTIFIER, ScriptContext.ENGINE_SCOPE);
        context.setAttribute(CONTEXT_KEY_EXTENSION_ACCESSOR, scriptExtensionAccessor, ScriptContext.ENGINE_SCOPE);
        context.setAttribute(CONTEXT_KEY_DEPENDENCY_LISTENER, jsDependencyTracker.getTracker(ENGINE_IDENTIFIER),
                ScriptContext.ENGINE_SCOPE);
    }

    @Override
    @AfterEach
    public void afterEach() throws Exception {
        if (scriptEngine instanceof Closeable closeable) {
            closeable.close();
        }
        scriptEngine = null;

        super.afterEach();
    }

    @Test
    public void evaluatesBasicExpressions() throws ScriptException {
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
}
