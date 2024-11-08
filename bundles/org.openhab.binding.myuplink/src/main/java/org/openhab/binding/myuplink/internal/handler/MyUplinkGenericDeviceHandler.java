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
package org.openhab.binding.myuplink.internal.handler;

import static org.openhab.binding.myuplink.internal.MyUplinkBindingConstants.*;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myuplink.internal.AtomicReferenceTrait;
import org.openhab.binding.myuplink.internal.MyUplinkBindingConstants;
import org.openhab.binding.myuplink.internal.Utils;
import org.openhab.binding.myuplink.internal.command.MyUplinkCommand;
import org.openhab.binding.myuplink.internal.command.account.GetSystems;
import org.openhab.binding.myuplink.internal.command.device.GetPoints;
import org.openhab.binding.myuplink.internal.command.device.GetSmartHomeMode;
import org.openhab.binding.myuplink.internal.command.device.SetPoints;
import org.openhab.binding.myuplink.internal.command.device.SetPointsAdvanced;
import org.openhab.binding.myuplink.internal.command.device.SetSmartHomeMode;
import org.openhab.binding.myuplink.internal.config.MyUplinkConfiguration;
import org.openhab.binding.myuplink.internal.connector.CommunicationStatus;
import org.openhab.binding.myuplink.internal.provider.ChannelFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link MyUplinkGenericDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class MyUplinkGenericDeviceHandler extends BaseThingHandler
        implements MyUplinkThingHandler, DynamicChannelProvider, AtomicReferenceTrait {
    private final Logger logger = LoggerFactory.getLogger(MyUplinkGenericDeviceHandler.class);

    /**
     * Schedule for polling live data
     */
    private final AtomicReference<@Nullable Future<?>> dataPollingJobReference;

    private final ChannelFactory channelFactory;

    private final Configuration config;

    public MyUplinkGenericDeviceHandler(Thing thing, ChannelFactory channelFactory) {
        super(thing);
        this.dataPollingJobReference = new AtomicReference<>(null);
        this.channelFactory = channelFactory;
        this.config = getConfig();
    }

    @Override
    public void initialize() {
        logger.debug("About to initialize myUplink Generic Device with id: {}", getDeviceId());

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, STATUS_WAITING_FOR_BRIDGE);
        startPolling();
    }

    public String getDeviceId() {
        return config.get(THING_CONFIG_ID).toString();
    }

    private void updatePropertiesAndOnlineStatus(CommunicationStatus status, JsonObject systemsJson) {
        JsonObject deviceFound = extractDevice(systemsJson);

        if (deviceFound != null) {
            Map<String, String> properties = editProperties();
            String currentFwVersion = Utils.getAsString(deviceFound, JSON_KEY_CURRENT_FW_VERSION, GENERIC_NO_VAL);
            properties.put(THING_CONFIG_CURRENT_FW_VERSION, currentFwVersion);
            updateProperties(properties);

            String connectionStatus = Utils.getAsString(deviceFound, JSON_KEY_CONNECTION_STATE, GENERIC_NO_VAL);
            if (connectionStatus.equals(JSON_VAL_CONNECTION_CONNECTED)) {
                super.updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            } else {
                super.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, STATUS_NO_CONNECTION);
            }
        } else {
            super.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, STATUS_DEVICE_NOT_FOUND);
        }
    }

    private final @Nullable JsonObject extractDevice(JsonObject systemsJson) {
        JsonArray systems = systemsJson.getAsJsonArray(JSON_KEY_SYSTEMS);
        if (systems != null && !systems.isEmpty()) {
            for (JsonElement systemJson : systems) {
                JsonObject system = systemJson.getAsJsonObject();
                JsonArray devices = system.getAsJsonArray(JSON_KEY_DEVICES);
                if (devices != null && !devices.isEmpty()) {
                    for (JsonElement deviceJson : devices) {
                        JsonObject device = deviceJson.getAsJsonObject();
                        String deviceId = Utils.getAsString(device, JSON_KEY_GENERIC_ID, GENERIC_NO_VAL);
                        if (deviceId.equals(getDeviceId())) {
                            return device;
                        }

                    }
                }
            }
        }
        return null;
    }

    @Override
    public MyUplinkCommand buildMyUplinkCommand(Command command, Channel channel) {
        var deviceId = config.get(MyUplinkBindingConstants.THING_CONFIG_ID).toString();
        String systemId = "";
        if (config.containsKey(THING_CONFIG_SYSTEM_ID)) {
            systemId = config.get(THING_CONFIG_SYSTEM_ID).toString();
        }

        var channelTypeId = Utils.getChannelTypeId(channel);
        return switch (channelTypeId) {
            case CHANNEL_TYPE_RW_COMMAND ->
                new SetPointsAdvanced(this, channel, command, deviceId, this::updateOnlineStatus);
            case CHANNEL_TYPE_RW_MODE -> {
                if (systemId.isBlank()) {
                    throw new UnsupportedOperationException("systemId not configured");
                }
                yield new SetSmartHomeMode(this, channel, command, systemId, this::updateOnlineStatus);
            }
            default -> new SetPoints(this, channel, command, deviceId, this::updateOnlineStatus);
        };
    }

    /**
     * Start the polling.
     */
    private void startPolling() {
        updateJobReference(dataPollingJobReference, scheduler.scheduleWithFixedDelay(this::pollingRun,
                POLLING_INITIAL_DELAY, getBridgeConfiguration().getDataPollingInterval(), TimeUnit.SECONDS));
    }

    /**
     * Poll the myUplink Cloud API one time.
     */
    void pollingRun() {
        String deviceId = config.get(THING_CONFIG_ID).toString();
        String systemId = "";
        if (config.containsKey(THING_CONFIG_SYSTEM_ID)) {
            systemId = config.get(THING_CONFIG_SYSTEM_ID).toString();
        }
        logger.debug("polling device data for {}", deviceId);

        // proceed if device is online
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            enqueueCommand(new GetPoints(this, deviceId, this::updateOnlineStatus));
            if (!systemId.isBlank()) {
                enqueueCommand(new GetSmartHomeMode(this, systemId, this::updateOnlineStatus));
            }
        }
        enqueueCommand(new GetSystems(this, this::updatePropertiesAndOnlineStatus));
    }

    /**
     * result processor to handle online status updates
     *
     * @param status of command execution
     * @param jsonObject json respone result
     */
    protected final void updateOnlineStatus(CommunicationStatus status, JsonObject jsonObject) {
        String msg = Utils.getAsString(jsonObject, JSON_KEY_ERROR);
        if (msg == null || msg.isBlank()) {
            msg = status.getMessage();
        }

        switch (status.getHttpCode()) {
            case OK:
            case ACCEPTED:
                super.updateStatus(ThingStatus.ONLINE);
                break;
            case BAD_REQUEST:
            case UNAUTHORIZED:
            case FORBIDDEN:
                super.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
                break;
            default:
                super.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msg);
        }
    }

    /**
     * Disposes the thing.
     */
    @Override
    public void dispose() {
        logger.debug("Handler disposing.");
        cancelJobReference(dataPollingJobReference);
    }

    /**
     * adds a channel.
     */
    @Override
    public void registerChannel(Channel channel) {
        ThingBuilder thingBuilder = editThing();
        thingBuilder.withChannel(channel);
        updateThing(thingBuilder.build());
    }

    /**
     * will update all channels provided in the map
     */
    @Override
    public void updateChannelStatus(Map<Channel, State> values) {
        logger.debug("Handling heatpump channel update.");

        for (Channel channel : values.keySet()) {
            if (getThing().getChannels().contains(channel)) {
                if (isLinked(channel.getUID())) {
                    State value = values.get(channel);
                    if (value != null) {
                        logger.debug("Channel is to be updated: {}: {}", channel.getUID().getAsString(), value);
                        updateState(channel.getUID(), value);
                    } else {
                        logger.debug("Value is null or not provided by myUplink Cloud (channel: {})",
                                channel.getUID().getAsString());
                        updateState(channel.getUID(), UnDefType.UNDEF);
                    }
                }
            } else {
                logger.debug("Could not identify channel: {} for model {}", channel.getUID().getAsString(),
                        getThing().getThingTypeUID().getAsString());
            }
        }
    }

    @Override
    public void enqueueCommand(MyUplinkCommand command) {
        MyUplinkBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            bridgeHandler.enqueueCommand(command);
        } else {
            // this should not happen
            logger.warn("no bridge handler found");
        }
    }

    private @Nullable MyUplinkBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        return bridge != null && bridge.getHandler() instanceof MyUplinkBridgeHandler handler ? handler : null;
    }

    @Override
    public MyUplinkConfiguration getBridgeConfiguration() {
        MyUplinkBridgeHandler bridgeHandler = getBridgeHandler();
        return bridgeHandler == null ? new MyUplinkConfiguration() : bridgeHandler.getBridgeConfiguration();
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public ThingUID getThingUid() {
        return getThing().getUID();
    }

    @Override
    public ChannelFactory getChannelFactory() {
        return channelFactory;
    }
}
