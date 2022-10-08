/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.binding.modbus.internal.ModbusBindingConstantsInternal.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.openhab.binding.modbus.handler.EndpointNotInitializedException;
import org.openhab.binding.modbus.handler.ModbusPollerThingHandler;
import org.openhab.binding.modbus.internal.ModbusBindingConstantsInternal;
import org.openhab.binding.modbus.internal.handler.ModbusDataThingHandler;
import org.openhab.binding.modbus.internal.handler.ModbusTcpThingHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.modbus.AsyncModbusFailure;
import org.openhab.core.io.transport.modbus.AsyncModbusReadResult;
import org.openhab.core.io.transport.modbus.AsyncModbusWriteResult;
import org.openhab.core.io.transport.modbus.BitArray;
import org.openhab.core.io.transport.modbus.ModbusConstants;
import org.openhab.core.io.transport.modbus.ModbusConstants.ValueType;
import org.openhab.core.io.transport.modbus.ModbusReadCallback;
import org.openhab.core.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.io.transport.modbus.ModbusResponse;
import org.openhab.core.io.transport.modbus.ModbusWriteCoilRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusWriteFunctionCode;
import org.openhab.core.io.transport.modbus.ModbusWriteRegisterRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusWriteRequestBlueprint;
import org.openhab.core.io.transport.modbus.PollTask;
import org.openhab.core.io.transport.modbus.endpoint.ModbusSlaveEndpoint;
import org.openhab.core.io.transport.modbus.endpoint.ModbusTCPSlaveEndpoint;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.Item;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;

/**
 * @author Sami Salonen - Initial contribution
 */
public class ModbusDataHandlerTest extends AbstractModbusOSGiTest {

    private final class MultiplyTransformation implements TransformationService {
        @Override
        public String transform(String function, String source) throws TransformationException {
            return String.valueOf(Integer.parseInt(function) * Integer.parseInt(source));
        }
    }

    private static final String HOST = "thisishost";
    private static final int PORT = 44;

    private static final Map<String, String> CHANNEL_TO_ACCEPTED_TYPE = new HashMap<>();
    static {
        CHANNEL_TO_ACCEPTED_TYPE.put(CHANNEL_SWITCH, "Switch");
        CHANNEL_TO_ACCEPTED_TYPE.put(CHANNEL_CONTACT, "Contact");
        CHANNEL_TO_ACCEPTED_TYPE.put(CHANNEL_DATETIME, "DateTime");
        CHANNEL_TO_ACCEPTED_TYPE.put(CHANNEL_DIMMER, "Dimmer");
        CHANNEL_TO_ACCEPTED_TYPE.put(CHANNEL_NUMBER, "Number");
        CHANNEL_TO_ACCEPTED_TYPE.put(CHANNEL_STRING, "String");
        CHANNEL_TO_ACCEPTED_TYPE.put(CHANNEL_ROLLERSHUTTER, "Rollershutter");
        CHANNEL_TO_ACCEPTED_TYPE.put(CHANNEL_LAST_READ_SUCCESS, "DateTime");
        CHANNEL_TO_ACCEPTED_TYPE.put(CHANNEL_LAST_WRITE_SUCCESS, "DateTime");
        CHANNEL_TO_ACCEPTED_TYPE.put(CHANNEL_LAST_WRITE_ERROR, "DateTime");
        CHANNEL_TO_ACCEPTED_TYPE.put(CHANNEL_LAST_READ_ERROR, "DateTime");
    }
    private List<ModbusWriteRequestBlueprint> writeRequests = new ArrayList<>();
    private Bridge realEndpointWithMockedComms;

    public ModbusReadCallback getPollerCallback(ModbusPollerThingHandler handler) {
        Field callbackField;
        try {
            callbackField = ModbusPollerThingHandler.class.getDeclaredField("callbackDelegator");
            callbackField.setAccessible(true);
            return (ModbusReadCallback) callbackField.get(handler);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            fail(e);
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    public void beforeEach() {
        mockCommsToModbusManager();
        Configuration tcpConfig = new Configuration();
        tcpConfig.put("host", HOST);
        tcpConfig.put("port", PORT);
        tcpConfig.put("id", 9);

        realEndpointWithMockedComms = BridgeBuilder
                .create(ModbusBindingConstantsInternal.THING_TYPE_MODBUS_TCP,
                        new ThingUID(ModbusBindingConstantsInternal.THING_TYPE_MODBUS_TCP, "mytcp"))
                .withLabel("label for mytcp").withConfiguration(tcpConfig).build();
        addThing(realEndpointWithMockedComms);
        assertEquals(ThingStatus.ONLINE, realEndpointWithMockedComms.getStatus(),
                realEndpointWithMockedComms.getStatusInfo().getDescription());
    }

    @AfterEach
    public void tearDown() {
        writeRequests.clear();
        if (realEndpointWithMockedComms != null) {
            thingProvider.remove(realEndpointWithMockedComms.getUID());
        }
    }

    private static Arguments appendArg(Arguments args, Object obj) {
        Object[] newArgs = Arrays.copyOf(args.get(), args.get().length + 1);
        newArgs[args.get().length] = obj;
        return Arguments.of(newArgs);
    }

    private void captureModbusWrites() {
        Mockito.when(comms.submitOneTimeWrite(any(), any(), any())).then(invocation -> {
            ModbusWriteRequestBlueprint task = (ModbusWriteRequestBlueprint) invocation.getArgument(0);
            writeRequests.add(task);
            return Mockito.mock(ScheduledFuture.class);
        });
    }

    private Bridge createPollerMock(String pollerId, PollTask task) {
        final Bridge poller;
        ThingUID thingUID = new ThingUID(THING_TYPE_MODBUS_POLLER, pollerId);
        BridgeBuilder builder = BridgeBuilder.create(THING_TYPE_MODBUS_POLLER, thingUID)
                .withLabel("label for " + pollerId);
        for (Entry<String, String> entry : CHANNEL_TO_ACCEPTED_TYPE.entrySet()) {
            String channelId = entry.getKey();
            String channelAcceptedType = entry.getValue();
            builder = builder.withChannel(
                    ChannelBuilder.create(new ChannelUID(thingUID, channelId), channelAcceptedType).build());
        }
        poller = builder.build();
        poller.setStatusInfo(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, ""));

        ModbusPollerThingHandler mockHandler = Mockito.mock(ModbusPollerThingHandler.class);
        doReturn(task.getRequest()).when(mockHandler).getRequest();
        assert comms != null;
        doReturn(comms).when(mockHandler).getCommunicationInterface();
        doReturn(task.getEndpoint()).when(comms).getEndpoint();
        poller.setHandler(mockHandler);
        assertSame(poller.getHandler(), mockHandler);
        assertSame(((ModbusPollerThingHandler) poller.getHandler()).getCommunicationInterface().getEndpoint(),
                task.getEndpoint());
        assertSame(((ModbusPollerThingHandler) poller.getHandler()).getRequest(), task.getRequest());

        addThing(poller);
        return poller;
    }

    private Bridge createTcpMock() {
        Bridge tcpBridge = ModbusPollerThingHandlerTest.createTcpThingBuilder("tcp1").build();
        ModbusTcpThingHandler tcpThingHandler = Mockito.mock(ModbusTcpThingHandler.class);
        tcpBridge.setStatusInfo(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, ""));
        tcpBridge.setHandler(tcpThingHandler);
        doReturn(comms).when(tcpThingHandler).getCommunicationInterface();
        try {
            doReturn(0).when(tcpThingHandler).getSlaveId();
        } catch (EndpointNotInitializedException e) {
            // not raised -- we are mocking return value only, not actually calling the method
            throw new IllegalStateException();
        }
        tcpThingHandler.initialize();
        assertThat(tcpBridge.getStatus(), is(equalTo(ThingStatus.ONLINE)));
        return tcpBridge;
    }

    private ModbusDataThingHandler createDataHandler(String id, Bridge bridge,
            Function<ThingBuilder, ThingBuilder> builderConfigurator) {
        return createDataHandler(id, bridge, builderConfigurator, null);
    }

    private ModbusDataThingHandler createDataHandler(String id, Bridge bridge,
            Function<ThingBuilder, ThingBuilder> builderConfigurator, BundleContext context) {
        return createDataHandler(id, bridge, builderConfigurator, context, true);
    }

    private ModbusDataThingHandler createDataHandler(String id, Bridge bridge,
            Function<ThingBuilder, ThingBuilder> builderConfigurator, BundleContext context,
            boolean autoCreateItemsAndLinkToChannels) {
        ThingUID thingUID = new ThingUID(THING_TYPE_MODBUS_DATA, id);
        ThingBuilder builder = ThingBuilder.create(THING_TYPE_MODBUS_DATA, thingUID).withLabel("label for " + id);
        Map<String, ChannelUID> toBeLinked = new HashMap<>();
        for (Entry<String, String> entry : CHANNEL_TO_ACCEPTED_TYPE.entrySet()) {
            String channelId = entry.getKey();
            // accepted item type
            String channelAcceptedType = entry.getValue();
            ChannelUID channelUID = new ChannelUID(thingUID, channelId);
            builder = builder.withChannel(ChannelBuilder.create(channelUID, channelAcceptedType).build());

            if (autoCreateItemsAndLinkToChannels) {
                // Create item of correct type and link it to channel
                String itemName = getItemName(channelUID);
                final GenericItem item;
                item = coreItemFactory.createItem(channelAcceptedType, itemName);
                assertThat(String.format("Could not determine correct item type for %s", channelId), item,
                        is(notNullValue()));
                assertNotNull(item);
                Objects.requireNonNull(item);
                addItem(item);
                toBeLinked.put(itemName, channelUID);
            }
        }
        if (builderConfigurator != null) {
            builder = builderConfigurator.apply(builder);
        }

        Thing dataThing = builder.withBridge(bridge.getUID()).build();
        addThing(dataThing);

        // Link after the things and items have been created
        for (Entry<String, ChannelUID> entry : toBeLinked.entrySet()) {
            linkItem(entry.getKey(), entry.getValue());
        }
        return (ModbusDataThingHandler) dataThing.getHandler();
    }

    private String getItemName(ChannelUID channelUID) {
        return channelUID.toString().replace(':', '_') + "_item";
    }

    private void assertSingleStateUpdate(ModbusDataThingHandler handler, String channel, Matcher<State> matcher) {
        waitForAssert(() -> {
            ChannelUID channelUID = new ChannelUID(handler.getThing().getUID(), channel);
            String itemName = getItemName(channelUID);
            Item item = itemRegistry.get(itemName);
            assertThat(String.format("Item %s is not available from item registry", itemName), item,
                    is(notNullValue()));
            assertNotNull(item);
            List<State> updates = getStateUpdates(itemName);
            if (updates != null) {
                assertThat(
                        String.format("Many updates found, expected one: %s", Arrays.deepToString(updates.toArray())),
                        updates.size(), is(equalTo(1)));
            }
            State state = updates == null ? null : updates.get(0);
            assertThat(String.format("%s %s, state %s of type %s", item.getClass().getSimpleName(), itemName, state,
                    state == null ? null : state.getClass().getSimpleName()), state, is(matcher));
        });
    }

    private void assertSingleStateUpdate(ModbusDataThingHandler handler, String channel, State state) {
        assertSingleStateUpdate(handler, channel, is(equalTo(state)));
    }

    private void testOutOfBoundsGeneric(int pollStart, int pollLength, String start,
            ModbusReadFunctionCode functionCode, ValueType valueType, ThingStatus expectedStatus) {
        testOutOfBoundsGeneric(pollStart, pollLength, start, functionCode, valueType, expectedStatus, null);
    }

    private void testOutOfBoundsGeneric(int pollStart, int pollLength, String start,
            ModbusReadFunctionCode functionCode, ValueType valueType, ThingStatus expectedStatus,
            BundleContext context) {
        ModbusSlaveEndpoint endpoint = new ModbusTCPSlaveEndpoint("thisishost", 502, false);

        // Minimally mocked request
        ModbusReadRequestBlueprint request = Mockito.mock(ModbusReadRequestBlueprint.class);
        doReturn(pollStart).when(request).getReference();
        doReturn(pollLength).when(request).getDataLength();
        doReturn(functionCode).when(request).getFunctionCode();

        PollTask task = Mockito.mock(PollTask.class);
        doReturn(endpoint).when(task).getEndpoint();
        doReturn(request).when(task).getRequest();

        Bridge pollerThing = createPollerMock("poller1", task);

        Configuration dataConfig = new Configuration();
        dataConfig.put("readStart", start);
        dataConfig.put("readTransform", "default");
        dataConfig.put("readValueType", valueType.getConfigValue());
        ModbusDataThingHandler dataHandler = createDataHandler("data1", pollerThing,
                builder -> builder.withConfiguration(dataConfig), context);
        assertThat(dataHandler.getThing().getStatusInfo().getDescription(), dataHandler.getThing().getStatus(),
                is(equalTo(expectedStatus)));
    }

    @Test
    public void testInitCoilsOutOfIndex() {
        testOutOfBoundsGeneric(4, 3, "8", ModbusReadFunctionCode.READ_COILS, ModbusConstants.ValueType.BIT,
                ThingStatus.OFFLINE);
    }

    @Test
    public void testInitCoilsOutOfIndex2() {
        // Reading coils 4, 5, 6. Coil 7 is out of bounds
        testOutOfBoundsGeneric(4, 3, "7", ModbusReadFunctionCode.READ_COILS, ModbusConstants.ValueType.BIT,
                ThingStatus.OFFLINE);
    }

    @Test
    public void testInitCoilsOK() {
        // Reading coils 4, 5, 6. Coil 6 is OK
        testOutOfBoundsGeneric(4, 3, "6", ModbusReadFunctionCode.READ_COILS, ModbusConstants.ValueType.BIT,
                ThingStatus.ONLINE);
    }

    @Test
    public void testInitRegistersWithBitOutOfIndex() {
        testOutOfBoundsGeneric(4, 3, "8.0", ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                ModbusConstants.ValueType.BIT, ThingStatus.OFFLINE);
    }

    @Test
    public void testInitRegistersWithBitOutOfIndex2() {
        testOutOfBoundsGeneric(4, 3, "7.16", ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                ModbusConstants.ValueType.BIT, ThingStatus.OFFLINE);
    }

    @Test
    public void testInitRegistersWithBitOK() {
        testOutOfBoundsGeneric(4, 3, "6.0", ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                ModbusConstants.ValueType.BIT, ThingStatus.ONLINE);
    }

    @Test
    public void testInitRegistersWithBitOK2() {
        testOutOfBoundsGeneric(4, 3, "6.15", ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                ModbusConstants.ValueType.BIT, ThingStatus.ONLINE);
    }

    @Test
    public void testInitRegistersWithInt8OutOfIndex() {
        testOutOfBoundsGeneric(4, 3, "8.0", ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                ModbusConstants.ValueType.INT8, ThingStatus.OFFLINE);
    }

    @Test
    public void testInitRegistersWithInt8OutOfIndex2() {
        testOutOfBoundsGeneric(4, 3, "7.2", ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                ModbusConstants.ValueType.INT8, ThingStatus.OFFLINE);
    }

    @Test
    public void testInitRegistersWithInt8OK() {
        testOutOfBoundsGeneric(4, 3, "6.0", ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                ModbusConstants.ValueType.INT8, ThingStatus.ONLINE);
    }

    @Test
    public void testInitRegistersWithInt8OK2() {
        testOutOfBoundsGeneric(4, 3, "6.1", ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                ModbusConstants.ValueType.INT8, ThingStatus.ONLINE);
    }

    @Test
    public void testInitRegistersWithInt16OK() {
        // Poller reading registers 4, 5, 6. Register 6 is OK
        testOutOfBoundsGeneric(4, 3, "6", ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                ModbusConstants.ValueType.INT16, ThingStatus.ONLINE);
    }

    @Test
    public void testInitRegistersWithInt16OutOfBounds() {
        // Poller reading registers 4, 5, 6. Register 7 is out-of-bounds
        testOutOfBoundsGeneric(4, 3, "7", ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                ModbusConstants.ValueType.INT16, ThingStatus.OFFLINE);
    }

    @Test
    public void testInitRegistersWithInt16OutOfBounds2() {
        testOutOfBoundsGeneric(4, 3, "8", ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                ModbusConstants.ValueType.INT16, ThingStatus.OFFLINE);
    }

    @Test
    public void testInitRegistersWithInt16NoDecimalFormatAllowed() {
        testOutOfBoundsGeneric(4, 3, "7.0", ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                ModbusConstants.ValueType.INT16, ThingStatus.OFFLINE);
    }

    @Test
    public void testInitRegistersWithInt32OK() {
        testOutOfBoundsGeneric(4, 3, "5", ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                ModbusConstants.ValueType.INT32, ThingStatus.ONLINE);
    }

    @Test
    public void testInitRegistersWithInt32OutOfBounds() {
        testOutOfBoundsGeneric(4, 3, "6", ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                ModbusConstants.ValueType.INT32, ThingStatus.OFFLINE);
    }

    @Test
    public void testInitRegistersWithInt32AtTheEdge() {
        testOutOfBoundsGeneric(4, 3, "5", ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                ModbusConstants.ValueType.INT32, ThingStatus.ONLINE);
    }

    private ModbusDataThingHandler testReadHandlingGeneric(ModbusReadFunctionCode functionCode, String start,
            String transform, ValueType valueType, BitArray bits, ModbusRegisterArray registers, Exception error) {
        return testReadHandlingGeneric(functionCode, start, transform, valueType, bits, registers, error, null);
    }

    private ModbusDataThingHandler testReadHandlingGeneric(ModbusReadFunctionCode functionCode, String start,
            String transform, ValueType valueType, BitArray bits, ModbusRegisterArray registers, Exception error,
            BundleContext context) {
        return testReadHandlingGeneric(functionCode, start, transform, valueType, bits, registers, error, context,
                true);
    }

    @SuppressWarnings({ "null" })
    private ModbusDataThingHandler testReadHandlingGeneric(ModbusReadFunctionCode functionCode, String start,
            String transform, ValueType valueType, BitArray bits, ModbusRegisterArray registers, Exception error,
            BundleContext context, boolean autoCreateItemsAndLinkToChannels) {
        ModbusSlaveEndpoint endpoint = new ModbusTCPSlaveEndpoint("thisishost", 502, false);

        int pollLength = 3;

        // Minimally mocked request
        ModbusReadRequestBlueprint request = Mockito.mock(ModbusReadRequestBlueprint.class);
        doReturn(pollLength).when(request).getDataLength();
        doReturn(functionCode).when(request).getFunctionCode();

        PollTask task = Mockito.mock(PollTask.class);
        doReturn(endpoint).when(task).getEndpoint();
        doReturn(request).when(task).getRequest();

        Bridge poller = createPollerMock("poller1", task);

        Configuration dataConfig = new Configuration();
        dataConfig.put("readStart", start);
        dataConfig.put("readTransform", transform);
        dataConfig.put("readValueType", valueType.getConfigValue());

        String thingId = "read1";
        ModbusDataThingHandler dataHandler = createDataHandler(thingId, poller,
                builder -> builder.withConfiguration(dataConfig), context, autoCreateItemsAndLinkToChannels);

        assertThat(dataHandler.getThing().getStatus(), is(equalTo(ThingStatus.ONLINE)));

        // call callbacks
        if (bits != null) {
            assertNull(registers);
            assertNull(error);
            AsyncModbusReadResult result = new AsyncModbusReadResult(request, bits);
            dataHandler.onReadResult(result);
        } else if (registers != null) {
            assertNull(bits);
            assertNull(error);
            AsyncModbusReadResult result = new AsyncModbusReadResult(request, registers);
            dataHandler.onReadResult(result);
        } else {
            assertNull(bits);
            assertNull(registers);
            assertNotNull(error);
            AsyncModbusFailure<ModbusReadRequestBlueprint> result = new AsyncModbusFailure<ModbusReadRequestBlueprint>(
                    request, error);
            dataHandler.handleReadError(result);
        }
        return dataHandler;
    }

    private ModbusDataThingHandler testWriteHandlingGeneric(String start, String transform, ValueType valueType,
            String writeType, ModbusWriteFunctionCode successFC, String channel, Command command, Exception error,
            BundleContext context) {
        return testWriteHandlingGeneric(start, transform, valueType, writeType, successFC, channel, command, error,
                context, false);
    }

    @SuppressWarnings({ "null" })
    private ModbusDataThingHandler testWriteHandlingGeneric(String start, String transform, ValueType valueType,
            String writeType, ModbusWriteFunctionCode successFC, String channel, Command command, Exception error,
            BundleContext context, boolean parentIsEndpoint) {
        ModbusSlaveEndpoint endpoint = new ModbusTCPSlaveEndpoint("thisishost", 502, false);

        // Minimally mocked request
        ModbusReadRequestBlueprint request = Mockito.mock(ModbusReadRequestBlueprint.class);

        PollTask task = Mockito.mock(PollTask.class);
        doReturn(endpoint).when(task).getEndpoint();
        doReturn(request).when(task).getRequest();

        final Bridge parent;
        if (parentIsEndpoint) {
            parent = createTcpMock();
            addThing(parent);
        } else {
            parent = createPollerMock("poller1", task);
        }

        Configuration dataConfig = new Configuration();
        dataConfig.put("readStart", "");
        dataConfig.put("writeStart", start);
        dataConfig.put("writeTransform", transform);
        dataConfig.put("writeValueType", valueType.getConfigValue());
        dataConfig.put("writeType", writeType);

        String thingId = "write";

        ModbusDataThingHandler dataHandler = createDataHandler(thingId, parent,
                builder -> builder.withConfiguration(dataConfig), context);

        assertThat(dataHandler.getThing().getStatus(), is(equalTo(ThingStatus.ONLINE)));

        dataHandler.handleCommand(new ChannelUID(dataHandler.getThing().getUID(), channel), command);

        if (error != null) {
            dataHandler.handleReadError(new AsyncModbusFailure<ModbusReadRequestBlueprint>(request, error));
        } else {
            ModbusResponse resp = new ModbusResponse() {

                @Override
                public int getFunctionCode() {
                    return successFC.getFunctionCode();
                }
            };
            dataHandler
                    .onWriteResponse(new AsyncModbusWriteResult(Mockito.mock(ModbusWriteRequestBlueprint.class), resp));
        }
        return dataHandler;
    }

    @Test
    public void testOnError() {
        ModbusDataThingHandler dataHandler = testReadHandlingGeneric(ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                "0.0", "default", ModbusConstants.ValueType.BIT, null, null, new Exception("fooerror"));

        assertSingleStateUpdate(dataHandler, CHANNEL_LAST_READ_ERROR, is(notNullValue(State.class)));
    }

    @Test
    public void testOnRegistersInt16StaticTransformation() {
        ModbusDataThingHandler dataHandler = testReadHandlingGeneric(ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                "0", "-3", ModbusConstants.ValueType.INT16, null,
                new ModbusRegisterArray(new byte[] { (byte) 0xff, (byte) 0xfd }), null);

        assertSingleStateUpdate(dataHandler, CHANNEL_LAST_READ_SUCCESS, is(notNullValue(State.class)));
        assertSingleStateUpdate(dataHandler, CHANNEL_LAST_READ_ERROR, is(nullValue(State.class)));

        // -3 converts to "true"
        assertSingleStateUpdate(dataHandler, CHANNEL_CONTACT, is(nullValue(State.class)));
        assertSingleStateUpdate(dataHandler, CHANNEL_SWITCH, is(nullValue(State.class)));
        assertSingleStateUpdate(dataHandler, CHANNEL_DIMMER, is(nullValue(State.class)));
        assertSingleStateUpdate(dataHandler, CHANNEL_NUMBER, new DecimalType(-3));
        // roller shutter fails since -3 is invalid value (not between 0...100)
        // assertThatStateContains(state, CHANNEL_ROLLERSHUTTER, new PercentType(1));
        assertSingleStateUpdate(dataHandler, CHANNEL_STRING, new StringType("-3"));
        // no datetime, conversion not possible without transformation
    }

    @Test
    public void testOnRegistersRealTransformation() {
        mockTransformation("MULTIPLY", new MultiplyTransformation());
        ModbusDataThingHandler dataHandler = testReadHandlingGeneric(ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                "0", "MULTIPLY(10)", ModbusConstants.ValueType.INT16, null,
                new ModbusRegisterArray(new byte[] { (byte) 0xff, (byte) 0xfd }), null, bundleContext);

        assertSingleStateUpdate(dataHandler, CHANNEL_LAST_READ_SUCCESS, is(notNullValue(State.class)));
        assertSingleStateUpdate(dataHandler, CHANNEL_LAST_READ_ERROR, is(nullValue(State.class)));

        // transformation output (-30) is not valid for contact or switch
        assertSingleStateUpdate(dataHandler, CHANNEL_CONTACT, is(nullValue(State.class)));
        assertSingleStateUpdate(dataHandler, CHANNEL_SWITCH, is(nullValue(State.class)));
        // -30 is not valid value for Dimmer (PercentType) (not between 0...100)
        assertSingleStateUpdate(dataHandler, CHANNEL_DIMMER, is(nullValue(State.class)));
        assertSingleStateUpdate(dataHandler, CHANNEL_NUMBER, new DecimalType(-30));
        // roller shutter fails since -3 is invalid value (not between 0...100)
        assertSingleStateUpdate(dataHandler, CHANNEL_ROLLERSHUTTER, is(nullValue(State.class)));
        assertSingleStateUpdate(dataHandler, CHANNEL_STRING, new StringType("-30"));
        // no datetime, conversion not possible without transformation
    }

    @Test
    public void testOnRegistersNaNFloatInRegisters() throws InvalidSyntaxException {
        ModbusDataThingHandler dataHandler = testReadHandlingGeneric(ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                "0", "default", ModbusConstants.ValueType.FLOAT32, null, new ModbusRegisterArray(
                        // equivalent of floating point NaN
                        new byte[] { (byte) 0x7f, (byte) 0xc0, (byte) 0x00, (byte) 0x00 }),
                null, bundleContext);

        assertSingleStateUpdate(dataHandler, CHANNEL_LAST_READ_SUCCESS, is(notNullValue(State.class)));
        assertSingleStateUpdate(dataHandler, CHANNEL_LAST_READ_ERROR, is(nullValue(State.class)));

        // UNDEF is treated as "boolean true" (OPEN/ON) since it is != 0.
        assertSingleStateUpdate(dataHandler, CHANNEL_CONTACT, OpenClosedType.OPEN);
        assertSingleStateUpdate(dataHandler, CHANNEL_SWITCH, OnOffType.ON);
        assertSingleStateUpdate(dataHandler, CHANNEL_DIMMER, OnOffType.ON);
        assertSingleStateUpdate(dataHandler, CHANNEL_NUMBER, UnDefType.UNDEF);
        assertSingleStateUpdate(dataHandler, CHANNEL_ROLLERSHUTTER, UnDefType.UNDEF);
        assertSingleStateUpdate(dataHandler, CHANNEL_STRING, UnDefType.UNDEF);
    }

    @Test
    public void testOnRegistersRealTransformation2() throws InvalidSyntaxException {
        mockTransformation("ONOFF", new TransformationService() {

            @Override
            public String transform(String function, String source) throws TransformationException {
                return Integer.parseInt(source) != 0 ? "ON" : "OFF";
            }
        });
        ModbusDataThingHandler dataHandler = testReadHandlingGeneric(ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                "0", "ONOFF(10)", ModbusConstants.ValueType.INT16, null,
                new ModbusRegisterArray(new byte[] { (byte) 0xff, (byte) 0xfd }), null, bundleContext);

        assertSingleStateUpdate(dataHandler, CHANNEL_LAST_READ_SUCCESS, is(notNullValue(State.class)));
        assertSingleStateUpdate(dataHandler, CHANNEL_LAST_READ_ERROR, is(nullValue(State.class)));

        assertSingleStateUpdate(dataHandler, CHANNEL_CONTACT, is(nullValue(State.class)));
        assertSingleStateUpdate(dataHandler, CHANNEL_SWITCH, is(equalTo(OnOffType.ON)));
        assertSingleStateUpdate(dataHandler, CHANNEL_DIMMER, is(equalTo(OnOffType.ON)));
        assertSingleStateUpdate(dataHandler, CHANNEL_NUMBER, is(nullValue(State.class)));
        assertSingleStateUpdate(dataHandler, CHANNEL_ROLLERSHUTTER, is(nullValue(State.class)));
        assertSingleStateUpdate(dataHandler, CHANNEL_STRING, is(equalTo(new StringType("ON"))));
    }

    @Test
    public void testWriteWithDataAsChildOfEndpoint() throws InvalidSyntaxException {
        captureModbusWrites();
        mockTransformation("MULTIPLY", new MultiplyTransformation());
        ModbusDataThingHandler dataHandler = testWriteHandlingGeneric("50", "MULTIPLY(10)",
                ModbusConstants.ValueType.BIT, "coil", ModbusWriteFunctionCode.WRITE_COIL, "number",
                new DecimalType("2"), null, bundleContext, /* parent is endpoint */true);

        assertSingleStateUpdate(dataHandler, CHANNEL_LAST_WRITE_SUCCESS, is(notNullValue(State.class)));
        assertSingleStateUpdate(dataHandler, CHANNEL_LAST_WRITE_ERROR, is(nullValue(State.class)));
        assertThat(writeRequests.size(), is(equalTo(1)));
        ModbusWriteRequestBlueprint writeRequest = writeRequests.get(0);
        assertThat(writeRequest.getFunctionCode(), is(equalTo(ModbusWriteFunctionCode.WRITE_COIL)));
        assertThat(writeRequest.getReference(), is(equalTo(50)));
        assertThat(((ModbusWriteCoilRequestBlueprint) writeRequest).getCoils().size(), is(equalTo(1)));
        // Since transform output is non-zero, it is mapped as "true"
        assertThat(((ModbusWriteCoilRequestBlueprint) writeRequest).getCoils().getBit(0), is(equalTo(true)));
    }

    @Test
    public void testWriteRealTransformation() throws InvalidSyntaxException {
        captureModbusWrites();
        mockTransformation("MULTIPLY", new MultiplyTransformation());
        ModbusDataThingHandler dataHandler = testWriteHandlingGeneric("50", "MULTIPLY(10)",
                ModbusConstants.ValueType.BIT, "coil", ModbusWriteFunctionCode.WRITE_COIL, "number",
                new DecimalType("2"), null, bundleContext);

        assertSingleStateUpdate(dataHandler, CHANNEL_LAST_WRITE_SUCCESS, is(notNullValue(State.class)));
        assertSingleStateUpdate(dataHandler, CHANNEL_LAST_WRITE_ERROR, is(nullValue(State.class)));
        assertThat(writeRequests.size(), is(equalTo(1)));
        ModbusWriteRequestBlueprint writeRequest = writeRequests.get(0);
        assertThat(writeRequest.getFunctionCode(), is(equalTo(ModbusWriteFunctionCode.WRITE_COIL)));
        assertThat(writeRequest.getReference(), is(equalTo(50)));
        assertThat(((ModbusWriteCoilRequestBlueprint) writeRequest).getCoils().size(), is(equalTo(1)));
        // Since transform output is non-zero, it is mapped as "true"
        assertThat(((ModbusWriteCoilRequestBlueprint) writeRequest).getCoils().getBit(0), is(equalTo(true)));
    }

    @Test
    public void testWriteRealTransformation2() throws InvalidSyntaxException {
        captureModbusWrites();
        mockTransformation("ZERO", new TransformationService() {

            @Override
            public String transform(String function, String source) throws TransformationException {
                return "0";
            }
        });
        ModbusDataThingHandler dataHandler = testWriteHandlingGeneric("50", "ZERO(foobar)",
                ModbusConstants.ValueType.BIT, "coil", ModbusWriteFunctionCode.WRITE_COIL, "number",
                new DecimalType("2"), null, bundleContext);

        assertSingleStateUpdate(dataHandler, CHANNEL_LAST_WRITE_SUCCESS, is(notNullValue(State.class)));
        assertSingleStateUpdate(dataHandler, CHANNEL_LAST_WRITE_ERROR, is(nullValue(State.class)));
        assertThat(writeRequests.size(), is(equalTo(1)));
        ModbusWriteRequestBlueprint writeRequest = writeRequests.get(0);
        assertThat(writeRequest.getFunctionCode(), is(equalTo(ModbusWriteFunctionCode.WRITE_COIL)));
        assertThat(writeRequest.getReference(), is(equalTo(50)));
        assertThat(((ModbusWriteCoilRequestBlueprint) writeRequest).getCoils().size(), is(equalTo(1)));
        // Since transform output is zero, it is mapped as "false"
        assertThat(((ModbusWriteCoilRequestBlueprint) writeRequest).getCoils().getBit(0), is(equalTo(false)));
    }

    @Test
    public void testWriteRealTransformation3() throws InvalidSyntaxException {
        captureModbusWrites();
        mockTransformation("RANDOM", new TransformationService() {

            @Override
            public String transform(String function, String source) throws TransformationException {
                return "5";
            }
        });
        ModbusDataThingHandler dataHandler = testWriteHandlingGeneric("50", "RANDOM(foobar)",
                ModbusConstants.ValueType.INT16, "holding", ModbusWriteFunctionCode.WRITE_SINGLE_REGISTER, "number",
                new DecimalType("2"), null, bundleContext);

        assertSingleStateUpdate(dataHandler, CHANNEL_LAST_WRITE_SUCCESS, is(notNullValue(State.class)));
        assertSingleStateUpdate(dataHandler, CHANNEL_LAST_WRITE_ERROR, is(nullValue(State.class)));
        assertThat(writeRequests.size(), is(equalTo(1)));
        ModbusWriteRequestBlueprint writeRequest = writeRequests.get(0);
        assertThat(writeRequest.getFunctionCode(), is(equalTo(ModbusWriteFunctionCode.WRITE_SINGLE_REGISTER)));
        assertThat(writeRequest.getReference(), is(equalTo(50)));
        assertThat(((ModbusWriteRegisterRequestBlueprint) writeRequest).getRegisters().size(), is(equalTo(1)));
        assertThat(((ModbusWriteRegisterRequestBlueprint) writeRequest).getRegisters().getRegister(0), is(equalTo(5)));
    }

    @Test
    public void testWriteRealTransformation4() throws InvalidSyntaxException {
        captureModbusWrites();
        mockTransformation("JSON", new TransformationService() {

            @Override
            public String transform(String function, String source) throws TransformationException {
                return "[{"//
                        + "\"functionCode\": 16,"//
                        + "\"address\": 5412,"//
                        + "\"value\": [1, 0, 5]"//
                        + "},"//
                        + "{"//
                        + "\"functionCode\": 6,"//
                        + "\"address\": 555,"//
                        + "\"value\": [3]"//
                        + "}]";
            }
        });
        ModbusDataThingHandler dataHandler = testWriteHandlingGeneric("50", "JSON(foobar)",
                ModbusConstants.ValueType.INT16, "holding", ModbusWriteFunctionCode.WRITE_MULTIPLE_REGISTERS, "number",
                new DecimalType("2"), null, bundleContext);

        assertSingleStateUpdate(dataHandler, CHANNEL_LAST_WRITE_SUCCESS, is(notNullValue(State.class)));
        assertSingleStateUpdate(dataHandler, CHANNEL_LAST_WRITE_ERROR, is(nullValue(State.class)));
        assertThat(writeRequests.size(), is(equalTo(2)));
        {
            ModbusWriteRequestBlueprint writeRequest = writeRequests.get(0);
            assertThat(writeRequest.getFunctionCode(), is(equalTo(ModbusWriteFunctionCode.WRITE_MULTIPLE_REGISTERS)));
            assertThat(writeRequest.getReference(), is(equalTo(5412)));
            assertThat(((ModbusWriteRegisterRequestBlueprint) writeRequest).getRegisters().size(), is(equalTo(3)));
            assertThat(((ModbusWriteRegisterRequestBlueprint) writeRequest).getRegisters().getRegister(0),
                    is(equalTo(1)));
            assertThat(((ModbusWriteRegisterRequestBlueprint) writeRequest).getRegisters().getRegister(1),
                    is(equalTo(0)));
            assertThat(((ModbusWriteRegisterRequestBlueprint) writeRequest).getRegisters().getRegister(2),
                    is(equalTo(5)));
        }
        {
            ModbusWriteRequestBlueprint writeRequest = writeRequests.get(1);
            assertThat(writeRequest.getFunctionCode(), is(equalTo(ModbusWriteFunctionCode.WRITE_SINGLE_REGISTER)));
            assertThat(writeRequest.getReference(), is(equalTo(555)));
            assertThat(((ModbusWriteRegisterRequestBlueprint) writeRequest).getRegisters().size(), is(equalTo(1)));
            assertThat(((ModbusWriteRegisterRequestBlueprint) writeRequest).getRegisters().getRegister(0),
                    is(equalTo(3)));
        }
    }

    @Test
    public void testWriteRealTransformation5() throws InvalidSyntaxException {
        captureModbusWrites();
        mockTransformation("PLUS", new TransformationService() {

            @Override
            public String transform(String arg, String source) throws TransformationException {
                return String.valueOf(Integer.parseInt(arg) + Integer.parseInt(source));
            }
        });
        mockTransformation("CONCAT", new TransformationService() {

            @Override
            public String transform(String function, String source) throws TransformationException {
                return source + function;
            }
        });
        mockTransformation("MULTIPLY", new MultiplyTransformation());
        ModbusDataThingHandler dataHandler = testWriteHandlingGeneric("50", "MULTIPLY:3∩PLUS(2)∩CONCAT(0)",
                ModbusConstants.ValueType.INT16, "holding", ModbusWriteFunctionCode.WRITE_SINGLE_REGISTER, "number",
                new DecimalType("2"), null, bundleContext);

        assertSingleStateUpdate(dataHandler, CHANNEL_LAST_WRITE_SUCCESS, is(notNullValue(State.class)));
        assertSingleStateUpdate(dataHandler, CHANNEL_LAST_WRITE_ERROR, is(nullValue(State.class)));
        assertThat(writeRequests.size(), is(equalTo(1)));
        ModbusWriteRequestBlueprint writeRequest = writeRequests.get(0);
        assertThat(writeRequest.getFunctionCode(), is(equalTo(ModbusWriteFunctionCode.WRITE_SINGLE_REGISTER)));
        assertThat(writeRequest.getReference(), is(equalTo(50)));
        assertThat(((ModbusWriteRegisterRequestBlueprint) writeRequest).getRegisters().size(), is(equalTo(1)));
        assertThat(((ModbusWriteRegisterRequestBlueprint) writeRequest).getRegisters().getRegister(0),
                is(equalTo(/* (2*3 + 2) + '0' */ 80)));
    }

    private void testValueTypeGeneric(ModbusReadFunctionCode functionCode, ValueType valueType,
            ThingStatus expectedStatus) {
        ModbusSlaveEndpoint endpoint = new ModbusTCPSlaveEndpoint("thisishost", 502, false);

        // Minimally mocked request
        ModbusReadRequestBlueprint request = Mockito.mock(ModbusReadRequestBlueprint.class);
        doReturn(3).when(request).getDataLength();
        doReturn(functionCode).when(request).getFunctionCode();

        PollTask task = Mockito.mock(PollTask.class);
        doReturn(endpoint).when(task).getEndpoint();
        doReturn(request).when(task).getRequest();

        Bridge poller = createPollerMock("poller1", task);

        Configuration dataConfig = new Configuration();
        dataConfig.put("readStart", "1");
        dataConfig.put("readTransform", "default");
        dataConfig.put("readValueType", valueType.getConfigValue());
        ModbusDataThingHandler dataHandler = createDataHandler("data1", poller,
                builder -> builder.withConfiguration(dataConfig));
        assertThat(dataHandler.getThing().getStatus(), is(equalTo(expectedStatus)));
    }

    @Test
    public void testCoilDoesNotAcceptFloat32ValueType() {
        testValueTypeGeneric(ModbusReadFunctionCode.READ_COILS, ModbusConstants.ValueType.FLOAT32, ThingStatus.OFFLINE);
    }

    @Test
    public void testCoilAcceptsBitValueType() {
        testValueTypeGeneric(ModbusReadFunctionCode.READ_COILS, ModbusConstants.ValueType.BIT, ThingStatus.ONLINE);
    }

    @Test
    public void testDiscreteInputDoesNotAcceptFloat32ValueType() {
        testValueTypeGeneric(ModbusReadFunctionCode.READ_INPUT_DISCRETES, ModbusConstants.ValueType.FLOAT32,
                ThingStatus.OFFLINE);
    }

    @Test
    public void testDiscreteInputAcceptsBitValueType() {
        testValueTypeGeneric(ModbusReadFunctionCode.READ_INPUT_DISCRETES, ModbusConstants.ValueType.BIT,
                ThingStatus.ONLINE);
    }

    @Test
    public void testRefreshOnData() throws InterruptedException {
        ModbusReadFunctionCode functionCode = ModbusReadFunctionCode.READ_COILS;

        ModbusSlaveEndpoint endpoint = new ModbusTCPSlaveEndpoint("thisishost", 502, false);

        int pollLength = 3;

        // Minimally mocked request
        ModbusReadRequestBlueprint request = Mockito.mock(ModbusReadRequestBlueprint.class);
        doReturn(pollLength).when(request).getDataLength();
        doReturn(functionCode).when(request).getFunctionCode();

        PollTask task = Mockito.mock(PollTask.class);
        doReturn(endpoint).when(task).getEndpoint();
        doReturn(request).when(task).getRequest();

        Bridge poller = createPollerMock("poller1", task);

        Configuration dataConfig = new Configuration();
        dataConfig.put("readStart", "0");
        dataConfig.put("readTransform", "default");
        dataConfig.put("readValueType", "bit");

        String thingId = "read1";

        ModbusDataThingHandler dataHandler = createDataHandler(thingId, poller,
                builder -> builder.withConfiguration(dataConfig), bundleContext);
        assertThat(dataHandler.getThing().getStatus(), is(equalTo(ThingStatus.ONLINE)));

        verify(comms, never()).submitOneTimePoll(eq(request), notNull(), notNull());
        // Wait for all channels to receive the REFRESH command (initiated by the core)
        waitForAssert(
                () -> verify((ModbusPollerThingHandler) poller.getHandler(), times(CHANNEL_TO_ACCEPTED_TYPE.size()))
                        .refresh());
        // Reset the mock
        reset(poller.getHandler());

        // Issue REFRESH command and verify the results
        dataHandler.handleCommand(Mockito.mock(ChannelUID.class), RefreshType.REFRESH);

        // data handler asynchronously calls the poller.refresh() -- it might take some time
        // We check that refresh is finally called
        waitForAssert(() -> verify((ModbusPollerThingHandler) poller.getHandler()).refresh());
    }

    private static Stream<Arguments> provideArgsForUpdateThenCommandFromItem()

    {
        return Stream.of(//
                // ON/OFF commands
                Arguments.of((short) 0b1011_0100_0000_1111, "1", (short) 0b1011_0100_0000_1101, OnOffType.OFF),
                Arguments.of((short) 0b1011_0100_0000_1111, "4", (short) 0b1011_0100_0001_1111, OnOffType.ON),
                // OPEN/CLOSED commands
                Arguments.of((short) 0b1011_0100_0000_1111, "1", (short) 0b1011_0100_0000_1101, OpenClosedType.CLOSED),
                Arguments.of((short) 0b1011_0100_0000_1111, "4", (short) 0b1011_0100_0001_1111, OpenClosedType.OPEN),
                // DecimalType commands
                Arguments.of((short) 0b1011_0100_0000_1111, "1", (short) 0b1011_0100_0000_1101, new DecimalType(0)),
                Arguments.of((short) 0b1011_0100_0010_1111, "5", (short) 0b1011_0100_0000_1111, new DecimalType(0)),
                Arguments.of((short) 0b1011_0100_0000_1111, "4", (short) 0b1011_0100_0001_1111, new DecimalType(5)),
                Arguments.of((short) 0b1011_0100_0000_1111, "15", (short) 0b0011_0100_0000_1111, new DecimalType(0))

        ).flatMap(a -> {
            // parametrize by channel (yes, it does not matter what channel is used, commands are interpreted all the
            // same)
            Stream<String> channels = Stream.of("switch", "number", "contact");
            return channels.map(channel -> appendArg(a, channel));
        });
    }

    @ParameterizedTest
    @MethodSource("provideArgsForUpdateThenCommandFromItem")
    public void testUpdateFromHandlerThenCommandFromItem(short stateUpdateFromHandler, String bitIndex,
            short expectedWriteDataToSlave, Command commandFromItem, String channel) {
        int expectedWriteDataToSlaveUnsigned = expectedWriteDataToSlave & 0xFFFF;
        captureModbusWrites();
        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 0L); // 0 -> non polling
        pollerConfig.put("start", 2);
        pollerConfig.put("length", 3);
        pollerConfig.put("type", ModbusBindingConstantsInternal.READ_TYPE_HOLDING_REGISTER);
        ThingUID pollerUID = new ThingUID(ModbusBindingConstantsInternal.THING_TYPE_MODBUS_POLLER, "realPoller");
        Bridge poller = BridgeBuilder.create(ModbusBindingConstantsInternal.THING_TYPE_MODBUS_POLLER, pollerUID)
                .withLabel("label for realPoller").withConfiguration(pollerConfig)
                .withBridge(realEndpointWithMockedComms.getUID()).build();
        addThing(poller);
        assertEquals(ThingStatus.ONLINE, poller.getStatus(), poller.getStatusInfo().getDescription());

        Configuration dataConfig = new Configuration();
        dataConfig.put("writeStart", "3." + bitIndex);
        dataConfig.put("writeValueType", "bit");
        dataConfig.put("writeType", "holding");

        String thingId = "read1";

        ModbusDataThingHandler dataHandler = createDataHandler(thingId, poller,
                builder -> builder.withConfiguration(dataConfig), bundleContext);
        assertEquals(ThingStatus.ONLINE, dataHandler.getThing().getStatus());
        assertEquals(pollerUID, dataHandler.getThing().getBridgeUID());

        AsyncModbusReadResult result = new AsyncModbusReadResult(Mockito.mock(ModbusReadRequestBlueprint.class),
                new ModbusRegisterArray(/* register 2, dummy data */0, /* register 3 */ stateUpdateFromHandler,
                        /* register 4, dummy data */9));

        // poller receives some data (and therefore data as well)
        getPollerCallback(((ModbusPollerThingHandler) poller.getHandler())).handle(result);
        dataHandler.handleCommand(new ChannelUID(dataHandler.getThing().getUID(), channel), commandFromItem);

        // Assert data written
        {
            assertEquals(1, writeRequests.size());
            ModbusWriteRequestBlueprint writeRequest = writeRequests.get(0);
            assertEquals(writeRequest.getFunctionCode(), ModbusWriteFunctionCode.WRITE_SINGLE_REGISTER);
            assertEquals(writeRequest.getReference(), 3);
            assertEquals(((ModbusWriteRegisterRequestBlueprint) writeRequest).getRegisters().size(), 1);
            assertEquals(expectedWriteDataToSlaveUnsigned,
                    ((ModbusWriteRegisterRequestBlueprint) writeRequest).getRegisters().getRegister(0));
        }
    }

    private void testInitGeneric(ModbusReadFunctionCode pollerFunctionCode, Configuration config,
            Consumer<ThingStatusInfo> statusConsumer) {
        testInitGeneric(pollerFunctionCode, 0, config, statusConsumer);
    }

    /**
     *
     * @param pollerFunctionCode poller function code. Use null if you want to have data thing direct child of endpoint
     *            thing
     * @param pollerStart start index of poller
     * @param config thing config
     * @param statusConsumer assertion method for data thingstatus
     */
    private void testInitGeneric(ModbusReadFunctionCode pollerFunctionCode, int pollerStart, Configuration config,
            Consumer<ThingStatusInfo> statusConsumer) {
        int pollLength = 3;

        Bridge parent;
        if (pollerFunctionCode == null) {
            parent = createTcpMock();
            addThing(parent);
        } else {
            ModbusSlaveEndpoint endpoint = new ModbusTCPSlaveEndpoint("thisishost", 502, false);

            // Minimally mocked request
            ModbusReadRequestBlueprint request = Mockito.mock(ModbusReadRequestBlueprint.class);
            doReturn(pollerStart).when(request).getReference();
            doReturn(pollLength).when(request).getDataLength();
            doReturn(pollerFunctionCode).when(request).getFunctionCode();

            PollTask task = Mockito.mock(PollTask.class);
            doReturn(endpoint).when(task).getEndpoint();
            doReturn(request).when(task).getRequest();

            parent = createPollerMock("poller1", task);
        }

        String thingId = "read1";

        ModbusDataThingHandler dataHandler = createDataHandler(thingId, parent,
                builder -> builder.withConfiguration(config), bundleContext);

        statusConsumer.accept(dataHandler.getThing().getStatusInfo());
    }

    @Test
    public void testReadOnlyData() {
        Configuration dataConfig = new Configuration();
        dataConfig.put("readStart", "0");
        dataConfig.put("readValueType", "bit");
        testInitGeneric(ModbusReadFunctionCode.READ_COILS, dataConfig,
                status -> assertThat(status.getStatus(), is(equalTo(ThingStatus.ONLINE))));
    }

    /**
     * readValueType=bit should be assumed with coils, so it's ok to skip it
     */
    @Test
    public void testReadOnlyDataMissingValueTypeWithCoils() {
        Configuration dataConfig = new Configuration();
        dataConfig.put("readStart", "0");
        // missing value type
        testInitGeneric(ModbusReadFunctionCode.READ_COILS, dataConfig,
                status -> assertThat(status.getStatus(), is(equalTo(ThingStatus.ONLINE))));
    }

    @Test
    public void testReadOnlyDataInvalidValueType() {
        Configuration dataConfig = new Configuration();
        dataConfig.put("readStart", "0");
        dataConfig.put("readValueType", "foobar");
        testInitGeneric(ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, dataConfig, status -> {
            assertThat(status.getStatus(), is(equalTo(ThingStatus.UNINITIALIZED)));
            assertThat(status.getStatusDetail(), is(equalTo(ThingStatusDetail.HANDLER_CONFIGURATION_PENDING)));
        });
    }

    /**
     * We do not assume value type with registers, not ok to skip it
     */
    @Test
    public void testReadOnlyDataMissingValueTypeWithRegisters() {
        Configuration dataConfig = new Configuration();
        dataConfig.put("readStart", "0");
        testInitGeneric(ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, dataConfig, status -> {
            assertThat(status.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
            assertThat(status.getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
        });
    }

    @Test
    public void testWriteOnlyData() {
        Configuration dataConfig = new Configuration();
        dataConfig.put("writeStart", "0");
        dataConfig.put("writeValueType", "bit");
        dataConfig.put("writeType", "coil");
        testInitGeneric(ModbusReadFunctionCode.READ_COILS, dataConfig,
                status -> assertThat(status.getStatus(), is(equalTo(ThingStatus.ONLINE))));
    }

    @Test
    public void testWriteHoldingInt16Data() {
        Configuration dataConfig = new Configuration();
        dataConfig.put("writeStart", "0");
        dataConfig.put("writeValueType", "int16");
        dataConfig.put("writeType", "holding");
        testInitGeneric(ModbusReadFunctionCode.READ_COILS, dataConfig,
                status -> assertThat(status.getStatus(), is(equalTo(ThingStatus.ONLINE))));
    }

    @Test
    public void testWriteHoldingInt8Data() {
        Configuration dataConfig = new Configuration();
        dataConfig.put("writeStart", "0");
        dataConfig.put("writeValueType", "int8");
        dataConfig.put("writeType", "holding");
        testInitGeneric(null, dataConfig, status -> {
            assertThat(status.getStatus(), is(equalTo(ThingStatus.UNINITIALIZED)));
            assertThat(status.getStatusDetail(), is(equalTo(ThingStatusDetail.HANDLER_CONFIGURATION_PENDING)));
        });
    }

    @Test
    public void testWriteHoldingBitDataWrongWriteType() {
        Configuration dataConfig = new Configuration();
        dataConfig.put("writeStart", "0.15");
        dataConfig.put("writeValueType", "bit");
        dataConfig.put("writeType", "coil"); // X.Y writeStart only applicable with holding
        testInitGeneric(ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, dataConfig, status -> {
            assertEquals(ThingStatus.OFFLINE, status.getStatus(), status.getDescription());
            assertThat(status.getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
        });
    }

    @Test
    public void testWriteHoldingBitData() {
        Configuration dataConfig = new Configuration();
        dataConfig.put("writeStart", "0.15");
        dataConfig.put("writeValueType", "bit");
        dataConfig.put("writeType", "holding");
        testInitGeneric(ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, dataConfig, status -> {
            assertEquals(status.getStatus(), ThingStatus.ONLINE, status.getDescription());
        });
    }

    @Test
    public void testWriteHoldingInt8WithSubIndexData() {
        Configuration dataConfig = new Configuration();
        dataConfig.put("writeStart", "1.0");
        dataConfig.put("writeValueType", "int8");
        dataConfig.put("writeType", "holding");
        // OFFLINE since sub-register writes are not supported for other than bit
        testInitGeneric(ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, dataConfig, status -> {
            assertThat(status.getStatus(), is(equalTo(ThingStatus.UNINITIALIZED)));
            assertThat(status.getStatusDetail(), is(equalTo(ThingStatusDetail.HANDLER_CONFIGURATION_PENDING)));
        });
    }

    @Test
    public void testWriteHoldingBitDataRegisterOutOfBounds() {
        Configuration dataConfig = new Configuration();
        // in this test poller reads from register 2. Register 1 is out of bounds
        dataConfig.put("writeStart", "1.15");
        dataConfig.put("writeValueType", "bit");
        dataConfig.put("writeType", "holding");
        testInitGeneric(ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, /* poller start */2, dataConfig, status -> {
            assertEquals(ThingStatus.OFFLINE, status.getStatus(), status.getDescription());
            assertThat(status.getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
        });
    }

    @Test
    public void testWriteHoldingBitDataRegisterOutOfBounds2() {
        Configuration dataConfig = new Configuration();
        // register 3 is the last one polled, 4 is out of bounds
        dataConfig.put("writeStart", "4.15");
        dataConfig.put("writeValueType", "bit");
        dataConfig.put("writeType", "holding");
        testInitGeneric(ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, dataConfig, status -> {
            assertEquals(ThingStatus.OFFLINE, status.getStatus(), status.getDescription());
        });
    }

    @ParameterizedTest
    @CsvSource({ "READ_COILS", "READ_INPUT_DISCRETES", "READ_INPUT_REGISTERS" })
    public void testWriteHoldingBitDataWrongPoller(ModbusReadFunctionCode poller) {
        Configuration dataConfig = new Configuration();
        dataConfig.put("writeStart", "0.15");
        dataConfig.put("writeValueType", "bit");
        dataConfig.put("writeType", "holding");
        testInitGeneric(poller, dataConfig, status -> {
            assertEquals(ThingStatus.OFFLINE, status.getStatus(), status.getDescription());
            assertThat(status.getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
        });
    }

    @Test
    public void testWriteHoldingBitParentEndpointData() {
        Configuration dataConfig = new Configuration();
        dataConfig.put("writeStart", "0.15");
        dataConfig.put("writeValueType", "bit");
        dataConfig.put("writeType", "holding");
        // OFFLINE since we require poller as parent when sub-register writes are used
        testInitGeneric(/* poller not as parent */null, dataConfig, status -> {
            assertEquals(ThingStatus.OFFLINE, status.getStatus(), status.getDescription());
            assertThat(status.getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
        });
    }

    @Test
    public void testWriteHoldingBitBadStartData() {
        Configuration dataConfig = new Configuration();
        dataConfig.put("writeStart", "0.16");
        dataConfig.put("writeValueType", "int8");
        dataConfig.put("writeType", "holding");
        testInitGeneric(ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, dataConfig, status -> {
            assertThat(status.getStatus(), is(equalTo(ThingStatus.UNINITIALIZED)));
            assertThat(status.getStatusDetail(), is(equalTo(ThingStatusDetail.HANDLER_CONFIGURATION_PENDING)));
        });
    }

    @Test
    public void testWriteOnlyDataChildOfEndpoint() {
        Configuration dataConfig = new Configuration();
        dataConfig.put("writeStart", "0");
        dataConfig.put("writeValueType", "bit");
        dataConfig.put("writeType", "coil");
        testInitGeneric(null, dataConfig, status -> assertThat(status.getStatus(), is(equalTo(ThingStatus.ONLINE))));
    }

    @Test
    public void testWriteOnlyDataMissingOneParameter() {
        Configuration dataConfig = new Configuration();
        dataConfig.put("writeStart", "0");
        dataConfig.put("writeValueType", "bit");
        // missing writeType --> error
        testInitGeneric(ModbusReadFunctionCode.READ_COILS, dataConfig, status -> {
            assertEquals(ThingStatus.OFFLINE, status.getStatus(), status.getDescription());
            assertThat(status.getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
            assertThat(status.getDescription(), is(not(equalTo(null))));
        });
    }

    /**
     * OK to omit writeValueType with coils since bit is assumed
     */
    @Test
    public void testWriteOnlyDataMissingValueTypeWithCoilParameter() {
        Configuration dataConfig = new Configuration();
        dataConfig.put("writeStart", "0");
        dataConfig.put("writeType", "coil");
        testInitGeneric(ModbusReadFunctionCode.READ_COILS, dataConfig,
                status -> assertEquals(ThingStatus.ONLINE, status.getStatus(), status.getDescription()));
    }

    @Test
    public void testWriteOnlyIllegalValueType() {
        Configuration dataConfig = new Configuration();
        dataConfig.put("writeStart", "0");
        dataConfig.put("writeType", "coil");
        dataConfig.put("writeValueType", "foobar");
        testInitGeneric(ModbusReadFunctionCode.READ_COILS, dataConfig, status -> {
            assertThat(status.getStatus(), is(equalTo(ThingStatus.UNINITIALIZED)));
            assertThat(status.getStatusDetail(), is(equalTo(ThingStatusDetail.HANDLER_CONFIGURATION_PENDING)));
        });
    }

    @Test
    public void testWriteInvalidType() {
        Configuration dataConfig = new Configuration();
        dataConfig.put("writeStart", "0");
        dataConfig.put("writeType", "foobar");
        testInitGeneric(ModbusReadFunctionCode.READ_COILS, dataConfig, status -> {
            assertThat(status.getStatus(), is(equalTo(ThingStatus.UNINITIALIZED)));
            assertThat(status.getStatusDetail(), is(equalTo(ThingStatusDetail.HANDLER_CONFIGURATION_PENDING)));
        });
    }

    @Test
    public void testWriteCoilBadStart() {
        Configuration dataConfig = new Configuration();
        dataConfig.put("writeStart", "0.4");
        dataConfig.put("writeType", "coil");
        testInitGeneric(null, dataConfig, status -> {
            assertThat(status.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
            assertThat(status.getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
        });
    }

    @Test
    public void testWriteHoldingBadStart() {
        Configuration dataConfig = new Configuration();
        dataConfig.put("writeStart", "0.4");
        dataConfig.put("writeType", "holding");
        testInitGeneric(null, dataConfig, status -> {
            assertThat(status.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
            assertThat(status.getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
        });
    }

    @Test
    public void testReadHoldingBadStart() {
        Configuration dataConfig = new Configuration();
        dataConfig.put("readStart", "0.0");
        dataConfig.put("readValueType", "int16");
        testInitGeneric(ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, dataConfig, status -> {
            assertThat(status.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
            assertThat(status.getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
        });
    }

    @Test
    public void testReadHoldingBadStart2() {
        Configuration dataConfig = new Configuration();
        dataConfig.put("readStart", "0.0");
        dataConfig.put("readValueType", "bit");
        testInitGeneric(ModbusReadFunctionCode.READ_COILS, dataConfig, status -> {
            assertThat(status.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
            assertThat(status.getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
        });
    }

    @Test
    public void testReadHoldingOKStart() {
        Configuration dataConfig = new Configuration();
        dataConfig.put("readStart", "0.0");
        dataConfig.put("readType", "holding");
        dataConfig.put("readValueType", "bit");
        testInitGeneric(ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, dataConfig,
                status -> assertThat(status.getStatus(), is(equalTo(ThingStatus.ONLINE))));
    }

    @Test
    public void testReadValueTypeIllegal() {
        Configuration dataConfig = new Configuration();
        dataConfig.put("readStart", "0.0");
        dataConfig.put("readType", "holding");
        dataConfig.put("readValueType", "foobar");
        testInitGeneric(ModbusReadFunctionCode.READ_COILS, dataConfig, status -> {
            assertThat(status.getStatus(), is(equalTo(ThingStatus.UNINITIALIZED)));
            assertThat(status.getStatusDetail(), is(equalTo(ThingStatusDetail.HANDLER_CONFIGURATION_PENDING)));
        });
    }

    @Test
    public void testWriteOnlyTransform() {
        Configuration dataConfig = new Configuration();
        // no need to have start, JSON output of transformation defines everything
        dataConfig.put("writeTransform", "JS(myJsonTransform.js)");
        testInitGeneric(null, dataConfig, status -> assertThat(status.getStatus(), is(equalTo(ThingStatus.ONLINE))));
    }

    @Test
    public void testWriteTransformAndStart() {
        Configuration dataConfig = new Configuration();
        // It's illegal to have start and transform. Just have transform or have all
        dataConfig.put("writeStart", "3");
        dataConfig.put("writeTransform", "JS(myJsonTransform.js)");
        testInitGeneric(ModbusReadFunctionCode.READ_COILS, dataConfig, status -> {
            assertThat(status.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
            assertThat(status.getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
        });
    }

    @Test
    public void testWriteTransformAndNecessary() {
        Configuration dataConfig = new Configuration();
        dataConfig.put("writeStart", "3");
        dataConfig.put("writeType", "holding");
        dataConfig.put("writeValueType", "int16");
        dataConfig.put("writeTransform", "JS(myJsonTransform.js)");
        testInitGeneric(null, dataConfig, status -> assertThat(status.getStatus(), is(equalTo(ThingStatus.ONLINE))));
    }
}
