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

import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.jsscripting.internal.fs.watch.JSDependencyTracker;
import org.openhab.core.automation.module.script.ScriptDependencyTracker;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.openhab.core.config.core.ConfigurableService;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

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
    private static final String ALT_MIME_TYPE = "text/javascript;version=ECMAScript-2021";
    private static final String ALIAS = "graaljs";

    private final JSScriptServiceUtil jsScriptServiceUtil;
    private final JSDependencyTracker jsDependencyTracker;

    @Activate
    public GraalJSScriptEngineFactory(final @Reference JSScriptServiceUtil jsScriptServiceUtil,
            final @Reference JSDependencyTracker jsDependencyTracker, Map<String, Object> config) {
        this.jsDependencyTracker = jsDependencyTracker;
        this.jsScriptServiceUtil = jsScriptServiceUtil;
        modified(config);
    }

    @Override
    public List<String> getScriptTypes() {

        /*
         * Whilst we run in parallel with Nashorn, we use a custom mime-type to avoid
         * disrupting Nashorn scripts. When Nashorn is removed, we take over the standard
         * JS runtime.
         */

        // GraalJSEngineFactory graalJSEngineFactory = new GraalJSEngineFactory();
        //
        // scriptTypes.addAll(graalJSEngineFactory.getMimeTypes());
        // scriptTypes.addAll(graalJSEngineFactory.getExtensions());

        return List.of(MIME_TYPE, ALT_MIME_TYPE, ALIAS);
    }

    @Override
    public void scopeValues(ScriptEngine scriptEngine, Map<String, Object> scopeValues) {
        // noop; they are retrieved via modules, not injected
    }

    @Override
    public ScriptEngine createScriptEngine(String scriptType) {
        return new DebuggingGraalScriptEngine<>(
                new OpenhabGraalJSScriptEngine(injectionEnabled ? INJECTION_CODE : null, jsScriptServiceUtil));
    }

    @Override
    public @Nullable ScriptDependencyTracker getDependencyTracker() {
        return jsDependencyTracker;
    }

    @Modified
    protected void modified(Map<String, ?> config) {
        Object injectionEnabled = config.get(CFG_INJECTION_ENABLED);
        this.injectionEnabled = injectionEnabled == null || (Boolean) injectionEnabled;
    }
}
