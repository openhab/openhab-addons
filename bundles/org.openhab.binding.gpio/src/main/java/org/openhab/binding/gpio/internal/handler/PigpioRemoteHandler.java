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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gpio.internal.ChannelConfigurationException;
import org.openhab.binding.gpio.internal.configuration.GPIOInputConfiguration;
import org.openhab.binding.gpio.internal.configuration.GPIOOutputConfiguration;
import org.openhab.binding.gpio.internal.configuration.PigpioConfiguration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.xeli.jpigpio.JPigpio;
import eu.xeli.jpigpio.PigpioException;
import eu.xeli.jpigpio.PigpioSocket;

/**
 * Remote pigpio Handler
 *
 * This bridge is used to control remote pigpio instances.
 *
 * @author Nils Bauer - Initial contribution
 * @author Jan N. Klug - Channel redesign
 * @author Jeremy Rumpf - Improve JPigpio connection handling
 */
@NonNullByDefault
public class PigpioRemoteHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(PigpioRemoteHandler.class);
    private final Map<ChannelUID, ChannelHandler> channelHandlers = new HashMap<>();

    /**
     * Instantiates a new pigpio remote bridge handler.
     *
     * @param thing the thing
     */
    public PigpioRemoteHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            synchronized (this.connectionLock) {
                ChannelHandler channelHandler = channelHandlers.get(channelUID);

                if (channelHandler == null || !(ThingStatus.ONLINE.equals(thing.getStatus()))) {
                    // We raced with connectPollWorker and lost
                    return;
                }

                if (channelHandler instanceof PigpioDigitalInputHandler inputHandler) {
                    try {
                        inputHandler.handleCommand(command);
                    } catch (PigpioException pe) {
                        logger.warn("Input command exception on channel {} {}", channelUID, pe.toString());
                        if (pe.getErrorCode() == -99999999) {
                            runDisconnectActions();
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                                    pe.getLocalizedMessage());
                        }
                    }
                } else if (channelHandler instanceof PigpioDigitalOutputHandler outputHandler) {
                    try {
                        outputHandler.handleCommand(command);
                    } catch (PigpioException pe) {
                        logger.warn("Output command exception on channel {} {}", channelUID, pe.toString());
                        if (pe.getErrorCode() == -99999999) {
                            runDisconnectActions();
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                                    pe.getLocalizedMessage());
                        }
                    }
                } else {
                    logger.warn("Command received for an unknown channel: {}", channelUID);
                }
            }
        } catch (Exception e) {
            logger.warn("Command exception on channel {} {}", channelUID, e.toString());
        }
    }

    protected PigpioConfiguration config = new PigpioConfiguration();
    protected @Nullable JPigpio jPigpio = null;

    @Override
    public void initialize() {
        PigpioConfiguration lconfig = getConfigAs(PigpioConfiguration.class);
        this.config = lconfig;

        if (lconfig.host == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to PiGPIO Service on remote raspberry. IP address not set.");
            return;
        }
        if (lconfig.port < 1 && lconfig.port > 65535) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to PiGPIO Service on remote raspberry. Invalid Port.");
            return;
        }

        createChannelHandlers();

        logger.debug("gpio binding initialized");

        connectionJob = scheduler.submit(() -> {
            connectionPollWorker();
        });
    }

    protected void clearChannelHandlers() {
        for (ChannelHandler handler : channelHandlers.values()) {
            handler.dispose();
        }
        channelHandlers.clear();
    }

    protected void createChannelHandlers() {
        clearChannelHandlers();
        this.getThing().getChannels().forEach(channel -> {
            ChannelUID channelUID = channel.getUID();
            ChannelTypeUID type = channel.getChannelTypeUID();

            try {
                if (CHANNEL_TYPE_DIGITAL_INPUT.equals(type)) {
                    GPIOInputConfiguration configuration = channel.getConfiguration().as(GPIOInputConfiguration.class);
                    this.channelHandlers.put(channelUID, new PigpioDigitalInputHandler(configuration, scheduler,
                            state -> updateState(channelUID.getId(), state)));
                } else if (CHANNEL_TYPE_DIGITAL_OUTPUT.equals(type)) {
                    GPIOOutputConfiguration configuration = channel.getConfiguration()
                            .as(GPIOOutputConfiguration.class);
                    PigpioDigitalOutputHandler handler = new PigpioDigitalOutputHandler(configuration, scheduler,
                            state -> updateState(channelUID.getId(), state));
                    this.channelHandlers.put(channelUID, handler);
                }
            } catch (PigpioException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        String.format("Failed to initialize channel {} {}", channelUID, e.getLocalizedMessage()));
            } catch (ChannelConfigurationException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        String.format("Invalid configuration for channel {} {}", channelUID, e.getLocalizedMessage()));
            }
        });

        logger.debug("gpio channels initialized");
    }

    protected void setChannelJPigpio(@Nullable JPigpio jPigpio) throws PigpioException {
        if (this.channelHandlers.isEmpty()) {
            createChannelHandlers();
        }

        for (ChannelHandler handler : this.channelHandlers.values()) {
            handler.listen(jPigpio);
        }

        logger.debug("gpio jPigpio listening");
    }

    private @Nullable Future<?> connectionJob = null;
    /**
     * Syncronizes all socket related code
     * to avoid racing.
     */
    private Object connectionLock = new Object();

    protected void killConnectionPoll() {
        if (this.connectionJob != null) {
            synchronized (this.connectionLock) {
                if (this.connectionJob != null) {
                    Future<?> job = this.connectionJob;
                    this.connectionJob = null;
                    if (job != null) {
                        logger.debug("gpio connection poll : killing");
                        job.cancel(true);
                    }
                }
            }
        }
    }

    protected void connectionPollWorker() {
        Thing thing = this.getThing();

        synchronized (connectionLock) {
            ThingStatus currentStatus = thing.getStatus();
            JPigpio ljPigpio = this.jPigpio;

            if (ThingStatus.ONLINE.equals(currentStatus) && ljPigpio != null) {
                // We are ONLINE and jPigpio is instantiated, this is the normal path
                try {
                    logger.debug("gpio connection poll : CMD_TICK");
                    ljPigpio.getCurrentTick();
                } catch (PigpioException e) {
                    logger.debug("gpio connection poll : disconnect");
                    runDisconnectActions();
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                            e.getLocalizedMessage());

                    // We disconnected, reschedule ourselves to try a reconnect.
                    // First, try a quick reconnect if the user specified a long(ish) interval
                    int interval = this.config.heartBeatInterval;
                    if (interval > 1000) {
                        interval = 1000;
                    }

                    this.connectionJob = scheduler.schedule(() -> {
                        connectionPollWorker();
                    }, interval, TimeUnit.MILLISECONDS);

                    logger.warn("Pigpiod disconnected : {}", this.config.host);

                    return;
                }
            } else {
                // We are OFFLINE and jPigpio may or may not be instantiated
                try {
                    if (ljPigpio == null) {
                        // First initialization or re-initialization after dispose()
                        // jPigpio is not up and running yet.
                        logger.debug("gpio connection poll : connecting");
                        ljPigpio = new PigpioSocket(this.config.host, this.config.port);
                        this.jPigpio = ljPigpio;
                        setChannelJPigpio(ljPigpio);
                        updateStatus(ThingStatus.ONLINE);
                        runConnectActions();
                    } else {
                        // jPigpio is instantiated, but not connected.
                        // Use it's internal reconnect logic.
                        logger.debug("gpio connection poll : reconnecting");
                        ljPigpio.reconnect();
                        // jPigpio listeners are not re-established after reconnect.
                        // We need to reinject them into the channel handlers.
                        setChannelJPigpio(ljPigpio);
                        updateStatus(ThingStatus.ONLINE);
                        runReconnectActions();
                    }

                    logger.debug("Pigpiod connected : {}", this.config.host);
                } catch (PigpioException e) {
                    logger.debug("gpio connection poll : failed, {}", e.getErrorCode());
                    if (currentStatus.equals(ThingStatus.ONLINE) || currentStatus.equals(ThingStatus.INITIALIZING)) {
                        runDisconnectActions();
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                                e.getLocalizedMessage());
                    }
                }
            }

            if (this.config.heartBeatInterval > 0) {
                this.connectionJob = scheduler.schedule(() -> {
                    connectionPollWorker();
                }, this.config.heartBeatInterval, TimeUnit.MILLISECONDS);
            } else {
                // User disabled periodic connections, one shot?
                logger.debug("gpio connection poll : disabled");
                this.connectionJob = null;
            }
        }
    }

    protected void runConnectActions() throws PigpioException {
        if (this.config.inputConnectAction != null) {
            if (ACTION_REFRESH.equals(this.config.inputConnectAction)) {
                refreshInputChannels();
            }
        }

        if (this.config.outputConnectAction != null) {
            if (ACTION_ALL_ON.equals(this.config.outputConnectAction)) {
                setOutputChannels(OnOffType.ON);
            } else if (ACTION_ALL_OFF.equals(this.config.outputConnectAction)) {
                setOutputChannels(OnOffType.OFF);
            } else if (ACTION_REFRESH.equals(this.config.outputConnectAction)) {
                refreshOutputChannels();
            }
        }
    }

    protected void runReconnectActions() throws PigpioException {
        if (this.config.inputConnectAction != null) {
            if (ACTION_REFRESH.equals(this.config.inputConnectAction)) {
                refreshInputChannels();
            }
        }

        if (this.config.outputConnectAction != null) {
            if (ACTION_REFRESH.equals(this.config.outputConnectAction)) {
                refreshOutputChannels();
            }
        }
    }

    protected void runDisconnectActions() {
        if (this.config.inputDisconnectAction != null) {
            if (ACTION_SET_UNDEF.equals(this.config.inputDisconnectAction)) {
                undefInputChannels();
            }
        }

        if (this.config.outputDisconnectAction != null) {
            if (ACTION_SET_UNDEF.equals(this.config.outputDisconnectAction)) {
                undefOutputChannels();
            }
        }
    }

    protected void refreshInputChannels() throws PigpioException {
        logger.debug("gpio refresh input channels");
        for (ChannelUID channelUID : channelHandlers.keySet()) {
            ChannelHandler handler = channelHandlers.get(channelUID);
            if (handler instanceof PigpioDigitalInputHandler) {
                handler.handleCommand(RefreshType.REFRESH);
                postCommand(channelUID, RefreshType.REFRESH);
            }
        }
    }

    protected void refreshOutputChannels() throws PigpioException {
        logger.debug("gpio refresh output channels");
        for (ChannelUID channelUID : this.channelHandlers.keySet()) {
            ChannelHandler handler = this.channelHandlers.get(channelUID);
            if (handler instanceof PigpioDigitalOutputHandler) {
                handler.handleCommand(RefreshType.REFRESH);
                postCommand(channelUID, RefreshType.REFRESH);
            }
        }
    }

    protected void undefInputChannels() {
        logger.debug("gpio undef input channels");
        for (ChannelUID channelUID : this.channelHandlers.keySet()) {
            ChannelHandler handler = this.channelHandlers.get(channelUID);
            if (handler instanceof PigpioDigitalInputHandler) {
                updateState(channelUID, UnDefType.UNDEF);
            }
        }
    }

    protected void undefOutputChannels() {
        logger.debug("gpio undef output channels");
        for (ChannelUID channelUID : channelHandlers.keySet()) {
            ChannelHandler handler = channelHandlers.get(channelUID);
            if (handler instanceof PigpioDigitalOutputHandler) {
                updateState(channelUID, UnDefType.UNDEF);
            }
        }
    }

    protected void setOutputChannels(OnOffType command) throws PigpioException {
        logger.debug("gpio setting output channels: {}", command.toString());
        for (ChannelUID channelUID : this.channelHandlers.keySet()) {
            ChannelHandler handler = this.channelHandlers.get(channelUID);
            if (handler instanceof PigpioDigitalOutputHandler) {
                handler.handleCommand(command);
                postCommand(channelUID, command);
            }
        }
    }

    @Override
    public void dispose() {
        try {
            synchronized (this.connectionLock) {
                JPigpio ljPigpio = this.jPigpio;

                killConnectionPoll();

                if (ACTION_SET_UNDEF.equals(this.config.inputDisconnectAction)) {
                    undefInputChannels();
                }
                if (ACTION_SET_UNDEF.equals(this.config.outputDisconnectAction)) {
                    undefOutputChannels();
                }

                clearChannelHandlers();

                if (ljPigpio != null) {
                    try {
                        ljPigpio.gpioTerminate();
                        this.jPigpio = null;
                    } catch (PigpioException e) {
                        // Best effort at a socket shutdown
                    }
                }
            }
            logger.debug("gpio disposed");
        } catch (Exception e) {
            logger.debug("Dispose exception :", e);
        }

        super.dispose();
    }
}
