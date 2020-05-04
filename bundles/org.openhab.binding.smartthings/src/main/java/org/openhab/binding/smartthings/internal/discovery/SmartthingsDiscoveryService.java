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
package org.openhab.binding.smartthings.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.smartthings.internal.SmartthingsBindingConstants;
import org.openhab.binding.smartthings.internal.SmartthingsHandlerFactory;
import org.openhab.binding.smartthings.internal.dto.SmartthingsDeviceData;
import org.openhab.binding.smartthings.internal.dto.SmartthingsDiscoveryData;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Smartthings Discovery service
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
@Component(service = { DiscoveryService.class,
        EventHandler.class }, immediate = true, configurationPid = "discovery.smartthings", property = "event.topics=org/openhab/binding/smartthings/discovery")
public class SmartthingsDiscoveryService extends AbstractDiscoveryService implements EventHandler {
    private static final int SEARCH_TIME = 30;
    private static final int INITIAL_DELAY = 10; // Delay 10 sec to give time for bridge and things to be created
    private static final int SCAN_INTERVAL = 600;

    private final Pattern findIllegalChars = Pattern.compile("[^A-Za-z0-9_-]");

    private Logger logger = LoggerFactory.getLogger(SmartthingsDiscoveryService.class);

    public Gson gson;

    @Nullable
    private SmartthingsHandlerFactory smartthingsHandlerFactory;

    private SmartthingsScan scanningRunnable;
    @Nullable
    private ScheduledFuture<?> scanningJob;

    /*
     * default constructor
     */
    public SmartthingsDiscoveryService() {
        super(SmartthingsBindingConstants.SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME);

        gson = new Gson();
        this.scanningRunnable = new SmartthingsScan();
        logger.debug("Initializing discovery service with default constructor.");
    }

    @Override
    @Activate
    protected void activate(@Nullable Map<String, @Nullable Object> config) {
        super.activate(config);
    }

    @Reference
    protected void setThingHandlerFactory(ThingHandlerFactory handlerFactory) {
        logger.debug("Setting handlerFactory {}", handlerFactory);
        smartthingsHandlerFactory = (SmartthingsHandlerFactory) handlerFactory;
    }

    protected void unsetThingHandlerFactory(ThingHandlerFactory handlerFactory) {
        logger.debug("Unsetting handlerFactory");
        this.smartthingsHandlerFactory = null;
    }

    /**
     * Called from the UI when starting a search.
     */
    @Override
    public void startScan() {
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
        logger.debug("SmartthingsDiscoveryService Starting background scan");
        if (scanningJob == null || scanningJob.isCancelled()) {
            logger.debug("Starting background scanning job");
            if (scanningRunnable != null) {
                this.scanningJob = scheduler.scheduleWithFixedDelay(this.scanningRunnable, INITIAL_DELAY, SCAN_INTERVAL,
                        TimeUnit.SECONDS);
                logger.debug("Background scanning job started");
            } else {
                logger.debug("Background scanning job NOT started because the runnable has not been started yet");
            }
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
        if (smartthingsHandlerFactory != null) {
            try {
                String discoveryMsg = String.format("{\"discovery\": \"yes\", \"openHabStartTime\": %d}",
                        System.currentTimeMillis());
                smartthingsHandlerFactory.sendDeviceCommand("/discovery", discoveryMsg);
                // Smartthings will not return a response to this message but will send it's response message
                // which will get picked up by the SmartthingBridgeHandler.receivedPushMessage handler
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("Attempt to send command to the Smartthings hub failed with: {}", e.getMessage());
            }
        }
    }

    /**
     * Handle discovery data returned from the Smartthings hub.
     * The data is delivered into the SmartthingServlet. From there it is sent here via the Event service
     */
    @Override
    public void handleEvent(@Nullable Event event) {
        String topic = event.getTopic();
        String data = (String) event.getProperty("data");
        if (data == null) {
            logger.debug("Event received on topic: {} but the data field is null", topic);
            return;
        } else {
            logger.debug("Event received on topic: {}", topic);
        }

        // Two classes are required.
        // 1. SmarthingsDiscoveryData contains timing info and the discovery data which is sent as an array of Strings
        // 2. SmartthingDeviceData contains the device data for one device.
        // First the SmarthingsDiscoveryData is converted from json to java. Then each data string is converted into
        // device data
        SmartthingsDiscoveryData discoveryData = gson.fromJson(data, SmartthingsDiscoveryData.class);
        long openHabStartTime = discoveryData.openHabStartTime;
        long hubTime = discoveryData.hubTime;
        long transmissionCompleteTime = System.currentTimeMillis();

        if (discoveryData.data != null) {
            for (String deviceStr : discoveryData.data) {
                SmartthingsDeviceData deviceData = gson.fromJson(deviceStr, SmartthingsDeviceData.class);
                createDevice(deviceData);
            }
        }

        // Log processing time
        logger.info(
                "Discovery timing data, Request time until data received {}, Request time until data recieved and processed {}, Hub processing time: {} ",
                transmissionCompleteTime - openHabStartTime, System.currentTimeMillis() - openHabStartTime, hubTime);
    }

    /**
     * Create a device with the data from the Smartthings hub
     *
     * @param deviceData Device data from the hub
     */
    private void createDevice(SmartthingsDeviceData deviceData) {
        logger.info("Discovery: Creating device: ThingType {} with name {}", deviceData.capability, deviceData.name);

        // Build the UID as a string smartthings:{ThingType}:{BridgeName}:{DeviceName}
        String name = deviceData.name; // Note: this is necessary for null analysis to work
        if (name == null) {
            logger.info(
                    "Unexpectedly received data for a device with no name. Check the Smartthings hub devices and make sure every device has a name");
            return;
        }
        String deviceNameNoSpaces = name.replaceAll("\\s", "_");
        String smartthingsDeviceName = findIllegalChars.matcher(deviceNameNoSpaces).replaceAll("");
        ThingUID bridgeUid = smartthingsHandlerFactory.getBridgeHandler().getThing().getUID();
        String bridgeId = bridgeUid.getId();
        String uidStr = String.format("smartthings:%s:%s:%s", deviceData.capability, bridgeId, smartthingsDeviceName);

        Map<String, Object> properties = new HashMap<>();
        properties.put("smartthingsName", name);
        properties.put("deviceId", deviceData.getNonNullId());

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(new ThingUID(uidStr)).withProperties(properties)
                .withRepresentationProperty(deviceData.getNonNullId()).withBridge(bridgeUid).withLabel(name).build();

        thingDiscovered(discoveryResult);
    }

    /**
     * Scanning worker class.
     */
    @NonNullByDefault
    public class SmartthingsScan implements Runnable {

        /**
         * Constructor.
         *
         */
        public SmartthingsScan() {
        }

        /**
         * Poll Smartthings hub one time.
         */
        @Override
        public void run() {
            logger.debug("Starting Smartthings scan");
            sendSmartthingsDiscoveryRequest();
        }
    }
}
