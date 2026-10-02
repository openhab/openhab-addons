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
package org.openhab.automation.java223.internal.codegeneration;

import static org.openhab.automation.java223.common.Java223Constants.LIB_DIR;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.automation.java223.common.Java223Constants;
import org.openhab.automation.java223.common.Java223Exception;
import org.openhab.core.automation.type.ModuleTypeRegistry;
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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate all helper libraries (generated sources and dependencies jar)
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
@Component(service = { EventSubscriber.class, Java223HelperLibGenerator.class })
public class Java223HelperLibGenerator implements EventSubscriber, WatchService.WatchEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(Java223HelperLibGenerator.class);

    private final WatchService watchService;

    private final SourceGenerator sourceGenerator;
    private final SourceWriter sourceWriter;
    private final DependencyGenerator dependencyGenerator;

    private int startupGuardTime;
    private int writeWaitTime;
    private boolean enabled = true;

    public static final String HELPER_LIB_JAR = "java223-helper-lib.jar";

    private static final Set<ThingStatus> INITIALIZED = Set.of(ThingStatus.ONLINE, ThingStatus.OFFLINE,
            ThingStatus.UNKNOWN);
    private static final Set<String> ACTION_EVENTS = Set.of(ThingStatusInfoChangedEvent.TYPE);
    private static final Set<String> ITEM_EVENTS = Set.of(ItemAddedEvent.TYPE, ItemRemovedEvent.TYPE);
    private static final Set<String> THING_EVENTS = Set.of(ThingAddedEvent.TYPE, ThingRemovedEvent.TYPE);
    private static final Set<String> EVENTS = Stream.of(ACTION_EVENTS, ITEM_EVENTS, THING_EVENTS).flatMap(Set::stream)
            .collect(Collectors.toSet());

    @Activate
    public Java223HelperLibGenerator(BundleContext bundleContext,
            @Reference(target = WatchService.CONFIG_WATCHER_FILTER) WatchService watchService,
            @Reference ItemRegistry itemRegistry, @Reference ThingRegistry thingRegistry,
            @Reference ModuleTypeRegistry moduleTypeRegistry, @Reference InjectedCodeGenerator injectedCodeGenerator) {
        this.watchService = watchService;
        this.dependencyGenerator = new DependencyGenerator(LIB_DIR, bundleContext);
        this.sourceWriter = new SourceWriter(LIB_DIR);
        this.sourceGenerator = new SourceGenerator(sourceWriter, dependencyGenerator, injectedCodeGenerator,
                itemRegistry, thingRegistry, moduleTypeRegistry, bundleContext);
        watchService.registerListener(this, LIB_DIR);
    }

    public void setDelayTime(int startupGuardTime, int writeWaitTime) {
        this.startupGuardTime = startupGuardTime;
        this.writeWaitTime = writeWaitTime;
    }

    public void setAdditionalInclusion(String additionalBundlesConfig, String additionalClassesConfig) {
        this.dependencyGenerator.setAdditionalConfig(additionalBundlesConfig, additionalClassesConfig);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Deactivate
    public void deactivate() {
        watchService.unregisterListener(this);
        sourceGenerator.deactivate();
        this.enabled = false;
    }

    @Override
    public void processWatchEvent(WatchService.Kind kind, Path path) {
        // When a lib is removed, SourceWriter should know because it may have to regenerate it
        // Because we write only if we found differences with the existent,
        // we have to maintain a consistent view of the file system.
        // And thus we need to be notified when a file is deleted or modified
        Path fullPath = LIB_DIR.resolve(path);
        if (fullPath.getFileName().toString().endsWith("." + Java223Constants.JAVA_FILE_TYPE)
                && (kind == WatchService.Kind.DELETE || kind == WatchService.Kind.MODIFY)) {
            // by intercepting delete or modify signal, we ensure that we remove java files from our internal database:
            // The file is different or absent. We can / should regenerate them thereafter
            sourceWriter.removeSourceFile(fullPath);
        } else {
            LOGGER.trace(
                    "Received '{}' for path '{}' - ignoring it for the purpose of tracing autogenerated source modification (wrong extension, or unused event kind)",
                    kind, fullPath);
        }
    }

    @Override
    public void receive(Event event) {
        if (!enabled) {
            LOGGER.debug("Event received but helper lib is disabled");
            return;
        }

        String eventType = event.getType();

        SourceGenerator sourceGeneratorLocal = sourceGenerator;
        if (ACTION_EVENTS.contains(eventType)) {
            ThingStatusInfoChangedEvent eventStatusInfoChange = (ThingStatusInfoChangedEvent) event;
            if ((ThingStatus.INITIALIZING.equals(eventStatusInfoChange.getOldStatusInfo().getStatus())
                    && INITIALIZED.contains(eventStatusInfoChange.getStatusInfo().getStatus()))
                    || (ThingStatus.UNINITIALIZED.equals(eventStatusInfoChange.getStatusInfo().getStatus())
                            && INITIALIZED.contains(eventStatusInfoChange.getOldStatusInfo().getStatus()))) {
                sourceGeneratorLocal.generateActions(writeWaitTime);
            }
        } else if (ITEM_EVENTS.contains(eventType)) {
            LOGGER.debug("Added/updated item: {}", event);
            sourceGeneratorLocal.generateItems(writeWaitTime);
        } else if (THING_EVENTS.contains(eventType)) {
            LOGGER.debug("Added/updated thing: {}", event);
            sourceGeneratorLocal.generateThings(writeWaitTime);
            sourceGeneratorLocal.generateActions(writeWaitTime);
        }
    }

    public void generateOrDeleteHelpers() throws IOException {
        if (enabled) {
            Files.createDirectories(LIB_DIR);
            sourceWriter.createHelperDirectory();
            copyHelperLibJar();
            sourceGenerator.generateThings(startupGuardTime);
            sourceGenerator.generateActions(startupGuardTime);
            sourceGenerator.generateItems(startupGuardTime);
            sourceGenerator.generateJava223Script();
            sourceGenerator.generateEnumStrings();
            dependencyGenerator.createCoreDependencies();
        } else {
            LOGGER.debug("Helper lib generation is disabled. Cleaning up generated files.");
            sourceGenerator.cleanGeneratedFiles(sourceWriter.getHelperPath());
            // noinspection ResultOfMethodCallIgnored
            LIB_DIR.resolve(DependencyGenerator.CONVENIENCE_DEPENDENCIES_JAR).toFile().delete();
            // noinspection ResultOfMethodCallIgnored
            LIB_DIR.resolve(HELPER_LIB_JAR).toFile().delete();
        }
    }

    private void copyHelperLibJar() throws IOException {
        // get old file:
        Path dest = LIB_DIR.resolve(HELPER_LIB_JAR);
        byte[] oldHelperLibAsByteArray = new byte[0];
        if (dest.toFile().exists()) {
            oldHelperLibAsByteArray = Files.readAllBytes(dest);
        }

        // get new file:
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

    @Override
    public Set<String> getSubscribedEventTypes() {
        return EVENTS;
    }
}
