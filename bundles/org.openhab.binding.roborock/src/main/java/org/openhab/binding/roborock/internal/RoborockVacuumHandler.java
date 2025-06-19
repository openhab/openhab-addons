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
package org.openhab.binding.roborock.internal;

import static org.openhab.binding.roborock.internal.RoborockBindingConstants.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.roborock.internal.api.Home;
import org.openhab.binding.roborock.internal.api.HomeData;
import org.openhab.binding.roborock.internal.api.Login.Rriot;
import org.openhab.binding.roborock.internal.util.HashUtil;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import com.hivemq.client.mqtt.lifecycle.MqttDisconnectSource;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3DisconnectException;
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;

/**
 * The {@link RoborockHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
public class RoborockVacuumHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(RoborockVacuumHandler.class);

    @Nullable
    RoborockAccountHandler bridgeHandler;
    private @Nullable ScheduledFuture<?> pollFuture;

    private String token = "";
    private @Nullable Rriot rriot;
    private String rrHomeId = "";
    private @Nullable Mqtt3AsyncClient mqttClient;
    private @Nullable MqttConnection mqttConnection;
    private String mqttUser = "";

    private final Object mqttConnectionLock = new Object();

    public RoborockVacuumHandler(Thing thing) {
        super(thing);
    }

    protected String getToken() {
        RoborockAccountHandler localBridge = bridgeHandler;
        if (localBridge == null) {
            return "";
        }
        try {
            return localBridge.getToken();
        } catch (IllegalStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, e.getMessage());
            return "";
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // we do not have any channels -> nothing to do here
    }

    @Override
    public void initialize() {
        if (!(getBridge() instanceof Bridge bridge
                && bridge.getHandler() instanceof RoborockAccountHandler accountHandler)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "No Roborock Bridge selected");
            return;
        }
        bridgeHandler = accountHandler;
        updateStatus(ThingStatus.UNKNOWN);
        token = getToken();
        if (!token.isEmpty()) {
            rriot = bridgeHandler.getRriot();
            Home home;
            home = bridgeHandler.getHomeDetail();
            if (home != null) {
                rrHomeId = Integer.toString(home.data.rrHomeId);
            }
            updateStatus(ThingStatus.ONLINE);
            schedulePoll();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Token empty, can't login");
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        stopPoll();
    }

    private void schedulePoll() {
        this.pollFuture = scheduler.scheduleWithFixedDelay(this::pollStatus, 0, 300, TimeUnit.SECONDS);
    }

    private void stopPoll() {
        final Future<?> future = pollFuture;
        if (future != null) {
            future.cancel(true);
            pollFuture = null;
        }
    }

    private void establishMqttConnection() {
        if (rriot == null) {
            logger.trace("Api not yet initialized, postponing MQTT connection");
            return;
        }

        synchronized (mqttConnectionLock) {
            MqttConnection oldConnection = mqttConnection;
            if (oldConnection != null) {
                try {
                    oldConnection.disconnect().get();
                } catch (InterruptedException | ExecutionException e) {
                    logger.debug("Could not discard MQTT connection", e);
                }
            }

            mqttConnection = null;

            try {
                Mqtt3AsyncClient client = establishMqttConnection(rriot.u);

                MqttConnection connection = new MqttConnection(client, rriot, mqttUser);
                subscribeForDeviceLocked(connection, getThing().getUID().getId());

                mqttConnection = connection;

                // for (AbstractEcoflowHandler handler : activeChildHandlers.values()) {
                // handler.handleMqttConnected();
                // }
            } catch (RoborockCommunicationException e) {
                logger.debug("Could not establish MQTT connection", e);
                // mqttConnectTask.schedule(5);
            }
        }
    }

    private void subscribeForDeviceLocked(MqttConnection connection, String deviceId)
            throws RoborockCommunicationException {
        try {
            logger.debug("Subscribing for updates from {}", deviceId);
            connection.subscribeForDevice(deviceId, this::handleQuotaMessage, this::handleStatusMessage);
        } catch (ExecutionException | InterruptedException e) {
            throw new RoborockCommunicationException(e);
        }
    }

    private Mqtt3AsyncClient establishMqttConnection(String username) throws RoborockCommunicationException {
        String mqttHost = "";
        int mqttPort = 1883;
        mqttUser = "";
        String mqttPassword = "";
        try {
            URI mqttURL = new URI(rriot.r.m);
            mqttHost = mqttURL.getHost();
            mqttPort = mqttURL.getPort();
            mqttUser = HashUtil.md5Hex(rriot.u + ':' + rriot.k).substring(2, 10);
            mqttPassword = HashUtil.md5Hex(rriot.s + ':' + rriot.k).substring(16);
        } catch (URISyntaxException e) {
            logger.info("Malformed mqtt URL");
        }

        Mqtt3SimpleAuth auth = Mqtt3SimpleAuth.builder().username(mqttUser).password(mqttPassword.getBytes()).build();

        final MqttClientDisconnectedListener disconnectListener = ctx -> {
            boolean expectedShutdown = ctx.getSource() == MqttDisconnectSource.USER
                    && ctx.getCause() instanceof Mqtt3DisconnectException;
            mqttConnection = null;
            if (!expectedShutdown) {
                logger.debug("MQTT disconnected (source {}): {}", ctx.getSource(), ctx.getCause().getMessage());
                // mqttConnectTask.schedule(5);
            }
        };

        final Mqtt3AsyncClient client = MqttClient.builder() //
                .useMqttVersion3() //
                .identifier(mqttUser) //
                .simpleAuth(auth) //
                .serverHost(mqttHost) //
                .serverPort(mqttPort) //
                .sslWithDefaultConfig() //
                .addDisconnectedListener(disconnectListener) //
                .buildAsync();
        try {
            logger.debug("Opening MQTT connection");
            client.connect().get();

            logger.debug("Established MQTT connection");
            return client;
        } catch (ExecutionException | InterruptedException e) {
            throw new RoborockCommunicationException(e);
        }
    }

    private void handleQuotaMessage(@Nullable Mqtt3Publish publish) {
        if (publish == null) {
            return;
        }
        // final AbstractEcoflowHandler handler = findHandlerForTopic(publish.getTopic());
        // if (handler != null) {
        // handler.handleQuotaMessage(extractPayload(publish));
        // }
    }

    private void handleStatusMessage(@Nullable Mqtt3Publish publish) {
        if (publish == null) {
            return;
        }
        // final AbstractEcoflowHandler handler = findHandlerForTopic(publish.getTopic());
        // if (handler != null) {
        // handler.handleStatusMessage(extractPayload(publish));
        // }
    }

    private static class MqttConnection {
        final Mqtt3AsyncClient client;
        private final String topicBase;

        MqttConnection(Mqtt3AsyncClient client, @Nullable Rriot rriot2, String userName) {
            this.client = client;
            String topic = "rr/m/o/" + rriot2.u + "/" + userName + "/#";
            topicBase = topic;
        }

        void subscribeForDevice(String deviceId, Consumer<@Nullable Mqtt3Publish> quotaHandler,
                Consumer<@Nullable Mqtt3Publish> statusHandler) throws ExecutionException, InterruptedException {
            String deviceTopicBase = topicBase + deviceId + "/";
            client.subscribeWith().topicFilter(deviceTopicBase + "quota").callback(quotaHandler).send().get();
            client.subscribeWith().topicFilter(deviceTopicBase + "status").callback(statusHandler).send().get();
        }

        CompletableFuture<Void> disconnect() {
            return client.disconnect();
        }
    }

    private void pollStatus() {

        HomeData homeData;
        homeData = bridgeHandler.getHomeData(rrHomeId, rriot);
        if (homeData != null) {
            for (int i = 0; i < homeData.result.devices.length; i++) {
                if (getThing().getUID().getId().equals(homeData.result.devices[i].duid)) {
                    logger.info("Update channels");
                    updateState(RoborockBindingConstants.CHANNEL_ERROR_ID,
                            new DecimalType(homeData.result.devices[i].deviceStatus.errorCode));
                    updateState(RoborockBindingConstants.CHANNEL_STATE,
                            new DecimalType(homeData.result.devices[i].deviceStatus.vacuumState));
                    updateState(RoborockBindingConstants.CHANNEL_BATTERY,
                            new DecimalType(homeData.result.devices[i].deviceStatus.battery));
                    updateState(RoborockBindingConstants.CHANNEL_FAN_POWER,
                            new DecimalType(homeData.result.devices[i].deviceStatus.fanPower));
                    updateState(RoborockBindingConstants.CHANNEL_CONSUMABLE_MAIN_PERC,
                            new DecimalType(homeData.result.devices[i].deviceStatus.mainBrushWorkTime));
                    updateState(RoborockBindingConstants.CHANNEL_CONSUMABLE_SIDE_PERC,
                            new DecimalType(homeData.result.devices[i].deviceStatus.sideBrushWorkTime));
                    updateState(RoborockBindingConstants.CHANNEL_CONSUMABLE_FILTER_PERC,
                            new DecimalType(homeData.result.devices[i].deviceStatus.filterWorkTime));
                    if (homeData.result.devices[i].online == true) {
                        // get MQTT data
                    }
                }
            }
        }

        updateStatus(ThingStatus.ONLINE);
    }
}
