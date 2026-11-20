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
import org.openhab.automation.jsscripting.internal.scope.OSGiScriptExtensionProvider;
import org.openhab.automation.jsscripting.internal.util.ThreadLocalSlf4jOutputStream;
import org.openhab.core.OpenHAB;
import org.openhab.core.automation.module.script.ScriptDependencyTracker;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.graal.GraalUtil;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * An implementation of {@link ScriptEngineFactory} with customizations for GraalJS ScriptEngines.
 *
 * @author Jonathan Gilbert - Initial contribution
 * @author Dan Cunningham - Script injections
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
     * Shared Polyglot {@link Engine} instance to be used by all instances of {@link OpenhabGraalJSScriptEngine}.
     */
    private final @Nullable Engine engine;

    private final JSScriptServiceUtil jsScriptServiceUtil;
    private volatile @Nullable JSDependencyTracker jsDependencyTracker;

    @Activate
    public GraalJSScriptEngineFactory(final @Reference JSScriptServiceUtil jsScriptServiceUtil, //
            /*
             * declare dependency on OSGiScriptExtensionProvider to fix a timing issue where openhab-js attempts to
             * lookup OSGi services before OSGiScriptExtensionProvider is active
             */
            final @Reference OSGiScriptExtensionProvider osgiScriptExtensionProvider, Map<String, Object> config) {
        logger.debug("Loading GraalJSScriptEngineFactory");

        this.jsScriptServiceUtil = jsScriptServiceUtil;
        this.configuration = new GraalJSScriptEngineConfiguration(config);

        Engine engine;
        try {
            engine = createEngine();
            logger.debug("GraalJS engine created; language resolution deferred to first use");
        } catch (Exception e) {
            logger.error("Failed to create GraalJS engine", e);
            engine = null;
        }
        this.engine = engine;
    }

    private Engine createEngine() {
        Thread thread = Thread.currentThread();

        // The classloader is swapped during creation to make sure the engine can "see" what it needs
        ClassLoader original = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(GraalJSScriptEngineFactory.class.getClassLoader());
            if (configuration.isDebuggerEnabled()) {
                Engine.Builder engineBuilder = createEngineBuilder();
                engineBuilder //
                        .option("inspect", "0.0.0.0:" + configuration.getDebuggerPort()) //
                        .option("inspect.Suspend", "false") // Don't pause at startup waiting for debugger to attach
                        .option("inspect.WaitAttached", "false") // Don't block code execution waiting for debugger to
                                                                 // attach
                        .option("inspect.Secure", "false"); // Disable TLS
                Engine engine;
                try {
                    engine = engineBuilder.build();
                } catch (RuntimeException e) {
                    logger.error(
                            "Failed to initialize Graal JavaScript engine with debugger support. Continuing without debugger support.",
                            e);
                    engine = createEngineBuilder().build();
                }
                logger.info("Debugger support is enabled for JavaScript Scripting.");
                return engine;
            } else {
                return createEngineBuilder().build();
            }
        } finally {
            thread.setContextClassLoader(original);
        }
    }

    private Engine.Builder createEngineBuilder() {
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

    @Deactivate
    public void dispose() {
        Engine engine = this.engine;
        if (engine != null) {
            engine.close();
        }
        GraalUtil.clearCache();
    }

    @Modified
    protected void modified(Map<String, ?> config) {
        configuration.modified(config);
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    protected void setJsDependencyTracker(JSDependencyTracker tracker) {
        this.jsDependencyTracker = tracker;
    }

    protected void unsetJsDependencyTracker(JSDependencyTracker tracker) {
        if (this.jsDependencyTracker == tracker) {
            this.jsDependencyTracker = null;
        }
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

        if (engine == null) {
            logger.error("Graal engine not initialized");
            return null;
        }

        // Use the common lock to safely get the language
        Language language = GraalUtil.getLanguage(engine, OpenhabGraalJSScriptEngine.LANGUAGE_ID);
        if (language == null) {
            logger.error(LANG_NOT_INITIALIZED_MSG);
            return null;
        }
        return new DebuggingGraalScriptEngine<>(
                new OpenhabGraalJSScriptEngine(configuration, engine, jsScriptServiceUtil, jsDependencyTracker));
    }

    @Override
    public @Nullable ScriptDependencyTracker getDependencyTracker() {
        return jsDependencyTracker;
    }

    @Override
    public boolean isReady() {
        return GraalUtil.getLanguage(engine, OpenhabGraalJSScriptEngine.LANGUAGE_ID) != null;
    }
}
