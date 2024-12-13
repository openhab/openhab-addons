/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.Objects;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartthingsBindingConstants;
import org.openhab.binding.smartthings.internal.SmartthingsHandlerFactory;
import org.openhab.binding.smartthings.internal.SmartthingsHubCommand;
import org.openhab.binding.smartthings.internal.api.SmartthingsApi;
import org.openhab.binding.smartthings.internal.dto.SmartthingsCategory;
import org.openhab.binding.smartthings.internal.dto.SmartthingsComponent;
import org.openhab.binding.smartthings.internal.dto.SmartthingsDevice;
import org.openhab.binding.smartthings.internal.handler.SmartthingsBridgeHandler;
import org.openhab.binding.smartthings.internal.type.SmartthingsException;
import org.openhab.binding.smartthings.internal.type.SmartthingsTypeRegistry;
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

    private final Pattern findIllegalChars = Pattern.compile("[^A-Za-z0-9_-]");

    private final Logger logger = LoggerFactory.getLogger(SmartthingsDiscoveryService.class);

    private @Nullable SmartthingsHubCommand smartthingsHubCommand;
    private @Nullable SmartthingsTypeRegistry typeRegistry;

    /*
     * default constructor
     */
    public SmartthingsDiscoveryService() {
        super(SmartthingsBindingConstants.SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIMEOUT_SEC);
    }

    @Reference
    protected void setSmartthingsTypeRegistry(SmartthingsTypeRegistry typeRegistry) {
        this.typeRegistry = typeRegistry;
    }

    protected void unsetSmartthingsTypeRegistry(SmartthingsTypeRegistry typeRegistry) {
        // Make sure it is this handleFactory that should be unset
        if (Objects.equals(this.typeRegistry, typeRegistry)) {
            this.typeRegistry = null;
        }
    }

    @Reference
    protected void setSmartthingsHubCommand(SmartthingsHubCommand hubCommand) {
        smartthingsHubCommand = hubCommand;
        ((SmartthingsHandlerFactory) hubCommand).setSmartthingsDiscoveryService(this);
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
        doScan(true);
    }

    public void doScan(Boolean addDevice) {
        SmartthingsBridgeHandler bridge = null;
        if (smartthingsHubCommand != null) {
            bridge = smartthingsHubCommand.getBridgeHandler();
        }
        if (bridge == null) {
            return;
        }
        SmartthingsApi api = bridge.getSmartthingsApi();

        try {
            SmartthingsDevice[] devices = api.getAllDevices();

            for (SmartthingsDevice device : devices) {

                String name = device.name;
                String label = device.label;

                logger.debug("Device");

                if (device.components == null || device.components.length == 0) {
                    return;
                }

                Boolean enabled = false;
                if ("Four".equals(label)) {
                    // enabled = true;
                }
                if ("Petrole".equals(label)) {
                    enabled = true;
                }

                // enabled = true;

                if (!enabled) {
                    continue;
                }

                String deviceType = null;
                for (SmartthingsComponent component : device.components) {
                    String compId = component.id;

                    if (component.categories != null && component.categories.length > 0) {
                        for (SmartthingsCategory cat : component.categories) {
                            String catId = cat.name;

                            if ("main".equals(compId)) {
                                deviceType = catId;
                            }

                            logger.info("");
                        }
                    }
                }

                if (deviceType == null) {
                    logger.info("unknow device, bypass");
                    continue;
                }

                if ("white-and-color-ambiance".equals(name)) {
                    continue;
                }

                deviceType = deviceType.toLowerCase();
                if (this.typeRegistry != null) {
                    this.typeRegistry.register(deviceType, device);
                }
                if (addDevice) {
                    createDevice(deviceType, Objects.requireNonNull(device));
                }

            }
        } catch (SmartthingsException ex) {
            logger.error("Unable to get devices !!");
            return;
        }

        logger.debug("End Discovery");
    }

    /**
     * Create a device with the data from the Smartthings hub
     *
     * @param deviceData Device data from the hub
     */
    private void createDevice(String deviceType, SmartthingsDevice device) {
        logger.trace("Discovery: Creating device: ThingType {} with name {}", deviceType, device.name);

        // Build the UID as a string smartthings:{ThingType}:{BridgeName}:{DeviceName}
        String name = device.label; // Note: this is necessary for null analysis to work
        if (name == null) {
            logger.info(
                    "Unexpectedly received data for a device with no name. Check the Smartthings hub devices and make sure every device has a name");
            return;
        }
        String deviceNameNoSpaces = name.replaceAll("\\s", "_");
        String smartthingsDeviceName = findIllegalChars.matcher(deviceNameNoSpaces).replaceAll("");
        if (smartthingsHubCommand != null) {
            ThingUID bridgeUid = smartthingsHubCommand.getBridgeUID();
            String bridgeId = bridgeUid.getId();
            String uidStr = String.format("smartthings:%s:%s:%s", deviceType, bridgeId, smartthingsDeviceName);

            Map<String, Object> properties = new HashMap<>();
            properties.put("smartthingsName", name);
            properties.put("deviceId", device.deviceId);
            properties.put("deviceLabel", device.label);
            properties.put("deviceName", device.name);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(new ThingUID(uidStr))
                    .withProperties(properties).withRepresentationProperty("deviceId").withBridge(bridgeUid)
                    .withLabel(name).build();

            thingDiscovered(discoveryResult);
        }
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
    }

    /**
     * Stops background scanning for attached devices.
     */
    @Override
    protected void stopBackgroundDiscovery() {
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
        /*
         * List<String> devices = new ArrayList<>();
         * devices = gson.fromJson(data, devices.getClass());
         * for (String device : devices) {
         * SmartthingsDeviceData deviceData = gson.fromJson(device, SmartthingsDeviceData.class);
         * createDevice(Objects.requireNonNull(deviceData));
         * }
         */
    }
}
