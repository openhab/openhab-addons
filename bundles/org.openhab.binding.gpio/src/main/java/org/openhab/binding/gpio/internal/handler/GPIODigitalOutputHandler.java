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

import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gpio.internal.configuration.GPIOOutputConfiguration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.xeli.jpigpio.GPIO;
import eu.xeli.jpigpio.JPigpio;
import eu.xeli.jpigpio.PigpioException;

/**
 * Thing Handler for digital GPIO outputs.
 *
 * @author Nils Bauer - Initial contribution
 */
@NonNullByDefault
public class GPIODigitalOutputHandler extends BaseThingHandler {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(GPIODigitalOutputHandler.class);

    /** The JPigpio instance. */
    private @Nullable JPigpio jPigpio;

    /** The gpio pin handled by this handler. */
    private @Nullable GPIO gpio;

    /** The config. */
    private @Nullable GPIOOutputConfiguration config;

    /**
     * Instantiates a new GPIO digital output handler.
     *
     * @param thing the thing
     */
    public GPIODigitalOutputHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            try {
                Channel channel = getThing().getChannel(THING_TYPE_DIGITAL_OUTPUT_CHANNEL);
                if (channel == null) {
                    logger.warn("Cannot find Channel: " + THING_TYPE_DIGITAL_OUTPUT_CHANNEL);
                    return;
                }
                updateState(channel.getUID(), getValue());
            } catch (PigpioException e) {
                logger.warn("An error occured while getting the gpio value: {}", e.getMessage());
            }
        } else if (command instanceof OnOffType) {
            OnOffType s = (OnOffType) command;
            try {
                setValue(s);
            } catch (PigpioException e) {
                logger.warn("An error occured while changing the gpio value: {}", e.getMessage());
            }
        }
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE);
            logger.warn("Bridge is null");
            return;
        }
        PigpioBridgeHandler handler = (PigpioBridgeHandler) bridge.getHandler();
        if (handler == null) {
            updateStatus(ThingStatus.OFFLINE);
            logger.warn("Handler is null");
            return;
        }
        try {
            jPigpio = handler.getJPiGpio().orElseThrow();

        } catch (NoSuchElementException e) {
            updateStatus(ThingStatus.OFFLINE);
            logger.warn("JPigpio is null");
            return;
        }
        config = getConfigAs(GPIOOutputConfiguration.class);
        try {
            gpio = new GPIO(jPigpio, config.gpioId, 0);
            updateStatus(ThingStatus.ONLINE);
        } catch (NumberFormatException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Pin not numeric");
            logger.debug("Non numeric pin number", e);
        } catch (PigpioException e) {
            if (e.getErrorCode() == PigpioException.PI_BAD_GPIO) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Bad GPIO Pin");
                logger.debug("Bad GPIO Pin", e);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        e.getLocalizedMessage());
                logger.debug("Unknown jpigpio exception", e);
            }
        }
    }

    /**
     * Gets the (inverted) value.
     *
     * @return the value
     * @throws PigpioException the pigpio exception
     */
    private OnOffType getValue() throws PigpioException {
        return config.invert != gpio.getValue() ? OnOffType.ON : OnOffType.OFF;
    }

    /**
     * Sets the value (inverted).
     *
     * @param onOffType the value
     * @throws PigpioException the pigpio exception
     */
    private void setValue(OnOffType onOffType) throws PigpioException {
        gpio.setValue(config.invert != (onOffType == OnOffType.ON));
    }
}
