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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.magentatv.internal.network.MagentaTVNetwork;
import org.openhab.binding.magentatv.internal.network.MagentaTVPoweroffListener;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link MagentaTVHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@Component(service = { ThingHandlerFactory.class,
        MagentaTVHandlerFactory.class }, immediate = true, configurationPid = "binding." + BINDING_ID)
public class MagentaTVHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_RECEIVER);
    private final MagentaTVLogger logger = new MagentaTVLogger(MagentaTVHandlerFactory.class, "Factory");
    private @Nullable NetworkAddressService networkAddressService = null;

    private final MagentaTVNetwork network = new MagentaTVNetwork();
    private @Nullable MagentaTVPoweroffListener upnpListener;
    private boolean notifyServletInitialized = false;

    protected class MagentaTVDevice {
        protected String udn = "";
        protected String mac = "";
        protected String deviceId = "";
        protected String ipAddress = "";
        // protected ThingUID uid = new ThingUID();
        protected Map<String, Object> properties = new HashMap<String, Object>();
        @Nullable
        protected MagentaTVHandler thingHandler;
    }

    private final Map<String, MagentaTVDevice> deviceList = new HashMap<String, MagentaTVDevice>();

    /**
     * Activate the bundle: save properties
     *
     * @param componentContext
     * @param configProperties set of properties from cfg (use same names as in
     *                             thing config)
     */

    @Activate
    protected void activate(ComponentContext componentContext, Map<String, Object> configProperties) {
        super.activate(componentContext);
        logger.debug("Activate HandlerFactory");
        network.initLocalNet(networkAddressService);
        NetworkInterface localInterface = network.getLocalInterface();
        if (localInterface != null) {
            upnpListener = new MagentaTVPoweroffListener(this, localInterface);
        }
    }

    /**
     * return list of supported thins
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @SuppressWarnings("null")
    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        logger.debug("Create thing type {}", thing.toString());
        if (THING_TYPE_RECEIVER.equals(thingTypeUID)) {
            if ((upnpListener != null) && !upnpListener.isStarted()) {
                upnpListener.start();
            }
            return new MagentaTVHandler(this, thing, network);
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
            synchronized (deviceList) {
                MagentaTVDevice dev = null;
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
                    dev = addNewDevice(discoveredUDN, null, ipAddress, mac, discoveryProperties, null);
                }
            }
        } catch (Exception e) {
            logger.exception("Unable to process discovered device, UDN=" + discoveredUDN, e);
        }
    }

    /**
     * Add a device to the device table
     *
     * @param udn       UDN for the device
     * @param deviceId  A unique device id
     * @param ipAddress IP address of the receiver
     * @param handler   The corresponding thing handler
     */
    @SuppressWarnings({ "null", "unused" })
    public void registerDevice(String udn, String deviceId, String ipAddress, MagentaTVHandler handler) {
        logger.trace("Register new device, UDN={}, deviceId={}, ipAddress={}", udn, deviceId, ipAddress);

        MagentaTVDevice dev = deviceList.get(udn.toUpperCase());
        if (dev == null) {
            addNewDevice(udn, deviceId, ipAddress, null, null, handler);
        } else {
            logger.trace("Device with UDN {} is already registered", udn);
        }
    }

    private MagentaTVDevice addNewDevice(String udn, @Nullable String deviceId, String ipAddress,
            @Nullable String macAddress, @Nullable Map<String, Object> discoveryProperties,
            @Nullable MagentaTVHandler handler) {

        String mac = "";
        if (macAddress == null) {
            // build MAC from UDN
            mac = StringUtils.substringAfterLast(udn, "-");
        } else {
            mac = macAddress;
        }

        MagentaTVDevice dev = new MagentaTVDevice();
        synchronized (deviceList) {
            dev.udn = udn.toUpperCase();
            dev.mac = mac.toUpperCase();
            if ((deviceId != null) && !deviceId.isEmpty()) {
                dev.deviceId = deviceId.toUpperCase();
            }
            dev.ipAddress = ipAddress;
            if (discoveryProperties != null) {
                dev.properties = discoveryProperties;
            }
            dev.thingHandler = handler; // maybe null for new discovered devices
            deviceList.put(dev.udn, dev);
        }
        logger.debug("Device added (UDN={} ,deviceId={}, ipAddress={}, macAddress={}), now {} devices.", udn, deviceId,
                ipAddress, mac, deviceList.size());
        return dev;
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
    private @Nullable MagentaTVDevice lookupDevice(String uniqueId) {
        MagentaTVDevice dev = null;
        logger.trace("Lookup device, uniqueId={}", uniqueId);
        int i = 0;
        for (String key : deviceList.keySet()) {
            synchronized (deviceList) {
                dev = deviceList.get(key);
                logger.trace("Devies[{}]: deviceId={}, UDN={}, ipAddress={}, macAddress={}", i++, dev.deviceId, dev.udn,
                        dev.ipAddress, dev.mac);
                if (dev.udn.equalsIgnoreCase(uniqueId) || dev.ipAddress.equalsIgnoreCase(uniqueId)
                        || dev.deviceId.equalsIgnoreCase(uniqueId) || dev.mac.equalsIgnoreCase(uniqueId)) {
                    return dev;
                }
            }
        }
        return null;
    }

    /**
     * returned the discovered properties
     *
     * @param udn Unique ID from UPnP discovery
     * @return property map with discovered properties
     */
    @SuppressWarnings({ "null", "unused" })
    public Map<String, Object> getDiscoveredProperties(String udn) {
        MagentaTVDevice dev = deviceList.get(udn.toUpperCase());
        if (dev != null) {
            return dev.properties;
        }
        if (deviceList.size() > 0) {
            logger.debug("getDiscoveredProperties(): Unknown UDN: {}", udn);
        }
        return null;
    }

    public boolean getNotifyServletStatus() {
        return notifyServletInitialized;
    }

    public void setNotifyServletStatus(boolean newStatus) {
        logger.debug("NotifyServlet started");
        this.notifyServletInitialized = newStatus;
    }

    /**
     * We received the pairing resuled (by the Norify servlet)
     *
     * @param notifyDeviceId The unique device id pairing was initiated for
     * @param pairingCode    Pairng code computed by the receiver
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

            logger.fatal("Received pairingCode {} for unregistered device {}!", pairingCode, ipAddress);
        } catch (Exception e) {
            logger.exception("Unable to process pairing result for deviceID " + notifyDeviceId, e);
        }
        return false;
    }

    /**
     * A programInfo or playStatus event was received from the receiver
     *
     * @param stbMac    MAC address (used to map the device)
     * @param jsonEvent Event data in JSON format
     * @return true: thing handler was called, false: failed, e.g. unknown device
     */
    public boolean notifyStbEvent(String stbMac, String jsonEvent) {
        try {
            logger.trace("Received STB Event from MAC {}", stbMac);
            MagentaTVDevice dev = lookupDevice(stbMac);
            if ((dev != null) && (dev.thingHandler != null)) {
                dev.thingHandler.onStbEvent(jsonEvent);
                return true;
            }
            logger.fatal("Received event for unregistered MAC '{}', JSON='{}'", stbMac.toUpperCase(), jsonEvent);
        } catch (Exception e) {
            logger.exception("Unable to process playContent for MAC " + stbMac, e);
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
        } catch (Exception e) {
            logger.exception("Unable to process SSDP message for IP " + ipAddress, e);
        }

    }

    @Reference
    protected void setNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = networkAddressService;
    }

    protected void unsetNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = null;
    }
}
