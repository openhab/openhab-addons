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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.TypeLiteral;
import org.graalvm.polyglot.Value;

/***
 * @author Holger Hees - Initial contribution
 * @author Jeff James - Initial contribution
 */
final class GraalPythonBindings extends AbstractMap<String, Object> implements javax.script.Bindings, AutoCloseable {
    private static final TypeLiteral<Map<String, Object>> STRING_MAP = new TypeLiteral<Map<String, Object>>() {
    };

    private Context context;
    private Map<String, Object> global;

    private Context.Builder contextBuilder;
    // ScriptContext of the ScriptEngine where these bindings form ENGINE_SCOPE bindings
    private ScriptContext scriptContext;
    private ScriptEngine scriptEngine;

    GraalPythonBindings(Context.Builder contextBuilder, ScriptContext scriptContext, ScriptEngine scriptEngine) {
        this.contextBuilder = contextBuilder;
        this.scriptContext = scriptContext;
        this.scriptEngine = scriptEngine;
    }

    GraalPythonBindings(Context context, ScriptContext scriptContext, ScriptEngine scriptEngine) {
        this.context = context;
        this.scriptContext = scriptContext;
        this.scriptEngine = scriptEngine;

        initGlobal();
    }

    private void requireContext() {
        if (context == null) {
            context = GraalPythonScriptEngine.createDefaultContext(contextBuilder, scriptContext);

            initGlobal();
        }
    }

    private void initGlobal() {
        // GraalPythonScriptEngine.evalInternal(context, "globals()").as(STRING_MAP);
        this.global = new HashMap<String, Object>();

        requireContext();

        context.getBindings(GraalPythonScriptEngine.LANGUAGE_ID).putMember("engine", scriptEngine);
        if (scriptContext != null) {
            context.getBindings(GraalPythonScriptEngine.LANGUAGE_ID).putMember("context", scriptContext);
        }
    }

    @Override
    public Object put(String key, Object v) {
        checkKey(key);
        requireContext();

        context.getBindings(GraalPythonScriptEngine.LANGUAGE_ID).putMember(key, v);
        return global.put(key, v);
    }

    @Override
    public void clear() {
        if (context != null) {
            Value binding = context.getBindings(GraalPythonScriptEngine.LANGUAGE_ID);
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
        context.getBindings(GraalPythonScriptEngine.LANGUAGE_ID).removeMember((String) key);
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
        this.scriptContext = scriptContext;
    }
}
