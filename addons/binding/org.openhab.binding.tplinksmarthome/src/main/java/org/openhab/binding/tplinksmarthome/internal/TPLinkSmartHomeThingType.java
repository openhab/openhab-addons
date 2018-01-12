/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome.internal;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.tplinksmarthome.TPLinkSmartHomeBindingConstants;

/**
 * ThingType enum with all supported TP-Link Smart Home devices.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 *
 */
@NonNullByDefault
enum TPLinkSmartHomeThingType {

    // Bulb Thing Type UIDs
    KB100("kb100", DeviceType.BULB),
    KB130("kb130", DeviceType.BULB),
    LB100("lb100", DeviceType.BULB),
    LB110("lb110", DeviceType.BULB),
    LB120("lb120", DeviceType.BULB),
    LB130("lb130", DeviceType.BULB),
    LB200("lb200", DeviceType.BULB),
    LB230("lb230", DeviceType.BULB),

    // Plug Thing Type UIDs
    HS100("hs100", DeviceType.PLUG),
    HS105("hs105", DeviceType.PLUG),
    HS110("hs110", DeviceType.PLUG),
    KP100("kp100", DeviceType.PLUG),

    // Switch Thing Type UIDs
    HS200("hs200", DeviceType.SWITCH),

    // Range Extender Thing Type UIDs
    RE270K("re270", DeviceType.RANGE_EXTENDER),
    RE370K("re370", DeviceType.RANGE_EXTENDER);

    /**
     * All supported Smart Home devices in a list.
     */
    private static final List<TPLinkSmartHomeThingType> SUPPORTED_THING_TYPES_LIST = Arrays
            .asList(TPLinkSmartHomeThingType.values());

    /**
     * All {@link ThingTypeUID}s of all supported Smart Home devices.
     */
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = SUPPORTED_THING_TYPES_LIST.stream()
            .map(TPLinkSmartHomeThingType::thingTypeUID).collect(Collectors.toSet());

    private ThingTypeUID thingTypeUID;
    private DeviceType type;

    TPLinkSmartHomeThingType(String name, DeviceType type) {
        thingTypeUID = new ThingTypeUID(TPLinkSmartHomeBindingConstants.BINDING_ID, name);
        this.type = type;
    }

    /**
     * @return The {@link ThingTypeUID} of this device.
     */
    public ThingTypeUID thingTypeUID() {
        return thingTypeUID;
    }

    /**
     * Returns true if the given {@link ThingTypeUID} matches a device that is a bulb.
     *
     * @param thingTypeUID if the check
     * @return true if it's a bulb device
     */
    public static boolean isBulbDevice(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_LIST.stream().filter(t -> t.is(thingTypeUID))
                .anyMatch(t -> t.type == DeviceType.BULB);
    }

    /**
     * Returns true if the given {@link ThingTypeUID} matches a device that supports the switching communication
     * protocol.
     *
     * @param thingTypeUID if the check
     * @return true if it's a switching supporting device
     */
    public static boolean isSwitchingDevice(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_LIST.stream().filter(t -> t.is(thingTypeUID))
                .anyMatch(t -> t.type != DeviceType.BULB);
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
     * Internal enum indicating the type of the device.
     */
    private enum DeviceType {
        /**
         * Light Bulb device.
         */
        BULB,
        /**
         * Plug device.
         */
        PLUG,
        /**
         * Wi-Fi range extender device with plug.
         */
        RANGE_EXTENDER,
        /**
         * Switch device.
         */
        SWITCH;
    }
}
