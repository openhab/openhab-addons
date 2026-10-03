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
package org.openhab.binding.modbus.tests;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openhab.binding.modbus.handler.ModbusPoller2ThingHandler;
import org.openhab.binding.modbus.handler.ModbusPollerThingHandler;
import org.openhab.binding.modbus.internal.ModbusBindingConstantsInternal;
import org.openhab.binding.modbus.internal.handler.ModbusDataThingHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.modbus.AsyncModbusFailure;
import org.openhab.core.io.transport.modbus.AsyncModbusReadResult;
import org.openhab.core.io.transport.modbus.AsyncModbusWriteResult;
import org.openhab.core.io.transport.modbus.BitArray;
import org.openhab.core.io.transport.modbus.ModbusConstants;
import org.openhab.core.io.transport.modbus.ModbusFailureCallback;
import org.openhab.core.io.transport.modbus.ModbusReadCallback;
import org.openhab.core.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.io.transport.modbus.PollTask;
import org.openhab.core.io.transport.modbus.endpoint.ModbusSlaveEndpoint;
import org.openhab.core.io.transport.modbus.endpoint.ModbusTCPSlaveEndpoint;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sami Salonen - Initial contribution
 */
public class ModbusPollerThingHandlerTest extends AbstractModbusOSGiTest {

    private static final String HOST = "thisishost";
    private static final int PORT = 44;

    private final Logger logger = LoggerFactory.getLogger(ModbusPollerThingHandlerTest.class);

    private Bridge endpoint;
    private Bridge poller;

    private @Mock ThingHandlerCallback thingCallback;

    public static BridgeBuilder createTcpThingBuilder(String id) {
        return BridgeBuilder
                .create(ModbusBindingConstantsInternal.THING_TYPE_MODBUS_TCP,
                        new ThingUID(ModbusBindingConstantsInternal.THING_TYPE_MODBUS_TCP, id))
                .withLabel("label for " + id);
    }

    public static BridgeBuilder createPollerThingBuilder(String id) {
        return BridgeBuilder
                .create(ModbusBindingConstantsInternal.THING_TYPE_MODBUS_POLLER,
                        new ThingUID(ModbusBindingConstantsInternal.THING_TYPE_MODBUS_POLLER, id))
                .withLabel("label for " + id);
    }

    public static BridgeBuilder createPoller2ThingBuilder(String id) {
        return BridgeBuilder
                .create(ModbusBindingConstantsInternal.THING_TYPE_MODBUS_POLLER2,
                        new ThingUID(ModbusBindingConstantsInternal.THING_TYPE_MODBUS_POLLER2, id))
                .withLabel("label for " + id);
    }

    /**
     * Verify that basic poller <-> endpoint interaction has taken place (on poller init)
     */
    private void verifyEndpointBasicInitInteraction() {
        verify(mockedModbusManager).newModbusCommunicationInterface(any(), any());
    }

    public ModbusReadCallback getPollerCallback(ModbusPollerThingHandler handler)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field callbackField = ModbusPollerThingHandler.class.getDeclaredField("callbackDelegator");
        callbackField.setAccessible(true);
        return (ModbusReadCallback) callbackField.get(handler);
    }

    public ModbusFailureCallback<ModbusReadRequestBlueprint> getPollerFailureCallback(ModbusPollerThingHandler handler)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field callbackField = ModbusPollerThingHandler.class.getDeclaredField("callbackDelegator");
        callbackField.setAccessible(true);
        return (ModbusFailureCallback<ModbusReadRequestBlueprint>) callbackField.get(handler);
    }

    /**
     * Before each test, setup TCP endpoint thing, configure mocked item registry
     */
    @BeforeEach
    public void setUp() {
        mockCommsToModbusManager();
        Configuration tcpConfig = new Configuration();
        tcpConfig.put("host", HOST);
        tcpConfig.put("port", PORT);
        tcpConfig.put("id", 9);
        endpoint = createTcpThingBuilder("tcpendpoint").withConfiguration(tcpConfig).build();
        addThing(endpoint);

        assertThat(endpoint.getStatus(), is(equalTo(ThingStatus.ONLINE)));
    }

    @AfterEach
    public void tearDown() {
        if (endpoint != null) {
            thingProvider.remove(endpoint.getUID());
        }
        if (poller != null) {
            thingProvider.remove(poller.getUID());
        }
    }

    @Test
    public void testInitializeNonPolling()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 0L); // 0 -> non polling
        pollerConfig.put("start", 5);
        pollerConfig.put("length", 9);
        pollerConfig.put("type", ModbusBindingConstantsInternal.READ_TYPE_HOLDING_REGISTER);
        poller = createPollerThingBuilder("poller").withConfiguration(pollerConfig).withBridge(endpoint.getUID())
                .build();

        logger.info("Poller created, registering to registry...");
        addThing(poller);
        assertThat(poller.getStatus(), is(equalTo(ThingStatus.ONLINE)));
        logger.info("Poller registered");

        verifyEndpointBasicInitInteraction();
        // polling is _not_ setup
        verifyNoMoreInteractions(mockedModbusManager);
    }

    @Test
    public void testPoller2UpdatesItsConfiguredChannelFromPollResult()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 0L);
        pollerConfig.put("start", 100);
        pollerConfig.put("length", 2);
        pollerConfig.put("type", ModbusBindingConstantsInternal.READ_TYPE_HOLDING_REGISTER);
        ThingUID pollerUid = new ThingUID(ModbusBindingConstantsInternal.THING_TYPE_MODBUS_POLLER2, "poller2");
        ChannelUID channelUid = new ChannelUID(pollerUid, "value");
        Configuration channelConfig = new Configuration(Map.of("address", "101", "valueType", "uint16"));
        poller = createPoller2ThingBuilder("poller2").withConfiguration(pollerConfig).withBridge(endpoint.getUID())
                .withChannel(ChannelBuilder.create(channelUid, "Number").withConfiguration(channelConfig).build())
                .build();
        addThing(poller);

        ModbusPoller2ThingHandler handler = (ModbusPoller2ThingHandler) poller.getHandler();
        handler.setCallback(thingCallback);
        ModbusReadCallback callback = getPollerCallback(handler);
        callback.handle(new AsyncModbusReadResult(Mockito.mock(ModbusReadRequestBlueprint.class),
                new ModbusRegisterArray(0x0001, 0x002A)));

        verify(thingCallback).stateUpdated(channelUid, new org.openhab.core.library.types.DecimalType("42"));
    }

    @Test
    public void testPoller2UpdatesRawChannelFromPollResult()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 0L);
        pollerConfig.put("start", 100);
        pollerConfig.put("length", 2);
        pollerConfig.put("type", ModbusBindingConstantsInternal.READ_TYPE_HOLDING_REGISTER);
        ThingUID pollerUid = new ThingUID(ModbusBindingConstantsInternal.THING_TYPE_MODBUS_POLLER2, "raw-poller2");
        ChannelUID channelUid = new ChannelUID(pollerUid, "raw");
        Configuration channelConfig = new Configuration(Map.of("address", "100", "valueType", "raw", "length", 2));
        poller = createPoller2ThingBuilder("raw-poller2").withConfiguration(pollerConfig).withBridge(endpoint.getUID())
                .withChannel(ChannelBuilder.create(channelUid, "String").withConfiguration(channelConfig).build())
                .build();
        addThing(poller);

        ModbusPoller2ThingHandler handler = (ModbusPoller2ThingHandler) poller.getHandler();
        handler.setCallback(thingCallback);
        getPollerCallback(handler).handle(new AsyncModbusReadResult(Mockito.mock(ModbusReadRequestBlueprint.class),
                new ModbusRegisterArray(0x0001, 0x002A)));

        verify(thingCallback).stateUpdated(channelUid, new org.openhab.core.library.types.StringType("0001002A"));
    }

    @Test
    public void testPoller2AssignsGeneratedChannelTypeToRuntimeStringChannel() {
        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 0L);
        pollerConfig.put("start", 100);
        pollerConfig.put("length", 2);
        pollerConfig.put("type", ModbusBindingConstantsInternal.READ_TYPE_HOLDING_REGISTER);
        ThingUID pollerUid = new ThingUID(ModbusBindingConstantsInternal.THING_TYPE_MODBUS_POLLER2,
                "typed-raw-poller2");
        ChannelUID channelUid = new ChannelUID(pollerUid, "raw");
        Configuration channelConfig = new Configuration(Map.of("address", "100", "valueType", "raw", "length", 2));
        poller = createPoller2ThingBuilder("typed-raw-poller2").withConfiguration(pollerConfig)
                .withBridge(endpoint.getUID())
                .withChannel(ChannelBuilder.create(channelUid, "String").withConfiguration(channelConfig).build())
                .build();

        addThing(poller);

        waitForAssert(() -> assertThat(poller.getChannel("raw").getChannelTypeUID(),
                is(equalTo(new ChannelTypeUID("modbus", "poller2-shim-string")))));
        assertThat(poller.getChannel("raw").getAcceptedItemType(), is(equalTo("String")));
        assertThat(poller.getChannel("raw").getConfiguration().get("length"), is(equalTo(2)));
    }

    @Test
    public void testPoller2DispatchesTransformedHoldingWriteWithoutOptimisticStateUpdate() {
        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 0L);
        pollerConfig.put("start", 100);
        pollerConfig.put("length", 2);
        pollerConfig.put("type", ModbusBindingConstantsInternal.READ_TYPE_HOLDING_REGISTER);
        ThingUID pollerUid = new ThingUID(ModbusBindingConstantsInternal.THING_TYPE_MODBUS_POLLER2, "write-poller2");
        ChannelUID channelUid = new ChannelUID(pollerUid, "setpoint");
        Configuration channelConfig = new Configuration(Map.of("address", "100", "valueType", "uint16", "writeStart",
                "42", "writeValueType", "int16", "writeTransform", "17", "writeMaxTries", 2));
        poller = createPoller2ThingBuilder("write-poller2").withConfiguration(pollerConfig)
                .withBridge(endpoint.getUID())
                .withChannel(ChannelBuilder.create(channelUid, "Number").withConfiguration(channelConfig).build())
                .build();
        addThing(poller);

        ModbusPoller2ThingHandler handler = (ModbusPoller2ThingHandler) poller.getHandler();
        handler.setCallback(thingCallback);
        handler.handleCommand(channelUid, new org.openhab.core.library.types.DecimalType(4));

        ArgumentCaptor<org.openhab.core.io.transport.modbus.ModbusWriteRequestBlueprint> requests = ArgumentCaptor
                .forClass(org.openhab.core.io.transport.modbus.ModbusWriteRequestBlueprint.class);
        ArgumentCaptor<org.openhab.core.io.transport.modbus.ModbusWriteCallback> successfulWrites = ArgumentCaptor
                .forClass(org.openhab.core.io.transport.modbus.ModbusWriteCallback.class);
        verify(comms).submitOneTimeWrite(requests.capture(), successfulWrites.capture(), any());
        org.openhab.core.io.transport.modbus.ModbusWriteRegisterRequestBlueprint request = (org.openhab.core.io.transport.modbus.ModbusWriteRegisterRequestBlueprint) requests
                .getValue();
        assertThat(request.getReference(), is(equalTo(42)));
        assertThat(request.getRegisters().getRegister(0), is(equalTo(17)));
        assertThat(request.getMaxTries(), is(equalTo(2)));
        verifyNoInteractions(thingCallback);

        successfulWrites.getValue().handle(Mockito.mock(AsyncModbusWriteResult.class));
        verify(comms).submitOneTimePoll(any(), any(), any());
    }

    @Test
    public void testPoller2DoesNotWriteWhenTransformOutputCannotBeParsed() {
        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 0L);
        pollerConfig.put("start", 100);
        pollerConfig.put("length", 1);
        pollerConfig.put("type", ModbusBindingConstantsInternal.READ_TYPE_HOLDING_REGISTER);
        ThingUID pollerUid = new ThingUID(ModbusBindingConstantsInternal.THING_TYPE_MODBUS_POLLER2,
                "invalid-write-poller2");
        ChannelUID channelUid = new ChannelUID(pollerUid, "setpoint");
        Configuration channelConfig = new Configuration(Map.of("address", "100", "valueType", "uint16", "writeStart",
                "42", "writeValueType", "int16", "writeTransform", "not-a-command"));
        poller = createPoller2ThingBuilder("invalid-write-poller2").withConfiguration(pollerConfig)
                .withBridge(endpoint.getUID())
                .withChannel(ChannelBuilder.create(channelUid, "Number").withConfiguration(channelConfig).build())
                .build();
        addThing(poller);

        ((ModbusPoller2ThingHandler) poller.getHandler()).handleCommand(channelUid,
                new org.openhab.core.library.types.DecimalType(4));

        verify(comms, never()).submitOneTimeWrite(any(), any(), any());
    }

    @Test
    public void testPoller2WritesMaintainedCoil() {
        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 0L);
        pollerConfig.put("start", 100);
        pollerConfig.put("length", 1);
        pollerConfig.put("type", ModbusBindingConstantsInternal.READ_TYPE_COIL);
        ThingUID pollerUid = new ThingUID(ModbusBindingConstantsInternal.THING_TYPE_MODBUS_POLLER2,
                "coil-write-poller2");
        ChannelUID channelUid = new ChannelUID(pollerUid, "output");
        Configuration channelConfig = new Configuration(Map.of("address", "100", "writeStart", "7"));
        poller = createPoller2ThingBuilder("coil-write-poller2").withConfiguration(pollerConfig)
                .withBridge(endpoint.getUID())
                .withChannel(ChannelBuilder.create(channelUid, "Switch").withConfiguration(channelConfig).build())
                .build();
        addThing(poller);

        ((ModbusPoller2ThingHandler) poller.getHandler()).handleCommand(channelUid,
                org.openhab.core.library.types.OnOffType.ON);

        ArgumentCaptor<org.openhab.core.io.transport.modbus.ModbusWriteRequestBlueprint> requests = ArgumentCaptor
                .forClass(org.openhab.core.io.transport.modbus.ModbusWriteRequestBlueprint.class);
        verify(comms).submitOneTimeWrite(requests.capture(), any(), any());
        org.openhab.core.io.transport.modbus.ModbusWriteCoilRequestBlueprint request = (org.openhab.core.io.transport.modbus.ModbusWriteCoilRequestBlueprint) requests
                .getValue();
        assertThat(request.getReference(), is(equalTo(7)));
        assertThat(request.getCoils().getBit(0), is(equalTo(true)));
    }

    @Test
    public void testPoller2ReportsTransportFailureFromWrite() {
        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 0L);
        pollerConfig.put("start", 100);
        pollerConfig.put("length", 1);
        pollerConfig.put("type", ModbusBindingConstantsInternal.READ_TYPE_HOLDING_REGISTER);
        ThingUID pollerUid = new ThingUID(ModbusBindingConstantsInternal.THING_TYPE_MODBUS_POLLER2,
                "failing-write-poller2");
        ChannelUID channelUid = new ChannelUID(pollerUid, "setpoint");
        Configuration channelConfig = new Configuration(
                Map.of("address", "100", "valueType", "uint16", "writeStart", "42", "writeValueType", "int16"));
        poller = createPoller2ThingBuilder("failing-write-poller2").withConfiguration(pollerConfig)
                .withBridge(endpoint.getUID())
                .withChannel(ChannelBuilder.create(channelUid, "Number").withConfiguration(channelConfig).build())
                .build();
        addThing(poller);

        ((ModbusPoller2ThingHandler) poller.getHandler()).handleCommand(channelUid,
                new org.openhab.core.library.types.DecimalType(4));
        ArgumentCaptor<org.openhab.core.io.transport.modbus.ModbusFailureCallback> failures = ArgumentCaptor
                .forClass(org.openhab.core.io.transport.modbus.ModbusFailureCallback.class);
        verify(comms).submitOneTimeWrite(any(), any(), failures.capture());
        failures.getValue()
                .handle(new AsyncModbusFailure<>(
                        Mockito.mock(org.openhab.core.io.transport.modbus.ModbusWriteRequestBlueprint.class),
                        new RuntimeException("transport down")));

        assertThat(poller.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
        assertThat(poller.getStatusInfo().getStatusDetail(), is(equalTo(ThingStatusDetail.COMMUNICATION_ERROR)));
    }

    @Test
    public void testPoller2RejectsNumericChannelForCoilPoller() {
        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 0L);
        pollerConfig.put("start", 100);
        pollerConfig.put("length", 2);
        pollerConfig.put("type", ModbusBindingConstantsInternal.READ_TYPE_COIL);
        ThingUID pollerUid = new ThingUID(ModbusBindingConstantsInternal.THING_TYPE_MODBUS_POLLER2, "coil-poller2");
        ChannelUID channelUid = new ChannelUID(pollerUid, "value");
        Configuration channelConfig = new Configuration(Map.of("address", "100", "valueType", "uint16"));
        poller = createPoller2ThingBuilder("coil-poller2").withConfiguration(pollerConfig).withBridge(endpoint.getUID())
                .withChannel(ChannelBuilder.create(channelUid, "Number").withConfiguration(channelConfig).build())
                .build();
        addThing(poller);

        assertThat(poller.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
        assertThat(poller.getStatusInfo().getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
        assertThat(poller.getStatusInfo().getDescription(),
                is(equalTo("Channel 'value' has invalid valueType 'uint16'; coils and discrete inputs are bits")));
    }

    @Test
    public void testPoller2UpdatesDefaultBitChannelFromCoilResult()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 0L);
        pollerConfig.put("start", 100);
        pollerConfig.put("length", 2);
        pollerConfig.put("type", ModbusBindingConstantsInternal.READ_TYPE_COIL);
        ThingUID pollerUid = new ThingUID(ModbusBindingConstantsInternal.THING_TYPE_MODBUS_POLLER2, "coil-poller2");
        ChannelUID channelUid = new ChannelUID(pollerUid, "value");
        Configuration channelConfig = new Configuration(Map.of("address", "101"));
        poller = createPoller2ThingBuilder("coil-poller2").withConfiguration(pollerConfig).withBridge(endpoint.getUID())
                .withChannel(ChannelBuilder.create(channelUid, "Switch").withConfiguration(channelConfig).build())
                .build();
        addThing(poller);

        ModbusPoller2ThingHandler handler = (ModbusPoller2ThingHandler) poller.getHandler();
        handler.setCallback(thingCallback);
        ModbusReadCallback callback = getPollerCallback(handler);
        callback.handle(
                new AsyncModbusReadResult(Mockito.mock(ModbusReadRequestBlueprint.class), new BitArray(false, true)));

        verify(thingCallback).stateUpdated(channelUid, new org.openhab.core.library.types.DecimalType("1"));
    }

    @Test
    public void testPoller2ReportsEveryInvalidChannelConfiguration() {
        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 0L);
        pollerConfig.put("start", 100);
        pollerConfig.put("length", 2);
        pollerConfig.put("type", ModbusBindingConstantsInternal.READ_TYPE_HOLDING_REGISTER);
        ThingUID pollerUid = new ThingUID(ModbusBindingConstantsInternal.THING_TYPE_MODBUS_POLLER2, "invalid-poller2");
        poller = createPoller2ThingBuilder("invalid-poller2").withConfiguration(pollerConfig)
                .withBridge(endpoint.getUID())
                .withChannel(ChannelBuilder.create(new ChannelUID(pollerUid, "first"), "Number")
                        .withConfiguration(new Configuration(Map.of("address", "102", "valueType", "uint16"))).build())
                .withChannel(ChannelBuilder.create(new ChannelUID(pollerUid, "second"), "Number")
                        .withConfiguration(new Configuration(Map.of("address", "100.1", "valueType", "uint16")))
                        .build())
                .build();
        addThing(poller);

        assertThat(poller.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
        assertThat(poller.getStatusInfo().getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
        assertThat(poller.getStatusInfo().getDescription(),
                is(equalTo("Channel 'first': Address 102 is outside poll window 100..101\n"
                        + "Channel 'second': Sub-address 1 is invalid for value type uint16")));
    }

    private void testPollerLengthCheck(String type, int length, boolean expectedOnline) {
        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 0L);
        pollerConfig.put("start", 5);
        pollerConfig.put("length", length);
        pollerConfig.put("type", type);
        poller = createPollerThingBuilder("poller").withConfiguration(pollerConfig).withBridge(endpoint.getUID())
                .build();

        addThing(poller);
        assertThat(poller.getStatus(), is(equalTo(expectedOnline ? ThingStatus.ONLINE : ThingStatus.OFFLINE)));
        if (!expectedOnline) {
            assertThat(poller.getStatusInfo().getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
        }

        verifyEndpointBasicInitInteraction();
        verifyNoMoreInteractions(mockedModbusManager);
    }

    @Test
    public void testPollerWithMaxRegisters()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        testPollerLengthCheck(ModbusBindingConstantsInternal.READ_TYPE_HOLDING_REGISTER,
                ModbusConstants.MAX_REGISTERS_READ_COUNT, true);
    }

    @Test
    public void testPollerLengthOutOfBoundsWithRegisters()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        testPollerLengthCheck(ModbusBindingConstantsInternal.READ_TYPE_HOLDING_REGISTER,
                ModbusConstants.MAX_REGISTERS_READ_COUNT + 1, false);
    }

    @Test
    public void testPollerWithMaxInputRegisters()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        testPollerLengthCheck(ModbusBindingConstantsInternal.READ_TYPE_INPUT_REGISTER,
                ModbusConstants.MAX_REGISTERS_READ_COUNT, true);
    }

    @Test
    public void testPollerLengthOutOfBoundsWithInputRegisters()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        testPollerLengthCheck(ModbusBindingConstantsInternal.READ_TYPE_INPUT_REGISTER,
                ModbusConstants.MAX_REGISTERS_READ_COUNT + 1, false);
    }

    @Test
    public void testPollerWithMaxCoils()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        testPollerLengthCheck(ModbusBindingConstantsInternal.READ_TYPE_COIL, ModbusConstants.MAX_BITS_READ_COUNT, true);
    }

    @Test
    public void testPollerLengthOutOfBoundsWithCoils()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        testPollerLengthCheck(ModbusBindingConstantsInternal.READ_TYPE_COIL, ModbusConstants.MAX_BITS_READ_COUNT + 1,
                false);
    }

    @Test
    public void testPollerWithMaxDiscreteInput()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        testPollerLengthCheck(ModbusBindingConstantsInternal.READ_TYPE_DISCRETE_INPUT,
                ModbusConstants.MAX_BITS_READ_COUNT, true);
    }

    @Test
    public void testPollerLengthOutOfBoundsWithDiscreteInput()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        testPollerLengthCheck(ModbusBindingConstantsInternal.READ_TYPE_DISCRETE_INPUT,
                ModbusConstants.MAX_BITS_READ_COUNT + 1, false);
    }

    public void testPollingGeneric(String type, ModbusReadFunctionCode expectedFunctionCode)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        PollTask pollTask = Mockito.mock(PollTask.class);
        doReturn(pollTask).when(comms).registerRegularPoll(notNull(), eq(150l), eq(0L), notNull(), notNull());

        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 150L);
        pollerConfig.put("start", 5);
        pollerConfig.put("length", 13);
        pollerConfig.put("type", type);
        poller = createPollerThingBuilder("poller").withConfiguration(pollerConfig).withBridge(endpoint.getUID())
                .build();
        addThing(poller);

        assertThat(poller.getStatusInfo().toString(), poller.getStatus(), is(equalTo(ThingStatus.ONLINE)));

        verifyEndpointBasicInitInteraction();
        // this test has been disabled as it fails since openhab/openhab-core#4584
        // TODO apadt this test to hamcrest 4
        /*
         * verify(mockedModbusManager).newModbusCommunicationInterface(argThat(new TypeSafeMatcher<>() {
         * 
         * @Override
         * public void describeTo(Description description) {
         * description.appendText("correct endpoint (");
         * }
         * 
         * @Override
         * protected boolean matchesSafely(ModbusSlaveEndpoint endpoint) {
         * return checkEndpoint(endpoint);
         * }
         * }), any());
         */
        /*
         * verify(comms).registerRegularPoll(argThat(new TypeSafeMatcher<>() {
         * 
         * @Override
         * public void describeTo(Description description) {
         * description.appendText("correct request");
         * }
         * 
         * @Override
         * protected boolean matchesSafely(ModbusReadRequestBlueprint request) {
         * return checkRequest(request, expectedFunctionCode);
         * }
         * }), eq(150l), eq(0L), notNull(), notNull());
         */
        verifyNoMoreInteractions(mockedModbusManager);
    }

    @SuppressWarnings("null")
    private boolean checkEndpoint(ModbusSlaveEndpoint endpointParam) {
        return endpointParam.equals(new ModbusTCPSlaveEndpoint(HOST, PORT, false));
    }

    private boolean checkRequest(ModbusReadRequestBlueprint request, ModbusReadFunctionCode functionCode) {
        return request.getDataLength() == 13 && request.getFunctionCode() == functionCode
                && request.getProtocolID() == 0 && request.getReference() == 5 && request.getUnitID() == 9;
    }

    @Test
    public void testInitializePollingWithCoils()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        testPollingGeneric("coil", ModbusReadFunctionCode.READ_COILS);
    }

    @Test
    public void testInitializePollingWithDiscrete()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        testPollingGeneric("discrete", ModbusReadFunctionCode.READ_INPUT_DISCRETES);
    }

    @Test
    public void testInitializePollingWithInputRegisters()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        testPollingGeneric("input", ModbusReadFunctionCode.READ_INPUT_REGISTERS);
    }

    @Test
    public void testInitializePollingWithHoldingRegisters()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        testPollingGeneric("holding", ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS);
    }

    @Test
    public void testPollUnregistrationOnDispose()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        PollTask pollTask = Mockito.mock(PollTask.class);
        doReturn(pollTask).when(comms).registerRegularPoll(notNull(), eq(150l), eq(0L), notNull(), notNull());

        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 150L);
        pollerConfig.put("start", 5);
        pollerConfig.put("length", 13);
        pollerConfig.put("type", "coil");
        poller = createPollerThingBuilder("poller").withConfiguration(pollerConfig).withBridge(endpoint.getUID())
                .build();
        addThing(poller);
        verifyEndpointBasicInitInteraction();

        // verify registration
        final AtomicReference<ModbusReadCallback> callbackRef = new AtomicReference<>();
        // this test has been disabled as it fails since openhab/openhab-core#4584
        // TODO apadt this test to hamcrest 4
        /*
         * verify(mockedModbusManager).newModbusCommunicationInterface(argThat(new TypeSafeMatcher<>() {
         * 
         * @Override
         * public void describeTo(Description description) {
         * description.appendText("correct endpoint");
         * }
         * 
         * @Override
         * protected boolean matchesSafely(ModbusSlaveEndpoint endpoint) {
         * return checkEndpoint(endpoint);
         * }
         * }), any());
         */
        /*
         * verify(comms).registerRegularPoll(argThat(new TypeSafeMatcher<>() {
         * 
         * @Override
         * public void describeTo(Description description) {
         * }
         * 
         * @Override
         * protected boolean matchesSafely(ModbusReadRequestBlueprint request) {
         * return checkRequest(request, ModbusReadFunctionCode.READ_COILS);
         * }
         * }), eq(150l), eq(0L), argThat(new TypeSafeMatcher<>() {
         * 
         * @Override
         * public void describeTo(Description description) {
         * }
         * 
         * @Override
         * protected boolean matchesSafely(ModbusReadCallback callback) {
         * callbackRef.set(callback);
         * return true;
         * }
         * }), notNull());
         */ verifyNoMoreInteractions(mockedModbusManager);

        // reset call counts for easy assertions
        reset(mockedModbusManager);

        // remove the thing
        disposeThing(poller);

        // 1) should first unregister poll task
        verify(comms).unregisterRegularPoll(eq(pollTask));

        verifyNoMoreInteractions(mockedModbusManager);
    }

    @Test
    public void testInitializeWithOfflineBridge()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 150L);
        pollerConfig.put("start", 5);
        pollerConfig.put("length", 13);
        pollerConfig.put("type", "coil");

        endpoint.setStatusInfo(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, ""));
        poller = createPollerThingBuilder("poller").withConfiguration(pollerConfig).withBridge(endpoint.getUID())
                .build();
        addThing(poller);
        verifyEndpointBasicInitInteraction();

        assertThat(poller.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
        assertThat(poller.getStatusInfo().getStatusDetail(), is(equalTo(ThingStatusDetail.BRIDGE_OFFLINE)));

        verifyNoMoreInteractions(mockedModbusManager);
    }

    @Test
    public void testRegistersPassedToChildDataThings()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        PollTask pollTask = Mockito.mock(PollTask.class);
        doReturn(pollTask).when(comms).registerRegularPoll(notNull(), eq(150l), eq(0L), notNull(), notNull());

        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 150L);
        pollerConfig.put("start", 5);
        pollerConfig.put("length", 13);
        pollerConfig.put("type", "coil");
        poller = createPollerThingBuilder("poller").withConfiguration(pollerConfig).withBridge(endpoint.getUID())
                .build();
        addThing(poller);
        verifyEndpointBasicInitInteraction();

        assertThat(poller.getStatus(), is(equalTo(ThingStatus.ONLINE)));

        ArgumentCaptor<ModbusReadCallback> callbackCapturer = ArgumentCaptor.forClass(ModbusReadCallback.class);
        verify(comms).registerRegularPoll(notNull(), eq(150l), eq(0L), callbackCapturer.capture(), notNull());
        ModbusReadCallback readCallback = callbackCapturer.getValue();

        assertNotNull(readCallback);

        ModbusReadRequestBlueprint request = Mockito.mock(ModbusReadRequestBlueprint.class);
        ModbusRegisterArray registers = Mockito.mock(ModbusRegisterArray.class);

        ModbusPollerThingHandler thingHandler = (ModbusPollerThingHandler) poller.getHandler();
        assertNotNull(thingHandler);

        ModbusDataThingHandler child1 = Mockito.mock(ModbusDataThingHandler.class);
        ModbusDataThingHandler child2 = Mockito.mock(ModbusDataThingHandler.class);

        AsyncModbusReadResult result = new AsyncModbusReadResult(request, registers);

        // has one data child
        thingHandler.childHandlerInitialized(child1, Mockito.mock(Thing.class));
        readCallback.handle(result);
        verify(child1).onReadResult(result);
        verifyNoMoreInteractions(child1);
        verifyNoMoreInteractions(child2);

        reset(child1);

        // two children (one child initialized)
        thingHandler.childHandlerInitialized(child2, Mockito.mock(Thing.class));
        readCallback.handle(result);
        verify(child1).onReadResult(result);
        verify(child2).onReadResult(result);
        verifyNoMoreInteractions(child1);
        verifyNoMoreInteractions(child2);

        reset(child1);
        reset(child2);

        // one child disposed
        thingHandler.childHandlerDisposed(child1, Mockito.mock(Thing.class));
        readCallback.handle(result);
        verify(child2).onReadResult(result);
        verifyNoMoreInteractions(child1);
        verifyNoMoreInteractions(child2);
    }

    @Test
    public void testBitsPassedToChildDataThings()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        PollTask pollTask = Mockito.mock(PollTask.class);
        doReturn(pollTask).when(comms).registerRegularPoll(notNull(), eq(150l), eq(0L), notNull(), notNull());

        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 150L);
        pollerConfig.put("start", 5);
        pollerConfig.put("length", 13);
        pollerConfig.put("type", "coil");
        poller = createPollerThingBuilder("poller").withConfiguration(pollerConfig).withBridge(endpoint.getUID())
                .build();
        addThing(poller);
        verifyEndpointBasicInitInteraction();

        assertThat(poller.getStatus(), is(equalTo(ThingStatus.ONLINE)));

        ArgumentCaptor<ModbusReadCallback> callbackCapturer = ArgumentCaptor.forClass(ModbusReadCallback.class);
        verify(comms).registerRegularPoll(any(), eq(150l), eq(0L), callbackCapturer.capture(), notNull());
        ModbusReadCallback readCallback = callbackCapturer.getValue();

        assertNotNull(readCallback);

        ModbusReadRequestBlueprint request = Mockito.mock(ModbusReadRequestBlueprint.class);
        BitArray bits = Mockito.mock(BitArray.class);

        ModbusPollerThingHandler thingHandler = (ModbusPollerThingHandler) poller.getHandler();
        assertNotNull(thingHandler);

        ModbusDataThingHandler child1 = Mockito.mock(ModbusDataThingHandler.class);
        ModbusDataThingHandler child2 = Mockito.mock(ModbusDataThingHandler.class);

        AsyncModbusReadResult result = new AsyncModbusReadResult(request, bits);

        // has one data child
        thingHandler.childHandlerInitialized(child1, Mockito.mock(Thing.class));
        readCallback.handle(result);
        verify(child1).onReadResult(result);
        verifyNoMoreInteractions(child1);
        verifyNoMoreInteractions(child2);

        reset(child1);

        // two children (one child initialized)
        thingHandler.childHandlerInitialized(child2, Mockito.mock(Thing.class));
        readCallback.handle(result);
        verify(child1).onReadResult(result);
        verify(child2).onReadResult(result);
        verifyNoMoreInteractions(child1);
        verifyNoMoreInteractions(child2);

        reset(child1);
        reset(child2);

        // one child disposed
        thingHandler.childHandlerDisposed(child1, Mockito.mock(Thing.class));
        readCallback.handle(result);
        verify(child2).onReadResult(result);
        verifyNoMoreInteractions(child1);
        verifyNoMoreInteractions(child2);
    }

    @Test
    public void testErrorPassedToChildDataThings()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        PollTask pollTask = Mockito.mock(PollTask.class);
        doReturn(pollTask).when(comms).registerRegularPoll(notNull(), eq(150l), eq(0L), notNull(), notNull());

        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 150L);
        pollerConfig.put("start", 5);
        pollerConfig.put("length", 13);
        pollerConfig.put("type", "coil");
        poller = createPollerThingBuilder("poller").withConfiguration(pollerConfig).withBridge(endpoint.getUID())
                .build();
        addThing(poller);
        verifyEndpointBasicInitInteraction();

        assertThat(poller.getStatus(), is(equalTo(ThingStatus.ONLINE)));

        final ArgumentCaptor<ModbusFailureCallback<ModbusReadRequestBlueprint>> callbackCapturer = ArgumentCaptor
                .forClass((Class) ModbusFailureCallback.class);
        verify(comms).registerRegularPoll(any(), eq(150l), eq(0L), notNull(), callbackCapturer.capture());
        ModbusFailureCallback<ModbusReadRequestBlueprint> readCallback = callbackCapturer.getValue();

        assertNotNull(readCallback);

        ModbusReadRequestBlueprint request = Mockito.mock(ModbusReadRequestBlueprint.class);
        Exception error = Mockito.mock(Exception.class);

        ModbusPollerThingHandler thingHandler = (ModbusPollerThingHandler) poller.getHandler();
        assertNotNull(thingHandler);

        ModbusDataThingHandler child1 = Mockito.mock(ModbusDataThingHandler.class);
        ModbusDataThingHandler child2 = Mockito.mock(ModbusDataThingHandler.class);

        AsyncModbusFailure<ModbusReadRequestBlueprint> result = new AsyncModbusFailure<>(request, error);

        // has one data child
        thingHandler.childHandlerInitialized(child1, Mockito.mock(Thing.class));
        readCallback.handle(result);
        verify(child1).handleReadError(result);
        verifyNoMoreInteractions(child1);
        verifyNoMoreInteractions(child2);

        reset(child1);

        // two children (one child initialized)
        thingHandler.childHandlerInitialized(child2, Mockito.mock(Thing.class));
        readCallback.handle(result);
        verify(child1).handleReadError(result);
        verify(child2).handleReadError(result);
        verifyNoMoreInteractions(child1);
        verifyNoMoreInteractions(child2);

        reset(child1);
        reset(child2);

        // one child disposed
        thingHandler.childHandlerDisposed(child1, Mockito.mock(Thing.class));
        readCallback.handle(result);
        verify(child2).handleReadError(result);
        verifyNoMoreInteractions(child1);
        verifyNoMoreInteractions(child2);
    }

    @Test
    public void testRefresh()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 0L);
        pollerConfig.put("start", 5);
        pollerConfig.put("length", 13);
        pollerConfig.put("type", "coil");
        poller = createPollerThingBuilder("poller").withConfiguration(pollerConfig).withBridge(endpoint.getUID())
                .build();
        addThing(poller);
        verifyEndpointBasicInitInteraction();

        assertThat(poller.getStatus(), is(equalTo(ThingStatus.ONLINE)));

        verify(comms, never()).submitOneTimePoll(any(), any(), any());
        ModbusPollerThingHandler thingHandler = (ModbusPollerThingHandler) poller.getHandler();
        assertNotNull(thingHandler);
        thingHandler.refresh();
        verify(comms).submitOneTimePoll(any(), any(), any());
    }

    /**
     * When there's no recently received data, refresh() will re-use that instead
     *
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     * @throws SecurityException
     */
    @Test
    public void testRefreshWithPreviousData()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 0L);
        pollerConfig.put("start", 5);
        pollerConfig.put("length", 13);
        pollerConfig.put("type", "coil");
        pollerConfig.put("cacheMillis", 10000L);
        poller = createPollerThingBuilder("poller").withConfiguration(pollerConfig).withBridge(endpoint.getUID())
                .build();
        addThing(poller);
        verifyEndpointBasicInitInteraction();

        ModbusDataThingHandler child1 = Mockito.mock(ModbusDataThingHandler.class);
        ModbusPollerThingHandler thingHandler = (ModbusPollerThingHandler) poller.getHandler();
        assertNotNull(thingHandler);
        thingHandler.childHandlerInitialized(child1, Mockito.mock(Thing.class));

        assertThat(poller.getStatus(), is(equalTo(ThingStatus.ONLINE)));

        verify(comms, never()).submitOneTimePoll(any(), any(), any());

        // data is received
        ModbusReadCallback pollerReadCallback = getPollerCallback(thingHandler);
        ModbusReadRequestBlueprint request = Mockito.mock(ModbusReadRequestBlueprint.class);
        ModbusRegisterArray registers = Mockito.mock(ModbusRegisterArray.class);
        AsyncModbusReadResult result = new AsyncModbusReadResult(request, registers);
        pollerReadCallback.handle(result);

        // data child receives the data
        verify(child1).onReadResult(result);
        verifyNoMoreInteractions(child1);
        reset(child1);

        // call refresh
        // cache is still valid, we should not have real data poll this time
        thingHandler.refresh();
        verify(comms, never()).submitOneTimePoll(any(), any(), any());

        // data child receives the cached data
        verify(child1).onReadResult(result);
        verifyNoMoreInteractions(child1);
    }

    /**
     * When there's no recently received data, refresh() will re-use that instead
     *
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     * @throws SecurityException
     */
    @Test
    public void testRefreshWithPreviousDataCacheDisabled()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 0L);
        pollerConfig.put("start", 5);
        pollerConfig.put("length", 13);
        pollerConfig.put("type", "coil");
        pollerConfig.put("cacheMillis", 0L);
        poller = createPollerThingBuilder("poller").withConfiguration(pollerConfig).withBridge(endpoint.getUID())
                .build();
        addThing(poller);
        verifyEndpointBasicInitInteraction();

        ModbusPollerThingHandler thingHandler = (ModbusPollerThingHandler) poller.getHandler();
        assertNotNull(thingHandler);
        ModbusDataThingHandler child1 = Mockito.mock(ModbusDataThingHandler.class);
        thingHandler.childHandlerInitialized(child1, Mockito.mock(Thing.class));

        assertThat(poller.getStatus(), is(equalTo(ThingStatus.ONLINE)));

        verify(comms, never()).submitOneTimePoll(any(), any(), any());

        // data is received
        ModbusReadCallback pollerReadCallback = getPollerCallback(thingHandler);
        ModbusReadRequestBlueprint request = Mockito.mock(ModbusReadRequestBlueprint.class);
        ModbusRegisterArray registers = Mockito.mock(ModbusRegisterArray.class);
        AsyncModbusReadResult result = new AsyncModbusReadResult(request, registers);

        pollerReadCallback.handle(result);

        // data child receives the data
        verify(child1).onReadResult(result);
        verifyNoMoreInteractions(child1);
        reset(child1);

        // call refresh
        // caching disabled, should poll from manager
        thingHandler.refresh();
        verify(comms).submitOneTimePoll(any(), any(), any());
        verifyNoMoreInteractions(mockedModbusManager);

        // data child receives the cached data
        verifyNoMoreInteractions(child1);
    }

    /**
     * Testing again caching, such that most recently received data is propagated to children
     *
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws InterruptedException
     */
    @Test
    public void testRefreshWithPreviousData2() throws IllegalArgumentException, IllegalAccessException,
            NoSuchFieldException, SecurityException, InterruptedException {
        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 0L);
        pollerConfig.put("start", 5);
        pollerConfig.put("length", 13);
        pollerConfig.put("type", "coil");
        pollerConfig.put("cacheMillis", 10000L);
        poller = createPollerThingBuilder("poller").withConfiguration(pollerConfig).withBridge(endpoint.getUID())
                .build();
        addThing(poller);
        verifyEndpointBasicInitInteraction();

        ModbusPollerThingHandler thingHandler = (ModbusPollerThingHandler) poller.getHandler();
        assertNotNull(thingHandler);
        ModbusDataThingHandler child1 = Mockito.mock(ModbusDataThingHandler.class);
        thingHandler.childHandlerInitialized(child1, Mockito.mock(Thing.class));

        assertThat(poller.getStatus(), is(equalTo(ThingStatus.ONLINE)));

        verify(comms, never()).submitOneTimePoll(any(), any(), any());

        // data is received
        ModbusReadCallback pollerReadCallback = getPollerCallback(thingHandler);
        ModbusFailureCallback<ModbusReadRequestBlueprint> failureCallback = getPollerFailureCallback(thingHandler);
        ModbusReadRequestBlueprint request = Mockito.mock(ModbusReadRequestBlueprint.class);
        ModbusReadRequestBlueprint request2 = Mockito.mock(ModbusReadRequestBlueprint.class);
        ModbusRegisterArray registers = Mockito.mock(ModbusRegisterArray.class);
        Exception error = Mockito.mock(Exception.class);
        AsyncModbusReadResult registersResult = new AsyncModbusReadResult(request, registers);
        AsyncModbusFailure<ModbusReadRequestBlueprint> errorResult = new AsyncModbusFailure<>(request2, error);

        pollerReadCallback.handle(registersResult);

        // data child should receive the data
        verify(child1).onReadResult(registersResult);
        verifyNoMoreInteractions(child1);
        reset(child1);

        // Sleep to have time between the data
        Thread.sleep(5L);

        // error is received
        failureCallback.handle(errorResult);

        // data child should receive the error
        verify(child1).handleReadError(errorResult);
        verifyNoMoreInteractions(child1);
        reset(child1);

        // call refresh, should return latest data (that is, error)
        // cache is still valid, we should not have real data poll this time
        thingHandler.refresh();
        verify(comms, never()).submitOneTimePoll(any(), any(), any());

        // data child receives the cached error
        verify(child1).handleReadError(errorResult);
        verifyNoMoreInteractions(child1);
    }

    @Test
    public void testRefreshWithOldPreviousData() throws IllegalArgumentException, IllegalAccessException,
            NoSuchFieldException, SecurityException, InterruptedException {
        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 0L);
        pollerConfig.put("start", 5);
        pollerConfig.put("length", 13);
        pollerConfig.put("type", "coil");
        pollerConfig.put("cacheMillis", 10L);
        poller = createPollerThingBuilder("poller").withConfiguration(pollerConfig).withBridge(endpoint.getUID())
                .build();
        addThing(poller);
        verifyEndpointBasicInitInteraction();

        ModbusPollerThingHandler thingHandler = (ModbusPollerThingHandler) poller.getHandler();
        assertNotNull(thingHandler);
        ModbusDataThingHandler child1 = Mockito.mock(ModbusDataThingHandler.class);
        thingHandler.childHandlerInitialized(child1, Mockito.mock(Thing.class));

        assertThat(poller.getStatus(), is(equalTo(ThingStatus.ONLINE)));

        verify(comms, never()).submitOneTimePoll(any(), any(), any());

        // data is received
        ModbusReadCallback pollerReadCallback = getPollerCallback(thingHandler);
        ModbusReadRequestBlueprint request = Mockito.mock(ModbusReadRequestBlueprint.class);
        ModbusRegisterArray registers = Mockito.mock(ModbusRegisterArray.class);
        AsyncModbusReadResult result = new AsyncModbusReadResult(request, registers);

        pollerReadCallback.handle(result);

        // data child should receive the data
        verify(child1).onReadResult(result);
        verifyNoMoreInteractions(child1);
        reset(child1);

        // Sleep to ensure cache expiry
        Thread.sleep(15L);

        // call refresh. Since cache expired, will poll for more
        verify(comms, never()).submitOneTimePoll(any(), any(), any());
        thingHandler.refresh();
        verify(comms).submitOneTimePoll(any(), any(), any());
    }
}
