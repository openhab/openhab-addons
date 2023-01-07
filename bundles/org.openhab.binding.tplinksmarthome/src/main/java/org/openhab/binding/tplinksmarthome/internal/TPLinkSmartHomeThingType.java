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
package org.openhab.binding.tplinksmarthome.internal;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeBindingConstants.ColorScales;
import org.openhab.core.thing.ThingTypeUID;

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
    KB130("kb130", DeviceType.BULB, ColorScales.K_2500_9000),
    LB100("lb100", DeviceType.BULB),
    LB110("lb110", DeviceType.BULB),
    LB120("lb120", DeviceType.BULB, ColorScales.K_2700_6500),
    LB130("lb130", DeviceType.BULB, ColorScales.K_2500_9000),
    LB200("lb200", DeviceType.BULB),
    LB230("lb230", DeviceType.BULB, ColorScales.K_2500_9000),
    KL50("kl50", DeviceType.BULB),
    KL60("kl60", DeviceType.BULB),
    KL110("kl110", DeviceType.BULB),
    KL120("kl120", DeviceType.BULB, ColorScales.K_2700_6500),
    KL125("kl125", DeviceType.BULB, ColorScales.K_2500_6500),
    KL130("kl130", DeviceType.BULB, ColorScales.K_2500_9000),
    KL135("kl135", DeviceType.BULB, ColorScales.K_2500_6500),

    // Light String thing Type UIDs.
    KL400("kl400", DeviceType.LIGHT_STRIP, ColorScales.K_2500_9000),
    KL430("kl430", DeviceType.LIGHT_STRIP, ColorScales.K_2500_9000),

    // Plug Thing Type UIDs
    EP10("ep10", DeviceType.PLUG),
    HS100("hs100", DeviceType.PLUG),
    HS103("hs103", DeviceType.PLUG),
    HS105("hs105", DeviceType.PLUG),
    HS110("hs110", DeviceType.PLUG_WITH_ENERGY),
    KP100("kp100", DeviceType.PLUG),
    KP105("kp105", DeviceType.PLUG),
    KP115("kp115", DeviceType.PLUG_WITH_ENERGY),
    KP125("kp125", DeviceType.PLUG_WITH_ENERGY),
    KP401("kp401", DeviceType.PLUG),

    // Switch Thing Type UIDs
    HS200("hs200", DeviceType.SWITCH),
    HS210("hs210", DeviceType.SWITCH),

    // Dimmer Thing Type UIDs
    ES20M("es20m", DeviceType.DIMMER),
    HS220("hs220", DeviceType.DIMMER),
    KS230("ks230", DeviceType.DIMMER),
    KP405("kp405", DeviceType.DIMMER),

    // Power Strip Thing Type UIDs.
    EP40("ep40", DeviceType.STRIP, 2),
    HS107("hs107", DeviceType.STRIP, 2),
    HS300("hs300", DeviceType.STRIP, 6),
    KP200("kp200", DeviceType.STRIP, 2),
    KP303("kp303", DeviceType.STRIP, 3),
    KP400("kp400", DeviceType.STRIP, 2),

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

    private final ThingTypeUID thingTypeUID;
    private final DeviceType type;
    private final ColorScales colorScales;
    private final int sockets;

    TPLinkSmartHomeThingType(final String name, final DeviceType type) {
        this(name, type, 0);
    }

    TPLinkSmartHomeThingType(final String name, final DeviceType type, final ColorScales colorScales) {
        this(name, type, colorScales, 0);
    }

    TPLinkSmartHomeThingType(final String name, final DeviceType type, final int sockets) {
        this(name, type, ColorScales.NOT_SUPPORTED, sockets);
    }

    TPLinkSmartHomeThingType(final String name, final DeviceType type, final ColorScales colorScales,
            final int sockets) {
        thingTypeUID = new ThingTypeUID(TPLinkSmartHomeBindingConstants.BINDING_ID, name);
        this.type = type;
        this.colorScales = colorScales;
        this.sockets = sockets;
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
     * @return Returns the number of sockets. Only for Strip devices.
     */
    public int getSockets() {
        return sockets;
    }

    /**
     * @return Returns the color temperature color scales if supported or else returns null
     */
    public ColorScales getColorScales() {
        return colorScales;
    }

    /**
     * Returns true if the given {@link ThingTypeUID} matches the {@link ThingTypeUID} in this enum.
     *
     * @param otherThingTypeUID to check
     * @return true if matches
     */
    public boolean is(final ThingTypeUID otherThingTypeUID) {
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
         * Light Strip device.
         */
        LIGHT_STRIP,
        /**
         * Plug device.
         */
        PLUG,
        /**
         * Plug device with energy measurement support.
         */
        PLUG_WITH_ENERGY,
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
