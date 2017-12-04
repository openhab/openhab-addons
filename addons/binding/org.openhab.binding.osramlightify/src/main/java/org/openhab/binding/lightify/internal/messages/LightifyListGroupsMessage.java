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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;

import static org.openhab.binding.osramlightify.LightifyBindingConstants.PROPERTY_IEEE_ADDRESS;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.THING_TYPE_LIGHTIFY_GROUP;
import static org.openhab.binding.osramlightify.internal.messages.LightifyBaseMessage.NAME_LENGTH;

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
public final class LightifyListGroupsMessage extends LightifyBaseMessage implements LightifyMessage {

    private final Logger logger = LoggerFactory.getLogger(LightifyListGroupsMessage.class);

    private byte[] deviceName = new byte[NAME_LENGTH];
    private ByteBuffer encodedMessage;

    public LightifyListGroupsMessage() {
        super(null, Command.LIST_GROUPS);

        try {
            encodedMessage = super.encodeMessage(1)
                .put((byte) 0x01);
        } catch (LightifyMessageTooLongException e) {
        }
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

    private String makeGroupAddress(short id) {
        return String.format("00:00:00:00:00:00:%02X:%02X",
            ((id >> 8) & 0xff), (id & 0xff));
    }

    @Override
    public boolean handleResponse(LightifyBridgeHandler bridgeHandler, ByteBuffer data) throws LightifyException {
        super.handleResponse(bridgeHandler, data);

        short deviceCount = data.getShort();

        ThingUID bridgeUID = bridgeHandler.getThing().getUID();

        HashMap<Short, Object> known = bridgeHandler.knownGroups;
        HashMap<Short, Object> toRemove = (HashMap) known.clone();

        for (int i = 0; i < deviceCount; i++) {
            short deviceNumber = data.getShort();
            toRemove.remove(deviceNumber);

            data.get(deviceName);

            Object obj = known.get(deviceNumber);

            LightifyDeviceHandler deviceHandler = null;
            ThingUID thingUID = null;

            if (obj != null && obj instanceof LightifyDeviceHandler) {
                deviceHandler = (LightifyDeviceHandler) obj;

                // If we have a thing but it is no longer initialized we put it straight back in
                // the inbox and carry on.
                if (!deviceHandler.isStatusInitialized()) {
                    Thing thing = deviceHandler.getThing();

                    // N.B. The IEEE address can't have changed but the name on the gateway might have.
                    bridgeHandler.getDiscoveryService().discoveryResult(thing.getUID(), THING_TYPE_LIGHTIFY_GROUP,
                        new String(deviceName, StandardCharsets.UTF_8).trim(),
                        thing.getProperties().get(PROPERTY_IEEE_ADDRESS));

                    known.put(deviceNumber, thing.getUID());
                    continue;
                }
            } else {
                // We don't know of a thing for this. Is there one?
                if (obj != null) {
                    thingUID = (ThingUID) obj;
                } else {
                    thingUID = new ThingUID(THING_TYPE_LIGHTIFY_GROUP, bridgeUID, String.format("%d", (deviceNumber & 0xffff)));
                }

                Thing thing = bridgeHandler.getThingByUID(thingUID);

                if (thing != null) {
                    deviceHandler = (LightifyDeviceHandler) thing.getHandler();
                    known.put(deviceNumber, deviceHandler);
                }
            }

            if (deviceHandler != null) {
                // We have a thing so make sure it is online.
                if (deviceHandler.isStatusInitialized() && deviceHandler.getThing().getStatus() != ThingStatus.ONLINE) {
                    deviceHandler.setStatus(ThingStatus.ONLINE);
                }
            } else if (!known.containsKey(deviceNumber)) {
                // No thing so if we haven't seen this before it goes in the inbox.
                bridgeHandler.getDiscoveryService().discoveryResult(thingUID, THING_TYPE_LIGHTIFY_GROUP,
                    new String(deviceName, StandardCharsets.UTF_8).trim(),
                    makeGroupAddress(deviceNumber));

                known.put(deviceNumber, thingUID);
            }
        }

        // If there are groups we saw before but that do not exist now then we either remove
        // them from the inbox or set their status to UNKNOWN.
        toRemove.forEach((deviceNumber, obj) -> {
            if (obj instanceof LightifyDeviceHandler) {
                LightifyDeviceHandler deviceHandler = (LightifyDeviceHandler) obj;

                if (deviceHandler.isStatusInitialized() && deviceHandler.getThing().getStatus() != ThingStatus.UNKNOWN) {
                    deviceHandler.setStatus(ThingStatus.UNKNOWN);
                }
            } else {
                bridgeHandler.getDiscoveryService().removeThing((ThingUID) obj);
            }

            known.remove(deviceNumber);
        });

        return true;
    }
}
