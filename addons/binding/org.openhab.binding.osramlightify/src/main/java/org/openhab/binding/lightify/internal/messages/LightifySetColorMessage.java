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

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyException;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyMessageTooLongException;
import org.openhab.binding.osramlightify.internal.messages.LightifyBaseMessage;

/**
 * Set the colour of a light.
 *
 * @author Mike Jagdis - Initial contribution
 */
public final class LightifySetColorMessage extends LightifyBaseMessage implements LightifyMessage {

    byte r;
    byte g;
    byte b;
    byte a;
    short unknown1;
    String deviceId;

    public LightifySetColorMessage(String deviceAddress, HSBType hsb) {
        super(deviceAddress, Command.SET_COLOR);

        PercentType[] rgb = hsb.toRGB();

        r = (byte) ((int) ((rgb[0].doubleValue() * 255) / 100) & 0xff);
        g = (byte) ((int) ((rgb[1].doubleValue() * 255) / 100) & 0xff);
        b = (byte) ((int) ((rgb[2].doubleValue() * 255) / 100) & 0xff);
        a = (byte) 255;
    }

    @Override
    public String toString() {
        String string = super.toString()
            + ", Transition = " + transitionTime
            + ", RGBA = " + (r & 0xff) + "," + (g & 0xff) + "," + (b & 0xff) + "," + (a & 0xff);

        if (!isResponse()) {
            return string;
        }

        return string
            + ", unknown1 = " + String.format("0x%04x", unknown1)
            + ", deviceId = " + deviceId;
    }

    // ****************************************
    //      Request transmission section
    // ****************************************

    public LightifySetColorMessage setTransitionTime(Double transitionTime) {
        return (LightifySetColorMessage) super.setTransitionTime(transitionTime);
    }

    @Override
    public ByteBuffer encodeMessage() throws LightifyMessageTooLongException {
        return super.encodeMessage(6)
            .put(r)
            .put(g)
            .put(b)
            .put(a)
            .putShort(transitionTime);
    }

    // ****************************************
    //        Response handling section
    // ****************************************

    public boolean handleResponse(LightifyBridgeHandler bridgeHandler, ByteBuffer data) throws LightifyException {
        decodeHeader(bridgeHandler, data);

        unknown1 = data.getShort();
        deviceId = decodeDeviceAddress(data);

        return true;
    }
}
