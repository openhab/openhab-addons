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
package org.openhab.binding.folderwatcher.internal.handler;

import static org.openhab.binding.folderwatcher.internal.FolderWatcherBindingConstants.CHANNEL_NEWFILE;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.folderwatcher.internal.api.AzureActions;
import org.openhab.binding.folderwatcher.internal.common.WatcherCommon;
import org.openhab.binding.folderwatcher.internal.config.AzureBlobWatcherConfiguration;
import org.openhab.core.OpenHAB;
import org.openhab.core.io.net.http.HttpClientFactory;
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
 * The {@link AzureBlobWatcherHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexandr Salamatov - Initial contribution
 */
@NonNullByDefault
public class AzureBlobWatcherHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(AzureBlobWatcherHandler.class);
    private AzureBlobWatcherConfiguration config = new AzureBlobWatcherConfiguration();
    private File currentBlobListingFile = new File(OpenHAB.getUserDataFolder() + File.separator + "AzureBlob"
            + File.separator + thing.getUID().getAsString().replace(':', '_') + ".data");
    private @Nullable ScheduledFuture<?> executionJob;
    private List<String> previousBlobListing = new ArrayList<>();
    private HttpClientFactory httpClientFactory;
    private @Nullable AzureActions azure;

    public AzureBlobWatcherHandler(Thing thing, HttpClientFactory httpClientFactory) {
        super(thing);
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Channel {} triggered with command {}", channelUID.getId(), command);
        if (command instanceof RefreshType) {
            refreshAzureBlobInformation();
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(AzureBlobWatcherConfiguration.class);
        if (config.azureAccountName.isBlank() || config.azureContainerName.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Account name or container configuration is invalid");
            return;
        } else if (config.pollIntervalAzure == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Polling interval must be greater than 0 seconds");
            return;
        }

        if (config.azureAnonymous) {
            azure = new AzureActions(httpClientFactory, config.azureAccountName, config.azureContainerName);
        } else {
            azure = new AzureActions(httpClientFactory, config.azureAccountName, config.azureContainerName,
                    config.azureAccessKey);
        }
        updateStatus(ThingStatus.UNKNOWN);
        executionJob = scheduler.scheduleWithFixedDelay(this::refreshAzureBlobInformation, 0, config.pollIntervalAzure,
                TimeUnit.SECONDS);
    }

    private boolean refreshAzureBlobInformation() {
        if (previousBlobListing.size() == 0) {
            try {
                previousBlobListing = WatcherCommon.initStorage(currentBlobListingFile,
                        config.azureAccountName + "-" + config.azureContainerName);
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Local storage initialization error: " + e.getMessage());
                logger.debug("Can't write file {}: {}", currentBlobListingFile, e.getMessage());
                executionJob.cancel(false);
                return false;
            }
        }

        List<String> currentBlobListing = new ArrayList<>();
        try {
            currentBlobListing = azure.listContainer(config.contanerPath);
            updateStatus(ThingStatus.ONLINE);
            List<String> difBlobListing = new ArrayList<>(currentBlobListing);
            difBlobListing.removeAll(previousBlobListing);
            difBlobListing.forEach(file -> triggerChannel(CHANNEL_NEWFILE, file));

            if (!difBlobListing.isEmpty()) {
                WatcherCommon.saveNewListing(difBlobListing, currentBlobListingFile);
            }
            previousBlobListing = new ArrayList<>(currentBlobListing);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Can't connect to the contaner: " + e.getMessage());
            logger.debug("Can't connect to the contaner: {}", e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> executionJob = this.executionJob;
        if (executionJob != null) {
            executionJob.cancel(true);
            this.executionJob = null;
        }
    }
}
