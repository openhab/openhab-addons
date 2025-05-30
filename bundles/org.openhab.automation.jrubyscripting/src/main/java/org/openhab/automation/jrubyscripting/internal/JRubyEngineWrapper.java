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
package org.openhab.automation.jrubyscripting.internal;

import java.io.Reader;
import java.util.Objects;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jruby.embed.jsr223.JRubyEngine;

/**
 * This is a wrapper for {@link JRubyEngine}.
 * 
 * The purpose of this class is to intercept the call to eval and save the context into
 * a global variable for use in the helper library.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class JRubyEngineWrapper implements Invocable, ScriptEngine, AutoCloseable {
    // Don't implement Compilable because there is a bug
    // in JRuby's compiled scripts: https://github.com/jruby/jruby/issues/8346

    private final JRubyEngine engine;

    private static final String CONTEXT_VAR_NAME = "ctx";
    private static final String GLOBAL_VAR_NAME = "$" + CONTEXT_VAR_NAME;

    JRubyEngineWrapper(JRubyEngine engine) {
        this.engine = Objects.requireNonNull(engine);
    }

    @Override
    public Object eval(@Nullable String script, @Nullable ScriptContext context) throws ScriptException {
        Object ctx = Objects.requireNonNull(context).getBindings(ScriptContext.ENGINE_SCOPE).get(CONTEXT_VAR_NAME);

        if (ctx == null) {
            return engine.eval(script, context);
        }

        context.setAttribute(GLOBAL_VAR_NAME, ctx, ScriptContext.ENGINE_SCOPE);
        try {
            return engine.eval(script, context);
        } finally {
            context.removeAttribute(GLOBAL_VAR_NAME, ScriptContext.ENGINE_SCOPE);
        }
    }

    @Override
    public Object eval(@Nullable Reader reader, @Nullable ScriptContext context) throws ScriptException {
        Object ctx = Objects.requireNonNull(context).getBindings(ScriptContext.ENGINE_SCOPE).get(CONTEXT_VAR_NAME);

        if (ctx == null) {
            return engine.eval(reader, context);
        }

        context.setAttribute(GLOBAL_VAR_NAME, ctx, ScriptContext.ENGINE_SCOPE);
        try {
            return engine.eval(reader, context);
        } finally {
            context.removeAttribute(GLOBAL_VAR_NAME, ScriptContext.ENGINE_SCOPE);
        }
    }

    @Override
    public Object eval(@Nullable String script, @Nullable Bindings bindings) throws ScriptException {
        Object ctx = Objects.requireNonNull(bindings).get(CONTEXT_VAR_NAME);

        if (ctx == null) {
            return engine.eval(script, bindings);
        }

        bindings.put(GLOBAL_VAR_NAME, ctx);
        try {
            return engine.eval(script, bindings);
        } finally {
            bindings.remove(GLOBAL_VAR_NAME);
        }
    }

    @Override
    public Object eval(@Nullable Reader reader, @Nullable Bindings bindings) throws ScriptException {
        Object ctx = Objects.requireNonNull(bindings).get(CONTEXT_VAR_NAME);

        if (ctx == null) {
            return engine.eval(reader, bindings);
        }

        bindings.put(GLOBAL_VAR_NAME, ctx);
        try {
            return engine.eval(reader, bindings);
        } finally {
            bindings.remove(GLOBAL_VAR_NAME);
        }
    }

    @Override
    public Object eval(@Nullable String script) throws ScriptException {
        Object ctx = getBindings(ScriptContext.ENGINE_SCOPE).get(CONTEXT_VAR_NAME);

        if (ctx == null) {
            return engine.eval(script);
        }

        getContext().setAttribute(GLOBAL_VAR_NAME, ctx, ScriptContext.ENGINE_SCOPE);
        try {
            return engine.eval(script);
        } finally {
            getContext().removeAttribute(GLOBAL_VAR_NAME, ScriptContext.ENGINE_SCOPE);
        }
    }

    @Override
    public Object eval(@Nullable Reader reader) throws ScriptException {
        Object ctx = getBindings(ScriptContext.ENGINE_SCOPE).get(CONTEXT_VAR_NAME);

        if (ctx == null) {
            return engine.eval(reader);
        }

        getContext().setAttribute(GLOBAL_VAR_NAME, ctx, ScriptContext.ENGINE_SCOPE);
        try {
            return engine.eval(reader);
        } finally {
            getContext().removeAttribute(GLOBAL_VAR_NAME, ScriptContext.ENGINE_SCOPE);
        }
    }

    @Override
    public Object get(@Nullable String key) {
        return engine.get(key);
    }

    @Override
    public void put(@Nullable String key, @Nullable Object value) {
        engine.put(key, value);
    }

    @Override
    public Bindings getBindings(int scope) {
        return engine.getBindings(scope);
    }

    @Override
    public void setBindings(@Nullable Bindings bindings, int scope) {
        engine.setBindings(bindings, scope);
    }

    @Override
    public Bindings createBindings() {
        return engine.createBindings();
    }

    @Override
    public ScriptContext getContext() {
        return engine.getContext();
    }

    @Override
    public void setContext(@Nullable ScriptContext context) {
        engine.setContext(context);
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return engine.getFactory();
    }

    @Override
    public Object invokeMethod(@Nullable Object receiver, @Nullable String method, Object @Nullable... args)
            throws ScriptException, NoSuchMethodException {
        return engine.invokeMethod(receiver, method, args);
    }

    @Override
    public Object invokeFunction(@Nullable String method, Object @Nullable... args)
            throws ScriptException, NoSuchMethodException {
        return engine.invokeFunction(method, args);
    }

    @Override
    public <T> T getInterface(@Nullable Class<T> returnType) {
        return engine.getInterface(returnType);
    }

    @Override
    public <T> T getInterface(@Nullable Object receiver, @Nullable Class<T> returnType) {
        return engine.getInterface(receiver, returnType);
    }

    @Override
    public void close() {
        engine.close();
    }
}
