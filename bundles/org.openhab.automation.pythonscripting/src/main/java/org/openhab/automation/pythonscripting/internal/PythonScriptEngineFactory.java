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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.ScriptEngine;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.openhab.core.config.core.ConfigParser;
import org.openhab.core.config.core.ConfigurableService;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an implementation of {@link ScriptEngineFactory} for Python.
 *
 * @author Holger Hees - Initial contribution
 */
@Component(service = ScriptEngineFactory.class, configurationPid = "org.openhab.pythonscripting", property = Constants.SERVICE_PID
        + "=org.openhab.pythonscripting")
@ConfigurableService(category = "automation", label = "Python Scripting", description_uri = "automation:pythonscripting")
@NonNullByDefault
public class PythonScriptEngineFactory implements ScriptEngineFactory {
    private final Logger logger = LoggerFactory.getLogger(PythonScriptEngineFactory.class);
    private static final String CFG_JYTHON_EMULATION = "jythonEmulation";

    private boolean jythonEmulation = false;

    public static final String SCRIPT_TYPE = "py3";
    private final List<String> scriptTypes = Arrays.asList(PythonScriptEngineFactory.SCRIPT_TYPE);

    @Activate
    public PythonScriptEngineFactory(Map<String, Object> config) {
        logger.debug("Loading PythonScriptEngineFactory");

        modified(config);
    }

    @Deactivate
    public void cleanup() {
        logger.debug("Unloading PythonScriptEngineFactory");
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

    @Override
    public @Nullable ScriptEngine createScriptEngine(String scriptType) {
        if (!scriptTypes.contains(scriptType)) {
            return null;
        }
        return new OpenhabGraalPythonScriptEngine(jythonEmulation);
        // return new DebuggingGraalScriptEngine<>(new OpenhabGraalPythonScriptEngine(jythonEmulation));
    }

    @Modified
    protected void modified(Map<String, ?> config) {
        this.jythonEmulation = ConfigParser.valueAsOrElse(config.get(CFG_JYTHON_EMULATION), Boolean.class, false);
    }
}
