/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.folderwatcher.internal.common.WatcherCommon;
import org.openhab.binding.folderwatcher.internal.config.FolderWatcherConfiguration;
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
    private @Nullable FolderWatcherConfiguration config;
    private @Nullable File currentFtpListingFile;
    private @Nullable ScheduledFuture<?> executionJob, initJob;
    private @Nullable FTPClient ftp;
    private List<String> currentFtpListing = new ArrayList<>();
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
        config = getConfigAs(FolderWatcherConfiguration.class);
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
        if (config.ftpDir == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "FTP directory can't be empty");
            return;
        }
        if (config.pollInterval <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Polling interval can't be null or negative");
        }
        final Set<String> ftpModes = Set.of("NONE", "IMPLICIT", "EXPLICIT");
        if (!ftpModes.contains(config.secureMode)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unsupported secure mode");
            return;
        }
        currentFtpListingFile = new File(ConfigConstants.getUserDataFolder() + File.separator + "FolderWatcher"
                + File.separator + thing.getUID().getAsString().replace(':', '_') + ".data");
        try {
            previousFtpListing = WatcherCommon.initStorage(currentFtpListingFile);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            logger.debug("Can't write file {}, error message {}", currentFtpListingFile, e.getMessage());
            return;
        }
        initJob = scheduler.scheduleWithFixedDelay(this::connectionKeepAlive, 0, config.pollInterval, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        if (executionJob != null) {
            executionJob.cancel(true);
        }
        if (initJob != null) {
            initJob.cancel(true);
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

    private List<String> listDirectory(@Nullable FTPClient ftpClient, String parentDir, String currentDir,
            boolean recursive) throws IOException {
        Instant dateNow = Instant.now();
        List<String> dirList = new ArrayList<>();
        final String dirToList = parentDir + ((!currentDir.equals("")) ? ("/" + currentDir) : "");

        for (FTPFile file : ftpClient.listFiles(dirToList)) {
            String currentFileName = file.getName();
            if (currentFileName.equals(".") || currentFileName.equals("..")) {
                continue;
            }
            if (file.isDirectory()) {
                if (recursive) {
                    try {
                        dirList.addAll(listDirectory(ftpClient, dirToList, currentFileName, recursive));
                    } catch (IOException e) {
                        logger.debug("Can't read FTP directory: {}", dirToList, e);
                    }
                }
            } else {
                long diff = ChronoUnit.HOURS.between(file.getTimestamp().toInstant(), dateNow);

                if (diff < config.diffHours) {
                    file.setName(dirToList + "/" + currentFileName);
                    dirList.add("ftp:/" + ftpClient.getRemoteAddress().toString() + file.getName());
                }
            }
        }
        return dirList;
    }

    private void connectionKeepAlive() {
        if (ftp == null || ftp.isConnected() == false) {
            if (config.secureMode.equals("NONE")) {
                ftp = new FTPClient();
            } else {
                switch (config.secureMode) {
                    case "NONE":
                        ftp = new FTPClient();
                        break;
                    case "IMPLICIT":
                        ftp = new FTPSClient(true);
                        break;
                    case "EXPLICIT":
                        ftp = new FTPSClient(false);
                        break;
                }
            }

            int reply = 0;
            ftp.setListHiddenFiles(config.listHidden);
            ftp.setConnectTimeout(config.connectionTimeout * 1000);

            try {
                if (config.ftpPort > 0) {
                    ftp.connect(config.ftpAddress, config.ftpPort);
                } else {
                    ftp.connect(config.ftpAddress);
                }
                reply = ftp.getReplyCode();

                if (!FTPReply.isPositiveCompletion(reply)) {
                    ftp.disconnect();
                    logger.debug("FTP server refused connection.");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "FTP server refused connection.");
                    return;
                }
            } catch (IOException e) {
                if (ftp.isConnected()) {
                    try {
                        ftp.disconnect();
                    } catch (IOException e2) {
                        logger.debug("Error disconneting, lost connection? : ", e2);
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
                if (executionJob != null) {
                    executionJob.cancel(true);
                }
                executionJob = scheduler.scheduleWithFixedDelay(this::refreshFTPFolderInformation, 0,
                        config.pollInterval, TimeUnit.SECONDS);
                return;
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
            return;
        }
    }

    private void refreshFTPFolderInformation() {
        String ftpRootDir = config.ftpDir;
        if (ftp != null && ftp.isConnected()) {
            ftp.enterLocalPassiveMode();
            try {
                if (ftpRootDir.endsWith("/")) {
                    ftpRootDir = ftpRootDir.substring(0, ftpRootDir.length() - 1);
                }
                if (!ftpRootDir.startsWith("/")) {
                    ftpRootDir = "/" + ftpRootDir;
                }
                currentFtpListing.clear();
                currentFtpListing.addAll(listDirectory(ftp, ftpRootDir, "", config.listRecursiveFtp));
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
                logger.debug("FTP connection lost.", e);
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
