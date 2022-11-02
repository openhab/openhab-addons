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
import org.openhab.binding.gpio.internal.ChannelConfigurationException;
import org.openhab.binding.gpio.internal.GPIOBindingConstants;
import org.openhab.binding.gpio.internal.configuration.GPIOInputConfiguration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.xeli.jpigpio.GPIO;
import eu.xeli.jpigpio.GPIOListener;
import eu.xeli.jpigpio.JPigpio;
import eu.xeli.jpigpio.PigpioException;

/**
 * Thing Handler for digital GPIO inputs.
 *
 * @author Nils Bauer - Initial contribution
 * @author Jan N. Klug - Channel redesign
 * @author Martin Dagarin - Pull Up/Down GPIO pin
 * @author Jeremy Rumpf - Refactored for network disruptions
 */
@NonNullByDefault
public class PigpioDigitalInputHandler implements ChannelHandler {
    private final Logger logger = LoggerFactory.getLogger(PigpioDigitalInputHandler.class);
    private Date lastChanged = new Date();

    private final GPIOInputConfiguration configuration;
    private final ScheduledExecutorService scheduler;
    private final Integer gpioId;
    private GPIO gpio;
    private Consumer<State> updateStatus;
    private Integer pullupdown = JPigpio.PI_PUD_OFF;
    private final GPIOListener listener;
    private int edgeMode = JPigpio.PI_EITHER_EDGE;

    /**
     * Constructor for PigpioDigitalOutputHandler
     * 
     * @param configuration The channel configuration
     * @param jPigpio The jPigpio instance
     * @param updateStatus Is called when the state should be changed
     * 
     * @throws PigpioException Can be thrown by Pigpio
     * @throws ChannelConfigurationException Thrown on configuration error
     */
    public PigpioDigitalInputHandler(GPIOInputConfiguration configuration, ScheduledExecutorService scheduler,
            Consumer<State> updateStatus) throws PigpioException, ChannelConfigurationException {
        this.configuration = configuration;
        this.scheduler = scheduler;
        this.updateStatus = updateStatus;
        this.gpioId = configuration.gpioId;

        if (this.gpioId == null || this.gpioId <= 0) {
            throw new ChannelConfigurationException("Invalid gpioId value.");
        }

        String pullupdownStr = configuration.pullupdown.toUpperCase();
        if (pullupdownStr.equals(GPIOBindingConstants.PUD_DOWN)) {
            pullupdown = JPigpio.PI_PUD_DOWN;
        } else if (pullupdownStr.equals(GPIOBindingConstants.PUD_UP)) {
            pullupdown = JPigpio.PI_PUD_UP;
        } else if (pullupdownStr.equals(GPIOBindingConstants.PUD_OFF)) {
            pullupdown = JPigpio.PI_PUD_OFF;
        } else {
            throw new ChannelConfigurationException("Invalid pull up/down value.");
        }

        String edgeModeStr = configuration.edgeMode;
        if (edgeModeStr.equals(GPIOBindingConstants.EDGE_RISING)) {
            edgeMode = JPigpio.PI_RISING_EDGE;
        } else if (edgeModeStr.equals(GPIOBindingConstants.EDGE_FALLING)) {
            edgeMode = JPigpio.PI_FALLING_EDGE;
        } else if (edgeModeStr.equals(GPIOBindingConstants.EDGE_EITHER)) {
            edgeMode = JPigpio.PI_EITHER_EDGE;
        } else {
            throw new ChannelConfigurationException("Invalid edgeMode value.");
        }

        // Maybe add LEVEL as well as EDGE?
        this.listener = new GPIOListener(this.gpioId, edgeMode) {
            @Override
            public void alert(int gpio, int level, long tick) {
                alertFunc(gpio, level, tick);
            }
        };
    }

    public void alertFunc(int gpio, int level, long tick) {
        lastChanged = new Date();
        Date thisChange = new Date();
        if (configuration.debouncingTime > 0) {
            scheduler.schedule(() -> afterDebounce(thisChange), configuration.debouncingTime, TimeUnit.MILLISECONDS);
        } else {
            afterDebounce(thisChange);
        }
    }

    /**
     * Syncronize debouncing callbacks to
     * ensure they are not out of order.
     */
    private Object debounceLock = new Object();

    private void afterDebounce(Date thisChange) {
        synchronized (debounceLock) {
            try {
                // Check if value changed over time
                if (!thisChange.before(lastChanged)) {
                    if (updateStatus != null) {
                        updateStatus.accept(OnOffType.from(configuration.invert != this.gpio.getValue()));
                    }
                }
            } catch (PigpioException e) {
                // -99999999 is communication related, we will let the Thing connect poll refresh it.
                if (e.getErrorCode() != -99999999) {
                    logger.error("Debounce exception :", e);
                }
            }
        }
    }

    /**
     * Establishes or re-establishes a listener on the JPigpio
     * instance for the configured gpio pin.
     */
    public void listen(JPigpio jPigpio) throws PigpioException {
        this.gpio = new GPIO(jPigpio, this.gpioId, JPigpio.PI_INPUT);

        try {
            gpio.setDirection(JPigpio.PI_INPUT);
            jPigpio.gpioSetPullUpDown(gpio.getPin(), pullupdown);
            jPigpio.removeCallback(this.listener);
        } catch (PigpioException e) {
            // If there is a communication error, the set alert below will throw.
            if (e.getErrorCode() != -99999999) {
                logger.error("Listen exception :", e);
            }
        }

        jPigpio.gpioSetAlertFunc(gpio.getPin(), this.listener);
    }

    @Override
    public void handleCommand(Command command) throws PigpioException {
        if (gpio == null) {
            logger.warn("An attempt to submit a command was made when pigpiod was offline: {}", command.toString());
            return;
        }

        if (command instanceof RefreshType) {
            if (updateStatus != null) {
                updateStatus.accept(OnOffType.from(configuration.invert != gpio.getValue()));
            }
        }
    }

    @Override
    public void dispose() {
        synchronized (debounceLock) {
            updateStatus = null;
            if (gpio != null) {
                JPigpio jPigpio = gpio.getPigpio();
                if (jPigpio != null && listener != null) {
                    try {
                        jPigpio.removeCallback(listener);
                    } catch (PigpioException e) {
                        // Best effort to remove listener,
                        // the command socket could already be dead.
                        if (e.getErrorCode() != -99999999) {
                            logger.error("Dispose exception :", e);
                        }
                    }
                }
            }
        }
    }
}
