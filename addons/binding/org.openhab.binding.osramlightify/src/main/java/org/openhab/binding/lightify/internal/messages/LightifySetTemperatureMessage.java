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

import org.eclipse.smarthome.core.library.types.DecimalType;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.handler.LightifyDeviceHandler;
import org.openhab.binding.osramlightify.internal.LightifyDeviceState;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyException;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyMessageTooLongException;

/**
 * Set the white temperature of a light.
 *
 * @author Mike Jagdis - Initial contribution
 */
public final class LightifySetTemperatureMessage extends LightifyTransitionableMessage implements LightifyMessage {

    private int temperature;
    private short unknown1;
    private String deviceId;

    public LightifySetTemperatureMessage(LightifyDeviceHandler deviceHandler, DecimalType temperature) {
        super(deviceHandler, Command.SET_TEMPERATURE);

        this.temperature = temperature.intValue();
    }

    @Override
    public String toString() {
        String string = super.toString()
            + ", Temperature = " + temperature;

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
    public LightifySetTemperatureMessage setTransitionEndNanos(long transitionEndNanos) {
        super.setTransitionEndNanos(transitionEndNanos);
        return this;
    }

    @Override
    public ByteBuffer encodeMessage() throws LightifyMessageTooLongException {
        state.setTransitionEndNanos(1, getThisTransitionEndNanos());

        return super.encodeMessage(4)
            .putShort((short) (temperature & 0xffff))
            .putShort((short) (getThisTransitionTimeNanos() / 100000000L));
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
