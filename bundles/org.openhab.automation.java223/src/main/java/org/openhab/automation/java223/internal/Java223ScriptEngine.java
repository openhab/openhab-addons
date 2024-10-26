/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import static org.openhab.core.automation.module.script.ScriptEngineFactory.CONTEXT_KEY_DEPENDENCY_LISTENER;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.script.Invocable;
import javax.script.ScriptException;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.java223.internal.strategy.Java223Strategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.obermuhlner.scriptengine.java.JavaScriptEngine;
import ch.obermuhlner.scriptengine.java.MemoryFileManager;
import ch.obermuhlner.scriptengine.java.compilation.ScriptInterceptorStrategy;
import ch.obermuhlner.scriptengine.java.name.DefaultNameStrategy;
import ch.obermuhlner.scriptengine.java.name.NameStrategy;
import ch.obermuhlner.scriptengine.java.packagelisting.PackageResourceListingStrategy;

/**
 * This class adds the Invocable aspect to the JavaScriptEngine. The Invocable aspect adds the ability to be called
 * The compile method is also rewritten for our specificity.
 * When loaded and unloaded, script events are triggered.
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public class Java223ScriptEngine extends JavaScriptEngine implements Invocable {
    private final Logger logger = LoggerFactory.getLogger(Java223ScriptEngine.class);

    private @Nullable Java223CompiledScript lastCompiledScript;

    private final Java223Strategy java223Strategy;
    private final PackageResourceListingStrategy osgiPackageResourceListingStrategy;
    private final ScriptInterceptorStrategy scriptInterceptorStrategy;
    private final List<String> compilationOptions;
    private final NameStrategy nameStrategy = new DefaultNameStrategy();

    public Java223ScriptEngine(Java223Strategy java223Strategy,
            PackageResourceListingStrategy osgiPackageResourceListingStrategy,
            ScriptInterceptorStrategy scriptInterceptorStrategy, List<String> compilationOptions) {
        this.java223Strategy = java223Strategy;
        this.osgiPackageResourceListingStrategy = osgiPackageResourceListingStrategy;
        this.scriptInterceptorStrategy = scriptInterceptorStrategy;
        this.compilationOptions = compilationOptions;
    }

    @Override
    public Java223CompiledScript compile(@Nullable String originalScript) throws ScriptException {
        try {

            if (originalScript == null) {
                throw new ScriptException("script cannot be null");
            }
            // add a wrapper if needed
            String script = scriptInterceptorStrategy.intercept(originalScript);

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            JavaFileManager fileManager = java223Strategy.getJavaFileManager(
                    ToolProvider.getSystemJavaCompiler().getStandardFileManager(diagnostics, null, null));
            ClassLoader parentClassLoader = fileManager.getClassLoader(StandardLocation.CLASS_PATH);
            MemoryFileManager memoryFileManager = new MemoryFileManager(fileManager, parentClassLoader);
            memoryFileManager.setPackageResourceListingStrategy(osgiPackageResourceListingStrategy);

            String fullClassName = nameStrategy.getFullName(script);
            String simpleClassName = NameStrategy.extractSimpleName(fullClassName);

            // get all things to compile
            List<JavaFileObject> toCompile = java223Strategy.getJavaFileObjectsToCompile(simpleClassName, script);

            // compile
            try {
                JavaCompiler.CompilationTask task = compiler.getTask(null, memoryFileManager, diagnostics,
                        compilationOptions, null, toCompile);
                if (!task.call()) {
                    String message = diagnostics.getDiagnostics().stream().map(Object::toString)
                            .collect(Collectors.joining("\n"));
                    throw new ScriptException(message);
                }
            } catch (RuntimeException e) {
                // Catching RuntimeException is no good practice, but the Javadoc of getTask and compile explicitly says
                // that the ONLY source of runtime error is the user-supplied code. So catching it is OK and doesn't
                // mean that we are meddling with something we shouldn't.
                // We then keep responsibility of logging full stack trace, as ScriptException cannot contain cause:
                logger.error("Error compiling script: {}", e.getMessage(), e);
                throw new ScriptException(e.getMessage());
            }

            // declare lib dependencies (all files are supposed libraries)
            @SuppressWarnings("unchecked")
            Consumer<String> scriptDependencyListener = (Consumer<String>) getContext()
                    .getAttribute(CONTEXT_KEY_DEPENDENCY_LISTENER);
            for (Path libPath : java223Strategy.getAllLibraries()) {
                scriptDependencyListener.accept(libPath.toString());
            }

            // load in class loader
            ClassLoader classLoader = memoryFileManager.getClassLoader(StandardLocation.CLASS_OUTPUT);
            try {
                Class<?> result = classLoader.loadClass(fullClassName);
                var compiledScriptResult = new Java223CompiledScript(this, result, java223Strategy);
                lastCompiledScript = compiledScriptResult;
                return compiledScriptResult;
            } catch (ClassNotFoundException e) {
                throw new ScriptException(e);
            }
        } catch (NoClassDefFoundError e) {
            throw new ScriptException("NoClassDefFoundError: " + e.getMessage());
        }
    }

    @Override
    public @Nullable Object invokeMethod(@Nullable Object o, @Nullable String name, Object @Nullable... args)
            throws NoSuchMethodException {
        throw new NoSuchMethodException("not implemented");
    }

    @Override
    public @Nullable Object invokeFunction(@Nullable String name, Object @Nullable... args)
            throws ScriptException, NoSuchMethodException {
        // here we assume (from OpenHAB usual behavior) that the script engine served only once and so the wanted
        // compiled script is the last (and only) one
        Java223CompiledScript compiledScript = this.lastCompiledScript;
        if (compiledScript == null || name == null) {
            return null;
        }
        var localArgs = args == null ? new Object[0] : args;
        Class<?>[] argClasses = Arrays.stream(localArgs).map(Object::getClass).toArray(Class[]::new);

        Method method = compiledScript.getCompiledClass().getMethod(name, argClasses);
        try {
            if (Modifier.isStatic(method.getModifiers())) {
                return method.invoke(new Object(), localArgs); // new object() required (but value ignored) to avoid
                                                               // both
                // non-null check and compiler error
            }

            Object compiledInstance = compiledScript.getCompiledInstance();
            if (compiledInstance != null) {
                return method.invoke(compiledInstance, localArgs);
            }
            logger.debug("Calling {} method from a script not yet instantiated is ignored. Use a static modifier",
                    name);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ScriptException(e);
        }

        return null;
    }

    @Override
    public <T> T getInterface(@Nullable Class<T> clazz) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public <T> T getInterface(@Nullable Object o, @Nullable Class<T> clazz) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
