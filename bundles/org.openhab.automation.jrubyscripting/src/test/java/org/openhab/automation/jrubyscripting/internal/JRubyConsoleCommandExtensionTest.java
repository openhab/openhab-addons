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
    @CsvSource({ "true, false", "true, true", "false, false", "false, true" })
    void startsConsole(boolean withoutBundle, boolean explicitScript) throws Exception {
        ScriptEngineManager manager = mock(ScriptEngineManager.class);
        JRubyScriptEngineFactory factory = mock(JRubyScriptEngineFactory.class);
        JRubyScriptEngineConfiguration configuration = mock(JRubyScriptEngineConfiguration.class);
        when(factory.getScriptTypes()).thenReturn(List.of("rb"));
        when(factory.getConfiguration()).thenReturn(configuration);
        when(configuration.getConsole()).thenReturn("irb");
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
        if (explicitScript) {
            args.add("pry");
        }
        args.add("--verbose");
        command.execute(args.toArray(String[]::new), mock(Console.class));
        verify(factory, times(withoutBundle ? 1 : 0)).withoutBundle(any());
        verify(engine).put(eq(ScriptEngine.ARGV), aryEq(new String[] { "--verbose" }));
        verify(engine).eval("require 'openhab/console/" + (explicitScript ? "pry" : "irb") + "'");
        verify(manager).removeEngine(anyString());
    }
}
