/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.openhab.binding.folderwatcher.internal.FolderWatcherBindingConstants.CHANNEL_FILENAME;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.folderwatcher.internal.config.FolderWatcherConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FolderWatcherHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexandr Salamatov - Initial contribution
 */
// @NonNullByDefault
public class FolderWatcherHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FolderWatcherHandler.class);

    @Nullable
    private FolderWatcherConfiguration config;

    private File currentFtpListingFile;

    @Nullable
    private ScheduledFuture<?> executionJob, initJob;
    @Nullable
    private FTPClient ftp;
    @Nullable
    private ArrayList<String> currentFtpListing = new ArrayList<String>();
    @Nullable
    private ArrayList<String> previousFtpListing = new ArrayList<String>();

    public FolderWatcherHandler(Thing thing) {
        super(thing);

        currentFtpListingFile = new File(ConfigConstants.getUserDataFolder() + File.separator + "FolderWatcher"
                + File.separator + thing.getUID().getAsString().replace(':', '_') + ".data");

        InitStorage();

    }

    private void InitStorage() {

        try {
            if (!currentFtpListingFile.exists()) {

                Files.createDirectories(currentFtpListingFile.toPath().getParent());

                FileWriter fileWriter = new FileWriter(currentFtpListingFile);
                fileWriter.write("INIT");

                fileWriter.close();
            } else {

                previousFtpListing = (ArrayList<String>) Files
                        .readAllLines(currentFtpListingFile.toPath().toAbsolutePath());

            }
        } catch (IOException e) {
            logger.debug("Can't write file {}.", currentFtpListingFile);
        }

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_FILENAME.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {

            }

        }
    }

    @Override
    public void initialize() {

        config = getConfigAs(FolderWatcherConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);

        if (config.pollInterval > 0) {

            scheduler.execute(() -> {
                initJob = scheduler.scheduleWithFixedDelay(periodicConnectRunnable, 0, config.pollInterval,
                        TimeUnit.SECONDS);
            });

            scheduler.execute(() -> {
                executionJob = scheduler.scheduleWithFixedDelay(periodicExecutionRunnable, 0, config.pollInterval + 5,
                        TimeUnit.SECONDS);
            });
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }
    }

    @Override
    public void dispose() {
        executionJob.cancel(true);
        initJob.cancel(true);
        if (ftp.isConnected()) {
            try {
                ftp.logout();
                ftp.disconnect();
            } catch (IOException e) {
                logger.debug("Error terminating FTP connection");
            }
        }
    }

    private void listDirectory(FTPClient ftpClient, String parentDir, String currentDir, int level) throws IOException {
        String dirToList = parentDir;
        Calendar fileTimestamp = null;
        Calendar cal = Calendar.getInstance();
        if (!currentDir.equals("")) {
            dirToList += "/" + currentDir;
        }

        FTPFile[] subFiles = ftpClient.listFiles(dirToList);

        int reply = ftpClient.getReplyCode();

        if (!FTPReply.isPositiveCompletion(reply)) {
            logger.debug("FTP Error:" + ftpClient.getReplyString());
            return;
        }
        if (subFiles != null && subFiles.length > 0) {
            for (FTPFile aFile : subFiles) {
                String currentFileName = aFile.getName();
                if (currentFileName.equals(".") || currentFileName.equals("..")) {
                    // skip parent directory and directory itself
                    continue;
                }

                if (aFile.isDirectory()) {
                    listDirectory(ftpClient, dirToList, currentFileName, level + 1);
                } else {

                    fileTimestamp = aFile.getTimestamp();
                    long diff = ChronoUnit.HOURS.between(fileTimestamp.toInstant(), cal.toInstant());

                    if (diff < config.diffHours) {
                        aFile.setName(dirToList + "/" + currentFileName);
                        currentFtpListing.add("ftp:/" + ftpClient.getRemoteAddress().toString() + aFile.getName());

                    }
                }
            }
        }
    }

    private void listNewFiles() throws IOException {

        ArrayList<String> diffFtpListing = new ArrayList<String>();

        if (!currentFtpListing.isEmpty()) {
            FileWriter fileWriter = new FileWriter(currentFtpListingFile);

            for (String newFtpFile : currentFtpListing) {
                fileWriter.write(newFtpFile + "\n");
            }

            fileWriter.close();

            diffFtpListing = (ArrayList<String>) currentFtpListing.clone();
            diffFtpListing.removeAll(previousFtpListing);

            for (String newFtpFile : diffFtpListing) {

                triggerChannel(CHANNEL_FILENAME, newFtpFile);

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {

                }
            }
            previousFtpListing = (ArrayList<String>) currentFtpListing.clone();
        }
    }

    protected Runnable periodicConnectRunnable = new Runnable() {

        int reply;

        @Override
        public void run() {

            if (ftp == null || ftp.isConnected() == false) {

                if (config.connectionTimeout <= 0) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
                    return;
                }

                ftp = new FTPClient();
                reply = 0;

                ftp.setListHiddenFiles(config.listHidden);
                ftp.setConnectTimeout(config.connectionTimeout * 1000);
                // Uncomment to see FTP response
                // ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out), true));

                try {
                    ftp.connect(config.ftpAddress);
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
                        } catch (IOException f) {
                            // do nothing
                        }
                    }
                    logger.debug("Can't conect: {}", e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    return;
                }
                __main: try {
                    if (!ftp.login(config.ftpUsername, config.ftpPassword)) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ftp.getReplyString());
                        ftp.logout();
                        break __main;
                    }
                    updateStatus(ThingStatus.ONLINE);
                    return;
                }

                catch (FTPConnectionClosedException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                }

                catch (IOException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                }
                // end main
                return;
            }

        }
    };

    protected Runnable periodicExecutionRunnable = new Runnable() {
        @Override
        public void run() {

            if (ftp != null && ftp.isConnected()) {

                ftp.enterLocalPassiveMode();

                try {

                    String ftpRootDir = config.ftpDir;

                    if (ftpRootDir.endsWith("/")) {
                        ftpRootDir = ftpRootDir.substring(0, ftpRootDir.length() - 1);
                    }

                    if (!ftpRootDir.startsWith("/")) {
                        ftpRootDir = "/" + ftpRootDir;
                    }

                    currentFtpListing.clear();
                    listDirectory(ftp, ftpRootDir, "", 0);
                    listNewFiles();

                } catch (IOException e) {
                    logger.debug("FTP connection lost.");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "FTP connection lost.");
                    try {
                        ftp.disconnect();
                    } catch (IOException e1) {

                    }

                }

            } else {

                logger.debug("FTP connection lost.");

            }
        }
    };

}