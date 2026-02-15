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
import org.graalvm.polyglot.Language;
import org.openhab.automation.pythonscripting.internal.fs.PythonDependencyTracker;
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

    private final @Nullable Language language;

    @Activate
    public PythonScriptEngineFactory(final @Reference PythonDependencyTracker pythonDependencyTracker,
            final @Reference TimeZoneProvider timeZoneProvider, Map<String, Object> config) {
        logger.debug("Loading PythonScriptEngineFactory");

        this.language = PythonScriptEngine.getLanguage();
        if (this.language == null) {
            logger.error(
                    "Graal Python language not initialized. Restart openHAB to initialize available Graal languages properly.");
        }

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
        if (language == null) {
            return null;
        }
        return new PythonScriptEngine(configuration, this);
    }

    @Override
    public @Nullable ScriptDependencyTracker getDependencyTracker() {
        return pythonDependencyTracker;
    }

    public PythonScriptEngineConfiguration getConfiguration() {
        return this.configuration;
    }
}
