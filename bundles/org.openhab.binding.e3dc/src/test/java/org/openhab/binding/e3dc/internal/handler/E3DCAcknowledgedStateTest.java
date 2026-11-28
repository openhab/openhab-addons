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
package org.openhab.binding.e3dc.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.e3dc.internal.E3DCBindingConstants;
import org.openhab.binding.e3dc.internal.E3DCCommandOptionsProvider;
import org.openhab.binding.e3dc.internal.rscp.RscpException;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * Tests the state a handler publishes for a write the device acknowledged: a measurement has to carry the unit of
 * its channel, because a plain number is taken as being in the unit of the item.
 *
 * <p>
 * {@code @SuppressWarnings("null")}: Mockito is not designed with null type annotations in mind, so combining it
 * with this {@code @NonNullByDefault} test class produces "unsafe interpretation" compiler advisories with no
 * null-safety benefit.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
class E3DCAcknowledgedStateTest {

    private static final long WAIT_MILLIS = 10_000;

    private static final ThingUID BRIDGE_UID = new ThingUID(E3DCBindingConstants.THING_TYPE_BRIDGE, "device");

    /** A handler together with the channel whose write it acknowledges. */
    private record Fixture(E3DCThingHandler handler, ThingHandlerCallback callback, ChannelUID channelUID) {
    }

    /** Builds a handler with one channel of the given item type, behind a bridge that acknowledges every write. */
    private static Fixture fixture(ThingTypeUID thingType, Function<Thing, E3DCThingHandler> factory, String group,
            String channelId, String acceptedItemType) throws RscpException {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        E3DCBridgeHandler bridgeHandler = mock(E3DCBridgeHandler.class);
        when(bridgeHandler.sendRequest(any())).thenReturn(RscpValue.container(0, List.of()));
        Bridge bridge = BridgeBuilder.create(E3DCBindingConstants.THING_TYPE_BRIDGE, BRIDGE_UID).build();
        bridge.setHandler(bridgeHandler);
        when(callback.getBridge(BRIDGE_UID)).thenReturn(bridge);
        ThingUID thingUID = new ThingUID(thingType, BRIDGE_UID, "child");
        ChannelUID channelUID = new ChannelUID(thingUID, group, channelId);
        Thing thing = ThingBuilder.create(thingType, thingUID).withBridge(BRIDGE_UID)
                .withChannel(ChannelBuilder.create(channelUID, acceptedItemType).build()).build();
        E3DCThingHandler handler = factory.apply(thing);
        handler.setCallback(callback);
        return new Fixture(handler, callback, channelUID);
    }

    private static Fixture powerPlant(String group, String channelId, String acceptedItemType) throws RscpException {
        return fixture(E3DCBindingConstants.THING_TYPE_POWER_PLANT,
                thing -> new E3DCPowerPlantHandler(thing, mock(E3DCCommandOptionsProvider.class)), group, channelId,
                acceptedItemType);
    }

    private static Fixture wallbox(String group, String channelId, String acceptedItemType) throws RscpException {
        return fixture(E3DCBindingConstants.THING_TYPE_WALLBOX, E3DCWallboxHandler::new, group, channelId,
                acceptedItemType);
    }

    /** Sends a command and returns the state the acknowledged write publishes. */
    private static State stateAfterCommand(Fixture fixture, Command command) {
        fixture.handler().handleCommand(fixture.channelUID(), command);
        ArgumentCaptor<State> state = ArgumentCaptor.forClass(State.class);
        verify(fixture.callback(), timeout(WAIT_MILLIS)).stateUpdated(eq(fixture.channelUID()), state.capture());
        fixture.handler().dispose();
        return state.getValue();
    }

    private static QuantityType<?> quantityOf(State state) {
        assertTrue(state instanceof QuantityType<?>, "the acknowledged state must be a quantity, but was " + state);
        return (QuantityType<?>) state;
    }

    /** A 2 kW command reaches the device as 2000 W, which must be published as that and not as a plain number. */
    @Test
    void anAcknowledgedPowerWriteIsPublishedInWatts() throws RscpException {
        Fixture fixture = powerPlant(E3DCBindingConstants.GROUP_BATTERY_LIMITS, "max-charge-power", "Number:Power");

        QuantityType<?> power = quantityOf(
                stateAfterCommand(fixture, new QuantityType<>(2, MetricPrefix.KILO(Units.WATT))));

        assertEquals(Units.WATT, power.getUnit());
        assertEquals(2000.0, power.doubleValue(), 0.001);
    }

    @Test
    void anAcknowledgedCurrentWriteIsPublishedInAmperes() throws RscpException {
        Fixture fixture = wallbox(E3DCBindingConstants.GROUP_WALLBOX_STATUS_CONTROL, "max-charge-current",
                "Number:ElectricCurrent");

        QuantityType<?> current = quantityOf(
                stateAfterCommand(fixture, new QuantityType<>(16_000, MetricPrefix.MILLI(Units.AMPERE))));

        assertEquals(Units.AMPERE, current.getUnit());
        assertEquals(16.0, current.doubleValue(), 0.001);
    }

    /** A write states the reserve in Wh, so unlike the energy poll it is not scaled to kWh. */
    @Test
    void anAcknowledgedEnergyWriteIsPublishedInWattHours() throws RscpException {
        Fixture fixture = powerPlant(E3DCBindingConstants.GROUP_EMERGENCY, "energy", "Number:Energy");

        QuantityType<?> energy = quantityOf(stateAfterCommand(fixture, new QuantityType<>(450, Units.WATT_HOUR)));

        assertEquals(Units.WATT_HOUR, energy.getUnit());
        assertEquals(450.0, energy.doubleValue(), 0.001);
    }

    @Test
    void anAcknowledgedPercentageWriteIsPublishedInPercent() throws RscpException {
        Fixture fixture = powerPlant(E3DCBindingConstants.GROUP_EMERGENCY, "percent", "Number:Dimensionless");

        QuantityType<?> percent = quantityOf(stateAfterCommand(fixture, new QuantityType<>(0.5, Units.ONE)));

        assertEquals(Units.PERCENT, percent.getUnit());
        assertEquals(50.0, percent.doubleValue(), 0.001);
    }

    /** A channel whose item type carries no unit keeps the plain number it published before. */
    @Test
    void anAcknowledgedWriteOnAPlainNumberChannelStaysAPlainNumber() throws RscpException {
        Fixture fixture = wallbox(E3DCBindingConstants.GROUP_WALLBOX_STATUS_CONTROL, "number-phases", "Number");

        State state = stateAfterCommand(fixture, new DecimalType(3));

        assertEquals(new DecimalType(3), state);
    }

    @Test
    void anAcknowledgedSwitchWriteStaysASwitchState() throws RscpException {
        Fixture fixture = wallbox(E3DCBindingConstants.GROUP_WALLBOX_STATUS_CONTROL, "sun-mode", "Switch");

        assertEquals(OnOffType.ON, stateAfterCommand(fixture, OnOffType.ON));
    }
}
