/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.modbus;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
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
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.openhab.binding.modbus.handler.ModbusPollerThingHandlerImpl;
import org.openhab.binding.modbus.handler.ModbusTcpThingHandler;
import org.openhab.io.transport.modbus.ModbusManager;
import org.openhab.io.transport.modbus.ModbusManager.PollTask;
import org.openhab.io.transport.modbus.ModbusReadCallback;
import org.openhab.io.transport.modbus.ModbusReadFunctionCode;

@RunWith(MockitoJUnitRunner.class)
public class ModbusPollerThingHandlerTest {

    @Mock
    private ModbusManager modbusManager;

    @Mock
    private ThingRegistry thingRegistry;

    private Bridge endpoint;
    private Bridge poller;
    private List<Thing> things = new ArrayList<>();

    private ModbusTcpThingHandler tcpThingHandler;

    @Mock
    private ThingHandlerCallback thingCallback;

    public static BridgeBuilder createTcpThingBuilder(String id) {
        return BridgeBuilder.create(ModbusBindingConstants.THING_TYPE_MODBUS_TCP,
                new ThingUID(ModbusBindingConstants.THING_TYPE_MODBUS_TCP, id)).withLabel("label for " + id);
    }

    public static BridgeBuilder createPollerThingBuilder(String id) {
        return BridgeBuilder.create(ModbusBindingConstants.THING_TYPE_MODBUS_POLLER,
                new ThingUID(ModbusBindingConstants.THING_TYPE_MODBUS_POLLER, id)).withLabel("label for " + id);
    }

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

    /**
     * Before each test, setup TCP endpoint thing, configure mocked item registry
     */
    @SuppressWarnings("null")
    @Before
    public void setUp() {
        Mockito.when(thingRegistry.get(Matchers.any())).then(invocation -> {
            ThingUID uid = invocation.getArgumentAt(0, ThingUID.class);
            for (Thing thing : things) {
                if (thing.getUID().equals(uid)) {
                    return thing;
                }
            }
            throw new IllegalArgumentException("UID is unknown: " + uid.getAsString());
        });

        Configuration tcpConfig = new Configuration();
        tcpConfig.put("host", "thisishost");
        tcpConfig.put("port", 44);
        tcpConfig.put("id", 9);
        endpoint = createTcpThingBuilder("tcpendpoint").withConfiguration(tcpConfig).build();

        hookStatusUpdates(endpoint);

        tcpThingHandler = new ModbusTcpThingHandler(endpoint, () -> modbusManager);
        tcpThingHandler.setCallback(thingCallback);
        endpoint.setHandler(tcpThingHandler);
        registerThingToMockRegistry(endpoint);
        tcpThingHandler.initialize();

        assertThat(endpoint.getStatus(), is(equalTo(ThingStatus.ONLINE)));
        // no need to test endpoint otherwise, see other unit tests
        // start tracking assertions separately for the tests
        reset(modbusManager);
    }

    private void hookItemRegistry(ThingHandler thingHandler)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        Field thingRegisteryField = BaseThingHandler.class.getDeclaredField("thingRegistry");
        thingRegisteryField.setAccessible(true);
        thingRegisteryField.set(thingHandler, thingRegistry);
    }

    @SuppressWarnings("null")
    private void hookStatusUpdates(Thing thing) {
        Mockito.doAnswer(invocation -> {
            thing.setStatusInfo(invocation.getArgumentAt(1, ThingStatusInfo.class));
            return null;
        }).when(thingCallback).statusUpdated(Matchers.same(thing), Matchers.any());
    }

    @SuppressWarnings("null")
    @Test
    public void testInitializeNonPolling()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 0L); // 0 -> non polling
        pollerConfig.put("start", 5);
        pollerConfig.put("length", 9);
        pollerConfig.put("type", ModbusBindingConstants.READ_TYPE_HOLDING_REGISTER);
        poller = createPollerThingBuilder("poller").withConfiguration(pollerConfig).withBridge(endpoint.getUID())
                .build();
        registerThingToMockRegistry(poller);
        hookStatusUpdates(poller);

        ModbusPollerThingHandlerImpl pollerThingHandler = new ModbusPollerThingHandlerImpl(poller, () -> modbusManager);
        hookItemRegistry(pollerThingHandler);
        pollerThingHandler.setCallback(thingCallback);
        poller.setHandler(pollerThingHandler);
        pollerThingHandler.initialize();
        assertThat(poller.getStatus(), is(equalTo(ThingStatus.ONLINE)));

        // polling not setup
        verifyZeroInteractions(modbusManager);
    }

    @SuppressWarnings("null")
    public void testPollingGeneric(String type, Supplier<Matcher<PollTask>> pollTaskMatcherSupplier)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 150L);
        pollerConfig.put("start", 5);
        pollerConfig.put("length", 13);
        pollerConfig.put("type", type);
        poller = createPollerThingBuilder("poller").withConfiguration(pollerConfig).withBridge(endpoint.getUID())
                .build();
        registerThingToMockRegistry(poller);

        hookStatusUpdates(poller);

        ModbusPollerThingHandlerImpl thingHandler = new ModbusPollerThingHandlerImpl(poller, () -> modbusManager);
        thingHandler.setCallback(thingCallback);
        poller.setHandler(thingHandler);
        hookItemRegistry(thingHandler);

        thingHandler.initialize();
        assertThat(poller.getStatus(), is(equalTo(ThingStatus.ONLINE)));

        verify(modbusManager).registerRegularPoll(argThat(pollTaskMatcherSupplier.get()), eq(150l), eq(0L));
        verifyNoMoreInteractions(modbusManager);
    }

    private boolean checkPollTask(PollTask item, ModbusReadFunctionCode functionCode) {
        return item.getEndpoint().equals(tcpThingHandler.asSlaveEndpoint()) && item.getRequest().getDataLength() == 13
                && item.getRequest().getFunctionCode() == functionCode && item.getRequest().getProtocolID() == 0
                && item.getRequest().getReference() == 5 && item.getRequest().getUnitID() == 9;
    }

    Matcher<PollTask> isRequestOkGeneric(ModbusReadFunctionCode functionCode) {
        return new TypeSafeMatcher<PollTask>() {
            @Override
            public boolean matchesSafely(PollTask item) {
                return checkPollTask(item, functionCode);
            }

            @Override
            public void describeTo(Description description) {

            }
        };
    }

    Matcher<PollTask> okCoilRequest() {
        return new TypeSafeMatcher<PollTask>() {
            @Override
            public boolean matchesSafely(PollTask item) {
                // we are not testing the callback at all!
                return item.getEndpoint().equals(tcpThingHandler.asSlaveEndpoint())
                        && item.getRequest().getDataLength() == 13
                        && item.getRequest().getFunctionCode() == ModbusReadFunctionCode.READ_COILS
                        && item.getRequest().getProtocolID() == 0 && item.getRequest().getReference() == 5
                        && item.getRequest().getUnitID() == 9;
            }

            @Override
            public void describeTo(Description description) {

            }
        };
    }

    @Test
    public void testInitializePollingWithCoils()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        testPollingGeneric("coil", () -> isRequestOkGeneric(ModbusReadFunctionCode.READ_COILS));
    }

    @Test
    public void testInitializePollingWithDiscrete()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        testPollingGeneric("discrete", () -> isRequestOkGeneric(ModbusReadFunctionCode.READ_INPUT_DISCRETES));
    }

    @Test
    public void testInitializePollingWithInputRegisters()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        testPollingGeneric("input", () -> isRequestOkGeneric(ModbusReadFunctionCode.READ_INPUT_REGISTERS));
    }

    @Test
    public void testInitializePollingWithHoldingRegisters()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        testPollingGeneric("holding", () -> isRequestOkGeneric(ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS));
    }

    @SuppressWarnings("null")
    @Test
    public void testDisconnectOnDispose()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 150L);
        pollerConfig.put("start", 5);
        pollerConfig.put("length", 13);
        pollerConfig.put("type", "coil");
        poller = createPollerThingBuilder("poller").withConfiguration(pollerConfig).withBridge(endpoint.getUID())
                .build();
        registerThingToMockRegistry(poller);
        hookStatusUpdates(poller);

        ModbusPollerThingHandlerImpl thingHandler = new ModbusPollerThingHandlerImpl(poller, () -> modbusManager);
        thingHandler.setCallback(thingCallback);
        poller.setHandler(thingHandler);
        hookItemRegistry(thingHandler);

        thingHandler.initialize();

        // verify registration
        final AtomicReference<ModbusReadCallback> callbackRef = new AtomicReference<>();
        verify(modbusManager).registerRegularPoll(argThat(new TypeSafeMatcher<PollTask>() {

            @Override
            public void describeTo(Description description) {
            }

            @Override
            protected boolean matchesSafely(PollTask item) {
                callbackRef.set(item.getCallback());
                return checkPollTask(item, ModbusReadFunctionCode.READ_COILS);

            }
        }), eq(150l), eq(0L));
        verifyNoMoreInteractions(modbusManager);

        // reset call counts for easy assertions
        reset(modbusManager);

        thingHandler.dispose();

        // 1) should first unregister poll task
        verify(modbusManager).unregisterRegularPoll(argThat(new TypeSafeMatcher<PollTask>() {

            @Override
            public void describeTo(Description description) {
            }

            @Override
            protected boolean matchesSafely(PollTask item) {
                assertThat(item.getCallback(), is(sameInstance(callbackRef.get())));
                return checkPollTask(item, ModbusReadFunctionCode.READ_COILS);

            }
        }));

        verifyNoMoreInteractions(modbusManager);

    }

    @SuppressWarnings("null")
    @Test
    public void testInitializeWithNoBridge()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 150L);
        pollerConfig.put("start", 5);
        pollerConfig.put("length", 13);
        pollerConfig.put("type", "coil");
        poller = createPollerThingBuilder("poller").withConfiguration(pollerConfig).build();
        registerThingToMockRegistry(poller);
        hookStatusUpdates(poller);

        ModbusPollerThingHandlerImpl thingHandler = new ModbusPollerThingHandlerImpl(poller, () -> modbusManager);
        thingHandler.setCallback(thingCallback);
        poller.setHandler(thingHandler);
        hookItemRegistry(thingHandler);

        thingHandler.initialize();
        assertThat(poller.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
        assertThat(poller.getStatusInfo().getStatusDetail(), is(equalTo(ThingStatusDetail.BRIDGE_OFFLINE)));

        verifyNoMoreInteractions(modbusManager);

    }

    @SuppressWarnings("null")
    @Test
    public void testInitializeWithOfflineBridge()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        Configuration pollerConfig = new Configuration();
        pollerConfig.put("refresh", 150L);
        pollerConfig.put("start", 5);
        pollerConfig.put("length", 13);
        pollerConfig.put("type", "coil");

        poller = createPollerThingBuilder("poller").withConfiguration(pollerConfig).withBridge(endpoint.getUID())
                .build();
        registerThingToMockRegistry(poller);
        hookStatusUpdates(poller);

        ModbusPollerThingHandlerImpl thingHandler = new ModbusPollerThingHandlerImpl(poller, () -> modbusManager);
        thingHandler.setCallback(thingCallback);
        poller.setHandler(thingHandler);
        hookItemRegistry(thingHandler);

        endpoint.setStatusInfo(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, ""));
        thingHandler.initialize();
        assertThat(poller.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
        assertThat(poller.getStatusInfo().getStatusDetail(), is(equalTo(ThingStatusDetail.BRIDGE_OFFLINE)));

        verifyNoMoreInteractions(modbusManager);
    }

}
