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
package org.openhab.automation.java223.internal.strategy;

import static org.openhab.automation.java223.common.Java223Constants.BINDINGS;
import static org.openhab.automation.java223.common.Java223Constants.LIB_DIR;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptException;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.java223.common.BindingInjector;
import org.openhab.automation.java223.common.Java223Constants;
import org.openhab.automation.java223.common.RunScript;
import org.openhab.automation.java223.common.ServiceGetter;
import org.openhab.automation.java223.internal.codegeneration.DependencyGenerator;
import org.openhab.automation.java223.internal.strategy.jarloader.JarFileManager.JarFileManagerFactory;
import org.openhab.core.service.WatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.obermuhlner.scriptengine.java.MemoryFileManager;
import ch.obermuhlner.scriptengine.java.bindings.BindingStrategy;
import ch.obermuhlner.scriptengine.java.compilation.CompilationStrategy;
import ch.obermuhlner.scriptengine.java.execution.ExecutionStrategy;
import ch.obermuhlner.scriptengine.java.execution.ExecutionStrategyFactory;
import ch.obermuhlner.scriptengine.java.name.DefaultNameStrategy;
import ch.obermuhlner.scriptengine.java.name.NameStrategy;

/**
 * A one-for-all strategy for a goal: providing binding / execution / library compilation and management to java223
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public class Java223Strategy
        implements ExecutionStrategyFactory, ExecutionStrategy, BindingStrategy, CompilationStrategy {

    private static final Logger logger = LoggerFactory.getLogger(Java223Strategy.class);

    private static final List<String> METHOD_NAMES_TO_EXECUTE = Arrays.asList("eval", "main", "run", "exec");

    // Keeping a list of .java files libraries in the lib directory
    private static final Map<String, JavaFileObject> librariesByPath = Collections.synchronizedMap(new HashMap<>());

    NameStrategy nameStrategy = new DefaultNameStrategy();
    JarFileManagerFactory jarFileManagerfactory;

    // Allow instance reuse by default
    private boolean allowInstanceReuseDefaultProperty;

    private final ServiceGetter serviceGetter;

    public Java223Strategy(ClassLoader classLoader, ServiceGetter serviceGetter) {
        this.allowInstanceReuseDefaultProperty = false;
        this.serviceGetter = serviceGetter;
        jarFileManagerfactory = new JarFileManagerFactory(LIB_DIR, classLoader);
    }

    public boolean getInstanceReuseDefaultProperty() {
        return allowInstanceReuseDefaultProperty;
    }

    @Override
    public ExecutionStrategy create(@Nullable Class<?> clazz) throws ScriptException {
        return this;
    }

    /**
     * Add data in bindings. Do not use the compiledClass or compiledInstance.
     * 
     * @param compiledClass Not used
     * @param compiledInstance Not used
     * @param bindings Map to add special data inside
     */
    @Override
    public void associateBindings(@Nullable Class<?> compiledClass, @Nullable Object compiledInstance,
            Map<String, Object> bindings) {
        // adding a special self-reference to bindings: "bindings", to receive a map with all bindings
        // noinspection CollectionAddedToSelf
        bindings.put(BINDINGS, bindings);
        // adding a special service getter for the script to access OSGi services
        bindings.put(Java223Constants.SERVICE_GETTER, serviceGetter);
    }

    @Override
    public @Nullable Object execute(@Nullable Object instance) throws ScriptException {
        throw new UnsupportedOperationException(
                "Wrong way to use this strategy. Use execute(script, bindings instead)");
    }

    /**
     * Execute the instance with binding context
     * 
     * @param instance an instantiated script
     * @param bindings bindings data to inject
     * @return Execution result
     * @throws ScriptException When the script cannot execute
     */
    public @Nullable Object execute(Object instance, Map<String, Object> bindings) throws ScriptException {

        Class<?> compiledClass = instance.getClass();

        // inject binding's data in the script
        ClassLoader classLoader = compiledClass.getClassLoader();
        if (classLoader == null) { // should not happen
            throw new ScriptException("Cannot get the classloader of " + compiledClass.getName());
        }
        BindingInjector.injectBindingsInto(classLoader, bindings, instance);

        // find methods to execute
        // noinspection OptionalAssignedToNull
        Optional<Object> returned = null;
        for (Method method : instance.getClass().getMethods()) {
            // methods with a special name, or methods with a special annotation
            if (METHOD_NAMES_TO_EXECUTE.contains(method.getName()) || method.getAnnotation(RunScript.class) != null) {
                try {
                    @Nullable
                    Object[] parameterValues = BindingInjector.getParameterValuesFor(classLoader, method, bindings,
                            null);
                    var returnedLocal = method.invoke(instance, parameterValues);
                    // keep arbitrarily only the first returned value
                    // comparing this optional to null is OK. Null value means no method was yet executed.
                    // noinspection OptionalAssignedToNull
                    if (returned == null || returned.isEmpty()) {
                        if (returnedLocal != null) {
                            returned = Optional.of(returnedLocal);
                        } else {
                            returned = Optional.empty();
                        }
                    }
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                        | InstantiationException e) {
                    String simpleName = instance.getClass().getSimpleName();
                    // keep responsibility of logging full stack trace, as ScriptException cannot contain cause
                    // and the caller sometimes does not log it fully.
                    logger.error("Error executing entry point {} in {}, exception {}", method.getName(), simpleName,
                            e.getMessage(), e);
                    throw new ScriptException("Cannot execute script. Entry point error");
                }
            }
        }

        // return if there was at least one execution
        // comparing this optional to null is OK. Null value means no method was executed.
        // noinspection OptionalAssignedToNull
        if (returned != null) {
            return returned.orElse(null);
        }
        if (bindings.containsKey("javax.script.filename")) {
            logger.trace("No runnable method found in {}", bindings.get("javax.script.filename"));
            return null;
        }

        throw new ScriptException(String.format(
                "cannot execute: %s doesn't have a method named eval/main/run, or a RunScript annotated method",
                compiledClass.getSimpleName()));
    }

    @Override
    public Map<String, Object> retrieveBindings(Class<?> compiledClass, Object compiledInstance) {
        // not needed? What is the use case?
        return new HashMap<>();
    }

    @Override
    public List<JavaFileObject> getJavaFileObjectsToCompile(@Nullable String simpleClassName,
            @Nullable String currentSource) {
        // the script
        JavaFileObject currentJavaFileObject = MemoryFileManager.createSourceFileObject(null, simpleClassName,
                currentSource);
        // and we add all the .java libraries
        List<JavaFileObject> sumFileObjects = new ArrayList<>(librariesByPath.values());
        sumFileObjects.add(currentJavaFileObject);
        return sumFileObjects;
    }

    public void processWatchEvent(WatchService.Kind kind, Path pathEvent) {
        Path fullPath = LIB_DIR.resolve(pathEvent);

        // All new .java files will be kept in memory
        if (fullPath.getFileName().toString().endsWith("." + Java223Constants.JAVA_FILE_TYPE)) {
            switch (kind) {
                case CREATE:
                case MODIFY:
                    addLibrary(fullPath);
                    break;
                case DELETE:
                    removeLibrary(fullPath);
                    break;
                default:
                    logger.warn("watch event not implemented {}", kind);
            }
        } else if (fullPath.getFileName().toString().endsWith("." + Java223Constants.JAR_FILE_TYPE)) {
            // jar will be scanned to be added to the JarFileManagerFactory
            // exclude convenience jar from processing
            if (fullPath.getFileName().toString().equals(DependencyGenerator.CONVENIENCE_DEPENDENCIES_JAR)) {
                return;
            }
            switch (kind) {
                case CREATE:
                    jarFileManagerfactory.addLibPackage(fullPath);
                    break;
                case MODIFY:
                case DELETE:
                    // we cannot remove something from a ClassLoader, so we have to rebuild it
                    logger.debug("From watch event {} {}", kind, pathEvent);
                    jarFileManagerfactory.rebuildLibPackages();
                    break;
                case OVERFLOW:
                    break;
            }
        } else {
            logger.trace(
                    "Received '{}' for path '{}' - ignoring (wrong extension, only .java and .jar file are supported)",
                    kind, fullPath);
        }
    }

    private void addLibrary(Path path) {
        try {
            String readString = Files.readString(path);
            String fullName = nameStrategy.getFullName(readString);
            String simpleClassName = NameStrategy.extractSimpleName(fullName);
            JavaFileObject javafileObject = MemoryFileManager.createSourceFileObject(null, simpleClassName, readString);
            librariesByPath.put(path.toString(), javafileObject);
        } catch (ScriptException | IOException e) {
            logger.warn("Cannot get the file {} as a valid java object. Cause: {} {}", path, e.getClass().getName(),
                    e.getMessage());
        }
    }

    private void removeLibrary(Path path) {
        librariesByPath.remove(path.toString());
    }

    public Set<Path> getAllLibraries() {
        // combine lib package (jar) and lib java file :
        HashSet<Path> libsPath = new HashSet<>();
        libsPath.addAll(librariesByPath.keySet().stream().map(Path::of).collect(Collectors.toSet()));
        libsPath.addAll(jarFileManagerfactory.getAllJarPaths());
        return libsPath;
    }

    public void scanLibDirectory() {
        try (Stream<Path> walk = Files.walk(LIB_DIR)) {
            walk.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith("." + Java223Constants.JAVA_FILE_TYPE))
                    .forEach(this::addLibrary);
            jarFileManagerfactory.rebuildLibPackages();
        } catch (IOException e) {
            logger.error("Cannot use libraries", e);
        }
    }

    @Override
    public JavaFileManager getJavaFileManager(@Nullable JavaFileManager parentJavaFileManager) {
        if (parentJavaFileManager == null) {
            throw new IllegalArgumentException("Parent JavaFileManager should not be null");
        }
        return jarFileManagerfactory.create(parentJavaFileManager);
    }

    public void setAllowInstanceReuse(boolean allowInstanceReuse) {
        this.allowInstanceReuseDefaultProperty = allowInstanceReuse;
    }
}
