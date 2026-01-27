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
package org.openhab.binding.worxlandroid.internal.handler;

import static org.openhab.binding.worxlandroid.internal.WorxLandroidBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.worxlandroid.internal.api.WebApiException;
import org.openhab.binding.worxlandroid.internal.api.WorxApiDeserializer;
import org.openhab.binding.worxlandroid.internal.api.dto.Payload;
import org.openhab.binding.worxlandroid.internal.mqtt.AWSClient;
import org.openhab.binding.worxlandroid.internal.mqtt.AWSClientCallbackI;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
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

import software.amazon.awssdk.crt.mqtt.MqttMessage;

/**
 * The{@link AWSClientThingHandler} is handles outside communications (AWS,API) parts
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public abstract class AWSClientThingHandler extends BaseThingHandler
        implements AWSClientCallbackI, ThingHandlerHelper, AccessTokenRefreshListener {
    private static final Duration MIN_PUBLISH_DELAY_S = Duration.ofSeconds(2);

    private final Logger logger = LoggerFactory.getLogger(AWSClientThingHandler.class);
    private final AWSClient awsClient;
    protected final WorxApiDeserializer deserializer;

    protected String endpoint = "";
    protected String uuid = "";
    protected String userId = "";
    protected String topic = "";
    protected String token = "";

    private Instant lastPublishTS = Instant.MIN;
    private int lastReqHash = 0;

    public AWSClientThingHandler(Thing thing, WorxApiDeserializer deserializer) {
        super(thing);
        this.deserializer = deserializer;
        this.awsClient = new AWSClient(this);
    }

    @Override
    public void initialize() {
        checkInitBridgeOAuth();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        checkInitBridgeOAuth();
    }

    private void checkInitBridgeOAuth() {
        WorxLandroidBridgeHandler bridgeHandler = getBridgeHandler(getBridge(), WorxLandroidBridgeHandler.class);
        if (bridgeHandler != null) {
            setAccessToken(bridgeHandler.getAccessToken());
            bridgeHandler.oAuthClientService.addAccessTokenRefreshListener(this);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void dispose() {
        if (!topic.isEmpty()) {
            awsClient.unsubscribe(topic);
        }
        awsClient.dispose();

        WorxLandroidBridgeHandler bridgeHandler = getBridgeHandler(getBridge(), WorxLandroidBridgeHandler.class);
        if (bridgeHandler != null) {
            bridgeHandler.oAuthClientService.removeAccessTokenRefreshListener(this);
        }

        super.dispose();
    }

    @Override
    public void onAWSConnectionSuccess() {
        logger.debug("AWS connection is available");
        if (!topic.isEmpty()) {
            awsClient.subscribe(topic, this::onMqttMessage);
            logger.debug("subscribed to topic: {}", topic);
        } else {
            logger.warn("Connected but no topic to subscribe to");
        }

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
        updateChannelOnOff(GROUP_AWS, CHANNEL_CONNECTED, awsClient.isConnected());
    }

    @Override
    public void onAWSConnectionClosed() {
        // Don't try to reconnect if the connection is closed by the thing being disable
        if (thing.getStatus() == ThingStatus.ONLINE) {
            updateChannelOnOff(GROUP_AWS, CHANNEL_CONNECTED, awsClient.isConnected());

            WorxLandroidBridgeHandler bridgeHandler = getBridgeHandler(getBridge(), WorxLandroidBridgeHandler.class);
            if (bridgeHandler != null) {
                bridgeHandler.requestTokenRefresh();
            }
        }
    }

    @Override
    public void onAWSConnectionFailed(@Nullable String message) {
        updateChannelOnOff(GROUP_AWS, CHANNEL_CONNECTED, awsClient.isConnected());
        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "No AWS Connection");
    }

    @Override
    public boolean isLinked(ChannelUID channelUID) {
        return super.isLinked(channelUID);
    }

    @Override
    public void updateState(ChannelUID channelUID, State state) {
        super.updateState(channelUID, state);
    }

    public void publishMessage(String topic, String cmd) {
        Instant now = Instant.now();
        int requestHash = topic.hashCode() + cmd.hashCode();
        if (requestHash == lastReqHash) {
            if (now.isBefore(lastPublishTS.plus(MIN_PUBLISH_DELAY_S))) {
                logger.debug("Won't post again too soon");
                return;
            }
        }
        lastPublishTS = now;
        lastReqHash = requestHash;
        logger.debug("publish on topic: '{}' - message: '{}'", topic, cmd);
        awsClient.publish(topic, cmd);
    }

    public void onMqttMessage(MqttMessage mqttMessage) {
        String messagePayload = new String(mqttMessage.getPayload(), StandardCharsets.UTF_8);
        logger.debug("onMessage: {}", messagePayload);
        try {
            Payload payload = deserializer.deserialize(Payload.class, messagePayload);
            internalHandlePayload(payload);
        } catch (WebApiException e) {
            logger.warn("Error processing incoming AWS message: {}", e.getMessage());
        }
    }

    protected abstract void internalHandlePayload(Payload payload);

    @Override
    public void onAccessTokenResponse(AccessTokenResponse tokenResponse) {
        setAccessToken(tokenResponse.getAccessToken());
    }

    private void setAccessToken(String token) {
        this.token = token;
        connectAWS();
    }

    public void connectAws(String mqttEndpoint, String uuid, String userId, String commandOut) {
        this.endpoint = mqttEndpoint;
        this.uuid = uuid;
        this.userId = userId;
        this.topic = commandOut;
        connectAWS();
    }

    private void connectAWS() {
        awsClient.disconnect();
        if (endpoint.isEmpty() || userId.isEmpty() || uuid.isEmpty() || token.isEmpty()) {
            logger.debug("Some data missing to initiate AWS connection");
            return;
        }
        awsClient.connect(endpoint, userId, uuid, token);
        updateStatus(ThingStatus.ONLINE);
        updateChannelOnOff(GROUP_AWS, CHANNEL_CONNECTED, awsClient.isConnected());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        }
        if (!isOnline()) {
            logger.warn("handleCommand mower: {} is offline!", getThing().getUID());
            return;
        }
        WorxLandroidBridgeHandler bridgeHandler = getBridgeHandler(getBridge(), WorxLandroidBridgeHandler.class);
        if (bridgeHandler == null) {
            logger.error("no bridgeHandler");
            return;
        }
        String groupId = channelUID.getGroupId();
        String channelId = channelUID.getIdWithoutGroup();
        internalHandleCommand(groupId, channelId, command);
    }

    protected abstract void internalHandleCommand(@Nullable String groupId, String channelId, Command command);
}
