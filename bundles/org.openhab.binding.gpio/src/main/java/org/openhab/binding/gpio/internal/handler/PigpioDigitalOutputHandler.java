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
    private String pulseCommand = "";
    @Nullable
    private GPIO gpio;
    @Nullable
    private Consumer<State> updateStatus;

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

        if (this.gpioId == null || this.gpioId <= 0) {
            throw new ChannelConfigurationException("Invalid gpioId value.");
        }

        if (configuration.pulse != null && configuration.pulse.compareTo(BigDecimal.ZERO) > 0) {
            try {
                this.pulseTimeout = configuration.pulse.intValue();
            } catch (Exception e) {
                throw new ChannelConfigurationException("Invalid expire value.");
            }
        }

        if (configuration.pulseCommand != null && configuration.pulseCommand.length() > 0) {
            this.pulseCommand = configuration.pulseCommand.toUpperCase();
            logger.debug("gpio config pulseCommand : {} {}", this.gpioId, this.pulseCommand);
            if (!"ON".equals(pulseCommand) && !"OFF".equals(pulseCommand) && !"BLINK".equals(pulseCommand)) {
                throw new ChannelConfigurationException("Invalid pulseCommand value.");
            }
        }
    }

    /**
     * Future to track pulse commands.
     */
    @Nullable
    private Future<?> pulseJob = null;
    private OnOffType lastPulseCommand;

    /**
     * Used to only keep a single gpio command handle in flight
     * at a time.
     */
    private Object handleLock = new Object();

    @Override
    public void handleCommand(Command command) throws PigpioException {
        synchronized (handleLock) {
            if (gpio == null) {
                logger.warn("An attempt to submit a command was made when the gpio was offline: {}",
                        command.toString());
                return;
            }

            if (command instanceof RefreshType) {
                if (updateStatus != null) {
                    updateStatus.accept(OnOffType.from(configuration.invert != gpio.getValue()));
                }
            } else if (command instanceof OnOffType) {
                gpio.setValue(configuration.invert != (OnOffType.ON.equals(command)));
                if (updateStatus != null) {
                    updateStatus.accept((State) command);
                }

                if (this.pulseTimeout > 0 && this.pulseCommand.length() > 0) {
                    if (pulseJob != null) {
                        pulseJob.cancel(false);
                    }
                    logger.debug("gpio pulse timeout : {} {}", this.gpioId, this.pulseTimeout);
                    this.pulseJob = scheduler.schedule(() -> handlePulseCommand(command), this.pulseTimeout,
                            TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    public void handlePulseCommand(Command command) {
        OnOffType eCommand = OnOffType.OFF;

        try {
            synchronized (handleLock) {
                if (gpio == null) {
                    return;
                }

                if (command instanceof OnOffType) {
                    if (this.pulseCommand != null) {
                        if ("ON".equals(this.pulseCommand)) {
                            eCommand = OnOffType.ON;
                        } else if ("OFF".equals(this.pulseCommand)) {
                            eCommand = OnOffType.OFF;
                        } else if ("BLINK".equals(this.pulseCommand)) {
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

                    gpio.setValue(configuration.invert != (OnOffType.ON.equals(eCommand)));
                    if (updateStatus != null) {
                        updateStatus.accept((State) eCommand);
                    }

                    lastPulseCommand = eCommand;

                    if ("BLINK".equals(this.pulseCommand) && this.pulseTimeout > 0) {
                        final OnOffType feCommand = eCommand;
                        if (pulseJob != null) {
                            pulseJob.cancel(false);
                        }
                        pulseJob = scheduler.schedule(() -> handlePulseCommand(feCommand), this.pulseTimeout,
                                TimeUnit.MILLISECONDS);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Pulse command exception :", e);
        }
    }

    /**
     * Configures the GPIO pin for OUTPUT.
     */
    public void listen(JPigpio jPigpio) throws PigpioException {
        this.gpio = new GPIO(jPigpio, gpioId, JPigpio.PI_OUTPUT);
        this.gpio.setDirection(JPigpio.PI_OUTPUT);
        scheduleBlink();
    }

    private void scheduleBlink() {
        synchronized (handleLock) {
            if (this.pulseTimeout > 0 && "BLINK".equals(configuration.pulseCommand)) {
                if (pulseJob != null) {
                    pulseJob.cancel(false);
                }
                if (this.lastPulseCommand != null) {
                    scheduler.schedule(() -> handlePulseCommand(this.lastPulseCommand), this.pulseTimeout,
                            TimeUnit.MILLISECONDS);
                } else {
                    pulseJob = scheduler.schedule(() -> handlePulseCommand(OnOffType.OFF), this.pulseTimeout,
                            TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    @Override
    public void dispose() {
        synchronized (handleLock) {
            if (pulseJob != null) {
                pulseJob.cancel(true);
            }
            updateStatus = null;
            gpio = null;
        }
    }
}
