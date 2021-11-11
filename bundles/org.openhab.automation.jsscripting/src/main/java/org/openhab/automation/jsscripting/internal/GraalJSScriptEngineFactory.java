/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;

import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.openhab.core.config.core.ConfigurableService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oracle.truffle.js.scriptengine.GraalJSEngineFactory;

/**
 * An implementation of {@link ScriptEngineFactory} with customizations for GraalJS ScriptEngines.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@Component(service = ScriptEngineFactory.class, configurationPid = "org.openhab.automation.jsscripting", property = Constants.SERVICE_PID
        + "=org.openhab.automation.jsscripting")
@ConfigurableService(category = "automation", label = "JS Scripting", description_uri = "automation:jsscripting")
public final class GraalJSScriptEngineFactory implements ScriptEngineFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraalJSScriptEngineFactory.class);
    private static final String CFG_INJECTION_ENABLED = "injectionEnabled";
    private static final String CFG_INJECTION_CODE = "injectionCode";

    private String injectionCode;

    @Override
    public List<String> getScriptTypes() {
        List<String> scriptTypes = new ArrayList<>();
        GraalJSEngineFactory graalJSEngineFactory = new GraalJSEngineFactory();

        scriptTypes.addAll(graalJSEngineFactory.getMimeTypes());
        scriptTypes.addAll(graalJSEngineFactory.getExtensions());

        return Collections.unmodifiableList(scriptTypes);
    }

    @Override
    public void scopeValues(ScriptEngine scriptEngine, Map<String, Object> scopeValues) {
        // noop; the are retrieved via modules, not injected
    }

    @Override
    public ScriptEngine createScriptEngine(String scriptType) {
        return new DebuggingGraalScriptEngine<>(new OpenhabGraalJSScriptEngine(injectionCode));
    }

    @Activate
    protected void activate(BundleContext context, Map<String, ?> config) {
        modified(config);
    }

    @Modified
    protected void modified(Map<String, ?> config) {
        Object injectionEnabled = config.get(CFG_INJECTION_ENABLED);
        boolean enabled = injectionEnabled != null && (Boolean) injectionEnabled;
        if (enabled) {
            Object injectionCodeObj = config.get(CFG_INJECTION_CODE);
            if (injectionCodeObj != null) {
                LOGGER.debug("Adding code {}", injectionCodeObj);
                injectionCode = (String) injectionCodeObj;
            } else {
                injectionCode = null;
            }
        } else {
            injectionCode = null;
        }
    }
}
