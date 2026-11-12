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
package org.openhab.binding.tailwind.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tailwind.internal.FakeController;
import org.openhab.binding.tailwind.internal.FakeController.Answer;
import org.openhab.binding.tailwind.internal.FakeController.Received;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.RefreshType;

/**
 * Tests for {@link TailwindHandler}.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public class TailwindHandlerTest {
    private static final int WAIT_MS = 20000;
    private static final ThingUID THING_UID = new ThingUID("tailwind", "iq3", "test");

    private final FakeController controller = new FakeController();
    private final NetworkAddressService networkAddressService = mock(NetworkAddressService.class);
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);

    private TailwindHandler handler = newHandler(Map.of("hostname", "127.0.0.1", "token", "123456"));

    private TailwindHandler newHandler(Map<String, Object> config) {
        List<Channel> channels = new ArrayList<>();
        for (String id : List.of("night-mode", "led-brightness", "rssi", "server-monitor", "command")) {
            channels.add(ChannelBuilder.create(new ChannelUID(THING_UID, "controller", id)).build());
        }
        for (int door = 1; door <= 3; door++) {
            for (String id : List.of("status", "control", "locked", "disabled")) {
                channels.add(ChannelBuilder.create(new ChannelUID(THING_UID, "door" + door, id)).build());
            }
        }
        Thing thing = ThingBuilder.create(new ThingTypeUID("tailwind", "iq3"), THING_UID).withChannels(channels)
                .withConfiguration(new Configuration(config)).build();
        TailwindHandler newHandler = new TailwindHandler(thing, controller.httpClient, networkAddressService);
        newHandler.setCallback(callback);
        return newHandler;
    }

    @BeforeEach
    public void setUp() {
        when(networkAddressService.getPrimaryIpv4HostAddress()).thenReturn("127.0.0.1");
        doAnswer(invocation -> {
            Thing thing = invocation.getArgument(0);
            thing.setStatusInfo(invocation.getArgument(1));
            return null;
        }).when(callback).statusUpdated(any(), any());
    }

    @AfterEach
    public void tearDown() {
        handler.dispose();
    }

    private static ChannelUID channel(String group, String id) {
        return new ChannelUID(THING_UID, group, id);
    }

    private void verifyStatus(ThingStatus status, ThingStatusDetail detail) {
        verify(callback, timeout(WAIT_MS)).statusUpdated(any(),
                argThat(info -> info.getStatus() == status && info.getStatusDetail() == detail));
    }

    private void initializeOnline() {
        handler.initialize();
        verifyStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        // The states of the first status follow the status update, and the registration for reports follows them
        verify(callback, timeout(WAIT_MS)).stateUpdated(eq(channel("door2", "disabled")), any());
        awaitAtLeast("notify_url", 1);
    }

    private void awaitRequests(String name, int count) {
        awaitAtLeast(name, count);
        assertEquals(count, controller.requestsNamed(name).size(), name + " requests");
    }

    private void awaitAtLeast(String name, int count) {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(WAIT_MS);
        while (controller.requestsNamed(name).size() < count && System.nanoTime() < deadline) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        assertTrue(controller.requestsNamed(name).size() >= count, name + " requests");
    }

    private int notificationPort() {
        String url = controller.requestsNamed("notify_url").get(0).value().get("url").getAsString();
        return Integer.parseInt(url.substring(url.indexOf(':') + 1));
    }

    private static void sendReport(int port, String report) throws Exception {
        byte[] data = report.getBytes(StandardCharsets.UTF_8);
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.send(new DatagramPacket(data, data.length, InetAddress.getLoopbackAddress(), port));
        }
    }

    @Test
    public void goesOnlineRegistersForReportsAndPublishesTheStatus() {
        initializeOnline();

        Received notify = controller.requestsNamed("notify_url").get(0);
        assertTrue(notify.value().get("url").getAsString().startsWith("127.0.0.1:"));
        assertEquals("udp", notify.value().get("proto").getAsString());

        verify(callback).stateUpdated(channel("controller", "night-mode"), OnOffType.OFF);
        verify(callback).stateUpdated(channel("controller", "led-brightness"), PercentType.HUNDRED);
        verify(callback).stateUpdated(channel("controller", "rssi"), new QuantityType<>(-31, Units.DECIBEL_MILLIWATTS));
        verify(callback).stateUpdated(channel("controller", "server-monitor"), OnOffType.ON);
        verify(callback).stateUpdated(channel("door1", "status"), new StringType("open"));
        verify(callback).stateUpdated(channel("door1", "control"), new StringType("open"));
        verify(callback).stateUpdated(channel("door2", "status"), new StringType("close"));
        verify(callback).stateUpdated(channel("door2", "locked"), OnOffType.ON);
        verify(callback).stateUpdated(channel("door2", "disabled"), OnOffType.OFF);

        Thing thing = handler.getThing();
        assertEquals("30aea4801880", thing.getProperties().get(Thing.PROPERTY_MAC_ADDRESS));
        assertEquals("10.80", thing.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION));
        assertEquals("iQ3", thing.getProperties().get(Thing.PROPERTY_MODEL_ID));
    }

    @Test
    public void everyStatusRequestRegistersForReportsAgain() {
        initializeOnline();
        int port = notificationPort();

        // For example after the controller rebooted and forgot the address
        handler.poll();

        awaitRequests("notify_url", 2);
        String url = controller.requestsNamed("notify_url").get(1).value().get("url").getAsString();
        assertEquals("127.0.0.1:" + port, url);
    }

    @Test
    public void onlyConnectedDoorsKeepTheirChannels() {
        initializeOnline();

        Thing thing = handler.getThing();
        assertNotNull(thing.getChannel(channel("door1", "status")));
        assertNotNull(thing.getChannel(channel("door2", "status")));
        assertNull(thing.getChannel(channel("door3", "status")));
        assertEquals(5 + 2 * 4, thing.getChannels().size());
    }

    @Test
    public void missingHostnameOrTokenIsAConfigurationError() {
        handler = newHandler(Map.of("token", "123456"));
        handler.initialize();
        verifyStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        assertEquals("@text/offline.conf-error.no-hostname", handler.getThing().getStatusInfo().getDescription());

        handler = newHandler(Map.of("hostname", "127.0.0.1"));
        handler.initialize();
        assertEquals("@text/offline.conf-error.no-token", handler.getThing().getStatusInfo().getDescription());
        assertTrue(controller.requests.isEmpty());
    }

    @Test
    public void rejectedTokenIsAConfigurationError() {
        controller.answerAllWith(400, FakeController.TOKEN_FAIL);

        handler.initialize();

        verifyStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        assertEquals("@text/offline.conf-error.token-rejected", handler.getThing().getStatusInfo().getDescription());
    }

    @Test
    public void unreachableControllerIsACommunicationError() {
        controller.setUnreachable(true);

        handler.initialize();

        verifyStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    public void interruptedStatusRequestLeavesTheStatusAlone() {
        initializeOnline();
        clearInvocations(callback);
        controller.setInterrupted(true);

        handler.poll();

        assertTrue(Thread.interrupted(), "the interruption must be kept");
        verify(callback, never()).statusUpdated(any(), any());
    }

    @Test
    public void missingPrimaryAddressIsAConfigurationError() {
        when(networkAddressService.getPrimaryIpv4HostAddress()).thenReturn(null);

        handler.initialize();

        verifyStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        assertEquals("@text/offline.conf-error.no-primary-address",
                handler.getThing().getStatusInfo().getDescription());
        assertTrue(controller.requestsNamed("notify_url").isEmpty());
    }

    @Test
    public void doorCommandsAreSentWithThePartialOpenTimeOfTheDoor() {
        handler = newHandler(Map.of("hostname", "127.0.0.1", "token", "123456", "door2PartialOpenTime", 4.5));
        initializeOnline();

        handler.handleCommand(channel("door1", "control"), new StringType("close"));
        handler.handleCommand(channel("door2", "control"), new StringType("partial"));

        awaitRequests("door_op", 2);
        List<String> values = controller.requestsNamed("door_op").stream().map(r -> r.value().toString()).sorted()
                .toList();
        assertEquals(List.of("{\"door_idx\":0,\"cmd\":\"close\"}",
                "{\"door_idx\":1,\"cmd\":\"open\",\"partial_time\":4500}"), values);
    }

    @Test
    public void controllerCommandsAreSentAndTheirStatePublished() {
        initializeOnline();

        handler.handleCommand(channel("controller", "led-brightness"), new PercentType(40));
        handler.handleCommand(channel("controller", "server-monitor"), OnOffType.OFF);
        handler.handleCommand(channel("controller", "command"), new StringType("identify"));

        verify(callback, timeout(WAIT_MS)).stateUpdated(channel("controller", "led-brightness"), new PercentType(40));
        verify(callback, timeout(WAIT_MS)).stateUpdated(channel("controller", "server-monitor"), OnOffType.OFF);
        awaitRequests("identify", 1);
        assertEquals("{\"brightness\":40}", controller.requestsNamed("status_led").get(0).value().toString());
        assertEquals("{\"enable\":false}", controller.requestsNamed("server_monitor").get(0).value().toString());
    }

    @Test
    public void statusReportUpdatesTheDoor() throws Exception {
        initializeOnline();

        sendReport(notificationPort(), FakeController.STATUS.replace("\"status\":\"open\"", "\"status\":\"close\"")
                .replace("\"data\"", "\"notify\":{\"door_idx\":0,\"event\":\"close\"},\"data\""));

        verify(callback, timeout(WAIT_MS)).stateUpdated(channel("door1", "status"), new StringType("close"));
    }

    @Test
    public void statusReportsOfOtherDevicesAndMalformedReportsAreIgnored() throws Exception {
        initializeOnline();
        int port = notificationPort();

        sendReport(port, FakeController.STATUS.replace("_30_ae_a4_80_18_80_", "_30_ae_a4_80_18_81_")
                .replace("\"status\":\"open\"", "\"status\":\"close\""));
        sendReport(port, "{\"result\":");
        // A report that is handled, to know the ones before it were processed
        sendReport(port, FakeController.STATUS.replace("\"led_brightness\":100", "\"led_brightness\":10"));

        verify(callback, timeout(WAIT_MS)).stateUpdated(channel("controller", "led-brightness"), new PercentType(10));
        verify(callback, never()).stateUpdated(channel("door1", "status"), new StringType("close"));
    }

    @Test
    public void statusReportsFromOtherAddressesAreIgnored() throws Exception {
        // A controller address that the loopback interface is not
        handler = newHandler(Map.of("hostname", "192.0.2.1", "token", "123456"));
        initializeOnline();

        sendReport(notificationPort(), FakeController.STATUS.replace("\"status\":\"open\"", "\"status\":\"close\""));

        verify(callback, after(1000).never()).stateUpdated(channel("door1", "status"), new StringType("close"));
    }

    @Test
    public void failedRegistrationForReportsKeepsTheThingOnline() {
        controller.answerWith(received -> switch (received.name()) {
            case "dev_st" -> new Answer(200, FakeController.STATUS);
            case "notify_url" -> new Answer(200, "{\"result\":\"Fail\",\"info\":\"not supported\"}");
            default -> new Answer(200, FakeController.OK);
        });

        initializeOnline();
        awaitRequests("notify_url", 1);

        verify(callback, after(500).never()).statusUpdated(any(),
                argThat(info -> info.getStatus() == ThingStatus.OFFLINE));
    }

    @Test
    public void refreshPublishesTheLastStatusWithoutARequest() {
        initializeOnline();
        int requests = controller.requests.size();

        handler.handleCommand(channel("door1", "status"), RefreshType.REFRESH);

        verify(callback, timeout(WAIT_MS).times(2)).stateUpdated(channel("door1", "status"), new StringType("open"));
        assertEquals(requests, controller.requests.size());
    }

    @Test
    public void failedCommandSetsTheThingOffline() {
        initializeOnline();
        controller.setUnreachable(true);

        handler.handleCommand(channel("door1", "control"), new StringType("open"));

        verifyStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
    }

    @Test
    public void disposedHandlerPublishesNothing() throws Exception {
        initializeOnline();
        int port = notificationPort();
        handler.dispose();
        clearInvocations(callback);

        controller.answerWith(received -> new Answer(200, FakeController.OK));
        handler.handleCommand(channel("controller", "led-brightness"), new PercentType(40));
        try {
            sendReport(port, FakeController.STATUS);
        } catch (IOException e) {
            // The port is closed
        }

        verify(callback, after(500).never()).stateUpdated(any(), any());
        assertFalse(controller.requests.stream().anyMatch(r -> "status_led".equals(r.name())));
    }
}
