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

import static org.openhab.binding.ntfy.internal.NtfyBindingConstants.*;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ntfy.internal.action.NtfyActions;
import org.openhab.binding.ntfy.internal.models.BaseEvent;
import org.openhab.binding.ntfy.internal.models.MessageEvent;
import org.openhab.binding.ntfy.internal.network.NtfyMessage;
import org.openhab.binding.ntfy.internal.network.NtfySender;
import org.openhab.binding.ntfy.internal.network.NtfyWebSocket;
import org.openhab.binding.ntfy.internal.network.WebSocketConnectionListener;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
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
    private @Nullable NtfySender ntfySender;
    private boolean isInitializing;
    private @Nullable MessageEvent lastMessageEvent;

    /**
     * Creates a new {@link NtfyTopicHandler} for the provided topic thing.
     *
     * @param thing the topic thing this handler belongs to
     * @param httpClient the HTTP client used by the sender to perform REST calls
     */
    public NtfyTopicHandler(Thing thing) {
        super(thing);

        this.ntfyWebSocket = new NtfyWebSocket(this);
    }

    private @Nullable NtfyConnectionHandler getBridgeHandler() {
        Bridge bridge = getBridge();

        if (bridge != null) {
            if (bridge.getHandler() instanceof NtfyConnectionHandler bridgeHandler) {
                return bridgeHandler;
            }
        }
        return null;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(NtfyActions.class);
    }

    @Override
    public void thingUpdated(Thing thing) {
        super.thingUpdated(thing);

        start(thing);
    }

    private void start(Thing thing) {
        NtfyConnectionHandler bridgeHandler = getBridgeHandler();

        if (bridgeHandler == null) {
            return;
        }
        ntfySender = bridgeHandler.CreateSender(this.getConfigAs(NtfyTopicConfiguration.class).topicName);
        bridgeHandler.createAndRegisterWebSocketClient(thing);
        startConnection();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            initialize();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            ntfySender = null;
        }

        super.bridgeStatusChanged(bridgeStatusInfo);
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
                start(thing);
            } finally {
                this.isInitializing = false;
            }
        });
    }

    private void startConnection() {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            NtfyConnectionHandler bridgeHandler = getBridgeHandler();
            if (bridgeHandler == null) {
                return;
            }
            if (!bridgeHandler.startWebSocketConnection(thing, ntfyWebSocket)) {
                updateStatus(ThingStatus.OFFLINE);
            }
        }
    }

    @Override
    public void connectionEstablished() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void connectionLost(@Nullable String reason) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                reason == null || reason.isBlank() ? "WebSocket connection lost" : reason);
    }

    @Override
    public void connectionError(Throwable cause) {
        NtfyConnectionHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            return;
        }
        updateStatus(ThingStatus.OFFLINE);
        bridgeHandler.connectionError(cause);
    }

    @Override
    public void messageReceived(BaseEvent event) {
        if (event instanceof MessageEvent message) {
            this.lastMessageEvent = message;
            updateChannels();
        }
    }

    private void updateChannels() {
        final MessageEvent lastMessageEvent = this.lastMessageEvent;
        if (lastMessageEvent != null) {
            updateState(CHANNEL_LASTMESSAGE, StringType.valueOf(lastMessageEvent.getMessage()));
            updateState(CHANNEL_LASTMESSAGETIME, new DateTimeType(lastMessageEvent.getTime()));
            updateState(CHANNEL_LASTMESSAGEID, StringType.valueOf(lastMessageEvent.getId()));
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
        final @Nullable NtfySender sender = ntfySender;
        if (sender == null) {
            return "";
        }

        MessageEvent sendMessage = sender.sendMessage(ntfyMessage);

        if (sendMessage == null) {
            return "";
        }
        return sendMessage.getId();
    }

    /**
     * Uploads a local file to the configured topic via the underlying
     * {@link NtfySender#sendFile(String, String, String)} and returns the
     * created message id on success.
     *
     * @param file the filesystem path to the file to upload
     * @param filename optional filename to present to recipients (may be null)
     * @param sequenceId optional sequence id to associate with the uploaded message
     * @return the created message id on success, or an empty string on failure
     * @throws URISyntaxException when the constructed request URI is invalid
     */
    public String sendFile(String file, @Nullable String filename, @Nullable String sequenceId)
            throws URISyntaxException {
        final @Nullable NtfySender sender = ntfySender;
        if (sender == null) {
            return "";
        }

        @Nullable
        MessageEvent sendMessage = sender.sendFile(file, filename, sequenceId);

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
        final @Nullable NtfySender sender = ntfySender;
        if (sender == null) {
            return false;
        }
        return sender.deleteMessage(sequenceId);
    }
}
