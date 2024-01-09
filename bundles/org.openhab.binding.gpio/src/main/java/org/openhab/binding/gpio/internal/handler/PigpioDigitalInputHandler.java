/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.eclipse.jdt.annotation.Nullable;
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
    private @Nullable GPIO gpio;
    private @Nullable Consumer<State> updateStatus;
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

        if (this.gpioId <= 0) {
            throw new ChannelConfigurationException("Invalid gpioId value: " + this.gpioId);
        }

        String pullupdownStr = configuration.pullupdown.toUpperCase();
        if (pullupdownStr.equals(GPIOBindingConstants.PUD_DOWN)) {
            this.pullupdown = JPigpio.PI_PUD_DOWN;
        } else if (pullupdownStr.equals(GPIOBindingConstants.PUD_UP)) {
            this.pullupdown = JPigpio.PI_PUD_UP;
        } else if (pullupdownStr.equals(GPIOBindingConstants.PUD_OFF)) {
            this.pullupdown = JPigpio.PI_PUD_OFF;
        } else {
            throw new ChannelConfigurationException("Invalid pull up/down value.");
        }

        String edgeModeStr = configuration.edgeMode;
        if (edgeModeStr.equals(GPIOBindingConstants.EDGE_RISING)) {
            this.edgeMode = JPigpio.PI_RISING_EDGE;
        } else if (edgeModeStr.equals(GPIOBindingConstants.EDGE_FALLING)) {
            this.edgeMode = JPigpio.PI_FALLING_EDGE;
        } else if (edgeModeStr.equals(GPIOBindingConstants.EDGE_EITHER)) {
            this.edgeMode = JPigpio.PI_EITHER_EDGE;
        } else {
            throw new ChannelConfigurationException("Invalid edgeMode value.");
        }

        this.listener = new GPIOListener(this.gpioId, this.edgeMode) {
            @Override
            public void alert(int gpio, int level, long tick) {
                alertFunc(gpio, level, tick);
            }
        };
    }

    public void alertFunc(int gpio, int level, long tick) {
        this.lastChanged = new Date();
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
            GPIO lgpio = this.gpio;
            Consumer<State> lupdateStatus = this.updateStatus;

            if (lgpio == null || lupdateStatus == null) {
                // We raced and went offline in the meantime.
                return;
            }

            try {
                // Check if value changed over time
                if (!thisChange.before(lastChanged)) {
                    lupdateStatus.accept(OnOffType.from(configuration.invert != lgpio.getValue()));
                }
            } catch (PigpioException e) {
                // -99999999 is communication related, we will let the Thing connect poll refresh it.
                if (e.getErrorCode() != -99999999) {
                    logger.debug("Debounce exception :", e);
                }
            }
        }
    }

    /**
     * Establishes or re-establishes a listener on the JPigpio
     * instance for the configured gpio pin.
     */
    public void listen(@Nullable JPigpio jPigpio) throws PigpioException {
        if (jPigpio == null) {
            this.gpio = null;
            return;
        }

        GPIO lgpio = new GPIO(jPigpio, this.gpioId, JPigpio.PI_INPUT);
        this.gpio = lgpio;

        try {
            lgpio.setDirection(JPigpio.PI_INPUT);
            jPigpio.gpioSetPullUpDown(lgpio.getPin(), this.pullupdown);
            jPigpio.removeCallback(this.listener);
        } catch (PigpioException e) {
            // If there is a communication error, the set alert below will throw.
            if (e.getErrorCode() != -99999999) {
                logger.debug("Listen exception :", e);
            }
        }

        jPigpio.gpioSetAlertFunc(lgpio.getPin(), this.listener);
    }

    @Override
    public void handleCommand(Command command) throws PigpioException {
        GPIO lgpio = this.gpio;
        Consumer<State> lupdateStatus = this.updateStatus;

        if (lgpio == null || lupdateStatus == null) {
            logger.warn("An attempt to submit a command was made when pigpiod was offline: {}", command.toString());
            return;
        }

        if (command instanceof RefreshType) {
            lupdateStatus.accept(OnOffType.from(configuration.invert != lgpio.getValue()));
        }
    }

    @Override
    public void dispose() {
        synchronized (debounceLock) {
            GPIO lgpio = this.gpio;

            updateStatus = null;
            if (lgpio != null) {
                JPigpio ljPigpio = lgpio.getPigpio();
                if (ljPigpio != null) {
                    try {
                        ljPigpio.removeCallback(listener);
                    } catch (PigpioException e) {
                        // Best effort to remove listener,
                        // the command socket could already be dead.
                        if (e.getErrorCode() != -99999999) {
                            logger.debug("Dispose exception :", e);
                        }
                    }
                }
            }
        }
    }
}
