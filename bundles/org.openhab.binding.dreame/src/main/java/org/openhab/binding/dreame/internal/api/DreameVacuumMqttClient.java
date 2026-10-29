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
package org.openhab.binding.dreame.internal.api;

import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.openhab.binding.dreame.internal.model.DreameMqttConfiguration;
import org.openhab.binding.dreame.internal.util.DreameVacuumDiagnostics;
import org.openhab.binding.dreame.internal.util.DreameVacuumMapImage;

/**
 * Subscribes to one vacuum's status topic without publishing commands or interpreting mower telemetry.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class DreameVacuumMqttClient implements MqttCallback {
    private final MqttAsyncClient client;
    private final DreameMqttConfiguration configuration;
    private final Consumer<String> diagnostics;
    private final String model;
    private final Consumer<Map<String, Integer>> properties;
    private Consumer<String> mapImage = image -> {
    };
    private Consumer<String> mapList = objectName -> {
    };
    private boolean closed;
    private boolean started;
    private volatile boolean subscribed;

    public DreameVacuumMqttClient(DreameMqttConfiguration configuration, String model, Consumer<String> diagnostics,
            Consumer<Map<String, Integer>> properties) throws MqttException {
        this(configuration, model, diagnostics, properties,
                new MqttAsyncClient(serverUri(configuration), configuration.clientId(), new MemoryPersistence()));
    }

    public DreameVacuumMqttClient(DreameMqttConfiguration configuration, String model, Consumer<String> diagnostics,
            Consumer<Map<String, Integer>> properties, Consumer<String> mapImage) throws MqttException {
        this(configuration, model, diagnostics, properties);
        this.mapImage = mapImage;
    }

    public DreameVacuumMqttClient(DreameMqttConfiguration configuration, String model, Consumer<String> diagnostics,
            Consumer<Map<String, Integer>> properties, Consumer<String> mapImage, Consumer<String> mapList)
            throws MqttException {
        this(configuration, model, diagnostics, properties, mapImage);
        this.mapList = mapList;
    }

    DreameVacuumMqttClient(DreameMqttConfiguration configuration, String model, Consumer<String> diagnostics,
            Consumer<Map<String, Integer>> properties, MqttAsyncClient client) {
        this.configuration = configuration;
        this.model = model;
        this.diagnostics = diagnostics;
        this.properties = properties;
        this.client = client;
        client.setCallback(this);
    }

    static String serverUri(DreameMqttConfiguration configuration) {
        if (!configuration.host().matches("[A-Za-z0-9.-]{1,253}") || configuration.port() < 1
                || configuration.port() > 65535 || configuration.topic().isBlank()
                || configuration.topic().contains("#") || configuration.topic().contains("+")
                || configuration.topic().chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Invalid vacuum MQTT routing metadata");
        }
        return "ssl://" + configuration.host() + ":" + configuration.port();
    }

    public synchronized void connect() throws MqttException {
        if (closed || started) {
            return;
        }
        MqttConnectOptions options = DreameMqttClient.createConnectOptions(configuration);
        // The handler retries with fresh account credentials instead of reconnecting with an expired token.
        options.setAutomaticReconnect(false);
        started = true;
        client.connect(options, null, new IMqttActionListener() {
            @Override
            public void onSuccess(@Nullable IMqttToken token) {
                subscribe();
            }

            @Override
            public void onFailure(@Nullable IMqttToken token, @Nullable Throwable exception) {
                reportFailure("connect-failed");
            }
        });
    }

    private synchronized void subscribe() {
        if (closed) {
            return;
        }
        try {
            client.subscribe(configuration.topic(), 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(@Nullable IMqttToken token) {
                    subscriptionComplete(token);
                }

                @Override
                public void onFailure(@Nullable IMqttToken token, @Nullable Throwable exception) {
                    reportFailure("subscribe-failed");
                }
            });
        } catch (MqttException e) {
            reportFailure("subscribe-failed");
        }
    }

    private synchronized void subscriptionComplete(@Nullable IMqttToken token) {
        if (closed) {
            return;
        }
        int[] granted = token == null ? null : token.getGrantedQos();
        if (granted == null || granted.length != 1 || granted[0] != 0) {
            reportFailure("subscribe-rejected");
            return;
        }
        subscribed = true;
        diagnostics.accept("subscribed (device connectivity not established)");
    }

    private synchronized void reportFailure(String stage) {
        subscribed = false;
        if (!closed) {
            diagnostics.accept(stage);
        }
    }

    @Override
    public void connectionLost(@Nullable Throwable cause) {
        reportFailure("connection-lost");
    }

    @Override
    public void messageArrived(@Nullable String topic, @Nullable MqttMessage message) {
        synchronized (this) {
            if (closed || !configuration.topic().equals(topic) || message == null) {
                return;
            }
        }
        byte[] payload = message.getPayload();
        diagnostics.accept(DreameVacuumDiagnostics.describeMessage(payload, model));
        Map<String, Integer> updates = DreameVacuumDiagnostics.readProperties(payload, model);
        if (!updates.isEmpty()) {
            // The handler checks the connection generation again before publishing states.
            properties.accept(updates);
        }
        String image = DreameVacuumMapImage.encodedFromMessage(payload, model);
        if (image != null) {
            mapImage.accept(image);
        }
        String mapListObjectName = DreameVacuumMapImage.mapListObjectNameFromMessage(payload, model);
        if (mapListObjectName != null) {
            mapList.accept(mapListObjectName);
        }
    }

    @Override
    public void deliveryComplete(@Nullable IMqttDeliveryToken token) {
    }

    public boolean isSubscribed() {
        return subscribed && client.isConnected();
    }

    public void close() {
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
            subscribed = false;
        }
        try {
            client.disconnectForcibly(0, 1000, false);
        } catch (MqttException ignored) {
            // Also close clients whose connection attempt failed or is still pending.
        } finally {
            try {
                client.close(true);
            } catch (MqttException ignored) {
                // No exception text: the MQTT library may include credentials or routing metadata.
            }
        }
    }
}
