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
package org.openhab.automation.jrubyscripting.internal;

import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.function.Supplier;

import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openhab.automation.jrubyscripting.internal.watch.JRubyScriptFileWatcher;
import org.openhab.core.automation.module.script.ScriptEngineContainer;
import org.openhab.core.automation.module.script.ScriptEngineManager;
import org.openhab.core.config.core.ConfigDescriptionRegistry;
import org.openhab.core.io.console.Console;

/**
 * Tests console arguments and dependency management selection.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
class JRubyConsoleCommandExtensionTest {
    @ParameterizedTest
    @CsvSource({ "true, default", "true, script", "false, default", "false, script", "true, code", "false, code",
            "true, empty-code", "false, empty-code", "true, code-no-default", "false, code-no-default" })
    void startsConsole(boolean withoutBundle, String mode) throws Exception {
        ScriptEngineManager manager = mock(ScriptEngineManager.class);
        JRubyScriptEngineFactory factory = mock(JRubyScriptEngineFactory.class);
        JRubyScriptEngineConfiguration configuration = mock(JRubyScriptEngineConfiguration.class);
        when(factory.getScriptTypes()).thenReturn(List.of("rb"));
        when(factory.getConfiguration()).thenReturn(configuration);
        when(configuration.getConsole()).thenReturn("code-no-default".equals(mode) ? "" : "irb");
        when(factory.withoutBundle(any())).thenAnswer(call -> ((Supplier<?>) call.getArgument(0)).get());
        ScriptEngine engine = mock(ScriptEngine.class);
        when(engine.getContext()).thenReturn(new SimpleScriptContext());
        ScriptEngineContainer container = new ScriptEngineContainer(engine, factory, "test");
        when(manager.createScriptEngine(eq("rb"), anyString())).thenReturn(container);
        JRubyConsoleCommandExtension command = new JRubyConsoleCommandExtension(manager, factory,
                mock(JRubyScriptFileWatcher.class), mock(ConfigDescriptionRegistry.class));
        var args = new java.util.ArrayList<String>();
        args.add("console");
        if (withoutBundle) {
            args.add("--no-bundle");
        }
        boolean inlineCode = mode.contains("code");
        if (inlineCode) {
            args.add("--");
            if (!"empty-code".equals(mode)) {
                args.add("puts");
                args.add("'hello';");
                args.add("puts '--help --no-bundle'");
            }
        } else {
            if ("script".equals(mode)) {
                args.add("pry");
            }
            args.add("--verbose");
        }
        command.execute(args.toArray(String[]::new), mock(Console.class));
        verify(factory, times(withoutBundle ? 1 : 0)).withoutBundle(any());
        verify(engine).put(eq(ScriptEngine.ARGV), aryEq(inlineCode ? new String[0] : new String[] { "--verbose" }));
        String expectedCode = inlineCode ? ("empty-code".equals(mode) ? "" : "puts 'hello'; puts '--help --no-bundle'")
                : "require 'openhab/console/" + ("script".equals(mode) ? "pry" : "irb") + "'";
        verify(engine).eval(expectedCode);
        verify(manager).removeEngine(anyString());
    }
}
