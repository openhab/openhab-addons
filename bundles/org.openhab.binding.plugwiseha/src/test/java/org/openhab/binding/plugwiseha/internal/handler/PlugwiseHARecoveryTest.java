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
package org.openhab.binding.plugwiseha.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.plugwiseha.internal.PlugwiseHABindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openhab.binding.plugwiseha.internal.api.exception.PlugwiseHAException;
import org.openhab.binding.plugwiseha.internal.api.model.PlugwiseHAController;
import org.openhab.binding.plugwiseha.internal.api.model.dto.Appliance;
import org.openhab.binding.plugwiseha.internal.api.model.dto.Location;
import org.openhab.binding.plugwiseha.internal.api.xml.PlugwiseHAXStream;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.openhab.core.types.UnDefType;

/**
 * Tests child initialization and recovery after gateway and communication failures.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
class PlugwiseHARecoveryTest {

    private final Bridge bridge = BridgeBuilder.create(THING_TYPE_GATEWAY, "test").build();
    private final PlugwiseHABridgeHandler bridgeHandler = mock(PlugwiseHABridgeHandler.class);
    private final PlugwiseHAController controller = mock(PlugwiseHAController.class);
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private final Appliance appliance = (Appliance) new PlugwiseHAXStream().fromXML("""
            <appliance id="appliance">
                <name>Radiator</name>
                <type>thermostatic_radiator_valve</type>
                <zig_bee_node><power_source>battery</power_source></zig_bee_node>
                <point_log><type>battery</type><measurement>0.5</measurement></point_log>
                <point_log><type>temperature</type><unit>C</unit><measurement>21</measurement></point_log>
            </appliance>
            """);

    @BeforeEach
    void setUp() throws PlugwiseHAException {
        bridge.setHandler(bridgeHandler);
        bridge.setStatusInfo(status(ThingStatus.ONLINE));
        when(bridgeHandler.getThing()).thenReturn(bridge);
        when(bridgeHandler.getController()).thenReturn(controller);
        when(callback.getBridge(bridge.getUID())).thenReturn(bridge);
        when(callback.isChannelLinked(any(ChannelUID.class))).thenReturn(true);
        doAnswer(invocation -> {
            Thing thing = invocation.getArgument(0);
            thing.setStatusInfo(invocation.getArgument(1));
            return null;
        }).when(callback).statusUpdated(any(Thing.class), any(ThingStatusInfo.class));
        when(controller.getAppliance("appliance")).thenReturn(appliance);
    }

    @Test
    void applianceRecoversAfterItsOwnCommunicationFailure() throws PlugwiseHAException {
        PlugwiseHAApplianceHandler handler = applianceHandler("appliance");
        handler.initialize();
        when(controller.getAppliance("appliance")).thenThrow(new PlugwiseHAException("Temporary failure"))
                .thenReturn(appliance);

        handler.refresh();

        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, handler.getThing().getStatusInfo().getStatusDetail());
        ChannelUID temperature = new ChannelUID(handler.getThing().getUID(), APPLIANCE_TEMPERATURE_CHANNEL);
        verify(callback).stateUpdated(temperature, UnDefType.UNDEF);

        handler.refresh();

        assertEquals(ThingStatus.ONLINE, handler.getThing().getStatus());
        verify(callback).stateUpdated(temperature, new QuantityType<>(21, SIUnits.CELSIUS));
        assertBatteryChannels(handler.getThing());
    }

    @ParameterizedTest
    @EnumSource(value = ThingStatus.class, names = { "OFFLINE", "INITIALIZING" })
    void applianceWaitsForBridgeAndCompletesInitializationDuringPolling(ThingStatus bridgeStatus)
            throws PlugwiseHAException {
        bridge.setStatusInfo(status(bridgeStatus));
        PlugwiseHAApplianceHandler handler = applianceHandler("appliance");

        handler.initialize();

        assertEquals(ThingStatusDetail.BRIDGE_OFFLINE, handler.getThing().getStatusInfo().getStatusDetail());
        verify(controller, never()).getAppliance(anyString());

        bridge.setStatusInfo(status(ThingStatus.ONLINE));
        handler.bridgeStatusChanged(bridge.getStatusInfo());
        assertEquals(ThingStatus.OFFLINE, handler.getThing().getStatus());
        handler.refresh();

        assertEquals(ThingStatus.ONLINE, handler.getThing().getStatus());
        assertBatteryChannels(handler.getThing());
        assertEquals("thermostatic_radiator_valve", handler.getThing().getProperties().get(APPLIANCE_PROPERTY_TYPE));

        bridge.setStatusInfo(status(ThingStatus.OFFLINE));
        handler.bridgeStatusChanged(bridge.getStatusInfo());
        bridge.setStatusInfo(status(ThingStatus.ONLINE));
        handler.bridgeStatusChanged(bridge.getStatusInfo());
        handler.refresh();

        assertEquals(ThingStatus.ONLINE, handler.getThing().getStatus());
        assertBatteryChannels(handler.getThing());
    }

    @Test
    void zoneCompletesInitializationAfterBridgeRecovery() throws PlugwiseHAException {
        Location location = (Location) new PlugwiseHAXStream().fromXML("""
                <location id="location"><name>Living room</name><type>livingroom</type></location>
                """);
        when(controller.getLocation("location")).thenReturn(location);
        bridge.setStatusInfo(status(ThingStatus.OFFLINE));
        PlugwiseHAZoneHandler handler = new PlugwiseHAZoneHandler(thing(THING_TYPE_ZONE, "location"));
        handler.setCallback(callback);
        handler.initialize();

        bridge.setStatusInfo(status(ThingStatus.ONLINE));
        handler.bridgeStatusChanged(bridge.getStatusInfo());
        handler.refresh();

        assertEquals(ThingStatus.ONLINE, handler.getThing().getStatus());
        assertEquals("livingroom", handler.getThing().getProperties().get(LOCATION_PROPERTY_TYPE));
    }

    @Test
    void applianceCanAppearAfterInitialLookup() throws PlugwiseHAException {
        when(controller.getAppliance("appliance")).thenReturn(null, appliance);
        PlugwiseHAApplianceHandler handler = applianceHandler("appliance");
        handler.initialize();
        assertEquals(ThingStatus.OFFLINE, handler.getThing().getStatus());

        handler.refresh();

        assertEquals(ThingStatus.ONLINE, handler.getThing().getStatus());
        assertBatteryChannels(handler.getThing());
    }

    @Test
    void bridgeRecoveryDoesNotOverrideInvalidConfiguration() throws PlugwiseHAException {
        PlugwiseHAApplianceHandler handler = applianceHandler("");
        handler.initialize();

        handler.bridgeStatusChanged(bridge.getStatusInfo());
        handler.refresh();

        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, handler.getThing().getStatusInfo().getStatusDetail());
        verify(controller, never()).getAppliance(anyString());
    }

    @Test
    void pollingDoesNotInitializeAnUninitializedThing() throws PlugwiseHAException {
        PlugwiseHAApplianceHandler handler = applianceHandler("appliance");
        handler.getThing().setStatusInfo(status(ThingStatus.UNINITIALIZED));

        handler.refresh();

        assertEquals(ThingStatus.UNINITIALIZED, handler.getThing().getStatus());
        verify(controller, never()).getAppliance(anyString());
        verify(callback, never()).statusUpdated(any(Thing.class), any(ThingStatusInfo.class));
    }

    private PlugwiseHAApplianceHandler applianceHandler(String id) {
        Thing thing = thing(THING_TYPE_APPLIANCE_VALVE, id);
        Thing withChannel = ThingBuilder.create(thing.getThingTypeUID(), thing.getUID()).withBridge(bridge.getUID())
                .withConfiguration(thing.getConfiguration())
                .withChannel(ChannelBuilder
                        .create(new ChannelUID(thing.getUID(), APPLIANCE_TEMPERATURE_CHANNEL), "Number:Temperature")
                        .build())
                .build();
        withChannel.setStatusInfo(status(ThingStatus.INITIALIZING));
        PlugwiseHAApplianceHandler handler = new PlugwiseHAApplianceHandler(withChannel);
        handler.setCallback(callback);
        return handler;
    }

    private Thing thing(ThingTypeUID type, String id) {
        Thing thing = ThingBuilder.create(type, "test").withBridge(bridge.getUID())
                .withConfiguration(new Configuration(Map.of("id", id))).build();
        thing.setStatusInfo(status(ThingStatus.INITIALIZING));
        return thing;
    }

    private ThingStatusInfo status(ThingStatus status) {
        return ThingStatusInfoBuilder.create(status, ThingStatusDetail.NONE).build();
    }

    private void assertBatteryChannels(Thing thing) {
        assertNotNull(thing.getChannel(APPLIANCE_BATTERYLEVEL_CHANNEL));
        assertNotNull(thing.getChannel(APPLIANCE_BATTERYLEVELLOW_CHANNEL));
        assertEquals(3, thing.getChannels().size());
        assertEquals(3, thing.getChannels().stream().map(channel -> channel.getUID()).distinct().count());
    }
}
