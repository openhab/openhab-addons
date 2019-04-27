/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.generic.tools;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttMessageSubscriber;

/**
 * Waits for a topic value to appear on a MQTT topic. One-time useable only per instance.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class WaitForTopicValue {
    private CompletableFuture<String> future = new CompletableFuture<>();
    private final CompletableFuture<Boolean> subscripeFuture;

    /**
     * Creates an a instance.
     *
     * @param connection A broker connection.
     * @param topic The topic
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public WaitForTopicValue(MqttBrokerConnection connection, String topic)
            throws InterruptedException, ExecutionException {
        final MqttMessageSubscriber mqttMessageSubscriber = (t, payload) -> {
            future.complete(new String(payload));
        };
        future = future.whenComplete((r, e) -> {
            connection.unsubscribe(topic, mqttMessageSubscriber);
        });

        subscripeFuture = connection.subscribe(topic, mqttMessageSubscriber);
    }

    /**
     * Free any resources
     */
    public void stop() {
        future.completeExceptionally(new Exception("Stopped"));
    }

    /**
     * Wait for the value to appear on the MQTT broker.
     *
     * @param timeoutInMS Maximum time in milliseconds to wait for the value.
     * @return Return the value or null if timed out.
     */
    public @Nullable String waitForTopicValue(int timeoutInMS) {
        try {
            return subscripeFuture.thenCompose(b -> future).get(timeoutInMS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return null;
        }
    }

    private void timeout() {
        if (!future.isDone()) {
            future.completeExceptionally(new TimeoutException());
        }
    }

    /**
     * Return a future that completes successfully with a topic value or fails exceptionally with a timeout exception.
     *
     * @param scheduler A scheduler for the timeout
     * @param timeoutInMS The timeout in milliseconds
     * @return The future
     */
    public CompletableFuture<String> waitForTopicValueAsync(ScheduledExecutorService scheduler, int timeoutInMS) {
        scheduler.schedule(this::timeout, timeoutInMS, TimeUnit.MILLISECONDS);
        return subscripeFuture.thenCompose(b -> future);
    }
}
