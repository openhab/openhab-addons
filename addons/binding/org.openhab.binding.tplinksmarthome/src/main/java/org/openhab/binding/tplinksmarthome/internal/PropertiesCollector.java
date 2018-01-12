/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome.internal;

import static org.openhab.binding.tplinksmarthome.TPLinkSmartHomeBindingConstants.*;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.tplinksmarthome.internal.model.Sysinfo;

/**
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public final class PropertiesCollector {

    private PropertiesCollector() {
        // Util class
    }

    /**
     * Collect all properties of the thing from the {@link Sysinfo} object.
     *
     * @param thingTypeUID thing to get the properties for
     * @param ipAddress ip address of the device
     * @param sysinfo system info data returned from the device
     * @return map of properties
     */
    public static Map<String, Object> collectProperties(ThingTypeUID thingTypeUID, String ipAddress, Sysinfo sysinfo) {
        Map<String, Object> properties = new TreeMap<>();

        putNonNull(properties, CONFIG_IP, ipAddress);
        collectProperties(properties, sysinfo);
        if (TPLinkSmartHomeThingType.isBulbDevice(thingTypeUID)) {
            collectPropertiesBulb(properties, sysinfo);
        } else {
            collectPropertiesOther(properties, sysinfo);
        }
        return properties;
    }

    /**
     * Collect generic properties.
     *
     * @param properties properties object to store properties in
     * @param sysinfo system info data returned from the device
     */
    private static void collectProperties(Map<String, Object> properties, Sysinfo sysinfo) {
        putNonNull(properties, PROPERTY_MODEL, sysinfo.getModel());
        putNonNull(properties, PROPERTY_DEVICE_ID, sysinfo.getDeviceId());
        putNonNull(properties, PROPERTY_HARDWARE_VERSION, sysinfo.getHwVer());
        putNonNull(properties, PROPERTY_SOFWARE_VERSION, sysinfo.getSwVer());
        putNonNull(properties, PROPERTY_HARDWARE_ID, sysinfo.getHwId());
        putNonNull(properties, PROPERTY_OEM_ID, sysinfo.getOemId());
    }

    /**
     * Collect Smart Bulb specific properties.
     *
     * @param properties properties object to store properties in
     * @param sysinfo system info data returned from the device
     */
    private static void collectPropertiesBulb(Map<String, Object> properties, Sysinfo sysinfo) {
        putNonNull(properties, PROPERTY_TYPE, sysinfo.getMicType());
        putNonNull(properties, PROPERTY_MAC, sysinfo.getMicMac());
        putNonNull(properties, PROPERTY_PROTOCOL_NAME, sysinfo.getProtocolName());
        putNonNull(properties, PROPERTY_PROTOCOL_VERSION, sysinfo.getProtocolVersion());
    }

    /**
     * Collect Smart Switch specific properties.
     *
     * @param properties properties object to store properties in
     * @param sysinfo system info data returned from the device
     */
    private static void collectPropertiesOther(Map<String, Object> properties, Sysinfo sysinfo) {
        putNonNull(properties, PROPERTY_TYPE, sysinfo.getType());
        putNonNull(properties, PROPERTY_MAC, sysinfo.getMac());
        putNonNull(properties, PROPERTY_DEVICE_NAME, sysinfo.getDevName());
        putNonNull(properties, PROPERTY_FIRMWARE_ID, sysinfo.getFwId());
        putNonNull(properties, PROPERTY_FEATURE, sysinfo.getFeature());
    }

    private static void putNonNull(Map<String, Object> properties, String key, @Nullable String value) {
        if (value != null) {
            properties.put(key, value);
        }
    }
}
