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
package org.openhab.automation.java223.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.List;
import java.util.Objects;

import javax.script.ScriptException;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.automation.java223.common.ServiceGetter;
import org.openhab.automation.java223.internal.strategy.Java223Strategy;

import ch.obermuhlner.scriptengine.java.compilation.NoInterceptorStrategy;
import ch.obermuhlner.scriptengine.java.compilation.ScriptInterceptorStrategy;
import standalone.com.sun.tools.javac.api.JavacTool;

/**
 * Tests for the runtime compilation of Java scripts through the {@link Java223ScriptEngine}.
 *
 * The compilation path is exercised for both the JVM's default system compiler (when available)
 * and the standalone fallback compiler shipped with the bundle (used when the JRE has no
 * {@code tools.jar} / {@code com.sun.tools.javac.api.JavacTool} reachable).
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
class Java223ScriptEngineTest {

    /**
     * A trivial script with a {@code public class} declaration: this bypasses the
     * {@link org.openhab.automation.java223.internal.strategy.ScriptWrappingStrategy} wrapper
     * (see {@link NoInterceptorStrategy}) and lets us validate the raw compilation pipeline.
     */
    private static final String SIMPLE_SCRIPT = """
            public class HelloWorld {
                public static int add(int a, int b) {
                    return a + b;
                }
            }
            """;

    /**
     * A script that intentionally fails to compile. The error message must surface
     * through a {@link ScriptException}.
     */
    private static final String BROKEN_SCRIPT = """
            public class BrokenScript {
                public static int boo() {
                    return doesNotExist + 1;
                }
            }
            """;

    private static final List<String> COMPILATION_OPTIONS = List.of("-g", "-parameters");

    /**
     * Build a {@link Java223ScriptEngine} wired with the given compiler and the bare
     * {@link NoInterceptorStrategy} so the script is compiled as-is.
     */
    private Java223ScriptEngine buildEngine(JavaCompiler compiler) {
        ScriptInterceptorStrategy interceptor = new NoInterceptorStrategy();
        ServiceGetter serviceGetter = new ServiceGetter() {
            @Override
            public <T> @org.eclipse.jdt.annotation.Nullable T getService(Class<T> tClass) {
                return null;
            }
        };
        ClassLoader classLoader = getClass().getClassLoader();
        if (classLoader == null) {
            throw new IllegalStateException("Test class loader is null. Should not happen.");
        }
        Java223Strategy strategy = new Java223Strategy(classLoader, serviceGetter);
        // The package resource listing strategy is normally provided by the OSGi bundle wiring.
        // For a unit test we don't need to walk any extra packages, so a no-op lambda is enough.
        return new Java223ScriptEngine(strategy, packageName -> List.of(), interceptor, COMPILATION_OPTIONS, compiler);
    }

    @Test
    void compileSimpleScriptWithSystemCompiler() throws ScriptException {
        JavaCompiler systemCompiler = ToolProvider.getSystemJavaCompiler();
        // The JDK running the tests may or may not expose a system compiler (e.g. a JRE).
        // Skip rather than fail in that case; the fallback test below covers it.
        assumeTrue(systemCompiler != null, "No system Java compiler available on this JVM");

        Java223ScriptEngine engine = buildEngine(Objects.requireNonNull(systemCompiler));

        Java223CompiledScript compiled = engine.compile(SIMPLE_SCRIPT);

        assertNotNull(compiled);
        assertEquals("HelloWorld", compiled.getCompiledClass().getSimpleName());
    }

    @Test
    void compileSimpleScriptWithFallbackCompiler() throws ScriptException {
        // The fallback compiler is shipped in the bundle as `standalone-jdk21` and is expected
        // to be available regardless of the host JVM configuration.
        JavaCompiler fallbackCompiler = JavacTool.create();

        Java223ScriptEngine engine = buildEngine(fallbackCompiler);

        Java223CompiledScript compiled = engine.compile(SIMPLE_SCRIPT);

        assertNotNull(compiled);
        assertEquals("HelloWorld", compiled.getCompiledClass().getSimpleName());
    }

    @Test
    void compileBrokenScriptThrowsWithSystemCompiler() {
        JavaCompiler systemCompiler = ToolProvider.getSystemJavaCompiler();
        assumeTrue(systemCompiler != null, "No system Java compiler available on this JVM");

        Java223ScriptEngine engine = buildEngine(Objects.requireNonNull(systemCompiler));

        // The exact error wording is compiler-dependent, so we only assert that
        // a ScriptException is raised with the diagnostics embedded in the message.
        ScriptException ex = assertThrows(ScriptException.class, () -> engine.compile(BROKEN_SCRIPT));
        assertNotNull(ex.getMessage());
    }

    @Test
    void compileBrokenScriptThrowsWithFallbackCompiler() {
        JavaCompiler fallbackCompiler = JavacTool.create();

        Java223ScriptEngine engine = buildEngine(fallbackCompiler);

        ScriptException ex = assertThrows(ScriptException.class, () -> engine.compile(BROKEN_SCRIPT));
        assertNotNull(ex.getMessage());
    }
}
