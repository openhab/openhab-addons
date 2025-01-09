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
import org.openhab.automation.pythonscripting.internal.graal.GraalPythonScriptEngine;
import org.openhab.automation.pythonscripting.internal.scriptengine.InvocationInterceptingScriptEngineWithInvocableAndCompilableAndAutoCloseable;
import org.openhab.core.OpenHAB;
import org.openhab.core.automation.module.script.ScriptExtensionAccessor;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
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

    /** Shared Polyglot {@link Engine} across all instances of {@link OpenhabGraalPythonScriptEngine} */
    private static final Engine ENGINE = Engine.newBuilder().allowExperimentalOptions(true)
            .option("engine.WarnInterpreterOnly", "false").build();

    /** Provides unlimited host access as well as custom translations from Python to Java Objects */
    private static final HostAccess HOST_ACCESS = HostAccess.newBuilder(HostAccess.ALL) //
            .build();

    private final Bundle script_bundle;

    private final Logger logger = LoggerFactory.getLogger(OpenhabGraalPythonScriptEngine.class);

    /** {@link Lock} synchronization of multi-thread access */
    private final Lock lock = new ReentrantLock();

    // these fields start as null because they are populated on first use
    // private @Nullable Consumer<String> scriptDependencyListener;
    private String engineIdentifier; // this field is very helpful for debugging, please do not remove it

    private boolean initialized = false;
    private final boolean jythonEmulation;

    public String testString = "Test String";

    /**
     * Creates an implementation of ScriptEngine {@code (& Invocable)}, wrapping the contained engine,
     * that tracks the script lifecycle and provides hooks for scripts to do so too.
     */
    public OpenhabGraalPythonScriptEngine(boolean jythonEmulation) {
        // JSDependencyTracker jsDependencyTracker) {
        super(null); // delegate depends on fields not yet initialised, so we cannot set it immediately
        this.jythonEmulation = jythonEmulation;

        script_bundle = FrameworkUtil.getBundle(org.openhab.core.automation.module.script.ScriptEngineManager.class);

        Context.Builder contextConfig = Context.newBuilder("python") //
                // .allowHostAccess(HOST_ACCESS) //
                // .allowHostClassLoading(true) //
                .allowAllAccess(true) //
                .allowNativeAccess(true) //

                // .allowNativeAccess(true) //
                // .allowCreateProcess(true) //
                // .allowCreateThread(true) //
                // .allowExperimentalOptions(true) //
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

        delegate.getPolyglotContext().getBindings("python").putMember("scriptBundle", script_bundle);

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
