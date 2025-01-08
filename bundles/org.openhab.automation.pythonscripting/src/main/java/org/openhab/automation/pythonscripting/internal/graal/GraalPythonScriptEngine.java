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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

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
import org.graalvm.polyglot.proxy.Proxy;

/**
 * A Graal.Python implementation of the script engine. It provides access to the polyglot context using
 * {@link #getPolyglotContext()}.
 *
 * @author Jeff James - Initial contribution
 */
public final class GraalPythonScriptEngine extends AbstractScriptEngine
        implements Compilable, Invocable, AutoCloseable {
    private static final String ID = "python";
    private static final String POLYGLOT_CONTEXT = "polyglot.context";
    private static final String OUT_SYMBOL = "$$internal.out$$";
    private static final String IN_SYMBOL = "$$internal.in$$";
    private static final String ERR_SYMBOL = "$$internal.err$$";
    // private static final String PYTHON_SCRIPT_ENGINE_GLOBAL_SCOPE_IMPORT_OPTION =
    // "python.script-engine-global-scope-import";

    private static final String PYTHON_OPTION_POSIXMODULEBACKEND = "python.PosixModuleBackend";
    private static final String PYTHON_OPTION_DONTWRITEBYTECODEFLAG = "python.DontWriteBytecodeFlag";
    private static final String PYTHON_OPTION_FORCEIMPORTSITE = "python.ForceImportSite";
    private static final String PYTHON_OPTION_CHECKHASHPYCSMODE = "python.CheckHashPycsMode";
    // private static final String INSECURE_SCRIPTENGINE_ACCESS_SYSTEM_PROPERTY =
    // "graalpy.insecure-scriptengine-access";

    static final String MAGIC_OPTION_PREFIX = "polyglot.py.";

    // ToString() operation
    private static String toString(Value value) {
        return toPrimitive(value).toString();
    }

    // "Type(result) is not Object" heuristic for the purpose of ToPrimitive() conversion
    private static boolean isPrimitive(Value value) {
        return value.isString() || value.isNumber() || value.isBoolean() || value.isNull();
    }

    // ToPrimitive()/OrdinaryToPrimitive() operation
    private static Value toPrimitive(Value value) {
        if (value.hasMembers()) {
            for (String methodName : new String[] { "toString", "valueOf" }) {
                if (value.canInvokeMember(methodName)) {
                    Value maybePrimitive = value.invokeMember(methodName);
                    if (isPrimitive(maybePrimitive)) {
                        return maybePrimitive;
                    }
                }
            }
        }
        if (isPrimitive(value)) {
            return value;
        } else {
            throw new ClassCastException();
        }
    }

    private static boolean toBoolean(double d) {
        return d != 0.0 && !Double.isNaN(d);
    }

    interface MagicBindingsOptionSetter {

        String getOptionKey();

        Context.Builder setOption(Builder builder, Object value);
    }

    private static boolean toBoolean(MagicBindingsOptionSetter optionSetter, Object value) {
        if (!(value instanceof Boolean)) {
            throw magicOptionValueErrorBool(optionSetter.getOptionKey(), value);
        }
        return (Boolean) value;
    }

    private final GraalPythonScriptEngineFactory factory;
    private final Context.Builder contextConfig;

    private boolean evalCalled;

    GraalPythonScriptEngine(GraalPythonScriptEngineFactory factory) {
        this(factory, factory.getPolyglotEngine(), null);
    }

    GraalPythonScriptEngine(GraalPythonScriptEngineFactory factory, Engine engine, Context.Builder contextConfig) {
        Engine engineToUse = (engine != null) ? engineToUse = engine : factory.getPolyglotEngine();
        // this.factory = (factory == null) ? new GraalPythonScriptEngineFactory(engineToUse) : factory;

        Context.Builder contextConfigToUse = contextConfig;
        if (contextConfigToUse == null) {
            contextConfigToUse = Context.newBuilder(ID) // TODO: ID
                    .allowExperimentalOptions(true) //
                    .allowAllAccess(true) //
                    .allowHostAccess(HostAccess.ALL) //
                    // TODO .allowIO(IOAccess.newBuilder().allowHostSocketAccess(true).fileSystem(null).build()) //
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
        DelegatingInputStream in = new DelegatingInputStream();
        DelegatingOutputStream out = new DelegatingOutputStream();
        DelegatingOutputStream err = new DelegatingOutputStream();
        if (ctxt != null) {
            in.setReader(ctxt.getReader());
            out.setWriter(ctxt.getWriter());
            err.setWriter(ctxt.getErrorWriter());
        }
        builder.in(in).out(out).err(err);
        Context ctx = builder.build();
        ctx.getPolyglotBindings().putMember(OUT_SYMBOL, out);
        ctx.getPolyglotBindings().putMember(ERR_SYMBOL, err);
        ctx.getPolyglotBindings().putMember(IN_SYMBOL, in);
        return ctx;
    }

    /**
     * Closes the current context and makes it unusable. Operations performed after closing will
     * throw an {@link IllegalStateException}.
     */
    @Override
    public void close() {
        getPolyglotContext().close();
    }

    /**
     * Returns the polyglot engine associated with this script engine.
     */
    public Engine getPolyglotEngine() {
        return factory.getPolyglotEngine();
    }

    /**
     * Returns the polyglot context associated with the default ScriptContext of the engine.
     *
     * @see #getPolyglotContext(ScriptContext) to access the polyglot context of a particular
     *      context.
     */
    public Context getPolyglotContext() {
        return getPolyglotContext(context);
    }

    /**
     * Returns the polyglot context associated with a ScriptContext. If the context is not yet
     * initialized then it will be initialized using the default context builder specified in
     * {@link #create(Engine, org.graalvm.polyglot.Context.Builder)}.
     */
    public Context getPolyglotContext(ScriptContext ctxt) {
        return getOrCreateGraalPythonBindings(ctxt).getContext();
    }

    static Value evalInternal(Context context, String script) {
        return context.eval(Source.newBuilder(ID, script, "internal-script").internal(true).buildLiteral());
    }

    @Override
    public Bindings createBindings() {
        return new GraalPythonBindings(contextConfig, null, this);
    }

    @Override
    public void setBindings(Bindings bindings, int scope) {
        if (scope == ScriptContext.ENGINE_SCOPE) {
            Bindings oldBindings = getBindings(scope);
            if (oldBindings instanceof GraalPythonBindings) {
                ((GraalPythonBindings) oldBindings).updateEngineScriptContext(null);
            }
        }
        super.setBindings(bindings, scope);
        if (scope == ScriptContext.ENGINE_SCOPE && (bindings instanceof GraalPythonBindings)) {
            ((GraalPythonBindings) bindings).updateEngineScriptContext(getContext());
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
            try {
                while (true) {
                    final int count = reader.read(buffer);
                    if (count == -1) {
                        break;
                    }
                    builder.append(buffer, 0, count);
                }
            } finally {
                reader.close();
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
            return Source.newBuilder(ID, script, "<eval>").buildLiteral();
        } else {
            try {
                return Source.newBuilder(ID, new File(val.toString())).content(script).build();
            } catch (IOException ioex) {
                throw new ScriptException(ioex);
            }
        }
    }

    private static void updateDelegatingIOStreams(Context polyglotContext, ScriptContext scriptContext) {
        Value polyglotBindings = polyglotContext.getPolyglotBindings();
        ((DelegatingOutputStream) polyglotBindings.getMember(OUT_SYMBOL).asProxyObject())
                .setWriter(scriptContext.getWriter());
        ((DelegatingOutputStream) polyglotBindings.getMember(ERR_SYMBOL).asProxyObject())
                .setWriter(scriptContext.getErrorWriter());
        ((DelegatingInputStream) polyglotBindings.getMember(IN_SYMBOL).asProxyObject())
                .setReader(scriptContext.getReader());
    }

    private Object eval(Source source, ScriptContext scriptContext) throws ScriptException {
        GraalPythonBindings engineBindings = getOrCreateGraalPythonBindings(scriptContext);
        Context polyglotContext = engineBindings.getContext();
        updateDelegatingIOStreams(polyglotContext, scriptContext);
        try {
            /*
             * if (!evalCalled) {
             * jrunscriptInitWorkaround(source, polyglotContext);
             * }
             */
            engineBindings.importGlobalBindings(scriptContext);
            return polyglotContext.eval(source).as(Object.class);
        } catch (PolyglotException e) {
            throw toScriptException(e);
        } finally {
            evalCalled = true;
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
            Context.Builder builder = contextConfig;
            ctx = createDefaultContext(builder, context);
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
        engineBindings.importGlobalBindings(context);
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
        engineBindings.importGlobalBindings(context);
        Value function = engineBindings.getContext().getBindings(ID).getMember(name);

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

    private static class DelegatingInputStream extends InputStream implements Proxy {

        private Reader reader;
        private CharsetEncoder encoder = Charset.defaultCharset().newEncoder();
        private CharBuffer charBuffer = CharBuffer.allocate(2);
        private ByteBuffer byteBuffer = ByteBuffer.allocate((int) encoder.maxBytesPerChar() * 2);

        DelegatingInputStream() {
            byteBuffer.flip();
        }

        @Override
        public int read() throws IOException {
            if (reader != null) {
                while (!byteBuffer.hasRemaining()) {
                    int c = reader.read();
                    if (c == -1) {
                        return -1;
                    }
                    byteBuffer.clear();
                    charBuffer.put((char) c);
                    charBuffer.flip();
                    encoder.encode(charBuffer, byteBuffer, false);
                    charBuffer.compact();
                    byteBuffer.flip();
                }
                return byteBuffer.get();
            }
            return 0;
        }

        void setReader(Reader reader) {
            this.reader = reader;
        }
    }

    private static class DelegatingOutputStream extends OutputStream implements Proxy {

        private Writer writer;
        private CharsetDecoder decoder = Charset.defaultCharset().newDecoder();
        private ByteBuffer byteBuffer = ByteBuffer
                .allocate((int) Charset.defaultCharset().newEncoder().maxBytesPerChar() * 2);
        private CharBuffer charBuffer = CharBuffer.allocate(byteBuffer.capacity() * (int) decoder.maxCharsPerByte());

        @Override
        public void write(int b) throws IOException {
            if (writer != null) {
                byteBuffer.put((byte) b);
                byteBuffer.flip();
                decoder.decode(byteBuffer, charBuffer, false);
                byteBuffer.compact();
                charBuffer.flip();
                while (charBuffer.hasRemaining()) {
                    char c = charBuffer.get();
                    writer.write(c);
                }
                charBuffer.clear();
            }
        }

        @Override
        public void flush() throws IOException {
            if (writer != null) {
                writer.flush();
            }
        }

        void setWriter(Writer writer) {
            this.writer = writer;
        }
    }

    /**
     * Creates a new GraalPythonScriptEngine with default configuration.
     *
     * @see #create(Engine, Context.Builder) to customize the configuration.
     */
    public static GraalPythonScriptEngine create() {
        return create(null, null);
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

    /**
     * Detects jrunscript "init.js" and installs a JSAdapter polyfill if needed.
     */
    /*
     * private static void jrunscriptInitWorkaround(Source source, Context polyglotContext) {
     * if (source.getName().equals(JRUNSCRIPT_INIT_NAME)) {
     * String initCode = source.getCharacters().toString();
     * if (initCode.contains("jrunscript") && initCode.contains("JSAdapter")
     * && !polyglotContext.getBindings(ID).hasMember("JSAdapter")) {
     * polyglotContext.eval(ID, JSADAPTER_POLYFILL);
     * }
     * }
     * }
     */

    //
    /*
     * private static final String JRUNSCRIPT_INIT_NAME = "<system-init>";
     * private static final String JSADAPTER_POLYFILL = "this.JSAdapter || "
     * +
     * "Object.defineProperty(this, \"JSAdapter\", {configurable:true, writable:true, enumerable: false, value: function(t) {\n"
     * + "    var target = {};\n" + "    var handler = {\n"
     * +
     * "        get: function(target, name) {return typeof t.__get__ == 'function' ? t.__get__.call(target, name) : undefined;},\n"
     * +
     * "        has: function(target, name) {return typeof t.__has__ == 'function' ? t.__has__.call(target, name) : false;},\n"
     * +
     * "        deleteProperty: function(target, name) {return typeof t.__delete__ == 'function' ? t.__delete__.call(target, name) : true;},\n"
     * +
     * "        set: function(target, name, value) {return typeof t.__put__ == 'function' ? t.__put__.call(target, name, value) : undefined;},\n"
     * +
     * "        ownKeys: function(target) {return typeof t.__getIds__ == 'function' ? t.__getIds__.call(target) : [];},\n"
     * + "    }\n" + "    return new Proxy(target, handler);\n" + "}});\n";
     */

    private static IllegalArgumentException magicOptionValueErrorBool(String name, Object v) {
        return new IllegalArgumentException(
                String.format("failed to set graal-js option \"%s\": expected a boolean value, got \"%s\"", name, v));
    }
}
