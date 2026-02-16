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

import static org.openhab.core.automation.module.script.ScriptTransformationService.OPENHAB_TRANSFORMATION_SCRIPT;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.java223.common.BindingInjector;
import org.openhab.automation.java223.common.Java223Constants;
import org.openhab.automation.java223.common.Java223Exception;
import org.openhab.automation.java223.common.ReuseScriptInstance;
import org.openhab.automation.java223.internal.strategy.Java223Strategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.obermuhlner.scriptengine.java.JavaCompiledScript;
import ch.obermuhlner.scriptengine.java.JavaScriptEngine;

/**
 * Custom java compiled script instance wrapping additional information
 * and adding functionality.
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public class Java223CompiledScript extends JavaCompiledScript {

    private final Logger logger = LoggerFactory.getLogger(Java223CompiledScript.class);

    // overwrite compiledInstance from super class
    /**
     * Write access mandatory for setting instance after creation.
     */
    @Nullable
    private Object java223CompiledInstance;

    private final Java223Strategy java223Strategy;

    /**
     * Construct a {@link JavaCompiledScript}.
     *
     * @param engine the {@link JavaScriptEngine} that compiled this script
     * @param compiledClass the compiled {@link Class}
     * @param java223Strategy the {@link Java223Strategy}
     */
    public Java223CompiledScript(JavaScriptEngine engine, Class<?> compiledClass, Java223Strategy java223Strategy) {
        super(engine, compiledClass, null, java223Strategy, java223Strategy);
        this.java223Strategy = java223Strategy;
    }

    @Override
    public @Nullable Object eval(@Nullable ScriptContext context) throws ScriptException {

        // prepare bindings data
        if (context == null) {
            throw new IllegalArgumentException("ScriptContext must not be null");
        }

        Bindings globalBindings = context.getBindings(ScriptContext.GLOBAL_SCOPE);
        Bindings engineBindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
        Map<String, Object> mergedBindings = new HashMap<>();
        if (globalBindings != null) {
            mergedBindings.putAll(globalBindings);
        }
        if (engineBindings != null) {
            mergedBindings.putAll(engineBindings);
        }
        // add an origin identifier in the bindings for the user scripts to use
        String identifier = getIdentifier(context);
        mergedBindings.put(Java223Constants.JAVA_223_IDENTIFIER, identifier);
        java223Strategy.associateBindings(null, null, mergedBindings);

        try {
            // default re-instantiation option overwritten by annotation if present
            boolean instanceReuse = java223Strategy.getInstanceReuseDefaultProperty();
            ReuseScriptInstance reuseAnnotation = getCompiledClass().getAnnotation(ReuseScriptInstance.class);
            if (reuseAnnotation != null) {
                instanceReuse = reuseAnnotation.value();
            }
            // if allowed, get the cached instance
            var localScriptInstance = java223CompiledInstance;
            if (!instanceReuse || localScriptInstance == null) {
                // no cache, instantiate the script
                localScriptInstance = construct(getCompiledClass(), mergedBindings);
                java223CompiledInstance = localScriptInstance;
            }

            // execute
            return java223Strategy.execute(localScriptInstance, mergedBindings);
        } catch (Java223Exception e) {
            // keep responsibility of logging full stack trace, as ScriptException cannot contain cause
            // and caller sometimes does not do it well
            logger.error("Exception during evaluation of a java223 script: {}", e.getMessage(), e);
            // and sending only the message upstream
            throw new ScriptException(e.getMessage());
        }
    }

    // get the identifier for the script, marking origin
    private static String getIdentifier(ScriptContext context) {
        Object fileName = context.getAttribute("javax.script.filename");
        Object ruleUID = context.getAttribute("ruleUID");
        Object ohEngineIdentifier = context.getAttribute("oh.engine-identifier");
        String identifier = "stack";
        if (fileName != null) {
            identifier = fileName.toString().replaceAll("^.*[/\\\\]", "");
        } else if (ruleUID != null) {
            identifier = ruleUID.toString();
        } else if (ohEngineIdentifier != null) {
            if (ohEngineIdentifier.toString().startsWith(OPENHAB_TRANSFORMATION_SCRIPT)) {
                identifier = ohEngineIdentifier.toString().replaceAll(OPENHAB_TRANSFORMATION_SCRIPT, "transformation.");
            }
        }
        return identifier;
    }

    private Object construct(Class<?> compiledClass, Map<String, Object> bindings) {

        // create real instance from compiled class
        // use the empty constructor if available, or the first one otherwise
        Constructor<?>[] constructors = compiledClass.getDeclaredConstructors();
        Constructor<?> constructor = Arrays.stream(constructors).filter(c -> c.getParameterCount() == 0).findFirst()
                .orElseGet(() -> constructors[0]);

        try {
            ClassLoader classLoader = compiledClass.getClassLoader();
            if (classLoader == null) { // should not happen
                throw new Java223Exception(
                        "Cannot get the classloader of " + compiledClass.getName() + ". Should not happen");
            }
            @Nullable
            Object[] parameterValues = BindingInjector.getParameterValuesFor(classLoader, constructor, bindings, null);
            Object compiledInstance = constructor.newInstance(parameterValues);
            if (compiledInstance == null) { // can't be null, but null-check thinks so
                throw new Java223Exception("Instantiation of compiledInstance failed. Should not happened");
            }
            return compiledInstance;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new Java223Exception("Cannot instantiate the script", e);
        }
    }

    @Override
    public @Nullable Object getCompiledInstance() {
        return java223CompiledInstance;
    }
}
