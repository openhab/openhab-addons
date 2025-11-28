/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Smartthings Discovery service
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartthingsDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {

    private static final int DISCOVERY_TIMEOUT_SEC = 30;

    private final Pattern findIllegalChars = Pattern.compile("[^A-Za-z0-9_-]");

    private final Logger logger = LoggerFactory.getLogger(SmartthingsDiscoveryService.class);

    private @Nullable SmartthingsBridgeHandler smartthingsBridgeHandler;
    private @Nullable SmartthingsTypeRegistry typeRegistry;

    /*
     * default constructor
     */
    public SmartthingsDiscoveryService() {
        super(SmartthingsBindingConstants.SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIMEOUT_SEC);
    }

    public void setSmartthingsTypeRegistry(SmartthingsTypeRegistry typeRegistry) {
        this.typeRegistry = typeRegistry;
    }

    /**
     * Called from the UI when starting a search.
     */
    @Override
    public void startScan() {
        try {
            doScan(true);
        } catch (SmartthingsException ex) {
            logger.error("Error during device scan: {}", ex.toString());
        }
    }

    public void doScan(Boolean addDevice) throws SmartthingsException {
        SmartthingsBridgeHandler bridge = smartthingsBridgeHandler;
        if (bridge == null) {
            return;
        }
        logger.trace("Start Discovery");

        SmartthingsApi api = bridge.getSmartthingsApi();
        SmartthingsDevice[] devices = api.getAllDevices();

        for (SmartthingsDevice device : devices) {

            String name = device.name;
            String label = device.label;

            logger.trace("Find Device : {} / {}", device.name, device.label);

            if (device.components == null || device.components.length == 0) {
                return;
            }

            Boolean enabled = false;
            if ("Four".equals(label)) {
                enabled = false;
            }
            if ("Petrole".equals(label)) {
                enabled = false;
            }
            if (label.contains("cuisson")) {
                enabled = false;
            }

            if (label.contains("Plug")) {
                enabled = true;
            }

            enabled = true;

            if (!enabled) {
                continue;
            }

            String deviceType = null;
            for (SmartthingsComponent component : device.components) {
                String compId = component.id;

                if (component.categories != null && component.categories.length > 0) {
                    for (SmartthingsCategory cat : component.categories) {
                        String catId = cat.name;

                        if (SmartthingsBindingConstants.GROUPD_ID_MAIN.equals(compId)) {
                            deviceType = catId;
                        }
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

        logger.trace("End Discovery");
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
        if (smartthingsBridgeHandler != null) {
            ThingUID bridgeUid = smartthingsBridgeHandler.getThing().getUID();
            String bridgeId = bridgeUid.getId();
            String uidStr = String.format("smartthings:%s:%s:%s", deviceType, bridgeId, smartthingsDeviceName);

            Map<String, Object> properties = new HashMap<>();
            properties.put(SmartthingsBindingConstants.SMARTTHINGS_NAME, name);
            properties.put(SmartthingsBindingConstants.DEVICE_ID, device.deviceId);
            properties.put(SmartthingsBindingConstants.DEVICE_LABEL, device.label);
            properties.put(SmartthingsBindingConstants.DEVICE_NAME, device.name);

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

    @Override
    public void deactivate() {
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof SmartthingsBridgeHandler smartthingsBridgeHandler) {
            this.smartthingsBridgeHandler = smartthingsBridgeHandler;
            this.smartthingsBridgeHandler.registerDiscoveryListener(this);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return smartthingsBridgeHandler;
    }
}
