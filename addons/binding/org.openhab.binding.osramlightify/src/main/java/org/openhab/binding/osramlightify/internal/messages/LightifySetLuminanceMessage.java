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

import org.eclipse.smarthome.core.library.types.PercentType;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.handler.LightifyDeviceHandler;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyException;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyMessageTooLongException;

import org.openhab.binding.osramlightify.internal.util.IEEEAddress;

/**
 * Set the luminance of a light.
 *
 * @author Mike Jagdis - Initial contribution
 */
public final class LightifySetLuminanceMessage extends LightifyTransitionableMessage implements LightifyMessage {

    private int luminance;
    private short unknown1;
    private IEEEAddress deviceId = new IEEEAddress();

    public LightifySetLuminanceMessage(LightifyDeviceHandler deviceHandler, PercentType luminance) {
        super(deviceHandler, Command.SET_LUMINANCE);

        this.luminance = luminance.intValue();
    }

    @Override
    public String toString() {
        String string = super.toString()
            + ", Luminance = " + luminance;

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
    public LightifySetLuminanceMessage setTransitionEndNanos(long transitionEndNanos) {
        super.setTransitionEndNanos(transitionEndNanos);
        return this;
    }

    @Override
    public ByteBuffer encodeMessage() throws LightifyMessageTooLongException {
        return super.encodeMessage(3)
            .put((byte) (luminance & 0xff))
            .putShort((short) (getThisTransitionTimeNanos(0) / 100000000L));
    }

    // ****************************************
    //        Response handling section
    // ****************************************

    @Override
    public boolean handleResponse(LightifyBridgeHandler bridgeHandler, ByteBuffer data) throws LightifyException {
        super.handleResponse(bridgeHandler, data);

        unknown1 = data.getShort();
        data.get(deviceId.array());

        return true;
    }
}
