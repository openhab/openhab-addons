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
package org.openhab.binding.tapocontrol.internal.api.protocol.kasa;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.openhab.binding.tapocontrol.internal.constants.TapoComConstants.DEVICE_CMD_GETINFO;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tapocontrol.internal.api.TapoDeviceConnector;
import org.openhab.binding.tapocontrol.internal.dto.TapoRequest;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;

import com.google.gson.JsonObject;

/**
 * Tests for {@link KasaXorProtocol}.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
class KasaXorProtocolTest {

    @Test
    void roundTripsUtf8Payload() {
        String payload = "{\"system\":{\"get_sysinfo\":{}}}";

        assertThat(KasaXorProtocol.decrypt(KasaXorProtocol.encrypt(payload)), is(payload));
    }

    @Test
    void buildsDimmerBrightnessAndOnCommands() {
        JsonObject values = new JsonObject();
        values.addProperty("brightness", 42);
        values.addProperty("device_on", true);

        List<String> commands = KasaXorProtocol.buildSetCommands(values, true);

        assertThat(commands, contains("{\"smartlife.iot.dimmer\":{\"set_brightness\":{\"brightness\":42}}}",
                "{\"smartlife.iot.dimmer\":{\"set_switch_state\":{\"state\":1}}}"));
    }

    @Test
    void buildsDimmerOffWithoutZeroBrightness() {
        JsonObject values = new JsonObject();
        values.addProperty("device_on", false);

        assertThat(KasaXorProtocol.buildSetCommands(values, true),
                contains("{\"smartlife.iot.dimmer\":{\"set_switch_state\":{\"state\":0}}}"));
    }

    @Test
    void buildsLegacySwitchRelayCommand() {
        JsonObject values = new JsonObject();
        values.addProperty("device_on", true);

        assertThat(KasaXorProtocol.buildSetCommands(values, false),
                contains("{\"system\":{\"set_relay_state\":{\"state\":1}}}"));
    }

    @Test
    void logoutInvalidatesRunningAndQueuedRequests() throws Exception {
        TapoDeviceConnector connector = mock(TapoDeviceConnector.class);
        CountDownLatch tasksStarted = new CountDownLatch(2);
        CountDownLatch requestAccepted = new CountDownLatch(1);
        ExecutorService requestExecutor = Executors.newFixedThreadPool(2);
        ExecutorService serverExecutor = Executors.newSingleThreadExecutor();

        try (ServerSocket serverSocket = new ServerSocket(0)) {
            serverExecutor.submit(() -> acceptWithoutResponding(serverSocket, requestAccepted));
            doAnswer(invocation -> {
                Runnable task = invocation.getArgument(0, Runnable.class);
                requestExecutor.submit(() -> {
                    tasksStarted.countDown();
                    task.run();
                });
                return null;
            }).when(connector).executeAsync(any(Runnable.class));

            KasaXorProtocol protocol = new KasaXorProtocol(connector, "127.0.0.1", true, serverSocket.getLocalPort());
            try {
                protocol.login(new TapoCredentials());
                protocol.sendAsyncRequest(new TapoRequest(DEVICE_CMD_GETINFO));
                assertTrue(requestAccepted.await(2, TimeUnit.SECONDS));

                protocol.sendAsyncRequest(new TapoRequest(DEVICE_CMD_GETINFO));
                assertTrue(tasksStarted.await(2, TimeUnit.SECONDS));
                protocol.logout();

                requestExecutor.shutdown();
                assertTrue(requestExecutor.awaitTermination(2, TimeUnit.SECONDS));
                verify(connector, never()).handleResponse(any(), anyString());
                verify(connector, never()).handleError(any());
            } finally {
                protocol.logout();
            }
        } finally {
            requestExecutor.shutdownNow();
            serverExecutor.shutdownNow();
        }
    }

    private static void acceptWithoutResponding(ServerSocket serverSocket, CountDownLatch requestAccepted) {
        try (Socket socket = serverSocket.accept()) {
            requestAccepted.countDown();
            socket.getInputStream().transferTo(OutputStream.nullOutputStream());
        } catch (IOException e) {
            // The client or server socket is closed during test cleanup.
        }
    }
}
