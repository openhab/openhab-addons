/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.folderwatcher.internal.handler;

import static org.openhab.binding.folderwatcher.internal.FolderWatcherBindingConstants.CHANNEL_NEWFILE;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.folderwatcher.internal.common.WatcherCommon;
import org.openhab.binding.folderwatcher.internal.config.LocalFolderWatcherConfiguration;
import org.openhab.core.OpenHAB;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LocalFolderWatcherHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexandr Salamatov - Initial contribution
 */
@NonNullByDefault
public class LocalFolderWatcherHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(LocalFolderWatcherHandler.class);
    private LocalFolderWatcherConfiguration config = new LocalFolderWatcherConfiguration();
    private File currentLocalListingFile = new File(OpenHAB.getUserDataFolder() + File.separator + "FolderWatcher"
            + File.separator + thing.getUID().getAsString().replace(':', '_') + ".data");
    private @Nullable ScheduledFuture<?> executionJob;
    private List<String> previousLocalListing = new ArrayList<>();

    public LocalFolderWatcherHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Channel {} triggered with command {}", channelUID.getId(), command);
        if (command instanceof RefreshType) {
            refreshFolderInformation();
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(LocalFolderWatcherConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);

        if (!Files.isDirectory(Paths.get(config.localDir))) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Local directory is not valid");
            return;
        }
        try {
            previousLocalListing = WatcherCommon.initStorage(currentLocalListingFile, config.localDir);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            logger.debug("Can't write file {}: {}", currentLocalListingFile, e.getMessage());
            return;
        }

        if (config.pollIntervalLocal > 0) {
            updateStatus(ThingStatus.ONLINE);
            executionJob = scheduler.scheduleWithFixedDelay(this::refreshFolderInformation, config.pollIntervalLocal,
                    config.pollIntervalLocal, TimeUnit.SECONDS);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Polling interval can't be null or negative");
            return;
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> executionJob = this.executionJob;
        if (executionJob != null) {
            executionJob.cancel(true);
            this.executionJob = null;
        }
    }

    private void refreshFolderInformation() {
        final String rootDir = config.localDir;
        try {
            List<String> currentLocalListing = new ArrayList<>();

            Files.walkFileTree(Paths.get(rootDir), new FileVisitor<@Nullable Path>() {
                @Override
                public FileVisitResult preVisitDirectory(@Nullable Path dir, @Nullable BasicFileAttributes attrs)
                        throws IOException {
                    if (dir != null) {
                        if (!dir.equals(Paths.get(rootDir)) && !config.listRecursiveLocal) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(@Nullable Path file, @Nullable BasicFileAttributes attrs)
                        throws IOException {
                    if (file != null) {
                        if (Files.isHidden(file) && !config.listHiddenLocal) {
                            return FileVisitResult.CONTINUE;
                        }
                        currentLocalListing.add(file.toAbsolutePath().toString());
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(@Nullable Path file, @Nullable IOException exc)
                        throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(@Nullable Path dir, @Nullable IOException exc)
                        throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });

            List<String> diffLocalListing = new ArrayList<>(currentLocalListing);
            diffLocalListing.removeAll(previousLocalListing);
            diffLocalListing.forEach(file -> triggerChannel(CHANNEL_NEWFILE, file));

            if (!diffLocalListing.isEmpty()) {
                WatcherCommon.saveNewListing(diffLocalListing, currentLocalListingFile);
            }
            previousLocalListing = new ArrayList<>(currentLocalListing);
        } catch (IOException e) {
            logger.debug("File manipulation error: {}", e.getMessage());
        }
    }
}
