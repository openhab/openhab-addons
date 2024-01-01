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
package org.openhab.binding.mycroft.internal;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mycroft.internal.api.MessageType;
import org.openhab.binding.mycroft.internal.api.MycroftConnection;
import org.openhab.binding.mycroft.internal.api.MycroftConnectionListener;
import org.openhab.binding.mycroft.internal.api.MycroftMessageListener;
import org.openhab.binding.mycroft.internal.api.dto.BaseMessage;
import org.openhab.binding.mycroft.internal.api.dto.MessageVolumeGet;
import org.openhab.binding.mycroft.internal.channels.AudioPlayerChannel;
import org.openhab.binding.mycroft.internal.channels.ChannelCommandHandler;
import org.openhab.binding.mycroft.internal.channels.FullMessageChannel;
import org.openhab.binding.mycroft.internal.channels.ListenChannel;
import org.openhab.binding.mycroft.internal.channels.MuteChannel;
import org.openhab.binding.mycroft.internal.channels.MycroftChannel;
import org.openhab.binding.mycroft.internal.channels.SpeakChannel;
import org.openhab.binding.mycroft.internal.channels.UtteranceChannel;
import org.openhab.binding.mycroft.internal.channels.VolumeChannel;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.util.ThingWebClientUtil;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MycroftHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public class MycroftHandler extends BaseThingHandler implements MycroftConnectionListener {

    private final Logger logger = LoggerFactory.getLogger(MycroftHandler.class);

    private final WebSocketFactory webSocketFactory;
    private @NonNullByDefault({}) MycroftConnection connection;
    private @Nullable ScheduledFuture<?> scheduledFuture;
    private MycroftConfiguration config = new MycroftConfiguration();
    private boolean thingDisposing = false;
    protected Map<ChannelUID, MycroftChannel<?>> mycroftChannels = new HashMap<>();

    /** The reconnect frequency in case of error */
    private static final int POLL_FREQUENCY_SEC = 30;
    private int sometimesSendVolumeRequest = 0;

    public MycroftHandler(Thing thing, WebSocketFactory webSocketFactory) {
        super(thing);
        this.webSocketFactory = webSocketFactory;
    }

    /**
     * Stops the API request or websocket reconnect timer
     */
    private void stopTimer() {
        ScheduledFuture<?> future = scheduledFuture;
        if (future != null) {
            future.cancel(false);
            scheduledFuture = null;
        }
    }

    /**
     * Starts the websocket connection.
     * It sometimes also sends a get volume request to check the connection and refresh the volume.
     */
    private void checkOrstartWebsocket() {
        if (thingDisposing) {
            return;
        }
        if (connection.isConnected()) {
            // sometimes test the connection by sending a real message
            // AND refreshing volume in the same step
            if (sometimesSendVolumeRequest >= 3) { // arbitrary one on three times
                sometimesSendVolumeRequest = 0;
                sendMessage(new MessageVolumeGet());
            } else {
                sometimesSendVolumeRequest++;
            }
        } else {
            connection.start(config.host, config.port);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        ChannelCommandHandler channelCommand = mycroftChannels.get(channelUID);
        if (channelCommand == null) {
            logger.error("Command {} for channel {} cannot be handled", command.toString(), channelUID.toString());
        } else {
            channelCommand.handleCommand(command);
        }
    }

    @Override
    public void initialize() {
        thingDisposing = false;

        updateStatus(ThingStatus.UNKNOWN);

        logger.debug("Start initializing Mycroft {}", thing.getUID());

        String websocketID = ThingWebClientUtil.buildWebClientConsumerName(thing.getUID(), null);
        this.connection = new MycroftConnection(this, webSocketFactory.createWebSocketClient(websocketID));

        config = getConfigAs(MycroftConfiguration.class);
        if (config.host.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "No host defined");
            return;
        } else if (config.port < 0 || config.port > 0xFFFF) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Port should be between 0 and 65536");
            return;
        }
        scheduledFuture = scheduler.scheduleWithFixedDelay(this::checkOrstartWebsocket, 0, POLL_FREQUENCY_SEC,
                TimeUnit.SECONDS);

        registerChannel(new ListenChannel(this));
        registerChannel(new VolumeChannel(this));
        registerChannel(new MuteChannel(this, config.volume_restoration_level));
        registerChannel(new SpeakChannel(this));
        registerChannel(new AudioPlayerChannel(this));
        registerChannel(new UtteranceChannel(this));

        final Channel fullMessageChannel = getThing().getChannel(MycroftBindingConstants.FULL_MESSAGE_CHANNEL);
        @SuppressWarnings("null") // cannot be null
        String messageTypesProperty = (String) fullMessageChannel.getConfiguration()
                .get(MycroftBindingConstants.FULL_MESSAGE_CHANNEL_MESSAGE_TYPE_PROPERTY);

        registerChannel(new FullMessageChannel(this, messageTypesProperty));

        checkLinkedChannelsAndRegisterMessageListeners();
    }

    private void checkLinkedChannelsAndRegisterMessageListeners() {
        for (Entry<ChannelUID, MycroftChannel<?>> channelEntry : mycroftChannels.entrySet()) {
            ChannelUID uid = channelEntry.getKey();
            MycroftChannel<?> channel = channelEntry.getValue();
            if (isLinked(uid)) {
                channel.registerListeners();
            } else {
                channel.unregisterListeners();
            }
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        checkLinkedChannelsAndRegisterMessageListeners();
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        checkLinkedChannelsAndRegisterMessageListeners();
    }

    private void registerChannel(MycroftChannel<?> channel) {
        mycroftChannels.put(channel.getChannelUID(), channel);
    }

    public void registerMessageListener(MessageType messageType, MycroftMessageListener<?> listener) {
        this.connection.registerListener(messageType, listener);
    }

    public void unregisterMessageListener(MessageType messageType, MycroftMessageListener<?> listener) {
        this.connection.unregisterListener(messageType, listener);
    }

    @Override
    public void connectionEstablished() {
        logger.debug("Mycroft thing {} is online", thing.getUID());
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void connectionLost(String reason) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, reason);
    }

    @Override
    public void dispose() {
        thingDisposing = true;
        stopTimer();
        connection.close();
    }

    public <T extends State> void updateMyChannel(MycroftChannel<T> mycroftChannel, T state) {
        updateState(mycroftChannel.getChannelUID(), state);
    }

    public boolean sendMessage(BaseMessage message) {
        try {
            connection.sendMessage(message);
            return true;
        } catch (IOException e) {
            logger.debug("Cannot send message of type {}, for reason {}", message.getClass().getName(), e.getMessage());
            return false;
        }
    }

    public boolean sendMessage(String message) {
        try {
            connection.sendMessage(message);
            return true;
        } catch (IOException e) {
            logger.debug("Cannot send message of type {}, for reason {}", message.getClass().getName(), e.getMessage());
            return false;
        }
    }
}
