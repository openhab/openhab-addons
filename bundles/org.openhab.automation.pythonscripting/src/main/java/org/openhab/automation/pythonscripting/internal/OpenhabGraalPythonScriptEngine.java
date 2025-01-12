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

import static org.openhab.core.automation.module.script.ScriptEngineFactory.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.script.ScriptContext;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotAccess;
import org.graalvm.polyglot.Value;
import org.openhab.automation.pythonscripting.internal.graal.GraalPythonScriptEngine;
import org.openhab.automation.pythonscripting.internal.scriptengine.InvocationInterceptingScriptEngineWithInvocableAndCompilableAndAutoCloseable;
import org.openhab.core.OpenHAB;
import org.openhab.core.automation.module.script.ScriptExtensionAccessor;
import org.openhab.core.items.Item;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GraalPython ScriptEngine implementation
 *
 * @author Holger Hees - initial contribution
 * @author Jeff James - initial contribution
 */
public class OpenhabGraalPythonScriptEngine
        extends InvocationInterceptingScriptEngineWithInvocableAndCompilableAndAutoCloseable<GraalPythonScriptEngine>
        implements Lock {
    private final Logger logger = LoggerFactory.getLogger(OpenhabGraalPythonScriptEngine.class);

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

    public static final Path PYTHON_DEFAULT_PATH = Paths.get(OpenHAB.getConfigFolder(), "automation", "python");

    private static final String PYTHON_OPTION_CACHEDIR = "python.PyCachePrefix";
    private static final String PYTHON_CACHEDIR_PATH = Paths.get(OpenHAB.getUserDataFolder(), "cache",
            OpenhabGraalPythonScriptEngine.class.getPackageName(), "cachedir").toString();

    /** Shared Polyglot {@link Engine} across all instances of {@link OpenhabGraalPythonScriptEngine} */
    private static final Engine ENGINE = Engine.newBuilder().allowExperimentalOptions(true)
            .option("engine.WarnInterpreterOnly", "false").build();

    /** Provides unlimited host access as well as custom translations from Python to Java Objects */
    private static final HostAccess HOST_ACCESS = HostAccess.newBuilder(HostAccess.ALL)
            // Translate python datetime to java.time.ZonedDateTime
            .targetTypeMapping(Value.class, ZonedDateTime.class, v -> v.hasMember("ctime") && v.hasMember("isoformat"),
                    v -> ZonedDateTime.parse(v.invokeMember("isoformat").asString()),
                    HostAccess.TargetMappingPrecedence.LOW)

            // Translate python timedelta to java.time.Duration
            .targetTypeMapping(Value.class, Duration.class,
                    // picking two members to check as Duration has many common function names
                    v -> v.hasMember("total_seconds") && v.hasMember("total_seconds"),
                    v -> Duration.ofNanos(v.invokeMember("total_seconds").asLong() * 10000000),
                    HostAccess.TargetMappingPrecedence.LOW)

            // .targetTypeMapping(Value.class, Instant.class,
            // // picking two members to check as Instant has many common function names
            // v -> v.hasMember("toEpochMilli") && v.hasMember("epochSecond"),
            // v -> Instant.ofEpochMilli(v.invokeMember("toEpochMilli").asLong()),
            // HostAccess.TargetMappingPrecedence.LOW)

            // Translate python item to org.openhab.core.items.Item
            .targetTypeMapping(Value.class, Item.class, v -> v.hasMember("raw_item"),
                    v -> v.getMember("raw_item").as(Item.class), HostAccess.TargetMappingPrecedence.LOW)

            // Translate python quantity to org.openhab.core.library.types.QuantityType
            // .targetTypeMapping(Value.class, QuantityType.class, v -> v.hasMember("rawQtyType"),
            // v -> v.getMember("rawQtyType").as(QuantityType.class), HostAccess.TargetMappingPrecedence.LOW)

            // Translate python GraalWrapperSet to java.util.Set
            .targetTypeMapping(Value.class, Set.class, v -> v.hasMember("isSetType"),
                    OpenhabGraalPythonScriptEngine::transformGraalWrapperSet, HostAccess.TargetMappingPrecedence.LOW)

            // Translate python list to java.util.Collection
            .targetTypeMapping(Value.class, Collection.class, (v) -> v.hasArrayElements(),
                    (v) -> v.as(Collection.class), HostAccess.TargetMappingPrecedence.LOW)

            .build();

    /** {@link Lock} synchronization of multi-thread access */
    private final Lock lock = new ReentrantLock();

    // these fields start as null because they are populated on first use
    // private @Nullable Consumer<String> scriptDependencyListener;
    private String engineIdentifier; // this field is very helpful for debugging, please do not remove it

    private boolean initialized = false;

    /**
     * Creates an implementation of ScriptEngine {@code (& Invocable)}, wrapping the contained engine,
     * that tracks the script lifecycle and provides hooks for scripts to do so too.
     *
     * @param jythonEmulation
     */
    public OpenhabGraalPythonScriptEngine(boolean cachingEnabled, boolean jythonEmulation) {
        // JSDependencyTracker jsDependencyTracker) {
        super(null); // delegate depends on fields not yet initialised, so we cannot set it immediately

        Context.Builder contextConfig = Context.newBuilder("python") //
                .allowHostAccess(HOST_ACCESS) //
                // .allowHostClassLoading(true) //
                .allowAllAccess(true) //
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
                .option(PYTHON_OPTION_EXECUTABLE, PYTHON_DEFAULT_PATH.resolve("bin").resolve("python").toString())
                // Set the python home to be read from the embedded resources
                // .option(PYTHON_OPTION_PYTHONHOME, PYTHON_DEFAULT_PATH.toString()) //
                // Set python path to point to sources stored in
                .option(PYTHON_OPTION_PYTHONPATH, PYTHON_DEFAULT_PATH.resolve("lib").toString())
                // pass the path to be executed
                .option(PYTHON_OPTION_INPUTFILEPATH, PYTHON_DEFAULT_PATH.toString()) //
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

        Bundle script_bundle = FrameworkUtil
                .getBundle(org.openhab.core.automation.module.script.ScriptEngineManager.class);
        delegate.getPolyglotContext().getBindings("python").putMember("scriptBundle", script_bundle);
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

        initialized = true;
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

    private static Set<String> transformGraalWrapperSet(Value value) {
        // Value raw_value = value.invokeMember("getWrappedSetValues");
        Set<String> set = new HashSet<String>();
        for (int i = 0; i < value.getArraySize(); ++i) {
            Value element = value.getArrayElement(i);
            set.add(element.asString());
        }
        return set;
    }
}
