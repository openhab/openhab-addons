/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import javax.script.ScriptEngine;
import javax.script.ScriptException;

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

    private static final Logger STACK_LOGGER = LoggerFactory
            .getLogger("org.openhab.automation.script.javascript.stack");

    public DebuggingGraalScriptEngine(T delegate) {
        super(delegate);
    }

    @Override
    public ScriptException afterThrowsInvocation(ScriptException se) {
        Throwable cause = se.getCause();
        if (cause instanceof PolyglotException) {
            STACK_LOGGER.error("Failed to execute script:", cause);
        }
        return se;
    }
}
