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
package org.openhab.binding.smartthings.internal;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Recognized Samsung OCF appliance families.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public enum ApplianceFamily {
    AIR_CONDITIONER(SmartThingsBindingConstants.THING_TYPE_AIR_CONDITIONER, "oic.d.airconditioner"),
    AIR_PURIFIER(SmartThingsBindingConstants.THING_TYPE_AIR_PURIFIER, "oic.d.airpurifier"),
    DEHUMIDIFIER(SmartThingsBindingConstants.THING_TYPE_DEHUMIDIFIER, "oic.d.dehumidifier"),
    DRYER(SmartThingsBindingConstants.THING_TYPE_DRYER, "oic.d.dryer"),
    WASHER(SmartThingsBindingConstants.THING_TYPE_WASHER, "oic.d.washer");

    private final ThingTypeUID thingTypeUID;
    private final String deviceType;

    ApplianceFamily(ThingTypeUID thingTypeUID, String deviceType) {
        this.thingTypeUID = thingTypeUID;
        this.deviceType = deviceType;
    }

    public ThingTypeUID thingTypeUID() {
        return thingTypeUID;
    }

    public String deviceType() {
        return deviceType;
    }

    public boolean matchesDeviceTypes(String deviceTypes) {
        return Arrays.stream(deviceTypes.split(",")).map(String::trim).anyMatch(deviceType::equals);
    }

    public static @Nullable ApplianceFamily forThingType(ThingTypeUID thingTypeUID) {
        return Arrays.stream(values()).filter(family -> family.thingTypeUID.equals(thingTypeUID)).findFirst()
                .orElse(null);
    }

    public static @Nullable ApplianceFamily forDeviceTypes(String deviceTypes) {
        ApplianceFamily match = null;
        for (ApplianceFamily family : values()) {
            if (family.matchesDeviceTypes(deviceTypes)) {
                if (match != null) {
                    return null;
                }
                match = family;
            }
        }
        return match;
    }
}
