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
package org.openhab.binding.folderwatcher.internal.handler;

import static org.openhab.binding.folderwatcher.internal.FolderWatcherBindingConstants.CHANNEL_NEWFILE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.folderwatcher.internal.api.S3Actions;
import org.openhab.binding.folderwatcher.internal.api.exception.APIException;
import org.openhab.binding.folderwatcher.internal.common.WatcherCommon;
import org.openhab.binding.folderwatcher.internal.config.S3BucketWatcherConfiguration;
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
 * The {@link S3BucketWatcherHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexandr Salamatov - Initial contribution
 */
@NonNullByDefault
public class S3BucketWatcherHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(S3BucketWatcherHandler.class);
    private S3BucketWatcherConfiguration config = new S3BucketWatcherConfiguration();
    private File currentS3ListingFile = new File(OpenHAB.getUserDataFolder() + File.separator + "FolderWatcher"
            + File.separator + thing.getUID().getAsString().replace(':', '_') + ".data");
    private @Nullable ScheduledFuture<?> executionJob;
    private List<String> previousS3Listing = new ArrayList<>();
    private HttpClientFactory httpClientFactory;
    private @Nullable S3Actions s3;

    public S3BucketWatcherHandler(Thing thing, HttpClientFactory httpClientFactory) {
        super(thing);
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Channel {} triggered with command {}", channelUID.getId(), command);
        if (command instanceof RefreshType) {
            refreshS3BucketInformation();
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(S3BucketWatcherConfiguration.class);
        logger.debug(
                "Initializing S3 Bucket Watcher handler for {} with bucket: {}, region: {}, anonymous: {}, path: {}, poll interval: {}s",
                thing.getUID(), config.s3BucketName, config.awsRegion, config.s3Anonymous, config.s3Path,
                config.pollIntervalS3);
        try {
            if (config.s3Anonymous) {
                logger.debug("Creating S3 connection with anonymous access");
                s3 = new S3Actions(httpClientFactory, config.s3BucketName, config.awsRegion);
            } else {
                logger.debug("Creating S3 connection with AWS credentials");
                s3 = new S3Actions(httpClientFactory, config.s3BucketName, config.awsRegion, config.awsKey,
                        config.awsSecret);
            }
        } catch (APIException e) {
            logger.debug("S3 initialization error: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, e.getMessage());
            return;
        }

        try {
            logger.debug("Initializing S3 listing file for bucket: {}", config.s3BucketName);
            previousS3Listing = WatcherCommon.initStorage(currentS3ListingFile, config.s3BucketName);
            logger.debug("Loaded {} previous S3 files from storage", previousS3Listing.size());
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            logger.debug("Can't write file {}: {}", currentS3ListingFile, e.getMessage());
            return;
        }

        if (refreshS3BucketInformation()) {
            if (config.pollIntervalS3 > 0) {
                logger.debug("Starting S3 bucket refresh with {} second interval", config.pollIntervalS3);
                executionJob = scheduler.scheduleWithFixedDelay(this::refreshS3BucketInformation, config.pollIntervalS3,
                        config.pollIntervalS3, TimeUnit.SECONDS);
            } else {
                logger.debug("Polling interval invalid: {}", config.pollIntervalS3);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Polling interval must be greater then 0 seconds");
                return;
            }
        }
    }

    private boolean refreshS3BucketInformation() {
        logger.debug("Refreshing S3 bucket information for {} with path {}", config.s3BucketName, config.s3Path);
        List<String> currentS3Listing = new ArrayList<>();
        try {
            currentS3Listing = s3.listBucket(config.s3Path);
            logger.debug("S3 bucket scan found {} total files", currentS3Listing.size());
            updateStatus(ThingStatus.ONLINE);
            List<String> difS3Listing = new ArrayList<>(currentS3Listing);
            difS3Listing.removeAll(previousS3Listing);
            logger.debug("Detected {} new S3 files since last refresh", difS3Listing.size());
            difS3Listing.forEach(file -> {
                logger.debug("Triggering CHANNEL_NEWFILE with: {}", file);
                triggerChannel(CHANNEL_NEWFILE, file);
            });

            if (!difS3Listing.isEmpty()) {
                logger.debug("Saving {} new files to listing file", difS3Listing.size());
                WatcherCommon.saveNewListing(difS3Listing, currentS3ListingFile);
            }
            logger.debug("S3 refresh completed, updated previous listing from {} to {} files", previousS3Listing.size(),
                    currentS3Listing.size());
            previousS3Listing = new ArrayList<>(currentS3Listing);
        } catch (Exception e) {
            logger.debug("Exception connecting to S3 bucket: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Can't connect to the bucket");
            return false;
        }
        return true;
    }

    @Override
    public void dispose() {
        logger.debug("Disposing S3 Bucket Watcher handler for {}", thing.getUID());
        ScheduledFuture<?> executionJob = this.executionJob;
        if (executionJob != null) {
            executionJob.cancel(true);
            this.executionJob = null;
            logger.debug("Cancelled execution job");
        }
    }
}
