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
package org.openhab.binding.magentatv.internal;

import static org.openhab.binding.magentatv.internal.MagentaTVBindingConstants.*;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.magentatv.internal.handler.MagentaTVHandler;
import org.openhab.binding.magentatv.internal.network.MagentaTVNetwork;
import org.openhab.binding.magentatv.internal.network.MagentaTVPoweroffListener;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MagentaTVHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@Component(service = { ThingHandlerFactory.class, MagentaTVHandlerFactory.class }, configurationPid = "binding."
        + BINDING_ID)
public class MagentaTVHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(MagentaTVHandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_RECEIVER);

    private final MagentaTVNetwork network = new MagentaTVNetwork();
    private @Nullable MagentaTVPoweroffListener upnpListener;
    private boolean servletInitialized = false;

    @SuppressWarnings("null")
    @NonNullByDefault
    protected class MagentaTVDevice {
        protected String udn = "";
        protected String mac = "";
        protected String deviceId = "";
        protected String ipAddress = "";
        protected Map<String, Object> properties = new HashMap<String, Object>();
        protected @Nullable MagentaTVHandler thingHandler;
    }

    private final Map<String, MagentaTVDevice> deviceList = new HashMap<String, MagentaTVDevice>();

    /**
     * Activate the bundle: save properties
     *
     * @param componentContext
     * @param configProperties set of properties from cfg (use same names as in
     *            thing config)
     */

    @Activate
    protected void activate(ComponentContext componentContext, Map<String, Object> configProperties) {
        super.activate(componentContext);
        logger.debug("Activate HandlerFactory");
    }

    /**
     * return list of supported thins
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        logger.debug("Create thing type {}", thing.getThingTypeUID().getAsString());
        if (THING_TYPE_RECEIVER.equals(thingTypeUID)) {
            try {
                return new MagentaTVHandler(this, thing, network);
            } catch (RuntimeException e) {
                logger.warn("Unable to create ThingHandler: {}", e.getMessage());
            }
        }

        return null;
    }

    /**
     * A device was discovered by UPnP. A new device gets inserted into the
     * deviceList table, otherwise the properties will be updated.
     *
     * @param discoveryProperties Properties discoverd by UPnP
     */
    @SuppressWarnings({ "null", "unused" })
    public void deviceDiscoverd(Map<String, Object> discoveryProperties) {
        String discoveredUDN = discoveryProperties.get(PROPERTY_UDN).toString().toUpperCase();
        logger.trace("Discovered device with UDN {}", discoveredUDN);
        try {
            MagentaTVDevice dev = null;
            synchronized (deviceList) {
                dev = deviceList.get(discoveredUDN);
                if (dev != null) {
                    logger.trace("Known device with UDN {}, update properties", discoveredUDN);
                    dev.properties = discoveryProperties;
                    deviceList.replace(discoveredUDN, dev);
                    if (dev.thingHandler != null) {
                        // we know the device
                        dev.thingHandler.onWakeup(dev.properties);
                    }
                } else {
                    // new device
                    String mac = StringUtils.substringAfterLast(discoveredUDN, "-");
                    String ipAddress = discoveryProperties.get(PROPERTY_IP).toString();
                    addNewDevice(discoveredUDN, null, ipAddress, mac, discoveryProperties, null);
                }
            }
        } catch (MagentaTVException | RuntimeException e) {
            logger.debug("Unable to process discovered device, UDN={} - {}", discoveredUDN, e.getMessage());
        }
    }

    /**
     * Add a device to the device table
     *
     * @param udn UDN for the device
     * @param deviceId A unique device id
     * @param ipAddress IP address of the receiver
     * @param handler The corresponding thing handler
     */
    public void registerDevice(String udn, String deviceId, String ipAddress, MagentaTVHandler handler) {
        logger.trace("Register new device, UDN={}, deviceId={}, ipAddress={}", udn, deviceId, ipAddress);
        addNewDevice(udn, deviceId, ipAddress, null, null, handler);
    }

    @SuppressWarnings("null")
    private void addNewDevice(String udn, @Nullable String deviceId, String ipAddress, @Nullable String macAddress,
            @Nullable Map<String, Object> discoveryProperties, @Nullable MagentaTVHandler handler) {
        String mac = "";
        if (macAddress == null) { // build MAC from UDN
            mac = StringUtils.substringAfterLast(udn, "-");
        } else {
            mac = macAddress;
        }

        boolean newDev;
        synchronized (deviceList) {
            MagentaTVDevice dev = deviceList.get(udn.toUpperCase());
            newDev = dev == null;
            if (newDev) {
                dev = new MagentaTVDevice();
            }
            dev.udn = udn.toUpperCase();
            dev.mac = mac.toUpperCase();
            if ((deviceId != null) && !deviceId.isEmpty()) {
                dev.deviceId = deviceId.toUpperCase();
            }
            dev.ipAddress = ipAddress;
            if (discoveryProperties != null) {
                dev.properties = discoveryProperties;
            }
            dev.thingHandler = handler;
            if (newDev) {
                deviceList.put(dev.udn, dev);
            } else {
                deviceList.replace(dev.udn, dev);
            }
        }
        logger.debug("{}: (UDN={} ,deviceId={}, ipAddress={}, macAddress={}), now {} devices.",
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
    @SuppressWarnings("null")
    private @Nullable MagentaTVDevice lookupDevice(String uniqueId) {
        @Nullable
        MagentaTVDevice dev = null;
        logger.trace("Lookup device, uniqueId={}", uniqueId);
        int i = 0;
        for (String key : deviceList.keySet()) {
            synchronized (deviceList) {
                dev = deviceList.get(key);
                Validate.notNull(dev);
                if (dev != null) {
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
    @SuppressWarnings({ "null", "unused" })
    public @Nullable Map<String, Object> getDiscoveredProperties(String udn) {
        MagentaTVDevice dev = deviceList.get(udn.toUpperCase());
        if (dev != null) {
            return dev.properties;
        }
        if (deviceList.size() > 0) {
            logger.debug("getDiscoveredProperties(): Unknown UDN: {}", udn);
        }
        return null;
    }

    public void setNotifyServletStatus(boolean newStatus) {
        logger.debug("NotifyServlet started");
        servletInitialized = newStatus;
    }

    public boolean getNotifyServletStatus() {
        return servletInitialized;
    }

    /**
     * We received the pairing resuled (by the Norify servlet)
     *
     * @param notifyDeviceId The unique device id pairing was initiated for
     * @param pairingCode Pairng code computed by the receiver
     * @return true: thing handler was called, false: failed, e.g. unknown device
     */
    @SuppressWarnings("null")
    public boolean notifyPairingResult(String notifyDeviceId, String ipAddress, String pairingCode) {
        try {
            logger.trace("PairingResult: Check {} devices for id {}, ipAddress {}", deviceList.size(), notifyDeviceId,
                    ipAddress);
            MagentaTVDevice dev = lookupDevice(ipAddress);
            if ((dev != null) && (dev.thingHandler != null)) {
                if ((dev.deviceId == null) || dev.deviceId.isEmpty()) {
                    logger.trace("deviceId {} assigned for ipAddress {}", notifyDeviceId, ipAddress);
                    dev.deviceId = notifyDeviceId;
                }
                dev.thingHandler.onPairingResult(pairingCode);
                return true;
            }

            logger.debug("Received pairingCode {} for unregistered device {}!", pairingCode, ipAddress);
        } catch (RuntimeException | MagentaTVException e) {
            logger.debug("Unable to process pairing result for deviceID {}: {}", notifyDeviceId, e.getMessage());
        }
        return false;
    }

    /**
     * A programInfo or playStatus event was received from the receiver
     *
     * @param mrMac MR MAC address (used to map the device)
     * @param jsonEvent Event data in JSON format
     * @return true: thing handler was called, false: failed, e.g. unknown device
     */
    @SuppressWarnings("null")
    public boolean notifyMREvent(String mrMac, String jsonEvent) {
        try {
            logger.trace("Received MR event from MAC {}, JSON={}", mrMac, jsonEvent);
            MagentaTVDevice dev = lookupDevice(mrMac);
            if ((dev != null) && (dev.thingHandler != null)) {
                dev.thingHandler.onMREvent(jsonEvent);
                return true;
            }
            logger.debug("Received event for unregistered MR: MAC address {}, JSON={}", mrMac, jsonEvent);
        } catch (RuntimeException e) {
            logger.debug("Unable to process MR event! json={}", jsonEvent);
        }
        return false;
    }

    /**
     * The PowerOff Listener got a byebye message. This comes in when the receiver
     * was is going to suspend mode.
     *
     * @param ipAddress receiver IP
     */
    @SuppressWarnings("null")
    public void onPowerOff(String ipAddress) {
        try {
            logger.debug("ByeBye message received for IP {}", ipAddress);
            MagentaTVDevice dev = lookupDevice(ipAddress);
            if ((dev != null) && (dev.thingHandler != null)) {
                logger.trace("Continue with UDN {}", dev.udn);
                dev.thingHandler.onPowerOff();
            }
        } catch (RuntimeException | MagentaTVException e) {
            logger.debug("Unable to process SSDP message for IP {} - {}", ipAddress, e.getMessage());
        }
    }

    @SuppressWarnings("null")
    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    protected void setNetworkAddressService(NetworkAddressService networkAddressService) {
        logger.debug("networkAddressService bound");
        try {
            logger.debug("Initialize network access");
            network.initLocalNet(networkAddressService);
            NetworkInterface intf = network.getLocalInterface();
            upnpListener = new MagentaTVPoweroffListener(this, intf);
            if (!upnpListener.isStarted()) {
                upnpListener.start();
            }
        } catch (MagentaTVException | RuntimeException e) {
            logger.warn("Unable to initialize network access: {}", e.getMessage());
        }
    }

    protected void unsetNetworkAddressService(NetworkAddressService networkAddressService) {
    }
}
