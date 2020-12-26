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
/**
 *
 */
package org.openhab.binding.gpio.internal.handler;

import static org.openhab.binding.gpio.internal.GPIOBindingConstants.CHANNEL_TYPE_DIGITAL_INPUT;
import static org.openhab.binding.gpio.internal.GPIOBindingConstants.CHANNEL_TYPE_DIGITAL_OUTPUT;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gpio.internal.configuration.GPIOInputConfiguration;
import org.openhab.binding.gpio.internal.configuration.GPIOOutputConfiguration;
import org.openhab.binding.gpio.internal.configuration.PigpioBridgeConfiguration;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.xeli.jpigpio.JPigpio;
import eu.xeli.jpigpio.PigpioException;
import eu.xeli.jpigpio.PigpioSocket;

/**
 * Remote JPigpio Bridge Hanlder
 *
 * This bridge is used to control remote pigpio instances.
 *
 * @author Nils Bauer - Initial contribution
 */
@NonNullByDefault
public class PigpioRemoteThingHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(PigpioRemoteThingHandler.class);
    /** The JPigpio instance. */
    private @Nullable JPigpio jPigpio;
    private final Map<ChannelUID, ChannelHandler> channelHandlers = new HashMap<>();

    /**
     * Instantiates a new pigpio remote bridge handler.
     *
     * @param thing the bridge
     */
    public PigpioRemoteThingHandler(Thing thing) {
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
        PigpioBridgeConfiguration config = getConfigAs(PigpioBridgeConfiguration.class);
        String ipAddress = config.ipAddress;
        Integer port = config.port;

        JPigpio jPigpio;

        if (ipAddress == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to PiGPIO Service on remote raspberry. IP address not set.");
            return;
        }
        try {
            jPigpio = new PigpioSocket(ipAddress, port);
            updateStatus(ThingStatus.ONLINE);
        } catch (NumberFormatException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Port not numeric");
            return;
        } catch (PigpioException e) {
            if (e.getErrorCode() == PigpioException.PI_BAD_SOCKET_PORT) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Port out of range");
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        e.getLocalizedMessage());
            }
            return;

        }
        this.jPigpio = jPigpio;
        thing.getChannels().forEach(channel -> {
            ChannelUID channelUID = channel.getUID();
            ChannelTypeUID type = channel.getChannelTypeUID();
            try {
                if (type.equals(CHANNEL_TYPE_DIGITAL_INPUT)) {
                    GPIOInputConfiguration configuration = channel.getConfiguration().as(GPIOInputConfiguration.class);
                    channelHandlers.put(channelUID, new GPIODigitalInputHandler(configuration, jPigpio, scheduler,
                            state -> updateState(channelUID.getId(), state)));
                } else if (type.equals(CHANNEL_TYPE_DIGITAL_OUTPUT)) {
                    GPIOOutputConfiguration configuration = channel.getConfiguration()
                            .as(GPIOOutputConfiguration.class);
                    channelHandlers.put(channelUID, new GPIODigitalOutputHandler(configuration, jPigpio,
                            state -> updateState(channelUID.getId(), state)));
                }
            } catch (PigpioException e) {
                logger.warn("Failed to initialize {}: {}", channelUID, e.getMessage());
            }
        });
    }
}
