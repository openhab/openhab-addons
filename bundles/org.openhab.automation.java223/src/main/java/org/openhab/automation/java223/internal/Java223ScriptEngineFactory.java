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
package org.openhab.automation.java223.internal;

import static org.openhab.automation.java223.common.Java223Constants.LIB_DIR;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.script.ScriptEngine;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.java223.common.Java223Constants;
import org.openhab.automation.java223.common.Java223Exception;
import org.openhab.automation.java223.common.ServiceGetter;
import org.openhab.automation.java223.internal.codegeneration.InjectedCodeGenerator;
import org.openhab.automation.java223.internal.codegeneration.Java223HelperLibGenerator;
import org.openhab.automation.java223.internal.strategy.Java223Strategy;
import org.openhab.automation.java223.internal.strategy.ScriptWrappingStrategy;
import org.openhab.core.automation.module.script.ScriptDependencyTracker;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.openhab.core.automation.type.ModuleTypeRegistry;
import org.openhab.core.config.core.ConfigParser;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.service.WatchService;
import org.openhab.core.thing.ThingRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.obermuhlner.scriptengine.java.JavaScriptEngineFactory;
import standalone.com.sun.tools.javac.api.JavacTool;

/**
 * This is an implementation of a {@link ScriptEngineFactory} for Java
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@SuppressWarnings("unused")
@Component(service = ScriptEngineFactory.class, configurationPid = "automation.java223")
@NonNullByDefault
public class Java223ScriptEngineFactory extends JavaScriptEngineFactory
        implements ScriptEngineFactory, WatchService.WatchEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(Java223ScriptEngineFactory.class);

    private final BundleWiring bundleWiring;

    private final Java223Strategy java223Strategy;
    private final ScriptWrappingStrategy scriptWrappingStrategy;

    private final WatchService watchService;

    private final Java223DependencyTracker dependencyTracker;
    private final Java223HelperLibGenerator helperLibGenerator;

    Boolean enableHelper;
    boolean deactivated = false;

    @Activate
    public Java223ScriptEngineFactory(BundleContext bundleContext, Map<String, Object> properties,
            @Reference(target = WatchService.CONFIG_WATCHER_FILTER) WatchService watchService,
            @Reference ItemRegistry itemRegistry, @Reference ThingRegistry thingRegistry,
            @Reference ModuleTypeRegistry moduleTypeRegistry, @Reference Java223DependencyTracker dependencyTracker,
            @Reference InjectedCodeGenerator injectedCodeGenerator, @Reference ServiceGetter serviceGetter,
            @Reference Java223HelperLibGenerator helperLibGenerator) {
        this.watchService = watchService;
        this.bundleWiring = bundleContext.getBundle().adapt(BundleWiring.class);
        this.dependencyTracker = dependencyTracker;
        this.helperLibGenerator = helperLibGenerator;

        String additionalBundlesConfig = ConfigParser
                .valueAsOrElse(properties.get("additionalBundles"), String.class, "").trim();
        String additionalClassesConfig = ConfigParser
                .valueAsOrElse(properties.get("additionalClasses"), String.class, "").trim();
        this.helperLibGenerator.setAdditionalInclusion(additionalBundlesConfig, additionalClassesConfig);

        int startupGuardTime = ConfigParser.valueAsOrElse(properties.get("startupGuardTime"), Integer.class, 60000);
        int writeWaitTime = ConfigParser.valueAsOrElse(properties.get("stabilityGenerationWaitTime"), Integer.class,
                10000);
        this.helperLibGenerator.setDelayTime(startupGuardTime, writeWaitTime);

        Boolean allowInstanceReuse = ConfigParser.valueAsOrElse(properties.get("allowInstanceReuse"), Boolean.class,
                false);
        java223Strategy = new Java223Strategy(bundleContext.getBundle().adapt(BundleWiring.class).getClassLoader(),
                serviceGetter);
        java223Strategy.setAllowInstanceReuse(allowInstanceReuse);

        enableHelper = ConfigParser.valueAsOrElse(properties.get("enableHelper"), Boolean.class, true);
        scriptWrappingStrategy = new ScriptWrappingStrategy(enableHelper, injectedCodeGenerator);
        this.helperLibGenerator.setEnabled(enableHelper);
        try {
            this.helperLibGenerator.generateOrDeleteHelpers();
        } catch (IOException e) {
            LOGGER.warn("Failed to create directory '{}': {}. You should check directory permission if you want "
                    + "to use the helper library.", LIB_DIR, e.getMessage());
        }

        // first build of internal in-memory lib representation
        java223Strategy.scanLibDirectory();
        // When a lib changes, notify
        watchService.registerListener(this, LIB_DIR);
        // registering the watch service for dependency tracker. We must do it AFTER our own
        dependencyTracker.finalizeInitialisation();

        LOGGER.info("Bundle activated");
    }

    @Modified
    protected void modified(Map<String, Object> properties) {
        String additionalBundlesConfig = ConfigParser.valueAsOrElse(properties.get("additionalBundles"), String.class,
                "");
        String additionalClassesConfig = ConfigParser.valueAsOrElse(properties.get("additionalClasses"), String.class,
                "");
        helperLibGenerator.setAdditionalInclusion(additionalBundlesConfig, additionalClassesConfig);

        int stabilityGenerationWaitTime = ConfigParser.valueAsOrElse(properties.get("stabilityGenerationWaitTime"),
                Integer.class, 10000);
        int startupGuardTime = ConfigParser.valueAsOrElse(properties.get("startupGuardTime"), Integer.class, 60000);
        helperLibGenerator.setDelayTime(startupGuardTime, stabilityGenerationWaitTime);

        Boolean allowInstanceReuse = ConfigParser.valueAsOrElse(properties.get("allowInstanceReuse"), Boolean.class,
                false);
        java223Strategy.setAllowInstanceReuse(allowInstanceReuse);

        this.enableHelper = ConfigParser.valueAsOrElse(properties.get("enableHelper"), Boolean.class, true);
        this.scriptWrappingStrategy.setEnableHelper(enableHelper);
        this.helperLibGenerator.setEnabled(enableHelper);
        try {
            this.helperLibGenerator.generateOrDeleteHelpers();
        } catch (IOException e) {
            throw new Java223Exception("Cannot create or delete helper library / class files in lib directory", e);
        }
        LOGGER.debug("java223 configuration update received ({})", properties);
    }

    @Deactivate
    public void deactivate() {
        watchService.unregisterListener(this);
        this.deactivated = true;
    }

    @Override
    public List<String> getScriptTypes() {
        return List.of(Java223Constants.JAVA_FILE_TYPE);
    }

    @Override
    public void scopeValues(ScriptEngine scriptEngine, Map<String, Object> scopeValues) {
        for (Entry<String, Object> entry : scopeValues.entrySet()) {
            scriptEngine.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public @Nullable ScriptEngine createScriptEngine(String scriptType) {
        if (getScriptTypes().contains(scriptType)) {
            JavaCompiler systemJavaCompiler = ToolProvider.getSystemJavaCompiler();
            // fallback if javac is not available in this execution environment :
            systemJavaCompiler = Objects.requireNonNullElseGet(systemJavaCompiler, JavacTool::create);
            return new Java223ScriptEngine(java223Strategy, this::listClassResources, scriptWrappingStrategy,
                    Arrays.asList("-g", "-parameters"), systemJavaCompiler);
        }
        return null;
    }

    @Override
    public ScriptEngine getScriptEngine() {
        ScriptEngine scriptEngine = createScriptEngine(Java223Constants.JAVA_FILE_TYPE);
        if (scriptEngine == null) {
            throw new Java223Exception("Null script engine returned. Should not happened");
        }
        return scriptEngine;
    }

    private Collection<String> listClassResources(String packageName) {
        String path = packageName.replace(".", "/");
        path = "/" + path;

        return bundleWiring.listResources(path, "*.class", 0);
    }

    @Override
    public void processWatchEvent(WatchService.Kind kind, Path fullPath) {
        // When a lib changes, update internal lib storage
        java223Strategy.processWatchEvent(kind, fullPath);
    }

    @Override
    public @Nullable ScriptDependencyTracker getDependencyTracker() {
        return dependencyTracker;
    }
}
