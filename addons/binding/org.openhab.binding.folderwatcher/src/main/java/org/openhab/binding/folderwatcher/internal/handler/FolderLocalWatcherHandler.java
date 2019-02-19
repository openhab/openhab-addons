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

import static org.openhab.binding.folderwatcher.internal.FolderWatcherBindingConstants.CHANNEL_LOCALFILENAME;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
import org.openhab.binding.folderwatcher.internal.config.FolderLocalWatcherConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FolderLocalWatcherHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexandr Salamatov - Initial contribution
 */
@NonNullByDefault
public class FolderLocalWatcherHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FolderLocalWatcherHandler.class);

    @Nullable
    private FolderLocalWatcherConfiguration config;
    @Nullable
    private WatcherCommon FileTools = new WatcherCommon();
    @Nullable
    private File currentLocalListingFile;
    @Nullable
    private ScheduledFuture<?> executionJob;
    @Nullable
    private ArrayList<String> currentLocalListing = new ArrayList<String>();
    @Nullable
    private ArrayList<String> previousLocalListing = new ArrayList<String>();

    public FolderLocalWatcherHandler(Thing thing) {
        super(thing);

        currentLocalListingFile = new File(ConfigConstants.getUserDataFolder() + File.separator + "FolderWatcher"
                + File.separator + thing.getUID().getAsString().replace(':', '_') + ".data");

        try {
            previousLocalListing = FileTools.InitStorage(currentLocalListingFile);
        } catch (IOException e) {
            logger.debug("Can't write file {}.", currentLocalListingFile);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_LOCALFILENAME.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {

            }

        }
    }

    @Override
    public void initialize() {

        config = getConfigAs(FolderLocalWatcherConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);

        if (config.pollIntervalLocal > 0) {
            updateStatus(ThingStatus.ONLINE);
            scheduler.execute(() -> {
                executionJob = scheduler.scheduleWithFixedDelay(periodicExecutionRunnable, 0, config.pollIntervalLocal,
                        TimeUnit.SECONDS);
            });
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }

    }

    @Override
    public void dispose() {
        executionJob.cancel(true);

    }
    /*
     * private void listNewFiles() throws IOException {
     *
     * ArrayList<String> diffFtpListing = new ArrayList<String>();
     *
     * if (!currentLocalListing.isEmpty()) {
     * FileWriter fileWriter = new FileWriter(currentLocalListingFile);
     *
     * for (String newFtpFile : currentLocalListing) {
     * fileWriter.write(newFtpFile + "\n");
     * }
     *
     * fileWriter.close();
     *
     * diffFtpListing = (ArrayList<String>) currentLocalListing.clone();
     * diffFtpListing.removeAll(previousLocalListing);
     *
     * for (String newFtpFile : diffFtpListing) {
     *
     * triggerChannel(CHANNEL_LOCALFILENAME, newFtpFile);
     *
     * try {
     * Thread.sleep(3000);
     * } catch (InterruptedException e) {
     *
     * }
     * }
     * previousLocalListing = (ArrayList<String>) currentLocalListing.clone();
     * }
     * }
     */

    void listFiles(String path, boolean listHidden, boolean recursive) throws IOException {
        File f = null;
        File[] fileList;

        try {

            // create new file
            f = new File(path);

            // array of files and directory

            fileList = f.listFiles(new FileFilter() {
                @Override
                public boolean accept(@Nullable File fileRaw) {
                    if (listHidden) {
                        return true;
                    } else {
                        return !fileRaw.isHidden();
                    }
                }
            });

            // for each name in the path array
            if (fileList != null && fileList.length > 0) {
                for (File file : fileList) {

                    if (file.isDirectory()) {

                        if (recursive) {
                            listFiles(file.getAbsolutePath(), listHidden, recursive);
                        }
                    }
                    if (file.isFile()) {
                        // logger.debug(file.getAbsolutePath());
                        currentLocalListing.add(file.getAbsolutePath());
                    }
                }
            }

        } catch (Exception e) {
            throw e;
        }
    }

    protected Runnable periodicExecutionRunnable = new Runnable() {
        @Override
        public void run() {

            String RootDir = config.localDir;
            ArrayList<String> diffLocalListing = new ArrayList<String>();

            /*
             * if (ftpRootDir.endsWith("/")) {
             * ftpRootDir = ftpRootDir.substring(0, ftpRootDir.length() - 1);
             * }
             *
             * if (!ftpRootDir.startsWith("/")) {
             * ftpRootDir = "/" + ftpRootDir;
             * }
             */
            currentLocalListing.clear();

            try {
                listFiles(RootDir, config.listHiddenLocal, config.listRecursiveLocal);
            } catch (IOException e) {

                logger.debug("Can't read directory: {}", config.localDir);
            }

            try {
                diffLocalListing = FileTools.listNewFiles(previousLocalListing, currentLocalListing,
                        currentLocalListingFile);
            } catch (IOException e1) {

                logger.debug("Error geeting new file names");
            }

            for (String newLocalFile : diffLocalListing) {

                triggerChannel(CHANNEL_LOCALFILENAME, newLocalFile);

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e2) {

                }
            }

            previousLocalListing = (ArrayList<String>) currentLocalListing.clone();

        }
    };

}