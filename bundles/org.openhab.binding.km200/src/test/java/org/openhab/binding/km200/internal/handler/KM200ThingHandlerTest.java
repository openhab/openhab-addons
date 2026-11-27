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
package org.openhab.binding.km200.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.binding.km200.internal.KM200BindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.km200.internal.KM200ChannelTypeProvider;
import org.openhab.binding.km200.internal.KM200Device;
import org.openhab.binding.km200.internal.KM200ServiceObject;
import org.openhab.core.config.core.ConfigUtil;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Tests service selection and configuration errors for file-defined KM200 Things.
 *
 * @author Leo Siepel - Initial contribution
 */
@Execution(ExecutionMode.SAME_THREAD)
@ResourceLock(value = "org.openhab.core.config.core.ConfigUtil", mode = ResourceAccessMode.READ_WRITE)
@NonNullByDefault
class KM200ThingHandlerTest {
    private static final ThingUID BRIDGE_UID = new ThingUID(THING_TYPE_KMDEVICE, "gateway");
    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_HEATING_CIRCUIT, BRIDGE_UID, "hc1");

    private final KM200Device device = new KM200Device(mock(HttpClient.class));
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);

    @BeforeEach
    void setUp() {
        Bridge bridge = mock(Bridge.class);
        KM200GatewayHandler gateway = mock(KM200GatewayHandler.class);
        when(callback.getBridge(BRIDGE_UID)).thenReturn(bridge);
        when(bridge.getHandler()).thenReturn(gateway);
        when(gateway.getDevice()).thenReturn(device);
        device.setInited(true);

        KM200ServiceObject circuits = new KM200ServiceObject("/heatingCircuits", DATA_TYPE_REF_ENUM, 1, 0, 0, 0, null);
        KM200ServiceObject circuit = new KM200ServiceObject("/heatingCircuits/hc1", DATA_TYPE_REF_ENUM, 1, 0, 0, 0,
                "/heatingCircuits");
        circuit.serviceTreeMap.put("mode", new KM200ServiceObject("/heatingCircuits/hc1/mode", DATA_TYPE_STRING_VALUE,
                1, 1, 0, 0, "/heatingCircuits/hc1"));
        circuits.serviceTreeMap.put("hc1", circuit);
        device.serviceTreeMap.put("heatingCircuits", circuits);
    }

    @Test
    void configurationOverridesThingTypeProperty() {
        Thing thing = createThing("/heatingCircuits/hc1", "#heatingCircuits");
        createHandler(thing).initialize();
        assertCircuitChannels();
        assertStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
    }

    @Test
    void resolvesConfiguredRootEnvironmentVariable() {
        ConfigUtilAccessor.setEnvironment(Map.of("KM200_ROOT_SERVICE", "/heatingCircuits/hc1"));
        try {
            Thing thing = createThing("${ENV:KM200_ROOT_SERVICE}", "#heatingCircuits");
            createHandler(thing).initialize();
            assertCircuitChannels();
            assertStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            assertEquals("${ENV:KM200_ROOT_SERVICE}", thing.getConfiguration().get(CONFIG_ROOT_SERVICE));
        } finally {
            ConfigUtilAccessor.resetEnvironment();
        }
    }

    @Test
    void configurationWorksWithoutRootProperty() {
        Thing thing = createThing("/heatingCircuits/hc1", null);
        createHandler(thing).initialize();
        assertCircuitChannels();
        assertStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
    }

    @Test
    void configurationOverridesEmptyRootProperty() {
        createHandler(createThing("/heatingCircuits/hc1", "")).initialize();
        assertCircuitChannels();
    }

    @Test
    void discoveredRootRemainsTheDefault() {
        Thing thing = createThing(null, "#heatingCircuits#hc1");
        createHandler(thing).initialize();
        assertCircuitChannels();
        assertStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "/", "////", "heatingCircuits", "heatingCircuits/hc1", "/unknown",
            "/heatingCircuits/hc2", "/heatingCircuits/hc1/mode", "/heatingCircuits/hc1/switchPrograms/unknown" })
    void invalidConfigurationPreservesChannelsAndReportsConfigurationError(String rootService) {
        Channel existing = ChannelBuilder.create(new ChannelUID(THING_UID, "existing"), "String").build();
        Thing thing = ThingBuilder.create(createThing(rootService, "#heatingCircuits#hc1")).withChannel(existing)
                .build();
        createHandler(thing).initialize();
        assertStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        verify(callback, never()).thingUpdated(any());
        assertSame(existing, thing.getChannel("existing"));
    }

    @Test
    void missingRootReportsConfigurationError() {
        Thing thing = createThing(null, null);
        createHandler(thing).initialize();
        assertStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        verify(callback, never()).thingUpdated(any());
    }

    @Test
    void waitsForGatewayInitializationBeforeCheckingServiceAvailability() {
        device.setInited(false);
        Thing thing = createThing("/heatingCircuits/hc1", "#heatingCircuits");
        KM200ThingHandler handler = createHandler(thing);
        handler.initialize();
        assertStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);
        verify(callback, never()).thingUpdated(any());

        device.setInited(true);
        handler.initialize();
        assertCircuitChannels();
        assertStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
    }

    @Test
    void recoversWhenTheConfiguredPathIsCorrected() {
        Thing thing = createThing("/heatingCircuits/hc2", "#heatingCircuits");
        KM200ThingHandler handler = createHandler(thing);
        handler.initialize();
        assertStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);

        handler.thingUpdated(createThing("/heatingCircuits/hc1", "#heatingCircuits"));
        assertCircuitChannels();
        assertStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
    }

    @Test
    void discoveredVirtualSwitchProgramStillCreatesChannels() {
        Map<String, String> properties = new HashMap<>();
        properties.put("root", "#heatingCircuits#hc1#switchPrograms#__current__");
        properties.put(SWITCH_PROGRAM_CURRENT_PATH_NAME, "#heatingCircuits#hc1#activeSwitchProgram");
        properties.put(SWITCH_PROGRAM_POSITIVE, "day");
        properties.put(SWITCH_PROGRAM_NEGATIVE, "night");
        Thing thing = ThingBuilder.create(THING_TYPE_HEATING_CIRCUIT, THING_UID).withBridge(BRIDGE_UID)
                .withProperties(properties).build();
        createHandler(thing).initialize();

        ArgumentCaptor<Thing> updated = ArgumentCaptor.forClass(Thing.class);
        verify(callback).thingUpdated(updated.capture());
        assertEquals(5, updated.getValue().getChannels().size());
        Channel weekday = updated.getValue().getChannel("weekday");
        assertNotNull(weekday);
        assertEquals("#heatingCircuits#hc1#switchPrograms#__current__#weekday", weekday.getProperties().get("root"));
        assertEquals(properties.get(SWITCH_PROGRAM_CURRENT_PATH_NAME),
                weekday.getProperties().get(SWITCH_PROGRAM_CURRENT_PATH_NAME));
        assertStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
    }

    private Thing createThing(@Nullable String rootService, @Nullable String rootProperty) {
        Configuration configuration = new Configuration();
        if (rootService != null) {
            configuration.put(CONFIG_ROOT_SERVICE, rootService);
        }
        return ThingBuilder.create(THING_TYPE_HEATING_CIRCUIT, THING_UID).withBridge(BRIDGE_UID)
                .withConfiguration(configuration)
                .withProperties(rootProperty == null ? Map.of() : Map.of("root", rootProperty)).build();
    }

    private KM200ThingHandler createHandler(Thing thing) {
        KM200ThingHandler handler = new KM200ThingHandler(thing, new KM200ChannelTypeProvider());
        handler.setCallback(callback);
        return handler;
    }

    private void assertCircuitChannels() {
        ArgumentCaptor<Thing> updated = ArgumentCaptor.forClass(Thing.class);
        verify(callback).thingUpdated(updated.capture());
        assertEquals(1, updated.getValue().getChannels().size());
        Channel mode = updated.getValue().getChannel("mode");
        assertNotNull(mode);
        assertEquals("#heatingCircuits#hc1#mode", mode.getProperties().get("root"));
    }

    private static class ConfigUtilAccessor extends ConfigUtil {
        static void setEnvironment(Map<String, String> values) {
            setEnvProvider(values::get);
        }

        static void resetEnvironment() {
            setEnvProvider(System::getenv);
        }
    }

    private void assertStatus(ThingStatus status, ThingStatusDetail detail) {
        ArgumentCaptor<ThingStatusInfo> statuses = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, atLeastOnce()).statusUpdated(any(Thing.class), statuses.capture());
        ThingStatusInfo lastStatus = statuses.getAllValues().getLast();
        assertEquals(status, lastStatus.getStatus());
        assertEquals(detail, lastStatus.getStatusDetail());
    }
}
