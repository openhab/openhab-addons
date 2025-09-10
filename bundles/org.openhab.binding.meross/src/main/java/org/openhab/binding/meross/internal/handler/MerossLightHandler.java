/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.meross.internal.handler;

import static org.openhab.binding.meross.internal.MerossBindingConstants.CHANNEL_LIGHT_POWER;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.meross.internal.MerossBindingConstants;
import org.openhab.binding.meross.internal.api.MerossEnum.Namespace;
import org.openhab.binding.meross.internal.api.MerossManager;
import org.openhab.binding.meross.internal.config.MerossLightConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.mqtt.MqttException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MerossLightHandler} class is responsible for handling communication with plugs and bulbs
 *
 * @author Giovanni Fabiani - Initial contribution
 * @author Mark Herwege - Extract common methods to abstract base class
 */
@NonNullByDefault
public class MerossLightHandler extends MerossDeviceHandler {

    private final Logger logger = LoggerFactory.getLogger(MerossLightHandler.class);

    public MerossLightHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        // This code moves from a "lightName" configuration parameter to a "name" configuration parameter
        MerossLightConfiguration config = getConfigAs(MerossLightConfiguration.class);
        String lightName = config.lightName;
        if (config.name.isEmpty() && (lightName != null)) {
            config.name = lightName;
            Configuration configuration = editConfiguration();
            configuration.put(MerossBindingConstants.PROPERTY_DEVICE_NAME, config.lightName);
            configuration.put(MerossBindingConstants.PROPERTY_LIGHT_DEVICE_NAME, null);
            updateConfiguration(configuration);
        }
        this.config = config;

        initializeDevice();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (thing.getStatus() == ThingStatus.OFFLINE) {
            return;
        }
        MerossManager manager = this.manager;
        if (manager == null) {
            logger.debug("Handling command, manager not available");
            return;
        }

        if (channelUID.getId().equals(CHANNEL_LIGHT_POWER)) {
            if (command instanceof OnOffType) {
                try {
                    if (OnOffType.ON.equals(command)) {
                        manager.sendCommand(0, Namespace.CONTROL_TOGGLEX, OnOffType.ON.name());
                    } else if (OnOffType.OFF.equals(command)) {
                        manager.sendCommand(0, Namespace.CONTROL_TOGGLEX, OnOffType.OFF.name());
                    }
                } catch (MqttException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Cannot send command" + e.getMessage());
                }
            } else if (command instanceof RefreshType) {
                logger.debug("Refresh command not supported");
            } else {
                logger.debug("Unsupported command {} for channel {}", command, channelUID);
            }
        } else {
            logger.debug("Unsupported channelUID {}", channelUID);
        }
    }

    @Override
    public void updateState(int deviceChannel, State state) {
        updateState(CHANNEL_LIGHT_POWER, state);
    }
}
