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
package org.openhab.io.mcp.internal.tools;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.io.mcp.internal.McpTestHelper.*;

import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.automation.module.script.ScriptEngineContainer;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.openhab.core.automation.module.script.ScriptEngineManager;
import org.openhab.io.mcp.internal.McpTestHelper;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * Tests for {@link ScriptTools}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class ScriptToolsTest {

    @Mock
    @Nullable
    ScriptEngineManager scriptEngineManager;

    @Mock
    @Nullable
    ScriptEngine scriptEngine;

    @Mock
    @Nullable
    ScriptEngineFactory scriptEngineFactory;

    private final McpJsonMapper jsonMapper = McpTestHelper.newJsonMapper();

    private ScriptTools enabled() {
        return new ScriptTools(requireNonNull(scriptEngineManager), jsonMapper, true);
    }

    private ScriptTools disabled() {
        return new ScriptTools(requireNonNull(scriptEngineManager), jsonMapper, false);
    }

    private static <T> T requireNonNull(@Nullable T value) {
        assertNotNull(value);
        return value;
    }

    @BeforeEach
    void setUp() throws ScriptException {
        ScriptEngineContainer container = new ScriptEngineContainer(requireNonNull(scriptEngine),
                requireNonNull(scriptEngineFactory), "id");
        lenient().when(scriptEngineManager.createScriptEngine(eq("application/javascript"), anyString()))
                .thenReturn(container);
    }

    @Test
    void executeScriptWhenDisabledReturnsScriptingDisabled() throws Exception {
        CallToolResult result = disabled().handleExecuteScript(createRequest(Map.of("script", "1 + 1")));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(false, parsed.get("success"));
        assertEquals("ScriptingDisabled", parsed.get("errorType"));
    }

    @Test
    void executeScriptMissingScriptReturnsError() {
        CallToolResult result = enabled().handleExecuteScript(createRequest(Map.of()));
        assertErrorContains(result, "script");
    }

    @Test
    void executeScriptSuccessReturnsResult() throws Exception {
        when(scriptEngine.eval("'hello'")).thenReturn("hello");

        CallToolResult result = enabled().handleExecuteScript(createRequest(Map.of("script", "'hello'")));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(true, parsed.get("success"));
        assertEquals("hello", parsed.get("result"));
        assertEquals("java.lang.String", parsed.get("resultType"));
        verify(scriptEngineManager).removeEngine(startsWith("mcp-execute-"));
    }

    @Test
    void executeScriptScriptExceptionReturnsStructuredError() throws Exception {
        when(scriptEngine.eval(anyString())).thenThrow(new ScriptException("ReferenceError: x is not defined", "?", 3));

        CallToolResult result = enabled().handleExecuteScript(createRequest(Map.of("script", "x.y")));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(false, parsed.get("success"));
        assertEquals("ScriptException", parsed.get("errorType"));
        assertEquals(3, parsed.get("lineNumber"));
        assertTrue(((String) parsed.get("message")).contains("ReferenceError"));
        verify(scriptEngineManager).removeEngine(startsWith("mcp-execute-"));
    }

    @Test
    void executeScriptRuntimeExceptionReturnsStructuredError() throws Exception {
        when(scriptEngine.eval(anyString())).thenThrow(new RuntimeException("boom"));

        CallToolResult result = enabled().handleExecuteScript(createRequest(Map.of("script", "doStuff()")));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(false, parsed.get("success"));
        assertEquals("RuntimeException", parsed.get("errorType"));
        assertEquals("boom", parsed.get("message"));
    }

    @Test
    void executeScriptTimeoutReturnsTimeoutError() throws Exception {
        when(scriptEngine.eval(anyString())).thenAnswer(inv -> {
            Thread.sleep(2000);
            return null;
        });

        CallToolResult result = enabled()
                .handleExecuteScript(createRequest(Map.of("script", "while(true){}", "timeoutMs", 100)));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(false, parsed.get("success"));
        assertEquals("TimeoutException", parsed.get("errorType"));
    }

    @Test
    void executeScriptEngineUnavailableReturnsError() throws Exception {
        when(scriptEngineManager.createScriptEngine(eq("application/javascript"), anyString())).thenReturn(null);

        CallToolResult result = enabled().handleExecuteScript(createRequest(Map.of("script", "1 + 1")));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(false, parsed.get("success"));
        assertEquals("EngineUnavailable", parsed.get("errorType"));
    }
}
