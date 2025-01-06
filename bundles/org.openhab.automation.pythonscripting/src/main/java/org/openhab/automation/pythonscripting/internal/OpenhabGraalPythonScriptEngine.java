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
package org.openhab.automation.pythonscripting.internal;

import static org.openhab.core.automation.module.script.ScriptEngineFactory.CONTEXT_KEY_ENGINE_IDENTIFIER;
import static org.openhab.core.automation.module.script.ScriptEngineFactory.CONTEXT_KEY_EXTENSION_ACCESSOR;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.script.ScriptContext;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotAccess;
import org.openhab.automation.pythonscripting.internal.generic.GenericScriptServiceUtil;
import org.openhab.automation.pythonscripting.internal.generic.RuntimeFeatures;
import org.openhab.automation.pythonscripting.internal.graal.GraalPythonScriptEngine;
import org.openhab.automation.pythonscripting.internal.scriptengine.InvocationInterceptingScriptEngineWithInvocableAndCompilableAndAutoCloseable;
import org.openhab.core.OpenHAB;
import org.openhab.core.automation.module.script.ScriptExtensionAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GraalPython ScriptEngine implementation
 *
 * @author Jeff James - initial contribution
 */
public class OpenhabGraalPythonScriptEngine
        extends InvocationInterceptingScriptEngineWithInvocableAndCompilableAndAutoCloseable<GraalPythonScriptEngine>
        implements Lock {

    private static final String PYTHON_OPTION_EXECUTABLE = "python.Executable";
    private static final String PYTHON_OPTION_PYTHONHOME = "python.PythonHome";
    private static final String PYTHON_OPTION_PYTHONPATH = "python.PythonPath";
    private static final String PYTHON_OPTION_INPUTFILEPATH = "python.InputFilePath";
    private static final String PYTHON_OPTION_EMULATEJYTHON = "python.EmulateJython";
    private static final String PYTHON_OPTION_POSIXMODULEBACKEND = "python.PosixModuleBackend";
    private static final String PYTHON_OPTION_DONTWRITEBYTECODEFLAG = "python.DontWriteBytecodeFlag";
    private static final String PYTHON_OPTION_FORCEIMPORTSITE = "python.ForceImportSite";
    private static final String PYTHON_OPTION_CHECKHASHPYCSMODE = "python.CheckHashPycsMode";

    private static final Path PYTHON_DEFAULT_PATH = Paths.get(OpenHAB.getConfigFolder(), "automation", "python");
    /*
     * private static final Source GLOBAL_SOURCE;
     * static {
     * try {
     * GLOBAL_SOURCE = Source.newBuilder("python", getFileAsReader("node_modules/@jsscripting-globals.js"),
     * "@jsscripting-globals.js").cached(true).build();
     * } catch (IOException e) {
     * throw new IllegalStateException("Failed to load @jsscripting-globals.js", e);
     * }
     * }
     *
     * private static final Source OPENHAB_JS_SOURCE;
     * static {
     * try {
     * OPENHAB_JS_SOURCE = Source
     * .newBuilder("js", getFileAsReader("node_modules/@openhab-globals.js"), "@openhab-globals.js")
     * .cached(true).build();
     * } catch (IOException e) {
     * throw new IllegalStateException("Failed to load @openhab-globals.js", e);
     * }
     * }
     */
    // private static final String OPENHAB_JS_INJECTION_CODE = "Object.assign(this, require('openhab'));";

    /** Final CommonJS search path for our library */
    // private static final Path NODE_DIR = Paths.get("node_modules");

    /** Shared Polyglot {@link Engine} across all instances of {@link OpenhabGraalPythonScriptEngine} */
    private static final Engine ENGINE = Engine.newBuilder().allowExperimentalOptions(true)
            .option("engine.WarnInterpreterOnly", "false").build();

    /** Provides unlimited host access as well as custom translations from Python to Java Objects */
    private static final HostAccess HOST_ACCESS = HostAccess.newBuilder(HostAccess.ALL) //
            /*
             * // Translate openhab-python Item to org.openhab.core.items.Item
             * .targetTypeMapping(Value.class, Item.class, v -> v.hasMember("rawItem"),
             * v -> v.getMember("rawItem").as(Item.class), HostAccess.TargetMappingPrecedence.LOW)
             */
            .build();
    /*
     * // Translate JS-Joda ZonedDateTime to java.time.ZonedDateTime
     * .targetTypeMapping(Value.class, ZonedDateTime.class, v -> v.hasMember("withFixedOffsetZone"),
     * v -> ZonedDateTime.parse(v.invokeMember("withFixedOffsetZone").invokeMember("toString").asString()),
     * HostAccess.TargetMappingPrecedence.LOW)
     *
     * // Translate JS-Joda Duration to java.time.Duration
     * .targetTypeMapping(Value.class, Duration.class,
     * // picking two members to check as Duration has many common function names
     * v -> v.hasMember("minusDuration") && v.hasMember("toNanos"),
     * v -> Duration.ofNanos(v.invokeMember("toNanos").asLong()), HostAccess.TargetMappingPrecedence.LOW)
     *
     * // Translate JS-Joda Instant to java.time.Instant
     * .targetTypeMapping(Value.class, Instant.class,
     * // picking two members to check as Instant has many common function names
     * v -> v.hasMember("toEpochMilli") && v.hasMember("epochSecond"),
     * v -> Instant.ofEpochMilli(v.invokeMember("toEpochMilli").asLong()),
     * HostAccess.TargetMappingPrecedence.LOW)
     *
     * // Translate openhab-js Quantity to org.openhab.core.library.types.QuantityType
     * .targetTypeMapping(Value.class, QuantityType.class, v -> v.hasMember("rawQtyType"),
     * v -> v.getMember("rawQtyType").as(QuantityType.class), HostAccess.TargetMappingPrecedence.LOW)
     * .build();
     */

    private final Logger logger = LoggerFactory.getLogger(OpenhabGraalPythonScriptEngine.class);

    /** {@link Lock} synchronization of multi-thread access */
    private final Lock lock = new ReentrantLock();
    private final RuntimeFeatures runtimeFeatures;

    // these fields start as null because they are populated on first use
    // private @Nullable Consumer<String> scriptDependencyListener;
    private String engineIdentifier; // this field is very helpful for debugging, please do not remove it

    private boolean initialized = false;
    private final boolean injectionEnabled;
    private final boolean injectionCachingEnabled;
    private final boolean jythonEmulation;

    /**
     * Creates an implementation of ScriptEngine {@code (& Invocable)}, wrapping the contained engine,
     * that tracks the script lifecycle and provides hooks for scripts to do so too.
     */
    public OpenhabGraalPythonScriptEngine(boolean injectionEnabled, boolean injectionCachingEnabled,
            boolean jythonEmulation, GenericScriptServiceUtil genericScriptServiceUtil) {
        // JSDependencyTracker jsDependencyTracker) {
        super(null); // delegate depends on fields not yet initialised, so we cannot set it immediately
        this.injectionEnabled = injectionEnabled;
        this.injectionCachingEnabled = injectionCachingEnabled;
        this.jythonEmulation = jythonEmulation;
        this.runtimeFeatures = genericScriptServiceUtil.getRuntimeFeatures(lock);

        Context.Builder contextConfig = Context.newBuilder("python") //
                .allowExperimentalOptions(true) //
                .allowAllAccess(true) //
                .allowPolyglotAccess(PolyglotAccess.ALL)
                // TODO: .allowHostAccess(HOST_ACCESS)
                // TODO: .hostClassLoader(getClass().getClassLoader())
                // .fileSystem(...)
                // allow exporting Python values to polyglot bindings and accessing Java
                // from Python
                // choose the backend for the POSIX module
                .option(PYTHON_OPTION_POSIXMODULEBACKEND, "java") //
                // equivalent to the Python -B flag
                .option(PYTHON_OPTION_DONTWRITEBYTECODEFLAG, "true") //
                // Force to automatically import site.py module, to make Python packages available
                .option(PYTHON_OPTION_FORCEIMPORTSITE, "true") //
                // causes the interpreter to always assume hash-based pycs are valid
                .option(PYTHON_OPTION_CHECKHASHPYCSMODE, "never") //
                // The sys.executable path, a virtual path that is used by the interpreter
                // to discover packages
                .option(PYTHON_OPTION_EXECUTABLE, PYTHON_DEFAULT_PATH.resolve("bin").resolve("python").toString())
                // Set the python home to be read from the embedded resources
                // .option(PYTHON_OPTION_PYTHONHOME, PYTHON_DEFAULT_PATH.toString()) //
                // Set python path to point to sources stored in
                .option(PYTHON_OPTION_PYTHONPATH, PYTHON_DEFAULT_PATH.resolve("lib").toString())
                // pass the path to be executed
                .option(PYTHON_OPTION_INPUTFILEPATH, PYTHON_DEFAULT_PATH.toString()) //
                .option(PYTHON_OPTION_EMULATEJYTHON, String.valueOf(jythonEmulation));

        delegate = GraalPythonScriptEngine.create(ENGINE, contextConfig);
    }

    @Override
    protected void beforeInvocation() {
        super.beforeInvocation();

        logger.debug("Initializing GraalPython script engine...");

        lock.lock();
        logger.debug("Lock acquired before invocation.");

        if (initialized) {
            return;
        }

        ScriptContext ctx = delegate.getContext();
        if (ctx == null) {
            throw new IllegalStateException("Failed to retrieve script context");
        }

        // these are added post-construction, so we need to fetch them late
        String localEngineIdentifier = (String) ctx.getAttribute(CONTEXT_KEY_ENGINE_IDENTIFIER);
        if (localEngineIdentifier == null) {
            throw new IllegalStateException("Failed to retrieve engine identifier from engine bindings");
        }
        this.engineIdentifier = localEngineIdentifier;

        ScriptExtensionAccessor scriptExtensionAccessor = (ScriptExtensionAccessor) ctx
                .getAttribute(CONTEXT_KEY_EXTENSION_ACCESSOR);
        if (scriptExtensionAccessor == null) {
            throw new IllegalStateException("Failed to retrieve script extension accessor from engine bindings");
        }

        /*
         * Consumer<String> localScriptDependencyListener = (Consumer<String>) ctx
         * .getAttribute(CONTEXT_KEY_DEPENDENCY_LISTENER);
         * if (localScriptDependencyListener == null) {
         * logger.warn(
         * "Failed to retrieve script script dependency listener from engine bindings. Script dependency tracking will be disabled."
         * );
         * }
         */
        // scriptDependencyListener = localScriptDependencyListener;

        // ScriptExtensionModuleProvider scriptExtensionModuleProvider = new ScriptExtensionModuleProvider(
        // scriptExtensionAccessor, lock);

        // Wrap the "require" function to also allow loading modules from the ScriptExtensionModuleProvider
        /*
         * Function<Function<Object[], Object>, Function<String, Object>> wrapRequireFn = originalRequireFn ->
         * moduleName -> scriptExtensionModuleProvider
         * .locatorFor(delegate.getPolyglotContext(), localEngineIdentifier).locateModule(moduleName)
         * .map(m -> (Object) m).orElseGet(() -> originalRequireFn.apply(new Object[] { moduleName }));
         */
        // delegate.getBindings(ScriptContext.ENGINE_SCOPE).put(REQUIRE_WRAPPER_NAME, wrapRequireFn);
        // delegate.put("require", wrapRequireFn.apply((Function<Object[], Object>) delegate.get("require")));

        // Injections into the runtime
        runtimeFeatures.getFeatures().forEach((key, obj) -> {
            logger.debug("Injecting {} into the runtime...", key);
            delegate.put(key, obj);
        });

        initialized = true;

        /*
         * try {
         * logger.debug("Evaluating cached global script...");
         * delegate.getPolyglotContext().eval(GLOBAL_SOURCE);
         * if (this.injectionEnabled) {
         * if (this.injectionCachingEnabled) {
         * logger.debug("Evaluating cached openhab-js injection...");
         * delegate.getPolyglotContext().eval(OPENHAB_JS_SOURCE);
         * } else {
         * logger.debug("Evaluating openhab-js injection from the file system...");
         * eval(OPENHAB_JS_INJECTION_CODE);
         * }
         * }
         * logger.debug("Successfully initialized GraalJS script engine.");
         * } catch (ScriptException e) {
         * logger.error("Could not inject global script", e);
         * }
         */
    }

    @Override
    protected Object afterInvocation(Object obj) {
        lock.unlock();
        logger.debug("Lock released after invocation.");
        return super.afterInvocation(obj);
    }

    @Override
    protected Exception afterThrowsInvocation(Exception e) {
        lock.unlock();
        return super.afterThrowsInvocation(e);
    }

    @Override
    public void close() {
        runtimeFeatures.close();
    }

    /**
     * Converts a root node path to a class resource path for loading local modules
     * Ex: C:\node_modules\foo.js -> /node_modules/foo.js
     *
     * @param path a root path, e.g. C:\node_modules\foo.js
     * @return the class resource path for loading local modules
     */
    private static String nodeFileToResource(Path path) {
        return "/" + path.subpath(0, path.getNameCount()).toString().replace('\\', '/');
    }

    /**
     * @param fileName filename relative to the resources folder
     * @return file as {@link InputStreamReader}
     */
    private static Reader getFileAsReader(String fileName) throws IOException {
        InputStream ioStream = OpenhabGraalPythonScriptEngine.class.getClassLoader().getResourceAsStream(fileName);

        if (ioStream == null) {
            throw new IOException(fileName + " not found");
        }

        return new InputStreamReader(ioStream);
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
        return lock.tryLock();
    }

    @Override
    public boolean tryLock(long l, TimeUnit timeUnit) throws InterruptedException {
        return lock.tryLock(l, timeUnit);
    }

    @Override
    public void unlock() {
        lock.unlock();
        logger.debug("Lock released.");
    }

    @Override
    public Condition newCondition() {
        return lock.newCondition();
    }
}
