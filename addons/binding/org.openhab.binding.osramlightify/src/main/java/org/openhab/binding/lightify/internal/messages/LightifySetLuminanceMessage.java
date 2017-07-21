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

import org.eclipse.smarthome.core.library.types.PercentType;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyException;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyMessageTooLongException;
import org.openhab.binding.osramlightify.internal.messages.LightifyBaseMessage;

/**
 * Set the luminance of a light.
 *
 * @author Mike Jagdis - Initial contribution
 */
public final class LightifySetLuminanceMessage extends LightifyBaseMessage implements LightifyMessage {

    byte luminance;
    short unknown1;
    String deviceId;

    public LightifySetLuminanceMessage(String deviceAddress, PercentType luminance) {
        super(deviceAddress, Command.SET_LUMINANCE);

        this.luminance = (byte) (luminance.intValue() & 0xff);
    }

    @Override
    public String toString() {
        String string = super.toString()
            + ", Transition = " + transitionTime
            + ", Luminance = " + (luminance & 0xff);

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

    public LightifySetLuminanceMessage setTransitionTime(Double transitionTime) {
        return (LightifySetLuminanceMessage) super.setTransitionTime(transitionTime);
    }

    @Override
    public ByteBuffer encodeMessage() throws LightifyMessageTooLongException {
        return super.encodeMessage(3)
            .put(luminance)
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
