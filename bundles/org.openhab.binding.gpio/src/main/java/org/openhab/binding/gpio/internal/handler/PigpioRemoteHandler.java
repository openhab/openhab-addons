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

import static org.openhab.binding.gpio.internal.GPIOBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.gpio.internal.InvalidPullUpDownException;
import org.openhab.binding.gpio.internal.NoGpioIdException;
import org.openhab.binding.gpio.internal.configuration.GPIOInputConfiguration;
import org.openhab.binding.gpio.internal.configuration.GPIOOutputConfiguration;
import org.openhab.binding.gpio.internal.configuration.PigpioConfiguration;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
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
        ChannelHandler channelHandler = channelHandlers.get(channelUID);
        if (channelHandler != null) {
            channelHandler.handleCommand(command);
        }
    }

    @Override
    public void initialize() {
        PigpioConfiguration config = getConfigAs(PigpioConfiguration.class);
        String host = config.host;
        int port = config.port;
        JPigpio jPigpio;
        if (host == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to PiGPIO Service on remote raspberry. IP address not set.");
            return;
        }
        try {
            jPigpio = new PigpioSocket(host, port);
            updateStatus(ThingStatus.ONLINE);
        } catch (PigpioException e) {
            if (e.getErrorCode() == PigpioException.PI_BAD_SOCKET_PORT) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Port out of range");
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        e.getLocalizedMessage());
            }
            return;
        }
        thing.getChannels().forEach(channel -> {
            ChannelUID channelUID = channel.getUID();
            ChannelTypeUID type = channel.getChannelTypeUID();
            try {
                if (CHANNEL_TYPE_DIGITAL_INPUT.equals(type)) {
                    GPIOInputConfiguration configuration = channel.getConfiguration().as(GPIOInputConfiguration.class);
                    channelHandlers.put(channelUID, new PigpioDigitalInputHandler(configuration, jPigpio, scheduler,
                            state -> updateState(channelUID.getId(), state)));
                } else if (CHANNEL_TYPE_DIGITAL_OUTPUT.equals(type)) {
                    GPIOOutputConfiguration configuration = channel.getConfiguration()
                            .as(GPIOOutputConfiguration.class);
                    channelHandlers.put(channelUID, new PigpioDigitalOutputHandler(configuration, jPigpio,
                            state -> updateState(channelUID.getId(), state)));
                }
            } catch (PigpioException e) {
                logger.warn("Failed to initialize {}: {}", channelUID, e.getMessage());
            } catch (InvalidPullUpDownException e) {
                logger.warn("Failed to initialize {}: Invalid Pull Up/Down resistor configuration", channelUID);
            } catch (NoGpioIdException e) {
                logger.warn("Failed to initialize {}: GpioId is not set", channelUID);
            }
        });
    }
}
