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
package org.openhab.binding.broadlink.internal;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.CommandOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Broadlink-specific mapper watches the given map file and loads the contents
 * into a Map in order to offer keys that it then dynamically supplies to the provided
 * BroadlinkRemoteDynamicCommandDescriptionProvider.
 *
 * @author John Marshall - Initial contribution
 */
@NonNullByDefault
public class BroadlinkMappingService {
    private static final String TRANSFORM_DIR = OpenHAB.getConfigFolder() + File.separator + "transform"
            + File.separator;
    private final Logger logger = LoggerFactory.getLogger(BroadlinkMappingService.class);
    private final String mapFileName;
    private final BroadlinkRemoteDynamicCommandDescriptionProvider commandDescriptionProvider;
    private final ChannelUID targetChannelUID;
    private final Map<String, String> commandMap = new HashMap<>();

    private @Nullable WatchService watchService = null;
    private @Nullable WatchKey transformDirWatchKey = null;
    private @Nullable Thread watchThread = null;

    public BroadlinkMappingService(String mapFileName,
            BroadlinkRemoteDynamicCommandDescriptionProvider commandDescriptionProvider, ChannelUID targetChannelUID) {
        this.mapFileName = mapFileName;
        this.commandDescriptionProvider = commandDescriptionProvider;
        this.targetChannelUID = targetChannelUID;
        reloadFromFile();
        startWatching();
        logger.debug("BroadlinkMappingService constructed on behalf of {}", this.targetChannelUID);
    }

    @SuppressWarnings("null")
    public void dispose() {
        try {
            if (watchThread != null && !watchThread.isInterrupted()) {
                watchThread.interrupt();
                watchThread = null;
            }
            if (this.transformDirWatchKey != null) {
                this.transformDirWatchKey.cancel();
                this.transformDirWatchKey = null;
            }
            if (this.watchService != null) {
                this.watchService.close();
                this.watchService = null;
            }
        } catch (IOException ioe) {
            logger.warn("Cannot deactivate watcher", ioe);
        }
    }

    public @Nullable String lookup(String command) {
        return commandMap.get(command);
    }

    @SuppressWarnings({ "null", "unchecked" })
    private Runnable watchingRunnable = new Runnable() {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    WatchKey key = watchService.take(); // Blocks
                    List<WatchEvent<?>> events = key.pollEvents();

                    Stream<WatchEvent<?>> modificationEvents = events.stream()
                            .filter(e -> e.kind() == StandardWatchEventKinds.ENTRY_MODIFY);

                    if (modificationEvents
                            .anyMatch(e -> ((WatchEvent<Path>) e).context().toString().equals(mapFileName))) {
                        logger.debug("File {} has changed - reloading", mapFileName);
                        reloadFromFile();
                    }
                    key.reset();
                } catch (InterruptedException x) {
                    return;
                }
            }
        }
    };

    @SuppressWarnings("null")
    private void startWatching() {
        try {
            this.watchService = FileSystems.getDefault().newWatchService();
            this.transformDirWatchKey = Paths.get(TRANSFORM_DIR).register(watchService,
                    StandardWatchEventKinds.ENTRY_MODIFY);
            this.watchThread = new Thread(watchingRunnable, "BroadlinkMappingService-mapfile-watcher");
            this.watchThread.setDaemon(true);
            this.watchThread.start();
        } catch (IOException ioe) {
            logger.warn("Couldn't setup automatic watch:", ioe);
        }
    }

    private void reloadFromFile() {
        Properties props = new Properties();
        Path mapFilePath = Paths.get(TRANSFORM_DIR + mapFileName);
        try (FileReader reader = new FileReader(mapFilePath.toFile())) {
            props.load(reader);
            commandMap.clear();
            props.stringPropertyNames().forEach(k -> {
                commandMap.put(k, props.getProperty(k));
            });
            logger.debug("Read {} commands from {}", commandMap.size(), mapFilePath);
            notifyAvailableCommands(commandMap.keySet());
        } catch (IOException e) {
            logger.warn("Couldn't read {}: {}", mapFilePath, e.getMessage());
        }
    }

    private void notifyAvailableCommands(Set<String> commandNames) {
        List<CommandOption> commandOptions = new ArrayList<>();
        commandNames.forEach((c) -> commandOptions.add(new CommandOption(c, null)));
        logger.debug("notifying framework about {} commands from {}", commandOptions.size(), mapFileName);
        this.commandDescriptionProvider.setCommandOptions(targetChannelUID, commandOptions);
    }
}
