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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.graalvm.polyglot.Context.Builder;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.SourceSection;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Graal.Python implementation of the script engine. It provides access to the polyglot context using
 * {@link #getPolyglotContext()}.
 *
 * @author Holger Hees - Initial contribution
 * @author Jeff James - Initial contribution
 */
public final class GraalPythonScriptEngine extends AbstractScriptEngine
        implements Compilable, Invocable, AutoCloseable {

    public static final String LANGUAGE_ID = "python";
    private static final String POLYGLOT_CONTEXT = "polyglot.context";

    private static final String PYTHON_OPTION_POSIXMODULEBACKEND = "python.PosixModuleBackend";
    private static final String PYTHON_OPTION_DONTWRITEBYTECODEFLAG = "python.DontWriteBytecodeFlag";
    private static final String PYTHON_OPTION_FORCEIMPORTSITE = "python.ForceImportSite";
    private static final String PYTHON_OPTION_CHECKHASHPYCSMODE = "python.CheckHashPycsMode";

    private final Logger logger = LoggerFactory.getLogger(GraalPythonScriptEngine.class);

    private final GraalPythonScriptEngineFactory factory;
    private final Context.Builder contextConfig;

    GraalPythonScriptEngine(GraalPythonScriptEngineFactory factory) {
        this(factory, factory.getPolyglotEngine(), null);
    }

    GraalPythonScriptEngine(GraalPythonScriptEngineFactory factory, Engine engine, Context.Builder contextConfig) {
        Engine engineToUse = (engine != null) ? engine : factory.getPolyglotEngine();

        Context.Builder contextConfigToUse = contextConfig;
        if (contextConfigToUse == null) {
            contextConfigToUse = Context.newBuilder(LANGUAGE_ID) //
                    .allowExperimentalOptions(true) //
                    .allowAllAccess(true) //
                    .allowHostAccess(HostAccess.ALL) //
                    // allow creating python threads
                    .allowCreateThread(true) //
                    // allow running Python native extensions
                    .allowNativeAccess(true) //
                    // allow exporting Python values to polyglot bindings and accessing Java
                    // choose the backend for the POSIX module
                    .option(PYTHON_OPTION_POSIXMODULEBACKEND, "java") //
                    // equivalent to the Python -B flag
                    .option(PYTHON_OPTION_DONTWRITEBYTECODEFLAG, "true") //
                    // Force to automatically import site.py module, to make Python packages available
                    .option(PYTHON_OPTION_FORCEIMPORTSITE, "true") //
                    // causes the interpreter to always assume hash-based pycs are valid
                    .option(PYTHON_OPTION_CHECKHASHPYCSMODE, "never");
        }
        this.factory = (factory == null) ? new GraalPythonScriptEngineFactory(engineToUse) : factory;
        this.contextConfig = contextConfigToUse.engine(engineToUse);
        this.context.setBindings(new GraalPythonBindings(this.contextConfig, this.context, this),
                ScriptContext.ENGINE_SCOPE);
    }

    static Context createDefaultContext(Context.Builder builder, ScriptContext ctxt) {
        return builder.build();
    }

    /**
     * Closes the current context and makes it unusable. Operations performed after closing will
     * throw an {@link IllegalStateException}.
     */
    @Override
    public void close() {
        logger.debug("GraalPythonScriptEngine closed");

        // "true" to get an exception if something is still running in context
        getPolyglotContext().close(true);
    }

    /**
     * Returns the polyglot engine associated with this script engine.
     */
    public Engine getPolyglotEngine() {
        return factory.getPolyglotEngine();
    }

    public Context getPolyglotContext() {
        return getOrCreateGraalPythonBindings(context).getContext();
    }

    static Value evalInternal(Context context, String script) {
        return context.eval(Source.newBuilder(LANGUAGE_ID, script, "internal-script").internal(true).buildLiteral());
    }

    @Override
    public Bindings createBindings() {
        return new GraalPythonBindings(contextConfig, null, this);
    }

    @Override
    public void setBindings(Bindings bindings, int scope) {
        if (scope == ScriptContext.ENGINE_SCOPE) {
            Bindings oldBindings = getBindings(scope);
            if (oldBindings instanceof GraalPythonBindings gpBindings) {
                gpBindings.updateEngineScriptContext(null);
            }
        }
        super.setBindings(bindings, scope);
        if (scope == ScriptContext.ENGINE_SCOPE && bindings instanceof GraalPythonBindings gpBindings) {
            gpBindings.updateEngineScriptContext(getContext());
        }
    }

    @Override
    public Object eval(Reader reader, ScriptContext ctxt) throws ScriptException {
        return eval(createSource(read(reader), ctxt), ctxt);
    }

    static String read(Reader reader) throws ScriptException {
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

    @Override
    public Object eval(String script, ScriptContext ctxt) throws ScriptException {
        return eval(createSource(script, ctxt), ctxt);
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

    private Object eval(Source source, ScriptContext scriptContext) throws ScriptException {
        GraalPythonBindings engineBindings = getOrCreateGraalPythonBindings(scriptContext);
        Context polyglotContext = engineBindings.getContext();
        try {
            return polyglotContext.eval(source).as(Object.class);
        } catch (PolyglotException e) {
            throw toScriptException(e);
        }
    }

    private static ScriptException toScriptException(PolyglotException ex) {
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

    private GraalPythonBindings getOrCreateGraalPythonBindings(ScriptContext scriptContext) {
        Bindings engineB = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
        if (engineB instanceof GraalPythonBindings) {
            return ((GraalPythonBindings) engineB);
        } else {
            GraalPythonBindings bindings = new GraalPythonBindings(createContext(engineB), scriptContext, this);
            bindings.putAll(engineB);
            return bindings;
        }
    }

    private Context createContext(Bindings engineB) {
        Object ctx = engineB.get(POLYGLOT_CONTEXT);
        if (!(ctx instanceof Context)) {
            ctx = createDefaultContext(contextConfig, context);
            engineB.put(POLYGLOT_CONTEXT, ctx);
        }
        return (Context) ctx;
    }

    @Override
    public GraalPythonScriptEngineFactory getFactory() {
        return factory;
    }

    @Override
    public Object invokeMethod(Object thiz, String name, Object... args) throws ScriptException, NoSuchMethodException {
        if (thiz == null) {
            throw new IllegalArgumentException("thiz is not a valid object.");
        }
        GraalPythonBindings engineBindings = getOrCreateGraalPythonBindings(context);
        Value thisValue = engineBindings.getContext().asValue(thiz);

        if (!thisValue.canInvokeMember(name)) {
            if (!thisValue.hasMember(name)) {
                throw noSuchMethod(name);
            } else {
                throw notCallable(name);
            }
        }
        try {
            return thisValue.invokeMember(name, args).as(Object.class);
        } catch (PolyglotException e) {
            throw toScriptException(e);
        }
    }

    @Override
    public Object invokeFunction(String name, Object... args) throws ScriptException, NoSuchMethodException {
        GraalPythonBindings engineBindings = getOrCreateGraalPythonBindings(context);
        Value function = engineBindings.getContext().getBindings(LANGUAGE_ID).getMember(name);

        if (function == null) {
            throw noSuchMethod(name);
        } else if (!function.canExecute()) {
            throw notCallable(name);
        }
        try {
            return function.execute(args).as(Object.class);
        } catch (PolyglotException e) {
            throw toScriptException(e);
        }
    }

    private static NoSuchMethodException noSuchMethod(String name) throws NoSuchMethodException {
        throw new NoSuchMethodException(name);
    }

    private static NoSuchMethodException notCallable(String name) throws NoSuchMethodException {
        throw new NoSuchMethodException(name + " is not a function");
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

    @Override
    public CompiledScript compile(String script) throws ScriptException {
        Source source = createSource(script, getContext());
        return compile(source);
    }

    @Override
    public CompiledScript compile(Reader reader) throws ScriptException {
        Source source = createSource(read(reader), getContext());
        return compile(source);
    }

    private CompiledScript compile(Source source) throws ScriptException {
        checkSyntax(source);
        return new CompiledScript() {
            @Override
            public ScriptEngine getEngine() {
                return GraalPythonScriptEngine.this;
            }

            @Override
            public Object eval(ScriptContext ctx) throws ScriptException {
                return GraalPythonScriptEngine.this.eval(source, ctx);
            }
        };
    }

    private void checkSyntax(Source source) throws ScriptException {
        try {
            getPolyglotContext().parse(source);
        } catch (PolyglotException pex) {
            throw toScriptException(pex);
        }
    }

    /**
     * Creates a new GraalPython script engine from a polyglot Engine instance with a base configuration
     * for new polyglot {@link Context} instances. Polyglot context instances can be accessed from
     * {@link ScriptContext} instances using {@link #getPolyglotContext()}. The
     * {@link Builder#out(OutputStream) out},{@link Builder#err(OutputStream) err} and
     * {@link Builder#in(InputStream) in} stream configuration are not inherited from the provided
     * polyglot context config. Instead {@link ScriptContext} output and input streams are used.
     *
     * @param engine the engine to be used for context configurations or <code>null</code> if a
     *            default engine should be used.
     * @param newContextConfig a base configuration to create new context instances or
     *            <code>null</code> if the default configuration should be used to construct new
     *            context instances.
     */
    public static GraalPythonScriptEngine create(Engine engine, Context.Builder newContextConfig) {
        return new GraalPythonScriptEngine(null, engine, newContextConfig);
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
}
