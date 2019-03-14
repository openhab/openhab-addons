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
package org.openhab.binding.hdl.internal.discovery;

import java.util.Date;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.hdl.HdlBindingConstants;
import org.openhab.binding.hdl.internal.device.Device;
import org.openhab.binding.hdl.internal.handler.DeviceStatusListener;
import org.openhab.binding.hdl.internal.handler.HdlBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HdlDeviceDiscoveryService} To be able to automatically find new devices.
 * is ignored for now since don't see a reason to use it yet.
 * based on MAX Discovery Service from MAX! binding to OpenHAB 2
 *
 * @author stigla - Initial contribution
 */
public class HdlDeviceDiscoveryService extends AbstractDiscoveryService implements DeviceStatusListener {

    private static final int SEARCH_TIME = 60;
    private final Logger logger = LoggerFactory.getLogger(HdlDeviceDiscoveryService.class);

    private HdlBridgeHandler hdlBridgeHandler;

    public HdlDeviceDiscoveryService(HdlBridgeHandler HdlBridgeHandler) {
        super(HdlBindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS, SEARCH_TIME, true);
        this.hdlBridgeHandler = HdlBridgeHandler;
    }

    public void activate() {
        hdlBridgeHandler.registerDeviceStatusListener(this);
    }

    @Override
    public void deactivate() {
        hdlBridgeHandler.unregisterDeviceStatusListener(this);
        removeOlderResults(new Date().getTime());
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return HdlBindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS;
    }

    @Override
    public void onDeviceAdded(Bridge bridge, Device device) {
        logger.trace("Adding new Hdl! {} with id '{}' to smarthome inbox", device.getType(), device.getdeviceID());
        ThingUID thingUID = null;
        switch (device.getType()) {
            case MDT0601_233:
                thingUID = new ThingUID(HdlBindingConstants.THING_TYPE_MDT0601_233, bridge.getUID(),
                        device.getSerialNr());
                break;
            case ML01:
                thingUID = new ThingUID(HdlBindingConstants.THING_TYPE_ML01, bridge.getUID(), device.getSerialNr());
                break;

            case MPL8_48_FH:
                thingUID = new ThingUID(HdlBindingConstants.THING_TYPE_MPL8_48_FH, bridge.getUID(),
                        device.getSerialNr());
                break;
            case MPT04_48:
                thingUID = new ThingUID(HdlBindingConstants.THING_TYPE_MPT04_48, bridge.getUID(), device.getSerialNr());
                break;
            case MR1216_233:
                thingUID = new ThingUID(HdlBindingConstants.THING_TYPE_MR1216_233, bridge.getUID(),
                        device.getSerialNr());
                break;
            case MRDA0610_432:
                thingUID = new ThingUID(HdlBindingConstants.THING_TYPE_MRDA0610_432, bridge.getUID(),
                        device.getSerialNr());
                break;
            case MS08Mn_2C:
                thingUID = new ThingUID(HdlBindingConstants.THING_TYPE_MS08MN_2C, bridge.getUID(),
                        device.getSerialNr());
                break;
            case MS12_2C:
                thingUID = new ThingUID(HdlBindingConstants.THING_TYPE_MS12_2C, bridge.getUID(), device.getSerialNr());
                break;
            case MS24:
                thingUID = new ThingUID(HdlBindingConstants.THING_TYPE_MS24, bridge.getUID(), device.getSerialNr());
                break;
            case MW02:
                thingUID = new ThingUID(HdlBindingConstants.THING_TYPE_MW02, bridge.getUID(), device.getSerialNr());
                break;
            default:
                break;
        }
        if (thingUID != null) {
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                    .withProperty(HdlBindingConstants.PROPERTY_SUBNET, device.getsubNet())
                    .withProperty(HdlBindingConstants.PROPERTY_DEVICEID, device.getdeviceID())
                    .withBridge(bridge.getUID()).withLabel(device.getType() + ": " + device.getSerialNr())
                    .withRepresentationProperty(HdlBindingConstants.PROPERTY_SUBNET)
                    .withRepresentationProperty(HdlBindingConstants.PROPERTY_DEVICEID).build();
            thingDiscovered(discoveryResult);

        } else {
            logger.debug("Discovered HDL! device is unsupported: type '{}' with id '{}'", device.getType(),
                    device.getSerialNr());
        }
    }

    @Override
    protected void startScan() {
        // if (HdlBridgeHandler != null) {
        // HdlBridgeHandler.clearDeviceList();
        // HdlBridgeHandler.deviceInclusion();
        // }
    }

    @Override
    public void onDeviceStateChanged(ThingUID bridge, Device device) {
        // this can be ignored here
    }

    @Override
    public void onDeviceRemoved(HdlBridgeHandler bridge, Device device) {
        // this can be ignored here
    }

    @Override
    public void onDeviceConfigUpdate(Bridge bridge, Device device) {
        // this can be ignored here
    }
}
