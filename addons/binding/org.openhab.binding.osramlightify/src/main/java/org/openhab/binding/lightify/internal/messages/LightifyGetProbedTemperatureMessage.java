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

import org.eclipse.smarthome.core.thing.Thing;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyException;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyMessageTooLongException;
import org.openhab.binding.osramlightify.internal.messages.LightifyGetDeviceInfoMessage;

/**
 * Get a response to a temperature limits probe message.
 *
 * @author Mike Jagdis - Initial contribution
 */
public final class LightifyGetProbedTemperatureMessage extends LightifyGetDeviceInfoMessage implements LightifyMessage {

    private final Logger logger = LoggerFactory.getLogger(LightifyGetProbedTemperatureMessage.class);

    private final String propertyName;
    private final Thing thing;

    public LightifyGetProbedTemperatureMessage(Thing thing, String deviceAddress, String propertyName) {
        super(deviceAddress);

        this.thing = thing;
        this.propertyName = propertyName;
    }

    @Override
    public String toString() {
        return super.toString()
            + ", propertyName = " + propertyName;
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
        super.handleResponse(bridgeHandler, data);

        if (reachable) {
            logger.debug("{}: {} = {}", thing.getUID(), propertyName, temperature);

            thing.setProperty(propertyName, Integer.toString(temperature));

        } else {
            logger.debug("{}: unreachable", thing.getUID());
        }

        return true;
    }
}
