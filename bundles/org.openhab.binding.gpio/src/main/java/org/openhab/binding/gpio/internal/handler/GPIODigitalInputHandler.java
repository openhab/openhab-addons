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

import static org.openhab.binding.gpio.internal.GPIOBindingConstants.THING_TYPE_DIGITAL_INPUT_CHANNEL;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.gpio.internal.configuration.GPIOInputConfiguration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.xeli.jpigpio.Alert;
import eu.xeli.jpigpio.GPIO;
import eu.xeli.jpigpio.JPigpio;
import eu.xeli.jpigpio.PigpioException;

/**
 * Thing Handler for digital GPIO inputs.
 *
 * @author Nils Bauer - Initial contribution
 */
@NonNullByDefault
public class GPIODigitalInputHandler extends GPIOHandler {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(GPIODigitalInputHandler.class);

    /** The last change. */
    private Date lastChanged = new Date();

    /**
     * Instantiates a new GPIO digital input handler.
     *
     * @param thing the thing
     */
    public GPIODigitalInputHandler(Thing thing) {
        super(thing, THING_TYPE_DIGITAL_INPUT_CHANNEL, true);
    }

    @Override
    public void initialize() {
        super.initialize();

        GPIOInputConfiguration config = getConfigAs(GPIOInputConfiguration.class);
        Integer debouncingTime = config.debouncingTime;
        try {
            JPigpio jPigpio = this.jPigpio;
            GPIO gpio = this.gpio;
            if (jPigpio == null) {
                logger.warn("Cannot setup gpio change alert because jPigpio is null");
                return;
            }
            if (gpio == null) {
                logger.warn("Cannot setup gpio change alert because gpio is null");
                return;
            }
            jPigpio.gpioSetAlertFunc(gpio.getPin(), new Alert() {

                @Override
                public void alert(int gpio, int level, long tick) {
                    lastChanged = new Date();

                    Date thisChange = new Date();
                    scheduler.schedule(() -> {
                        try {
                            // Check if value changed over time
                            if (!thisChange.before(lastChanged)) {
                                Channel channel = getThing().getChannel(THING_TYPE_DIGITAL_INPUT_CHANNEL);
                                if (channel == null) {
                                    logger.warn("Cannot find Channel: " + THING_TYPE_DIGITAL_INPUT_CHANNEL);
                                    return;
                                }
                                OnOffType value = getValue();
                                if (value == null) {
                                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                                            "GPIO is null");
                                } else {
                                    updateState(channel.getUID(), value);
                                }
                            }
                        } catch (PigpioException e) {
                            logger.warn("Unknown pigpio exception", e);
                        }
                    }, debouncingTime, TimeUnit.MILLISECONDS);
                }
            });
        } catch (PigpioException e) {
            if (e.getErrorCode() == PigpioException.PI_BAD_GPIO) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Bad GPIO Pin");
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        e.getLocalizedMessage());
            }
        }
    }
}
