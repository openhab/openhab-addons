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
package org.openhab.binding.keba.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.keba.internal.handler.KeContactCombinedHandler.Protocol;
import org.openhab.binding.keba.internal.handler.rest.KeContactRestHandler;
import org.openhab.binding.keba.internal.handler.udp.KeContactHandler;
import org.openhab.binding.keba.internal.handler.udp.KeContactTransceiver;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.modbus.AsyncModbusFailure;
import org.openhab.core.io.transport.modbus.AsyncModbusReadResult;
import org.openhab.core.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.core.io.transport.modbus.ModbusFailureCallback;
import org.openhab.core.io.transport.modbus.ModbusManager;
import org.openhab.core.io.transport.modbus.ModbusReadCallback;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.io.transport.modbus.ModbusWriteRegisterRequestBlueprint;
import org.openhab.core.io.transport.modbus.PollTask;
import org.openhab.core.io.transport.modbus.endpoint.ModbusTCPSlaveEndpoint;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests source ownership, reduced polling and the combined Thing's lifecycle.
 *
 * @author Michael Weger - Initial contribution
 */
class KeContactCombinedHandlerTest {

    @Test
    void removesCompatibilityAliasesFromExistingThings() {
        for (String prefix : List.of("", "modbus#")) {
            ThingUID uid = new ThingUID("keba:kecontact:removedaliases");
            var channels = new ArrayList<org.openhab.core.thing.Channel>();
            for (String alias : List.of("maxsystemcurrent", "maxpilotcurrent", "failsafecurrent", "failsafetimeout")) {
                channels.add(
                        ChannelBuilder
                                .create(new ChannelUID(uid, prefix + alias),
                                        "failsafetimeout".equals(alias) ? "Number:Time" : "Number:ElectricCurrent")
                                .build());
            }
            ChannelUID canonical = new ChannelUID(uid, prefix + "maxsupportedcurrent");
            channels.add(ChannelBuilder.create(canonical, "Number:ElectricCurrent").build());
            Thing thing = ThingBuilder.create(new ThingTypeUID("keba", "kecontact"), uid).withChannels(channels)
                    .build();
            KeContactCombinedHandler handler = new KeContactCombinedHandler(thing,
                    Objects.requireNonNull(mock(ModbusManager.class)),
                    Objects.requireNonNull(mock(KeContactTransceiver.class)));
            handler.setCallback(Objects.requireNonNull(mock(ThingHandlerCallback.class)));
            handler.updateProperties(Map.of("modbusAvailable", "unknown"));
            assertEquals(1, handler.getThing().getChannels().size());
            assertNotNull(handler.getThing().getChannel(canonical));
        }
    }

    @Test
    void p30ModelRemovesOnlyP40ChannelsForGroupedAndFlatThings() {
        for (Map<String, String> detection : List.of(Map.of("model", "KeContact P30 x-series"),
                Map.of("model", "Generic wallbox", "modbusModel", "P30"))) {
            for (String prefix : List.of("", "modbus#")) {
                ThingUID uid = new ThingUID("keba:kecontact:p30model");
                ChannelUID power = new ChannelUID(uid, prefix + "power");
                ChannelUID fastStatus = new ChannelUID(uid, prefix + "fastchargingstatus");
                ChannelUID fastCommand = new ChannelUID(uid, prefix + "activatefastcharging");
                Thing thing = ThingBuilder.create(new ThingTypeUID("keba", "kecontact"), uid)
                        .withChannels(ChannelBuilder.create(power, "Number:Power").build(),
                                ChannelBuilder.create(fastStatus, "Number").build(),
                                ChannelBuilder.create(fastCommand, "Switch").build())
                        .build();
                KeContactCombinedHandler handler = new KeContactCombinedHandler(thing,
                        Objects.requireNonNull(mock(ModbusManager.class)),
                        Objects.requireNonNull(mock(KeContactTransceiver.class)));
                ThingHandlerCallback callback = Objects.requireNonNull(mock(ThingHandlerCallback.class));
                handler.setCallback(callback);
                handler.updateProperties(Map.of("model", "Unknown"));
                assertNotNull(handler.getThing().getChannel(fastStatus));
                handler.updateProperties(Map.of("model", "P40"));
                assertNotNull(handler.getThing().getChannel(fastCommand));
                handler.updateProperties(detection);
                assertNull(handler.getThing().getChannel(fastStatus));
                assertNull(handler.getThing().getChannel(fastCommand));
                assertNotNull(handler.getThing().getChannel(power));
                clearInvocations(callback);
                handler.updateProperties(detection);
                verifyNoInteractions(callback);
            }
        }
    }

    @Test
    void metadataGroupsMatchPreferredSources() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        Map<String, String> groups = Map.of("kecontact-modbus", "modbus", "kecontact-udp", "udp", "kecontact-rest",
                "rest");
        Map<String, List<String>> sessionBlocks = Map.of("kecontact-modbus",
                List.of("sessionconsumption", "sessionrfidtag", "setenergylimit"), "kecontact-udp",
                List.of("authreq", "authenticate", "sessionid", "sessionrfidclass"), "kecontact-rest",
                List.of("session", "sessionstart", "sessionduration", "start", "stop"));
        try (InputStream source = Objects
                .requireNonNull(getClass().getResourceAsStream("/OH-INF/thing/kecontact.xml"))) {
            NodeList groupTypes = factory.newDocumentBuilder().parse(source).getElementsByTagName("channel-group-type");
            assertEquals(3, groupTypes.getLength());
            Set<String> channels = new HashSet<>();
            for (int groupIndex = 0; groupIndex < groupTypes.getLength(); groupIndex++) {
                Element group = (Element) groupTypes.item(groupIndex);
                String expectedGroup = groups.get(group.getAttribute("id"));
                assertNotNull(expectedGroup);
                NodeList groupChannels = group.getElementsByTagName("channel");
                List<String> orderedChannels = new ArrayList<>();
                for (int channelIndex = 0; channelIndex < groupChannels.getLength(); channelIndex++) {
                    Element definition = (Element) groupChannels.item(channelIndex);
                    String channel = definition.getAttribute("id");
                    assertTrue(channels.add(channel), "Duplicate channel: " + channel);
                    assertEquals(expectedGroup, KeContactCombinedHandler.channelGroup(channel), channel);
                    NodeList descriptions = definition.getElementsByTagName("description");
                    assertEquals(1, descriptions.getLength(), "Missing specific description: " + channel);
                    assertTrue(descriptions.item(0).getTextContent().strip().length() > 20, channel);
                    orderedChannels.add(channel);
                }
                List<String> sessionBlock = sessionBlocks.get(group.getAttribute("id"));
                assertNotNull(sessionBlock);
                int start = orderedChannels.indexOf(sessionBlock.get(0));
                assertTrue(start >= 0);
                assertEquals(sessionBlock, orderedChannels.subList(start, start + sessionBlock.size()));
            }
            assertEquals(71, channels.size());
            assertFalse(channels.contains("maxsystemcurrent"));
            assertFalse(channels.contains("maxpilotcurrent"));
            assertFalse(channels.contains("failsafecurrent"));
            assertFalse(channels.contains("failsafetimeout"));
        }
    }

    @Test
    void publishesAndChecksLinksInUdpAndRestGroups() {
        ThingUID uid = new ThingUID("keba:kecontact:groups");
        ChannelUID display = new ChannelUID(uid, "udp#display");
        ChannelUID temperature = new ChannelUID(uid, "rest#temperature");
        Thing thing = ThingBuilder.create(new ThingTypeUID("keba", "kecontact"), uid)
                .withChannels(ChannelBuilder.create(display, "String").build(),
                        ChannelBuilder.create(temperature, "Number:Temperature").build())
                .build();
        KeContactCombinedHandler handler = new KeContactCombinedHandler(thing,
                Objects.requireNonNull(mock(ModbusManager.class)),
                Objects.requireNonNull(mock(KeContactTransceiver.class)));
        ThingHandlerCallback callback = Objects.requireNonNull(mock(ThingHandlerCallback.class));
        handler.setCallback(callback);
        when(callback.isChannelLinked(display)).thenReturn(true);
        when(callback.isChannelLinked(temperature)).thenReturn(true);
        handler.updateState("display", new StringType("Ready"));
        handler.updateState("temperature", new QuantityType<>(23, SIUnits.CELSIUS));
        verify(callback).stateUpdated(display, new StringType("Ready"));
        verify(callback).stateUpdated(temperature, new QuantityType<>(23, SIUnits.CELSIUS));
        assertTrue(handler.isLinked(new ChannelUID(uid, "display")));
        assertTrue(handler.isLinked(new ChannelUID(uid, "temperature")));
    }

    @Test
    @Timeout(80)
    void udpInputDoesNotOverwriteRestX2Active() throws Exception {
        for (boolean legacy : List.of(false, true)) {
            ThingUID uid = new ThingUID("keba:kecontact:inputs");
            ChannelUID x1 = new ChannelUID(uid, legacy ? "input" : "udp#inputx1");
            ChannelUID restInput = new ChannelUID(uid, "rest#input");
            Thing thing = ThingBuilder.create(new ThingTypeUID("keba", "kecontact"), uid)
                    .withConfiguration(new Configuration(Map.of("ipAddress", "192.0.2.1")))
                    .withChannels(legacy ? List.of(ChannelBuilder.create(x1, "Switch").build())
                            : List.of(ChannelBuilder.create(x1, "Switch").build(),
                                    ChannelBuilder.create(restInput, "Switch").build()))
                    .build();
            ModbusManager manager = Objects.requireNonNull(mock(ModbusManager.class));
            ModbusCommunicationInterface comms = Objects.requireNonNull(mock(ModbusCommunicationInterface.class));
            when(manager.newModbusCommunicationInterface(any(), any())).thenReturn(comms);
            when(comms.registerRegularPoll(any(), anyLong(), anyLong(), any(), any()))
                    .thenReturn(Objects.requireNonNull(mock(PollTask.class)));
            when(comms.submitOneTimePoll(any(), any(), any())).thenAnswer(invocation -> {
                ModbusReadRequestBlueprint request = invocation.getArgument(0);
                if (request.getReference() == 1016) {
                    ModbusReadCallback callback = invocation.getArgument(1);
                    callback.handle(new AsyncModbusReadResult(request,
                            new ModbusRegisterArray(304111 >>> 16, 304111 & 0xffff)));
                }
                return java.util.concurrent.CompletableFuture.completedFuture(null);
            });
            KeContactTransceiver transceiver = new KeContactTransceiver() {
                @Override
                public void registerHandler(KeContactHandler handler) {
                }

                @Override
                protected @Nullable ByteBuffer send(String message, KeContactHandler handler) {
                    String reply = switch (message) {
                        case "report 1" -> "{\"ID\":1,\"Product\":\"P30\"}";
                        case "report 2" -> "{\"ID\":2,\"State\":3,\"Input\":1}";
                        default -> null;
                    };
                    return reply == null ? null : ByteBuffer.wrap(reply.getBytes(StandardCharsets.US_ASCII));
                }
            };
            ThingHandlerCallback callback = Objects.requireNonNull(mock(ThingHandlerCallback.class));
            when(callback.isChannelLinked(x1)).thenReturn(true);
            CountDownLatch inputReceived = new CountDownLatch(1);
            doAnswer(invocation -> {
                inputReceived.countDown();
                return null;
            }).when(callback).stateUpdated(x1, OnOffType.ON);
            KeContactCombinedHandler handler = new KeContactCombinedHandler(thing, manager, transceiver);
            handler.setCallback(callback);
            try {
                handler.initialize();
                assertTrue(inputReceived.await(30, TimeUnit.SECONDS));
                verify(callback).stateUpdated(x1, OnOffType.ON);
                verify(callback, never()).stateUpdated(eq(restInput), any());
            } finally {
                handler.dispose();
            }
        }
    }

    @Test
    @Timeout(40)
    void udpFallbackPublishesCanonicalChannelsWithoutAliases() throws Exception {
        ThingUID uid = new ThingUID("keba:kecontact:canonicalfallback");
        var channels = new ArrayList<org.openhab.core.thing.Channel>();
        for (String channel : List.of("maxsupportedcurrent", "maxchargingcurrent", "failsafecurrentsetting")) {
            channels.add(
                    ChannelBuilder.create(new ChannelUID(uid, "modbus#" + channel), "Number:ElectricCurrent").build());
        }
        ChannelUID timeout = new ChannelUID(uid, "modbus#failsafetimeoutsetting");
        channels.add(ChannelBuilder.create(timeout, "Number:Time").build());
        Thing thing = ThingBuilder.create(new ThingTypeUID("keba", "kecontact"), uid)
                .withConfiguration(new Configuration(Map.of("ipAddress", "192.0.2.1"))).withChannels(channels).build();
        ModbusManager manager = Objects.requireNonNull(mock(ModbusManager.class));
        when(manager.newModbusCommunicationInterface(any(), any()))
                .thenReturn(Objects.requireNonNull(mock(ModbusCommunicationInterface.class)));
        KeContactTransceiver transceiver = new KeContactTransceiver() {
            @Override
            public void registerHandler(KeContactHandler handler) {
            }

            @Override
            protected @Nullable ByteBuffer send(String message, KeContactHandler handler) {
                String reply = switch (message) {
                    case "report 1" -> "{\"ID\":1,\"Product\":\"P30\"}";
                    case "report 2" ->
                        "{\"ID\":2,\"State\":3,\"Curr HW\":32000,\"Max curr\":16000,\"Curr FS\":6000,\"Tmo FS\":60}";
                    default -> null;
                };
                return reply == null ? null : ByteBuffer.wrap(reply.getBytes(StandardCharsets.US_ASCII));
            }
        };
        ThingHandlerCallback callback = Objects.requireNonNull(mock(ThingHandlerCallback.class));
        CountDownLatch received = new CountDownLatch(1);
        doAnswer(invocation -> {
            received.countDown();
            return null;
        }).when(callback).stateUpdated(timeout, new QuantityType<>(60, Units.SECOND));
        KeContactCombinedHandler handler = new KeContactCombinedHandler(thing, manager, transceiver);
        handler.setCallback(callback);
        try {
            handler.initialize();
            assertTrue(received.await(30, TimeUnit.SECONDS));
            verify(callback).stateUpdated(new ChannelUID(uid, "modbus#maxsupportedcurrent"),
                    new QuantityType<>(32, Units.AMPERE));
            verify(callback).stateUpdated(new ChannelUID(uid, "modbus#maxchargingcurrent"),
                    new QuantityType<>(16, Units.AMPERE));
            verify(callback).stateUpdated(new ChannelUID(uid, "modbus#failsafecurrentsetting"),
                    new QuantityType<>(6, Units.AMPERE));
            for (String alias : List.of("maxsystemcurrent", "maxpilotcurrent", "failsafecurrent", "failsafetimeout")) {
                verify(callback, never()).stateUpdated(eq(new ChannelUID(uid, "modbus#" + alias)), any());
                verify(callback, never()).stateUpdated(eq(new ChannelUID(uid, alias)), any());
            }
        } finally {
            handler.dispose();
        }
    }

    @Test
    @Timeout(80)
    void routesGroupedCommandToModbus() throws Exception {
        ThingUID uid = new ThingUID("keba:kecontact:groupcommand");
        ChannelUID enabled = new ChannelUID(uid, "modbus#enableduser");
        Thing thing = ThingBuilder.create(new ThingTypeUID("keba", "kecontact"), uid)
                .withConfiguration(new Configuration(Map.of("ipAddress", "192.0.2.1", "udpEnabled", false)))
                .withChannels(ChannelBuilder.create(enabled, "Switch").build()).build();
        ModbusManager manager = Objects.requireNonNull(mock(ModbusManager.class));
        ModbusCommunicationInterface comms = Objects.requireNonNull(mock(ModbusCommunicationInterface.class));
        when(manager.newModbusCommunicationInterface(any(), any())).thenReturn(comms);
        when(comms.registerRegularPoll(any(), anyLong(), anyLong(), any(), any()))
                .thenReturn(Objects.requireNonNull(mock(PollTask.class)));
        BlockingQueue<Read> reads = new LinkedBlockingQueue<>();
        when(comms.submitOneTimePoll(any(), any(), any())).thenAnswer(invocation -> {
            reads.add(new Read(invocation.getArgument(0), invocation.getArgument(1)));
            return java.util.concurrent.CompletableFuture.completedFuture(null);
        });
        BlockingQueue<ModbusWriteRegisterRequestBlueprint> writes = new LinkedBlockingQueue<>();
        when(comms.submitOneTimeWrite(any(), any(), any())).thenAnswer(invocation -> {
            writes.add(invocation.getArgument(0));
            return java.util.concurrent.CompletableFuture.completedFuture(null);
        });
        KeContactCombinedHandler handler = new KeContactCombinedHandler(thing, manager,
                Objects.requireNonNull(mock(KeContactTransceiver.class)));
        handler.setCallback(Objects.requireNonNull(mock(ThingHandlerCallback.class)));
        try {
            handler.initialize();
            Read identification = reads.poll(30, TimeUnit.SECONDS);
            assertNotNull(identification);
            identification.respond(304111);
            handler.handleCommand(enabled, OnOffType.ON);
            ModbusWriteRegisterRequestBlueprint write = writes.poll(30, TimeUnit.SECONDS);
            assertNotNull(write);
            assertEquals(5014, write.getReference());
            assertEquals(1, write.getRegisters().getRegister(0));
        } finally {
            handler.dispose();
        }
    }

    @Test
    void prefersModbusWithoutLosingProtocolOnlyValues() {
        assertEquals(Protocol.MODBUS, KeContactCombinedHandler.sourceFor("power", true, true));
        assertEquals(Protocol.MODBUS, KeContactCombinedHandler.sourceFor("vehicle", true, true));
        assertEquals(Protocol.REST, KeContactCombinedHandler.sourceFor("temperature", true, true));
        assertEquals(Protocol.REST, KeContactCombinedHandler.sourceFor("sessionstart", true, true));
        assertEquals(Protocol.REST, KeContactCombinedHandler.sourceFor("input", true, true));
        assertEquals(Protocol.REST, KeContactCombinedHandler.sourceFor("input", false, false));
        assertEquals(Protocol.UDP, KeContactCombinedHandler.sourceFor("inputx1", true, true));
        assertEquals(Protocol.UDP, KeContactCombinedHandler.sourceFor("display", true, true));
        assertEquals(Protocol.UDP, KeContactCombinedHandler.sourceFor("maxpilotcurrentdutycyle", true, true));
        assertEquals(Protocol.REST, KeContactCombinedHandler.sourceFor("power", false, true));
        assertEquals(Protocol.UDP, KeContactCombinedHandler.sourceFor("power", false, false));
        assertEquals(Protocol.UDP, KeContactCombinedHandler.sourceFor("sessionrfidtag", false, true));
        assertEquals(Protocol.UDP, KeContactCombinedHandler.sourceFor("failsafecurrentsetting", false, true));
    }

    @Test
    @Timeout(40)
    void supplementalRestSkipsDuplicateAndUnlinkedEndpoints() throws Exception {
        for (Set<String> linked : List.of(Set.<String> of(), Set.of("sessionstart", "dipswitchinterpretation"))) {
            BlockingQueue<String> requests = new LinkedBlockingQueue<>();
            Map<String, String> restProperties = new ConcurrentHashMap<>();
            Map<String, State> restStates = new ConcurrentHashMap<>();
            CountDownLatch completed = new CountDownLatch(1);
            AtomicInteger onlineEvents = new AtomicInteger();
            Thing thing = ThingBuilder
                    .create(new ThingTypeUID("keba", "kecontact"), new ThingUID("keba:kecontact:resttest")).build();
            KeContactProtocolHandler.Listener listener = new KeContactProtocolHandler.Listener() {
                @Override
                public void stateUpdated(String channel, State state) {
                    restStates.put(channel, state);
                }

                @Override
                public void statusUpdated(ThingStatus status, ThingStatusDetail detail, @Nullable String description) {
                    if (status == ThingStatus.ONLINE && onlineEvents.incrementAndGet() == 2) {
                        completed.countDown();
                    }
                }

                @Override
                public void propertiesUpdated(Map<String, String> properties) {
                    restProperties.putAll(properties);
                }

                @Override
                public boolean isLinked(String channel) {
                    return linked.contains(channel);
                }
            };
            KeContactRestHandler rest = new KeContactRestHandler(
                    thing, new Configuration(Map.of("baseUrl", "https://192.0.2.1:8443", "username", "admin",
                            "password", "test-password", "refreshInterval", 3600, "verifyCertificate", true)),
                    listener) {
                @Override
                protected JsonObject request(String path, String method, @Nullable String body, boolean authenticated) {
                    requests.add(path);
                    if ("/jwt/login".equals(path)) {
                        return JsonParser.parseString("{\"accessToken\":\"test-token\"}").getAsJsonObject();
                    }
                    if ("/serialnumber".equals(path)) {
                        return JsonParser.parseString("{\"value\":\"12345\"}").getAsJsonObject();
                    }
                    if ("/wallboxes/12345".equals(path)) {
                        return JsonParser.parseString("{\"model\":\"P30\",\"x2active\":true}").getAsJsonObject();
                    }
                    return new JsonObject();
                }
            };
            try {
                rest.initialize();
                assertTrue(completed.await(30, TimeUnit.SECONDS));
                rest.dispose();
                assertTrue(requests.contains("/wallboxes/12345"));
                assertEquals("P30", restProperties.get("model"));
                assertEquals(OnOffType.ON, restStates.get("input"));
                assertFalse(requests.contains("/configs/lmgmt/"));
                assertEquals(linked.contains("dipswitchinterpretation"),
                        requests.contains("/wallboxes/dipswitch/12345"));
                assertEquals(linked.contains("sessionstart"),
                        requests.stream().anyMatch(path -> path.startsWith("/sessions?")));
            } finally {
                rest.dispose();
            }
        }
    }

    @Test
    void keepsNumericStateAndDoesNotInventUnknownValues() {
        assertEquals(new DecimalType(3), KeContactCombinedHandler.numericRestState("CHARGING"));
        assertEquals(new DecimalType(2), KeContactCombinedHandler.numericRestState("READY_FOR_CHARGING"));
        assertEquals(new DecimalType(4), KeContactCombinedHandler.numericRestState("DEGRADED"));
        assertEquals(new DecimalType(5), KeContactCombinedHandler.numericRestState("SUSPENDED"));
        assertEquals(UnDefType.UNDEF, KeContactCombinedHandler.numericRestState("OFFLINE"));
        assertEquals(UnDefType.UNDEF, KeContactCombinedHandler.numericRestState("NEW_FIRMWARE_STATE"));
    }

    @Test
    void disablesRestOnlyForIdentifiedUnsupportedModels() {
        assertTrue(KeContactCombinedHandler.restUnsupportedForProduct("KC-P20"));
        assertTrue(KeContactCombinedHandler.restUnsupportedForProduct("KC-P30-1234562-XXX"));
        assertTrue(KeContactCombinedHandler.restUnsupportedForProduct("KC-P30-123456A-XXX"));
        assertFalse(KeContactCombinedHandler.restUnsupportedForProduct("KC-P30-123456C-XXX"));
        assertFalse(KeContactCombinedHandler.restUnsupportedForProduct("KC-P30-123456B-XXX"));
        assertFalse(KeContactCombinedHandler.restUnsupportedForProduct("KC-P30-123456Z-XXX"));
        assertFalse(KeContactCombinedHandler.restUnsupportedForProduct("P30"));
        assertFalse(KeContactCombinedHandler.restUnsupportedForProduct("P40"));
    }

    @Test
    void disposedRestHandlerCannotBeRestartedByRetry() {
        AtomicInteger starts = new AtomicInteger();
        Thing thing = ThingBuilder
                .create(new ThingTypeUID("keba", "kecontact"), new ThingUID("keba:kecontact:disposedrest")).build();
        KeContactRestHandler rest = new KeContactRestHandler(thing) {
            @Override
            public void initialize() {
                starts.incrementAndGet();
            }
        };
        rest.dispose();
        rest.retryInitialization();
        assertEquals(0, starts.get());
    }

    @Test
    void blankRestUrlDisablesRestEvenWithCredentials() {
        for (String baseUrl : List.of("", "   ")) {
            Thing thing = ThingBuilder
                    .create(new ThingTypeUID("keba", "kecontact"), new ThingUID("keba:kecontact:norest"))
                    .withConfiguration(new Configuration(Map.of("ipAddress", "192.0.2.1", "udpEnabled", false,
                            "baseUrl", baseUrl, "password", "test-password")))
                    .build();
            ModbusManager manager = Objects.requireNonNull(mock(ModbusManager.class));
            ModbusCommunicationInterface comms = Objects.requireNonNull(mock(ModbusCommunicationInterface.class));
            when(manager.newModbusCommunicationInterface(any(), any())).thenReturn(comms);
            KeContactCombinedHandler handler = new KeContactCombinedHandler(thing, manager,
                    Objects.requireNonNull(mock(KeContactTransceiver.class)));
            handler.setCallback(Objects.requireNonNull(mock(ThingHandlerCallback.class)));
            try {
                handler.initialize();
                assertEquals("disabled", thing.getProperties().get("restAvailable"));
            } finally {
                handler.dispose();
            }
        }
    }

    @Test
    void explicitRestUrlRequiresCredentialsBeforeStartingProtocols() {
        Thing thing = ThingBuilder
                .create(new ThingTypeUID("keba", "kecontact"), new ThingUID("keba:kecontact:restcredentials"))
                .withConfiguration(
                        new Configuration(Map.of("ipAddress", "192.0.2.1", "baseUrl", "https://192.0.2.1:8443")))
                .build();
        ModbusManager manager = Objects.requireNonNull(mock(ModbusManager.class));
        KeContactTransceiver transceiver = Objects.requireNonNull(mock(KeContactTransceiver.class));
        ThingHandlerCallback callback = Objects.requireNonNull(mock(ThingHandlerCallback.class));
        KeContactCombinedHandler handler = new KeContactCombinedHandler(thing, manager, transceiver);
        handler.setCallback(callback);
        handler.initialize();
        verify(callback).statusUpdated(eq(thing),
                argThat(status -> status.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR));
        verifyNoInteractions(manager, transceiver);
        handler.dispose();
    }

    @Test
    @Timeout(200)
    void udpUsesIndependentAddressOrFallsBackToModbusAddress() throws Exception {
        for (String udpAddress : List.of("192.0.2.2", "", "   ")) {
            Thing thing = ThingBuilder
                    .create(new ThingTypeUID("keba", "kecontact"), new ThingUID("keba:kecontact:addresses"))
                    .withConfiguration(new Configuration(Map.of("ipAddress", "192.0.2.1", "udpIpAddress", udpAddress)))
                    .build();
            ModbusManager manager = Objects.requireNonNull(mock(ModbusManager.class));
            ModbusCommunicationInterface comms = Objects.requireNonNull(mock(ModbusCommunicationInterface.class));
            BlockingQueue<String> modbusHosts = new LinkedBlockingQueue<>();
            when(manager.newModbusCommunicationInterface(any(), any())).thenAnswer(invocation -> {
                ModbusTCPSlaveEndpoint endpoint = invocation.getArgument(0);
                modbusHosts.add(endpoint.getAddress());
                return comms;
            });
            KeContactTransceiver transceiver = Objects.requireNonNull(mock(KeContactTransceiver.class));
            BlockingQueue<String> udpHosts = new LinkedBlockingQueue<>();
            doAnswer(invocation -> {
                KeContactHandler udp = invocation.getArgument(0);
                udpHosts.add(udp.getIPAddress());
                return null;
            }).when(transceiver).registerHandler(any());
            KeContactCombinedHandler handler = new KeContactCombinedHandler(thing, manager, transceiver);
            handler.setCallback(Objects.requireNonNull(mock(ThingHandlerCallback.class)));
            try {
                handler.initialize();
                assertEquals(udpAddress.isBlank() ? "192.0.2.1" : udpAddress, udpHosts.poll(30, TimeUnit.SECONDS));
                assertEquals("192.0.2.1", modbusHosts.poll(30, TimeUnit.SECONDS));
            } finally {
                handler.dispose();
            }
        }
    }

    @Test
    @Timeout(80)
    void pollsOnlyLinkedRegistersAndIgnoresLateResultsAfterDispose() throws Exception {
        for (String prefix : List.of("", "modbus#")) {
            ThingUID uid = new ThingUID("keba:kecontact:test");
            ChannelUID power = new ChannelUID(uid, prefix + "power");
            Thing thing = ThingBuilder.create(new ThingTypeUID("keba", "kecontact"), uid)
                    .withConfiguration(
                            new Configuration(java.util.Map.of("ipAddress", "192.0.2.1", "udpEnabled", false)))
                    .withChannels(ChannelBuilder.create(power, "Number:Power").build()).build();
            ModbusManager manager = Objects.requireNonNull(mock(ModbusManager.class));
            ModbusCommunicationInterface comms = Objects.requireNonNull(mock(ModbusCommunicationInterface.class));
            ThingHandlerCallback callback = Objects.requireNonNull(mock(ThingHandlerCallback.class));
            when(callback.isChannelLinked(power)).thenReturn(true);
            when(manager.newModbusCommunicationInterface(any(), any())).thenReturn(comms);
            BlockingQueue<Read> oneTime = new LinkedBlockingQueue<>();
            BlockingQueue<Read> polls = new LinkedBlockingQueue<>();
            when(comms.submitOneTimePoll(any(), any(), any())).thenAnswer(invocation -> {
                oneTime.add(new Read(invocation.getArgument(0), invocation.getArgument(1)));
                return java.util.concurrent.CompletableFuture.completedFuture(null);
            });
            when(comms.registerRegularPoll(any(), anyLong(), anyLong(), any(), any())).thenAnswer(invocation -> {
                polls.add(new Read(invocation.getArgument(0), invocation.getArgument(3)));
                return Objects.requireNonNull(mock(PollTask.class));
            });
            KeContactCombinedHandler handler = new KeContactCombinedHandler(thing, manager,
                    Objects.requireNonNull(mock(KeContactTransceiver.class)));
            handler.setCallback(callback);
            try {
                handler.initialize();
                Read identification = oneTime.poll(30, TimeUnit.SECONDS);
                assertNotNull(identification);
                assertEquals(1016, identification.request().getReference());
                identification.respond(304111);
                assertEquals(List.of(1000, 1020), polls.stream().map(read -> read.request().getReference()).toList());
                Read powerRead = polls.stream().filter(read -> read.request().getReference() == 1020).findFirst()
                        .orElseThrow();
                powerRead.respond(1234000);
                verify(callback).stateUpdated(power, new QuantityType<>(1234, Units.WATT));
                handler.dispose();
                clearInvocations(callback, comms);
                powerRead.respond(5678000);
                identification.respond(304111);
                verifyNoInteractions(callback, comms);
            } finally {
                handler.dispose();
            }
        }
    }

    @Test
    @Timeout(80)
    void keepsStartupUnknownUntilProbeSucceedsOrFails() throws Exception {
        for (boolean fail : List.of(false, true)) {
            Thing thing = ThingBuilder
                    .create(new ThingTypeUID("keba", "kecontact"), new ThingUID("keba:kecontact:startup"))
                    .withConfiguration(new Configuration(Map.of("ipAddress", "192.0.2.1", "udpEnabled", false)))
                    .build();
            ModbusManager manager = Objects.requireNonNull(mock(ModbusManager.class));
            ModbusCommunicationInterface comms = Objects.requireNonNull(mock(ModbusCommunicationInterface.class));
            when(manager.newModbusCommunicationInterface(any(), any())).thenReturn(comms);
            when(comms.registerRegularPoll(any(), anyLong(), anyLong(), any(), any()))
                    .thenReturn(Objects.requireNonNull(mock(PollTask.class)));
            BlockingQueue<Read> reads = new LinkedBlockingQueue<>();
            BlockingQueue<ModbusFailureCallback<ModbusReadRequestBlueprint>> failures = new LinkedBlockingQueue<>();
            when(comms.submitOneTimePoll(any(), any(), any())).thenAnswer(invocation -> {
                reads.add(new Read(invocation.getArgument(0), invocation.getArgument(1)));
                failures.add(invocation.getArgument(2));
                return java.util.concurrent.CompletableFuture.completedFuture(null);
            });
            BlockingQueue<ThingStatus> statuses = new LinkedBlockingQueue<>();
            ThingHandlerCallback callback = Objects.requireNonNull(mock(ThingHandlerCallback.class));
            doAnswer(invocation -> {
                ThingStatusInfo info = invocation.getArgument(1);
                statuses.add(info.getStatus());
                return null;
            }).when(callback).statusUpdated(any(), any());
            KeContactCombinedHandler handler = new KeContactCombinedHandler(thing, manager,
                    Objects.requireNonNull(mock(KeContactTransceiver.class)));
            handler.setCallback(callback);
            try {
                handler.initialize();
                Read identification = reads.poll(30, TimeUnit.SECONDS);
                assertNotNull(identification);
                assertTrue(statuses.contains(ThingStatus.UNKNOWN));
                assertFalse(statuses.contains(ThingStatus.OFFLINE));
                if (fail) {
                    ModbusFailureCallback<ModbusReadRequestBlueprint> failure = failures.poll(30, TimeUnit.SECONDS);
                    assertNotNull(failure);
                    failure.handle(new AsyncModbusFailure<>(identification.request(), new IOException("Test failure")));
                    assertTrue(statuses.contains(ThingStatus.OFFLINE));
                } else {
                    identification.respond(304111);
                    assertTrue(statuses.contains(ThingStatus.ONLINE));
                }
            } finally {
                handler.dispose();
            }
        }
    }

    private record Read(ModbusReadRequestBlueprint request, ModbusReadCallback callback) {
        void respond(int value) {
            callback.handle(new AsyncModbusReadResult(request, new ModbusRegisterArray(value >>> 16, value & 0xffff)));
        }
    }
}
