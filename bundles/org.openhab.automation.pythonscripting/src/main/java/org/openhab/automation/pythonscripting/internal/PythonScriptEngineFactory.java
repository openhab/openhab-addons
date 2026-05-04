/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Language;
import org.openhab.automation.pythonscripting.internal.fs.PythonDependencyTracker;
import org.openhab.automation.pythonscripting.internal.scriptengine.graal.GraalPythonScriptEngine;
import org.openhab.automation.pythonscripting.internal.scriptengine.graal.GraalPythonScriptEngine.ScriptEngineProvider;
import org.openhab.core.automation.module.script.ScriptDependencyTracker;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.i18n.TimeZoneProvider;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an implementation of {@link ScriptEngineFactory} for Python.
 *
 * @author Holger Hees - Initial contribution
 * @author Jeff James - Initial contribution
 */
@Component(service = { ScriptEngineFactory.class, PythonScriptEngineFactory.class }, //
        configurationPid = "org.openhab.automation.pythonscripting", //
        property = Constants.SERVICE_PID + "=org.openhab.automation.pythonscripting")
@ConfigurableService(category = "automation", label = "Python Scripting", description_uri = PythonScriptEngineFactory.CONFIG_DESCRIPTION_URI)
@NonNullByDefault
public class PythonScriptEngineFactory implements ScriptEngineFactory, ScriptEngineProvider {
    private final Logger logger = LoggerFactory.getLogger(PythonScriptEngineFactory.class);

    public static final String CONFIG_DESCRIPTION_URI = "automation:pythonscripting";
    public static final String SCRIPT_TYPE = "application/x-python3";

    private final List<String> scriptTypes = Arrays.asList("py", SCRIPT_TYPE);
    private final PythonDependencyTracker pythonDependencyTracker;
    private final PythonScriptEngineConfiguration configuration;

    private static final String PYTHON_OPTION_ENGINE_WARNINTERPRETERONLY = "engine.WarnInterpreterOnly";

    /**
     * Shared Polyglot {@link Engine} instance to be used by all instances of {@link PythonScriptEngine}.
     */
    private final Engine engine;

    @Activate
    public PythonScriptEngineFactory(final @Reference PythonDependencyTracker pythonDependencyTracker,
            final @Reference TimeZoneProvider timeZoneProvider, Map<String, Object> config) {
        logger.debug("Loading PythonScriptEngineFactory");

        String defaultTimezone = ZoneId.systemDefault().getId();
        String providerTimezone = timeZoneProvider.getTimeZone().getId();
        if (!defaultTimezone.equals(providerTimezone)) {
            logger.warn(
                    "User timezone '{}' is different than openhab regional timezone '{}'. Python Scripting is running with timezone '{}'.",
                    defaultTimezone, providerTimezone, defaultTimezone);
        }

        this.pythonDependencyTracker = pythonDependencyTracker;
        this.configuration = new PythonScriptEngineConfiguration(config);
        this.configuration.init(this);

        Engine.Builder engineBuilder = createEngineBuilder();
        if (configuration.isDebuggerEnabled()) {
            engineBuilder //
                    .option("inspect", "0.0.0.0:" + configuration.getDebuggerPort()) //
                    .option("inspect.Suspend", "false") // Don't pause at startup waiting for debugger to attach
                    .option("inspect.WaitAttached", "false") // Don't block code execution waiting for debugger to
                                                             // attach
                    .option("inspect.Secure", "false"); // Disable TLS

            Engine engine;
            try {
                engine = engineBuilder.build();
                logger.info("Debugger support is enabled for Python Scripting.");
            } catch (RuntimeException e) {
                logger.error(
                        "Failed to initialize Graal Python engine with debugger support. Continuing without debugger support.",
                        e);
                engine = createEngineBuilder().build();
            }
            this.engine = engine;
        } else {
            this.engine = createEngineBuilder().build();
        }

        if (getLanguage() == null) {
            logger.error(
                    "Graal Python language not initialized. Restart openHAB to initialize available Graal languages properly.");
        }
    }

    private Engine.Builder createEngineBuilder() {
        return Engine.newBuilder().allowExperimentalOptions(true) //
                .option(PYTHON_OPTION_ENGINE_WARNINTERPRETERONLY, Boolean.toString(false));
    }

    @Deactivate
    public void cleanup() {
        logger.debug("Unloading PythonScriptEngineFactory");
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        this.configuration.modified(config, this);
    }

    @Override
    public List<String> getScriptTypes() {
        return scriptTypes;
    }

    @Override
    public void scopeValues(ScriptEngine scriptEngine, Map<String, Object> scopeValues) {
        for (Entry<String, Object> entry : scopeValues.entrySet()) {
            scriptEngine.put(entry.getKey(), entry.getValue());
        }
    }

    public @Nullable ScriptEngine createScriptEngine(String scriptType, String scriptIdentifier) {
        ScriptEngine engine = createScriptEngine(scriptType);
        if (engine != null) {
            engine.getContext().setAttribute(ScriptEngineFactory.CONTEXT_KEY_ENGINE_IDENTIFIER, scriptIdentifier,
                    ScriptContext.ENGINE_SCOPE);
        }
        return engine;
    }

    @Override
    public @Nullable ScriptEngine createScriptEngine(String scriptType) {
        if (!scriptTypes.contains(scriptType)) {
            return null;
        }
        return createScriptEngine();
    }

    @Override
    public @Nullable ScriptEngine createScriptEngine() {
        if (getLanguage() == null) {
            return null;
        }
        return new PythonScriptEngine(configuration, engine, this);
    }

    @Override
    public @Nullable ScriptDependencyTracker getDependencyTracker() {
        return pythonDependencyTracker;
    }

    public PythonScriptEngineConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * Gets the Graal language of {@link PythonScriptEngine}.
     *
     * @return the Graal language of {@link PythonScriptEngine} or {@code null} if not available
     */
    public @Nullable Language getLanguage() {
        return engine.getLanguages().get(GraalPythonScriptEngine.LANGUAGE_ID);
    }
}
