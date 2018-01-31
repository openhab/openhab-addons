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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openhab.binding.osramlightify.LightifyBindingConstants.PROPERTY_WIFI_FIRMWARE_VERSION;

import org.eclipse.smarthome.core.thing.Thing;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyException;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyMessageTooLongException;
import org.openhab.binding.osramlightify.internal.messages.LightifyBaseMessage;

/**
 * Get the firmware version of a Lightify gateway.
 *
 * @author Mike Jagdis - Initial contribution
 */
public final class LightifyGatewayFirmwareMessage extends LightifyBaseMessage implements LightifyMessage {

    private final Logger logger = LoggerFactory.getLogger(LightifyGatewayFirmwareMessage.class);

    private byte[] firmwareVersion;

    public LightifyGatewayFirmwareMessage() {
        super(null, Command.GET_GATEWAY_FIRMWARE_VERSION);
    }

    @Override
    public String toString() {
        String string = super.toString();

        if (!isResponse()) {
            return string;
        }

        return string + ", Firmware Version = "
            + firmwareVersion[0] + "." + firmwareVersion[1]
            + "." + firmwareVersion[2] + "." + firmwareVersion[3];
    }

    // ****************************************
    //      Request transmission section
    // ****************************************

    @Override
    public ByteBuffer encodeMessage() throws LightifyMessageTooLongException {
        return super.encodeMessage(0);
    }

    // ****************************************
    //        Response handling section
    // ****************************************

    @Override
    public boolean handleResponse(LightifyBridgeHandler bridgeHandler, ByteBuffer data) throws LightifyException {
        super.handleResponse(bridgeHandler, data);

        firmwareVersion = new byte[4];
        data.get(firmwareVersion);

        String fw = firmwareVersion[0] + "." + firmwareVersion[1] + "." + firmwareVersion[2] + "." + firmwareVersion[3];
        logger.info("{}: firmware version = {}", bridgeHandler.getThing().getUID(), fw);
        bridgeHandler.getThing().setProperty(Thing.PROPERTY_FIRMWARE_VERSION, fw);
        bridgeHandler.getThing().setProperty(PROPERTY_WIFI_FIRMWARE_VERSION, null);

        bridgeHandler.setStatusOnline();
        return true;
    }
}
