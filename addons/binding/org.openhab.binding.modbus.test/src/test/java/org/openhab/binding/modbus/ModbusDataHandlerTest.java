/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.modbus;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.common.registry.AbstractRegistry;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemBuilder;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemNotUniqueException;
import org.eclipse.smarthome.core.items.ItemProvider;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.ManagedItemProvider;
import org.eclipse.smarthome.core.items.RegistryHook;
import org.eclipse.smarthome.core.library.items.ContactItem;
import org.eclipse.smarthome.core.library.items.DateTimeItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.thing.link.ManagedItemChannelLinkProvider;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.test.java.JavaTest;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openhab.binding.modbus.handler.ModbusDataThingHandler;
import org.openhab.binding.modbus.handler.ModbusPollerThingHandler;
import org.openhab.binding.modbus.handler.ModbusPollerThingHandlerImpl;
import org.openhab.binding.modbus.handler.ModbusTcpThingHandler;
import org.openhab.io.transport.modbus.BitArray;
import org.openhab.io.transport.modbus.ModbusConstants;
import org.openhab.io.transport.modbus.ModbusConstants.ValueType;
import org.openhab.io.transport.modbus.ModbusManager;
import org.openhab.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.io.transport.modbus.ModbusRegister;
import org.openhab.io.transport.modbus.ModbusRegisterArray;
import org.openhab.io.transport.modbus.BasicModbusRegisterArray;
import org.openhab.io.transport.modbus.BasicModbusRegister;
import org.openhab.io.transport.modbus.ModbusResponse;
import org.openhab.io.transport.modbus.ModbusWriteCoilRequestBlueprint;
import org.openhab.io.transport.modbus.ModbusWriteFunctionCode;
import org.openhab.io.transport.modbus.ModbusWriteRegisterRequestBlueprint;
import org.openhab.io.transport.modbus.ModbusWriteRequestBlueprint;
import org.openhab.io.transport.modbus.PollTask;
import org.openhab.io.transport.modbus.WriteTask;
import org.openhab.io.transport.modbus.endpoint.ModbusSlaveEndpoint;
import org.openhab.io.transport.modbus.endpoint.ModbusTCPSlaveEndpoint;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;

import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class ModbusDataHandlerTest extends JavaTest {

    private class ItemChannelLinkRegistryTestImpl extends ItemChannelLinkRegistry {
        private final class ManagedItemChannelLinkProviderExtension extends ManagedItemChannelLinkProvider {
            ManagedItemChannelLinkProviderExtension() {
                setStorageService(new VolatileStorageService());
            }
        }

        public ItemChannelLinkRegistryTestImpl() {
            super();
            this.setThingRegistry(thingRegistry);
            setItemRegistry(itemRegistry);
            ManagedItemChannelLinkProviderExtension provider = new ManagedItemChannelLinkProviderExtension();
            addProvider(provider);
            setManagedProvider(provider);
        }
    };

    private class ItemRegisteryTestImpl extends AbstractRegistry<Item, String, ItemProvider> implements ItemRegistry {

        private Map<String, Item> items = new ConcurrentHashMap<>();

        private final class ManagedProviderTestImpl extends ManagedItemProvider {
            public ManagedProviderTestImpl() {
                setStorageService(new VolatileStorageService());
            }
        }

        public ItemRegisteryTestImpl() {
            super(null);
            setManagedProvider(new ManagedProviderTestImpl());
        }

        @Override
        public @NonNull Item getItem(String name) throws ItemNotFoundException {
            Item item = super.get(name);
            if (item == null) {
                throw new ItemNotFoundException(name);
            }
            return (@NonNull Item) super.get(name);
        }

        @Override
        public @NonNull Item getItemByPattern(@NonNull String name)
                throws ItemNotFoundException, ItemNotUniqueException {
            throw new IllegalStateException();
        }

        @Override
        public @NonNull Collection<@NonNull Item> getItems() {
            return items.values();
        }

        @Override
        public @NonNull Collection<Item> getItemsOfType(@NonNull String type) {
            throw new IllegalStateException();
        }

        @Override
        public @NonNull Collection<@NonNull Item> getItems(@NonNull String pattern) {
            throw new IllegalStateException();
        }

        @Override
        public @NonNull Collection<Item> getItemsByTag(@NonNull String... tags) {
            throw new IllegalStateException();
        }

        @Override
        public @NonNull Collection<Item> getItemsByTagAndType(@NonNull String type, @NonNull String... tags) {
            throw new IllegalStateException();
        }

        @Override
        public @Nullable Item remove(@NonNull String itemName, boolean recursive) {
            if (recursive) {
                throw new IllegalStateException();
            }
            return items.remove(itemName);
        }

        @Override
        public void addRegistryHook(RegistryHook<Item> hook) {
            throw new IllegalStateException();
        }

        @Override
        public void removeRegistryHook(RegistryHook<Item> hook) {
            throw new IllegalStateException();
        }

        @Override
        public <T extends Item> @NonNull Collection<T> getItemsByTag(@NonNull Class<T> typeFilter,
                @NonNull String... tags) {
            throw new IllegalStateException();
        }

        @Override
        public ItemBuilder newItemBuilder(Item item) {
            throw new IllegalStateException();
        }

        @Override
        public ItemBuilder newItemBuilder(String itemType, String itemName) {
            throw new IllegalStateException();
        }

    };

    private static final Map<String, Class<? extends Item>> channelToItemClass = new HashMap<>();
    static {
        channelToItemClass.put(ModbusBindingConstants.CHANNEL_SWITCH, SwitchItem.class);
        channelToItemClass.put(ModbusBindingConstants.CHANNEL_CONTACT, ContactItem.class);
        channelToItemClass.put(ModbusBindingConstants.CHANNEL_DATETIME, DateTimeItem.class);
        channelToItemClass.put(ModbusBindingConstants.CHANNEL_DIMMER, DimmerItem.class);
        channelToItemClass.put(ModbusBindingConstants.CHANNEL_NUMBER, NumberItem.class);
        channelToItemClass.put(ModbusBindingConstants.CHANNEL_STRING, StringItem.class);
        channelToItemClass.put(ModbusBindingConstants.CHANNEL_ROLLERSHUTTER, RollershutterItem.class);
    }

    private List<Thing> things = new ArrayList<>();
    private List<WriteTask> writeTasks = new ArrayList<>();

    @Mock
    private BundleContext bundleContext;

    @Mock
    private ThingHandlerCallback thingCallback;

    @Mock
    private ThingRegistry thingRegistry;

    private ItemRegistry itemRegistry = new ItemRegisteryTestImpl();

    @Mock
    private ModbusManager manager;

    private ItemChannelLinkRegistryTestImpl linkRegistry = new ItemChannelLinkRegistryTestImpl();

    Map<ChannelUID, List<State>> stateUpdates = new HashMap<>();

    private Map<String, String> channelToAcceptedType = ImmutableMap.<String, String> builder()
            .put(ModbusBindingConstants.CHANNEL_SWITCH, "Switch").put(ModbusBindingConstants.CHANNEL_CONTACT, "Contact")
            .put(ModbusBindingConstants.CHANNEL_DATETIME, "DateTime")
            .put(ModbusBindingConstants.CHANNEL_DIMMER, "Dimmer").put(ModbusBindingConstants.CHANNEL_NUMBER, "Number")
            .put(ModbusBindingConstants.CHANNEL_STRING, "String")
            .put(ModbusBindingConstants.CHANNEL_ROLLERSHUTTER, "Rollershutter")
            .put(ModbusBindingConstants.CHANNEL_LAST_READ_SUCCESS, "DateTime")
            .put(ModbusBindingConstants.CHANNEL_LAST_WRITE_SUCCESS, "DateTime")
            .put(ModbusBindingConstants.CHANNEL_LAST_WRITE_ERROR, "DateTime")
            .put(ModbusBindingConstants.CHANNEL_LAST_READ_ERROR, "DateTime").build();

    private void registerThingToMockRegistry(Thing thing) {
        things.add(thing);
        // update bridge with the new child thing
        if (thing.getBridgeUID() != null) {
            ThingUID bridgeUID = thing.getBridgeUID();
            things.stream().filter(t -> t.getUID().equals(bridgeUID)).findFirst().ifPresent(t -> {
                try {
                    t.getClass().getMethod("addThing", Thing.class).invoke(t, thing);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                        | NoSuchMethodException | SecurityException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void hookThingRegistry(ThingHandler thingHandler) {
        Field thingRegisteryField;
        try {
            thingRegisteryField = BaseThingHandler.class.getDeclaredField("thingRegistry");
            thingRegisteryField.setAccessible(true);
            thingRegisteryField.set(thingHandler, thingRegistry);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void hookLinkRegistry(ThingHandler thingHandler) {
        Mockito.doAnswer(invocation -> {
            ChannelUID channelUID = (ChannelUID) invocation.getArgument(0);
            return !linkRegistry.getLinks(channelUID).isEmpty();
        }).when(thingCallback).isChannelLinked(any());
    }

    @SuppressWarnings("null")
    private void hookStatusUpdates(Thing thing) {
        Mockito.doAnswer(invocation -> {
            thing.setStatusInfo((ThingStatusInfo) invocation.getArgument(1));
            return null;
        }).when(thingCallback).statusUpdated(ArgumentMatchers.same(thing), ArgumentMatchers.any());

    }

    @SuppressWarnings("null")
    private void hookStateUpdates(Thing thing) {
        Mockito.doAnswer(invocation -> {
            ChannelUID channelUID = (ChannelUID) invocation.getArgument(0);
            State state = (State) invocation.getArgument(1);
            stateUpdates.putIfAbsent(channelUID, new ArrayList<>());
            stateUpdates.get(channelUID).add(state);
            return null;
        }).when(thingCallback).stateUpdated(any(), any());
    }

    @SuppressWarnings("null")
    private Bridge createPollerMock(String pollerId, PollTask task) {

        final Bridge poller;
        ThingUID thingUID = new ThingUID(ModbusBindingConstants.THING_TYPE_MODBUS_POLLER, pollerId);
        BridgeBuilder builder = BridgeBuilder.create(ModbusBindingConstants.THING_TYPE_MODBUS_POLLER, thingUID)
                .withLabel("label for " + pollerId);
        for (Entry<String, String> entry : channelToAcceptedType.entrySet()) {
            String channelId = entry.getKey();
            String channelAcceptedType = entry.getValue();
            builder = builder.withChannel(new Channel(new ChannelUID(thingUID, channelId), channelAcceptedType));
        }
        poller = builder.build();
        poller.setStatusInfo(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, ""));
        ModbusPollerThingHandlerImpl handler = Mockito.mock(ModbusPollerThingHandlerImpl.class);
        doReturn(task).when(handler).getPollTask();
        Supplier<ModbusManager> managerRef = () -> manager;
        doReturn(managerRef).when(handler).getManagerRef();
        poller.setHandler(handler);
        registerThingToMockRegistry(poller);
        return poller;
    }

    private Bridge createTcpMock() {
        ModbusSlaveEndpoint endpoint = new ModbusTCPSlaveEndpoint("thisishost", 502);
        Bridge tcpBridge = ModbusPollerThingHandlerTest.createTcpThingBuilder("tcp1").build();
        ModbusTcpThingHandler tcpThingHandler = Mockito.mock(ModbusTcpThingHandler.class);
        tcpBridge.setStatusInfo(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, ""));
        tcpBridge.setHandler(tcpThingHandler);
        registerThingToMockRegistry(tcpBridge);
        Supplier<ModbusManager> managerRef = () -> manager;
        doReturn(managerRef).when(tcpThingHandler).getManagerRef();
        doReturn(0).when(tcpThingHandler).getSlaveId();
        doReturn(endpoint).when(tcpThingHandler).asSlaveEndpoint();
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
        ThingUID thingUID = new ThingUID(ModbusBindingConstants.THING_TYPE_MODBUS_DATA, id);
        ThingBuilder builder = ThingBuilder.create(ModbusBindingConstants.THING_TYPE_MODBUS_DATA, thingUID)
                .withLabel("label for " + id);
        for (Entry<String, String> entry : channelToAcceptedType.entrySet()) {
            String channelId = entry.getKey();
            String channelAcceptedType = entry.getValue();
            ChannelUID channelUID = new ChannelUID(thingUID, channelId);
            builder = builder.withChannel(new Channel(channelUID, channelAcceptedType));

            if (autoCreateItemsAndLinkToChannels) {
                // Create item and link it to channel
                String itemName = channelUID.toString().replace(':', '_') + "_item";
                GenericItem item = new StringItem(itemName);
                itemRegistry.add(item);
                linkRegistry.add(new ItemChannelLink(itemName, channelUID));
            }
        }
        if (builderConfigurator != null) {
            builder = builderConfigurator.apply(builder);
        }

        Thing dataThing = builder.withBridge(bridge.getUID()).build();
        registerThingToMockRegistry(dataThing);
        hookStatusUpdates(dataThing);
        hookStateUpdates(dataThing);

        ModbusDataThingHandler dataThingHandler = new ModbusDataThingHandler(dataThing);
        hookThingRegistry(dataThingHandler);
        hookLinkRegistry(dataThingHandler);
        dataThing.setHandler(dataThingHandler);
        dataThingHandler.setCallback(thingCallback);
        if (context != null) {
            dataThingHandler.setBundleContext(context);
        }
        dataThingHandler.initialize();
        return dataThingHandler;
    }

    private void assertSingleStateUpdate(ModbusDataThingHandler handler, String channel, Matcher<State> matcher) {
        List<State> updates = stateUpdates.get(new ChannelUID(handler.getThing().getUID(), channel));
        if (updates != null) {
            assertThat(updates.size(), is(equalTo(1)));
        }
        assertThat(updates == null ? null : updates.get(0), is(matcher));
    }

    private void assertSingleStateUpdate(ModbusDataThingHandler handler, String channel, State state) {
        assertSingleStateUpdate(handler, channel, is(equalTo(state)));
    }

    //
    // /**
    // * Updates item and link registries such that added items and links are reflected in handlers
    // */
    // private void updateItemsAndLinks() {
    // itemRegistry.update();
    // linkRegistry.update();
    // }

    @Before
    public void setUp() {
        Mockito.when(thingRegistry.get(ArgumentMatchers.any())).then(invocation -> {
            ThingUID uid = (ThingUID) invocation.getArgument(0);
            for (Thing thing : things) {
                if (thing.getUID().equals(uid)) {
                    return thing;
                }
            }
            throw new IllegalArgumentException("UID is unknown: " + uid.getAsString());
        });

        Mockito.when(manager.submitOneTimeWrite(any())).then(invocation -> {
            WriteTask task = (WriteTask) invocation.getArgument(0);

            writeTasks.add(task);
            return Mockito.mock(ScheduledFuture.class);
        });
    }

    private void testOutOfBoundsGeneric(int pollStart, int pollLength, String start,
            ModbusReadFunctionCode functionCode, ValueType valueType, ThingStatus expectedStatus) {
        testOutOfBoundsGeneric(pollStart, pollLength, start, functionCode, valueType, expectedStatus, null);
    }

    @SuppressWarnings({ "null" })
    private void testOutOfBoundsGeneric(int pollStart, int pollLength, String start,
            ModbusReadFunctionCode functionCode, ValueType valueType, ThingStatus expectedStatus,
            BundleContext context) {
        ModbusSlaveEndpoint endpoint = new ModbusTCPSlaveEndpoint("thisishost", 502);

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
        assertThat(dataHandler.getThing().getStatus(), is(equalTo(expectedStatus)));

    }

    @Test
    public void testInitCoilsOutOfIndex() {
        testOutOfBoundsGeneric(4, 3, "8", ModbusReadFunctionCode.READ_COILS, ModbusConstants.ValueType.BIT,
                ThingStatus.OFFLINE);
    }

    @Test
    public void testInitCoilsOK() {
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
        testOutOfBoundsGeneric(4, 3, "6", ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                ModbusConstants.ValueType.INT16, ThingStatus.ONLINE);
    }

    @Test
    public void testInitRegistersWithInt16OutOfBounds() {
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
        ModbusSlaveEndpoint endpoint = new ModbusTCPSlaveEndpoint("thisishost", 502);

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
            assert registers == null;
            assert error == null;
            dataHandler.onBits(request, bits);
        } else if (registers != null) {
            assert bits == null;
            assert error == null;
            dataHandler.onRegisters(request, registers);
        } else {
            assert bits == null;
            assert registers == null;
            assert error != null;
            dataHandler.onError(request, error);
        }
        return dataHandler;
    }

    @SuppressWarnings({ "null" })
    private ModbusDataThingHandler testWriteHandlingGeneric(String start, String transform, ValueType valueType,
            String writeType, ModbusWriteFunctionCode successFC, String channel, Command command, Exception error,
            BundleContext context) {
        ModbusSlaveEndpoint endpoint = new ModbusTCPSlaveEndpoint("thisishost", 502);

        // Minimally mocked request
        ModbusReadRequestBlueprint request = Mockito.mock(ModbusReadRequestBlueprint.class);

        PollTask task = Mockito.mock(PollTask.class);
        doReturn(endpoint).when(task).getEndpoint();
        doReturn(request).when(task).getRequest();

        Bridge poller = createPollerMock("poller1", task);

        Configuration dataConfig = new Configuration();
        dataConfig.put("readStart", "");
        dataConfig.put("writeStart", start);
        dataConfig.put("writeTransform", transform);
        dataConfig.put("writeValueType", valueType.getConfigValue());
        dataConfig.put("writeType", writeType);

        String thingId = "write";

        ModbusDataThingHandler dataHandler = createDataHandler(thingId, poller,
                builder -> builder.withConfiguration(dataConfig), context);

        assertThat(dataHandler.getThing().getStatus(), is(equalTo(ThingStatus.ONLINE)));

        dataHandler.handleCommand(new ChannelUID(dataHandler.getThing().getUID(), channel), command);

        if (error != null) {
            dataHandler.onError(request, error);
        } else {
            ModbusResponse resp = new ModbusResponse() {

                @Override
                public int getFunctionCode() {
                    return successFC.getFunctionCode();
                }
            };
            dataHandler.onWriteResponse(Mockito.mock(ModbusWriteRequestBlueprint.class), resp);
        }
        return dataHandler;
    }

    @SuppressWarnings("null")
    @Test
    public void testOnError() {
        ModbusDataThingHandler dataHandler = testReadHandlingGeneric(ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                "0.0", "default", ModbusConstants.ValueType.BIT, null, null, new Exception("fooerror"));

        assertThat(stateUpdates.size(), is(equalTo(1)));
        assertThat(
                stateUpdates.get(
                        dataHandler.getThing().getChannel(ModbusBindingConstants.CHANNEL_LAST_READ_ERROR).getUID()),
                is(notNullValue()));
    }

    @Test
    public void testOnRegistersInt16StaticTransformation() {
        ModbusDataThingHandler dataHandler = testReadHandlingGeneric(ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                "0", "-3", ModbusConstants.ValueType.INT16, null,
                new BasicModbusRegisterArray(new ModbusRegister[] { new BasicModbusRegister((byte) 0xff, (byte) 0xfd) }),
                null);

        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_LAST_READ_SUCCESS,
                is(notNullValue(State.class)));
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_LAST_READ_ERROR,
                is(nullValue(State.class)));

        // -3 converts to "true"
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_CONTACT, is(nullValue(State.class)));
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_SWITCH, is(nullValue(State.class)));
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_DIMMER, is(nullValue(State.class)));
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_NUMBER, new DecimalType(-3));
        // roller shutter fails since -3 is invalid value (not between 0...100)
        // assertThatStateContains(state, ModbusBindingConstants.CHANNEL_ROLLERSHUTTER, new PercentType(1));
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_STRING, new StringType("-3"));
        // no datetime, conversion not possible without transformation
    }

    private void mockTransformation(String name, TransformationService service) throws InvalidSyntaxException {
        doReturn(Arrays.asList(new Object[] { null })).when(bundleContext)
                .getServiceReferences(TransformationService.class, "(smarthome.transform=" + name + ")");
        doReturn(service).when(bundleContext).getService(any());
    }

    @Test
    public void testOnRegistersRealTransformation() throws InvalidSyntaxException {
        mockTransformation("MULTIPLY", new TransformationService() {

            @Override
            public String transform(String function, String source) throws TransformationException {
                return String.valueOf(Integer.parseInt(function) * Integer.parseInt(source));
            }
        });
        ModbusDataThingHandler dataHandler = testReadHandlingGeneric(ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                "0", "MULTIPLY(10)", ModbusConstants.ValueType.INT16, null,
                new BasicModbusRegisterArray(new ModbusRegister[] { new BasicModbusRegister((byte) 0xff, (byte) 0xfd) }),
                null, bundleContext);

        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_LAST_READ_SUCCESS,
                is(notNullValue(State.class)));
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_LAST_READ_ERROR,
                is(nullValue(State.class)));

        // -3 converts to "true"
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_CONTACT, is(nullValue(State.class)));
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_SWITCH, is(nullValue(State.class)));
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_DIMMER, is(nullValue(State.class)));
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_NUMBER, new DecimalType(-30));
        // roller shutter fails since -3 is invalid value (not between 0...100)
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_ROLLERSHUTTER, is(nullValue(State.class)));
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_STRING, new StringType("-30"));
        // no datetime, conversion not possible without transformation
    }

    @Test
    public void testOnRegistersRealTransformationNoLinks() throws InvalidSyntaxException {
        mockTransformation("MULTIPLY", new TransformationService() {

            @Override
            public String transform(String function, String source) throws TransformationException {
                return String.valueOf(Integer.parseInt(function) * Integer.parseInt(source));
            }
        });
        ModbusDataThingHandler dataHandler = testReadHandlingGeneric(ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                "0", "MULTIPLY(10)", ModbusConstants.ValueType.INT16, null,
                new BasicModbusRegisterArray(new ModbusRegister[] { new BasicModbusRegister((byte) 0xff, (byte) 0xfd) }),
                null, bundleContext,
                // Not linking items and channels
                false);

        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_LAST_READ_SUCCESS,
                is(notNullValue(State.class)));
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_LAST_READ_ERROR,
                is(nullValue(State.class)));

        // Since channles are not linked, they are not updated (are null)
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_CONTACT, is(nullValue(State.class)));
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_SWITCH, is(nullValue(State.class)));
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_DIMMER, is(nullValue(State.class)));
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_NUMBER, is(nullValue(State.class)));
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_ROLLERSHUTTER, is(nullValue(State.class)));
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_STRING, is(nullValue(State.class)));
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
                new BasicModbusRegisterArray(new ModbusRegister[] { new BasicModbusRegister((byte) 0xff, (byte) 0xfd) }),
                null, bundleContext);

        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_LAST_READ_SUCCESS,
                is(notNullValue(State.class)));
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_LAST_READ_ERROR,
                is(nullValue(State.class)));

        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_CONTACT, is(nullValue(State.class)));
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_SWITCH, is(equalTo(OnOffType.ON)));
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_DIMMER, is(equalTo(OnOffType.ON)));
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_NUMBER, is(nullValue(State.class)));
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_ROLLERSHUTTER, is(nullValue(State.class)));
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_STRING, is(equalTo(new StringType("ON"))));
    }

    @Test
    public void testWriteRealTransformation() throws InvalidSyntaxException {
        mockTransformation("MULTIPLY", new TransformationService() {

            @Override
            public String transform(String function, String source) throws TransformationException {
                return String.valueOf(Integer.parseInt(function) * Integer.parseInt(source));
            }
        });
        ModbusDataThingHandler dataHandler = testWriteHandlingGeneric("50", "MULTIPLY(10)",
                ModbusConstants.ValueType.BIT, "coil", ModbusWriteFunctionCode.WRITE_COIL, "number",
                new DecimalType("2"), null, bundleContext);

        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_LAST_WRITE_SUCCESS,
                is(notNullValue(State.class)));
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_LAST_WRITE_ERROR,
                is(nullValue(State.class)));
        assertThat(writeTasks.size(), is(equalTo(1)));
        WriteTask writeTask = writeTasks.get(0);
        assertThat(writeTask.getRequest().getFunctionCode(), is(equalTo(ModbusWriteFunctionCode.WRITE_COIL)));
        assertThat(writeTask.getRequest().getReference(), is(equalTo(50)));
        assertThat(((ModbusWriteCoilRequestBlueprint) writeTask.getRequest()).getCoils().size(), is(equalTo(1)));
        // Since transform output is non-zero, it is mapped as "true"
        assertThat(((ModbusWriteCoilRequestBlueprint) writeTask.getRequest()).getCoils().getBit(0), is(equalTo(true)));
    }

    @Test
    public void testWriteRealTransformation2() throws InvalidSyntaxException {
        mockTransformation("ZERO", new TransformationService() {

            @Override
            public String transform(String function, String source) throws TransformationException {
                return "0";
            }
        });
        ModbusDataThingHandler dataHandler = testWriteHandlingGeneric("50", "ZERO(foobar)",
                ModbusConstants.ValueType.BIT, "coil", ModbusWriteFunctionCode.WRITE_COIL, "number",
                new DecimalType("2"), null, bundleContext);

        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_LAST_WRITE_SUCCESS,
                is(notNullValue(State.class)));
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_LAST_WRITE_ERROR,
                is(nullValue(State.class)));
        assertThat(writeTasks.size(), is(equalTo(1)));
        WriteTask writeTask = writeTasks.get(0);
        assertThat(writeTask.getRequest().getFunctionCode(), is(equalTo(ModbusWriteFunctionCode.WRITE_COIL)));
        assertThat(writeTask.getRequest().getReference(), is(equalTo(50)));
        assertThat(((ModbusWriteCoilRequestBlueprint) writeTask.getRequest()).getCoils().size(), is(equalTo(1)));
        // Since transform output is zero, it is mapped as "false"
        assertThat(((ModbusWriteCoilRequestBlueprint) writeTask.getRequest()).getCoils().getBit(0), is(equalTo(false)));
    }

    @Test
    public void testWriteRealTransformation3() throws InvalidSyntaxException {
        mockTransformation("RANDOM", new TransformationService() {

            @Override
            public String transform(String function, String source) throws TransformationException {
                return "5";
            }
        });
        ModbusDataThingHandler dataHandler = testWriteHandlingGeneric("50", "RANDOM(foobar)",
                ModbusConstants.ValueType.INT16, "holding", ModbusWriteFunctionCode.WRITE_SINGLE_REGISTER, "number",
                new DecimalType("2"), null, bundleContext);

        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_LAST_WRITE_SUCCESS,
                is(notNullValue(State.class)));
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_LAST_WRITE_ERROR,
                is(nullValue(State.class)));
        assertThat(writeTasks.size(), is(equalTo(1)));
        WriteTask writeTask = writeTasks.get(0);
        assertThat(writeTask.getRequest().getFunctionCode(),
                is(equalTo(ModbusWriteFunctionCode.WRITE_SINGLE_REGISTER)));
        assertThat(writeTask.getRequest().getReference(), is(equalTo(50)));
        assertThat(((ModbusWriteRegisterRequestBlueprint) writeTask.getRequest()).getRegisters().size(),
                is(equalTo(1)));
        assertThat(
                ((ModbusWriteRegisterRequestBlueprint) writeTask.getRequest()).getRegisters().getRegister(0).getValue(),
                is(equalTo(5)));
    }

    @Test
    public void testWriteRealTransformation4() throws InvalidSyntaxException {
        // assertThat(WriteRequestJsonUtilities.fromJson(55, "[{"//
        // + "\"functionCode\": 15,"//
        // + "\"address\": 5412,"//
        // + "\"value\": [1, 0, 5]"//
        // + "}]").toArray(),
        // arrayContaining((Matcher) new CoilMatcher(55, 5412, ModbusWriteFunctionCode.WRITE_MULTIPLE_COILS, true,
        // false, true)));
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

        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_LAST_WRITE_SUCCESS,
                is(notNullValue(State.class)));
        assertSingleStateUpdate(dataHandler, ModbusBindingConstants.CHANNEL_LAST_WRITE_ERROR,
                is(nullValue(State.class)));
        assertThat(writeTasks.size(), is(equalTo(2)));
        {
            WriteTask writeTask = writeTasks.get(0);
            assertThat(writeTask.getRequest().getFunctionCode(),
                    is(equalTo(ModbusWriteFunctionCode.WRITE_MULTIPLE_REGISTERS)));
            assertThat(writeTask.getRequest().getReference(), is(equalTo(5412)));
            assertThat(((ModbusWriteRegisterRequestBlueprint) writeTask.getRequest()).getRegisters().size(),
                    is(equalTo(3)));
            assertThat(((ModbusWriteRegisterRequestBlueprint) writeTask.getRequest()).getRegisters().getRegister(0)
                    .getValue(), is(equalTo(1)));
            assertThat(((ModbusWriteRegisterRequestBlueprint) writeTask.getRequest()).getRegisters().getRegister(1)
                    .getValue(), is(equalTo(0)));
            assertThat(((ModbusWriteRegisterRequestBlueprint) writeTask.getRequest()).getRegisters().getRegister(2)
                    .getValue(), is(equalTo(5)));
        }
        {
            WriteTask writeTask = writeTasks.get(1);
            assertThat(writeTask.getRequest().getFunctionCode(),
                    is(equalTo(ModbusWriteFunctionCode.WRITE_SINGLE_REGISTER)));
            assertThat(writeTask.getRequest().getReference(), is(equalTo(555)));
            assertThat(((ModbusWriteRegisterRequestBlueprint) writeTask.getRequest()).getRegisters().size(),
                    is(equalTo(1)));
            assertThat(((ModbusWriteRegisterRequestBlueprint) writeTask.getRequest()).getRegisters().getRegister(0)
                    .getValue(), is(equalTo(3)));
        }
    }

    private void testValueTypeGeneric(ModbusReadFunctionCode functionCode, ValueType valueType,
            ThingStatus expectedStatus) {
        ModbusSlaveEndpoint endpoint = new ModbusTCPSlaveEndpoint("thisishost", 502);

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

        ModbusSlaveEndpoint endpoint = new ModbusTCPSlaveEndpoint("thisishost", 502);

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

        verify(manager, never()).submitOneTimePoll(task);
        dataHandler.handleCommand(Mockito.mock(ChannelUID.class), RefreshType.REFRESH);

        // data handler asynchronously calls the poller.refresh() -- it might take some time
        // We check that refresh is finally called
        waitForAssert(() -> verify((ModbusPollerThingHandler) poller.getHandler()).refresh(), 2500, 50);
    }

    /**
     *
     * @param pollerFunctionCode poller function code. Use null if you want to have data thing direct child of endpoint
     *            thing
     * @param config thing config
     * @param statusConsumer assertion method for data thingstatus
     */
    private void testInitGeneric(ModbusReadFunctionCode pollerFunctionCode, Configuration config,
            Consumer<ThingStatusInfo> statusConsumer) {

        int pollLength = 3;

        Bridge parent;
        if (pollerFunctionCode == null) {
            parent = createTcpMock();
        } else {
            ModbusSlaveEndpoint endpoint = new ModbusTCPSlaveEndpoint("thisishost", 502);

            // Minimally mocked request
            ModbusReadRequestBlueprint request = Mockito.mock(ModbusReadRequestBlueprint.class);
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
            assertThat(status.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
            assertThat(status.getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
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
            assertThat(status.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
            assertThat(status.getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
        });
    }

    @Test
    public void testWriteHoldingBitData() {
        Configuration dataConfig = new Configuration();
        dataConfig.put("writeStart", "0");
        dataConfig.put("writeValueType", "bit");
        dataConfig.put("writeType", "holding");
        testInitGeneric(null, dataConfig, status -> {
            assertThat(status.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
            assertThat(status.getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
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
            assertThat(status.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
            assertThat(status.getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
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
                status -> assertThat(status.getStatus(), is(equalTo(ThingStatus.ONLINE))));
    }

    @Test
    public void testWriteOnlyIllegalValueType() {
        Configuration dataConfig = new Configuration();
        dataConfig.put("writeStart", "0");
        dataConfig.put("writeType", "coil");
        dataConfig.put("writeValueType", "foobar");
        testInitGeneric(ModbusReadFunctionCode.READ_COILS, dataConfig, status -> {
            assertThat(status.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
            assertThat(status.getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
        });
    }

    @Test
    public void testWriteInvalidType() {
        Configuration dataConfig = new Configuration();
        dataConfig.put("writeStart", "0");
        dataConfig.put("writeType", "foobar");
        testInitGeneric(ModbusReadFunctionCode.READ_COILS, dataConfig, status -> {
            assertThat(status.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
            assertThat(status.getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
        });
    }

    @Test
    public void testNoReadNorWrite() {
        Configuration dataConfig = new Configuration();
        testInitGeneric(ModbusReadFunctionCode.READ_COILS, dataConfig, status -> {
            assertThat(status.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
            assertThat(status.getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
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
            assertThat(status.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
            assertThat(status.getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
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
        // It's illegal to have start and transform. Just have transform or have all
        dataConfig.put("writeStart", "3");
        dataConfig.put("writeType", "holding");
        dataConfig.put("writeValueType", "int16");
        dataConfig.put("writeTransform", "JS(myJsonTransform.js)");
        testInitGeneric(null, dataConfig, status -> assertThat(status.getStatus(), is(equalTo(ThingStatus.ONLINE))));

    }
}
