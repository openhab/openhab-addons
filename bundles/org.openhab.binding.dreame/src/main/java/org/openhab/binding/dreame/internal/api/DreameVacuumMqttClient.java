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
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dreame.internal.model.DreameMqttConfiguration;
import org.openhab.binding.dreame.internal.util.DreameVacuumDiagnostics;
import org.openhab.binding.dreame.internal.util.DreameVacuumMapImage;
import org.openhab.binding.dreame.internal.util.DreameVacuumMapImage.MapListRevision;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttConnectionObserver;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;

/**
 * Subscribes to one vacuum's status topic without publishing commands or interpreting mower telemetry.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class DreameVacuumMqttClient implements MqttMessageSubscriber, MqttConnectionObserver {
    private final MqttBrokerConnection connection;
    private final DreameMqttConfiguration configuration;
    private final Consumer<String> diagnostics;
    private final String model;
    private final Consumer<Map<String, Integer>> properties;
    private Consumer<String> mapImage = image -> {
    };
    private Consumer<MapListRevision> mapList = revision -> {
    };
    private boolean closed;
    private boolean started;
    private volatile boolean subscribed;

    public DreameVacuumMqttClient(DreameMqttConfiguration configuration, String model, Consumer<String> diagnostics,
            Consumer<Map<String, Integer>> properties) throws DreameCloudException {
        this(configuration, model, diagnostics, properties, DreameMqttClient.createConnection(configuration, false));
    }

    public DreameVacuumMqttClient(DreameMqttConfiguration configuration, String model, Consumer<String> diagnostics,
            Consumer<Map<String, Integer>> properties, Consumer<String> mapImage) throws DreameCloudException {
        this(configuration, model, diagnostics, properties);
        this.mapImage = mapImage;
    }

    public DreameVacuumMqttClient(DreameMqttConfiguration configuration, String model, Consumer<String> diagnostics,
            Consumer<Map<String, Integer>> properties, Consumer<String> mapImage, Consumer<MapListRevision> mapList)
            throws DreameCloudException {
        this(configuration, model, diagnostics, properties, mapImage);
        this.mapList = mapList;
    }

    DreameVacuumMqttClient(DreameMqttConfiguration configuration, String model, Consumer<String> diagnostics,
            Consumer<Map<String, Integer>> properties, MqttBrokerConnection connection) {
        this.configuration = configuration;
        this.model = model;
        this.diagnostics = diagnostics;
        this.properties = properties;
        this.connection = connection;
        connection.addConnectionObserver(this);
    }

    public synchronized void connect() {
        if (closed || started) {
            return;
        }
        started = true;
        connection.start().thenCompose(connected -> {
            synchronized (DreameVacuumMqttClient.this) {
                if (closed || !Boolean.TRUE.equals(connected)) {
                    return CompletableFuture.completedFuture(false);
                }
                return connection.subscribe(configuration.topic(), this);
            }
        }).whenComplete((success, error) -> {
            synchronized (DreameVacuumMqttClient.this) {
                if (closed) {
                    return;
                }
                subscribed = error == null && Boolean.TRUE.equals(success);
            }
            diagnostics.accept(subscribed ? "subscribed (device connectivity not established)"
                    : error == null ? "subscribe-rejected" : "connect-failed");
        });
    }

    @Override
    public void processMessage(String topic, byte[] payload) {
        synchronized (this) {
            if (closed || !configuration.topic().equals(topic)) {
                return;
            }
        }
        diagnostics.accept(DreameVacuumDiagnostics.describeMessage(payload, model));
        Map<String, Integer> updates = DreameVacuumDiagnostics.readProperties(payload, model);
        if (!updates.isEmpty()) {
            properties.accept(updates);
        }
        String image = DreameVacuumMapImage.encodedFromMessage(payload, model);
        if (image != null) {
            mapImage.accept(image);
        }
        MapListRevision revision = DreameVacuumMapImage.mapListRevisionFromMessage(payload, model);
        if (revision != null) {
            mapList.accept(revision);
        }
    }

    @Override
    public void connectionStateChanged(MqttConnectionState state, @Nullable Throwable error) {
        if (state == MqttConnectionState.DISCONNECTED) {
            synchronized (this) {
                subscribed = false;
                if (closed) {
                    return;
                }
            }
            diagnostics.accept("connection-lost");
        }
    }

    public boolean isSubscribed() {
        return subscribed && connection.connectionState() == MqttConnectionState.CONNECTED;
    }

    public void close() {
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
            subscribed = false;
        }
        connection.removeConnectionObserver(this);
        connection.stop();
    }
}
