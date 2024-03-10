/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.automation.jsscripting.internal;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.PolyglotException;
import org.openhab.automation.jsscripting.internal.scriptengine.InvocationInterceptingScriptEngineWithInvocableAndAutoCloseable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps ScriptEngines provided by Graal to provide error messages and stack traces for scripts.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
class DebuggingGraalScriptEngine<T extends ScriptEngine & Invocable & AutoCloseable>
        extends InvocationInterceptingScriptEngineWithInvocableAndAutoCloseable<T> {

    private static final String SCRIPT_TRANSFORMATION_ENGINE_IDENTIFIER = "openhab-transformation-script-";

    private @Nullable Logger logger;

    public DebuggingGraalScriptEngine(T delegate) {
        super(delegate);
    }

    @Override
    protected void beforeInvocation() {
        super.beforeInvocation();
        if (logger == null) {
            initializeLogger();
        }
    }

    @Override
    public Exception afterThrowsInvocation(Exception e) {
        Throwable cause = e.getCause();
        if (cause instanceof IllegalArgumentException) {
            logger.error("Failed to execute script:", e);
        }
        if (cause instanceof PolyglotException) {
            logger.error("Failed to execute script:", cause);
        }
        return e;
    }

    /**
     * Initializes the logger.
     * This cannot be done on script engine creation because the context variables are not yet initialized.
     * Therefore, the logger needs to be initialized on the first use after script engine creation.
     */
    private void initializeLogger() {
        ScriptContext ctx = delegate.getContext();
        Object fileName = ctx.getAttribute("javax.script.filename");
        Object ruleUID = ctx.getAttribute("ruleUID");
        Object ohEngineIdentifier = ctx.getAttribute("oh.engine-identifier");

        String identifier = "stack";
        if (fileName != null) {
            identifier = fileName.toString().replaceAll("^.*[/\\\\]", "");
        } else if (ruleUID != null) {
            identifier = ruleUID.toString();
        } else if (ohEngineIdentifier != null) {
            if (ohEngineIdentifier.toString().startsWith(SCRIPT_TRANSFORMATION_ENGINE_IDENTIFIER)) {
                identifier = ohEngineIdentifier.toString().replaceAll(SCRIPT_TRANSFORMATION_ENGINE_IDENTIFIER,
                        "transformation.");
            }
        }

        logger = LoggerFactory.getLogger("org.openhab.automation.script.javascript." + identifier);
    }
}
