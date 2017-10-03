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

import org.eclipse.smarthome.core.library.types.OnOffType;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.handler.LightifyDeviceHandler;
import org.openhab.binding.osramlightify.internal.LightifyDeviceState;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyException;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyMessageTooLongException;

/**
 * Set a device on or off.
 *
 * @author Mike Jagdis - Initial contribution
 */
public final class LightifySetSwitchMessage extends LightifyBaseMessage implements LightifyMessage {

    private byte onoff;
    private short unknown1;
    private String deviceId;

    public LightifySetSwitchMessage(LightifyDeviceHandler deviceHandler, OnOffType onoff, LightifyDeviceState state) {
        super(deviceHandler, Command.SET_SWITCH);

        this.onoff = (byte) (onoff == OnOffType.ON ? 0x01 : 0x00);
    }

    @Override
    public String toString() {
        String string = super.toString()
            + ", On/Off = " + (onoff != 0 ? "ON" : "OFF");

        if (!isResponse()) {
            return string;
        }

        return string
            + ", unknown1 = " + String.format("0x%02x", unknown1)
            + ", deviceId = " + deviceId;
    }

    // ****************************************
    //      Request transmission section
    // ****************************************

    @Override
    public ByteBuffer encodeMessage() throws LightifyMessageTooLongException {
        return super.encodeMessage(1)
            .put(onoff);
    }

    // ****************************************
    //        Response handling section
    // ****************************************

    @Override
    public boolean handleResponse(LightifyBridgeHandler bridgeHandler, ByteBuffer data) throws LightifyException {
        super.handleResponse(bridgeHandler, data);

        unknown1 = data.getShort();
        deviceId = decodeDeviceAddress(data);

        return true;
    }
}
