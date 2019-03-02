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
package org.openhab.binding.smartthings.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.smartthings.SmartthingsBindingConstants;
import org.openhab.binding.smartthings.handler.SmartthingsBridgeHandler;
import org.openhab.binding.smartthings.internal.SmartthingsHttpClient;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Smartthings Discovery service
 *
 * @author Bob Raker - Initial contribution
 *
 */
public class SmartthingsDiscoveryService extends AbstractDiscoveryService implements EventHandler {

    private static final int SEARCH_TIME = 30;
    private static final int INITIAL_DELAY = 5;
    private static final int SCAN_INTERVAL = 180;

    private final Pattern findIllegalChars = Pattern.compile("[^A-Za-z0-9_-]");

    private Logger logger = LoggerFactory.getLogger(SmartthingsDiscoveryService.class);

    private SmartthingsBridgeHandler bridgeHandler;

    private Gson gson;

    private SmartthingsHttpClient httpClient = null;

    private SmartthingsScan scanningRunnable;

    private ScheduledFuture<?> scanningJob;

    public SmartthingsDiscoveryService(SmartthingsBridgeHandler bridgeHandler) {
        super(SmartthingsBindingConstants.SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME);
        logger.debug("Initializing discovery service");
        this.bridgeHandler = bridgeHandler;

        // Get a Gson instance
        gson = new Gson();

        this.scanningRunnable = new SmartthingsScan(this);
        if (bridgeHandler == null) {
            logger.warn("No bridge handler for scan given");
        }
        this.activate(null);
    }

    public SmartthingsDiscoveryService() {
        super(SmartthingsBindingConstants.SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME);
        logger.debug("Initializing discovery service with default constructor");
    }

    @Override
    protected void activate(Map<String, Object> config) {
        logger.debug("SmartthingsDiscoveryService.activate() called");
    }

    protected void deactivate(ComponentContext componentContext) {
        logger.debug("SmartthingsDiscoveryService.deactivate() called");
    }

    protected void modified(ComponentContext componentContext) {
        logger.debug("SmartthingsDiscoveryService.modified() called");
    }

    /**
     * Called from the UI when starting a search.
     */
    @Override
    public void startScan() {
        logger.debug("Starting discovery scan on bridge {}", bridgeHandler.getThing().getUID());
        sendSmartthingsDiscoveryRequest();
    }

    /**
     * Stops a running scan.
     */
    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    /**
     * Starts background scanning for attached devices.
     */
    @Override
    protected void startBackgroundDiscovery() {
        if (scanningJob == null || scanningJob.isCancelled()) {
            logger.debug("Starting background scanning job");
            this.scanningJob = scheduler.scheduleWithFixedDelay(this.scanningRunnable, INITIAL_DELAY, SCAN_INTERVAL,
                    TimeUnit.SECONDS);
        } else {
            logger.debug("ScanningJob active");
        }
    }

    /**
     * Stops background scanning for attached devices.
     */
    @Override
    protected void stopBackgroundDiscovery() {
        if (scanningJob != null && !scanningJob.isCancelled()) {
            scanningJob.cancel(false);
            scanningJob = null;
        }
    }

    /**
     * Start the discovery process by sending a discovery request to the Smartthings Hub
     */
    private void sendSmartthingsDiscoveryRequest() {
        httpClient = bridgeHandler.getSmartthingsHttpClient();
        if (httpClient != null) {
            try {
                String discoveryMsg = String.format("{\"discovery\": \"yes\", \"openHabStartTime\": %d}",
                        System.currentTimeMillis());
                httpClient.sendDeviceCommand("/discovery", discoveryMsg);
                // Smartthings will not return a response to this message but will send it's response message
                // which will get picked up by the SmartthingBridgeHandler.receivedPushMessage handler
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("Attempt to send command to the Smartthings hub failed with: ", e.getMessage());
            }
        }
    }

    /**
     * Handle discovery data returned from the Smartthings hub.
     * The data is delivered into the SmartthingServlet. From there it is sent here via the Event service
     */
    @Override
    public void handleEvent(Event event) {
        String topic = event.getTopic();
        String data = (String) event.getProperty("data");
        logger.debug("Event received on topic: {}", topic);

        // Two classes are required.
        // 1. SmarthingsDiscoveryData contains timing info and the discovery data which is sent as an array of Strings
        // 2. SmartthingDeviceData contains the device data for one device.
        // First the SmarthingsDiscoveryData is converted from json to java. Then each data string is converted into
        // device data
        SmartthingsDiscoveryData discoveryData = gson.fromJson(data, SmartthingsDiscoveryData.class);
        long openHabStartTime = discoveryData.getOpenHabStartTime();
        long hubTime = discoveryData.getHubTime();
        long transmissionCompleteTime = System.currentTimeMillis();

        for (String deviceStr : discoveryData.getData()) {
            SmartthingsDeviceData deviceData = gson.fromJson(deviceStr, SmartthingsDeviceData.class);
            createDevice(deviceData);
        }

        // Log processing time
        logger.info(
                "Discovery timing data, Request time until data received {}, Request time until data recieved and processed {}, Hub processing time: {} ",
                transmissionCompleteTime - openHabStartTime, System.currentTimeMillis() - openHabStartTime, hubTime);
    }

    /**
     * Create a device with the data from the Smartthings hub
     *
     * @param deviceData
     */
    private void createDevice(SmartthingsDeviceData deviceData) {
        logger.info("Discovery: Creating device: ThingType {} with name {}", deviceData.getCapability(),
                deviceData.getName());

        // Build the UID as a string smartthings:{ThingType}:{BridgeName}:{DeviceName}
        String deviceNameNoSpaces = deviceData.getName().replaceAll("\\s", "_");
        String smartthingsDeviceName = findIllegalChars.matcher(deviceNameNoSpaces).replaceAll("");
        ThingUID bridgeUid = bridgeHandler.getThing().getUID();
        String bridgeId = bridgeUid.getId();
        String uidStr = String.format("smartthings:%s:%s:%s", deviceData.getCapability(), bridgeId,
                smartthingsDeviceName);

        Map<String, Object> properties = new HashMap<>();
        properties.put("smartthingsName", deviceData.getName());
        properties.put("deviceId", deviceData.getId());

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(new ThingUID(uidStr)).withProperties(properties)
                .withRepresentationProperty(deviceData.getId()).withBridge(bridgeUid).withLabel(deviceData.getName())
                .build();

        thingDiscovered(discoveryResult);
    }

    /**
     * Scanning worker class.
     */
    public class SmartthingsScan implements Runnable {
        /**
         * Handler for delegation to callbacks.
         */
        private SmartthingsDiscoveryService service;

        /**
         * Constructor.
         *
         * @param handler
         */
        public SmartthingsScan(SmartthingsDiscoveryService service) {
            this.service = service;
        }

        /**
         * Poll Smartthings hub one time.
         */
        @Override
        public void run() {
            logger.debug("Starting scan on bridge {}", bridgeHandler.getThing().getUID());
            sendSmartthingsDiscoveryRequest();
        }
    }
}
