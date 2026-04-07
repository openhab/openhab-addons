/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.ntfy.internal;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.ntfy.internal.action.NtfyActions;
import org.openhab.binding.ntfy.internal.models.BaseEvent;
import org.openhab.binding.ntfy.internal.models.MessageEvent;
import org.openhab.binding.ntfy.internal.network.NtfyMessage;
import org.openhab.binding.ntfy.internal.network.NtfySender;
import org.openhab.binding.ntfy.internal.network.NtfyWebSocket;
import org.openhab.binding.ntfy.internal.network.WebSocketConnectionListener;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link NtfyTopicHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class NtfyTopicHandler extends BaseThingHandler implements WebSocketConnectionListener {

    private NtfyWebSocket ntfyWebSocket;
    private NtfySender ntfySender;
    private boolean isInitializing;
    private @Nullable MessageEvent lastMessageEvent;

    /**
     * Creates a new {@link NtfyTopicHandler} for the provided topic thing.
     *
     * @param thing the topic thing this handler belongs to
     * @param httpClient the HTTP client used by the sender to perform REST calls
     */
    public NtfyTopicHandler(Thing thing, HttpClient httpClient) {
        super(thing);

        this.ntfyWebSocket = new NtfyWebSocket(this);
        this.ntfySender = new NtfySender(this.getConfigAs(NtfyTopicConfiguration.class).topicname, httpClient,
                this::getBridgeHandler);
    }

    private NtfyConnectionHandler getBridgeHandler() {
        return (NtfyConnectionHandler) (java.util.Objects
                .requireNonNull(java.util.Objects.requireNonNull(getBridge()).getHandler()));
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(NtfyActions.class);
    }

    @Override
    public void thingUpdated(Thing thing) {
        super.thingUpdated(thing);

        getBridgeHandler().createAndRegisterWebSocketClient(thing);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE
                && getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE) {
            updateStatus(ThingStatus.UNKNOWN);
            initialize();
        } else {
            super.bridgeStatusChanged(bridgeStatusInfo);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannels();
        }
    }

    @Override
    public void initialize() {
        if (isInitializing) {
            return;
        }
        this.isInitializing = true;

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            try {
                getBridgeHandler().createAndRegisterWebSocketClient(thing);
                startConnection();
            } finally {
                this.isInitializing = false;
            }
        });
    }

    private void startConnection() {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            if (!getBridgeHandler().startWebSocketConnection(thing, ntfyWebSocket)) {
                updateStatus(ThingStatus.OFFLINE);
            }
        }
    }

    @Override
    public void connectionEstablished() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void connectionLost(String reason) {
    }

    @Override
    public void connectionError(Throwable cause) {
        getBridgeHandler().connectionError(cause);
    }

    @Override
    public void messageRecieved(BaseEvent event) {
        if (event instanceof MessageEvent message) {
            this.lastMessageEvent = message;
            updateChannels();
        }
    }

    private void updateChannels() {
        final MessageEvent lastMessageEvent = this.lastMessageEvent;
        if (lastMessageEvent != null) {
            updateState(NtfyBindingConstants.CHANNEL_NTFY_LASTMESSAGE,
                    StringType.valueOf(lastMessageEvent.getMessage()));
            updateState(NtfyBindingConstants.CHANNEL_NTFY_LASTMESSAGETIME,
                    new DateTimeType(lastMessageEvent.getTime()));
            updateState(NtfyBindingConstants.CHANNEL_NTFY_LASTMESSAGEID, StringType.valueOf(lastMessageEvent.getId()));
        }
    }

    /**
     * Sends a message using the configured {@link NtfySender}.
     *
     * @param ntfyMessage the message to send
     * @return the id of the created message on success, or an empty string when sending failed
     * @throws URISyntaxException
     */
    public String sendMessage(NtfyMessage ntfyMessage) throws URISyntaxException {
        @Nullable
        MessageEvent sendMessage = ntfySender.sendMessage(ntfyMessage);

        if (sendMessage == null) {
            return "";
        }
        return sendMessage.getId();
    }

    public String sendFile(String file, @Nullable String filename, @Nullable String sequenceId)
            throws URISyntaxException {
        @Nullable
        MessageEvent sendMessage = ntfySender.sendFile(file, filename, sequenceId);

        if (sendMessage == null) {
            return "";
        }
        return sendMessage.getId();
    }

    /**
     * Deletes a message identified by the given sequence id via the {@link NtfySender}.
     *
     * @param sequenceId the id of the message to delete
     * @return {@code true} when deletion succeeded, {@code false} otherwise
     * @throws URISyntaxException when the underlying request URI could not be constructed
     */
    public boolean deleteMessage(String sequenceId) throws URISyntaxException {
        return ntfySender.deleteMessage(sequenceId);
    }
}
