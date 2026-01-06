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

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptEngine;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.java223.common.Java223Constants;
import org.openhab.automation.java223.common.Java223Exception;
import org.openhab.automation.java223.common.ServiceGetter;
import org.openhab.automation.java223.internal.codegeneration.DependencyGenerator;
import org.openhab.automation.java223.internal.codegeneration.InjectedCodeGenerator;
import org.openhab.automation.java223.internal.codegeneration.SourceGenerator;
import org.openhab.automation.java223.internal.codegeneration.SourceWriter;
import org.openhab.automation.java223.internal.strategy.Java223Strategy;
import org.openhab.automation.java223.internal.strategy.ScriptWrappingStrategy;
import org.openhab.core.automation.module.script.ScriptDependencyTracker;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.openhab.core.automation.type.ModuleTypeRegistry;
import org.openhab.core.config.core.ConfigParser;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.ItemAddedEvent;
import org.openhab.core.items.events.ItemRemovedEvent;
import org.openhab.core.service.WatchService;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.events.ThingAddedEvent;
import org.openhab.core.thing.events.ThingRemovedEvent;
import org.openhab.core.thing.events.ThingStatusInfoChangedEvent;
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
import ch.obermuhlner.scriptengine.java.packagelisting.PackageResourceListingStrategy;

/**
 * This is an implementation of a {@link ScriptEngineFactory} for Java
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@SuppressWarnings("unused")
@Component(service = { ScriptEngineFactory.class, Java223ScriptEngineFactory.class,
        EventSubscriber.class }, configurationPid = "automation.java223")
@NonNullByDefault
public class Java223ScriptEngineFactory extends JavaScriptEngineFactory
        implements ScriptEngineFactory, EventSubscriber, WatchService.WatchEventListener {

    private static final Logger logger = LoggerFactory.getLogger(Java223ScriptEngineFactory.class);
    public static final String HELPER_LIB_JAR = "helper-lib.jar";

    private final BundleWiring bundleWiring;

    private final PackageResourceListingStrategy osgiPackageResourceListingStrategy;
    private final Java223Strategy java223Strategy;
    private final ScriptWrappingStrategy scriptWrappingStrategy;

    private final WatchService watchService;

    private final SourceGenerator sourceGenerator;
    private final SourceWriter sourceWriter;
    private final DependencyGenerator dependencyGenerator;
    private final Java223DependencyTracker dependencyTracker;

    private int startupGuardTime;
    private Integer writeWaitTime;

    private static final Set<ThingStatus> INITIALIZED = Set.of(ThingStatus.ONLINE, ThingStatus.OFFLINE,
            ThingStatus.UNKNOWN);
    private static final Set<String> ACTION_EVENTS = Set.of(ThingStatusInfoChangedEvent.TYPE);
    private static final Set<String> ITEM_EVENTS = Set.of(ItemAddedEvent.TYPE, ItemRemovedEvent.TYPE);
    private static final Set<String> THING_EVENTS = Set.of(ThingAddedEvent.TYPE, ThingRemovedEvent.TYPE);
    private static final Set<String> EVENTS = Stream.of(ACTION_EVENTS, ITEM_EVENTS, THING_EVENTS).flatMap(Set::stream)
            .collect(Collectors.toSet());
    Boolean enableHelper;

    @Activate
    public Java223ScriptEngineFactory(BundleContext bundleContext, Map<String, Object> properties,
            @Reference(target = WatchService.CONFIG_WATCHER_FILTER) WatchService watchService,
            @Reference ItemRegistry itemRegistry, @Reference ThingRegistry thingRegistry,
            @Reference ModuleTypeRegistry moduleTypeRegistry, @Reference Java223DependencyTracker dependencyTracker,
            @Reference InjectedCodeGenerator injectedCodeGenerator, @Reference ServiceGetter serviceGetter) {

        try {
            Files.createDirectories(LIB_DIR);
        } catch (IOException e) {
            logger.warn("Failed to create directory '{}': {}", LIB_DIR, e.getMessage());
            throw new IllegalStateException("Failed to initialize lib folder.");
        }

        this.watchService = watchService;
        this.bundleWiring = bundleContext.getBundle().adapt(BundleWiring.class);
        this.dependencyTracker = dependencyTracker;

        String additionalBundlesConfig = ConfigParser
                .valueAsOrElse(properties.get("additionalBundles"), String.class, "").trim();
        String additionalClassesConfig = ConfigParser
                .valueAsOrElse(properties.get("additionalClasses"), String.class, "").trim();
        enableHelper = ConfigParser.valueAsOrElse(properties.get("enableHelper"), Boolean.class, true);
        writeWaitTime = ConfigParser.valueAsOrElse(properties.get("stabilityGenerationWaitTime"), Integer.class, 10000);
        startupGuardTime = ConfigParser.valueAsOrElse(properties.get("startupGuardTime"), Integer.class, 60000);
        Boolean allowInstanceReuse = ConfigParser.valueAsOrElse(properties.get("allowInstanceReuse"), Boolean.class,
                false);

        osgiPackageResourceListingStrategy = this::listClassResources;
        java223Strategy = new Java223Strategy(bundleContext.getBundle().adapt(BundleWiring.class).getClassLoader(),
                serviceGetter);
        java223Strategy.setAllowInstanceReuse(allowInstanceReuse);
        scriptWrappingStrategy = new ScriptWrappingStrategy(enableHelper, injectedCodeGenerator);

        try {
            this.dependencyGenerator = new DependencyGenerator(LIB_DIR, additionalBundlesConfig,
                    additionalClassesConfig, bundleContext);
            this.sourceWriter = new SourceWriter(LIB_DIR);
            this.sourceGenerator = new SourceGenerator(sourceWriter, dependencyGenerator, injectedCodeGenerator,
                    itemRegistry, thingRegistry, moduleTypeRegistry, bundleContext);
            generateOrDeleteHelpers();
        } catch (IOException e) {
            throw new Java223Exception("Cannot create or delete helper library / class files in lib directory", e);
        }

        // first building of internal in-memory lib representation
        java223Strategy.scanLibDirectory();
        // When a lib changes, notify
        watchService.registerListener(this, LIB_DIR);
        // registering the watch service for dependency tracker. We must do it AFTER our own
        dependencyTracker.finalizeInitialisation();

        logger.info("Bundle activated");
    }

    private void generateOrDeleteHelpers() throws IOException {
        if (enableHelper) {
            sourceWriter.createHelperDirectory();
            copyHelperLibJar();
            sourceGenerator.generateThings(startupGuardTime);
            sourceGenerator.generateActions(startupGuardTime);
            sourceGenerator.generateItems(startupGuardTime);
            sourceGenerator.generateJava223Script();
            sourceGenerator.generateEnumStrings();
            dependencyGenerator.createCoreDependencies();
        } else {
            sourceGenerator.cleanGeneratedFiles(sourceWriter.getHelperPath());
            // noinspection ResultOfMethodCallIgnored
            LIB_DIR.resolve(DependencyGenerator.CONVENIENCE_DEPENDENCIES_JAR).toFile().delete();
            // noinspection ResultOfMethodCallIgnored
            LIB_DIR.resolve(HELPER_LIB_JAR).toFile().delete();
        }
    }

    private void copyHelperLibJar() throws IOException {
        // get old file :
        Path dest = LIB_DIR.resolve(HELPER_LIB_JAR);
        byte[] oldHelperLibAsByteArray = new byte[0];
        if (dest.toFile().exists()) {
            oldHelperLibAsByteArray = Files.readAllBytes(dest);
        }

        // get new file :
        byte[] newHelperLibAsByteArray;
        try (InputStream source = getClass().getResourceAsStream("/" + HELPER_LIB_JAR)) {
            if (source != null) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024]; // Buffer size
                int bytesRead;
                while ((bytesRead = source.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
                newHelperLibAsByteArray = byteArrayOutputStream.toByteArray();
            } else {
                throw new Java223Exception("Cannot read helper lib in java223. Should not happened");
            }
        } catch (IOException e) {
            throw new Java223Exception("Cannot read helper file in classpath", e);

        }

        // compare and write, but only if different
        if (!Arrays.equals(oldHelperLibAsByteArray, newHelperLibAsByteArray)) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(dest.toFile())) {
                fileOutputStream.write(newHelperLibAsByteArray);
            }
        }
    }

    @Modified
    protected void modified(Map<String, Object> properties) {
        String additionalBundlesConfig = ConfigParser.valueAsOrElse(properties.get("additionalBundles"), String.class,
                "");
        String additionalClassesConfig = ConfigParser.valueAsOrElse(properties.get("additionalClasses"), String.class,
                "");
        Integer stabilityGenerationWaitTime = ConfigParser.valueAsOrElse(properties.get("stabilityGenerationWaitTime"),
                Integer.class, 10000);
        Boolean allowInstanceReuse = ConfigParser.valueAsOrElse(properties.get("allowInstanceReuse"), Boolean.class,
                false);
        enableHelper = ConfigParser.valueAsOrElse(properties.get("enableHelper"), Boolean.class, true);
        this.startupGuardTime = ConfigParser.valueAsOrElse(properties.get("startupGuardTime"), Integer.class, 60000);

        scriptWrappingStrategy.setEnableHelper(enableHelper);
        this.writeWaitTime = stabilityGenerationWaitTime;
        java223Strategy.setAllowInstanceReuse(allowInstanceReuse);
        dependencyGenerator.setAdditionalConfig(additionalBundlesConfig, additionalClassesConfig);

        try {
            generateOrDeleteHelpers();
        } catch (IOException e) {
            throw new Java223Exception("Cannot create or delete helper library / class files in lib directory", e);
        }
        logger.debug("java223 configuration update received ({})", properties);
    }

    @Deactivate
    public void deactivate() {
        watchService.unregisterListener(this);
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
            return new Java223ScriptEngine(java223Strategy, osgiPackageResourceListingStrategy, scriptWrappingStrategy,
                    Arrays.asList("-g", "-parameters"));
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
    public Set<String> getSubscribedEventTypes() {
        return EVENTS;
    }

    @Override
    public void receive(Event event) {
        if (!enableHelper) {
            logger.debug("Event received but helper is disabled");
            return;
        }

        String eventType = event.getType();

        if (ACTION_EVENTS.contains(eventType)) {
            ThingStatusInfoChangedEvent eventStatusInfoChange = (ThingStatusInfoChangedEvent) event;
            if ((ThingStatus.INITIALIZING.equals(eventStatusInfoChange.getOldStatusInfo().getStatus())
                    && INITIALIZED.contains(eventStatusInfoChange.getStatusInfo().getStatus()))
                    || (ThingStatus.UNINITIALIZED.equals(eventStatusInfoChange.getStatusInfo().getStatus())
                            && INITIALIZED.contains(eventStatusInfoChange.getOldStatusInfo().getStatus()))) {
                sourceGenerator.generateActions(writeWaitTime);
            }
        } else if (ITEM_EVENTS.contains(eventType)) {
            logger.debug("Added/updated item: {}", event);
            sourceGenerator.generateItems(writeWaitTime);
        } else if (THING_EVENTS.contains(eventType)) {
            logger.debug("Added/updated thing: {}", event);
            sourceGenerator.generateThings(writeWaitTime);
            sourceGenerator.generateActions(writeWaitTime);
        }
    }

    @Override
    public void processWatchEvent(WatchService.Kind kind, Path fullPath) {
        // When a lib changes, update internal lib storage
        java223Strategy.processWatchEvent(kind, fullPath);
        // When a lib is removed, SourceWriter should know because it may have to regenerate it
        sourceWriter.processWatchEvent(kind, fullPath);
    }

    @Override
    public @Nullable ScriptDependencyTracker getDependencyTracker() {
        return dependencyTracker;
    }
}
