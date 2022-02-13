/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.gpio.internal.GPIOBindingConstants;
import org.openhab.binding.gpio.internal.InvalidPullUpDownException;
import org.openhab.binding.gpio.internal.NoGpioIdException;
import org.openhab.binding.gpio.internal.configuration.GPIOInputConfiguration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.xeli.jpigpio.GPIO;
import eu.xeli.jpigpio.JPigpio;
import eu.xeli.jpigpio.PigpioException;

/**
 * Thing Handler for digital GPIO inputs.
 *
 * @author Nils Bauer - Initial contribution
 * @author Jan N. Klug - Channel redesign
 * @author Martin Dagarin - Pull Up/Down GPIO pin
 */
@NonNullByDefault
public class PigpioDigitalInputHandler implements ChannelHandler {

    private final Logger logger = LoggerFactory.getLogger(PigpioDigitalInputHandler.class);
    private Date lastChanged = new Date();

    private final GPIOInputConfiguration configuration;
    private final GPIO gpio;
    private final Consumer<State> updateStatus;

    public PigpioDigitalInputHandler(GPIOInputConfiguration configuration, JPigpio jPigpio,
            ScheduledExecutorService scheduler, Consumer<State> updateStatus)
            throws PigpioException, InvalidPullUpDownException, NoGpioIdException {
        this.configuration = configuration;
        this.updateStatus = updateStatus;
        Integer gpioId = configuration.gpioId;
        if (gpioId == null) {
            throw new NoGpioIdException();
        }
        Integer pullupdown = JPigpio.PI_PUD_OFF;
        String pullupdownStr = configuration.pullupdown.toUpperCase();
        if (pullupdownStr.equals(GPIOBindingConstants.PUD_DOWN)) {
            pullupdown = JPigpio.PI_PUD_DOWN;
        } else if (pullupdownStr.equals(GPIOBindingConstants.PUD_UP)) {
            pullupdown = JPigpio.PI_PUD_UP;
        } else {
            if (!pullupdownStr.equals(GPIOBindingConstants.PUD_OFF))
                throw new InvalidPullUpDownException();
        }
        gpio = new GPIO(jPigpio, gpioId, JPigpio.PI_INPUT);
        jPigpio.gpioSetAlertFunc(gpio.getPin(), (gpio, level, tick) -> {
            lastChanged = new Date();
            Date thisChange = new Date();
            scheduler.schedule(() -> afterDebounce(thisChange), configuration.debouncingTime, TimeUnit.MILLISECONDS);
        });
        jPigpio.gpioSetPullUpDown(gpio.getPin(), pullupdown);
    }

    private void afterDebounce(Date thisChange) {
        try {
            // Check if value changed over time
            if (!thisChange.before(lastChanged)) {
                updateStatus.accept(OnOffType.from(configuration.invert != gpio.getValue()));
            }
        } catch (PigpioException e) {
            logger.warn("Unknown pigpio exception", e);
        }
    }

    @Override
    public void handleCommand(Command command) {
        if (command instanceof RefreshType) {
            try {
                updateStatus.accept(OnOffType.from(configuration.invert != gpio.getValue()));
            } catch (PigpioException e) {
                logger.warn("Unknown pigpio exception while handling Refresh", e);
            }
        }
    }
}
