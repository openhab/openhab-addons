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
package org.openhab.automation.jsscripting.internal;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Language;
import org.openhab.automation.jsscripting.internal.fs.watch.JSDependencyTracker;
import org.openhab.automation.jsscripting.internal.util.ThreadLocalSlf4jOutputStream;
import org.openhab.core.OpenHAB;
import org.openhab.core.automation.module.script.ScriptDependencyTracker;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.openhab.core.config.core.ConfigurableService;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * An implementation of {@link ScriptEngineFactory} with customizations for GraalJS ScriptEngines.
 *
 * @author Jonathan Gilbert - Initial contribution
 * @author Dan Cunningham - Script injections; Scope the shared Engine to the bundle lifetime
 * @author Florian Hotze - Debugger support
 */
@Component(service = ScriptEngineFactory.class, configurationPid = "org.openhab.jsscripting", property = Constants.SERVICE_PID
        + "=org.openhab.jsscripting")
@ConfigurableService(category = "automation", label = "JavaScript Scripting", description_uri = "automation:jsscripting")
@NonNullByDefault
public class GraalJSScriptEngineFactory implements ScriptEngineFactory {
    public static final Path JS_DEFAULT_PATH = Paths.get(OpenHAB.getConfigFolder(), "automation", "js");
    public static final String NODE_DIR = "node_modules";
    public static final Path JS_LIB_PATH = JS_DEFAULT_PATH.resolve(NODE_DIR);

    public static final String SCRIPT_TYPE = "application/javascript";
    public static final String SCRIPT_FILE_EXTENSION = "js";

    private static final String LANG_NOT_INITIALIZED_MSG = "Graal JavaScript language not initialized. Restart openHAB to initialize available Graal languages properly.";

    private static final List<String> SCRIPT_TYPES = List.of(SCRIPT_TYPE, SCRIPT_FILE_EXTENSION, "graaljs",
            // backward compatibility with the MIME type used in openHAB 3.x:
            "application/javascript;version=ECMAScript-2021");

    private final Logger logger = LoggerFactory.getLogger(GraalJSScriptEngineFactory.class);
    private final GraalJSScriptEngineConfiguration configuration;

    /**
     * Shared Polyglot {@link Engine} used by all {@link OpenhabGraalJSScriptEngine} instances.
     * <p>
     * Kept static and never closed on deactivation. Script contexts created from it can outlive a component restart
     * (e.g. when an OSGi reference is rebound), and closing the engine would break every loaded rule with
     * "The Context is already closed". So we create it once and reuse it for the life of the bundle.
     */
    private static volatile @Nullable Engine engine;
    private static final Object ENGINE_LOCK = new Object();

    private final JSScriptServiceUtil jsScriptServiceUtil;
    private final JSDependencyTracker jsDependencyTracker;

    @Activate
    public GraalJSScriptEngineFactory(final @Reference JSScriptServiceUtil jsScriptServiceUtil,
            final @Reference JSDependencyTracker jsDependencyTracker, Map<String, Object> config) {
        logger.debug("Loading GraalJSScriptEngineFactory");

        this.jsDependencyTracker = jsDependencyTracker;
        this.jsScriptServiceUtil = jsScriptServiceUtil;
        this.configuration = new GraalJSScriptEngineConfiguration(config);

        if (getEngine(configuration, logger) == null || getLanguage() == null) {
            logger.error(LANG_NOT_INITIALIZED_MSG);
        }
    }

    /**
     * Creates the shared {@link Engine} on first use and reuses it afterwards. Debugger settings are read once here,
     * so changing them only takes effect after an openHAB restart.
     *
     * @return the shared {@link Engine}, or {@code null} if it could not be created
     */
    private static @Nullable Engine getEngine(GraalJSScriptEngineConfiguration configuration, Logger logger) {
        synchronized (ENGINE_LOCK) {
            Engine localEngine = engine;
            if (localEngine != null) {
                return localEngine;
            }
            try {
                if (configuration.isDebuggerEnabled()) {
                    Engine.Builder engineBuilder = createEngineBuilder() //
                            .option("inspect", "0.0.0.0:" + configuration.getDebuggerPort()) //
                            .option("inspect.Suspend", "false") // Don't pause at startup waiting for debugger to attach
                            .option("inspect.WaitAttached", "false") // Don't block code execution waiting for debugger
                                                                     // to attach
                            .option("inspect.Secure", "false"); // Disable TLS
                    try {
                        localEngine = engineBuilder.build();
                    } catch (RuntimeException e) {
                        logger.error(
                                "Failed to initialize Graal JavaScript engine with debugger support. Continuing without debugger support.",
                                e);
                        localEngine = createEngineBuilder().build();
                    }
                    logger.info("Debugger support is enabled for JavaScript Scripting.");
                } else {
                    localEngine = createEngineBuilder().build();
                }
            } catch (RuntimeException e) {
                logger.error("Failed to initialize Graal JavaScript engine.", e);
                return null;
            }
            engine = localEngine;
            return localEngine;
        }
    }

    private static Engine.Builder createEngineBuilder() {
        Logger engineLogger = LoggerFactory
                .getLogger(GraalJSScriptEngineFactory.class.getPackageName() + ".org.graalvm.polyglot.Engine");
        return Engine.newBuilder().allowExperimentalOptions(true) //
                .option("engine.WarnInterpreterOnly", "false") //
                .out(new ThreadLocalSlf4jOutputStream(engineLogger, Level.DEBUG)) //
                // Note: Due to a bug in GraalVM, info messages are logged to the err stream, so hide it until the fix
                // is available. FTR: https://github.com/oracle/graal/issues/13222
                // TODO: Increase level to WARN when upgrading GraalVM
                .err(new ThreadLocalSlf4jOutputStream(engineLogger, Level.DEBUG));
    }

    /**
     * Releases the shared {@link Engine}, but only when the bundle is actually stopping. The reason is checked so that
     * ordinary component restarts (e.g. an OSGi reference rebind) do not close the engine and break running scripts -
     * that is exactly what this fix prevents. Only a real bundle stop releases the engine.
     */
    @Deactivate
    protected void deactivate(int reason) {
        if (reason == ComponentConstants.DEACTIVATION_REASON_BUNDLE_STOPPED) {
            dispose();
        }
    }

    /**
     * Closes the shared {@link Engine} and clears it so it is re-created on next use. Called on bundle stop and from
     * tests.
     */
    public void dispose() {
        synchronized (ENGINE_LOCK) {
            Engine localEngine = engine;
            if (localEngine != null) {
                localEngine.close();
                engine = null;
            }
        }
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
        Engine localEngine = getEngine(configuration, logger);
        if (localEngine == null || getLanguage() == null) {
            logger.error(LANG_NOT_INITIALIZED_MSG);
            return null;
        }
        return new DebuggingGraalScriptEngine<>(
                new OpenhabGraalJSScriptEngine(configuration, localEngine, jsScriptServiceUtil, jsDependencyTracker));
    }

    @Override
    public @Nullable ScriptDependencyTracker getDependencyTracker() {
        return jsDependencyTracker;
    }

    /**
     * Gets the Graal language of {@link OpenhabGraalJSScriptEngine}.
     *
     * @return the Graal language of {@link OpenhabGraalJSScriptEngine} or {@code null} if not available
     */
    private @Nullable Language getLanguage() {
        Engine localEngine = engine;
        return localEngine == null ? null : localEngine.getLanguages().get(OpenhabGraalJSScriptEngine.LANGUAGE_ID);
    }
}
