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
package org.openhab.binding.mqtt.generic.tools;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;

/**
 * Tests that {@link WaitForTopicValue} always unsubscribes exactly once.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class WaitForTopicValueTest {
    private static final String TOPIC = "test/topic";

    private @Mock @NonNullByDefault({}) MqttBrokerConnection connection;
    private @NonNullByDefault({}) ScheduledExecutorService scheduler;

    @BeforeEach
    public void setUp() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        when(connection.subscribe(eq(TOPIC), any())).thenReturn(CompletableFuture.completedFuture(true));
    }

    @AfterEach
    public void tearDown() {
        scheduler.shutdownNow();
    }

    private MqttMessageSubscriber subscriber() {
        ArgumentCaptor<MqttMessageSubscriber> captor = ArgumentCaptor.forClass(MqttMessageSubscriber.class);
        verify(connection).subscribe(eq(TOPIC), captor.capture());
        return captor.getValue();
    }

    private void verifyUnsubscribedOnce(MqttMessageSubscriber subscriber) {
        verify(connection, timeout(1000).times(1)).unsubscribe(TOPIC, subscriber);
    }

    @Test
    public void messageReceived() {
        WaitForTopicValue w = new WaitForTopicValue(connection, TOPIC);
        MqttMessageSubscriber s = subscriber();
        s.processMessage(TOPIC, "hello".getBytes(StandardCharsets.UTF_8));
        assertEquals("hello", w.waitForTopicValue(1000));
        verifyUnsubscribedOnce(s);
    }

    @Test
    public void synchronousTimeout() {
        WaitForTopicValue w = new WaitForTopicValue(connection, TOPIC);
        MqttMessageSubscriber s = subscriber();
        assertNull(w.waitForTopicValue(50));
        verifyUnsubscribedOnce(s);
        // a late message must not trigger a second unsubscription
        s.processMessage(TOPIC, "late".getBytes(StandardCharsets.UTF_8));
        verify(connection, times(1)).unsubscribe(TOPIC, s);
    }

    @Test
    public void asynchronousTimeout() throws Exception {
        WaitForTopicValue w = new WaitForTopicValue(connection, TOPIC);
        MqttMessageSubscriber s = subscriber();
        CompletableFuture<String> f = w.waitForTopicValueAsync(scheduler, 50);
        ExecutionException e = assertThrows(ExecutionException.class, () -> f.get(2, TimeUnit.SECONDS));
        assertInstanceOf(TimeoutException.class, e.getCause());
        verifyUnsubscribedOnce(s);
    }

    @Test
    public void stopUnsubscribes() {
        WaitForTopicValue w = new WaitForTopicValue(connection, TOPIC);
        MqttMessageSubscriber s = subscriber();
        w.stop();
        assertNull(w.waitForTopicValue(50));
        verifyUnsubscribedOnce(s);
    }

    @Test
    public void subscriptionReturnsFalse() {
        when(connection.subscribe(eq(TOPIC), any())).thenReturn(CompletableFuture.completedFuture(false));
        WaitForTopicValue w = new WaitForTopicValue(connection, TOPIC);
        MqttMessageSubscriber s = subscriber();
        assertNull(w.waitForTopicValue(1000));
        verifyUnsubscribedOnce(s);
    }

    @Test
    public void subscriptionFails() {
        when(connection.subscribe(eq(TOPIC), any()))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("boom")));
        WaitForTopicValue w = new WaitForTopicValue(connection, TOPIC);
        MqttMessageSubscriber s = subscriber();
        assertNull(w.waitForTopicValue(1000));
        verifyUnsubscribedOnce(s);
    }

    @Test
    public void interruptionRestoresInterruptFlag() {
        WaitForTopicValue w = new WaitForTopicValue(connection, TOPIC);
        MqttMessageSubscriber s = subscriber();
        Thread.currentThread().interrupt();
        try {
            assertNull(w.waitForTopicValue(1000));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted(); // clear flag for other tests
        }
        verifyUnsubscribedOnce(s);
    }
}
