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

import static org.openhab.binding.meross.internal.MerossBindingConstants.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.meross.internal.api.MerossEnum;
import org.openhab.binding.meross.internal.api.MerossEnum.Namespace;
import org.openhab.binding.meross.internal.api.MerossManager;
import org.openhab.binding.meross.internal.api.MerossMqttConnector;
import org.openhab.binding.meross.internal.config.MerossDeviceConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.mqtt.MqttException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MerossDeviceHandler} is the main class for Meross device handlers
 *
 * @author Giovanni Fabiani - Initial contribution
 * @author Mark Herwege - Converted light handler to more generic device handler
 */
@NonNullByDefault
public class MerossDeviceHandler extends BaseThingHandler implements MerossDeviceHandlerCallback {

    protected @NonNullByDefault({}) MerossDeviceConfiguration config;
    protected @Nullable MerossBridgeHandler merossBridgeHandler;
    protected @Nullable MerossManager manager;

    private HttpClient httpClient;

    protected @Nullable String ipAddress;

    private final Logger logger = LoggerFactory.getLogger(MerossDeviceHandler.class);

    private long lastRefresh;
    private static final int REFRESH_INTERVAL_MILLIS = 5000;

    public MerossDeviceHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        this.config = getConfigAs(MerossDeviceConfiguration.class);
        initializeCommunication();
    }

    private void initializeCommunication() {
        Bridge bridge = getBridge();
        if (bridge == null || !(bridge.getHandler() instanceof MerossBridgeHandler merossBridgeHandler)) {
            return;
        }
        this.merossBridgeHandler = merossBridgeHandler;
        MerossMqttConnector mqttConnector = merossBridgeHandler.getMerossMqttConnector();
        if (mqttConnector == null) {
            return;
        }
        ThingStatus bridgeStatus = bridge.getStatus();
        if (bridgeStatus.equals(ThingStatus.OFFLINE)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        Thing thing = getThing();
        String label = thing.getLabel();
        if (config.name.isEmpty()) {
            if (label != null) {
                config.name = label;
            }
        }
        String ipAddress = config.ipAddress;
        if (ipAddress != null) {
            try {
                InetAddress.getByName(ipAddress);
                this.ipAddress = ipAddress;
            } catch (UnknownHostException e) {
                logger.debug("Invalid IP address configuration {}", ipAddress);
            }
        }
        String deviceUUID = config.uuid;
        if (deviceUUID.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No device found with name " + config.name + ", UUID not set");
            return;
        }
        scheduler.submit(() -> {
            MerossManager manager = this.manager;
            manager = manager != null ? manager : new MerossManager(httpClient, mqttConnector, deviceUUID, this);
            this.manager = manager;
            try {
                manager.initialize();
                initializeDevice();
            } catch (MqttException | InterruptedException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Communication error for device with name " + config.name);
            }
        });
    }

    /**
     * This method is called after the communication with the device is set up. Subclasses can implement this method to
     * implement device specific setup.
     */
    protected void initializeDevice() {
    }

    @Override
    public void dispose() {
        MerossManager manager = this.manager;
        if (manager != null) {
            manager.dispose();
            this.manager = null;
        }
        super.dispose();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() != null) {
            ThingStatus bridgeStatus = bridge.getStatus();
            if (ThingStatus.OFFLINE.equals(bridgeStatus)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            } else if (ThingStatus.ONLINE.equals(bridgeStatus) && !(ThingStatus.ONLINE.equals(thing.getStatus()))) {
                initializeCommunication();
            }
        }
    }

    @Override
    public void setThingStatusFromMerossStatus(int status) {
        if (status == MerossEnum.OnlineStatus.UNKNOWN.value() || status == MerossEnum.OnlineStatus.NOT_ONLINE.value()
                || status == MerossEnum.OnlineStatus.UPGRADING.value()) {
            updateStatus(ThingStatus.UNKNOWN);
        } else if (status == MerossEnum.OnlineStatus.OFFLINE.value()) {
            updateStatus(ThingStatus.OFFLINE);
        } else if (status == MerossEnum.OnlineStatus.ONLINE.value()) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void setIpAddress(String ipAddress) {
        if (ipAddress.equals(this.ipAddress)) {
            return;
        }
        this.ipAddress = ipAddress;

        // Check if a valid address
        try {
            InetAddress.getByName(ipAddress);
        } catch (UnknownHostException e) {
            logger.debug("Trying to set invalid IP address {}", ipAddress);
            this.ipAddress = null;
            return;
        }
        // Keep the ip address for the next start of the binding
        Configuration configuration = editConfiguration();
        configuration.put(PROPERTY_IP_ADDRESS, this.ipAddress);
        updateConfiguration(configuration);
    }

    @Override
    public @Nullable String getIpAddress() {
        return ipAddress;
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

        // Extract the channel group, removing any trailing digits
        String channelGroup = channelUID.getId().replaceFirst("(\\d+)$", "");
        Namespace namespace = CHANNEL_NAMESPACE_MAP.get(channelGroup);
        if (namespace == null) {
            logger.debug("Unsupported channelUID {}", channelUID);
            return;
        }
        int channel = getChannel(channelUID, channelGroup);

        // Local communication is waiting for http call responses, so can take some time. Therefore use the scheduler to
        // handle any command.
        scheduler.submit(() -> {
            try {
                if (command instanceof RefreshType) {
                    if (ipAddress == null) {
                        logger.debug("Not connected locally, refresh not supported");
                    } else {
                        // Refresh for most devices is for all data at once (System.All) and not for the specific
                        // namespace.
                        // Therefore don't do a refresh if we just had one triggered (probably from another channel) to
                        // avoid excessive http calls.
                        long now = System.currentTimeMillis();
                        if (now > lastRefresh + REFRESH_INTERVAL_MILLIS) {
                            manager.refresh(Namespace.SYSTEM_ALL);
                            lastRefresh = now;
                        }
                    }
                    return;
                }

                manager.sendCommand(channel, namespace, command);
            } catch (MqttException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Cannot send command, " + e.getMessage());
            } catch (InterruptedException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection interrupted");
            }
        });
    }

    private int getChannel(ChannelUID channelUID, String channelGroupUID) {
        int channel = 0;
        try {
            // If the device has multiple channels, the channel number will be appended to the channel name starting
            // with _
            channel = Integer.parseInt(channelUID.getId().substring(channelGroupUID.length() + 1));
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            // Ignore and default to channel 0, this is because only a single channel is available
        }
        return channel;
    }

    @Override
    public void updateState(Namespace namespace, int deviceChannel, State state) {
        String channelGroup = NAMESPACE_CHANNEL_MAP.get(namespace);
        if (channelGroup == null) {
            return;
        }
        // If the device has multiple channels, the channel number will be appended to the channel name starting
        // with _
        String channelId = channelGroup + "_" + deviceChannel;
        if (thing.getChannel(channelId) == null && deviceChannel == 0) {
            channelId = channelGroup;
        }
        if (thing.getChannel(channelId) == null) {
            logger.debug("Channel with id {} not supported for thing {}", channelId, thing.getUID().getId());
            return;
        }
        updateState(channelId, state);
    }
}
