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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.smarthome.core.thing.Thing;

import static org.openhab.binding.osramlightify.LightifyBindingConstants.MIN_TEMPERATURE;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.MAX_TEMPERATURE;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.handler.LightifyDeviceHandler;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyException;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyMessageTooLongException;
import org.openhab.binding.osramlightify.internal.messages.LightifyBaseGetDeviceInfoMessage;

/**
 * Get a response to a temperature limits probe message.
 *
 * @author Mike Jagdis - Initial contribution
 */
public final class LightifyGetProbedTemperatureMessage extends LightifyBaseGetDeviceInfoMessage implements LightifyMessage {

    private final Logger logger = LoggerFactory.getLogger(LightifyGetProbedTemperatureMessage.class);

    private final Thing thing;
    private final String propertyName;

    public LightifyGetProbedTemperatureMessage(LightifyDeviceHandler deviceHandler, String propertyName) {
        super(deviceHandler);

        this.thing = deviceHandler.getThing();
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

    @Override
    public boolean handleResponse(LightifyBridgeHandler bridgeHandler, ByteBuffer data) throws LightifyException {
        if (super.handleResponse(bridgeHandler, data)) {
            logger.debug("{}: {} = {}", thing.getUID(), propertyName, state.temperature);

            if (state.temperature < MIN_TEMPERATURE) {
                state.temperature = MIN_TEMPERATURE;
            } else if (state.temperature > MAX_TEMPERATURE) {
                state.temperature = MAX_TEMPERATURE;
            }

            thing.setProperty(propertyName, Integer.toString(state.temperature));

            return true;
        }

        return false;
    }
}
