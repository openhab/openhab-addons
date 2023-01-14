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
package org.openhab.binding.magentatv.internal;

import static org.openhab.binding.magentatv.internal.MagentaTVUtil.substringAfterLast;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.magentatv.internal.handler.MagentaTVHandler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MagentaTVDeviceManager} class manages the device table (shared between HandlerFactory and Thing handlers).
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@Component(service = MagentaTVDeviceManager.class)
public class MagentaTVDeviceManager {
    private final Logger logger = LoggerFactory.getLogger(MagentaTVDeviceManager.class);

    protected class MagentaTVDevice {
        protected String udn = "";
        protected String mac = "";
        protected String deviceId = "";
        protected String ipAddress = "";
        protected Map<String, String> properties = new HashMap<>();
        protected @Nullable MagentaTVHandler thingHandler;
    }

    private final Map<String, MagentaTVDevice> deviceList = new HashMap<>();

    public void registerDevice(String udn, String deviceId, String ipAddress, MagentaTVHandler handler) {
        logger.trace("Register new device, UDN={}, deviceId={}, ipAddress={}", udn, deviceId, ipAddress);
        addNewDevice(udn, deviceId, ipAddress, "", new TreeMap<String, String>(), handler);
    }

    private void addNewDevice(String udn, String deviceId, String ipAddress, String macAddress,
            Map<String, String> discoveryProperties, @Nullable MagentaTVHandler handler) {
        String mac = "";
        if (macAddress.isEmpty()) { // build MAC from UDN
            mac = substringAfterLast(udn, "-");
        } else {
            mac = macAddress;
        }

        boolean newDev = false;
        synchronized (deviceList) {
            MagentaTVDevice dev;
            if (deviceList.containsKey(udn.toUpperCase())) {
                dev = deviceList.get(udn.toUpperCase());
            } else {
                dev = new MagentaTVDevice();
                newDev = true;
            }
            dev.udn = udn.toUpperCase();
            dev.mac = mac.toUpperCase();
            if (!deviceId.isEmpty()) {
                dev.deviceId = deviceId.toUpperCase();
            }
            dev.ipAddress = ipAddress;
            dev.properties = discoveryProperties;
            dev.thingHandler = handler;
            if (newDev) {
                deviceList.put(dev.udn, dev);
            }
        }
        logger.debug("New device {}: (UDN={} ,deviceId={}, ipAddress={}, macAddress={}), now {} devices.",
                newDev ? "added" : "updated", udn, deviceId, ipAddress, mac, deviceList.size());
    }

    /**
     * Remove a device from the table
     *
     * @param deviceId
     */
    public void removeDevice(String deviceId) {
        MagentaTVDevice dev = lookupDevice(deviceId);
        if (dev != null) {
            synchronized (deviceList) {
                logger.trace("Device with UDN {} removed from table, new site={}", dev.udn, deviceList.size());
                deviceList.remove(dev.udn);
            }
        }
    }

    /**
     * Lookup a device in the table by an id (this could be the UDN, the MAC
     * address, the IP address or a unique device ID)
     *
     * @param uniqueId
     * @return
     */
    public @Nullable MagentaTVDevice lookupDevice(String uniqueId) {
        MagentaTVDevice dev = null;
        logger.trace("Lookup device, uniqueId={}", uniqueId);
        int i = 0;
        for (String key : deviceList.keySet()) {
            synchronized (deviceList) {
                if (deviceList.containsKey(key)) {
                    dev = deviceList.get(key);
                    logger.trace("Devies[{}]: deviceId={}, UDN={}, ipAddress={}, macAddress={}", i++, dev.deviceId,
                            dev.udn, dev.ipAddress, dev.mac);
                    if (dev.udn.equalsIgnoreCase(uniqueId) || dev.ipAddress.equalsIgnoreCase(uniqueId)
                            || dev.deviceId.equalsIgnoreCase(uniqueId) || dev.mac.equalsIgnoreCase(uniqueId)) {
                        return dev;
                    }
                }
            }
        }
        logger.debug("Device with id {} was not found in table ({} entries", uniqueId, deviceList.size());
        return null;
    }

    /**
     * returned the discovered properties
     *
     * @param udn Unique ID from UPnP discovery
     * @return property map with discovered properties
     */
    public @Nullable Map<String, String> getDiscoveredProperties(String udn) {
        if (deviceList.containsKey(udn.toUpperCase())) {
            MagentaTVDevice dev = deviceList.get(udn.toUpperCase());
            return dev.properties;
        }
        if (!deviceList.isEmpty()) {
            logger.debug("getDiscoveredProperties(): Unknown UDN: {}", udn);
        }
        return null;
    }

    public int numberOfDevices() {
        return deviceList.size();
    }
}
