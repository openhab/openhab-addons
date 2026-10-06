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
import java.util.HashMap;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.type.ThingTypeRegistry;
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
    void retainsLegacyUdpChannelIdsAsCanonicalChannels() {
        ThingUID uid = new ThingUID("keba:kecontact:legacyids");
        List<String> legacyIds = List.of("maxsystemcurrent", "maxpilotcurrent", "failsafecurrent", "failsafetimeout");
        var channels = legacyIds
                .stream().map(
                        channel -> ChannelBuilder
                                .create(new ChannelUID(uid, channel),
                                        "failsafetimeout".equals(channel) ? "Number:Time" : "Number:ElectricCurrent")
                                .build())
                .toList();
        Thing thing = ThingBuilder.create(new ThingTypeUID("keba", "kecontact"), uid).withChannels(channels).build();
        KeContactCombinedHandler handler = new KeContactCombinedHandler(thing,
                Objects.requireNonNull(mock(ModbusManager.class)),
                Objects.requireNonNull(mock(KeContactTransceiver.class)));
        handler.setCallback(Objects.requireNonNull(mock(ThingHandlerCallback.class)));
        handler.updateProperties(Map.of("modbusAvailable", "unknown"));
        for (String channel : legacyIds) {
            assertNotNull(handler.getThing().getChannel(new ChannelUID(uid, channel)));
        }
    }

    @Test
    void p20WithoutModbusKeepsUdpChannelsAndRemovesUnsupportedChannels() {
        ThingUID uid = new ThingUID("keba:kecontact:p20udp");
        List<String> exposedChannels = List.of("input", "maxpilotcurrent", "maxsystemcurrent", "failsafecurrent",
                "failsafetimeout", "power", "cablestate", "errorcode", "unlockplug", "stop", "udpstop",
                "failsafepersist", "temperature", "restoutput");
        var channels = exposedChannels.stream()
                .map(channel -> ChannelBuilder.create(new ChannelUID(uid, channel), "String").build()).toList();
        Thing thing = ThingBuilder.create(new ThingTypeUID("keba", "kecontact"), uid)
                .withConfiguration(
                        new Configuration(Map.of("modbusEnabled", false, "udpEnabled", true, "ipAddress", "192.0.2.1")))
                .withChannels(channels).build();
        ModbusManager manager = Objects.requireNonNull(mock(ModbusManager.class));
        KeContactCombinedHandler handler = new KeContactCombinedHandler(thing, manager,
                Objects.requireNonNull(mock(KeContactTransceiver.class)));
        handler.setCallback(Objects.requireNonNull(mock(ThingHandlerCallback.class)));
        try {
            handler.initialize();
            handler.updateProperties(Map.of("model", "KEBA P20"));
            for (String channel : List.of("input", "maxpilotcurrent", "maxsystemcurrent", "failsafecurrent",
                    "failsafetimeout", "power", "unlockplug", "udpstop", "failsafepersist")) {
                assertNotNull(handler.getThing().getChannel(new ChannelUID(uid, channel)), channel);
            }
            for (String channel : List.of("cablestate", "errorcode", "stop", "temperature", "restoutput")) {
                assertNull(handler.getThing().getChannel(new ChannelUID(uid, channel)), channel);
            }
            verifyNoInteractions(manager);
        } finally {
            handler.dispose();
        }
    }

    @Test
    @Timeout(60)
    void dynamicallyReconcilesChannelsWhenProtocolsChange() throws Exception {
        ThingUID uid = new ThingUID("keba:kecontact:dynamicprotocols");
        List<String> channelIds = List.of("input", "display", "state", "power", "cablestate", "temperature", "wallbox",
                "vehicle", "locked");
        Map<String, String> channelTypeIds = Map.of("input", "x1", "display", "display", "state", "state", "power",
                "power", "cablestate", "cable-state", "temperature", "temperature", "wallbox", "plug-wallbox",
                "vehicle", "plug-vehicle", "locked", "locked");
        List<ChannelDefinition> definitions = new ArrayList<>();
        List<org.openhab.core.thing.Channel> initialChannels = new ArrayList<>();
        for (String channel : channelIds) {
            ChannelDefinition definition = Objects.requireNonNull(mock(ChannelDefinition.class));
            when(definition.getId()).thenReturn(channel);
            when(definition.getChannelTypeUID())
                    .thenReturn(new ChannelTypeUID("keba", Objects.requireNonNull(channelTypeIds.get(channel))));
            when(definition.getProperties()).thenReturn(Map.of());
            definitions.add(definition);
            initialChannels.add(ChannelBuilder.create(new ChannelUID(uid, channel), "String")
                    .withType(new ChannelTypeUID("keba", "outdated-type")).withLabel("Custom " + channel)
                    .withConfiguration(new Configuration(Map.of("customSetting", "retained"))).build());
        }
        ThingType thingType = Objects.requireNonNull(mock(ThingType.class));
        when(thingType.getChannelDefinitions()).thenReturn(definitions);
        ThingTypeRegistry registry = Objects.requireNonNull(mock(ThingTypeRegistry.class));
        when(registry.getThingType(new ThingTypeUID("keba", "kecontact"))).thenReturn(thingType);
        Thing thing = ThingBuilder.create(new ThingTypeUID("keba", "kecontact"), uid)
                .withConfiguration(
                        new Configuration(Map.of("modbusEnabled", false, "udpEnabled", true, "ipAddress", "192.0.2.1")))
                .withChannels(initialChannels).build();

        ModbusManager manager = Objects.requireNonNull(mock(ModbusManager.class));
        ModbusCommunicationInterface comms = Objects.requireNonNull(mock(ModbusCommunicationInterface.class));
        when(manager.newModbusCommunicationInterface(any(), any())).thenReturn(comms);
        when(comms.registerRegularPoll(any(), anyLong(), anyLong(), any(), any()))
                .thenReturn(Objects.requireNonNull(mock(PollTask.class)));
        when(comms.submitOneTimePoll(any(), any(), any()))
                .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(null));

        KeContactTransceiver transceiver = new KeContactTransceiver() {
            @Override
            public void registerHandler(KeContactHandler handler) {
            }

            @Override
            protected @Nullable ByteBuffer send(String message, KeContactHandler handler) {
                String response = switch (message) {
                    case "report 1" -> "{\"ID\":1,\"Product\":\"P30\"}";
                    case "report 2" -> "{\"ID\":2,\"State\":3}";
                    default -> null;
                };
                return response == null ? null : ByteBuffer.wrap(response.getBytes(StandardCharsets.US_ASCII));
            }
        };
        KeContactCombinedHandler handler = new KeContactCombinedHandler(thing, manager, transceiver, registry);
        ThingHandlerCallback callback = Objects.requireNonNull(mock(ThingHandlerCallback.class));
        when(callback.createChannelBuilder(any(ChannelUID.class), any(ChannelTypeUID.class))).thenAnswer(invocation -> {
            ChannelUID channelUID = invocation.getArgument(0);
            ChannelTypeUID channelTypeUID = invocation.getArgument(1);
            String itemType = "power".equals(channelUID.getIdWithoutGroup()) ? "Number:Power" : "Number";
            return ChannelBuilder.create(channelUID, itemType).withType(channelTypeUID);
        });
        handler.setCallback(callback);
        try {
            handler.initialize();
            var repairedPower = Objects.requireNonNull(handler.getThing().getChannel(new ChannelUID(uid, "power")));
            assertEquals(new ChannelTypeUID("keba", "power"), repairedPower.getChannelTypeUID());
            assertEquals("Number:Power", repairedPower.getAcceptedItemType());
            assertEquals("Custom power", repairedPower.getLabel());
            assertEquals("retained", repairedPower.getConfiguration().get("customSetting"));

            handler.handleConfigurationUpdate(Map.of("udpEnabled", false));
            assertTrue(handler.getThing().getChannels().isEmpty());

            handler.handleConfigurationUpdate(Map.of("udpEnabled", true, "ipAddress", "192.0.2.1"));
            assertNotNull(handler.getThing().getChannel(new ChannelUID(uid, "input")));
            assertNotNull(handler.getThing().getChannel(new ChannelUID(uid, "display")));
            assertNotNull(handler.getThing().getChannel(new ChannelUID(uid, "state")));
            assertNotNull(handler.getThing().getChannel(new ChannelUID(uid, "power")));
            assertEquals(new ChannelTypeUID("keba", "x1"),
                    handler.getThing().getChannel(new ChannelUID(uid, "input")).getChannelTypeUID());
            assertEquals("Number", handler.getThing().getChannel(new ChannelUID(uid, "input")).getAcceptedItemType());
            assertEquals("Number:Power",
                    handler.getThing().getChannel(new ChannelUID(uid, "power")).getAcceptedItemType());
            assertNull(handler.getThing().getChannel(new ChannelUID(uid, "cablestate")));
            assertNull(handler.getThing().getChannel(new ChannelUID(uid, "temperature")));

            handler.handleConfigurationUpdate(Map.of("modbusEnabled", true, "ipAddress", "192.0.2.1"));
            assertNull(handler.getThing().getChannel(new ChannelUID(uid, "cablestate")));
            assertNotNull(handler.getThing().getChannel(new ChannelUID(uid, "input")));
            assertNull(handler.getThing().getChannel(new ChannelUID(uid, "temperature")));

            handler.handleConfigurationUpdate(Map.of("udpEnabled", false));
            assertNull(handler.getThing().getChannel(new ChannelUID(uid, "display")));
            assertNull(handler.getThing().getChannel(new ChannelUID(uid, "input")));
            assertNull(handler.getThing().getChannel(new ChannelUID(uid, "cablestate")));
            assertNotNull(handler.getThing().getChannel(new ChannelUID(uid, "power")));
            for (String channel : List.of("wallbox", "vehicle", "locked")) {
                assertNotNull(handler.getThing().getChannel(new ChannelUID(uid, channel)), channel);
            }
        } finally {
            handler.dispose();
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
    void metadataUsesFlatLegacyIdsWithoutRedundantChannels() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        try (InputStream source = Objects
                .requireNonNull(getClass().getResourceAsStream("/OH-INF/thing/kecontact.xml"))) {
            var document = factory.newDocumentBuilder().parse(source);
            NodeList channelGroups = document.getElementsByTagName("channel-groups");
            assertEquals(0, channelGroups.getLength());
            Map<String, Element> configParameters = new HashMap<>();
            NodeList parameters = document.getElementsByTagName("parameter");
            for (int parameterIndex = 0; parameterIndex < parameters.getLength(); parameterIndex++) {
                Element parameter = (Element) parameters.item(parameterIndex);
                configParameters.put(parameter.getAttribute("name"), parameter);
            }
            Element proxyAddress = Objects.requireNonNull(configParameters.get("modbusIpAddress"));
            assertEquals("false", proxyAddress.getAttribute("required"));
            assertEquals(0, proxyAddress.getElementsByTagName("context").getLength());
            assertTrue(
                    proxyAddress.getElementsByTagName("description").item(0).getTextContent().contains("leave empty"));
            assertFalse(configParameters.containsKey("udpIpAddress"));
            assertFalse(configParameters.containsKey("baseUrl"));
            Element displayOnly = Objects.requireNonNull(configParameters.get("udpDisplayOnly"));
            assertEquals("boolean", displayOnly.getAttribute("type"));
            assertEquals("udp", displayOnly.getAttribute("groupName"));
            assertEquals("false", displayOnly.getElementsByTagName("default").item(0).getTextContent());
            assertEquals("common",
                    ((Element) document.getElementsByTagName("parameter-group").item(0)).getAttribute("name"));
            assertEquals("ipAddress", ((Element) parameters.item(0)).getAttribute("name"));
            assertEquals("common", Objects.requireNonNull(configParameters.get("ipAddress")).getAttribute("groupName"));
            assertEquals("common",
                    Objects.requireNonNull(configParameters.get("refreshIntervalSlow")).getAttribute("groupName"));
            assertEquals("common",
                    Objects.requireNonNull(configParameters.get("refreshInterval")).getAttribute("groupName"));
            for (String protocol : List.of("modbus", "udp", "rest")) {
                Element enabled = Objects.requireNonNull(configParameters.get(protocol + "Enabled"));
                assertEquals("boolean", enabled.getAttribute("type"));
                assertEquals(protocol, enabled.getAttribute("groupName"));
                assertTrue(enabled.getElementsByTagName("label").item(0).getTextContent().startsWith("Enable "));
                assertEquals("rest".equals(protocol) ? "false" : "true",
                        enabled.getElementsByTagName("default").item(0).getTextContent());
            }
            assertEquals("8443", Objects.requireNonNull(configParameters.get("restPort"))
                    .getElementsByTagName("default").item(0).getTextContent());
            NodeList typeDefinitions = document.getElementsByTagName("channel-type");
            Set<String> channelTypes = new HashSet<>();
            for (int typeIndex = 0; typeIndex < typeDefinitions.getLength(); typeIndex++) {
                Element typeDefinition = (Element) typeDefinitions.item(typeIndex);
                assertTrue(channelTypes.add(typeDefinition.getAttribute("id")),
                        "Duplicate channel type: " + typeDefinition.getAttribute("id"));
                NodeList options = typeDefinition.getElementsByTagName("option");
                Set<String> optionValues = new HashSet<>();
                for (int optionIndex = 0; optionIndex < options.getLength(); optionIndex++) {
                    String value = ((Element) options.item(optionIndex)).getAttribute("value");
                    assertTrue(optionValues.add(value),
                            "Duplicate option value " + value + " for " + typeDefinition.getAttribute("id"));
                }
                if (Set.of("phase-switch-state", "phase-switch-state-readonly")
                        .contains(typeDefinition.getAttribute("id"))) {
                    assertEquals(Set.of("1", "3"), optionValues);
                    assertEquals("Number", typeDefinition.getElementsByTagName("item-type").item(0).getTextContent());
                    assertEquals(
                            "phase-switch-state-readonly".equals(typeDefinition.getAttribute("id")) ? "true" : "false",
                            ((Element) typeDefinition.getElementsByTagName("state").item(0)).getAttribute("readOnly"));
                }
            }
            assertTrue(channelTypes.stream().allMatch(type -> type.matches("[a-z0-9]+(-[a-z0-9]+)*")));
            assertTrue(channelTypes.contains("session-start"));
            assertFalse(channelTypes.contains("rest-session-start"));
            @Nullable
            Element stateType = null;
            Element phaseSourceType = null;
            for (int typeIndex = 0; typeIndex < typeDefinitions.getLength(); typeIndex++) {
                Element typeDefinition = (Element) typeDefinitions.item(typeIndex);
                if ("state".equals(typeDefinition.getAttribute("id"))) {
                    stateType = typeDefinition;
                } else if ("phase-switch-source".equals(typeDefinition.getAttribute("id"))) {
                    phaseSourceType = typeDefinition;
                }
            }
            NodeList suspendedOption = Objects.requireNonNull(stateType).getElementsByTagName("option");
            boolean hasSuspendedState = false;
            for (int optionIndex = 0; optionIndex < suspendedOption.getLength(); optionIndex++) {
                hasSuspendedState |= "5".equals(((Element) suspendedOption.item(optionIndex)).getAttribute("value"));
            }
            assertTrue(hasSuspendedState);
            boolean hasUdpPhaseSource = false;
            NodeList phaseOptions = Objects.requireNonNull(phaseSourceType).getElementsByTagName("option");
            for (int optionIndex = 0; optionIndex < phaseOptions.getLength(); optionIndex++) {
                hasUdpPhaseSource |= "4".equals(((Element) phaseOptions.item(optionIndex)).getAttribute("value"));
            }
            assertTrue(hasUdpPhaseSource);
            NodeList definitions = document.getElementsByTagName("channel");
            Set<String> channels = new HashSet<>();
            Map<String, String> channelTypesById = new HashMap<>();
            for (int channelIndex = 0; channelIndex < definitions.getLength(); channelIndex++) {
                Element definition = (Element) definitions.item(channelIndex);
                String channel = definition.getAttribute("id");
                assertTrue(channels.add(channel), "Duplicate channel: " + channel);
                String type = definition.getAttribute("typeId");
                assertTrue(channelTypes.contains(type), channel);
                channelTypesById.put(channel, type);
                NodeList descriptions = definition.getElementsByTagName("description");
                assertEquals(1, descriptions.getLength(), "Missing specific description: " + channel);
                assertTrue(descriptions.item(0).getTextContent().strip().length() > 20, channel);
            }
            assertEquals(69, channels.size());
            assertFalse(channels.contains("triggerphaseswitch"));
            assertTrue(channels.contains("unlockplug"));
            assertTrue(channels.contains("udpstop"));
            assertFalse(channels.contains("cablestate"));
            assertFalse(channels.contains("unlock"));
            assertTrue(channels.containsAll(List.of("maxsystemcurrent", "maxpilotcurrent", "failsafecurrent",
                    "failsafetimeout", "input", "restoutput", "togglephaseswitch")));
            assertEquals("command", channelTypesById.get("togglephaseswitch"));
            assertFalse(channels.contains("maxsupportedcurrent"));
            assertFalse(channels.contains("maxchargingcurrent"));
            assertFalse(channels.contains("setchargingcurrent"));
            assertFalse(channels.contains("inputx1"));
            try (InputStream updateSource = Objects
                    .requireNonNull(getClass().getResourceAsStream("/OH-INF/update/kecontact-enabled.xml"))) {
                var updateDocument = factory.newDocumentBuilder().parse(updateSource);
                NodeList instructionSets = updateDocument.getElementsByTagName("instruction-set");
                Set<String> removedChannels = new HashSet<>();
                for (int setIndex = 0; setIndex < instructionSets.getLength(); setIndex++) {
                    NodeList instructions = instructionSets.item(setIndex).getChildNodes();
                    for (int instructionIndex = 0; instructionIndex < instructions.getLength(); instructionIndex++) {
                        var child = instructions.item(instructionIndex);
                        if (child instanceof Element instruction
                                && "remove-channel".equals(instruction.getNodeName())) {
                            removedChannels.add(instruction.getAttribute("id"));
                        }
                    }
                }
                for (int setIndex = 0; setIndex < instructionSets.getLength(); setIndex++) {
                    Element instructionSet = (Element) instructionSets.item(setIndex);
                    Set<String> operatedChannels = new HashSet<>();
                    for (int instructionIndex = 0; instructionIndex < instructionSet.getChildNodes()
                            .getLength(); instructionIndex++) {
                        var child = instructionSet.getChildNodes().item(instructionIndex);
                        if (!(child instanceof Element)) {
                            continue;
                        }
                        Element instruction = (Element) child;
                        String operation = instruction.getNodeName();
                        if (!Set.of("add-channel", "update-channel", "remove-channel").contains(operation)) {
                            continue;
                        }
                        String channel = instruction.getAttribute("id");
                        assertTrue(operatedChannels.add(channel), "Duplicate operation for " + channel + " in target "
                                + instructionSet.getAttribute("targetVersion"));
                        if ("remove-channel".equals(operation)) {
                            assertFalse(channelTypesById.containsKey(channel),
                                    "Removed channel is still defined: " + channel);
                        } else {
                            if (!removedChannels.contains(channel)) {
                                assertTrue(channelTypesById.containsKey(channel),
                                        "Migrated channel is not defined: " + channel);
                                String type = instruction.getElementsByTagName("type").item(0).getTextContent()
                                        .replace("keba:", "");
                                assertEquals(channelTypesById.get(channel), type, channel);
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    @Timeout(30)
    void udpPrimaryPollsOperationalReportsFastAndSupplementalReportsSlow() throws Exception {
        ThingUID uid = new ThingUID("keba:kecontact:udppolling");
        List<org.openhab.core.thing.Channel> channels = new ArrayList<>();
        for (String id : List.of("input", "power", "backend", "sessionid")) {
            channels.add(ChannelBuilder.create(new ChannelUID(uid, id), "Number").build());
        }
        Thing thing = ThingBuilder.create(new ThingTypeUID("keba", "kecontact"), uid)
                .withConfiguration(new Configuration(Map.of("ipAddress", "192.0.2.1", "modbusEnabled", false,
                        "refreshInterval", 10, "refreshIntervalSlow", 60)))
                .withChannels(channels).build();
        AtomicLong clock = new AtomicLong();
        BlockingQueue<String> reports = new LinkedBlockingQueue<>();
        KeContactTransceiver transceiver = new KeContactTransceiver() {
            @Override
            public void registerHandler(KeContactHandler handler) {
            }

            @Override
            protected @Nullable ByteBuffer send(String message, KeContactHandler handler) {
                reports.add(message);
                String response = switch (message) {
                    case "report 1" -> "{\"ID\":1,\"Product\":\"P30\"}";
                    case "report 2" -> "{\"ID\":2,\"State\":3}";
                    case "report 3" -> "{\"ID\":3,\"P\":1000}";
                    case "report 100" -> "{\"ID\":100,\"Session ID\":1}";
                    default -> null;
                };
                return response == null ? null : ByteBuffer.wrap(response.getBytes(StandardCharsets.US_ASCII));
            }
        };
        KeContactCombinedHandler handler = new KeContactCombinedHandler(thing,
                Objects.requireNonNull(mock(ModbusManager.class)), transceiver) {
            @Override
            long pollingTime() {
                return clock.get();
            }
        };
        ThingHandlerCallback callback = Objects.requireNonNull(mock(ThingHandlerCallback.class));
        when(callback.isChannelLinked(any())).thenReturn(true);
        handler.setCallback(callback);
        try {
            handler.initialize();
            assertEquals("report 1", reports.poll(5, TimeUnit.SECONDS));
            assertEquals("report 2", reports.poll(5, TimeUnit.SECONDS));
            assertEquals("report 1", reports.poll(5, TimeUnit.SECONDS));
            assertEquals("report 3", reports.poll(5, TimeUnit.SECONDS));
            assertEquals("report 100", reports.poll(5, TimeUnit.SECONDS));
            assertNull(reports.poll(1200, TimeUnit.MILLISECONDS));

            clock.set(TimeUnit.SECONDS.toNanos(10));
            assertEquals("report 2", reports.poll(5, TimeUnit.SECONDS));
            assertEquals("report 3", reports.poll(5, TimeUnit.SECONDS));
            assertNull(reports.poll(1200, TimeUnit.MILLISECONDS));

            clock.set(TimeUnit.SECONDS.toNanos(60));
            assertEquals("report 2", reports.poll(5, TimeUnit.SECONDS));
            assertEquals("report 1", reports.poll(5, TimeUnit.SECONDS));
            assertEquals("report 3", reports.poll(5, TimeUnit.SECONDS));
            assertEquals("report 100", reports.poll(5, TimeUnit.SECONDS));
            assertNull(reports.poll(1200, TimeUnit.MILLISECONDS));
        } finally {
            handler.dispose();
        }
    }

    @Test
    @Timeout(40)
    void udpSupplementalPollingAndCommandChecksDoNotPreventFastFailover() throws Exception {
        ThingUID uid = new ThingUID("keba:kecontact:udpfailover");
        ChannelUID input = new ChannelUID(uid, "input");
        Thing thing = ThingBuilder.create(new ThingTypeUID("keba", "kecontact"), uid)
                .withConfiguration(new Configuration(
                        Map.of("ipAddress", "192.0.2.1", "refreshInterval", 10, "refreshIntervalSlow", 60)))
                .withChannels(ChannelBuilder.create(input, "Switch").build(),
                        ChannelBuilder.create(new ChannelUID(uid, "display"), "String").build())
                .build();
        AtomicLong clock = new AtomicLong();
        AtomicBoolean linked = new AtomicBoolean();
        CountDownLatch modbusReady = new CountDownLatch(1);
        BlockingQueue<String> requests = new LinkedBlockingQueue<>();
        ModbusManager manager = Objects.requireNonNull(mock(ModbusManager.class));
        ModbusCommunicationInterface comms = Objects.requireNonNull(mock(ModbusCommunicationInterface.class));
        when(manager.newModbusCommunicationInterface(any(), any())).thenReturn(comms);
        BlockingQueue<Read> identificationReads = new LinkedBlockingQueue<>();
        BlockingQueue<Read> operationalReads = new LinkedBlockingQueue<>();
        BlockingQueue<ModbusFailureCallback<ModbusReadRequestBlueprint>> failures = new LinkedBlockingQueue<>();
        when(comms.submitOneTimePoll(any(), any(), any())).thenAnswer(invocation -> {
            identificationReads.add(new Read(invocation.getArgument(0), invocation.getArgument(1)));
            return java.util.concurrent.CompletableFuture.completedFuture(null);
        });
        when(comms.registerRegularPoll(any(), anyLong(), anyLong(), any(), any())).thenAnswer(invocation -> {
            operationalReads.add(new Read(invocation.getArgument(0), invocation.getArgument(3)));
            failures.add(invocation.getArgument(4));
            return Objects.requireNonNull(mock(PollTask.class));
        });
        KeContactTransceiver transceiver = new KeContactTransceiver() {
            @Override
            public void registerHandler(KeContactHandler handler) {
            }

            @Override
            protected @Nullable ByteBuffer send(String message, KeContactHandler handler) {
                if ("report 1".equals(message)) {
                    try {
                        assertTrue(modbusReady.await(5, TimeUnit.SECONDS));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
                requests.add(message);
                String response = "report 1".equals(message) ? "{\"ID\":1,\"Product\":\"KC-P30-123456A-XXX\"}"
                        : "{\"ID\":2,\"State\":3}";
                return ByteBuffer.wrap(response.getBytes(StandardCharsets.US_ASCII));
            }
        };
        KeContactCombinedHandler handler = new KeContactCombinedHandler(thing, manager, transceiver) {
            @Override
            long pollingTime() {
                return clock.get();
            }
        };
        ThingHandlerCallback callback = Objects.requireNonNull(mock(ThingHandlerCallback.class));
        when(callback.isChannelLinked(input)).thenAnswer(invocation -> linked.get());
        handler.setCallback(callback);
        try {
            handler.initialize();
            Read identification = Objects.requireNonNull(identificationReads.poll(5, TimeUnit.SECONDS));
            identification.respond(304111);
            modbusReady.countDown();
            assertEquals("report 1", requests.poll(5, TimeUnit.SECONDS));
            assertNull(requests.poll(1200, TimeUnit.MILLISECONDS));

            handler.setDisplay("Ready", -1, -1);
            assertEquals("report 2", requests.poll(5, TimeUnit.SECONDS));
            assertTrue(Objects.requireNonNull(requests.poll(5, TimeUnit.SECONDS)).startsWith("display "));
            clock.set(TimeUnit.SECONDS.toNanos(10));
            assertNull(requests.poll(1200, TimeUnit.MILLISECONDS));

            linked.set(true);
            clock.set(TimeUnit.SECONDS.toNanos(60));
            assertEquals("report 2", requests.poll(5, TimeUnit.SECONDS));
            assertNull(requests.poll(1200, TimeUnit.MILLISECONDS));

            Read operational = Objects.requireNonNull(operationalReads.poll(5, TimeUnit.SECONDS));
            ModbusFailureCallback<ModbusReadRequestBlueprint> failure = Objects
                    .requireNonNull(failures.poll(5, TimeUnit.SECONDS));
            for (int attempt = 0; attempt < 10; attempt++) {
                failure.handle(new AsyncModbusFailure<>(operational.request(), new IOException("Test failure")));
            }
            assertEquals("unavailable", handler.getThing().getProperties().get("modbusAvailable"));
            clock.set(TimeUnit.SECONDS.toNanos(70));
            assertEquals("report 2", requests.poll(5, TimeUnit.SECONDS));
            clock.set(TimeUnit.SECONDS.toNanos(80));
            assertEquals("report 2", requests.poll(5, TimeUnit.SECONDS));

            operational.respond(3);
            assertEquals("available", handler.getThing().getProperties().get("modbusAvailable"));
            clock.set(TimeUnit.SECONDS.toNanos(90));
            assertNull(requests.poll(1200, TimeUnit.MILLISECONDS));
            clock.set(TimeUnit.SECONDS.toNanos(120));
            assertEquals("report 2", requests.poll(5, TimeUnit.SECONDS));
        } finally {
            modbusReady.countDown();
            handler.dispose();
        }
    }

    @Test
    @Timeout(30)
    void udpDisplayOnlyDoesNotPollOrExposeMeasurements() throws Exception {
        ThingUID uid = new ThingUID("keba:kecontact:displayonly");
        List<ChannelDefinition> definitions = new ArrayList<>();
        for (String id : List.of("display", "input", "state", "power", "phaseswitchstate")) {
            ChannelDefinition definition = Objects.requireNonNull(mock(ChannelDefinition.class));
            when(definition.getId()).thenReturn(id);
            when(definition.getChannelTypeUID()).thenReturn(new ChannelTypeUID("keba", id));
            when(definition.getProperties()).thenReturn(Map.of());
            definitions.add(definition);
        }
        ThingType type = Objects.requireNonNull(mock(ThingType.class));
        when(type.getChannelDefinitions()).thenReturn(definitions);
        ThingTypeRegistry registry = Objects.requireNonNull(mock(ThingTypeRegistry.class));
        when(registry.getThingType(new ThingTypeUID("keba", "kecontact"))).thenReturn(type);
        Thing thing = ThingBuilder.create(new ThingTypeUID("keba", "kecontact"), uid).withConfiguration(
                new Configuration(Map.of("ipAddress", "192.0.2.1", "modbusEnabled", false, "udpDisplayOnly", true)))
                .build();
        AtomicLong clock = new AtomicLong();
        AtomicBoolean reachable = new AtomicBoolean();
        BlockingQueue<String> requests = new LinkedBlockingQueue<>();
        KeContactTransceiver transceiver = new KeContactTransceiver() {
            @Override
            public void registerHandler(KeContactHandler handler) {
            }

            @Override
            protected @Nullable ByteBuffer send(String message, KeContactHandler handler) {
                requests.add(message);
                if ("report 1".equals(message) && !reachable.get()) {
                    return null;
                }
                String response = switch (message) {
                    case "report 1" -> "{\"ID\":1,\"Product\":\"KC-P30-123456C-XXX\",\"DIP-Sw1\":\"0x20\"}";
                    case "report 2" -> "{\"ID\":2,\"State\":3}";
                    default -> null;
                };
                return response == null ? null : ByteBuffer.wrap(response.getBytes(StandardCharsets.US_ASCII));
            }
        };
        ThingHandlerCallback callback = Objects.requireNonNull(mock(ThingHandlerCallback.class));
        when(callback.isChannelLinked(any())).thenReturn(true);
        when(callback.createChannelBuilder(any(ChannelUID.class), any(ChannelTypeUID.class))).thenAnswer(invocation -> {
            ChannelUID channelUID = invocation.getArgument(0);
            ChannelTypeUID typeUID = invocation.getArgument(1);
            return ChannelBuilder.create(channelUID, "String").withType(typeUID);
        });
        KeContactCombinedHandler handler = new KeContactCombinedHandler(thing,
                Objects.requireNonNull(mock(ModbusManager.class)), transceiver, registry) {
            @Override
            long pollingTime() {
                return clock.get();
            }
        };
        handler.setCallback(callback);
        try {
            handler.initialize();
            assertEquals(List.of("display"), handler.getThing().getChannels().stream()
                    .map(channel -> channel.getUID().getIdWithoutGroup()).toList());
            assertEquals("display-only", handler.getThing().getProperties().get("udpAvailable"));
            assertNull(requests.poll(1200, TimeUnit.MILLISECONDS));
            clock.set(TimeUnit.HOURS.toNanos(1));
            handler.handleCommand(new ChannelUID(uid, "input"), OnOffType.ON);
            assertNull(requests.poll(1200, TimeUnit.MILLISECONDS));

            handler.handleCommand(new ChannelUID(uid, "display"), new StringType("Ready"));
            assertEquals("report 1", requests.poll(5, TimeUnit.SECONDS));
            assertNull(requests.poll(1200, TimeUnit.MILLISECONDS));
            assertEquals("unavailable", handler.getThing().getProperties().get("udpAvailable"));
            reachable.set(true);
            handler.setDisplay("Ready", -1, -1);
            assertEquals("report 1", requests.poll(5, TimeUnit.SECONDS));
            assertTrue(Objects.requireNonNull(requests.poll(5, TimeUnit.SECONDS)).startsWith("display "));
            handler.setDisplay("Charging", -1, -1);
            assertTrue(Objects.requireNonNull(requests.poll(5, TimeUnit.SECONDS)).startsWith("display "));
            assertNull(requests.poll(1200, TimeUnit.MILLISECONDS));
            verify(callback, never()).stateUpdated(eq(new ChannelUID(uid, "state")), any());
        } finally {
            handler.dispose();
        }
    }

    @Test
    void publishesAndChecksFlatProtocolChannels() {
        ThingUID uid = new ThingUID("keba:kecontact:flatchannels");
        ChannelUID display = new ChannelUID(uid, "display");
        ChannelUID temperature = new ChannelUID(uid, "temperature");
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
    void udpInputDoesNotOverwriteRestX2Output() throws Exception {
        ThingUID uid = new ThingUID("keba:kecontact:inputs");
        ChannelUID x1 = new ChannelUID(uid, "input");
        ChannelUID restOutput = new ChannelUID(uid, "restoutput");
        Thing thing = ThingBuilder.create(new ThingTypeUID("keba", "kecontact"), uid)
                .withConfiguration(new Configuration(Map.of("ipAddress", "192.0.2.1")))
                .withChannels(ChannelBuilder.create(x1, "Switch").build(),
                        ChannelBuilder.create(restOutput, "Switch").build())
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
                callback.handle(
                        new AsyncModbusReadResult(request, new ModbusRegisterArray(304111 >>> 16, 304111 & 0xffff)));
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
            verify(callback, never()).stateUpdated(eq(restOutput), any());
        } finally {
            handler.dispose();
        }
    }

    @Test
    @Timeout(40)
    void udpPhaseSwitchAcceptsOnlyOneOrThreePhases() throws Exception {
        ThingUID uid = new ThingUID("keba:kecontact:phasecounts");
        ChannelUID trigger = new ChannelUID(uid, "phaseswitchstate");
        Thing thing = ThingBuilder.create(new ThingTypeUID("keba", "kecontact"), uid)
                .withConfiguration(new Configuration(Map.of("ipAddress", "192.0.2.1", "modbusEnabled", false,
                        "refreshInterval", 3600, "refreshIntervalSlow", 3600)))
                .withChannels(ChannelBuilder.create(trigger, "Number").build()).build();
        BlockingQueue<String> writes = new LinkedBlockingQueue<>();
        CountDownLatch initialized = new CountDownLatch(1);
        KeContactTransceiver transceiver = new KeContactTransceiver() {
            @Override
            public void registerHandler(KeContactHandler handler) {
            }

            @Override
            protected @Nullable ByteBuffer send(String message, KeContactHandler handler) {
                if (message.startsWith("x2 ")) {
                    writes.add(message);
                }
                String response = switch (message) {
                    case "report 1" -> "{\"ID\":1,\"Product\":\"KC-P30-123456A-XXX\"}";
                    case "report 2" -> "{\"ID\":2,\"State\":3}";
                    default -> null;
                };
                if ("report 2".equals(message)) {
                    initialized.countDown();
                }
                return response == null ? null : ByteBuffer.wrap(response.getBytes(StandardCharsets.US_ASCII));
            }
        };
        KeContactCombinedHandler handler = new KeContactCombinedHandler(thing,
                Objects.requireNonNull(mock(ModbusManager.class)), transceiver);
        handler.setCallback(Objects.requireNonNull(mock(ThingHandlerCallback.class)));
        try {
            handler.initialize();
            assertTrue(initialized.await(5, TimeUnit.SECONDS));
            handler.handleCommand(trigger, new DecimalType(1));
            assertEquals("x2 0", writes.poll(5, TimeUnit.SECONDS));
            handler.handleCommand(trigger, new DecimalType(3));
            assertEquals("x2 1", writes.poll(5, TimeUnit.SECONDS));
            for (String value : List.of("0", "2", "4", "-1", "1.5", "3.1")) {
                handler.handleCommand(trigger, new DecimalType(value));
            }
            handler.handleCommand(trigger, OnOffType.ON);
            assertNull(writes.poll(1200, TimeUnit.MILLISECONDS));
        } finally {
            handler.dispose();
        }
    }

    @Test
    @Timeout(40)
    void udpFallbackPublishesLegacyChannelIds() throws Exception {
        ThingUID uid = new ThingUID("keba:kecontact:legacyfallback");
        var channels = new ArrayList<org.openhab.core.thing.Channel>();
        for (String channel : List.of("maxsystemcurrent", "maxpilotcurrent", "failsafecurrent")) {
            channels.add(ChannelBuilder.create(new ChannelUID(uid, channel), "Number:ElectricCurrent").build());
        }
        ChannelUID timeout = new ChannelUID(uid, "failsafetimeout");
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
            verify(callback).stateUpdated(new ChannelUID(uid, "maxsystemcurrent"),
                    new QuantityType<>(32, Units.AMPERE));
            verify(callback).stateUpdated(new ChannelUID(uid, "maxpilotcurrent"), new QuantityType<>(16, Units.AMPERE));
            verify(callback).stateUpdated(new ChannelUID(uid, "failsafecurrent"), new QuantityType<>(6, Units.AMPERE));
            for (String duplicate : List.of("maxsupportedcurrent", "maxchargingcurrent", "setchargingcurrent",
                    "failsafecurrentsetting", "failsafetimeoutsetting")) {
                verify(callback, never()).stateUpdated(eq(new ChannelUID(uid, duplicate)), any());
            }
        } finally {
            handler.dispose();
        }
    }

    @Test
    @Timeout(80)
    void routesFlatCommandToModbus() throws Exception {
        ThingUID uid = new ThingUID("keba:kecontact:flatcommand");
        ChannelUID enabled = new ChannelUID(uid, "enableduser");
        ChannelUID failsafeCurrent = new ChannelUID(uid, "failsafecurrent");
        ChannelUID failsafeTimeout = new ChannelUID(uid, "failsafetimeout");
        Thing thing = ThingBuilder.create(new ThingTypeUID("keba", "kecontact"), uid)
                .withConfiguration(new Configuration(Map.of("ipAddress", "192.0.2.1", "udpEnabled", false)))
                .withChannels(ChannelBuilder.create(enabled, "Switch").build(),
                        ChannelBuilder.create(failsafeCurrent, "Number:ElectricCurrent").build(),
                        ChannelBuilder.create(failsafeTimeout, "Number:Time").build())
                .build();
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

            handler.handleCommand(failsafeCurrent, new QuantityType<>(8, Units.AMPERE));
            write = writes.poll(15, TimeUnit.SECONDS);
            assertNotNull(write);
            assertEquals(5016, write.getReference());
            assertEquals(8000, write.getRegisters().getRegister(0));

            handler.handleCommand(failsafeTimeout, new QuantityType<>(30, Units.SECOND));
            write = writes.poll(15, TimeUnit.SECONDS);
            assertNotNull(write);
            assertEquals(5018, write.getReference());
            assertEquals(30, write.getRegisters().getRegister(0));
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
        assertEquals(Protocol.REST, KeContactCombinedHandler.sourceFor("restoutput", true, true));
        assertEquals(Protocol.REST, KeContactCombinedHandler.sourceFor("restoutput", false, false));
        assertEquals(Protocol.UDP, KeContactCombinedHandler.sourceFor("input", true, true));
        assertEquals(Protocol.UDP, KeContactCombinedHandler.sourceFor("display", true, true));
        assertEquals(Protocol.UDP, KeContactCombinedHandler.sourceFor("maxpilotcurrentdutycyle", true, true));
        assertEquals(Protocol.REST, KeContactCombinedHandler.sourceFor("power", false, true));
        assertEquals(Protocol.UDP, KeContactCombinedHandler.sourceFor("power", false, false));
        assertEquals(Protocol.UDP, KeContactCombinedHandler.sourceFor("sessionrfidtag", false, true));
        assertEquals(Protocol.UDP, KeContactCombinedHandler.sourceFor("failsafecurrent", false, true));
        assertEquals(Protocol.REST, KeContactCombinedHandler.sourceFor("stop", false, true));
        assertEquals(Protocol.UDP, KeContactCombinedHandler.sourceFor("stop", false, false));
    }

    @Test
    void phaseSwitchSourceAcceptsOnlyIntegerValuesFromZeroThroughFour() {
        for (int source = 0; source <= 4; source++) {
            assertTrue(KeContactCombinedHandler.isValidPhaseSwitchSource(new DecimalType(source)));
        }
        assertTrue(KeContactCombinedHandler.isValidPhaseSwitchSource(new DecimalType("1.0")));
        assertFalse(KeContactCombinedHandler.isValidPhaseSwitchSource(new DecimalType("1.5")));
        assertFalse(KeContactCombinedHandler.isValidPhaseSwitchSource(new DecimalType(-1)));
        assertFalse(KeContactCombinedHandler.isValidPhaseSwitchSource(new DecimalType(5)));
        assertFalse(KeContactCombinedHandler.isValidPhaseSwitchSource(OnOffType.ON));
    }

    @Test
    @Timeout(40)
    void supplementalRestSkipsDuplicateAndUnlinkedEndpoints() throws Exception {
        for (Set<String> linked : List.of(Set.<String> of(), Set.of("sessionstart", "dipswitchinterpretation"),
                Set.of("sessionconsumption"))) {
            boolean sessionsExpected = linked.contains("sessionstart") || linked.contains("sessionconsumption");
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
                    if (!sessionsExpected && status == ThingStatus.ONLINE && onlineEvents.incrementAndGet() == 2) {
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
            KeContactRestHandler rest = new KeContactRestHandler(thing,
                    new Configuration(Map.of("ipAddress", "192.0.2.1", "restEnabled", true, "username", "admin",
                            "password", "test-password", "refreshIntervalSlow", 3600, "verifyCertificate", true)),
                    listener) {
                @Override
                protected JsonObject request(String path, String method, @Nullable String body, boolean authenticated) {
                    requests.add(path);
                    if (path.startsWith("/sessions?")) {
                        completed.countDown();
                    }
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
                assertEquals(OnOffType.ON, restStates.get("restoutput"));
                assertFalse(requests.contains("/configs/lmgmt/"));
                assertEquals(linked.contains("dipswitchinterpretation"),
                        requests.contains("/wallboxes/dipswitch/12345"));
                assertEquals(sessionsExpected, requests.stream().anyMatch(path -> path.startsWith("/sessions?")));
            } finally {
                rest.dispose();
            }
        }
    }

    @Test
    @Timeout(40)
    void restOperationalPollingAdaptsWithoutSpeedingUpSupplementalRequests() throws Exception {
        AtomicLong clock = new AtomicLong();
        AtomicBoolean primary = new AtomicBoolean(true);
        AtomicBoolean failWallbox = new AtomicBoolean();
        BlockingQueue<String> requests = new LinkedBlockingQueue<>();
        BlockingQueue<ThingStatus> statuses = new LinkedBlockingQueue<>();
        Thing thing = ThingBuilder
                .create(new ThingTypeUID("keba", "kecontact"), new ThingUID("keba:kecontact:restpolling")).build();
        KeContactProtocolHandler.Listener listener = new KeContactProtocolHandler.Listener() {
            @Override
            public void stateUpdated(String channel, State state) {
            }

            @Override
            public void statusUpdated(ThingStatus status, ThingStatusDetail detail, @Nullable String description) {
                statuses.add(status);
            }

            @Override
            public void propertiesUpdated(Map<String, String> properties) {
            }

            @Override
            public boolean isLinked(String channel) {
                return "sessionstart".equals(channel) || "dipswitchinterpretation".equals(channel);
            }

            @Override
            public boolean isPrimary() {
                return primary.get();
            }
        };
        KeContactRestHandler rest = new KeContactRestHandler(thing, new Configuration(Map.of("ipAddress", "192.0.2.1",
                "restEnabled", true, "password", "test-password", "refreshInterval", 10, "refreshIntervalSlow", 60)),
                listener) {
            @Override
            protected long pollingTime() {
                return clock.get();
            }

            @Override
            protected JsonObject request(String path, String method, @Nullable String body, boolean authenticated) {
                if ("/jwt/login".equals(path)) {
                    return JsonParser.parseString("{\"accessToken\":\"test-token\"}").getAsJsonObject();
                }
                if ("/serialnumber".equals(path)) {
                    return JsonParser.parseString("{\"value\":\"12345\"}").getAsJsonObject();
                }
                requests.add(path);
                if ("/wallboxes/12345".equals(path) && failWallbox.get()) {
                    throw new IllegalStateException("Test failure");
                }
                return new JsonObject();
            }
        };
        try {
            rest.initialize();
            assertEquals("/wallboxes/12345", requests.poll(5, TimeUnit.SECONDS));
            assertEquals("/wallboxes/dipswitch/12345", requests.poll(5, TimeUnit.SECONDS));
            assertTrue(Objects.requireNonNull(requests.poll(5, TimeUnit.SECONDS)).startsWith("/sessions?"));
            assertNull(requests.poll(1200, TimeUnit.MILLISECONDS));

            clock.set(TimeUnit.SECONDS.toNanos(10));
            assertEquals("/wallboxes/12345", requests.poll(5, TimeUnit.SECONDS));
            assertNull(requests.poll(1200, TimeUnit.MILLISECONDS));

            primary.set(false);
            clock.set(TimeUnit.SECONDS.toNanos(20));
            assertNull(requests.poll(1200, TimeUnit.MILLISECONDS));

            clock.set(TimeUnit.SECONDS.toNanos(60));
            assertEquals("/wallboxes/dipswitch/12345", requests.poll(5, TimeUnit.SECONDS));
            assertTrue(Objects.requireNonNull(requests.poll(5, TimeUnit.SECONDS)).startsWith("/sessions?"));
            assertNull(requests.poll(1200, TimeUnit.MILLISECONDS));

            clock.set(TimeUnit.SECONDS.toNanos(70));
            assertEquals("/wallboxes/12345", requests.poll(5, TimeUnit.SECONDS));
            primary.set(true);
            clock.set(TimeUnit.SECONDS.toNanos(80));
            assertEquals("/wallboxes/12345", requests.poll(5, TimeUnit.SECONDS));
            assertNull(requests.poll(1200, TimeUnit.MILLISECONDS));

            statuses.clear();
            failWallbox.set(true);
            clock.set(TimeUnit.SECONDS.toNanos(90));
            assertEquals("/wallboxes/12345", requests.poll(5, TimeUnit.SECONDS));
            assertEquals(ThingStatus.OFFLINE, statuses.poll(5, TimeUnit.SECONDS));
            primary.set(false);
            clock.set(TimeUnit.SECONDS.toNanos(120));
            assertEquals("/wallboxes/dipswitch/12345", requests.poll(5, TimeUnit.SECONDS));
            assertTrue(Objects.requireNonNull(requests.poll(5, TimeUnit.SECONDS)).startsWith("/sessions?"));
            assertNull(statuses.poll(1200, TimeUnit.MILLISECONDS));
            failWallbox.set(false);
            primary.set(true);
            clock.set(TimeUnit.SECONDS.toNanos(130));
            assertEquals("/wallboxes/12345", requests.poll(5, TimeUnit.SECONDS));
            assertEquals(ThingStatus.ONLINE, statuses.poll(5, TimeUnit.SECONDS));
            rest.dispose();
            clock.set(TimeUnit.SECONDS.toNanos(200));
            assertNull(requests.poll(1200, TimeUnit.MILLISECONDS));
        } finally {
            rest.dispose();
        }
    }

    @Test
    @Timeout(20)
    void restPhaseTogglePostsOnlyOnAndResetsAfterSuccess() throws Exception {
        Thing thing = ThingBuilder
                .create(new ThingTypeUID("keba", "kecontact"), new ThingUID("keba:kecontact:resttoggle")).build();
        CountDownLatch ready = new CountDownLatch(1);
        AtomicBoolean failToggle = new AtomicBoolean();
        BlockingQueue<String> posts = new LinkedBlockingQueue<>();
        KeContactProtocolHandler.Listener listener = Objects
                .requireNonNull(mock(KeContactProtocolHandler.Listener.class));
        doAnswer(invocation -> {
            ready.countDown();
            return null;
        }).when(listener).statusUpdated(eq(ThingStatus.ONLINE), any(), any());
        KeContactRestHandler rest = new KeContactRestHandler(thing,
                new Configuration(Map.of("ipAddress", "192.0.2.1", "restEnabled", true, "password", "test-password",
                        "refreshInterval", 3600, "refreshIntervalSlow", 3600)),
                listener) {
            @Override
            protected JsonObject request(String path, String method, @Nullable String body, boolean authenticated) {
                if ("/jwt/login".equals(path)) {
                    return JsonParser.parseString("{\"accessToken\":\"test-token\"}").getAsJsonObject();
                }
                if ("/serialnumber".equals(path)) {
                    return JsonParser.parseString("{\"value\":\"12345\"}").getAsJsonObject();
                }
                if ("POST".equals(method)) {
                    assertNull(body);
                    assertTrue(authenticated);
                    posts.add(path);
                    if ("/wallboxes/12345/phase-toggle".equals(path) && failToggle.get()) {
                        throw new IllegalStateException("Test failure");
                    }
                    assertTrue(List.of("/wallboxes/12345/phase-toggle", "/wallboxes/12345/unlock").contains(path));
                }
                return new JsonObject();
            }
        };
        ChannelUID toggle = new ChannelUID(thing.getUID(), "togglephaseswitch");
        ChannelUID unlock = new ChannelUID(thing.getUID(), "unlockplug");
        try {
            rest.initialize();
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            rest.handleCommand(toggle, OnOffType.OFF);
            rest.handleCommand(toggle, new DecimalType(1));
            rest.handleCommand(new ChannelUID(thing.getUID(), "triggerphaseswitch"), OnOffType.ON);
            assertTrue(posts.isEmpty());
            rest.handleCommand(toggle, OnOffType.ON);
            assertEquals("/wallboxes/12345/phase-toggle", posts.poll(5, TimeUnit.SECONDS));
            verify(listener).stateUpdated("togglephaseswitch", OnOffType.OFF);

            clearInvocations(listener);
            rest.handleCommand(unlock, OnOffType.ON);
            assertEquals("/wallboxes/12345/unlock", posts.poll(5, TimeUnit.SECONDS));
            verify(listener).stateUpdated("unlockplug", OnOffType.OFF);

            clearInvocations(listener);
            failToggle.set(true);
            rest.handleCommand(toggle, OnOffType.ON);
            assertEquals("/wallboxes/12345/phase-toggle", posts.poll(5, TimeUnit.SECONDS));
            verify(listener, never()).stateUpdated("togglephaseswitch", OnOffType.OFF);
        } finally {
            rest.dispose();
        }
    }

    @Test
    @Timeout(20)
    void restOnlyThingExposesRoutesAndRemovesPhaseToggle() throws Exception {
        ThingUID uid = new ThingUID("keba:kecontact:restonlytoggle");
        Thing thing = ThingBuilder.create(new ThingTypeUID("keba", "kecontact"), uid)
                .withConfiguration(new Configuration(Map.of("ipAddress", "192.0.2.1", "modbusEnabled", false,
                        "udpEnabled", false, "restEnabled", true, "password", "test-password")))
                .build();
        List<ChannelDefinition> definitions = new ArrayList<>();
        for (String id : List.of("phaseswitchstate", "togglephaseswitch", "unlockplug", "stop")) {
            ChannelDefinition definition = Objects.requireNonNull(mock(ChannelDefinition.class));
            when(definition.getId()).thenReturn(id);
            when(definition.getChannelTypeUID()).thenReturn(new ChannelTypeUID("keba", "togglephaseswitch".equals(id)
                    ? "command"
                    : "unlockplug".equals(id) ? "unlock-plug" : "stop".equals(id) ? "command" : "phase-switch-state"));
            when(definition.getProperties()).thenReturn(Map.of());
            definitions.add(definition);
        }
        ThingType type = Objects.requireNonNull(mock(ThingType.class));
        when(type.getChannelDefinitions()).thenReturn(definitions);
        ThingTypeRegistry registry = Objects.requireNonNull(mock(ThingTypeRegistry.class));
        when(registry.getThingType(new ThingTypeUID("keba", "kecontact"))).thenReturn(type);
        ThingHandlerCallback callback = Objects.requireNonNull(mock(ThingHandlerCallback.class));
        when(callback.createChannelBuilder(any(ChannelUID.class), any(ChannelTypeUID.class))).thenAnswer(invocation -> {
            ChannelUID channelUID = invocation.getArgument(0);
            ChannelTypeUID typeUID = invocation.getArgument(1);
            String itemType = new ChannelTypeUID("keba", "command").equals(typeUID) ? "Switch" : "Number";
            return ChannelBuilder.create(channelUID, itemType).withType(typeUID);
        });
        List<KeContactProtocolHandler.Listener> listeners = new ArrayList<>();
        try (var adapters = mockConstruction(KeContactRestHandler.class, (adapter, context) -> {
            KeContactProtocolHandler.Listener listener = (KeContactProtocolHandler.Listener) Objects
                    .requireNonNull(context.arguments().get(2));
            listeners.add(listener);
            doAnswer(invocation -> {
                listener.statusUpdated(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
                return null;
            }).when(adapter).initialize();
        })) {
            ModbusManager manager = Objects.requireNonNull(mock(ModbusManager.class));
            KeContactTransceiver transceiver = Objects.requireNonNull(mock(KeContactTransceiver.class));
            KeContactCombinedHandler handler = new KeContactCombinedHandler(thing, manager, transceiver, registry);
            handler.setCallback(callback);
            ChannelUID toggle = new ChannelUID(uid, "togglephaseswitch");
            ChannelUID unlock = new ChannelUID(uid, "unlockplug");
            ChannelUID stop = new ChannelUID(uid, "stop");
            try {
                handler.initialize();
                var channel = Objects.requireNonNull(handler.getThing().getChannel(toggle));
                assertEquals("Switch", channel.getAcceptedItemType());
                assertEquals(new ChannelTypeUID("keba", "command"), channel.getChannelTypeUID());
                assertNull(handler.getThing().getChannel("triggerphaseswitch"));
                var phaseState = Objects.requireNonNull(handler.getThing().getChannel("phaseswitchstate"));
                assertEquals("Number", phaseState.getAcceptedItemType());
                assertEquals(new ChannelTypeUID("keba", "phase-switch-state-readonly"), phaseState.getChannelTypeUID());
                assertNotNull(handler.getThing().getChannel(unlock));
                assertNotNull(handler.getThing().getChannel(stop));
                assertNull(handler.getThing().getChannel(new ChannelUID(uid, "unlock")));
                KeContactRestHandler rest = adapters.constructed().get(0);
                handler.handleCommand(phaseState.getUID(), new DecimalType(3));
                verify(rest, after(250).never()).handleCommand(eq(phaseState.getUID()), any());
                handler.handleCommand(toggle, OnOffType.ON);
                verify(rest, timeout(5000)).handleCommand(toggle, OnOffType.ON);
                listeners.get(0).stateUpdated("togglephaseswitch", OnOffType.OFF);
                verify(callback).stateUpdated(toggle, OnOffType.OFF);
                assertEquals(Protocol.REST, KeContactCombinedHandler.sourceFor("togglephaseswitch", true, true));
                assertEquals(Protocol.MODBUS, KeContactCombinedHandler.sourceFor("unlockplug", true, true));
                assertEquals(Protocol.REST, KeContactCombinedHandler.sourceFor("unlockplug", false, true));
                assertEquals(Protocol.UDP, KeContactCombinedHandler.sourceFor("unlockplug", false, false));
                verifyNoInteractions(manager, transceiver);

                handler.handleCommand(unlock, OnOffType.ON);
                verify(rest, timeout(5000)).handleCommand(unlock, OnOffType.ON);
                listeners.get(0).stateUpdated("unlockplug", OnOffType.OFF);
                verify(callback).stateUpdated(unlock, OnOffType.OFF);

                handler.handleCommand(stop, OnOffType.ON);
                verify(rest, timeout(5000)).handleCommand(stop, OnOffType.ON);

                ModbusCommunicationInterface comms = Objects.requireNonNull(mock(ModbusCommunicationInterface.class));
                when(manager.newModbusCommunicationInterface(any(), any())).thenReturn(comms);
                when(comms.submitOneTimePoll(any(), any(), any()))
                        .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(null));
                handler.handleConfigurationUpdate(Map.of("modbusEnabled", true));
                assertEquals(new ChannelTypeUID("keba", "phase-switch-state"),
                        handler.getThing().getChannel(phaseState.getUID()).getChannelTypeUID());
                handler.handleConfigurationUpdate(Map.of("modbusEnabled", false));
                assertEquals(new ChannelTypeUID("keba", "phase-switch-state-readonly"),
                        handler.getThing().getChannel(phaseState.getUID()).getChannelTypeUID());
                handler.handleConfigurationUpdate(Map.of("udpEnabled", true, "udpDisplayOnly", true));
                assertEquals(new ChannelTypeUID("keba", "phase-switch-state-readonly"),
                        handler.getThing().getChannel(phaseState.getUID()).getChannelTypeUID());
                handler.handleConfigurationUpdate(Map.of("restEnabled", false));
                assertNull(handler.getThing().getChannel(toggle));
                verify(rest).dispose();
            } finally {
                handler.dispose();
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
        for (String series : List.of("0", "1", "2", "3", "A")) {
            assertTrue(KeContactCombinedHandler.restUnsupportedForProduct("KC-P30-123456" + series + "-XXX"));
        }
        assertFalse(KeContactCombinedHandler.restUnsupportedForProduct("KC-P30-123456B-XXX"));
        assertFalse(KeContactCombinedHandler.restUnsupportedForProduct("KC-P30-123456C-XXX"));
        assertFalse(KeContactCombinedHandler.restUnsupportedForProduct("KC-P30-123456Z-XXX"));
        assertFalse(KeContactCombinedHandler.restUnsupportedForProduct("P30"));
        assertFalse(KeContactCombinedHandler.restUnsupportedForProduct("P40"));
    }

    @Test
    @Timeout(20)
    void stopsModbusAndMarksP20UnsupportedWhenModelIsIdentified() throws Exception {
        ThingUID uid = new ThingUID("keba:kecontact:p20-modbus");
        Thing thing = ThingBuilder.create(new ThingTypeUID("keba", "kecontact"), uid)
                .withConfiguration(
                        new Configuration(Map.of("ipAddress", "192.0.2.1", "udpEnabled", false, "restEnabled", false)))
                .build();
        ModbusManager manager = Objects.requireNonNull(mock(ModbusManager.class));
        ModbusCommunicationInterface comms = Objects.requireNonNull(mock(ModbusCommunicationInterface.class));
        when(manager.newModbusCommunicationInterface(any(), any())).thenReturn(comms);
        KeContactCombinedHandler handler = new KeContactCombinedHandler(thing, manager,
                Objects.requireNonNull(mock(KeContactTransceiver.class)));
        handler.setCallback(Objects.requireNonNull(mock(ThingHandlerCallback.class)));
        try {
            handler.initialize();
            verify(manager, timeout(5000)).newModbusCommunicationInterface(any(), any());

            handler.updateProperties(Map.of("model", "P20"));

            verify(comms, timeout(5000)).close();
            assertEquals("unsupported", handler.getThing().getProperties().get("modbusAvailable"));
        } finally {
            handler.dispose();
        }
    }

    @Test
    @Timeout(20)
    void persistedP20IdentityDoesNotSuppressModbusProbeForNewSession() throws Exception {
        ThingUID uid = new ThingUID("keba:kecontact:persisted-p20-new-endpoint");
        Thing thing = ThingBuilder.create(new ThingTypeUID("keba", "kecontact"), uid)
                .withConfiguration(
                        new Configuration(Map.of("ipAddress", "192.0.2.2", "udpEnabled", false, "restEnabled", false)))
                .withProperties(Map.of("model", "P20", "modbusModel", "P20")).build();
        ModbusManager manager = Objects.requireNonNull(mock(ModbusManager.class));
        ModbusCommunicationInterface comms = Objects.requireNonNull(mock(ModbusCommunicationInterface.class));
        when(manager.newModbusCommunicationInterface(any(), any())).thenReturn(comms);
        KeContactCombinedHandler handler = new KeContactCombinedHandler(thing, manager,
                Objects.requireNonNull(mock(KeContactTransceiver.class)));
        handler.setCallback(Objects.requireNonNull(mock(ThingHandlerCallback.class)));
        try {
            handler.initialize();

            verify(manager, timeout(5000)).newModbusCommunicationInterface(any(), any());
            assertEquals("unknown", handler.getThing().getProperties().get("modbusAvailable"));

            handler.updateProperties(Map.of("model", "P40"));

            verify(comms, never()).close();
            assertEquals("unknown", handler.getThing().getProperties().get("modbusAvailable"));
        } finally {
            handler.dispose();
        }
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
    void configurationDerivesRestUrlFromSharedAddressAndPort() {
        KeContactCombinedConfiguration config = new KeContactCombinedConfiguration();
        assertTrue(config.modbusEnabled);
        assertTrue(config.udpEnabled);
        assertFalse(config.udpDisplayOnly);
        assertFalse(config.restEnabled);
        assertEquals(8443, config.restPort);
        config.ipAddress = "192.0.2.1";
        assertEquals("192.0.2.1", config.getModbusAddress());
        assertEquals("https://192.0.2.1:8443", config.getRestBaseUrl());
        config.modbusIpAddress = "192.0.2.2";
        assertEquals("192.0.2.2", config.getModbusAddress());
        assertEquals("https://192.0.2.1:8443", config.getRestBaseUrl());
        config.restPort = 443;
        config.ipAddress = "wallbox.example";
        assertEquals("https://wallbox.example:443", config.getRestBaseUrl());
        for (String host : List.of("2001:db8::1", "[2001:db8::1]")) {
            config.ipAddress = host;
            assertEquals("https://[2001:db8::1]:443", config.getRestBaseUrl());
        }
        for (String host : List.of("https://192.0.2.1", "user@192.0.2.1", "192.0.2.1/path")) {
            config.ipAddress = host;
            assertThrows(IllegalArgumentException.class, config::getRestBaseUrl);
        }
    }

    @Test
    void invalidRestEndpointStopsBeforeStartingProtocols() {
        for (Map<String, Object> endpoint : List.<Map<String, Object>> of(Map.of("restPort", 0),
                Map.of("restPort", 65536), Map.of("ipAddress", "https://192.0.2.1"), Map.of("ipAddress", ""))) {
            Map<String, Object> configuration = new HashMap<>(
                    Map.of("ipAddress", "192.0.2.1", "restEnabled", true, "password", "test-password"));
            configuration.putAll(endpoint);
            Thing thing = ThingBuilder
                    .create(new ThingTypeUID("keba", "kecontact"), new ThingUID("keba:kecontact:invalidendpoint"))
                    .withConfiguration(new Configuration(configuration)).build();
            ModbusManager manager = Objects.requireNonNull(mock(ModbusManager.class));
            KeContactTransceiver transceiver = Objects.requireNonNull(mock(KeContactTransceiver.class));
            ThingHandlerCallback callback = Objects.requireNonNull(mock(ThingHandlerCallback.class));
            KeContactCombinedHandler handler = new KeContactCombinedHandler(thing, manager, transceiver);
            handler.setCallback(callback);
            try {
                handler.initialize();
                verify(callback).statusUpdated(eq(thing),
                        argThat(status -> status.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR));
                verifyNoInteractions(manager, transceiver);
            } finally {
                handler.dispose();
            }
        }
    }

    @Test
    void disabledRestDoesNotProbeEvenWithCredentials() {
        for (Map<String, Object> restSettings : List.<Map<String, Object>> of(Map.of(), Map.of("restEnabled", false))) {
            Map<String, Object> configuration = new HashMap<>(restSettings);
            configuration.putAll(Map.of("ipAddress", "192.0.2.1", "udpEnabled", false, "password", "test-password"));
            Thing thing = ThingBuilder
                    .create(new ThingTypeUID("keba", "kecontact"), new ThingUID("keba:kecontact:norest"))
                    .withConfiguration(new Configuration(configuration)).build();
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
    void enabledRestRequiresCredentialsBeforeStartingProtocols() {
        Thing thing = ThingBuilder
                .create(new ThingTypeUID("keba", "kecontact"), new ThingUID("keba:kecontact:restcredentials"))
                .withConfiguration(new Configuration(Map.of("ipAddress", "192.0.2.1", "restEnabled", true))).build();
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
    void protocolsShareWallboxAddressExceptForModbusProxy() throws Exception {
        for (String proxyAddress : List.of("192.0.2.2", "", "   ")) {
            Thing thing = ThingBuilder
                    .create(new ThingTypeUID("keba", "kecontact"), new ThingUID("keba:kecontact:addresses"))
                    .withConfiguration(
                            new Configuration(Map.of("ipAddress", "192.0.2.1", "modbusIpAddress", proxyAddress)))
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
                assertEquals("192.0.2.1", udpHosts.poll(30, TimeUnit.SECONDS));
                assertEquals(proxyAddress.isBlank() ? "192.0.2.1" : proxyAddress,
                        modbusHosts.poll(30, TimeUnit.SECONDS));
            } finally {
                handler.dispose();
            }
        }
    }

    @Test
    @Timeout(80)
    void reidentifiesModbusModelForEachNewEndpoint() throws Exception {
        ThingUID uid = new ThingUID("keba:kecontact:modelchanges");
        List<ChannelDefinition> definitions = new ArrayList<>();
        for (String id : List.of("state", "fastchargingstatus", "activatefastcharging")) {
            ChannelDefinition definition = Objects.requireNonNull(mock(ChannelDefinition.class));
            when(definition.getId()).thenReturn(id);
            when(definition.getChannelTypeUID()).thenReturn(new ChannelTypeUID("keba", id));
            when(definition.getProperties()).thenReturn(Map.of());
            definitions.add(definition);
        }
        ThingType thingType = Objects.requireNonNull(mock(ThingType.class));
        when(thingType.getChannelDefinitions()).thenReturn(definitions);
        ThingTypeRegistry registry = Objects.requireNonNull(mock(ThingTypeRegistry.class));
        when(registry.getThingType(new ThingTypeUID("keba", "kecontact"))).thenReturn(thingType);
        Thing thing = ThingBuilder.create(new ThingTypeUID("keba", "kecontact"), uid)
                .withConfiguration(new Configuration(Map.of("ipAddress", "192.0.2.1", "udpEnabled", false)))
                .withProperties(Map.of("modbusModel", "P30", "model", "P30")).build();
        ModbusManager manager = Objects.requireNonNull(mock(ModbusManager.class));
        List<ModbusCommunicationInterface> interfaces = List.of(
                Objects.requireNonNull(mock(ModbusCommunicationInterface.class)),
                Objects.requireNonNull(mock(ModbusCommunicationInterface.class)),
                Objects.requireNonNull(mock(ModbusCommunicationInterface.class)));
        when(manager.newModbusCommunicationInterface(any(), any())).thenReturn(interfaces.get(0), interfaces.get(1),
                interfaces.get(2));
        BlockingQueue<Read> identifications = new LinkedBlockingQueue<>();
        List<List<Integer>> pollAddresses = List.of(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        for (int interfaceIndex = 0; interfaceIndex < interfaces.size(); interfaceIndex++) {
            ModbusCommunicationInterface comms = interfaces.get(interfaceIndex);
            List<Integer> addresses = pollAddresses.get(interfaceIndex);
            when(comms.submitOneTimePoll(any(), any(), any())).thenAnswer(invocation -> {
                ModbusReadRequestBlueprint request = invocation.getArgument(0);
                if (request.getReference() == 1016) {
                    identifications.add(new Read(request, invocation.getArgument(1)));
                }
                return java.util.concurrent.CompletableFuture.completedFuture(null);
            });
            when(comms.registerRegularPoll(any(), anyLong(), anyLong(), any(), any())).thenAnswer(invocation -> {
                ModbusReadRequestBlueprint request = invocation.getArgument(0);
                addresses.add(request.getReference());
                return Objects.requireNonNull(mock(PollTask.class));
            });
        }
        ThingHandlerCallback callback = Objects.requireNonNull(mock(ThingHandlerCallback.class));
        when(callback.isChannelLinked(any())).thenReturn(true);
        when(callback.createChannelBuilder(any(ChannelUID.class), any(ChannelTypeUID.class))).thenAnswer(invocation -> {
            ChannelUID channelUID = invocation.getArgument(0);
            ChannelTypeUID channelTypeUID = invocation.getArgument(1);
            return ChannelBuilder.create(channelUID, "Number").withType(channelTypeUID);
        });
        KeContactCombinedHandler handler = new KeContactCombinedHandler(thing, manager,
                Objects.requireNonNull(mock(KeContactTransceiver.class)), registry);
        handler.setCallback(callback);
        try {
            handler.initialize();
            Read first = Objects.requireNonNull(identifications.poll(5, TimeUnit.SECONDS));
            assertTrue(pollAddresses.get(0).isEmpty());
            first.respond(304111);
            assertEquals("P30", handler.getThing().getProperties().get("modbusModel"));
            assertFalse(pollAddresses.get(0).contains(1200));
            assertNull(handler.getThing().getChannel("fastchargingstatus"));

            handler.handleConfigurationUpdate(Map.of("ipAddress", "192.0.2.2"));
            Read second = Objects.requireNonNull(identifications.poll(5, TimeUnit.SECONDS));
            verify(interfaces.get(0)).close();
            assertTrue(pollAddresses.get(1).isEmpty());
            second.respond(4212311);
            assertEquals("P40", handler.getThing().getProperties().get("modbusModel"));
            assertTrue(pollAddresses.get(1).contains(1200));
            assertNotNull(handler.getThing().getChannel("fastchargingstatus"));
            assertNotNull(handler.getThing().getChannel("activatefastcharging"));
            first.respond(304111);
            assertEquals("P40", handler.getThing().getProperties().get("modbusModel"));

            handler.handleConfigurationUpdate(Map.of("ipAddress", "192.0.2.3"));
            Read third = Objects.requireNonNull(identifications.poll(5, TimeUnit.SECONDS));
            verify(interfaces.get(1)).close();
            assertTrue(pollAddresses.get(2).isEmpty());
            third.respond(304111);
            assertEquals("P30", handler.getThing().getProperties().get("modbusModel"));
            assertFalse(pollAddresses.get(2).contains(1200));
            assertNull(handler.getThing().getChannel("fastchargingstatus"));
            assertNull(handler.getThing().getChannel("activatefastcharging"));
        } finally {
            handler.dispose();
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
