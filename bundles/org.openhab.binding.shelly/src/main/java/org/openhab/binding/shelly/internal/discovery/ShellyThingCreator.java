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

import static org.openhab.binding.shelly.internal.ShellyDevices.*;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * The {@link ShellyThingCreator} maps the device id into the thing type id
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyThingCreator {
    private static final Pattern SHELLY_SERVICE_NAME_PATTERN = Pattern
            .compile("^([a-z0-9]*shelly[a-z0-9]*)-([a-z0-9]+)$", Pattern.CASE_INSENSITIVE);

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

        // First check for special handling
        if (serviceNameLowerCase.startsWith(SERVICE_NAME_SHELLY25_PREFIX)) { // Shelly v2.5
            return getRelayOrRollerType(THING_TYPE_SHELLY25_RELAY, THING_TYPE_SHELLY25_ROLLER, mode);
        }
        if (serviceNameLowerCase.startsWith(SERVICE_NAME_SHELLY2_PREFIX)) { // Shelly v2
            return getRelayOrRollerType(THING_TYPE_SHELLY2_RELAY, THING_TYPE_SHELLY2_ROLLER, mode);
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
        if (serviceNameLowerCase.startsWith(SERVICE_NAME_SHELLYMOTION_PREFIX)) {
            // depending on firmware release the Motion advertises under shellymotion-xxx or shellymotionsensor-xxxx
            return THING_TYPE_SHELLYMOTION;
        }

        if (!deviceType.isEmpty()) {
            Map<String, ThingTypeUID> deviceTypeMap = switch (mode) {
                case SHELLY_MODE_RELAY -> THING_TYPE_CLASS_RELAY_BY_DEVICE_TYPE;
                case SHELLY_MODE_ROLLER -> THING_TYPE_CLASS_ROLLER_BY_DEVICE_TYPE;
                default -> THING_TYPE_BY_DEVICE_TYPE;
            };

            ThingTypeUID res = deviceTypeMap.get(deviceType);
            if (res != null) {
                return res;
            }
        }

        return THING_TYPE_BY_SERVICE_NAME.getOrDefault(type, THING_TYPE_SHELLYUNKNOWN);
    }

    private static ThingTypeUID getRelayOrRollerType(ThingTypeUID relayType, ThingTypeUID rollerType, String mode) {
        return SHELLY_MODE_RELAY.equals(mode) ? relayType : rollerType;
    }

    public static boolean isValidShellyServiceName(String serviceName) {
        return SHELLY_SERVICE_NAME_PATTERN.matcher(serviceName).matches();
    }
}
