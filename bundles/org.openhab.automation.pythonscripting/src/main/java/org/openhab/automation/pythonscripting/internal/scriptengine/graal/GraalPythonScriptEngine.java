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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.SourceSection;
import org.graalvm.polyglot.Value;

/**
 * A Graal.Python implementation of the script engine.
 *
 * @author Holger Hees - Initial contribution
 * @author Jeff James - Initial contribution
 */
public abstract class GraalPythonScriptEngine extends AbstractScriptEngine
        implements Compilable, Invocable, AutoCloseable {
    public static final String LANGUAGE_ID = "python";

    private GraalPythonScriptEngineFactory factory;
    private GraalPythonBindings bindings;

    /**
     * Creates a new GraalPython script engine from a polyglot Engine instance with a base configuration
     *
     * @param engine
     *
     * @param engine the engine to be used for context configurations
     * @param contextConfig a base configuration to create new context instances
     */
    protected void init(Engine engine, Context.Builder contextConfig, ScriptEngineProvider scriptEngineProvider) {
        this.bindings = new GraalPythonBindings(contextConfig.engine(engine), this.context, this);
        this.factory = new GraalPythonScriptEngineFactory(engine, scriptEngineProvider);
        this.context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
    }

    /*
     * First call will initialize lazy polyglot context
     */
    protected Context getPolyglotContext() {
        return bindings.getContext();
    }

    protected boolean isClosed() {
        return bindings.isClosed();
    }

    /**
     * Closes the current context and makes it unusable.
     *
     * Error happens in guest language will throw an {@link PolyglotException}.
     * Operations performed after closing will throw an {@link IllegalStateException}.
     */
    @Override
    public void close() throws ScriptException, IllegalStateException {
        try {
            bindings.close();
        } catch (PolyglotException e) {
            throw toScriptException(e);
        }
    }

    @Override
    public GraalPythonScriptEngineFactory getFactory() {
        return factory;
    }

    @Override
    public Bindings createBindings() {
        // Creating a new binding to replace the current one is not needed i context of pythonscripting
        throw new IllegalArgumentException("Creating new bindings is not supported in the context of pythonscripting");
    }

    @Override
    public void setBindings(Bindings bindings, int scope) {
        // Setting a new binding to replace the current one is not needed i context of pythonscripting
        throw new IllegalArgumentException("Setting bindings is not supported in the context of pythonscripting");
    }

    @Override
    public Object eval(Reader reader, ScriptContext ctxt) throws ScriptException {
        return eval(read(reader), ctxt);
    }

    @Override
    public Object eval(String script, ScriptContext ctxt) throws ScriptException {
        return eval(createSource(script, ctxt));
    }

    private Object eval(Source source) throws ScriptException {
        try {
            return getPolyglotContext().eval(source).as(Object.class);
        } catch (PolyglotException e) {
            throw toScriptException(e);
        }
    }

    @Override
    public Object invokeMethod(Object thiz, String name, Object... args) throws ScriptException, NoSuchMethodException {
        if (thiz == null) {
            throw new IllegalArgumentException("thiz is not a valid object.");
        }

        try {
            Value thisValue = getPolyglotContext().asValue(thiz);
            if (!thisValue.canInvokeMember(name)) {
                if (!thisValue.hasMember(name)) {
                    throw new NoSuchMethodException(name);
                } else {
                    throw new NoSuchMethodException(name + " is not a function");
                }
            }
            return thisValue.invokeMember(name, args).as(Object.class);
        } catch (PolyglotException e) {
            throw toScriptException(e);
        }
    }

    @Override
    public Object invokeFunction(String name, Object... args) throws ScriptException, NoSuchMethodException {
        try {
            Value function = getPolyglotContext().getBindings(LANGUAGE_ID).getMember(name);
            if (function == null) {
                throw new NoSuchMethodException(name);
            } else if (!function.canExecute()) {
                throw new NoSuchMethodException(name + " is not a function");
            }
            return function.execute(args).as(Object.class);
        } catch (PolyglotException e) {
            throw toScriptException(e);
        }
    }

    @Override
    public <T> T getInterface(Class<T> clasz) {
        checkInterface(clasz);
        return getInterfaceInner(evalInternal(getPolyglotContext(), "this"), clasz);
    }

    @Override
    public <T> T getInterface(Object thiz, Class<T> clasz) {
        if (thiz == null) {
            throw new IllegalArgumentException("this cannot be null");
        }
        checkInterface(clasz);
        Value thisValue = getPolyglotContext().asValue(thiz);
        checkThis(thisValue);
        return getInterfaceInner(thisValue, clasz);
    }

    @Override
    public CompiledScript compile(Reader reader) throws ScriptException {
        return compile(read(reader));
    }

    @Override
    public CompiledScript compile(String script) throws ScriptException {
        Source source = createSource(script, getContext());
        return compile(source);
    }

    private CompiledScript compile(Source source) throws ScriptException {
        try {
            // Syntax check
            getPolyglotContext().parse(source);
        } catch (PolyglotException pex) {
            throw toScriptException(pex);
        }

        return new CompiledScript() {
            @Override
            public ScriptEngine getEngine() {
                return GraalPythonScriptEngine.this;
            }

            @Override
            public Object eval(ScriptContext ctx) throws ScriptException {
                return GraalPythonScriptEngine.this.eval(source);
            }
        };
    }

    private static void checkInterface(Class<?> clasz) {
        if (clasz == null || !clasz.isInterface()) {
            throw new IllegalArgumentException("interface Class expected in getInterface");
        }
    }

    private static void checkThis(Value thiz) {
        if (thiz.isHostObject() || !thiz.hasMembers()) {
            throw new IllegalArgumentException("getInterface cannot be called on non-script object");
        }
    }

    private static <T> T getInterfaceInner(Value thiz, Class<T> iface) {
        if (!isInterfaceImplemented(iface, thiz)) {
            return null;
        }
        return thiz.as(iface);
    }

    private static String read(Reader reader) throws ScriptException {
        final StringBuilder builder = new StringBuilder();
        final char[] buffer = new char[1024];
        try {
            try (reader) {
                while (true) {
                    final int count = reader.read(buffer);
                    if (count == -1) {
                        break;
                    }
                    builder.append(buffer, 0, count);
                }
            }
            return builder.toString();
        } catch (IOException ioex) {
            throw new ScriptException(ioex);
        }
    }

    private static Source createSource(String script, ScriptContext ctxt) throws ScriptException {
        final Object val = ctxt.getAttribute(ScriptEngine.FILENAME);
        if (val == null) {
            return Source.newBuilder(LANGUAGE_ID, script, "<eval>").buildLiteral();
        } else {
            try {
                return Source.newBuilder(LANGUAGE_ID, new File(val.toString())).content(script).build();
            } catch (IOException ioex) {
                throw new ScriptException(ioex);
            }
        }
    }

    private static boolean isInterfaceImplemented(final Class<?> iface, final Value obj) {
        for (final Method method : iface.getMethods()) {
            // ignore methods of java.lang.Object class
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }

            // skip check for default methods - non-abstract, interface methods
            if (!Modifier.isAbstract(method.getModifiers())) {
                continue;
            }

            if (!obj.canInvokeMember(method.getName())) {
                return false;
            }
        }
        return true;
    }

    protected static ScriptException toScriptException(PolyglotException ex) {
        ScriptException sex;
        if (ex.isHostException()) {
            Throwable hostException = ex.asHostException();
            // ScriptException (unlike almost any other exception) does not
            // accept Throwable cause (requires the cause to be Exception)
            Exception cause;
            if (hostException instanceof Exception) {
                cause = (Exception) hostException;
            } else {
                cause = new Exception(hostException);
            }
            // Make the host exception accessible through the cause chain
            sex = new ScriptException(cause);
            // Re-use the stack-trace of PolyglotException (with guest-language stack-frames)
            sex.setStackTrace(ex.getStackTrace());
        } else {
            SourceSection sourceSection = ex.getSourceLocation();
            if (sourceSection != null && sourceSection.isAvailable()) {
                Source source = sourceSection.getSource();
                String fileName = source.getPath();
                if (fileName == null) {
                    fileName = source.getName();
                }
                int lineNo = sourceSection.getStartLine();
                int columnNo = sourceSection.getStartColumn();
                sex = new ScriptException(ex.getMessage(), fileName, lineNo, columnNo);
                sex.initCause(ex);
            } else {
                sex = new ScriptException(ex);
            }
        }
        return sex;
    }

    private static Value evalInternal(Context context, String script) {
        return context.eval(Source.newBuilder(LANGUAGE_ID, script, "internal-script").internal(true).buildLiteral());
    }

    public static interface ScriptEngineProvider {
        ScriptEngine createScriptEngine();
    }
}
