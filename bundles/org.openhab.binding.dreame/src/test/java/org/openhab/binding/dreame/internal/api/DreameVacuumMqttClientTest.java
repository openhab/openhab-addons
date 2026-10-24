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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.dreame.internal.model.DreameMqttConfiguration;

/**
 * Verifies subscription-only behavior, callback shutdown and safe connection diagnostics.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
class DreameVacuumMqttClientTest {
    @Test
    void subscribesWithoutPublishingAndDisablesStaleCredentialReconnect() throws Exception {
        MqttAsyncClient transport = Objects.requireNonNull(mock(MqttAsyncClient.class));
        List<String> diagnostics = new ArrayList<>();
        DreameVacuumMqttClient client = new DreameVacuumMqttClient(configuration(), "dreame.vacuum.r9445d",
                diagnostics::add, properties -> {
                }, transport);
        client.connect();
        client.connect();
        var options = ArgumentCaptor.forClass(MqttConnectOptions.class);
        var connected = ArgumentCaptor.forClass(IMqttActionListener.class);
        verify(transport).connect(options.capture(), isNull(), connected.capture());
        assertFalse(Objects.requireNonNull(options.getValue()).isAutomaticReconnect());
        assertTrue(Objects.requireNonNull(options.getValue()).isHttpsHostnameVerificationEnabled());
        Objects.requireNonNull(connected.getValue()).onSuccess(null);
        var subscribed = ArgumentCaptor.forClass(IMqttActionListener.class);
        verify(transport).subscribe(eq("device/topic"), eq(0), isNull(), subscribed.capture());
        IMqttToken token = Objects.requireNonNull(mock(IMqttToken.class));
        when(token.getGrantedQos()).thenReturn(new int[] { 0 });
        Objects.requireNonNull(subscribed.getValue()).onSuccess(token);
        assertEquals(List.of("subscribed (device connectivity not established)"), diagnostics);
        client.close();
        client.close();
        Objects.requireNonNull(subscribed.getValue()).onSuccess(token);
        assertEquals(1, diagnostics.size());
        verify(transport).setCallback(client);
        verify(transport).disconnectForcibly(0, 1000, false);
        verify(transport).close(true);
        verifyNoMoreInteractions(transport);
    }

    @Test
    void lateConnectionCompletionDoesNotSubscribeAfterClose() throws Exception {
        MqttAsyncClient transport = Objects.requireNonNull(mock(MqttAsyncClient.class));
        List<String> diagnostics = new ArrayList<>();
        DreameVacuumMqttClient client = new DreameVacuumMqttClient(configuration(), "dreame.vacuum.r9445d",
                diagnostics::add, properties -> {
                }, transport);
        client.connect();
        var connected = ArgumentCaptor.forClass(IMqttActionListener.class);
        verify(transport).connect(any(MqttConnectOptions.class), isNull(), connected.capture());
        client.close();
        Objects.requireNonNull(connected.getValue()).onSuccess(null);
        Objects.requireNonNull(connected.getValue()).onFailure(null, new RuntimeException("secret"));
        client.messageArrived("device/topic", new MqttMessage("secret".getBytes(StandardCharsets.UTF_8)));
        assertTrue(diagnostics.isEmpty());
        verify(transport).setCallback(client);
        verify(transport).disconnectForcibly(0, 1000, false);
        verify(transport).close(true);
        verifyNoMoreInteractions(transport);
    }

    @Test
    void brokerRejectionAndDisconnectNeverExposeExceptionText() throws Exception {
        MqttAsyncClient transport = Objects.requireNonNull(mock(MqttAsyncClient.class));
        List<String> diagnostics = new ArrayList<>();
        DreameVacuumMqttClient client = new DreameVacuumMqttClient(configuration(), "dreame.vacuum.r9445d",
                diagnostics::add, properties -> {
                }, transport);
        client.connect();
        var connected = ArgumentCaptor.forClass(IMqttActionListener.class);
        verify(transport).connect(any(MqttConnectOptions.class), isNull(), connected.capture());
        Objects.requireNonNull(connected.getValue()).onFailure(null, new RuntimeException("private-host secret-token"));
        Objects.requireNonNull(connected.getValue()).onSuccess(null);
        var subscribed = ArgumentCaptor.forClass(IMqttActionListener.class);
        verify(transport).subscribe(eq("device/topic"), eq(0), isNull(), subscribed.capture());
        IMqttToken token = Objects.requireNonNull(mock(IMqttToken.class));
        when(token.getGrantedQos()).thenReturn(new int[] { 128 });
        Objects.requireNonNull(subscribed.getValue()).onSuccess(token);
        client.connectionLost(new RuntimeException("private-owner"));
        assertFalse(client.isSubscribed());
        assertEquals(List.of("connect-failed", "subscribe-rejected", "connection-lost"), diagnostics);
        client.close();
    }

    @Test
    void ignoresMessagesFromOtherTopics() {
        MqttAsyncClient transport = Objects.requireNonNull(mock(MqttAsyncClient.class));
        List<String> diagnostics = new ArrayList<>();
        DreameVacuumMqttClient client = new DreameVacuumMqttClient(configuration(), "dreame.vacuum.r9445d",
                diagnostics::add, properties -> {
                }, transport);
        client.messageArrived("another/device", new MqttMessage("private-data".getBytes(StandardCharsets.UTF_8)));
        assertTrue(diagnostics.isEmpty());
        client.close();
    }

    @Test
    void rejectsWildcardSubscriptions() {
        DreameMqttConfiguration configuration = new DreameMqttConfiguration("host", 8883, "client", "user", "secret",
                "/status/+/owner/#");
        assertThrows(IllegalArgumentException.class, () -> DreameVacuumMqttClient.serverUri(configuration));
    }

    @Test
    void forwardsValidatedPropertiesOnlyForMatchingTopicBeforeClose() {
        MqttAsyncClient transport = Objects.requireNonNull(mock(MqttAsyncClient.class));
        List<Map<String, Integer>> received = new ArrayList<>();
        DreameVacuumMqttClient client = new DreameVacuumMqttClient(configuration(), "dreame.vacuum.r9445d",
                diagnostic -> {
                }, received::add, transport);
        MqttMessage message = new MqttMessage("""
                {"method":"properties_changed","params":[{"siid":3,"piid":1,"value":96}]}
                """.getBytes(StandardCharsets.UTF_8));
        client.messageArrived("another/device", message);
        assertTrue(received.isEmpty());
        client.messageArrived("device/topic", message);
        assertEquals(List.of(Map.of("3/1", 96)), received);
        client.close();
        client.messageArrived("device/topic", message);
        assertEquals(1, received.size());
    }

    private static DreameMqttConfiguration configuration() {
        return new DreameMqttConfiguration("host", 8883, "client", "user", "secret", "device/topic");
    }
}
