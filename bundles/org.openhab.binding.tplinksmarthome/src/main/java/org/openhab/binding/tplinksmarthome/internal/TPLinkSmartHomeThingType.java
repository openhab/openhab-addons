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
package org.openhab.binding.tplinksmarthome.internal;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * ThingType enum with all supported TP-Link Smart Home devices.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 *
 */
@NonNullByDefault
public enum TPLinkSmartHomeThingType {

    // Bulb Thing Type UIDs
    KB100("kb100", DeviceType.BULB),
    KB130("kb130", DeviceType.BULB),
    LB100("lb100", DeviceType.BULB),
    LB110("lb110", DeviceType.BULB),
    LB120("lb120", DeviceType.BULB),
    LB130("lb130", DeviceType.BULB),
    LB200("lb200", DeviceType.BULB),
    LB230("lb230", DeviceType.BULB),
    KL110("kl110", DeviceType.BULB),
    KL120("kl120", DeviceType.BULB),
    KL130("kl130", DeviceType.BULB),

    // Plug Thing Type UIDs
    HS100("hs100", DeviceType.PLUG),
    HS103("hs103", DeviceType.PLUG),
    HS105("hs105", DeviceType.PLUG),
    HS110("hs110", DeviceType.PLUG),
    KP100("kp100", DeviceType.PLUG),

    // Switch Thing Type UIDs
    HS200("hs200", DeviceType.SWITCH),
    HS210("hs210", DeviceType.SWITCH),

    // Dimmer Thing Type UIDs
    HS220("hs220", DeviceType.DIMMER),

    // Power Strip Thing Type UIDs.
    HS107("hs107", DeviceType.STRIP),
    HS300("hs300", DeviceType.STRIP),
    KP200("kp200", DeviceType.STRIP),
    KP400("kp400", DeviceType.STRIP),

    // Range Extender Thing Type UIDs
    RE270K("re270", DeviceType.RANGE_EXTENDER),
    RE370K("re370", DeviceType.RANGE_EXTENDER);

    /**
     * All supported Smart Home devices in a list.
     */
    public static final List<TPLinkSmartHomeThingType> SUPPORTED_THING_TYPES_LIST = Arrays
            .asList(TPLinkSmartHomeThingType.values());

    /**
     * All {@link ThingTypeUID}s of all supported Smart Home devices.
     */
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = SUPPORTED_THING_TYPES_LIST.stream()
            .map(TPLinkSmartHomeThingType::thingTypeUID).collect(Collectors.toSet());

    /**
     * A map of all {@link TPLinkSmartHomeThingType} mapped to {@link ThingTypeUID}.
     */
    public static final Map<ThingTypeUID, TPLinkSmartHomeThingType> THING_TYPE_MAP = SUPPORTED_THING_TYPES_LIST.stream()
            .collect(Collectors.toMap(TPLinkSmartHomeThingType::thingTypeUID, Function.identity()));
    private static final List<TPLinkSmartHomeThingType> BULB_WITH_TEMPERATURE_COLOR_1 = Stream.of(LB120, KL120)
            .collect(Collectors.toList());
    private static final List<TPLinkSmartHomeThingType> BULB_WITH_TEMPERATURE_COLOR_2 = Stream
            .of(KB130, KL130, LB130, LB230).collect(Collectors.toList());

    private ThingTypeUID thingTypeUID;
    private DeviceType type;

    TPLinkSmartHomeThingType(final String name, final DeviceType type) {
        thingTypeUID = new ThingTypeUID(TPLinkSmartHomeBindingConstants.BINDING_ID, name);
        this.type = type;
    }

    /**
     * @return Returns the type of the device.
     */
    public DeviceType getDeviceType() {
        return type;
    }

    /**
     * @return The {@link ThingTypeUID} of this device.
     */
    public ThingTypeUID thingTypeUID() {
        return thingTypeUID;
    }

    /**
     * Returns true if the given {@link ThingTypeUID} matches a device that is a bulb with color temperature ranges 1
     * (2700 to 6500k).
     *
     * @param thingTypeUID if the check
     * @return true if it's a bulb device with color temperature range 1
     */
    public static boolean isBulbDeviceWithTemperatureColor1(ThingTypeUID thingTypeUID) {
        return isDevice(thingTypeUID, BULB_WITH_TEMPERATURE_COLOR_1);
    }

    /**
     * Returns true if the given {@link ThingTypeUID} matches a device that is a bulb with color temperature ranges 2
     * (2500 to 9000k).
     *
     * @param thingTypeUID if the check
     * @return true if it's a bulb device with color temperature range 2
     */
    public static boolean isBulbDeviceWithTemperatureColor2(ThingTypeUID thingTypeUID) {
        return isDevice(thingTypeUID, BULB_WITH_TEMPERATURE_COLOR_2);
    }

    private static boolean isDevice(ThingTypeUID thingTypeUID, List<TPLinkSmartHomeThingType> thingTypes) {
        return thingTypes.stream().anyMatch(t -> t.is(thingTypeUID));
    }

    /**
     * Returns true if the given {@link ThingTypeUID} matches the {@link ThingTypeUID} in this enum.
     *
     * @param otherThingTypeUID to check
     * @return true if matches
     */
    public boolean is(ThingTypeUID otherThingTypeUID) {
        return thingTypeUID.equals(otherThingTypeUID);
    }

    /**
     * Enum indicating the type of the device.
     */
    public enum DeviceType {
        /**
         * Light Bulb device.
         */
        BULB,
        /**
         * Dimmer device.
         */
        DIMMER,
        /**
         * Plug device.
         */
        PLUG,
        /**
         * Wi-Fi range extender device with plug.
         */
        RANGE_EXTENDER,
        /**
         * Power strip device.
         */
        STRIP,
        /**
         * Switch device.
         */
        SWITCH
    }
}
