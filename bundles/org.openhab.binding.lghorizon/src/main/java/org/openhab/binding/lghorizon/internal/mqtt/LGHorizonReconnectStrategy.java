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
package org.openhab.binding.lghorizon.internal.mqtt;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lghorizon.internal.api.LGHorizonApiException;
import org.openhab.binding.lghorizon.internal.api.LGHorizonAuthClient;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.reconnect.AbstractReconnectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The LG Horizon broker authenticates with a short-lived token (see {@link LGHorizonAuthClient#getMqttToken()}) rather
 * than a static password. The stock {@code PeriodicReconnectStrategy} would just keep retrying with the stale password
 * after a disconnect, which will never succeed once the token has expired.
 * <p>
 * This strategy instead fetches a fresh token and calls {@link MqttBrokerConnection#setCredentials(String, String)}
 * immediately before every reconnect attempt, with exponential backoff (5s, 10s, 20s, ... capped at 60s).
 *
 * @author Mark - Initial contribution
 */
@NonNullByDefault
public class LGHorizonReconnectStrategy extends AbstractReconnectStrategy {

    private static final int FIRST_RECONNECT_DELAY_SECONDS = 5;
    private static final int MAX_RECONNECT_DELAY_SECONDS = 60;

    private final Logger logger = LoggerFactory.getLogger(LGHorizonReconnectStrategy.class);

    private final LGHorizonAuthClient authClient;
    private final AtomicInteger attempt = new AtomicInteger();

    private @Nullable ScheduledExecutorService scheduler = null;
    private @Nullable ScheduledFuture<?> scheduledTask;

    public LGHorizonReconnectStrategy(LGHorizonAuthClient authClient) {
        this.authClient = authClient;
    }

    @Override
    public synchronized boolean isReconnecting() {
        ScheduledFuture<?> task = scheduledTask;
        return task != null && !task.isDone();
    }

    @Override
    public synchronized void lostConnection() {
        ScheduledExecutorService scheduler = this.scheduler;
        if (scheduler == null) {
            return;
        }
        if (getBrokerConnection() == null) {
            stop();
            return;
        }
        ScheduledFuture<?> existing = scheduledTask;
        if (existing != null && !existing.isDone()) {
            return;
        }
        int delay = (int) Math.min(FIRST_RECONNECT_DELAY_SECONDS * Math.pow(2, attempt.getAndIncrement()),
                MAX_RECONNECT_DELAY_SECONDS);
        logger.debug("LG Horizon MQTT connection lost, retrying in {}s", delay);
        scheduledTask = scheduler.schedule(this::attemptReconnect, delay, TimeUnit.SECONDS);
    }

    private synchronized void attemptReconnect() {
        MqttBrokerConnection connection = getBrokerConnection();
        if (connection == null || scheduler == null) {
            return;
        }
        try {
            String householdId = authClient.getHouseholdId();
            String freshToken = authClient.getMqttToken();
            if (householdId != null) {
                connection.setCredentials(householdId, freshToken);
            }
        } catch (LGHorizonApiException e) {
            logger.debug("Could not refresh LG Horizon MQTT token before reconnecting: {}", e.getMessage());
        }
        connection.start().exceptionally(e -> {
            logger.debug("LG Horizon MQTT reconnect attempt failed: {}", e.getMessage());
            return false;
        });
    }

    @Override
    public synchronized void connectionEstablished() {
        attempt.set(0);
        ScheduledFuture<?> task = scheduledTask;
        if (task != null) {
            task.cancel(false);
            scheduledTask = null;
        }
    }

    @Override
    public void start() {
        if (scheduler == null) {
            scheduler = Executors.newScheduledThreadPool(1);
        }
    }

    @Override
    public synchronized void stop() {
        ScheduledExecutorService scheduler = this.scheduler;
        if (scheduler != null) {
            scheduler.shutdownNow();
            this.scheduler = null;
        }

        ScheduledFuture<?> task = scheduledTask;
        if (task != null) {
            task.cancel(true);
            scheduledTask = null;
        }
    }
}
