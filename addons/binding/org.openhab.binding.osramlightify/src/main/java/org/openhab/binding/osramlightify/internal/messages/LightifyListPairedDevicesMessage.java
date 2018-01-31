/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.osramlightify.internal.messages;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;

import static org.openhab.binding.osramlightify.LightifyBindingConstants.DEVICE_TYPE_THING_TYPE_UID_MAP;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.PROPERTY_IEEE_ADDRESS;
import static org.openhab.binding.osramlightify.internal.messages.LightifyBaseMessage.NAME_LENGTH;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.handler.LightifyDeviceHandler;
import org.openhab.binding.osramlightify.internal.LightifyDeviceState;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyException;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyMessageTooLongException;
import org.openhab.binding.osramlightify.internal.messages.LightifyBaseMessage;

import org.openhab.binding.osramlightify.internal.util.IEEEAddress;

/**
 * Get the firmware version of a Lightify gateway.
 *
 * @author Mike Jagdis - Initial contribution
 */
public final class LightifyListPairedDevicesMessage extends LightifyBaseMessage implements LightifyMessage {

    private static final int FIRMWARE_LENGTH = 4;

    private final Logger logger = LoggerFactory.getLogger(LightifyListPairedDevicesMessage.class);

    private IEEEAddress deviceAddress = new IEEEAddress();
    private final byte[] deviceNameBytes = new byte[NAME_LENGTH];
    private final byte[] firmwareVersionBytes = new byte[FIRMWARE_LENGTH];

    private final LightifyDeviceState state = new LightifyDeviceState();

    private ByteBuffer encodedMessage;

    private boolean discovery = false;
    private boolean changes;

    public LightifyListPairedDevicesMessage() {
        super(null, Command.LIST_PAIRED_DEVICES);

        try {
            encodedMessage = super.encodeMessage(1)
                .put((byte) 0x01);
        } catch (LightifyMessageTooLongException e) {
        }
    }

    public LightifyListPairedDevicesMessage discovery(boolean discovery) {
        this.discovery = discovery;
        return this;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public boolean isPoller() {
        return true;
    }

    // ****************************************
    //      Request transmission section
    // ****************************************

    @Override
    public ByteBuffer encodeMessage() throws LightifyMessageTooLongException {
        return super.encodeMessage(encodedMessage);
    }

    // ****************************************
    //        Response handling section
    // ****************************************

    @Override
    public boolean handleResponse(LightifyBridgeHandler bridgeHandler, ByteBuffer data) throws LightifyException {
        long now = System.nanoTime();

        try {
            super.handleResponse(bridgeHandler, data);
        } catch (LightifyException e) {
            logger.warn("Error", e);
            return false;
        }

        HashMap<IEEEAddress, Object> known = bridgeHandler.knownDevices;
        HashMap<IEEEAddress, Object> toRemove = null;

        if (discovery) {
            toRemove = (HashMap<IEEEAddress, Object>) known.clone();
        }

        short deviceCount = data.getShort();

        changes = false;

        for (int i = 0; i < deviceCount; i++) {
            data.getShort(); // deviceNumber

            data.get(deviceAddress.array());
            deviceAddress.resetString();
            if (toRemove != null) {
                toRemove.remove(deviceAddress);
            }

            state.deviceType = ((int) data.get() & 0xff);
            data.get(firmwareVersionBytes);
            state.reachable = ((int) data.get() & 0xff);
            data.getShort(); // groupNumber
            state.power = ((int) data.get() & 0xff);
            state.luminance = ((int) data.get() & 0xff);
            state.temperature = ((int) data.getShort() & 0xffff);
            state.r = ((int) data.get() & 0xff);
            state.g = ((int) data.get() & 0xff);
            state.b = ((int) data.get() & 0xff);
            state.a = ((int) data.get() & 0xff);
            data.get(deviceNameBytes);
            state.timeSinceSeen = data.getInt();
            state.joining = data.getInt();

            LightifyDeviceHandler deviceHandler = null;
            ThingUID thingUID = null;
            ThingTypeUID thingTypeUID = null;

            Object obj = known.get(deviceAddress);

            if (obj instanceof LightifyDeviceHandler) {
                deviceHandler = (LightifyDeviceHandler) obj;

                // If we have a thing but it is no longer initialized we put it straight back in
                // the inbox and carry on.
                if (!deviceHandler.isStatusInitialized()) {
                    // N.B. The device type and IEEE address can't have changed but the name on the gateway might have.
                    Thing thing = deviceHandler.getThing();
                    bridgeHandler.getDiscoveryService().discoveryResult(thing.getUID(), thing.getThingTypeUID(),
                        new String(deviceNameBytes, StandardCharsets.UTF_8).trim(),
                        thing.getProperties().get(PROPERTY_IEEE_ADDRESS));

                    known.put(deviceHandler.getDeviceAddress(), thing.getUID());
                    continue;
                }
            } else if (discovery) {
                // We don't know of a thing for this. Is there one?
                thingTypeUID = DEVICE_TYPE_THING_TYPE_UID_MAP.get(state.deviceType);

                // When pairing, devices may appear in the list as generic devices and then be
                // updated to their correct device type once the gateway has finished probing
                // them. Other devices may be simply unsupported. In either case all we can
                // do is ignore their very existence.
                if (thingTypeUID != null) {
                    if (obj instanceof ThingUID) {
                        thingUID = (ThingUID) obj;
                    } else {
                        thingUID = new ThingUID(thingTypeUID, deviceAddress.toString().replace(":", "-"));
                    }

                    Thing thing = bridgeHandler.getThingByUIDGlobally(thingUID);

                    if (thing != null) {
                        thing.setBridgeUID(bridgeHandler.getThing().getUID());
                        deviceHandler = (LightifyDeviceHandler) thing.getHandler();
                        known.put(deviceHandler.getDeviceAddress(), deviceHandler);
                    }
                }
            }

            if (deviceHandler != null) {
                // If the thing is initialized we process the state update.
                if (deviceHandler.isStatusInitialized()) {
                    changes |= state.received(bridgeHandler, deviceHandler, now, false);
                }

                if (discovery) {
                    deviceHandler.updateFirmwareVersion(firmwareVersionBytes);
                }
            } else if (!known.containsKey(deviceAddress) && thingTypeUID != null) {
                // If we haven't seen this before and it is supported it goes in the inbox.
                bridgeHandler.getDiscoveryService().discoveryResult(thingUID, thingTypeUID,
                    new String(deviceNameBytes, StandardCharsets.UTF_8).trim(),
                    thingUID.getId().replace("-", ":"));

                known.put(deviceAddress.clone(), thingUID);
            }
        }

        if (toRemove != null) {
            // If there are devices we saw before but that do not exist now then we either remove
            // them from the inbox or set their status to UNKNOWN.
            toRemove.forEach((deviceAddress, obj) -> {
                if (obj instanceof LightifyDeviceHandler) {
                    LightifyDeviceHandler deviceHandler = (LightifyDeviceHandler) obj;

                    if (deviceHandler.isStatusInitialized() && deviceHandler.getThing().getStatus() != ThingStatus.UNKNOWN) {
                        deviceHandler.setStatus(ThingStatus.UNKNOWN);
                    }
                } else {
                    bridgeHandler.getDiscoveryService().removeThing((ThingUID) obj);
                }

                known.remove(deviceAddress);
            });
        }

        return true;
    }

    public boolean hasChanges() {
        return changes;
    }
}
