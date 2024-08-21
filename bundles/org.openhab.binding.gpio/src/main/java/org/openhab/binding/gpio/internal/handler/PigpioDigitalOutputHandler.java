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

import static org.openhab.binding.gpio.internal.GPIOBindingConstants.*;

import java.math.BigDecimal;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gpio.internal.ChannelConfigurationException;
import org.openhab.binding.gpio.internal.configuration.GPIOOutputConfiguration;
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
 * Thing Handler for digital GPIO outputs.
 *
 * @author Nils Bauer - Initial contribution
 * @author Jan N. Klug - Channel redesign
 * @author Jeremy Rumpf - Refactored for network disruptions
 */
@NonNullByDefault
public class PigpioDigitalOutputHandler implements ChannelHandler {
    private final Logger logger = LoggerFactory.getLogger(PigpioDigitalOutputHandler.class);

    private final GPIOOutputConfiguration configuration;
    private final ScheduledExecutorService scheduler;
    private final Integer gpioId;
    private Integer pulseTimeout = -1;
    private @Nullable String pulseCommand = "";
    private @Nullable GPIO gpio;
    private @Nullable Consumer<State> updateStatus;

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
    public PigpioDigitalOutputHandler(GPIOOutputConfiguration configuration, ScheduledExecutorService scheduler,
            Consumer<State> updateStatus) throws PigpioException, ChannelConfigurationException {
        this.configuration = configuration;
        this.gpioId = configuration.gpioId;
        this.scheduler = scheduler;
        this.updateStatus = updateStatus;

        if (this.gpioId <= 0) {
            throw new ChannelConfigurationException("Invalid gpioId value.");
        }

        if (configuration.pulse.compareTo(BigDecimal.ZERO) > 0) {
            try {
                this.pulseTimeout = configuration.pulse.intValue();
            } catch (Exception e) {
                throw new ChannelConfigurationException("Invalid expire value.");
            }
        }

        if (configuration.pulseCommand.length() > 0) {
            this.pulseCommand = configuration.pulseCommand.toUpperCase();
            if (!PULSE_ON.equals(pulseCommand) && !PULSE_OFF.equals(pulseCommand)
                    && !PULSE_BLINK.equals(pulseCommand)) {
                throw new ChannelConfigurationException("Invalid pulseCommand value.");
            }
        }
    }

    /**
     * Future to track pulse commands.
     */
    private @Nullable Future<?> pulseJob = null;
    private @Nullable OnOffType lastPulseCommand;

    /**
     * Used to only keep a single gpio command handle in flight
     * at a time.
     */
    private Object handleLock = new Object();

    @Override
    public void handleCommand(Command command) throws PigpioException {
        synchronized (handleLock) {
            GPIO lgpio = this.gpio;
            Consumer<State> lupdateStatus = this.updateStatus;
            Future<?> job = this.pulseJob;

            if (lgpio == null || lupdateStatus == null) {
                logger.warn("An attempt to submit a command was made when the pigpiod was offline: {}",
                        command.toString());
                return;
            }

            if (command instanceof RefreshType) {
                lupdateStatus.accept(OnOffType.from(configuration.invert != lgpio.getValue()));
            } else if (command instanceof OnOffType) {
                lgpio.setValue(configuration.invert != (OnOffType.ON.equals(command)));
                lupdateStatus.accept((State) command);

                if (this.pulseTimeout > 0 && this.pulseCommand != null) {
                    if (job != null) {
                        job.cancel(false);
                    }

                    this.pulseJob = scheduler.schedule(() -> handlePulseCommand(command), this.pulseTimeout,
                            TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    public void handlePulseCommand(@Nullable Command command) {
        OnOffType eCommand = OnOffType.OFF;

        try {
            synchronized (handleLock) {
                GPIO lgpio = this.gpio;
                Consumer<State> lupdateStatus = this.updateStatus;
                Future<?> job = this.pulseJob;

                if (lgpio == null) {
                    return;
                }

                if (command instanceof OnOffType) {
                    if (this.pulseCommand != null) {
                        if (PULSE_ON.equals(this.pulseCommand)) {
                            eCommand = OnOffType.ON;
                        } else if (PULSE_OFF.equals(this.pulseCommand)) {
                            eCommand = OnOffType.OFF;
                        } else if (PULSE_BLINK.equals(this.pulseCommand)) {
                            if (OnOffType.ON.equals(command)) {
                                eCommand = OnOffType.OFF;
                            } else if (OnOffType.OFF.equals(command)) {
                                eCommand = OnOffType.ON;
                            }
                        }
                    } else {
                        if (OnOffType.ON.equals(command)) {
                            eCommand = OnOffType.OFF;
                        } else if (OnOffType.OFF.equals(command)) {
                            eCommand = OnOffType.ON;
                        }
                    }

                    logger.debug("gpio pulse command : {} {}", this.gpioId, eCommand.toString());

                    lgpio.setValue(configuration.invert != (OnOffType.ON.equals(eCommand)));
                    if (lupdateStatus != null) {
                        lupdateStatus.accept((State) eCommand);
                    }

                    lastPulseCommand = eCommand;

                    if (PULSE_BLINK.equals(this.pulseCommand) && this.pulseTimeout > 0) {
                        final OnOffType feCommand = eCommand;
                        if (job != null) {
                            job.cancel(false);
                        }
                        this.pulseJob = scheduler.schedule(() -> handlePulseCommand(feCommand), this.pulseTimeout,
                                TimeUnit.MILLISECONDS);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn(
                    "Pulse command exception, {} command may not have been received by pigpiod resulting in an unknown state:",
                    eCommand.toString(), e);
        }
    }

    /**
     * Configures the GPIO pin for OUTPUT.
     */
    public void listen(@Nullable JPigpio jPigpio) throws PigpioException {
        if (jPigpio == null) {
            this.gpio = null;
            return;
        }

        GPIO lgpio = new GPIO(jPigpio, gpioId, JPigpio.PI_OUTPUT);
        this.gpio = lgpio;
        lgpio.setDirection(JPigpio.PI_OUTPUT);
        scheduleBlink();
    }

    private void scheduleBlink() {
        synchronized (handleLock) {
            Future<?> job = this.pulseJob;

            if (this.pulseTimeout > 0 && PULSE_BLINK.equals(configuration.pulseCommand)) {
                if (job != null) {
                    job.cancel(false);
                }
                if (this.lastPulseCommand != null) {
                    scheduler.schedule(() -> handlePulseCommand(this.lastPulseCommand), this.pulseTimeout,
                            TimeUnit.MILLISECONDS);
                } else {
                    this.pulseJob = scheduler.schedule(() -> handlePulseCommand(OnOffType.OFF), this.pulseTimeout,
                            TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    @Override
    public void dispose() {
        synchronized (handleLock) {
            Future<?> job = this.pulseJob;

            if (job != null) {
                job.cancel(true);
            }
            this.updateStatus = null;
            this.gpio = null;
        }
    }
}
