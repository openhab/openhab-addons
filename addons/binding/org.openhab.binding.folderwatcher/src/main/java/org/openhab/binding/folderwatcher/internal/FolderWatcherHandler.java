/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.folderwatcher.internal;

import static org.openhab.binding.folderwatcher.internal.FolderWatcherBindingConstants.CHANNEL_1;

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
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
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

    private ScheduledFuture<?> executionJob, initJob;
    private FTPClient ftp;
    // private ArrayList<FTPFile> currentFtpListing = new ArrayList<FTPFile>();
    private ArrayList<String> currentFtpListing = new ArrayList<String>();
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

                logger.debug("Trying to create file {}.", currentFtpListingFile);
                Files.createDirectories(currentFtpListingFile.toPath().getParent());

                FileWriter fileWriter = new FileWriter(currentFtpListingFile);
                fileWriter.write("INIT");

                fileWriter.close();
            } else {

                previousFtpListing = (ArrayList<String>) Files
                        .readAllLines(currentFtpListingFile.toPath().toAbsolutePath());

            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
        }

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_1.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void initialize() {
        // logger.debug("Start initializing!");
        config = getConfigAs(FolderWatcherConfiguration.class);

        // hoursBack = ((BigDecimal) getConfig().get("diffHours")).intValue();

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly. Also, before leaving this method a thing
        // status from one of ONLINE, OFFLINE or UNKNOWN must be set. This might already be the real thing status in
        // case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        if (config.pollInterval > 0 && config.pollInterval != null) {

            scheduler.execute(() -> {

                // int polling_interval = ((BigDecimal) getConfig().get("pollInterval")).intValue();

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

        // logger.debug("Finished initializing!");

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
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
                // TODO Auto-generated catch block
                // e.printStackTrace();
            }
        }
    }

    private void listDirectory(FTPClient ftpClient, String parentDir, String currentDir, int level) throws IOException {
        String dirToList = parentDir;
        Calendar fileTimestamp = null;
        Calendar cal = Calendar.getInstance();
        // Calendar calDiff = null;
        if (!currentDir.equals("")) {
            dirToList += "/" + currentDir;
        }
        // FTPFile[] subFiles = ftpClient.mlistDir(dirToList);
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
                for (int i = 0; i < level; i++) {
                    // System.out.print("\t");
                }
                if (aFile.isDirectory()) {
                    // System.out.println("[" + currentFileName + "]");
                    listDirectory(ftpClient, dirToList, currentFileName, level + 1);
                } else {
                    // System.out.println(currentFileName);
                    // updateState(CHANNEL_1, new StringType(currentFileName));
                    // aFile.setName(name);
                    fileTimestamp = aFile.getTimestamp();

                    long diff = ChronoUnit.HOURS.between(fileTimestamp.toInstant(), cal.toInstant());

                    if (diff < config.diffHours) {
                        aFile.setName(dirToList + "/" + currentFileName);
                        currentFtpListing.add(aFile.getName());

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

            // filelist = "Test files " + Integer.toString(rand.nextInt(100)) + " " +
            // Integer.toString(currentFtpListing.size());

            for (String newFtpFile : diffFtpListing) {

                updateState(CHANNEL_1, new StringType(newFtpFile));

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    // e.printStackTrace();
                }

            }

            previousFtpListing = (ArrayList<String>) currentFtpListing.clone();
        }

    }

    protected Runnable periodicConnectRunnable = new Runnable() {

        // String server = getConfig().get("ftpAddress").toString();
        // String username = getConfig().get("ftpUsername").toString();
        // String password = getConfig().get("ftpPassword").toString();
        // int timeout = ((BigDecimal) getConfig().get("connectionTimeout")).intValue();
        // boolean showHidden = (boolean) getConfig().get("listHidden");
        int reply;

        @Override
        public void run() {

            if (ftp == null || ftp.isConnected() == false) {

                if (config.connectionTimeout <= 0) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
                    return;
                }
                // boolean error = false;

                ftp = new FTPClient();
                reply = 0;

                ftp.setListHiddenFiles(config.listHidden);
                ftp.setConnectTimeout(config.connectionTimeout * 1000);
                // ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out), true));

                // final FTPClientConfig config;
                // config = new FTPClientConfig();
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

        // FTPFile[] f;
        // String fileList = "";
        // Random rand = new Random();
        // Calendar modDate = null;

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
                    // TODO Auto-generated catch block
                    // e.printStackTrace();
                    logger.debug("FTP connection lost.");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "FTP connection lost.");
                    try {
                        ftp.disconnect();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                    }

                }
                // if (f != null){
                // System.out.println(f.toFormattedString("CDT"));
                // }

            } else {

                // logger.debug("FTP connection lost.");
                // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "FTP connection lost.");

                // executionJob.cancel(true);

            }
        }
    };

}