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

/**
 * An implementation of {@link ScriptEngineFactory} with customizations for GraalJS ScriptEngines.
 *
 * @author Jonathan Gilbert - Initial contribution
 * @author Dan Cunningham - Script injections
 */
@Component(service = ScriptEngineFactory.class, configurationPid = "org.openhab.jsscripting", property = Constants.SERVICE_PID
        + "=org.openhab.jsscripting")
@ConfigurableService(category = "automation", label = "JS Scripting", description_uri = "automation:jsscripting")
public final class GraalJSScriptEngineFactory implements ScriptEngineFactory {
    private static final String CFG_INJECTION_ENABLED = "injectionEnabled";
    private static final String INJECTION_CODE = "Object.assign(this, require('openhab'));";
    private boolean injectionEnabled = true;

    public static final String MIME_TYPE = "application/javascript;version=ECMAScript-2021";

    @Override
    public List<String> getScriptTypes() {
        List<String> scriptTypes = new ArrayList<>();

        /*
         * Whilst we run in parallel with Nashorn, we use a custom mime-type to avoid
         * disrupting Nashorn scripts. When Nashorn is removed, we take over the standard
         * JS runtime.
         */

        // GraalJSEngineFactory graalJSEngineFactory = new GraalJSEngineFactory();
        //
        // scriptTypes.addAll(graalJSEngineFactory.getMimeTypes());
        // scriptTypes.addAll(graalJSEngineFactory.getExtensions());

        scriptTypes.add(MIME_TYPE);

        return Collections.unmodifiableList(scriptTypes);
    }

    @Override
    public void scopeValues(ScriptEngine scriptEngine, Map<String, Object> scopeValues) {
        // noop; the are retrieved via modules, not injected
    }

    @Override
    public ScriptEngine createScriptEngine(String scriptType) {
        return new DebuggingGraalScriptEngine<>(
                new OpenhabGraalJSScriptEngine(injectionEnabled ? INJECTION_CODE : null));
    }

    @Activate
    protected void activate(BundleContext context, Map<String, ?> config) {
        modified(config);
    }

    @Modified
    protected void modified(Map<String, ?> config) {
        Object injectionEnabled = config.get(CFG_INJECTION_ENABLED);
        this.injectionEnabled = injectionEnabled == null || (Boolean) injectionEnabled;
    }
}
