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
import org.openhab.binding.osramlightify.internal.exceptions.LightifyException;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyMessageTooLongException;
import org.openhab.binding.osramlightify.internal.messages.LightifyBaseMessage;

/**
 * Set the white temperature of a light.
 *
 * @author Mike Jagdis - Initial contribution
 */
public final class LightifySetTemperatureMessage extends LightifyBaseMessage implements LightifyMessage {

    short temperature;
    short unknown1;
    String deviceId;

    public LightifySetTemperatureMessage(String deviceAddress, DecimalType temperature) {
        super(deviceAddress, Command.SET_TEMPERATURE);

        this.temperature = (short) (temperature.intValue() & 0xffff);
    }

    @Override
    public String toString() {
        String string = super.toString()
            + ", Transition = " + transitionTime
            + ", Temperature = " + ((int) temperature & 0xffff);

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

    public LightifySetTemperatureMessage setTransitionTime(Double transitionTime) {
        return (LightifySetTemperatureMessage) super.setTransitionTime(transitionTime);
    }

    @Override
    public ByteBuffer encodeMessage() throws LightifyMessageTooLongException {
        return super.encodeMessage(4)
            .putShort(temperature)
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
