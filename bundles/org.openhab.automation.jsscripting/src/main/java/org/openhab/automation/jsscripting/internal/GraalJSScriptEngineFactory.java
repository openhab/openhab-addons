/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.stream.Stream;

import javax.script.ScriptEngine;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.jsscripting.internal.fs.watch.JSDependencyTracker;
import org.openhab.core.automation.module.script.ScriptDependencyTracker;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.openhab.core.config.core.ConfigParser;
import org.openhab.core.config.core.ConfigurableService;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import com.oracle.truffle.js.scriptengine.GraalJSEngineFactory;

/**
 * An implementation of {@link ScriptEngineFactory} with customizations for GraalJS ScriptEngines.
 *
 * @author Jonathan Gilbert - Initial contribution
 * @author Dan Cunningham - Script injections
 */
@Component(service = ScriptEngineFactory.class, configurationPid = "org.openhab.jsscripting", property = Constants.SERVICE_PID
        + "=org.openhab.jsscripting")
@ConfigurableService(category = "automation", label = "JS Scripting", description_uri = "automation:jsscripting")
@NonNullByDefault
public final class GraalJSScriptEngineFactory implements ScriptEngineFactory {
    private static final String CFG_INJECTION_ENABLED = "injectionEnabled";
    private static final String CFG_INJECTION_CACHING_ENABLED = "injectionCachingEnabled";

    private static final GraalJSEngineFactory factory = new GraalJSEngineFactory();

    private static final List<String> scriptTypes = createScriptTypes();

    private static List<String> createScriptTypes() {
        // Add those for backward compatibility (existing scripts may rely on those MIME types)
        List<String> backwardCompat = List.of("application/javascript;version=ECMAScript-2021", "graaljs");
        return Stream.of(factory.getMimeTypes(), factory.getExtensions(), backwardCompat).flatMap(List::stream)
                .toList();
    }

    private boolean injectionEnabled = true;
    private boolean injectionCachingEnabled = true;

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
        return new DebuggingGraalScriptEngine<>(new OpenhabGraalJSScriptEngine(injectionEnabled,
                injectionCachingEnabled, jsScriptServiceUtil, jsDependencyTracker));
    }

    @Override
    public @Nullable ScriptDependencyTracker getDependencyTracker() {
        return jsDependencyTracker;
    }

    @Modified
    protected void modified(Map<String, ?> config) {
        this.injectionEnabled = ConfigParser.valueAsOrElse(config.get(CFG_INJECTION_ENABLED), Boolean.class, true);
        this.injectionCachingEnabled = ConfigParser.valueAsOrElse(config.get(CFG_INJECTION_CACHING_ENABLED),
                Boolean.class, true);
    }
}
