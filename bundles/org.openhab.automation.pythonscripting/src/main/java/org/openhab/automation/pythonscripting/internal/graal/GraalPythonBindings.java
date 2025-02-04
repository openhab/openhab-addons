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
package org.openhab.automation.pythonscripting.internal.graal;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.TypeLiteral;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * @author Holger Hees - Initial contribution
 * @author Jeff James - Initial contribution
 */
final class GraalPythonBindings extends AbstractMap<String, Object> implements javax.script.Bindings, AutoCloseable {
    private static final TypeLiteral<Map<String, Object>> STRING_MAP = new TypeLiteral<Map<String, Object>>() {
    };

    private final Logger logger = LoggerFactory.getLogger(GraalPythonBindings.class);

    private Context context;
    private Map<String, Object> global;

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
    }

    @Override
    public Object put(String key, Object v) {
        checkKey(key);
        requireContext();

        context.getBindings("python").putMember(key, v);
        return global.put(key, v);
    }

    @Override
    public void clear() {
        if (context != null) {
            Value binding = context.getBindings("python");
            for (var entry : global.entrySet()) {
                binding.removeMember(entry.getKey());
            }
        }
    }

    @Override
    public Object get(Object key) {
        checkKey((String) key);
        requireContext();
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
        context.getBindings("python").removeMember((String) key);
        global.remove(key);
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

    void updateEngineScriptContext(ScriptContext scriptContext) {
        engineScriptContext = scriptContext;
    }
}
