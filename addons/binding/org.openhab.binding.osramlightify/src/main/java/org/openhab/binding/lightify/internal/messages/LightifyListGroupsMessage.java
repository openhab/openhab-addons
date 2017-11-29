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

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;

import static org.openhab.binding.osramlightify.LightifyBindingConstants.THING_TYPE_LIGHTIFY_GROUP;

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

    private static final Map<ThingUID, Integer> known = new HashMap<>();
    private static int seen = 0;

    public LightifyListGroupsMessage() {
        super(null, Command.LIST_GROUPS);
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
        super.handleResponse(bridgeHandler, data);

        short deviceCount = data.getShort();

        ThingUID bridgeUID = bridgeHandler.getThing().getUID();

        for (int i = 0; i < deviceCount; i++) {
            short deviceNumber = data.getShort();
            String deviceAddress = makeGroupAddress(deviceNumber);
            ThingTypeUID thingTypeUID = THING_TYPE_LIGHTIFY_GROUP;

            String deviceName = decodeName(data);

            logger.trace("{}: group {} \"{}\"", deviceAddress, i, deviceName);

            ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, String.format("%d", (deviceNumber & 0xffff)));

            known.put(thingUID, seen);

            Thing thing = bridgeHandler.getThingByUID(thingUID);

            if (thing != null) {
                if (thing.getStatus() != ThingStatus.ONLINE) {
                    LightifyDeviceHandler thingHandler = (LightifyDeviceHandler) thing.getHandler();
                    thingHandler.setStatus(ThingStatus.ONLINE);
                }
            } else {
                bridgeHandler.getDiscoveryService().discoveryResult(thingUID, thingTypeUID,
                    deviceName, deviceAddress, null);
            }
        }

        // If there are groups on this bridge that we didn't see data for in the above
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
}
