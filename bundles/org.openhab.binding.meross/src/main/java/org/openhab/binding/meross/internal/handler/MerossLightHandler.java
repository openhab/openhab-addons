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
import org.eclipse.jetty.client.HttpClient;
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
 * @author Mark Herwege - Implement refresh
 */
@NonNullByDefault
public class MerossLightHandler extends MerossDeviceHandler {

    private final Logger logger = LoggerFactory.getLogger(MerossLightHandler.class);

    public MerossLightHandler(Thing thing, HttpClient httpClient) {
        super(thing, httpClient);
    }

    @Override
    public void initialize() {
        // The following code is to update older light configurations:
        // This code moves from a "lightName" configuration parameter to a "name" configuration parameter
        // It also sets the uuid configuration representation configuration property from the deviceUUID property
        MerossLightConfiguration config = getConfigAs(MerossLightConfiguration.class);
        boolean configChanged = false;
        Configuration configuration = editConfiguration();
        String lightName = config.lightName;
        if (config.name.isEmpty() && (lightName != null)) {
            config.name = lightName;
            configuration.put(MerossBindingConstants.PROPERTY_DEVICE_NAME, config.lightName);
            configuration.put(MerossBindingConstants.PROPERTY_LIGHT_DEVICE_NAME, null);
            configChanged = true;
        }
        String deviceUUID = thing.getProperties().get("deviceUUID");
        if (config.uuid.isEmpty() && deviceUUID != null && !deviceUUID.isEmpty()) {
            config.uuid = deviceUUID;
            configuration.put(MerossBindingConstants.PROPERTY_DEVICE_UUID, deviceUUID);
            updateProperty("deviceUUID", null);
            configChanged = true;
        }
        if (configChanged) {
            updateConfiguration(configuration);
            this.config = config;
        }

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
            try {
                switch (command) {
                    case OnOffType onOffType:
                        if (OnOffType.ON.equals(onOffType)) {
                            manager.sendCommand(0, Namespace.CONTROL_TOGGLEX, OnOffType.ON.name());
                        } else if (OnOffType.OFF.equals(onOffType)) {
                            manager.sendCommand(0, Namespace.CONTROL_TOGGLEX, OnOffType.OFF.name());
                        }
                        break;
                    case RefreshType refreshType:
                        if (ipAddress == null) {
                            logger.debug("Not connected locally, refresh not supported");
                        } else {
                            manager.refresh(Namespace.CONTROL_TOGGLEX);
                        }
                        break;
                    default:
                        logger.debug("Unsupported command {} for channel {}", command, channelUID);
                        break;
                }
            } catch (MqttException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Cannot send command, " + e.getMessage());
            } catch (InterruptedException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection interrupted");
            }
        } else {
            logger.debug("Unsupported channelUID {}", channelUID);
        }
    }

    @Override
    public void updateState(Namespace namespace, int deviceChannel, State state) {
        if (namespace != Namespace.CONTROL_TOGGLEX) {
            return;
        }
        updateState(CHANNEL_LIGHT_POWER, state);
    }
}
