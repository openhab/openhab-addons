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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.folderwatcher.internal.common.WatcherCommon;
import org.openhab.binding.folderwatcher.internal.config.FtpFolderWatcherConfiguration;
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
 * The {@link FtpFolderWatcherHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexandr Salamatov - Initial contribution
 */
@NonNullByDefault
public class FtpFolderWatcherHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(FtpFolderWatcherHandler.class);
    private FtpFolderWatcherConfiguration config = new FtpFolderWatcherConfiguration();
    private @Nullable File currentFtpListingFile;
    private @Nullable ScheduledFuture<?> executionJob, initJob;
    private FTPClient ftp = new FTPClient();
    private List<String> previousFtpListing = new ArrayList<>();

    public FtpFolderWatcherHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Channel {} triggered with command {}", channelUID.getId(), command);
        if (command instanceof RefreshType) {
            refreshFTPFolderInformation();
        }
    }

    @Override
    public void initialize() {
        File currentFtpListingFile;
        config = getConfigAs(FtpFolderWatcherConfiguration.class);
        logger.debug(
                "Initializing FTP Folder Watcher handler for {} server {} with timeout {}ms, port {}, poll interval {}s",
                thing.getUID(), config.ftpAddress, config.connectionTimeout * 1000, config.ftpPort,
                config.pollInterval);
        updateStatus(ThingStatus.UNKNOWN);
        if (config.connectionTimeout <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Connection timeout can't be negative");
            return;
        }
        if (config.ftpPort < 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "FTP port can't be negative");
            return;
        }
        if (config.pollInterval <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Polling interval can't be null or negative");
        }

        currentFtpListingFile = new File(OpenHAB.getUserDataFolder() + File.separator + "FolderWatcher" + File.separator
                + thing.getUID().getAsString().replace(':', '_') + ".data");
        try {
            this.currentFtpListingFile = currentFtpListingFile;
            previousFtpListing = WatcherCommon.initStorage(currentFtpListingFile, config.ftpAddress + config.ftpDir);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            logger.debug("Can't write file {}, error message {}", currentFtpListingFile, e.getMessage());
            return;
        }
        this.initJob = scheduler.scheduleWithFixedDelay(this::connectionKeepAlive, 0, config.pollInterval,
                TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing FTP Folder Watcher handler for {}", thing.getUID());
        ScheduledFuture<?> executionJob = this.executionJob;
        ScheduledFuture<?> initJob = this.initJob;
        if (executionJob != null) {
            executionJob.cancel(true);
            this.executionJob = null;
            logger.debug("Cancelled execution job");
        }
        if (initJob != null) {
            initJob.cancel(true);
            this.initJob = null;
            logger.debug("Cancelled init job");
        }
        if (ftp.isConnected()) {
            try {
                ftp.logout();
                ftp.disconnect();
                logger.debug("FTP connection closed");
            } catch (IOException e) {
                logger.debug("Error terminating FTP connection: ", e);
            }
        }
    }

    private void listDirectory(FTPClient ftpClient, String dirPath, boolean recursive, List<String> dirFiles)
            throws IOException {
        Instant dateNow = Instant.now();
        for (FTPFile file : ftpClient.listFiles(dirPath)) {
            String currentFileName = file.getName();
            Instant fileTimestamp = file.getTimestamp().toInstant();
            logger.trace("Processing FTP file: {}, isDirectory: {}, timestamp: {}", currentFileName, file.isDirectory(),
                    fileTimestamp);
            if (".".equals(currentFileName) || "..".equals(currentFileName)) {
                continue;
            }
            String filePath = dirPath + "/" + currentFileName;
            if (file.isDirectory()) {
                if (recursive) {
                    try {
                        listDirectory(ftpClient, filePath, recursive, dirFiles);
                    } catch (IOException e) {
                        logger.debug("Can't read FTP directory: {}", filePath, e);
                    }
                }
            } else {
                long diff = ChronoUnit.HOURS.between(fileTimestamp, dateNow);
                logger.trace("FTP file {} is {} hours old", filePath, diff);
                if (diff < config.diffHours) {
                    logger.trace("File {} is newer than {} hours ({} hours), adding to list", filePath,
                            config.diffHours, diff);
                    dirFiles.add("ftp:/" + ftpClient.getRemoteAddress() + filePath);
                }
            }
        }
    }

    private void connectionKeepAlive() {
        logger.debug("FTP connection check");
        if (!ftp.isConnected()) {
            logger.debug("FTP connection not active, attempting connection to {}:{}", config.ftpAddress,
                    config.ftpPort);
            switch (config.secureMode) {
                case NONE:
                    ftp = new FTPClient();
                    break;
                case IMPLICIT:
                    ftp = new FTPSClient(true);
                    break;
                case EXPLICIT:
                    ftp = new FTPSClient(false);
                    break;
            }

            int reply = 0;
            ftp.setListHiddenFiles(config.listHidden);
            ftp.setConnectTimeout(config.connectionTimeout * 1000);

            try {
                logger.debug("Connecting to FTP server {}", config.ftpAddress);
                ftp.connect(config.ftpAddress, config.ftpPort);
                reply = ftp.getReplyCode();
                logger.debug("FTP connection reply code: {}", reply);

                if (!FTPReply.isPositiveCompletion(reply)) {
                    logger.debug("FTP server refused connection with reply code {}", reply);
                    ftp.disconnect();
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "FTP server refused connection.");
                    return;
                }
                logger.debug("FTP connection established successfully");
            } catch (IOException e) {
                if (ftp.isConnected()) {
                    try {
                        ftp.disconnect();
                    } catch (IOException e2) {
                        logger.debug("Error disconneting, lost connection? : {}", e2.getMessage());
                    }
                }
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                return;
            }
            try {
                logger.debug("Attempting FTP login");
                if (!ftp.login(config.ftpUsername, config.ftpPassword)) {
                    logger.debug("FTP login failed: {}", ftp.getReplyString());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ftp.getReplyString());
                    ftp.logout();
                    return;
                }
                logger.debug("FTP login successful");
                if (verifyFTPConnection()) {
                    logger.debug("FTP connection verified successfully");
                    updateStatus(ThingStatus.ONLINE);
                    ScheduledFuture<?> executionJob = this.executionJob;
                    if (executionJob != null) {
                        executionJob.cancel(true);
                    }
                    this.executionJob = scheduler.scheduleWithFixedDelay(this::refreshFTPFolderInformation, 0,
                            config.pollInterval, TimeUnit.SECONDS);
                } else {
                    logger.debug("FTP connection verification failed");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "FTP connection verification failed");
                    ftp.logout();
                }
            } catch (IOException e) {
                logger.debug("IOException during FTP login: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        } else {
            logger.debug("FTP client already connected, skipping connection attempt");
        }
    }

    private boolean verifyFTPConnection() {
        try {
            String testDir = config.ftpDir;
            if (testDir == null || testDir.isEmpty()) {
                testDir = "/";
            }
            if (testDir.endsWith("/")) {
                testDir = testDir.substring(0, testDir.length() - 1);
            }
            if (!testDir.startsWith("/")) {
                testDir = "/" + testDir;
            }
            logger.debug("Verifying FTP connection by listing directory: {}", testDir);
            FTPFile[] files = ftp.listFiles(testDir);
            if (files != null) {
                logger.debug("FTP connection verification successful, found {} items in {}", files.length, testDir);
                return true;
            } else {
                logger.debug("FTP list command returned null for directory: {}", testDir);
                return false;
            }
        } catch (IOException e) {
            logger.debug("FTP connection verification failed with exception: {}", e.getMessage(), e);
            return false;
        }
    }

    private void refreshFTPFolderInformation() {
        logger.debug("Refreshing FTP folder information for {}", config.ftpAddress + config.ftpDir);
        String ftpRootDir = config.ftpDir;
        final File currentFtpListingFile = this.currentFtpListingFile;
        if (ftp.isConnected()) {
            logger.debug("FTP connection active, proceeding with directory refresh");
            ftp.enterLocalPassiveMode();
            try {
                if (ftpRootDir.endsWith("/")) {
                    ftpRootDir = ftpRootDir.substring(0, ftpRootDir.length() - 1);
                }
                if (!ftpRootDir.startsWith("/")) {
                    ftpRootDir = "/" + ftpRootDir;
                }
                List<String> currentFtpListing = new ArrayList<>();
                listDirectory(ftp, ftpRootDir, config.listRecursiveFtp, currentFtpListing);
                logger.debug("FTP directory scan found {} total files", currentFtpListing.size());
                List<String> diffFtpListing = new ArrayList<>(currentFtpListing);
                diffFtpListing.removeAll(previousFtpListing);
                logger.debug("Detected {} new FTP files since last refresh", diffFtpListing.size());
                diffFtpListing.forEach(file -> {
                    logger.trace("Triggering CHANNEL_NEWFILE with: {}", file);
                    triggerChannel(CHANNEL_NEWFILE, file);
                });
                if (!diffFtpListing.isEmpty() && currentFtpListingFile != null) {
                    try {
                        logger.debug("Saving {} new files to listing file", diffFtpListing.size());
                        WatcherCommon.saveNewListing(diffFtpListing, currentFtpListingFile);
                    } catch (IOException e2) {
                        logger.debug("Can't save new listing into file: {}", e2.getMessage());
                    }
                }
                logger.debug("FTP refresh completed, updated previous listing from {} to {} files",
                        previousFtpListing.size(), currentFtpListing.size());
                previousFtpListing = new ArrayList<>(currentFtpListing);
            } catch (IOException e) {
                logger.debug("IOException during FTP directory listing: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "FTP connection lost. " + e.getMessage());
                try {
                    ftp.disconnect();
                } catch (IOException e1) {
                    logger.debug("Error disconnecting, lost connection? {}", e1.getMessage());
                }
            }
        } else {
            logger.debug("FTP connection not active, skipping refresh");
        }
    }
}
