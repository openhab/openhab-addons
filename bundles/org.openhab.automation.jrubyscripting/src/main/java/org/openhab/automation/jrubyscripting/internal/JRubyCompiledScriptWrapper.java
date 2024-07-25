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
package org.openhab.automation.jrubyscripting.internal;

import java.util.Objects;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This is a wrapper for {@link CompiledScript}.
 * 
 * The purpose of this class is to intercept the call to eval and save the context into
 * a global variable for use in the helper library.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class JRubyCompiledScriptWrapper extends CompiledScript {

    private final CompiledScript compiledScript;

    private static final String CONTEXT_VAR_NAME = "ctx";
    private static final String GLOBAL_VAR_NAME = "$" + CONTEXT_VAR_NAME;

    JRubyCompiledScriptWrapper(CompiledScript compiledScript) {
        this.compiledScript = Objects.requireNonNull(compiledScript);
    }

    @Override
    public Object eval(@Nullable ScriptContext context) throws ScriptException {
        Object ctx = Objects.requireNonNull(context).getBindings(ScriptContext.ENGINE_SCOPE).get(CONTEXT_VAR_NAME);
        if (ctx == null) {
            return compiledScript.eval(context);
        }

        context.setAttribute(GLOBAL_VAR_NAME, ctx, ScriptContext.ENGINE_SCOPE);
        try {
            return compiledScript.eval(context);
        } finally {
            context.removeAttribute(GLOBAL_VAR_NAME, ScriptContext.ENGINE_SCOPE);
        }
    }

    @Override
    public ScriptEngine getEngine() {
        return compiledScript.getEngine();
    }
}
