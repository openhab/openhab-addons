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
package org.openhab.binding.ecoflow.internal.handler;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.ecoflow.internal.api.EcoflowApi;
import org.openhab.binding.ecoflow.internal.api.EcoflowApiException;
import org.openhab.binding.ecoflow.internal.api.dto.response.MqttConnectionData;
import org.openhab.binding.ecoflow.internal.config.EcoflowApiConfiguration;
import org.openhab.binding.ecoflow.internal.discovery.EcoflowDeviceDiscoveryService;
import org.openhab.binding.ecoflow.internal.util.SchedulerTask;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttTopic;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import com.hivemq.client.mqtt.lifecycle.MqttDisconnectSource;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3DisconnectException;
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;

/**
 * The {@link EcoflowApiHandler} is responsible for connecting to the Ecoflow cloud API account.
 *
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class EcoflowApiHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(EcoflowApiHandler.class);
    private static final long RETRY_INTERVAL_SECONDS = 120;

    private Optional<EcoflowDeviceDiscoveryService> discoveryService = Optional.empty();
    private final SchedulerTask initTask;
    private final SchedulerTask mqttConnectTask;
    private final HttpClient httpClient;
    private @Nullable EcoflowApi api;
    private @Nullable MqttConnection mqttConnection;

    private final Object mqttConnectionLock = new Object();
    private final Map<String, AbstractEcoflowHandler> activeChildHandlers = new HashMap<>();

    public EcoflowApiHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
        this.initTask = new SchedulerTask(scheduler, logger, "API Init", this::initApi);
        this.mqttConnectTask = new SchedulerTask(scheduler, logger, "MQTT Connection", this::establishMqttConnection);
    }

    public void setDiscoveryService(EcoflowDeviceDiscoveryService discoveryService) {
        this.discoveryService = Optional.of(discoveryService);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Ecoflow account '{}'", getThing().getUID().getId());
        updateStatus(ThingStatus.UNKNOWN);
        initTask.submit();
    }

    @Override
    public void dispose() {
        super.dispose();
        api = null;
        discoveryService.ifPresent(ds -> ds.stopScan());
        initTask.cancel();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(EcoflowDeviceDiscoveryService.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            logger.debug("Refreshing Ecoflow API account '{}'", getThing().getUID().getId());
            scheduleApiInit(0);
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        super.childHandlerInitialized(childHandler, childThing);
        logger.debug("child handler {} initialized", childHandler);
        if (childHandler instanceof AbstractEcoflowHandler deviceHandler) {
            synchronized (mqttConnectionLock) {
                MqttConnection connection = mqttConnection;
                activeChildHandlers.put(deviceHandler.getSerialNumber(), deviceHandler);
                if (connection != null) {
                    try {
                        subscribeForDeviceLocked(connection, deviceHandler.getSerialNumber());
                        deviceHandler.handleMqttConnected();
                    } catch (EcoflowApiException e) {
                        logger.debug("{}: Could not subscribe for MQTT updates, re-scheduling connection",
                                deviceHandler.getSerialNumber());
                        mqttConnectTask.schedule(5);
                    }
                } else {
                    mqttConnectTask.submit();
                }
            }
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        super.childHandlerDisposed(childHandler, childThing);
        logger.debug("child handler {} disposed", childHandler);
        if (childHandler instanceof AbstractEcoflowHandler deviceHandler) {
            synchronized (mqttConnectionLock) {
                final MqttConnection connection = mqttConnection;
                activeChildHandlers.remove(deviceHandler.getSerialNumber());
                if (activeChildHandlers.isEmpty() && connection != null) {
                    connection.disconnect();
                    mqttConnection = null;
                }
            }
        }
    }

    private void scheduleApiInit(long delaySeconds) {
        initTask.cancel();
        initTask.schedule(delaySeconds);
    }

    public synchronized EcoflowApi getApi() {
        final EcoflowApi api = this.api;
        if (api == null) {
            throw new IllegalStateException();
        }
        return api;
    }

    private void initApi() {
        try {
            EcoflowApiConfiguration config = getConfigAs(EcoflowApiConfiguration.class);
            EcoflowApi api = new EcoflowApi(httpClient, config.accessKey, config.secretKey);

            api.getDeviceList(); // dummy call to see whether login data is correct

            synchronized (mqttConnectionLock) {
                synchronized (this) {
                    this.api = api;
                    updateStatus(ThingStatus.ONLINE);
                    discoveryService.ifPresent(ds -> ds.startScanningWithApi(api));
                }
                logger.debug("Ecoflow API initialized");
                if (!activeChildHandlers.isEmpty()) {
                    mqttConnectTask.submit();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            updateStatus(ThingStatus.OFFLINE);
        } catch (EcoflowApiException e) {
            logger.debug("Ecoflow API initialization failed", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            scheduleApiInit(RETRY_INTERVAL_SECONDS);
        }
    }

    private void establishMqttConnection() {
        final EcoflowApi api;
        synchronized (this) {
            api = this.api;
        }
        if (api == null) {
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
                MqttConnectionData connectData = api.createMqttLogin();
                Mqtt3AsyncClient client = establishMqttConnection(connectData);

                MqttConnection connection = new MqttConnection(client, connectData.userName);
                for (String serialNumber : activeChildHandlers.keySet()) {
                    subscribeForDeviceLocked(connection, serialNumber);
                }

                mqttConnection = connection;

                for (AbstractEcoflowHandler handler : activeChildHandlers.values()) {
                    handler.handleMqttConnected();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (EcoflowApiException e) {
                logger.debug("Could not establish MQTT connection", e);
                mqttConnectTask.schedule(5);
            }
        }
    }

    private void subscribeForDeviceLocked(MqttConnection connection, String serialNumber) throws EcoflowApiException {
        try {
            logger.debug("Subscribing for updates from {}", serialNumber);
            connection.subscribeForDevice(serialNumber, this::handleQuotaMessage, this::handleStatusMessage);
        } catch (ExecutionException | InterruptedException e) {
            throw new EcoflowApiException(e);
        }
    }

    private Mqtt3AsyncClient establishMqttConnection(MqttConnectionData connectData) throws EcoflowApiException {
        Mqtt3SimpleAuth auth = Mqtt3SimpleAuth.builder() //
                .username(connectData.userName) //
                .password(connectData.password.getBytes()) //
                .build();

        final MqttClientDisconnectedListener disconnectListener = ctx -> {
            boolean expectedShutdown = ctx.getSource() == MqttDisconnectSource.USER
                    && ctx.getCause() instanceof Mqtt3DisconnectException;
            mqttConnection = null;
            if (!expectedShutdown) {
                logger.debug("MQTT disconnected (source {}): {}", ctx.getSource(), ctx.getCause().getMessage());
                mqttConnectTask.schedule(5);
            }
        };

        final Mqtt3AsyncClient client = MqttClient.builder() //
                .useMqttVersion3() //
                .identifier(connectData.userName) //
                .simpleAuth(auth) //
                .serverHost(connectData.host) //
                .serverPort(connectData.port) //
                .sslWithDefaultConfig() //
                .addDisconnectedListener(disconnectListener) //
                .buildAsync();
        try {
            logger.debug("Opening MQTT connection");
            client.connect().get();

            logger.debug("Established MQTT connection");
            return client;
        } catch (ExecutionException | InterruptedException e) {
            throw new EcoflowApiException(e);
        }
    }

    private void handleQuotaMessage(@Nullable Mqtt3Publish publish) {
        if (publish == null) {
            return;
        }
        final AbstractEcoflowHandler handler = findHandlerForTopic(publish.getTopic());
        if (handler != null) {
            handler.handleQuotaMessage(extractPayload(publish));
        }
    }

    private void handleStatusMessage(@Nullable Mqtt3Publish publish) {
        if (publish == null) {
            return;
        }
        final AbstractEcoflowHandler handler = findHandlerForTopic(publish.getTopic());
        if (handler != null) {
            handler.handleStatusMessage(extractPayload(publish));
        }
    }

    @Nullable
    private AbstractEcoflowHandler findHandlerForTopic(MqttTopic topic) {
        List<String> levels = topic.getLevels();
        if (levels.size() != 5) {
            throw new IllegalStateException("Unexpected topic " + topic);
        }
        synchronized (mqttConnectionLock) {
            return activeChildHandlers.get(levels.get(3));
        }
    }

    private JsonObject extractPayload(Mqtt3Publish publish) {
        return JsonParser.parseString(new String(publish.getPayloadAsBytes())).getAsJsonObject();
    }

    private static class MqttConnection {
        final Mqtt3AsyncClient client;
        private final String topicBase;

        MqttConnection(Mqtt3AsyncClient client, String userName) {
            this.client = client;
            topicBase = String.format("/open/%s/", userName);
        }

        void subscribeForDevice(String serialNumber, Consumer<@Nullable Mqtt3Publish> quotaHandler,
                Consumer<@Nullable Mqtt3Publish> statusHandler) throws ExecutionException, InterruptedException {
            String deviceTopicBase = topicBase + serialNumber + "/";
            client.subscribeWith().topicFilter(deviceTopicBase + "quota").callback(quotaHandler).send().get();
            client.subscribeWith().topicFilter(deviceTopicBase + "status").callback(statusHandler).send().get();
        }

        CompletableFuture<Void> disconnect() {
            return client.disconnect();
        }
    }
}
