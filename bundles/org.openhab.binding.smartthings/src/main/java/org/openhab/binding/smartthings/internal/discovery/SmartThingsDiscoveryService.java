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
import org.openhab.binding.smartthings.internal.dto.SmartThingsCapability;
import org.openhab.binding.smartthings.internal.dto.SmartThingsCategory;
import org.openhab.binding.smartthings.internal.dto.SmartThingsComponent;
import org.openhab.binding.smartthings.internal.dto.SmartThingsDevice;
import org.openhab.binding.smartthings.internal.handler.SmartThingsBridgeHandler;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistry;
import org.openhab.binding.smartthings.internal.type.UidUtils;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * SmartThings discovery service
 *
 * @author Bob Raker - Initial contribution
 * @author Laurent Arnal - review code for new API
 */
@NonNullByDefault
public class SmartThingsDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {

    private static final int DISCOVERY_TIMEOUT_SEC = 30;

    private final Pattern findIllegalChars = Pattern.compile("[^A-Za-z0-9_-]");

    private final Logger logger = LoggerFactory.getLogger(SmartThingsDiscoveryService.class);

    private @Nullable SmartThingsBridgeHandler smartThingsBridgeHandler;
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
        SmartThingsBridgeHandler bridge = smartThingsBridgeHandler;
        if (bridge == null) {
            return;
        }
        logger.trace("Start Discovery");

        SmartThingsApi api = bridge.getSmartThingsApi();
        if (api != null) {
            SmartThingsDevice[] devices = api.getAllDevices();

            for (SmartThingsDevice device : devices) {
                registerDevice(device, addDevice);
            }
        }

        logger.trace("End Discovery");
    }

    public void registerDevice(String devDesc, Boolean addDevice) {
        Gson gson = new Gson();
        SmartThingsDevice dev = gson.fromJson(devDesc, SmartThingsDevice.class);
        if (dev != null) {
            registerDevice(dev, addDevice);
        }
    }

    public void registerDevice(SmartThingsDevice device, Boolean addDevice) {
        logger.trace("Find Device : {} / {}", device.name, device.label);

        if (device.components == null || device.components.length == 0) {
            return;
        }

        String deviceCategory = "";
        for (SmartThingsComponent component : device.components) {
            String compId = component.id;

            if (component.categories != null && component.categories.length > 0) {
                for (SmartThingsCategory cat : component.categories) {
                    String catId = cat.name;

                    if (SmartThingsBindingConstants.GROUP_ID_MAIN.equals(compId)) {
                        deviceCategory = catId;
                    }
                }
            }
        }

        if (deviceCategory == null || deviceCategory.isEmpty()) {
            logger.debug("unknow device, bypass");
            return;
        }

        deviceCategory = deviceCategory.toLowerCase(Locale.ROOT);
        deviceCategory = UidUtils.sanitizeId(deviceCategory);

        String deviceType = device.name;
        if (deviceType == null) {
            deviceType = device.deviceTypeName;
        }
        if (deviceType == null) {
            logger.warn("Unexpectedly received data for device {} with no type", device.deviceId);
            return;
        }

        deviceType = UidUtils.sanitizeId(deviceType);
        SmartThingsTypeRegistry registry = this.typeRegistry;
        SmartThingsBridgeHandler bridgeHandler = smartThingsBridgeHandler;
        boolean useDynamicThings = bridgeHandler != null && bridgeHandler.useDynamicThings();
        String staticThingTypeId = getStaticThingTypeId(deviceCategory, device);

        if (registry != null && useDynamicThings && staticThingTypeId == null) {
            registry.register(deviceCategory, deviceType, device);
        }
        if (addDevice) {
            String thingTypeId = getThingTypeId(deviceCategory, deviceType, staticThingTypeId, useDynamicThings);
            if (thingTypeId != null) {
                createDevice(deviceCategory, thingTypeId, deviceType, Objects.requireNonNull(device));
            }
        }
    }

    private @Nullable String getThingTypeId(String deviceCategory, String deviceType,
            @Nullable String staticThingTypeId, boolean useDynamicThings) {
        if (staticThingTypeId != null) {
            return staticThingTypeId;
        }
        if (useDynamicThings) {
            return deviceType;
        }
        logger.debug(
                "No static SmartThings thing type for category {} and device type {}. Enable dynamic thing discovery to discover this device.",
                deviceCategory, deviceType);
        return null;
    }

    private @Nullable String getStaticThingTypeId(String deviceCategory, SmartThingsDevice device) {
        switch (deviceCategory) {
            case "air_conditioner":
            case "airconditioner":
                return SmartThingsBindingConstants.THING_TYPE_SAMSUNG_ROOM_A_C.getId();
            case "illuminance_sensor":
            case "light_sensor":
            case "lightsensor":
                return SmartThingsBindingConstants.THING_TYPE_GENERIC_LIGHT_SENSOR.getId();
            case "light":
            case "light_bulb":
            case "lightbulb":
                return hasCapability(device, SmartThingsBindingConstants.CAPA_COLOR_CONTROL)
                        ? SmartThingsBindingConstants.THING_TYPE_GENERIC_COLOR_LIGHT_BULB.getId()
                        : SmartThingsBindingConstants.THING_TYPE_GENERIC_LIGHT_BULB.getId();
            case "oven":
                return SmartThingsBindingConstants.THING_TYPE_SAMSUNG_OVEN.getId();
            case "presence":
            case "presence_sensor":
            case "presencesensor":
                return SmartThingsBindingConstants.THING_TYPE_GENERIC_PRESENCE_SENSOR.getId();
            case "sound_bar":
            case "soundbar":
                return SmartThingsBindingConstants.THING_TYPE_SAMSUNG_SOUNDBAR.getId();
            case "television":
            case "tv":
                return isSamsungTheFrame(device) ? SmartThingsBindingConstants.THING_TYPE_SAMSUNG_THE_FRAME.getId()
                        : SmartThingsBindingConstants.THING_TYPE_GENERIC_TELEVISION.getId();
            case "washer":
            case "washing_machine":
            case "washingmachine":
                return SmartThingsBindingConstants.THING_TYPE_GENERIC_WASHER.getId();
            default:
                return null;
        }
    }

    private boolean isSamsungTheFrame(SmartThingsDevice device) {
        String deviceType = device.name != null ? device.name : device.deviceTypeName;
        return deviceType != null && SmartThingsBindingConstants.THING_TYPE_SAMSUNG_THE_FRAME.getId()
                .equals(UidUtils.sanitizeId(deviceType));
    }

    private boolean hasCapability(SmartThingsDevice device, String capabilityId) {
        SmartThingsComponent[] components = device.components;
        if (components == null) {
            return false;
        }

        for (SmartThingsComponent component : components) {
            SmartThingsCapability[] capabilities = component.capabilities;
            if (capabilities == null) {
                continue;
            }
            for (SmartThingsCapability capability : capabilities) {
                if (capabilityId.equals(capability.id)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Create a device with the data from the SmartThings account
     *
     * @param deviceCategory SmartThings category from the main component
     * @param thingTypeId openHAB Thing type ID used for the discovery result
     * @param deviceType SmartThings device type reported by the API
     * @param device Device data from the account
     */
    private void createDevice(String deviceCategory, String thingTypeId, String deviceType, SmartThingsDevice device) {
        logger.trace("Discovery: Creating device: ThingType {} with name {}", thingTypeId, device.name);

        // Build the UID as a string "smartthings:{ThingType}:{BridgeName}:{DeviceName}"
        String label = device.label; // Note: this is necessary for null analysis to work
        if (label == null) {
            logger.warn(
                    "Unexpectedly received data for a device with no label. Check the SmartThings account and make sure every device has a name.");
            return;
        }
        String deviceNameNoSpaces = label.replaceAll("\\s", "_");
        String smartthingsDeviceName = findIllegalChars.matcher(deviceNameNoSpaces).replaceAll("");
        SmartThingsBridgeHandler bridgeHandler = smartThingsBridgeHandler;
        if (bridgeHandler != null) {
            ThingUID bridgeUid = bridgeHandler.getThing().getUID();
            String bridgeId = bridgeUid.getId();
            String uidStr = String.format("smartthings:%s:%s:%s", thingTypeId, bridgeId, smartthingsDeviceName);

            Map<String, Object> properties = new HashMap<>();
            properties.put(SmartThingsBindingConstants.DEVICE_ID, device.deviceId);
            properties.put(SmartThingsBindingConstants.DEVICE_NAME, device.name);
            properties.put(SmartThingsBindingConstants.DEVICE_CATEGORY, deviceCategory);
            properties.put(SmartThingsBindingConstants.DEVICE_TYPE, deviceType);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(new ThingUID(uidStr))
                    .withProperties(properties).withRepresentationProperty(SmartThingsBindingConstants.DEVICE_ID)
                    .withBridge(bridgeUid).withLabel(label).build();

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
            this.smartThingsBridgeHandler = smartthingsBridgeHandler;
            smartthingsBridgeHandler.registerDiscoveryListener(this);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return smartThingsBridgeHandler;
    }
}
