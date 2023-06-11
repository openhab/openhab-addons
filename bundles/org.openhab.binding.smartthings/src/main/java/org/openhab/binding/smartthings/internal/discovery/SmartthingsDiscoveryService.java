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
package org.openhab.binding.smartthings.internal.discovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartthingsBindingConstants;
import org.openhab.binding.smartthings.internal.SmartthingsHubCommand;
import org.openhab.binding.smartthings.internal.dto.SmartthingsDeviceData;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
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
        EventHandler.class }, configurationPid = "discovery.smartthings", property = "event.topics=org/openhab/binding/smartthings/discovery")
public class SmartthingsDiscoveryService extends AbstractDiscoveryService implements EventHandler {
    private static final int DISCOVERY_TIMEOUT_SEC = 30;
    private static final int INITIAL_DELAY_SEC = 10; // Delay 10 sec to give time for bridge and things to be created
    private static final int SCAN_INTERVAL_SEC = 600;

    private final Pattern findIllegalChars = Pattern.compile("[^A-Za-z0-9_-]");

    private final Logger logger = LoggerFactory.getLogger(SmartthingsDiscoveryService.class);

    private final Gson gson;

    private @Nullable SmartthingsHubCommand smartthingsHubCommand;

    private @Nullable ScheduledFuture<?> scanningJob;

    /*
     * default constructor
     */
    public SmartthingsDiscoveryService() {
        super(SmartthingsBindingConstants.SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIMEOUT_SEC);
        gson = new Gson();
    }

    @Reference
    protected void setSmartthingsHubCommand(SmartthingsHubCommand hubCommand) {
        smartthingsHubCommand = hubCommand;
    }

    protected void unsetSmartthingsHubCommand(SmartthingsHubCommand hubCommand) {
        // Make sure it is this handleFactory that should be unset
        if (Objects.equals(hubCommand, smartthingsHubCommand)) {
            this.smartthingsHubCommand = null;
        }
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
        if (scanningJob == null) {
            this.scanningJob = scheduler.scheduleWithFixedDelay(this::sendSmartthingsDiscoveryRequest,
                    INITIAL_DELAY_SEC, SCAN_INTERVAL_SEC, TimeUnit.SECONDS);
            logger.debug("Discovery background scanning job started");
        }
    }

    /**
     * Stops background scanning for attached devices.
     */
    @Override
    protected void stopBackgroundDiscovery() {
        final ScheduledFuture<?> currentScanningJob = scanningJob;
        if (currentScanningJob != null) {
            currentScanningJob.cancel(false);
            scanningJob = null;
        }
    }

    /**
     * Start the discovery process by sending a discovery request to the Smartthings Hub
     */
    private void sendSmartthingsDiscoveryRequest() {
        if (smartthingsHubCommand != null) {
            try {
                String discoveryMsg = "{\"discovery\": \"yes\"}";
                smartthingsHubCommand.sendDeviceCommand("/discovery", 5, discoveryMsg);
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
        if (event == null) {
            logger.info("SmartthingsDiscoveryService.handleEvent: event is uexpectedly null");
            return;
        }
        String topic = event.getTopic();
        String data = (String) event.getProperty("data");
        if (data == null) {
            logger.debug("Event received on topic: {} but the data field is null", topic);
            return;
        } else {
            logger.trace("Event received on topic: {}", topic);
        }

        // The data returned from the Smartthings hub is a list of strings where each
        // element is the data for one device. That device string is another json object
        List<String> devices = new ArrayList<>();
        devices = gson.fromJson(data, devices.getClass());
        for (String device : devices) {
            SmartthingsDeviceData deviceData = gson.fromJson(device, SmartthingsDeviceData.class);
            createDevice(Objects.requireNonNull(deviceData));
        }
    }

    /**
     * Create a device with the data from the Smartthings hub
     *
     * @param deviceData Device data from the hub
     */
    private void createDevice(SmartthingsDeviceData deviceData) {
        logger.trace("Discovery: Creating device: ThingType {} with name {}", deviceData.capability, deviceData.name);

        // Build the UID as a string smartthings:{ThingType}:{BridgeName}:{DeviceName}
        String name = deviceData.name; // Note: this is necessary for null analysis to work
        if (name == null) {
            logger.info(
                    "Unexpectedly received data for a device with no name. Check the Smartthings hub devices and make sure every device has a name");
            return;
        }
        String deviceNameNoSpaces = name.replaceAll("\\s", "_");
        String smartthingsDeviceName = findIllegalChars.matcher(deviceNameNoSpaces).replaceAll("");
        if (smartthingsHubCommand == null) {
            logger.info("SmartthingsHubCommand is unexpectedly null, could not create device {}", deviceData);
            return;
        }
        ThingUID bridgeUid = smartthingsHubCommand.getBridgeUID();
        String bridgeId = bridgeUid.getId();
        String uidStr = String.format("smartthings:%s:%s:%s", deviceData.capability, bridgeId, smartthingsDeviceName);

        Map<String, Object> properties = new HashMap<>();
        properties.put("smartthingsName", name);
        properties.put("deviceId", deviceData.id);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(new ThingUID(uidStr)).withProperties(properties)
                .withRepresentationProperty("deviceId").withBridge(bridgeUid).withLabel(name).build();

        thingDiscovered(discoveryResult);
    }
}
