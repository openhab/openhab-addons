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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.openhab.binding.tapocontrol.internal.TapoControlHandlerFactory.GSON;
import static org.openhab.binding.tapocontrol.internal.constants.TapoComConstants.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.tapocontrol.internal.api.TapoDeviceConnector;
import org.openhab.binding.tapocontrol.internal.devices.dto.TapoBaseDeviceData;
import org.openhab.binding.tapocontrol.internal.devices.dto.TapoChildDeviceData;
import org.openhab.binding.tapocontrol.internal.devices.dto.TapoChildList;
import org.openhab.binding.tapocontrol.internal.devices.wifi.lightswitch.TapoDimmerSwitchData;
import org.openhab.binding.tapocontrol.internal.devices.wifi.socket.TapoSocketData;
import org.openhab.binding.tapocontrol.internal.dto.TapoChildRequest;
import org.openhab.binding.tapocontrol.internal.dto.TapoMultipleRequest;
import org.openhab.binding.tapocontrol.internal.dto.TapoRequest;
import org.openhab.binding.tapocontrol.internal.dto.TapoResponse;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;

import com.google.gson.JsonObject;

/**
 * Tests for {@link KasaXorProtocol}.
 *
 * @author Lee Ballard - Initial contribution
 * @author Christopher Smith - Add Kasa power strip child tests
 */
@NonNullByDefault
class KasaXorProtocolTest {
    // Based on python-kasa fixture EP40(US)_1.0_1.0.2.json
    private static final String EP40_SYSINFO = """
            {"system":{"get_sysinfo":{"alias":"Deck","child_num":2,"children":[
            {"alias":"Deck 1","id":"8006ECEB66F1C56B848E40B81F70EC9F1EDAD09900","next_action":{"type":-1},
            "on_time":120,"state":1},
            {"alias":"Deck 2","id":"8006ECEB66F1C56B848E40B81F70EC9F1EDAD09901","next_action":{"type":-1},
            "on_time":0,"state":0}],
            "deviceId":"8006ECEB66F1C56B848E40B81F70EC9F1EDAD099","err_code":0,"hw_ver":"1.0","led_off":0,
            "mac":"00:00:00:00:00:00","mic_type":"IOT.SMARTPLUGSWITCH","model":"EP40(US)","rssi":-47,
            "sw_ver":"1.0.4 Build 240305 Rel.111944"}}}
            """;

    @Test
    void mapsPowerStripChildrenToChildDeviceList() throws TapoErrorHandler {
        TapoResponse response = KasaXorProtocol.mapChildList(EP40_SYSINFO);

        assertThat(response.hasError(), is(false));
        assertThat(response.method(), is(DEVICE_CMD_GETCHILDDEVICELIST));
        TapoChildList childList = Objects.requireNonNull(GSON.fromJson(response.result(), TapoChildList.class));
        List<TapoChildDeviceData> children = childList.getChildDeviceList();
        assertThat(children, hasSize(2));
        assertThat(children.get(0).getPosition(), is(1));
        assertThat(children.get(0).getDeviceId(), is("8006ECEB66F1C56B848E40B81F70EC9F1EDAD09900"));
        assertThat(children.get(0).isOn(), is(true));
        assertThat(children.get(1).getPosition(), is(2));
        assertThat(children.get(1).isOn(), is(false));
    }

    @Test
    void mapsDeviceWithoutChildrenToEmptyChildDeviceList() throws TapoErrorHandler {
        String sysinfo = "{\"system\":{\"get_sysinfo\":{\"err_code\":0,\"model\":\"EP10(US)\",\"relay_state\":1}}}";

        TapoChildList childList = Objects
                .requireNonNull(GSON.fromJson(KasaXorProtocol.mapChildList(sysinfo).result(), TapoChildList.class));

        assertThat(childList.getChildDeviceList(), hasSize(0));
    }

    @Test
    void keepsOnTimeAndBrightnessForSocketAndDimmerHandlers() throws TapoErrorHandler {
        // Based on python-kasa fixture KP405(US)_1.0_1.0.6.json
        String kp405 = """
                {"system":{"get_sysinfo":{"err_code":0,"model":"KP405(US)","alias":"Deck Edison",
                "relay_state":1,"brightness":70,"on_time":3600,"rssi":-49,"sw_ver":"1.0.6 Build 240229 Rel.174151"}}}
                """;
        String ep10 = """
                {"system":{"get_sysinfo":{"err_code":0,"model":"EP10(US)","relay_state":1,"on_time":120,"rssi":-46}}}
                """;

        TapoDimmerSwitchData dimmer = Objects
                .requireNonNull(GSON.fromJson(KasaXorProtocol.mapSysinfo(kp405).result(), TapoDimmerSwitchData.class));
        TapoSocketData socket = Objects
                .requireNonNull(GSON.fromJson(KasaXorProtocol.mapSysinfo(ep10).result(), TapoSocketData.class));

        assertThat(dimmer.isOn(), is(true));
        assertThat(dimmer.getBrightness(), is(70));
        assertThat(dimmer.getOnTime().intValue(), is(3600));
        assertThat(socket.isOn(), is(true));
        assertThat(socket.getOnTime().intValue(), is(120));
    }

    @Test
    void mapsRssiToSignalStrengthLevel() throws TapoErrorHandler {
        TapoBaseDeviceData data = Objects.requireNonNull(
                GSON.fromJson(KasaXorProtocol.mapSysinfo(EP40_SYSINFO).result(), TapoBaseDeviceData.class));

        assertThat(data.getSignalLevel(), is(4));
        assertThat(KasaXorProtocol.signalLevel(-55), is(3));
        assertThat(KasaXorProtocol.signalLevel(-65), is(2));
        assertThat(KasaXorProtocol.signalLevel(-80), is(1));
        assertThat(KasaXorProtocol.signalLevel(-90), is(0));
        assertThat(KasaXorProtocol.signalLevel(0), is(0));
    }

    @Test
    void keepsPlainAliasThatLooksLikeBase64() throws TapoErrorHandler {
        String sysinfo = "{\"system\":{\"get_sysinfo\":{\"err_code\":0,\"alias\":\"Shed\",\"children\":["
                + "{\"id\":\"C1\",\"alias\":\"Pool\",\"state\":1}]}}}";

        TapoBaseDeviceData device = Objects
                .requireNonNull(GSON.fromJson(KasaXorProtocol.mapSysinfo(sysinfo).result(), TapoBaseDeviceData.class));
        TapoChildList children = Objects
                .requireNonNull(GSON.fromJson(KasaXorProtocol.mapChildList(sysinfo).result(), TapoChildList.class));

        assertThat(device.getNickname(), is("Shed"));
        assertThat(children.getChildDeviceList().get(0).getNickname(), is("Pool"));
    }

    @Test
    void readsSysinfoOnceForDeviceInfoAndChildListMultiRequest() throws Exception {
        TapoDeviceConnector connector = mock(TapoDeviceConnector.class);
        doAnswer(invocation -> {
            invocation.getArgument(0, Runnable.class).run();
            return null;
        }).when(connector).executeAsync(any(Runnable.class));
        AtomicInteger connections = new AtomicInteger();
        ExecutorService serverExecutor = Executors.newSingleThreadExecutor();

        try (ServerSocket serverSocket = new ServerSocket(0)) {
            serverExecutor.submit(() -> {
                while (!serverSocket.isClosed()) {
                    try (Socket socket = serverSocket.accept();
                            DataInputStream input = new DataInputStream(socket.getInputStream());
                            DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
                        connections.incrementAndGet();
                        input.readFully(new byte[input.readInt()]);
                        byte[] encrypted = KasaXorProtocol.encrypt(EP40_SYSINFO);
                        output.writeInt(encrypted.length);
                        output.write(encrypted);
                    } catch (IOException e) {
                        // server socket closed at the end of the test
                    }
                }
            });
            KasaXorProtocol protocol = new KasaXorProtocol(connector, "127.0.0.1", false, serverSocket.getLocalPort());
            try {
                protocol.login(new TapoCredentials());

                protocol.sendAsyncRequest(new TapoMultipleRequest(new TapoRequest(DEVICE_CMD_GETINFO),
                        new TapoRequest(DEVICE_CMD_GETCHILDDEVICELIST)));

                ArgumentCaptor<TapoResponse> response = ArgumentCaptor.forClass(TapoResponse.class);
                verify(connector).handleResponse(response.capture(), eq(DEVICE_CMD_MULTIPLE_REQ));
                assertThat(response.getValue().responses(), hasSize(2));
                assertThat(connections.get(), is(1));
            } finally {
                protocol.logout();
            }
        } finally {
            serverExecutor.shutdownNow();
        }
    }

    @Test
    void buildsChildRelayCommand() {
        assertThat(KasaXorProtocol.buildChildRelayCommand("CHILD01", true),
                is("{\"context\":{\"child_ids\":[\"CHILD01\"]},\"system\":{\"set_relay_state\":{\"state\":1}}}"));
    }

    @Test
    void sendsControlChildAsKasaRelayCommandWithChildContext() throws Exception {
        TapoDeviceConnector connector = mock(TapoDeviceConnector.class);
        CountDownLatch responseHandled = new CountDownLatch(1);
        doAnswer(invocation -> {
            invocation.getArgument(0, Runnable.class).run();
            return null;
        }).when(connector).executeAsync(any(Runnable.class));
        doAnswer(invocation -> {
            responseHandled.countDown();
            return null;
        }).when(connector).handleResponse(any(), anyString());
        ExecutorService serverExecutor = Executors.newSingleThreadExecutor();

        try (ServerSocket serverSocket = new ServerSocket(0)) {
            CompletableFuture<String> received = CompletableFuture.supplyAsync(
                    () -> respondOnce(serverSocket, "{\"system\":{\"set_relay_state\":{\"err_code\":0}}}"),
                    serverExecutor);
            KasaXorProtocol protocol = new KasaXorProtocol(connector, "127.0.0.1", false, serverSocket.getLocalPort());
            try {
                TapoChildDeviceData child = Objects.requireNonNull(
                        GSON.fromJson("{\"device_id\":\"8006ECEB66F1C56B848E40B81F70EC9F1EDAD09901\",\"position\":2}",
                                TapoChildDeviceData.class));
                child.setDeviceOn(true);
                protocol.login(new TapoCredentials());

                protocol.sendAsyncRequest(new TapoChildRequest(child));

                assertThat(received.get(2, TimeUnit.SECONDS), is(
                        "{\"context\":{\"child_ids\":[\"8006ECEB66F1C56B848E40B81F70EC9F1EDAD09901\"]},\"system\":{\"set_relay_state\":{\"state\":1}}}"));
                assertTrue(responseHandled.await(2, TimeUnit.SECONDS));
                verify(connector).handleResponse(any(), eq(DEVICE_CMD_CONTROL_CHILD));
                verify(connector, never()).handleError(any());
            } finally {
                protocol.logout();
            }
        } finally {
            serverExecutor.shutdownNow();
        }
    }

    private static String respondOnce(ServerSocket serverSocket, String response) {
        try (Socket socket = serverSocket.accept();
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
            byte[] request = new byte[input.readInt()];
            input.readFully(request);
            byte[] encrypted = KasaXorProtocol.encrypt(response);
            output.writeInt(encrypted.length);
            output.write(encrypted);
            output.flush();
            return KasaXorProtocol.decrypt(request);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

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
