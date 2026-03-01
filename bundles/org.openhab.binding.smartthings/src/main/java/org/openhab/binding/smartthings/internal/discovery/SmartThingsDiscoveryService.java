/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.binding.smartthings.internal.api.SmartThingsApi;
import org.openhab.binding.smartthings.internal.dto.SmartThingsCategory;
import org.openhab.binding.smartthings.internal.dto.SmartThingsComponent;
import org.openhab.binding.smartthings.internal.dto.SmartThingsDevice;
import org.openhab.binding.smartthings.internal.handler.SmartThingsBridgeHandler;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistry;
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
 * SmartThings Discovery service
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartThingsDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {

    private static final int DISCOVERY_TIMEOUT_SEC = 30;

    private final Pattern findIllegalChars = Pattern.compile("[^A-Za-z0-9_-]");

    private final Logger logger = LoggerFactory.getLogger(SmartThingsDiscoveryService.class);

    private @Nullable SmartThingsBridgeHandler smartthingsBridgeHandler;
    private @Nullable SmartThingsTypeRegistry typeRegistry;

    /*
     * default constructor
     */
    public SmartThingsDiscoveryService() {
        super(SmartThingsBindingConstants.SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIMEOUT_SEC);
    }

    public void setSmartThingsTypeRegistry(SmartThingsTypeRegistry typeRegistry) {
        this.typeRegistry = typeRegistry;
    }

    /**
     * Called from the UI when starting a search.
     */
    @Override
    public void startScan() {
        try {
            doScan(true);
        } catch (SmartThingsException ex) {
            logger.error("Error during device scan: {}", ex.toString());
        }
    }

    public void doScan(Boolean addDevice) throws SmartThingsException {
        SmartThingsBridgeHandler bridge = smartthingsBridgeHandler;
        if (bridge == null) {
            return;
        }
        logger.trace("Start Discovery");

        SmartThingsApi api = bridge.getSmartThingsApi();
        SmartThingsDevice[] devices = api.getAllDevices();

        for (SmartThingsDevice device : devices) {
            registerDevice(device, addDevice);

        }

        // doTest(addDevice);

        logger.trace("End Discovery");
    }

    public void registerDevice(SmartThingsDevice device, Boolean addDevice) {
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
            return;
        }

        String deviceType = null;
        for (SmartThingsComponent component : device.components) {
            String compId = component.id;

            if (component.categories != null && component.categories.length > 0) {
                for (SmartThingsCategory cat : component.categories) {
                    String catId = cat.name;

                    if (SmartThingsBindingConstants.GROUPD_ID_MAIN.equals(compId)) {
                        deviceType = catId;
                    }
                }
            }
        }

        if (deviceType == null) {
            logger.info("unknow device, bypass");
            return;
        }

        if ("white-and-color-ambiance".equals(name)) {
            return;
        }

        deviceType = deviceType.toLowerCase(Locale.ROOT);

        SmartThingsTypeRegistry registry = this.typeRegistry;
        if (registry != null) {
            registry.register(deviceType, device);
        }
        if (addDevice) {
            createDevice(deviceType, Objects.requireNonNull(device));
        }
    }

    /**
     * Create a device with the data from the SmartThings hub
     *
     * @param deviceData Device data from the hub
     */
    private void createDevice(String deviceType, SmartThingsDevice device) {
        logger.trace("Discovery: Creating device: ThingType {} with name {}", deviceType, device.name);

        // Build the UID as a string smartthings:{ThingType}:{BridgeName}:{DeviceName}
        String name = device.label; // Note: this is necessary for null analysis to work
        if (name == null) {
            logger.info(
                    "Unexpectedly received data for a device with no name. Check the SmartThings hub devices and make sure every device has a name");
            return;
        }
        String deviceNameNoSpaces = name.replaceAll("\\s", "_");
        String smartthingsDeviceName = findIllegalChars.matcher(deviceNameNoSpaces).replaceAll("");
        SmartThingsBridgeHandler bridgeHandler = smartthingsBridgeHandler;
        if (bridgeHandler != null) {
            ThingUID bridgeUid = bridgeHandler.getThing().getUID();
            String bridgeId = bridgeUid.getId();
            String uidStr = String.format("smartthings:%s:%s:%s", deviceType, bridgeId, smartthingsDeviceName);

            Map<String, Object> properties = new HashMap<>();
            properties.put(SmartThingsBindingConstants.SMARTTHINGS_NAME, name);
            properties.put(SmartThingsBindingConstants.DEVICE_ID, device.deviceId);
            properties.put(SmartThingsBindingConstants.DEVICE_LABEL, device.label);
            properties.put(SmartThingsBindingConstants.DEVICE_NAME, device.name);

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

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof SmartThingsBridgeHandler smartthingsBridgeHandler) {
            this.smartthingsBridgeHandler = smartthingsBridgeHandler;
            smartthingsBridgeHandler.registerDiscoveryListener(this);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return smartthingsBridgeHandler;
    }
}
