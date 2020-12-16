/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.gpio.internal.handler;

import static org.openhab.binding.gpio.internal.GPIOBindingConstants.THING_TYPE_DIGITAL_OUTPUT_CHANNEL;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.xeli.jpigpio.GPIO;
import eu.xeli.jpigpio.PigpioException;

/**
 * Thing Handler for digital GPIO outputs.
 *
 * @author Nils Bauer - Initial contribution
 */
@NonNullByDefault
public class GPIODigitalOutputHandler extends GPIOHandler {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(GPIODigitalOutputHandler.class);

    /**
     * Instantiates a new GPIO digital output handler.
     *
     * @param thing the thing
     */
    public GPIODigitalOutputHandler(Thing thing) {
        super(thing, THING_TYPE_DIGITAL_OUTPUT_CHANNEL, false);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if (command instanceof OnOffType) {
            OnOffType s = (OnOffType) command;
            try {
                setValue(s);
            } catch (PigpioException e) {
                logger.warn("An error occured while changing the gpio value: {}", e.getMessage());
            }
        }
    }

    /**
     * Sets the value (inverted).
     *
     * @param onOffType the value
     * @throws PigpioException the pigpio exception
     */
    private void setValue(OnOffType onOffType) throws PigpioException {
        GPIO gpio = this.gpio;
        if (gpio == null) {
            logger.warn("Cannot set gpio value because gpio is null");
        } else {
            gpio.setValue(invert != (onOffType == OnOffType.ON));
        }
    }
}
