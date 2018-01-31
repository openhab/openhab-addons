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

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.handler.LightifyDeviceHandler;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyException;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyMessageTooLongException;

import org.openhab.binding.osramlightify.internal.util.IEEEAddress;

/**
 * Set the colour of a light.
 *
 * @author Mike Jagdis - Initial contribution
 */
public final class LightifySetColorMessage extends LightifyTransitionableMessage implements LightifyMessage {

    private int[] rgba;
    private short unknown1;
    private IEEEAddress deviceId = new IEEEAddress();

    public LightifySetColorMessage(LightifyDeviceHandler deviceHandler, int[] rgba) {
        super(deviceHandler, Command.SET_COLOR);

        this.rgba = rgba;
    }

    public LightifySetColorMessage(LightifyDeviceHandler deviceHandler, HSBType hsb) {
        super(deviceHandler, Command.SET_COLOR);

        PercentType[] rgb = hsb.toRGB();

        rgba = new int[]{
            (int) ((rgb[0].doubleValue() * 255) / 100),
            (int) ((rgb[1].doubleValue() * 255) / 100),
            (int) ((rgb[2].doubleValue() * 255) / 100),
            255
        };
    }

    @Override
    public String toString() {
        String string = super.toString()
            + ", RGBA = " + rgba[0] + "," + rgba[1] + "," + rgba[2] + "," + rgba[3];

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

    @Override
    public LightifySetColorMessage setTransitionEndNanos(long transitionEndNanos) {
        super.setTransitionEndNanos(transitionEndNanos);
        return this;
    }

    @Override
    public ByteBuffer encodeMessage() throws LightifyMessageTooLongException {
        return super.encodeMessage(6)
            .put((byte) (rgba[0] & 0xff))
            .put((byte) (rgba[1] & 0xff))
            .put((byte) (rgba[2] & 0xff))
            .put((byte) (rgba[3] & 0xff))
            .putShort((short) (getThisTransitionTimeNanos(1) / 100000000L));
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
