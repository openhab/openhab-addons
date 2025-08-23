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
package org.openhab.binding.shelly.internal.discovery;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;
import static org.openhab.core.thing.Thing.PROPERTY_MODEL_ID;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.api2.ShellyBluEventDataDTO.Shelly2NotifyBluEventData;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ShellyThingCreator} maps the device id into the thing type id
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyThingCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShellyThingCreator.class);

    public static ThingUID getThingUID(String serviceName) {
        return getThingUID(serviceName, "", "");
    }

    public static ThingUID getThingUID(String serviceName, String deviceType, String mode) {
        String deviceId = getDeviceIdOrThrow(serviceName);
        return new ThingUID(getThingTypeUID(serviceName, deviceType, mode), deviceId);
    }

    public static ThingUID getThingUIDForUnknown(String serviceName, String deviceType, String mode) {
        String deviceId = getDeviceIdOrThrow(serviceName);
        return new ThingUID(getThingTypeUID(THING_TYPE_SHELLYUNKNOWN_STR + "-" + deviceId, deviceType, mode), deviceId);
    }

    private static String getDeviceIdOrThrow(String serviceName) {
        String deviceId = substringAfterLast(serviceName, "-");
        if (deviceId.isEmpty()) {
            throw new IllegalArgumentException("Invalid serviceName format: " + serviceName);
        }
        return deviceId;
    }

    public static ThingTypeUID getThingTypeUID(String serviceName) {
        return getThingTypeUID(serviceName, "", "");
    }

    public static ThingTypeUID getThingTypeUID(String serviceName, String deviceType, String mode) {
        if (THING_TYPE_SHELLYPROTECTED_STR.equals(serviceName)) {
            return THING_TYPE_SHELLYPROTECTED;
        }
        String serviceNameLowerCase = serviceName.toLowerCase();
        String type = substringBefore(serviceNameLowerCase, "-");
        if (type.isEmpty()) {
            throw new IllegalArgumentException("Invalid serviceName format: " + serviceName);
        }

        if (serviceNameLowerCase.startsWith(SERVICE_NAME_SHELLYPLUG_PREFIX) && !serviceNameLowerCase.contains("plugus")
                && !serviceNameLowerCase.contains("plugsg3")) {
            // shellyplug-s needs to be mapped to shellyplugs to follow the schema
            // for the thing types: <thing type>-<mode>
            if (serviceNameLowerCase.startsWith(SERVICE_NAME_SHELLYPLUGS_PREFIX)
                    || serviceNameLowerCase.contains("-s")) {
                return THING_TYPE_SHELLYPLUGS;
            }
            if (serviceNameLowerCase.startsWith(SERVICE_NAME_SHELLYPLUGU1_PREFIX)) {
                return THING_TYPE_SHELLYPLUGU1;
            }
            return THING_TYPE_SHELLYPLUG;
        }
        if (serviceNameLowerCase.startsWith(SERVICE_NAME_SHELLYRGBW2_PREFIX)) {
            return SHELLY_MODE_COLOR.equals(mode) ? THING_TYPE_SHELLYRGBW2_COLOR : THING_TYPE_SHELLYRGBW2_WHITE;
        }

        if (!deviceType.isEmpty()) {
            Map<String, ThingTypeUID> deviceTypeMap = switch (mode) {
                case SHELLY_MODE_RELAY -> RELAY_THING_TYPE_BY_DEVICE_TYPE;
                case SHELLY_MODE_ROLLER -> ROLLER_THING_TYPE_BY_DEVICE_TYPE;
                default -> THING_TYPE_BY_DEVICE_TYPE;
            };

            ThingTypeUID res = deviceTypeMap.get(deviceType);
            if (res != null) {
                return res;
            }
        }

        return THING_TYPE_BY_SERVICE_NAME.getOrDefault(type, THING_TYPE_SHELLYUNKNOWN);
    }

    public static void addBluThing(String gateway, Shelly2NotifyBluEventData data, ShellyThingTable thingTable) {
        String model = getString(data.name);
        String bluClass = substringBefore(model, "-").toUpperCase();
        String mac = getString(data.addr).replaceAll(":", "");

        ThingTypeUID thingTypeUID = THING_TYPE_BY_DEVICE_TYPE.get(model);
        if (thingTypeUID == null) {
            thingTypeUID = THING_TYPE_BY_DEVICE_TYPE.get(bluClass);
        }
        if (thingTypeUID == null) {
            LOGGER.debug("{}: Unsupported BLU device model {}, MAC={}", gateway, model, mac);
            return;
        }

        String serviceName = getBluServiceName(getString(data.name), mac);
        Map<String, Object> properties = new TreeMap<>();
        addProperty(properties, PROPERTY_MODEL_ID, model);
        addProperty(properties, PROPERTY_SERVICE_NAME, serviceName);
        addProperty(properties, PROPERTY_DEV_NAME, data.name);
        addProperty(properties, PROPERTY_DEV_TYPE, thingTypeUID.getId());
        addProperty(properties, PROPERTY_DEV_GEN, "BLU");
        addProperty(properties, PROPERTY_GW_DEVICE, gateway);
        addProperty(properties, CONFIG_DEVICEADDRESS, mac);

        LOGGER.debug("{}: Create thing {} for BLU device {} / {}", gateway, thingTypeUID, model, mac);
        thingTable.discoveredResult(thingTypeUID, model, serviceName, mac, properties);
    }

    private static void addProperty(Map<String, Object> properties, String key, @Nullable String value) {
        properties.put(key, value != null ? value : "");
    }

    /**
     * Generates a service name based on the provided model name and MAC address.
     * Delimiters will be stripped from the returned MAC address.
     *
     * @param name Model name such as SBBT-02C or just SBDW
     * @param mac MAC address with or without colon delimiters
     * @return service name in the form <code>&lt;service name&gt;-&lt;mac&gt;</code>
     */
    public static String getBluServiceName(String model, String mac) throws IllegalArgumentException {
        String bluClass = model.contains("-") ? substringBefore(model, "-") : model;
        ThingTypeUID uid = THING_TYPE_BY_DEVICE_TYPE.containsKey(model) ? THING_TYPE_BY_DEVICE_TYPE.get(model)
                : THING_TYPE_BY_DEVICE_TYPE.get(bluClass);
        if (uid != null) {
            String serviceName = uid.getId();
            if (!serviceName.isEmpty()) {
                return serviceName + "-" + mac.replaceAll(":", "").toLowerCase();
            }
        }

        throw new IllegalArgumentException("Unsupported BLU device model " + model);
    }
}
