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
package org.openhab.binding.hapero.internal.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hapero.internal.HaperoBindingConstants;
import org.openhab.binding.hapero.internal.config.HaperoConfiguration;
import org.openhab.binding.hapero.internal.device.Device;
import org.openhab.binding.hapero.internal.discovery.HaperoDiscoveryService;
import org.openhab.binding.hapero.internal.ftp.FtpDirectory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.drapostolos.rdp4j.DirectoryListener;
import com.github.drapostolos.rdp4j.DirectoryPoller;
import com.github.drapostolos.rdp4j.FileAddedEvent;
import com.github.drapostolos.rdp4j.FileModifiedEvent;
import com.github.drapostolos.rdp4j.FileRemovedEvent;
import com.github.drapostolos.rdp4j.InitialContentEvent;
import com.github.drapostolos.rdp4j.InitialContentListener;
import com.github.drapostolos.rdp4j.IoErrorCeasedEvent;
import com.github.drapostolos.rdp4j.IoErrorListener;
import com.github.drapostolos.rdp4j.IoErrorRaisedEvent;

/**
 * The {@link HaperoBridgeHandler} is the bridge for hapero devices
 *
 * @author Daniel Walter - Initial contribution
 */
@NonNullByDefault
public class HaperoBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(HaperoBridgeHandler.class);
    private final Map<String, Device> deviceList = new ConcurrentHashMap<String, Device>();
    private final Set<HaperoThingHandler> devicesToNotify = ConcurrentHashMap.newKeySet();
    private @Nullable HaperoDiscoveryService discoveryService;
    private @Nullable ScheduledFuture<?> fileWatcherJob;
    private @Nullable ScheduledFuture<?> ftpTimeoutJob;
    private @Nullable WatchService watchService = null;
    private @Nullable DirectoryPoller dp = null;
    private @Nullable FtpDirectory polledDirectory = null;

    /**
     * Listens for the Events from the FTP File Watcher
     *
     * @author Daniel Walter - Initial contribution
     *
     */
    private class HaperoFtpEventListener implements DirectoryListener, IoErrorListener, InitialContentListener {

        /**
         * Constructor
         */
        private HaperoFtpEventListener() {
        }

        @Override
        public void initialContent(@Nullable InitialContentEvent event) throws InterruptedException {
            /* Ignore this event */
        }

        @Override
        public void ioErrorRaised(@Nullable IoErrorRaisedEvent event) throws InterruptedException {
            /* FTP IO Error - Set the device to offline, it will come back online with the next successful update */
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.ftp.connectionerror");
            logger.debug("ioErrorRaised");
        }

        @Override
        public void ioErrorCeased(@Nullable IoErrorCeasedEvent event) throws InterruptedException {
            /* FTP IO Error is gone, the device will come back online with the next successful update */
            logger.debug("ioErrorCeased");
        }

        @Override
        public void fileAdded(@Nullable FileAddedEvent event) throws InterruptedException {
            /* Ignore this event */
        }

        @Override
        public void fileRemoved(@Nullable FileRemovedEvent event) throws InterruptedException {
            /* Ignore this event */
        }

        @Override
        public void fileModified(@Nullable FileModifiedEvent event) throws InterruptedException {
            /* If the Filename is Upload.hld trigger a device data update */
            if (event != null) {
                if (event.getFileElement().getName().contentEquals(HaperoBindingConstants.DATA_FILENAME)) {
                    updateThings();
                }

                /* Reset the timeout monitoring */
                startFtpTimeout();
            } else {
                logger.debug("Event received but event data null");
            }
        }
    }

    /**
     * Runnable for the FileWatcher to monitor changes of
     * a file on the local file system. Used if "Filesystem" is
     * selected as the access method for the Upload.hld
     */
    Runnable fileWatcherTask = () -> {
        Boolean faultOccurred = false;
        Boolean fileUpdated = false;
        WatchKey key;
        HaperoConfiguration config = getConfigAs(HaperoConfiguration.class);

        while (!Thread.currentThread().isInterrupted()) {
            try {
                if (watchService != null) {
                    /*
                     * Obtain a key from the file watcher, returns either if the file is
                     * changed or a timeout occurred
                     */
                    key = watchService.poll(config.refreshTimeout, TimeUnit.SECONDS);
                } else {
                    faultOccurred = true;
                    break;
                }
            } catch (InterruptedException e) {
                logger.debug("Exception in FileWatcherTask: {}", e.getMessage());
                faultOccurred = true;
                break;
            }

            /*
             * watchService.poll return null, timeout occurred. Switch to OFFLINE
             * and wait for the next update
             */
            if (key == null) {
                logger.warn("Upload.hld not updated withing timeout.");

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.update.timeout");
                continue;
            } else {
                /* A key was returned, check if a modify event for the Upload.hld is present */
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind().equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                        if (event.context().toString().equals(HaperoBindingConstants.DATA_FILENAME)) {
                            fileUpdated = true;
                            logger.trace("File update detected");
                        }
                    }
                }

                /* If Upload.hld was modified, trigger a State update */
                if (fileUpdated) {
                    updateThings();
                }

                /* Either way, restart the Watcher */
                if (!key.reset()) {
                    logger.debug("Key reset failed");
                    faultOccurred = true;
                    break;
                }
            }
        }

        /* We broke out of the while(true) loop, so a fault occurred. Try to clean up and set the thing to offline */
        try {
            if (watchService != null) {
                watchService.close();
            }
        } catch (IOException e) {
            logger.debug("Exception in FileWatcherTask: {}", e.getMessage());
        }

        if (faultOccurred) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.monitoring.failed");
        }
    };

    /**
     * Constructor
     *
     * @param bridge
     */
    public HaperoBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof HaperoThingHandler) {
            devicesToNotify.remove(childHandler);
            logger.trace("Bridge child disposed");
        } else {
            logger.debug("childHandler not of type HaperoThingHandler");
        }
        super.childHandlerDisposed(childHandler, childThing);
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof HaperoThingHandler) {
            devicesToNotify.add((HaperoThingHandler) childHandler);
            logger.trace("Bridge child created");
        } else {
            logger.debug("childHandler not of type HaperoThingHandler");
        }
        super.childHandlerInitialized(childHandler, childThing);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(HaperoDiscoveryService.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void dispose() {
        cancelAllJobs();
        super.dispose();
    }

    @Override
    public void initialize() {
        boolean validConfig = true;
        boolean faultOcurred = false;
        String errorMsg = "";
        Path watchPath;

        HaperoConfiguration config = getConfigAs(HaperoConfiguration.class);

        /* Check if all configuration parameters are valid */
        if (config.accessMode.contentEquals(HaperoBindingConstants.CONFIG_ACCESS_FTP)) {
            if (config.ftpServer.isBlank()) {
                validConfig = false;
            }

            if (config.userName.isBlank()) {
                validConfig = false;
            }

            if (config.ftpPath.isBlank()) {
                validConfig = false;
            }
        } else if (config.accessMode.contentEquals(HaperoBindingConstants.CONFIG_ACCESS_FILESYSTEM)) {
            if (config.fileStoragePath.isBlank()) {
                validConfig = false;
            }
        } else {
            validConfig = false;
        }

        /*
         * If config is valid, start either a FTP Watcher or a File Watcher to get notified of new data from the device
         */
        if (validConfig) {
            if (config.accessMode.contentEquals(HaperoBindingConstants.CONFIG_ACCESS_FTP)) {
                /* Start a FTP Watcher */
                polledDirectory = new FtpDirectory(config.ftpServer, config.ftpPath, config.userName, config.password);

                dp = DirectoryPoller.newBuilder().addPolledDirectory(polledDirectory)
                        .addListener(new HaperoFtpEventListener()).enableFileAddedEventsForInitialContent()
                        .setPollingInterval(config.pollingInterval, TimeUnit.SECONDS).start();

                /* Start Timeout monitoring for the FTP Watcher */
                startFtpTimeout();
            } else if (config.accessMode.contentEquals(HaperoBindingConstants.CONFIG_ACCESS_FILESYSTEM)) {
                /* Start a file watcher */
                try {
                    watchPath = Path.of(config.fileStoragePath);
                } catch (InvalidPathException e) {
                    logger.debug("Exception in Init: {}", e.getMessage());
                    faultOcurred = true;
                    watchPath = null;
                    errorMsg = "@text/offline.monitoring.failed";
                }

                if (watchPath != null) {
                    try {
                        watchService = FileSystems.getDefault().newWatchService();
                        watchPath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                    } catch (Exception e) {
                        logger.debug("Exception in Init: {}", e.getMessage());
                        faultOcurred = true;
                        errorMsg = "@text/offline.monitoring.failed";
                    }
                } else {
                    logger.debug("Failed to create watchservice");
                    faultOcurred = true;
                    errorMsg = "@text/offline.monitoring.failed";
                }

                /* Start the File Watcher Runnable if a Watcher could be created */
                if (!faultOcurred) {
                    fileWatcherJob = scheduler.schedule(fileWatcherTask, 0, TimeUnit.SECONDS);
                }
            }

            if (!faultOcurred) {
                /*
                 * If a watcher could be created, start a discovery.
                 * The Thing status will be set to ONLINE after the first successful update
                 */
                updateStatus(ThingStatus.UNKNOWN);

                scheduler.execute(() -> {
                    updateThings();

                    if (discoveryService != null) {
                        discoveryService.startScan();
                    }
                });
                logger.trace("Bridge initialized");
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMsg);
            }
        } else {
            /* A Watcher could not be created, set Thing to OFFLINE. Manual restart is required. */
            cancelAllJobs();

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
            logger.trace("Bridge switched to offline mode because of configuration error");
        }
    }

    /**
     * Get a list of of all devices reported from the control system
     *
     * @return String List of all devices (may be empty)
     */
    public String[] getAllDevices() {
        return deviceList.keySet().toArray(String[]::new);
    }

    /**
     * Get the {@link Device} corresponding to the given deviceID
     *
     * @param deviceID ID for which to get the Device
     * @return Device or null if the Device could not be found
     */
    public @Nullable Device getDevice(String deviceID) {
        Device result = deviceList.get(deviceID);
        if (result == null) {
            logger.warn("getDevice: No device found for ID {}.", deviceID);
        }
        return result;
    }

    /**
     * Set the discoveryService
     *
     * @param discoveryService new discoveryService
     */
    public void setDiscoveryService(HaperoDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    /**
     * Update the device data from an InputStream (for FTP access)
     *
     * @param stream InputStream containing the raw device data.
     */
    private void updateDeviceList(InputStream stream) {
        String result;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            result = reader.readLine();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.ftp.read.error");
            logger.debug("Exception in updateDeviceTree: {}", e.getMessage());
            return;
        }

        if (result != null) {
            updateDeviceList(result);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.ftp.read.error");
        }
    }

    /**
     * Update the device data from an Path (for Filesystem access)
     *
     * @param path The Path to the Upload.hld
     */
    private void updateDeviceList(Path path) {
        String result;

        try {
            result = Files.readString(path);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.file.read.error");
            logger.debug("Exception in updateDeviceTree: {}", e.getMessage());
            return;
        }

        updateDeviceList(result);
    }

    /**
     * Update the device data.
     * The data is parsed to create a list of devices and the
     * data items associated with the device. The Last Update
     * Timestamp is set to the current date and time.
     * If data can not be parsed, the bridge is set to OFFLINE.
     *
     * @param dataIn A String containing the raw data received from the control system
     */
    private void updateDeviceList(String dataIn) {
        String data;
        Boolean faultOcurred = false;

        /*
         * The control system terminates the data with EOT.
         * Check if the termination is there and discard everything after it.
         * (Sometimes there is bogus data behind EOT)
         */
        int dataEnd = dataIn.indexOf("EOT");
        if (dataEnd != -1) {
            data = dataIn.substring(0, dataEnd);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.data.invalid");
            return;
        }

        /*
         * Parse the data using a regex.
         * The Regex creates two groups:
         * Group 1 holds the device identifier
         * Group 2 holds the data for that device, separated by ;
         */
        Pattern pattern = Pattern.compile("[\\[](\\w+)[\\]]([-?\\w+\\.*;]+)");
        Matcher matcher = pattern.matcher(data);

        /* Go trough all matches of the regex */
        while (matcher.find() && !faultOcurred) {
            /* Check if the groups actually contain data */
            if (matcher.group(1) != null || matcher.group(2) != null) {
                if (matcher.group(1).isBlank() || matcher.group(2).isBlank()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/offline.data.invalid");
                    faultOcurred = true;
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.data.invalid");
                faultOcurred = true;
            }

            if (!faultOcurred) {
                /* Split the data for that device into a String array on the delimiter ; */
                String[] split = matcher.group(2).split(";");

                /*
                 * Check if the list of known devices already contains the device ID.
                 * If not, add it. (This is also how new devices are discovered).
                 * Update the data items of the device.
                 */
                Device device = deviceList.get(matcher.group(1));
                if (device == null) {
                    device = new Device(split);
                    deviceList.put(matcher.group(1), device);
                } else {
                    device.setDataItems(split);
                }
            }
        }

        /*
         * If the update was successful and the bridge was offline
         * because of some earlier communication error, set it back to online now.
         */
        if (getThing().getStatus() != ThingStatus.ONLINE && !faultOcurred) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    /**
     * Gets the device data from either the FTP Server or the file system
     * and calls {@link updateDeviceList}. Afterwards, triggers an channel data update for all Things.
     */
    private void updateThings() {
        HaperoConfiguration config = getConfigAs(HaperoConfiguration.class);

        /* Get Data from local filesystem */
        if (config.accessMode.contentEquals(HaperoBindingConstants.CONFIG_ACCESS_FILESYSTEM)) {
            Path filePath = null;
            try {
                filePath = Path.of(config.fileStoragePath, HaperoBindingConstants.DATA_FILENAME);
            } catch (Exception e) {
                logger.debug("Exception in updateThings: {}", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.file.read.error");
                return;
            }

            updateDeviceList(filePath);
        } else if (config.accessMode.contentEquals(HaperoBindingConstants.CONFIG_ACCESS_FTP)) {
            /* Get data from FTP */
            InputStream stream;
            if (polledDirectory != null) {
                try {
                    stream = polledDirectory.getFileStream(HaperoBindingConstants.DATA_FILENAME);
                } catch (IOException e) {
                    logger.debug("Exception in updateThings: {}", e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/offline.ftp.read.error");
                    return;
                }

                updateDeviceList(stream);
            } else {
                logger.debug("PolledDirectory null in updateThings");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.ftpmonitoring.failed");
            }
        }

        /* Notify all things to update their channels */
        for (HaperoThingHandler handler : devicesToNotify) {
            handler.updateAllChannels();
            handler.updateThingProperties();
        }
    }

    /**
     * Starts a Runnable with delay time refreshTimeout.
     * If the Runnable is triggered, set the Bridge status to offline.
     * The Runnable is restarted every time a FTP File update is detected.
     */
    private void startFtpTimeout() {
        HaperoConfiguration config = getConfigAs(HaperoConfiguration.class);

        if (null != ftpTimeoutJob) {
            ftpTimeoutJob.cancel(true);
        }

        ftpTimeoutJob = scheduler.schedule(() -> {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.update.timeout");
        }, config.refreshTimeout, TimeUnit.SECONDS);
    }

    /**
     * Cancel all background tasks upon disposal of the bridge.
     */
    private void cancelAllJobs() {
        if (ftpTimeoutJob != null) {
            ftpTimeoutJob.cancel(true);
            ftpTimeoutJob = null;
        }

        if (fileWatcherJob != null) {
            fileWatcherJob.cancel(true);
            fileWatcherJob = null;
        }

        if (dp != null) {
            dp.stop();
            dp = null;
        }

        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                logger.debug("Exception in cancelAllJobs: {}", e.getMessage());
            }
            watchService = null;
        }
    }
}
