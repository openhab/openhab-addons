/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.osramlightify.internal.messages;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.eclipse.smarthome.core.thing.Thing.PROPERTY_FIRMWARE_VERSION;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;

import static org.openhab.binding.osramlightify.LightifyBindingConstants.DEVICE_TYPE_THING_TYPE_UID_MAP;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.handler.LightifyDeviceHandler;
import org.openhab.binding.osramlightify.internal.LightifyDeviceState;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyException;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyMessageTooLongException;
import org.openhab.binding.osramlightify.internal.messages.LightifyBaseMessage;

/**
 * Get the firmware version of a Lightify gateway.
 *
 * @author Mike Jagdis - Initial contribution
 */
public final class LightifyListPairedDevicesMessage extends LightifyBaseMessage implements LightifyMessage {

    private final Logger logger = LoggerFactory.getLogger(LightifyListPairedDevicesMessage.class);

    private static final Map<ThingUID, Integer> known = new HashMap<>();
    private static int seen = 0;

    private boolean changes;

    public LightifyListPairedDevicesMessage() {
        super(null, Command.LIST_PAIRED_DEVICES);
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
        return super.encodeMessage(1)
            .put((byte) 0x01);
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

        short deviceCount = data.getShort();

        changes = false;

        for (int i = 0; i < deviceCount; i++) {
            LightifyDeviceState state = new LightifyDeviceState();

            data.getShort(); // deviceNumber

            String deviceAddress = decodeDeviceAddress(data);
            int deviceType = ((int) data.get() & 0xff);

            String firmwareVersion = decodeHex(data, 4);
            state.reachable = ((int) data.get() & 0xff);
            data.getShort(); // groupNumber
            state.power = ((int) data.get() & 0xff);
            state.luminance = ((int) data.get() & 0xff);
            state.temperature = ((int) data.getShort() & 0xffff);
            state.r = ((int) data.get() & 0xff);
            state.g = ((int) data.get() & 0xff);
            state.b = ((int) data.get() & 0xff);
            state.a = ((int) data.get() & 0xff);
            String deviceName = decodeName(data);
            state.timeSinceSeen = data.getInt();
            state.joining = data.getInt();

            logger.trace("{}: \"{}\" device type={}, firmware version={}, {}", deviceAddress, deviceName, deviceType, firmwareVersion, state);

            ThingTypeUID thingTypeUID = DEVICE_TYPE_THING_TYPE_UID_MAP.get(deviceType);

            // When pairing devices may appear in the list as generic devices and then be
            // updated to their correct device type once the gateway has finished probing
            // them. Other devices may be simply unsupported. In either case all we can
            // do is ignore their very existence.
            if (thingTypeUID != null) {
                ThingUID thingUID = new ThingUID(thingTypeUID, deviceAddress.replace(":", "-"));

                known.put(thingUID, seen);

                Thing thing = bridgeHandler.getThingByUID(thingUID);

                if (thing != null && state.received(bridgeHandler, thing, deviceAddress, now)) {
                    changes = true;
                }

                if (thing != null) {
                    String currentFirmwareVersion = thing.getProperties().get(PROPERTY_FIRMWARE_VERSION);

                    if (!firmwareVersion.equals(currentFirmwareVersion)) {
                        thing.setProperty(PROPERTY_FIRMWARE_VERSION, firmwareVersion);
                    }
                } else {
                    bridgeHandler.getDiscoveryService().discoveryResult(thingUID, thingTypeUID,
                        deviceName, deviceAddress, firmwareVersion);
                }
            }
        }

        // If there are devices on this bridge that we didn't see data for in the above
        // we set them offline.
        ThingUID[] removed = known.entrySet().stream()
            .filter(entry -> entry.getValue() < seen)
            .map(entry -> entry.getKey())
            .toArray(ThingUID[]::new);

        for (ThingUID thingUID : removed) {
            Thing thing = bridgeHandler.getThingByUID(thingUID);

            if (thing != null) {
                if (thing.getStatus() != ThingStatus.UNKNOWN) {
                    LightifyDeviceHandler thingHandler = (LightifyDeviceHandler) thing.getHandler();
                    thingHandler.setStatus(ThingStatus.UNKNOWN);
                }
            } else {
                bridgeHandler.getDiscoveryService().removeThing(thingUID);
            }

            known.remove(thingUID);
        }

        seen++;

        return true;
    }

    public boolean hasChanges() {
        return changes;
    }
}
