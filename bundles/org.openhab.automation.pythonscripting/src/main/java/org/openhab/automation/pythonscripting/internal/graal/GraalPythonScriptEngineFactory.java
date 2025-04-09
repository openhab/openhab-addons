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

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Language;

/**
 * @author Holger Hees - Initial contribution
 * @author Jeff James - Initial contribution
 */
public final class GraalPythonScriptEngineFactory implements ScriptEngineFactory {
    private WeakReference<Engine> defaultEngine;
    private final Engine userDefinedEngine;

    private static final String ENGINE_NAME = "Graal.py";
    private static final String NAME = "python3";

    private static final String[] EXTENSIONS = { "py" };

    public GraalPythonScriptEngineFactory() {
        super();
        this.userDefinedEngine = null;

        defaultEngine = new WeakReference<Engine>(createDefaultEngine());
    }

    public GraalPythonScriptEngineFactory(Engine engine) {
        this.defaultEngine = null;
        this.userDefinedEngine = engine;
    }

    private static Engine createDefaultEngine() {
        return Engine.newBuilder() //
                .allowExperimentalOptions(true) //
                .option("engine.WarnInterpreterOnly", "false") //
                .build();
    }

    /**
     * Returns the underlying polyglot engine.
     */
    public Engine getPolyglotEngine() {
        if (userDefinedEngine != null) {
            return userDefinedEngine;
        } else {
            Engine engine = defaultEngine == null ? null : defaultEngine.get();
            if (engine == null) {
                engine = createDefaultEngine();
                defaultEngine = new WeakReference<>(engine);
            }
            return engine;
        }
    }

    @Override
    public String getEngineName() {
        return ENGINE_NAME;
    }

    @Override
    public String getEngineVersion() {
        return getPolyglotEngine().getVersion();
    }

    @Override
    public List<String> getExtensions() {
        return List.of(EXTENSIONS);
    }

    @Override
    public List<String> getMimeTypes() {
        Language language = getPolyglotEngine().getLanguages().get(GraalPythonScriptEngine.LANGUAGE_ID);
        return List.copyOf(language.getMimeTypes());
    }

    @Override
    public List<String> getNames() {
        Language language = getPolyglotEngine().getLanguages().get(GraalPythonScriptEngine.LANGUAGE_ID);
        return List.of(language.getName(), GraalPythonScriptEngine.LANGUAGE_ID, language.getImplementationName());
    }

    @Override
    public String getLanguageName() {
        Language language = getPolyglotEngine().getLanguages().get(GraalPythonScriptEngine.LANGUAGE_ID);
        return language.getName();
    }

    @Override
    public String getLanguageVersion() {
        Language language = getPolyglotEngine().getLanguages().get(GraalPythonScriptEngine.LANGUAGE_ID);
        return language.getVersion();
    }

    @Override
    public Object getParameter(String key) {
        switch (key) {
            case ScriptEngine.NAME:
                return NAME;
            case ScriptEngine.ENGINE:
                return getEngineName();
            case ScriptEngine.ENGINE_VERSION:
                return getEngineVersion();
            case ScriptEngine.LANGUAGE:
                return getLanguageName();
            case ScriptEngine.LANGUAGE_VERSION:
                return getLanguageVersion();
            default:
                return null;
        }
    }

    @Override
    public GraalPythonScriptEngine getScriptEngine() {
        return new GraalPythonScriptEngine(this);
    }

    @Override
    public String getMethodCallSyntax(final String obj, final String method, final String... args) {
        Objects.requireNonNull(obj);
        Objects.requireNonNull(method);
        final StringBuilder sb = new StringBuilder().append(obj).append('.').append(method).append('(');
        final int len = args.length;

        if (len > 0) {
            Objects.requireNonNull(args[0]);
            sb.append(args[0]);
        }
        for (int i = 1; i < len; i++) {
            Objects.requireNonNull(args[i]);
            sb.append(',').append(args[i]);
        }
        sb.append(')');

        return sb.toString();
    }

    @Override
    public String getOutputStatement(final String toDisplay) {
        return "print(\"" + toDisplay + "\")";
    }

    @Override
    public String getProgram(final String... statements) {
        final StringBuilder sb = new StringBuilder();

        for (final String statement : statements) {
            Objects.requireNonNull(statement);
            sb.append(statement).append(';');
        }

        return sb.toString();
    }
}
