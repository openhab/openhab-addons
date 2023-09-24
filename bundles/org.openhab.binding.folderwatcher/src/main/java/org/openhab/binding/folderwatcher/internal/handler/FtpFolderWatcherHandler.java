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
        ScheduledFuture<?> executionJob = this.executionJob;
        ScheduledFuture<?> initJob = this.initJob;
        if (executionJob != null) {
            executionJob.cancel(true);
            this.executionJob = null;
        }
        if (initJob != null) {
            initJob.cancel(true);
            this.initJob = null;
        }
        if (ftp.isConnected()) {
            try {
                ftp.logout();
                ftp.disconnect();
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
                long diff = ChronoUnit.HOURS.between(file.getTimestamp().toInstant(), dateNow);
                if (diff < config.diffHours) {
                    dirFiles.add("ftp:/" + ftpClient.getRemoteAddress() + filePath);
                }
            }
        }
    }

    private void connectionKeepAlive() {
        if (!ftp.isConnected()) {
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
                ftp.connect(config.ftpAddress, config.ftpPort);
                reply = ftp.getReplyCode();

                if (!FTPReply.isPositiveCompletion(reply)) {
                    ftp.disconnect();
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "FTP server refused connection.");
                    return;
                }
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
                if (!ftp.login(config.ftpUsername, config.ftpPassword)) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ftp.getReplyString());
                    ftp.logout();
                    return;
                }
                updateStatus(ThingStatus.ONLINE);
                ScheduledFuture<?> executionJob = this.executionJob;
                if (executionJob != null) {
                    executionJob.cancel(true);
                }
                this.executionJob = scheduler.scheduleWithFixedDelay(this::refreshFTPFolderInformation, 0,
                        config.pollInterval, TimeUnit.SECONDS);
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    private void refreshFTPFolderInformation() {
        String ftpRootDir = config.ftpDir;
        final File currentFtpListingFile = this.currentFtpListingFile;
        if (ftp.isConnected()) {
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
                List<String> diffFtpListing = new ArrayList<>(currentFtpListing);
                diffFtpListing.removeAll(previousFtpListing);
                diffFtpListing.forEach(file -> triggerChannel(CHANNEL_NEWFILE, file));
                if (!diffFtpListing.isEmpty() && currentFtpListingFile != null) {
                    try {
                        WatcherCommon.saveNewListing(diffFtpListing, currentFtpListingFile);
                    } catch (IOException e2) {
                        logger.debug("Can't save new listing into file: {}", e2.getMessage());
                    }
                }
                previousFtpListing = new ArrayList<>(currentFtpListing);
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "FTP connection lost. " + e.getMessage());
                try {
                    ftp.disconnect();
                } catch (IOException e1) {
                    logger.debug("Error disconneting, lost connection? {}", e1.getMessage());
                }
            }
        } else {
            logger.debug("FTP connection lost.");
        }
    }
}
