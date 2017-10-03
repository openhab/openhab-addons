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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.handler.LightifyDeviceHandler;
import org.openhab.binding.osramlightify.internal.LightifyDeviceState;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyException;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyMessageTooLongException;
import org.openhab.binding.osramlightify.internal.messages.LightifyBaseMessage;

/**
 * Get the state of a single device.
 *
 * @author Mike Jagdis - Initial contribution
 */
abstract public class LightifyBaseGetDeviceInfoMessage extends LightifyBaseMessage implements LightifyMessage {

    private final Logger logger = LoggerFactory.getLogger(LightifyBaseGetDeviceInfoMessage.class);

    protected int unknown0;

    protected LightifyDeviceState state = new LightifyDeviceState();

    protected int unknown1;
    protected int unknown2;
    protected int unknown3;

    public LightifyBaseGetDeviceInfoMessage(LightifyDeviceHandler deviceHandler) {
        super(deviceHandler, Command.GET_DEVICE_INFO);
    }

    @Override
    public String toString() {
        String string = super.toString();

        if (!isResponse()) {
            return string;
        }

        return string
            + ", unknown0 = " + unknown0
            + ", power = " + state.power
            + ", luminance = " + state.luminance
            + ", temperature = " + state.temperature
            + ", r = " + state.r
            + ", g = " + state.g
            + ", b = " + state.b
            + ", a = " + state.a
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

    @Override
    public boolean handleResponse(LightifyBridgeHandler bridgeHandler, ByteBuffer data) throws LightifyException {
        super.handleResponse(bridgeHandler, data);

        data.getShort(); // deviceNumber
        String deviceAddress = decodeDeviceAddress(data);

        int reachability = ((int) data.get() & 0xff);

        if (reachability == 0) {
            state.reachable = 2;
            unknown0 = ((int) data.get() & 0xff);
            state.power = ((int) data.get() & 0xff);
            state.luminance = ((int) data.get() & 0xff);
            state.temperature = ((int) data.getShort() & 0xffff);
            state.r = ((int) data.get() & 0xff);
            state.g = ((int) data.get() & 0xff);
            state.b = ((int) data.get() & 0xff);
            state.a = ((int) data.get() & 0xff);
            unknown1 = ((int) data.get() & 0xff);
            unknown2 = ((int) data.get() & 0xff);
            unknown3 = ((int) data.get() & 0xff);

            return true;
        }

        logger.debug("{}: reachability = {}", deviceAddress, reachability);

        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
        }

        return false;
    }
}
