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
package org.openhab.automation.jsscripting.internal;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.script.ScriptEngine;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.jsscripting.internal.fs.watch.JSDependencyTracker;
import org.openhab.core.OpenHAB;
import org.openhab.core.automation.module.script.ScriptDependencyTracker;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.openhab.core.config.core.ConfigurableService;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class GraalJSScriptEngineFactory implements ScriptEngineFactory {
    public static final Path JS_DEFAULT_PATH = Paths.get(OpenHAB.getConfigFolder(), "automation", "js");
    public static final String NODE_DIR = "node_modules";
    public static final Path JS_LIB_PATH = JS_DEFAULT_PATH.resolve(NODE_DIR);

    public static final String SCRIPT_TYPE = "application/javascript";

    private static final GraalJSEngineFactory FACTORY = new GraalJSEngineFactory();

    private static final List<String> SCRIPT_TYPES = createScriptTypes();

    private static List<String> createScriptTypes() {
        // Add those for backward compatibility (existing scripts may rely on those MIME types)
        List<String> backwardCompat = List.of("application/javascript;version=ECMAScript-2021", "graaljs");
        return Stream.of(List.of(SCRIPT_TYPE), FACTORY.getMimeTypes(), FACTORY.getExtensions(), backwardCompat)
                .flatMap(List::stream).distinct().toList();
    }

    private final Logger logger = LoggerFactory.getLogger(GraalJSScriptEngineFactory.class);
    private final GraalJSScriptEngineConfiguration configuration;

    private final JSScriptServiceUtil jsScriptServiceUtil;
    private final JSDependencyTracker jsDependencyTracker;

    @Activate
    public GraalJSScriptEngineFactory(final @Reference JSScriptServiceUtil jsScriptServiceUtil,
            final @Reference JSDependencyTracker jsDependencyTracker, Map<String, Object> config) {
        logger.debug("Loading GraalJSScriptEngineFactory");

        this.jsDependencyTracker = jsDependencyTracker;
        this.jsScriptServiceUtil = jsScriptServiceUtil;
        this.configuration = new GraalJSScriptEngineConfiguration(config);
    }

    @Modified
    protected void modified(Map<String, ?> config) {
        configuration.modified(config);
    }

    @Override
    public List<String> getScriptTypes() {
        return SCRIPT_TYPES;
    }

    @Override
    public void scopeValues(ScriptEngine scriptEngine, Map<String, Object> scopeValues) {
        // noop; they are retrieved via modules, not injected
    }

    @Override
    public @Nullable ScriptEngine createScriptEngine(String scriptType) {
        if (!SCRIPT_TYPES.contains(scriptType)) {
            return null;
        }
        return new DebuggingGraalScriptEngine<>(
                new OpenhabGraalJSScriptEngine(configuration, jsScriptServiceUtil, jsDependencyTracker));
    }

    @Override
    public @Nullable ScriptDependencyTracker getDependencyTracker() {
        return jsDependencyTracker;
    }
}
