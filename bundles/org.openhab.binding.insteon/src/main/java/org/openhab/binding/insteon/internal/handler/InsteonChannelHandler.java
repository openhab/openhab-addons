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
package org.openhab.binding.insteon.internal.handler;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.insteon.internal.InsteonBinding;
import org.openhab.binding.insteon.internal.config.InsteonChannelConfiguration;
import org.openhab.binding.insteon.internal.device.DeviceFeature;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.InsteonDevice;
import org.openhab.binding.insteon.internal.device.database.LinkDB;
import org.openhab.binding.insteon.internal.device.database.ModemDB;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link InsteonChannelHandler} is the handler for all insteon channels.
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class InsteonChannelHandler {
    private final Logger logger = LoggerFactory.getLogger(InsteonChannelHandler.class);

    private InsteonBinding binding;
    private ChannelUID channelUID;
    private InsteonChannelConfiguration config;
    private DeviceFeature feature;
    private List<InsteonAddress> relatedDevices = List.of();

    public InsteonChannelHandler(InsteonBinding binding, ChannelUID channelUID, InsteonChannelConfiguration config,
            DeviceFeature feature) {
        this.binding = binding;
        this.channelUID = channelUID;
        this.config = config;
        this.feature = feature;

        feature.addChannelHandler(channelUID, this);
    }

    public void initialize() {
        // update channel state if defined
        State state = feature.getState();
        if (state != UnDefType.NULL) {
            updateState(state);
        }
        // update channel config
        updateChannelConfig();
    }

    public void dispose() {
        feature.removeChannelHandler(channelUID);
    }

    public void handleCommand(Command command) {
        feature.handleCommand(channelUID, config, command);
    }

    public void updateState(State state) {
        if (logger.isDebugEnabled()) {
            logger.debug("publishing state {} on {}", state, channelUID.getAsString());
        }
        binding.getHandler().updateState(channelUID, state);
    }

    public void triggerEvent(String event) {
        if (logger.isDebugEnabled()) {
            logger.debug("triggering event {} on {}", event, channelUID.getAsString());
        }
        binding.getHandler().triggerChannel(channelUID, event);
    }

    /**
     * Updates channel config for controller feature
     */
    public void updateChannelConfig() {
        if (feature.isControllerFeature()) {
            // update related devices
            updateRelatedDevices();
            // update broadcast group if not event feature
            if (!feature.isEventFeature()) {
                updateBroadcastGroup();
            }
        }
    }

    /**
     * Updates broadcast group based on device/modem link database
     */
    private void updateBroadcastGroup() {
        InsteonDevice device = feature.getDevice();
        LinkDB linkDB = device.getLinkDB();
        InsteonAddress modemAddress = binding.getModemAddress();
        ModemDB modemDB = binding.getModemDB();
        int broadcastGroup = -1;

        if (device.isModem()) {
            if (modemDB.isComplete()) {
                int group = config.getGroup();
                if (modemDB.hasBroadcastGroup(group)) {
                    broadcastGroup = group;
                } else {
                    logger.warn("broadcast group {} not found in modem db for {}", group, channelUID.getAsString());
                }
            }
        } else {
            if (linkDB.isComplete() && !relatedDevices.isEmpty()) {
                // iterate over device link db broadcast groups based on "group" feature parameter as component id
                int componentId = feature.getGroup();
                for (int group : linkDB.getBroadcastGroups(componentId, modemAddress)) {
                    // compare related devices channel config with the modem db for a given broadcast group
                    List<InsteonAddress> devices = modemDB.getRelatedDevices(group);
                    devices.remove(device.getAddress());
                    devices.removeAll(relatedDevices);
                    // set broadcast group if two lists identical
                    if (devices.isEmpty()) {
                        broadcastGroup = group;
                        break;
                    }
                }
            }
        }

        if (broadcastGroup != -1) {
            if (logger.isDebugEnabled()) {
                logger.debug("setting broadcast group to {} for {}", broadcastGroup, channelUID.getAsString());
            }
        }
        config.setGroup(broadcastGroup);
    }

    /**
     * Updates related devices based on device/modem link database
     */
    private void updateRelatedDevices() {
        InsteonDevice device = feature.getDevice();
        LinkDB linkDB = device.getLinkDB();
        ModemDB modemDB = binding.getModemDB();
        List<InsteonAddress> relatedDevices = List.of();

        if (device.isModem()) {
            // set devices using group channel config parameter to search in modem database if complete
            if (modemDB.isComplete()) {
                int group = config.getGroup();
                relatedDevices = modemDB.getRelatedDevices(group);
            }
        } else {
            // set devices using group feature parameter to search in device link database if complete
            if (linkDB.isComplete()) {
                int group = feature.getGroup();
                relatedDevices = linkDB.getRelatedDevices(group, modemDB);
            }
        }

        if (!relatedDevices.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("setting related devices to {} for {}", relatedDevices, channelUID.getAsString());
            }
        }
        this.relatedDevices = relatedDevices;
    }

    /**
     * Adjusts all device features that are related to this channel
     *
     * @param cmd the command to adjust to
     */
    public void adjustRelatedDevices(Command cmd) {
        InsteonAddress controllerAddress = feature.getDevice().getAddress();
        int controllerGroup = feature.getGroup();
        for (InsteonAddress address : relatedDevices) {
            InsteonDevice device = binding.getDevice(address);
            if (device != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("adjusting related device {} for controller {} group {}", address, controllerAddress,
                            controllerGroup);
                }
                device.adjustRelatedFeatures(controllerAddress, controllerGroup, config, cmd);
            }
        }
    }

    /**
     * Polls all devices that are related to this channel
     *
     * @param delay scheduling delay (in milliseconds)
     */
    public void pollRelatedDevices(long delay) {
        InsteonAddress controllerAddress = feature.getDevice().getAddress();
        int controllerGroup = feature.getGroup();
        for (InsteonAddress address : relatedDevices) {
            InsteonDevice device = binding.getDevice(address);
            if (device != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("polling related device {} for controller {} group {} in {} msec", address,
                            controllerAddress, controllerGroup, delay);
                }
                device.pollRelatedFeatures(controllerAddress, controllerGroup, delay);
            }
        }
    }

    /**
     * Factory method for creating a InsteonChannelHandler from a channel uid, feature and parameters
     *
     * @param binding the binding reference
     * @param channel the channel reference
     * @param feature the channel feature
     * @return the newly created InsteonChannelHandler
     */
    public static InsteonChannelHandler makeHandler(InsteonBinding binding, Channel channel, DeviceFeature feature) {
        ChannelUID channelUID = channel.getUID();
        InsteonChannelConfiguration config = channel.getConfiguration().as(InsteonChannelConfiguration.class);
        InsteonChannelHandler handler = new InsteonChannelHandler(binding, channelUID, config, feature);
        handler.initialize();
        return handler;
    }
}
