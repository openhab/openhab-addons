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
package org.openhab.automation.pythonscripting.internal.scriptengine.graal;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;

/***
 * A Graal.Python implementation of Bindings backed by a HashMap or some other specified Map.
 *
 * @author Holger Hees - Initial contribution
 * @author Jeff James - Initial contribution
 */
final class GraalPythonBindings extends AbstractMap<String, Object> implements javax.script.Bindings, AutoCloseable {
    private Context context;
    private Map<String, Object> global;

    private Context.Builder contextBuilder;
    // ScriptContext of the ScriptEngine where these bindings form ENGINE_SCOPE bindings
    private ScriptContext scriptContext;
    private ScriptEngine scriptEngine;

    private boolean isClosed = false;

    GraalPythonBindings(Context.Builder contextBuilder, ScriptContext scriptContext, ScriptEngine scriptEngine) {
        this.contextBuilder = contextBuilder;
        this.scriptContext = scriptContext;
        this.scriptEngine = scriptEngine;
    }

    public Context getContext() {
        requireContext();
        return context;
    }

    @Override
    public Object put(String key, Object v) {
        requireContext();
        context.getBindings(GraalPythonScriptEngine.LANGUAGE_ID).putMember(key, v);
        return global.put(key, v);
    }

    @Override
    public Object get(Object key) {
        requireContext();
        return global.get(key);
    }

    @Override
    public Object remove(Object key) {
        requireContext();
        Object prev = get(key);
        context.getBindings(GraalPythonScriptEngine.LANGUAGE_ID).removeMember((String) key);
        global.remove(key);
        return prev;
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
    public Set<Entry<String, Object>> entrySet() {
        requireContext();
        return global.entrySet();
    }

    /**
     * Closes the current context and makes it unusable.
     *
     * @throws PolyglotException when an error happens in guest language
     * @throws IllegalStateException when an operation is performed after closing
     */
    @Override
    public void close() throws PolyglotException, IllegalStateException {
        if (context != null) {
            context.close(true);
            // context = null;
            // global = null;
        }
        isClosed = true;
    }

    public boolean isClosed() {
        return isClosed;
    }

    private void requireContext() {
        if (context == null) {
            if (isClosed) {
                throw new IllegalStateException("Context already closed");
            }
            context = contextBuilder.build();
            global = new HashMap<>();

            context.getBindings(GraalPythonScriptEngine.LANGUAGE_ID).putMember("__engine__", scriptEngine);
            if (scriptContext != null) {
                context.getBindings(GraalPythonScriptEngine.LANGUAGE_ID).putMember("__context__", scriptContext);
            }
        }
    }
}
