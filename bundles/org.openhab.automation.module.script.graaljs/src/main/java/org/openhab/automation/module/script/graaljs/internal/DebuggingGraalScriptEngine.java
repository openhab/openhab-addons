/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.automation.module.script.graaljs.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.PolyglotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Optional;

/**
 * Wraps ScriptEngines provided by Graal to provide error messages and stack
 * traces for scripts.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@NonNullByDefault
class DebuggingGraalScriptEngine {
    private static final Logger logger = LoggerFactory.getLogger(DebuggingGraalScriptEngine.class);
    private static final Logger stackLogger = LoggerFactory.getLogger("org.openhab.automation.script.javascript.stack");
    private ScriptEngine engine;

    private DebuggingGraalScriptEngine(ScriptEngine engine) {
        this.engine = engine;
    }

    @Nullable
    private static Method EVAL_WITH_READER_METHOD;

    static {
        try {
            EVAL_WITH_READER_METHOD = ScriptEngine.class.getDeclaredMethod("eval", Reader.class);
        } catch (NoSuchMethodException e) {
            logger.warn("Failed to load ScriptEngine.eval(Reader) method: {}", e.getMessage());
        }
    }

    /**
     * Creates an implementation of ScriptEngine (& Invocable), wrapping the
     * contained engine, that logs PolyglotExceptions that are thrown from any
     * 'eval' methods.
     *
     * @return a ScriptEngine which logs script exceptions
     */
    static ScriptEngine create(ScriptEngine engine) {
        return new DebuggingGraalScriptEngine(engine).createProxy();
    }

    private ScriptEngine createProxy() {
        return (ScriptEngine) Proxy.newProxyInstance(ScriptEngine.class.getClassLoader(),
                new Class<?>[] { ScriptEngine.class, Invocable.class }, (proxy, method, args) -> {
                    try {
                        if (method.getName().equals("eval")) {
                            return evalInvocation(method, args);
                        } else {
                            return method.invoke(engine, args);
                        }
                    } catch (InvocationTargetException ite) {
                        throw ite.getTargetException();
                    }
                });
    }

    /**
     * Logs error with JS stack trace if it's caused by a PolyglotException (e.g.
     * the script caused the error)
     */
    private Object evalInvocation(Method method, Object[] args)
            throws InvocationTargetException, IllegalAccessException {
        try {
            // if this is the eval(Reader) version, attempt inject the script name
            if (method.equals(EVAL_WITH_READER_METHOD)) {
                findPathForReader((Reader) args[0]).ifPresent(filename -> engine.getContext()
                        .setAttribute(ScriptEngine.FILENAME, filename, ScriptContext.ENGINE_SCOPE));
            }

            return method.invoke(engine, args);
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getTargetException().getCause();
            if (cause instanceof PolyglotException) {
                stackLogger.error("Failed to execute script:", cause);
            }
            throw ite;
        }
    }

    /**
     * Nasty hack to opportunistically extract the path from the passed Reader. Not
     * currently possible as insufficient information is passed. Not guaranteed to
     * work, fragile and depends on caller implementation. Ideally requires update
     * of
     */
    private Optional<String> findPathForReader(Reader reader) {
        try {
            Field f = Reader.class.getDeclaredField("lock");
            f.setAccessible(true);
            BufferedInputStream bif = (BufferedInputStream) f.get(reader);
            Field f2 = FilterInputStream.class.getDeclaredField("in");
            f2.setAccessible(true);
            FileInputStream fis = (FileInputStream) f2.get(f2.get(bif));
            Field f3 = FileInputStream.class.getDeclaredField("path");
            f3.setAccessible(true);
            String fullpath = (String) f3.get(fis);
            return Optional.of(new File(fullpath).getName());
        } catch (Exception e) {
            logger.warn("Failed to extract path for source file: {}", e.getMessage());
            return Optional.empty();
        }
    }

}
