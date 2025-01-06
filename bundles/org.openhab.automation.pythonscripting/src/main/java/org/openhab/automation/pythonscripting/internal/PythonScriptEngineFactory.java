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

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.script.ScriptEngine;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.pythonscripting.internal.generic.GenericScriptServiceUtil;
import org.openhab.automation.pythonscripting.internal.graal.GraalPythonScriptEngineFactory;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.openhab.core.config.core.ConfigParser;
import org.openhab.core.config.core.ConfigurableService;
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
 */
@Component(service = ScriptEngineFactory.class, configurationPid = "org.openhab.pythonscripting", property = Constants.SERVICE_PID
        + "=org.openhab.pythonscripting")
@ConfigurableService(category = "automation", label = "Python Scripting", description_uri = "automation:pythonscripting")
@NonNullByDefault
public class PythonScriptEngineFactory implements ScriptEngineFactory {
    private final Logger logger = LoggerFactory.getLogger(PythonScriptEngineFactory.class);
    private static final String CFG_INJECTION_ENABLED = "injectionEnabled";
    private static final String CFG_INJECTION_CACHING_ENABLED = "injectionCachingEnabled";
    private static final String CFG_JYTHON_EMULATION = "jythonEmulation";

    private boolean injectionEnabled = true;
    private boolean injectionCachingEnabled = true;
    private boolean jythonEmulation = false;

    private static final GraalPythonScriptEngineFactory factory = new GraalPythonScriptEngineFactory();

    private final List<String> scriptTypes = (List<String>) Stream.of(factory.getExtensions(), factory.getMimeTypes()) //
            .flatMap(List::stream) //
            .toList();

    private final GenericScriptServiceUtil genericScriptServiceUtil;
    // private final JSDependencyTracker jsDependencyTracker;

    @Activate
    public PythonScriptEngineFactory(final @Reference GenericScriptServiceUtil genericScriptServiceUtil,
            Map<String, Object> config) {
        logger.debug("Loading PythonScriptEngineFactory");

        this.genericScriptServiceUtil = genericScriptServiceUtil;
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
        // noop; they are retrieved via modules, not injected
    }

    @Override
    public @Nullable ScriptEngine createScriptEngine(String scriptType) {
        if (!scriptTypes.contains(scriptType)) {
            return null;
        }
        return new DebuggingGraalScriptEngine<>(new OpenhabGraalPythonScriptEngine(injectionEnabled,
                injectionCachingEnabled, jythonEmulation, genericScriptServiceUtil));
    }

    /*
     * @Override
     * public @Nullable ScriptDependencyTracker getDependencyTracker() {
     * return jsDependencyTracker;
     * }
     */

    @Modified
    protected void modified(Map<String, ?> config) {
        this.injectionEnabled = ConfigParser.valueAsOrElse(config.get(CFG_INJECTION_ENABLED), Boolean.class, true);
        this.injectionCachingEnabled = ConfigParser.valueAsOrElse(config.get(CFG_INJECTION_CACHING_ENABLED),
                Boolean.class, true);
        this.jythonEmulation = ConfigParser.valueAsOrElse(config.get(CFG_JYTHON_EMULATION), Boolean.class, false);
    }
}
