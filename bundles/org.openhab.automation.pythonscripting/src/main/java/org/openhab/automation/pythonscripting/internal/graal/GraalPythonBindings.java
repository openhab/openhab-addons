/**
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
package org.openhab.automation.pythonscripting.internal.graal;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.TypeLiteral;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

/***
 * @author Jeff James - Initial contribution
 */
final class GraalPythonBindings extends AbstractMap<String, Object> implements javax.script.Bindings, AutoCloseable {
    private static final String SCRIPT_CONTEXT_GLOBAL_BINDINGS_IMPORT_FUNCTION_NAME = "importScriptEngineGlobalBindings";

    private static final TypeLiteral<Map<String, Object>> STRING_MAP = new TypeLiteral<Map<String, Object>>() {
    };

    private Context context;
    private Map<String, Object> global;
    private Value deleteProperty;
    private Value clear;
    private Context.Builder contextBuilder;
    // ScriptContext of the ScriptEngine where these bindings form ENGINE_SCOPE bindings
    private ScriptContext engineScriptContext;
    private ScriptEngine engineBinding;

    GraalPythonBindings(Context.Builder contextBuilder, ScriptContext scriptContext, ScriptEngine engine) {
        this.contextBuilder = contextBuilder;
        this.engineScriptContext = scriptContext;
        this.engineBinding = engine;
    }

    GraalPythonBindings(Context context, ScriptContext scriptContext, ScriptEngine engine) {
        this.context = context;
        this.engineScriptContext = scriptContext;
        this.engineBinding = engine;
        initGlobal();
    }

    private void requireContext() {
        if (context == null) {
            initContext();
        }
    }

    private void initContext() {
        context = GraalPythonScriptEngine.createDefaultContext(contextBuilder, engineScriptContext);
        initGlobal();
    }

    private void initGlobal() {
        this.global = GraalPythonScriptEngine.evalInternal(context, "globals()").as(STRING_MAP);
        updateEngineBinding();
        updateContextBinding();
    }

    private void updateEngineBinding() {
        updateBinding("engine", engineBinding);
    }

    private void updateContextBinding() {
        if (engineScriptContext != null) {
            updateBinding("context", engineScriptContext);
        }
    }

    private void updateBinding(String key, Object value) {
        requireContext();
        context.getBindings("python").putMember(key, value);
        // GraalPythonScriptEngine.evalInternal(context,
            // "lambda key, value: 
            // def set_global(key, value):\n" +
            // " globals()[key] = value\n" + 
            // "set_global(key, value)").execute(key, value);
    }

    private Value deletePropertyFunction() {
        if (this.deleteProperty == null) {
            this.deleteProperty = GraalPythonScriptEngine.evalInternal(context, //
                    "def delete_property(obj, prop):\n" //
                            + "    if prop in obj:\n" //
                            + "        del obj[prop]");
        }
        return this.deleteProperty;
    }

    private Value clearFunction() {
        if (this.clear == null) {
            this.clear = GraalPythonScriptEngine.evalInternal(context, //
                    "def delete_properties(obj):\n" //
                            + "    for prop in list(obj.keys()):" //
                            + "        del obj[prop]");
        }
        return this.clear;
    }

    @Override
    public Object put(String name, Object v) {
        checkKey(name);
        requireContext();

        // JJ: modified to directly put in context, not sure how the GraalJSBindings ever could have injected items without this.
        context.getBindings("python").putMember(name, v);
        return global.put(name, v);
    }

    @Override
    public void clear() {
        if (context != null) {
            clearFunction().execute(global);
        }
    }

    @Override
    public Object get(Object key) {
        checkKey((String) key);
        requireContext();
        if (engineScriptContext != null) {
            importGlobalBindings(engineScriptContext);
        }
        return global.get(key);
    }

    private static void checkKey(String key) {
        Objects.requireNonNull(key, "key can not be null");
        if (key.isEmpty()) {
            throw new IllegalArgumentException("key can not be empty");
        }
    }

    @Override
    public Object remove(Object key) {
        requireContext();
        Object prev = get(key);
        deletePropertyFunction().execute(global, key);
        return prev;
    }

    public Context getContext() {
        requireContext();
        return context;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        requireContext();
        return global.entrySet();
    }

    @Override
    public void close() {
        if (context != null) {
            context.close();
        }
    }

    private static IllegalStateException magicOptionContextInitializedError(String name) {
        return new IllegalStateException(
                String.format("failed to set graal-py option \"%s\": py context is already initialized", name));
    }

    void importGlobalBindings(ScriptContext scriptContext) {
        Bindings globalBindings = scriptContext.getBindings(ScriptContext.GLOBAL_SCOPE);
        if (globalBindings != null && !globalBindings.isEmpty() && !this.equals(globalBindings)) {
            ProxyObject bindingsProxy = ProxyObject.fromMap(Collections.unmodifiableMap(globalBindings));
            getContext().getBindings("python").getMember(SCRIPT_CONTEXT_GLOBAL_BINDINGS_IMPORT_FUNCTION_NAME)
                    .execute(bindingsProxy);
        }
    }

    void updateEngineScriptContext(ScriptContext scriptContext) {
        engineScriptContext = scriptContext;
    }
}
