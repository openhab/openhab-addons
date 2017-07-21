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

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyException;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyMessageTooLongException;
import org.openhab.binding.osramlightify.internal.messages.LightifyBaseMessage;

/**
 * Get the state of a single device.
 *
 * @author Mike Jagdis - Initial contribution
 */
abstract public class LightifyGetDeviceInfoMessage extends LightifyBaseMessage implements LightifyMessage {

    protected int unknown0;

    protected boolean reachable;
    protected int power;
    protected int luminance;
    protected int temperature;
    protected int r;
    protected int g;
    protected int b;
    protected int a;

    protected int unknown1;
    protected int unknown2;
    protected int unknown3;

    public LightifyGetDeviceInfoMessage(String deviceAddress) {
        super(deviceAddress, Command.GET_DEVICE_INFO);
    }

    @Override
    public String toString() {
        String string = super.toString();

        if (!isResponse()) {
            return string;
        }

        return string
            + ", unknown0 = " + unknown0
            + ", power = " + power
            + ", luminance = " + luminance
            + ", temperature = " + temperature
            + ", r = " + r
            + ", g = " + g
            + ", b = " + b
            + ", a = " + a
            + ", unknown1 = " + unknown1
            + ", unknown2 = " + unknown2
            + ", unknown3 = " + unknown3;
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

    public boolean handleResponse(LightifyBridgeHandler bridgeHandler, ByteBuffer data) throws LightifyException {
        decodeHeader(bridgeHandler, data);

        data.getShort(); // deviceNumber
        String deviceAddress = decodeDeviceAddress(data);
        reachable = (data.get() == 0);

        if (reachable) {
            unknown0 = ((int) data.get() & 0xff);
            power = ((int) data.get() & 0xff);
            luminance = ((int) data.get() & 0xff);
            temperature = ((int) data.getShort() & 0xffff);
            r = ((int) data.get() & 0xff);
            g = ((int) data.get() & 0xff);
            b = ((int) data.get() & 0xff);
            a = ((int) data.get() & 0xff);
            unknown1 = ((int) data.get() & 0xff);
            unknown2 = ((int) data.get() & 0xff);
            unknown3 = ((int) data.get() & 0xff);
        }

        return true;
    }
}
