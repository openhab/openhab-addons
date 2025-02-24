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
package org.openhab.automation.pythonscripting.internal;

import static org.openhab.core.automation.module.script.ScriptEngineFactory.*;
import static org.openhab.core.automation.module.script.ScriptTransformationService.OPENHAB_TRANSFORMATION_SCRIPT;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AccessMode;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.script.ScriptContext;
import javax.script.ScriptException;

import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.io.IOAccess;
import org.openhab.automation.pythonscripting.internal.fs.DelegatingFileSystem;
import org.openhab.automation.pythonscripting.internal.fs.watch.PythonDependencyTracker;
import org.openhab.automation.pythonscripting.internal.graal.GraalPythonScriptEngine;
import org.openhab.automation.pythonscripting.internal.scriptengine.InvocationInterceptingScriptEngineWithInvocableAndCompilableAndAutoCloseable;
import org.openhab.automation.pythonscripting.internal.wrapper.ScriptExtensionModuleProvider;
import org.openhab.core.OpenHAB;
import org.openhab.core.automation.module.script.ScriptExtensionAccessor;
import org.openhab.core.items.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * GraalPython ScriptEngine implementation
 *
 * @author Holger Hees - Initial contribution
 * @author Jeff James - Initial contribution
 */
public class PythonScriptEngine
        extends InvocationInterceptingScriptEngineWithInvocableAndCompilableAndAutoCloseable<GraalPythonScriptEngine>
        implements Lock {
    private final Logger logger = LoggerFactory.getLogger(PythonScriptEngine.class);

    private static final String PYTHON_OPTION_EXECUTABLE = "python.Executable";
    // private static final String PYTHON_OPTION_PYTHONHOME = "python.PythonHome";
    private static final String PYTHON_OPTION_PYTHONPATH = "python.PythonPath";
    private static final String PYTHON_OPTION_INPUTFILEPATH = "python.InputFilePath";
    private static final String PYTHON_OPTION_EMULATEJYTHON = "python.EmulateJython";
    private static final String PYTHON_OPTION_POSIXMODULEBACKEND = "python.PosixModuleBackend";
    private static final String PYTHON_OPTION_DONTWRITEBYTECODEFLAG = "python.DontWriteBytecodeFlag";
    private static final String PYTHON_OPTION_FORCEIMPORTSITE = "python.ForceImportSite";
    private static final String PYTHON_OPTION_CHECKHASHPYCSMODE = "python.CheckHashPycsMode";
    private static final String PYTHON_OPTION_ALWAYSRUNEXCEPTHOOK = "python.AlwaysRunExcepthook";

    private static final String PYTHON_OPTION_CACHEDIR = "python.PyCachePrefix";
    private static final String PYTHON_CACHEDIR_PATH = Paths
            .get(OpenHAB.getUserDataFolder(), "cache", PythonScriptEngine.class.getPackageName(), "cachedir")
            .toString();

    private static final int STACK_TRACE_LENGTH = 5;

    public static final String LOGGER_INIT_NAME = "__logger_init__";

    /** Shared Polyglot {@link Engine} across all instances of {@link PythonScriptEngine} */
    private static final Engine ENGINE = Engine.newBuilder().allowExperimentalOptions(true)
            .option("engine.WarnInterpreterOnly", "false").build();

    /** Provides unlimited host access as well as custom translations from Python to Java Objects */
    private static final HostAccess HOST_ACCESS = HostAccess.newBuilder(HostAccess.ALL)
            // Translate python datetime with timezone to java.time.ZonedDateTime
            .targetTypeMapping(Value.class, ZonedDateTime.class,
                    v -> v.hasMember("ctime") && v.hasMember("isoformat") && v.hasMember("tzinfo")
                            && !v.getMember("tzinfo").isNull(),
                    v -> ZonedDateTime.parse(v.invokeMember("isoformat").asString()),
                    HostAccess.TargetMappingPrecedence.LOW)

            // Translate python datetime without timezone to java.time.Instant
            .targetTypeMapping(Value.class, Instant.class,
                    v -> v.hasMember("ctime") && v.hasMember("isoformat") && v.hasMember("tzinfo")
                            && v.getMember("tzinfo").isNull(),
                    v -> Instant.parse(v.invokeMember("isoformat").asString()), HostAccess.TargetMappingPrecedence.LOW)

            // Translate python timedelta to java.time.Duration
            .targetTypeMapping(Value.class, Duration.class,
                    // picking two members to check as Duration has many common function names
                    v -> v.hasMember("total_seconds") && v.hasMember("total_seconds"),
                    v -> Duration.ofNanos(v.invokeMember("total_seconds").asLong() * 10000000),
                    HostAccess.TargetMappingPrecedence.LOW)

            // Translate python item to org.openhab.core.items.Item
            .targetTypeMapping(Value.class, Item.class, v -> v.hasMember("raw_item"),
                    v -> v.getMember("raw_item").as(Item.class), HostAccess.TargetMappingPrecedence.LOW)

            // Translate python quantity to org.openhab.core.library.types.QuantityType
            // .targetTypeMapping(Value.class, QuantityType.class, v -> v.hasMember("rawQtyType"),
            // v -> v.getMember("rawQtyType").as(QuantityType.class), HostAccess.TargetMappingPrecedence.LOW)

            // Translate python GraalWrapperSet to java.util.Set
            .targetTypeMapping(Value.class, Set.class, v -> v.hasMember("isSetType"),
                    PythonScriptEngine::transformGraalWrapperSet, HostAccess.TargetMappingPrecedence.LOW)

            // Translate python list to java.util.Collection
            .targetTypeMapping(Value.class, Collection.class, (v) -> v.hasArrayElements(),
                    (v) -> v.as(Collection.class), HostAccess.TargetMappingPrecedence.LOW)

            .build();

    /** {@link Lock} synchronization of multi-thread access */
    private final Lock lock = new ReentrantLock();

    // these fields start as null because they are populated on first use
    private @Nullable Consumer<String> scriptDependencyListener;
    private String engineIdentifier; // this field is very helpful for debugging, please do not remove it
    private final ScriptExtensionModuleProvider scriptExtensionModuleProvider;
    private final LifecycleTracker lifecycleTracker;

    private int injectionEnabled = PythonScriptEngineFactory.INJECTION_DISABLED;
    private boolean scopeEnabled = false;

    private boolean initialized = false;

    private final LogOutputStream scriptOutputStream;
    private final LogOutputStream scriptErrorStream;

    /**
     * Creates an implementation of ScriptEngine {@code (& Invocable)}, wrapping the contained engine,
     * that tracks the script lifecycle and provides hooks for scripts to do so too.
     *
     * @param pythonDependencyTracker
     * @param injectionEnabled
     * @param scopeEnabled
     * @param cachingEnabled
     * @param jythonEmulation
     */
    public PythonScriptEngine(PythonDependencyTracker pythonDependencyTracker, int injectionEnabled,
            boolean scopeEnabled, boolean cachingEnabled, boolean jythonEmulation) {
        super(null); // delegate depends on fields not yet initialised, so we cannot set it immediately

        this.injectionEnabled = injectionEnabled;
        this.scopeEnabled = scopeEnabled;

        scriptOutputStream = new LogOutputStream(logger, Level.INFO);
        scriptErrorStream = new LogOutputStream(logger, Level.ERROR);

        scriptExtensionModuleProvider = new ScriptExtensionModuleProvider();
        lifecycleTracker = new LifecycleTracker();

        Context.Builder contextConfig = Context.newBuilder(GraalPythonScriptEngine.LANGUAGE_ID) //
                .out(scriptOutputStream) //
                .err(scriptErrorStream) //
                .allowIO(IOAccess.newBuilder() //
                        .allowHostSocketAccess(true) //
                        .fileSystem(new DelegatingFileSystem(FileSystems.getDefault().provider()) {
                            @Override
                            public void checkAccess(Path path, Set<? extends AccessMode> modes,
                                    LinkOption... linkOptions) throws IOException {
                                if (path.toRealPath().startsWith(PythonScriptEngineFactory.PYTHON_LIB_PATH)) {
                                    Consumer<String> localScriptDependencyListener = scriptDependencyListener;
                                    if (localScriptDependencyListener != null) {
                                        localScriptDependencyListener.accept(path.toRealPath().toString());
                                    }
                                }

                                super.checkAccess(path, modes, linkOptions);
                            }
                        }).build()) //
                .allowHostAccess(HOST_ACCESS) //
                // .allowHostClassLoading(true) //
                .allowAllAccess(true) //
                // allow class lookup like "org.slf4j.LoggerFactory" from inline scripts
                .hostClassLoader(getClass().getClassLoader()) //
                // allow creating python threads
                .allowCreateThread(true)
                // .allowCreateProcess(true) //
                // allow running Python native extensions
                .allowNativeAccess(true) //
                // allow exporting Python values to polyglot bindings and accessing Java from Python
                .allowPolyglotAccess(PolyglotAccess.ALL)
                // allow experimental options
                .allowExperimentalOptions(true) //
                // choose the backend for the POSIX module
                .option(PYTHON_OPTION_POSIXMODULEBACKEND, "java") //
                // Force to automatically import site.py module, to make Python packages available
                .option(PYTHON_OPTION_FORCEIMPORTSITE, Boolean.toString(true)) //
                // The sys.executable path, a virtual path that is used by the interpreter
                // to discover packages
                .option(PYTHON_OPTION_EXECUTABLE,
                        PythonScriptEngineFactory.PYTHON_DEFAULT_PATH.resolve("bin").resolve("python").toString())
                // Set the python home to be read from the embedded resources
                // .option(PYTHON_OPTION_PYTHONHOME, PYTHON_DEFAULT_PATH.toString()) //
                // Set python path to point to sources stored in
                .option(PYTHON_OPTION_PYTHONPATH,
                        PythonScriptEngineFactory.PYTHON_LIB_PATH.toString() + File.pathSeparator
                                + PythonScriptEngineFactory.PYTHON_DEFAULT_PATH.toString())
                // pass the path to be executed
                .option(PYTHON_OPTION_INPUTFILEPATH, PythonScriptEngineFactory.PYTHON_DEFAULT_PATH.toString()) //
                // make sure the TopLevelExceptionHandler calls the excepthook to print Python exceptions
                .option(PYTHON_OPTION_ALWAYSRUNEXCEPTHOOK, Boolean.toString(true)) //
                // emulate jython behavior (will slowdown the engine)
                .option(PYTHON_OPTION_EMULATEJYTHON, String.valueOf(jythonEmulation));

        if (cachingEnabled) {
            contextConfig.option(PYTHON_OPTION_DONTWRITEBYTECODEFLAG, Boolean.toString(false)) //
                    .option(PYTHON_OPTION_CACHEDIR, PYTHON_CACHEDIR_PATH);
        } else {
            contextConfig.option(PYTHON_OPTION_DONTWRITEBYTECODEFLAG, Boolean.toString(true)) //
                    // causes the interpreter to always assume hash-based pycs are valid
                    .option(PYTHON_OPTION_CHECKHASHPYCSMODE, "never");
        }

        delegate = GraalPythonScriptEngine.create(ENGINE, contextConfig);
    }

    @Override
    protected void beforeInvocation() {

        lock.lock();
        logger.debug("Lock acquired before invocation.");

        if (initialized) {
            return;
        }

        logger.debug("Initializing GraalPython script engine...");

        ScriptContext ctx = getScriptContext();

        // these are added post-construction, so we need to fetch them late
        String engineIdentifier = (String) ctx.getAttribute(CONTEXT_KEY_ENGINE_IDENTIFIER);
        if (engineIdentifier == null) {
            throw new IllegalStateException("Failed to retrieve engine identifier from engine bindings");
        }
        this.engineIdentifier = engineIdentifier;

        ScriptExtensionAccessor scriptExtensionAccessor = (ScriptExtensionAccessor) ctx
                .getAttribute(CONTEXT_KEY_EXTENSION_ACCESSOR);
        if (scriptExtensionAccessor == null) {
            throw new IllegalStateException("Failed to retrieve script extension accessor from engine bindings");
        }

        Consumer<String> scriptDependencyListener = (Consumer<String>) ctx
                .getAttribute(CONTEXT_KEY_DEPENDENCY_LISTENER);
        if (scriptDependencyListener == null) {
            logger.warn(
                    "Failed to retrieve script script dependency listener from engine bindings. Script dependency tracking will be disabled.");
        }
        this.scriptDependencyListener = scriptDependencyListener;

        if (scopeEnabled) {
            // Wrap the "import" function to also allow loading modules from the ScriptExtensionModuleProvider
            BiFunction<String, List<String>, Object> wrapImportFn = (name, fromlist) -> scriptExtensionModuleProvider
                    .locatorFor(delegate.getPolyglotContext(), engineIdentifier, scriptExtensionAccessor)
                    .locateModule(name, fromlist);
            delegate.getBindings(ScriptContext.ENGINE_SCOPE).put(ScriptExtensionModuleProvider.IMPORT_PROXY_NAME,
                    wrapImportFn);
            try {
                String wrapperContent = new String(
                        Files.readAllBytes(PythonScriptEngineFactory.PYTHON_WRAPPER_FILE_PATH));
                delegate.getPolyglotContext().eval(Source.newBuilder(GraalPythonScriptEngine.LANGUAGE_ID,
                        wrapperContent, PythonScriptEngineFactory.PYTHON_WRAPPER_FILE_PATH.toString()).build());

                // inject scope, Registry and logger
                if (injectionEnabled != PythonScriptEngineFactory.INJECTION_DISABLED
                        && (ctx.getAttribute("javax.script.filename") == null
                                || injectionEnabled == PythonScriptEngineFactory.INJECTION_ENABLED_FOR_ALL_SCRIPTS)) {
                    String injectionContent = "import scope\nfrom openhab import Registry, logger";
                    delegate.getPolyglotContext().eval(Source
                            .newBuilder(GraalPythonScriptEngine.LANGUAGE_ID, injectionContent, "<generated>").build());
                }

            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to generate import wrapper", e);
            }
        }

        // logger initialization, for non file based scripts, has to be delayed, because ruleUID is not available yet
        if (ctx.getAttribute("javax.script.filename") == null) {
            Runnable wrapperLoggerFn = () -> setScriptLogger();
            delegate.getBindings(ScriptContext.ENGINE_SCOPE).put(LOGGER_INIT_NAME, wrapperLoggerFn);
        } else {
            setScriptLogger();
        }

        initialized = true;
    }

    @Override
    protected String beforeInvocation(String source) {
        String _source = super.beforeInvocation(source);

        // Happens for Transform and UI based rules (eval and compile)
        // and has to be evaluate every time, because of changing and late injected ruleUID
        if (delegate.getBindings(ScriptContext.ENGINE_SCOPE).get(LOGGER_INIT_NAME) != null) {
            return LOGGER_INIT_NAME + "()\n" + _source;
        }

        return _source;
    }

    @Override
    protected Object afterInvocation(Object obj) {
        lock.unlock();
        logger.debug("Lock released after invocation.");
        return super.afterInvocation(obj);
    }

    @Override
    protected Exception afterThrowsInvocation(Exception e) {
        // OPS4J Pax Logging holds a reference to the exception, which causes the OpenhabGraalJSScriptEngine to not be
        // removed from heap by garbage collection and causing a memory leak.
        // Therefore, don't pass the exceptions itself to the logger, but only their message!
        if (e instanceof ScriptException) {
            // PolyglotException will always be wrapped into ScriptException and they will be visualized in
            // org.openhab.core.automation.module.script.internal.ScriptEngineManagerImpl
            if (scriptErrorStream.logger.isDebugEnabled()) {
                scriptErrorStream.logger.debug("Failed to execute script (PolyglotException): {}",
                        stringifyThrowable(e.getCause()));
            }
        } else if (e.getCause() instanceof IllegalArgumentException) {
            scriptErrorStream.logger.error("Failed to execute script (IllegalArgumentException): {}",
                    stringifyThrowable(e.getCause()));
        }

        lock.unlock();

        return super.afterThrowsInvocation(e);
    }

    @Override
    // collect JSR223 (scope) variables separately, because they are delivered via 'import scope'
    public void put(String key, Object value) {
        if (key.equals("javax.script.filename")) {
            // super.put("__file__", value);
            super.put(key, value);
        } else {
            // use a custom lifecycleTracker to handle dispose hook before polyglot context is closed
            // original lifecycleTracker is handling it when polyglot context is already closed
            if (key.equals("lifecycleTracker")) {
                value = lifecycleTracker;
            }
            if (scopeEnabled) {
                scriptExtensionModuleProvider.put(key, value);
            } else {
                super.put(key, value);
            }
        }
    }

    @Override
    public void lock() {
        lock.lock();
        logger.debug("Lock acquired.");
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        lock.lockInterruptibly();
    }

    @Override
    public boolean tryLock() {
        boolean acquired = lock.tryLock();
        if (acquired) {
            logger.debug("Lock acquired.");
        } else {
            logger.debug("Lock not acquired.");
        }
        return acquired;
    }

    @Override
    public boolean tryLock(long l, TimeUnit timeUnit) throws InterruptedException {
        boolean acquired = lock.tryLock(l, timeUnit);
        if (acquired) {
            logger.debug("Lock acquired.");
        } else {
            logger.debug("Lock not acquired.");
        }
        return acquired;
    }

    @Override
    public void unlock() {
        lock.unlock();
        logger.debug("Lock released.");
    }

    @Override
    public void close() throws Exception {
        this.lifecycleTracker.dispose();
        super.close();
    }

    @Override
    public Condition newCondition() {
        return lock.newCondition();
    }

    private void setScriptLogger() {
        Logger scriptLogger = initScriptLogger();
        scriptOutputStream.setLogger(scriptLogger);
        scriptErrorStream.setLogger(scriptLogger);
    }

    private ScriptContext getScriptContext() {
        ScriptContext ctx = delegate.getContext();
        if (ctx == null) {
            throw new IllegalStateException("Failed to retrieve script context");
        }
        return ctx;
    }

    private String stringifyThrowable(Throwable throwable) {
        String message = throwable.getMessage();
        StackTraceElement[] stackTraceElements = throwable.getStackTrace();
        String stackTrace = Arrays.stream(stackTraceElements).limit(STACK_TRACE_LENGTH)
                .map(t -> "        at " + t.toString()).collect(Collectors.joining(System.lineSeparator()))
                + System.lineSeparator() + "        ... " + stackTraceElements.length + " more";
        return (message != null) ? message + System.lineSeparator() + stackTrace : stackTrace;
    }

    /**
     * Initializes the logger.
     * This cannot be done on script engine creation because the context variables are not yet initialized.
     * Therefore, the logger needs to be initialized on the first use after script engine creation.
     */
    private Logger initScriptLogger() {
        ScriptContext ctx = getScriptContext();
        Object fileName = ctx.getAttribute("javax.script.filename");
        Object ruleUID = ctx.getAttribute("ruleUID");
        Object ohEngineIdentifier = ctx.getAttribute("oh.engine-identifier");

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

        return LoggerFactory.getLogger("org.openhab.automation.pythonscripting." + identifier);
    }

    private static Set<String> transformGraalWrapperSet(Value value) {
        // Value raw_value = value.invokeMember("getWrappedSetValues");
        Set<String> set = new HashSet<String>();
        for (int i = 0; i < value.getArraySize(); ++i) {
            Value element = value.getArrayElement(i);
            set.add(element.asString());
        }
        return set;
    }

    private static class LogOutputStream extends OutputStream {
        private static final int DEFAULT_BUFFER_LENGTH = 2048;
        private static final String LINE_SEPERATOR = System.getProperty("line.separator");
        private static final int LINE_SEPERATOR_SIZE = LINE_SEPERATOR.length();

        private Logger logger;
        private Level level;

        private int bufLength;
        private byte[] buf;
        private int count;

        public LogOutputStream(Logger logger, Level level) {
            this.logger = logger;
            this.level = level;

            bufLength = DEFAULT_BUFFER_LENGTH;
            buf = new byte[DEFAULT_BUFFER_LENGTH];
            count = 0;
        }

        public void setLogger(Logger logger) {
            this.logger = logger;
        }

        @Override
        public void write(int b) {
            // don't log nulls
            if (b == 0) {
                return;
            }

            if (count == bufLength) {
                growBuffer();
            }

            buf[count] = (byte) b;
            count++;
        }

        @Override
        public void flush() {
            if (count == 0) {
                return;
            }

            // don't print out blank lines;
            if (count == LINE_SEPERATOR_SIZE) {
                if (((char) buf[0]) == LINE_SEPERATOR.charAt(0)
                        && ((count == 1) || ((count == 2) && ((char) buf[1]) == LINE_SEPERATOR.charAt(1)))) {
                    reset();
                    return;
                }
            } else if (count > LINE_SEPERATOR_SIZE) {
                // remove linebreaks at the end
                if (((char) buf[count - 1]) == LINE_SEPERATOR.charAt(LINE_SEPERATOR_SIZE - 1)
                        && ((LINE_SEPERATOR_SIZE == 1) || ((LINE_SEPERATOR_SIZE == 2)
                                && ((char) buf[count - 1]) == LINE_SEPERATOR.charAt(LINE_SEPERATOR_SIZE - 2)))) {
                    count -= LINE_SEPERATOR_SIZE;
                }
            }

            final byte[] line = new byte[count];
            System.arraycopy(buf, 0, line, 0, count);
            logger.atLevel(level).log(new String(line));
            reset();
        }

        private void growBuffer() {
            final int newBufLength = bufLength + DEFAULT_BUFFER_LENGTH;
            final byte[] newBuf = new byte[newBufLength];
            System.arraycopy(buf, 0, newBuf, 0, bufLength);
            buf = newBuf;
            bufLength = newBufLength;
        }

        private void reset() {
            // don't shrink buffer. assuming that if it grew that it will likely grow similarly again
            count = 0;
        }
    }

    public static class LifecycleTracker {
        List<Function<Object[], Object>> disposables = new ArrayList<>();

        public void addDisposeHook(Function<Object[], Object> disposable) {
            disposables.add(disposable);
        }

        void dispose() {
            for (Function<Object[], Object> disposable : disposables) {
                disposable.apply(null);
            }
        }
    }
}
