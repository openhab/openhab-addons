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

import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gpio.internal.configuration.GPIOConfiguration;
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
 * Abstract Thing Handler for GPIO.
 *
 * @author Nils Bauer - Initial contribution
 */
@NonNullByDefault
public abstract class GPIOHandler extends BaseThingHandler {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(GPIOHandler.class);

    /** The gpio pin handled by this handler. */
    protected @Nullable GPIO gpio;

    /** If the pin should be inverted */
    protected Boolean invert = false;

    /** The JPigpio instance */
    protected @Nullable JPigpio jPigpio;

    /** Name of the channel */
    private String channelName;

    /** Definies if it is an input pin: true */
    private Boolean input;

    /**
     * Instantiates a new GPIO handler.
     * 
     * @param thing the thing
     * @param channelName the name of the channel
     * @param input True if it is an input pin
     */
    public GPIOHandler(Thing thing, String channelName, Boolean input) {
        super(thing);
        this.channelName = channelName;
        this.input = input;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            try {
                Channel channel = getThing().getChannel(channelName);
                if (channel == null) {
                    logger.warn("Cannot find Channel: {}", channelName);
                    return;
                }
                OnOffType value = getValue();
                if (value == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "GPIO is null");
                } else {
                    updateState(channel.getUID(), value);
                }
            } catch (PigpioException e) {
                logger.warn("An error occured while changing the gpio value: {}", e.getMessage());
            }
        }
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            return;
        }
        PigpioBridgeHandler handler = (PigpioBridgeHandler) bridge.getHandler();
        if (handler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR);
            return;
        }
        JPigpio jPigpio = null;
        try {
            jPigpio = handler.getJPiGpio();
        } catch (NoSuchElementException ignore) {
        }
        if (jPigpio == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "JPigpio is null");
            return;
        }
        this.jPigpio = jPigpio;
        GPIOConfiguration config = getConfigAs(GPIOConfiguration.class);
        Integer gpioId = config.gpioId;
        this.invert = config.invert;
        if (gpioId == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "GPIO Pin not set.");
            return;
        }

        try {
            gpio = new GPIO(jPigpio, gpioId, input ? 1 : 0);
            updateStatus(ThingStatus.ONLINE);
        } catch (PigpioException e) {
            if (e.getErrorCode() == PigpioException.PI_BAD_GPIO) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Bad GPIO Pin");
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        e.getLocalizedMessage());
            }
        }
    }

    /**
     * Gets the (inverted) value.
     *
     * @return the value
     * @throws PigpioException the pigpio exception
     */
    protected @Nullable OnOffType getValue() throws PigpioException {
        GPIO gpio = this.gpio;
        if (gpio == null) {
            return null;
        }
        return invert != gpio.getValue() ? OnOffType.ON : OnOffType.OFF;
    }
}
