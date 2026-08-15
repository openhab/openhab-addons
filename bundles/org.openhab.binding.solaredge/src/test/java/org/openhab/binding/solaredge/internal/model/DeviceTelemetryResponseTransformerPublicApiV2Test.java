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
package org.openhab.binding.solaredge.internal.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solaredge.internal.handler.ChannelProvider;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;

import com.google.gson.Gson;

/**
 * Tests transformation of actual site device telemetry response shapes.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class DeviceTelemetryResponseTransformerPublicApiV2Test {
    private final Gson gson = new Gson();
    private final Map<String, Channel> channels = new java.util.HashMap<>();
    private final DeviceTelemetryResponseTransformerPublicApiV2 transformer = createTransformer();

    @Test
    public void transformsLiveMeterValues() {
        DeviceTelemetryResponsePublicApiV2 response = parse("""
                {"meters":{"606599711":{
                  "importPower":{"unit":"W","values":[{"value":null},{"value":3.2}]},
                  "exportPower":{"unit":"W","values":[{"value":2.4}]}
                }}}
                """);

        Map<Channel, State> result = transformer.transformLive(response);

        assertEquals("3.2 W", state(result, "import"));
        assertEquals("2.4 W", state(result, "export"));
    }

    @Test
    public void acceptsFractionalBatteryStateOfEnergy() {
        DeviceTelemetryResponsePublicApiV2 response = parse("""
                {"storage":{"7E047411":{
                  "chargePower":{"unit":"W","values":[{"value":55.0}]},
                  "dischargePower":{"unit":"W","values":[{"value":118.666664}]},
                  "stateOfEnergy":{"unit":"PERCENTAGE","values":[{"value":0.9866666}]}
                }}}
                """);

        Map<Channel, State> result = transformer.transformLive(response);

        assertEquals("55 W", state(result, "battery_charge"));
        assertEquals("118.666664 W", state(result, "battery_discharge"));
        assertEquals("-63.666664 W", state(result, "battery_charge_discharge"));
        assertEquals("98.66666 %", state(result, "battery_level"));
    }

    @Test
    public void doesNotOverwriteDerivedConsumptionWhenDirectSeriesIsMissing() {
        DeviceTelemetryResponsePublicApiV2 response = parse("""
                {"meters":{"606599711":{
                  "importEnergy":{"unit":"WH","values":[{"value":515.0}]},
                  "exportEnergy":{"unit":"WH","values":[{"value":14991.0}]}
                }}}
                """);

        Map<Channel, State> result = transformer.transformAggregate(response, AggregatePeriod.DAY);

        assertEquals(false, result.containsKey(channels.get("consumption")));
        assertEquals("515 Wh", state(result, "import"));
        assertEquals("14991 Wh", state(result, "export"));
    }

    @Test
    public void ignoresNegativeStorageEnergySamples() {
        DeviceTelemetryResponsePublicApiV2 response = parse("""
                {"storage":{"7E047411":{
                  "chargeEnergy":{"unit":"WH","values":[{"value":9.0},{"value":-1576.0}]},
                  "dischargeEnergy":{"unit":"WH","values":[{"value":21.0},{"value":-40.0},{"value":147.0}]}
                }}}
                """);

        DeviceTelemetryResponseTransformerPublicApiV2.AggregateEnergies energies = transformer
                .extractAggregateEnergies(response);
        Map<Channel, State> result = transformer.transformAggregate(response, AggregatePeriod.DAY);

        assertEquals(9.0, energies.charged());
        assertEquals(168.0, energies.discharged());
        assertEquals("168 Wh", state(result, "batterySelfConsumption"));
    }

    @Test
    public void filtersDeviceEnergyByAggregatePeriodStart() {
        DeviceTelemetryResponsePublicApiV2 response = parse("""
                {"meters":{"606599711":{
                  "importEnergy":{"unit":"WH","values":[
                    {"timestamp":"2026-08-10T00:00:00+02:00","value":100.0},
                    {"timestamp":"2026-08-13T00:00:00+02:00","value":25.0}
                  ]}
                }}}
                """);

        Map<Channel, State> result = transformer.transformAggregate(response, AggregatePeriod.DAY,
                OffsetDateTime.parse("2026-08-13T00:00:00+02:00"));

        assertEquals("25 Wh", state(result, "import"));
    }

    private DeviceTelemetryResponsePublicApiV2 parse(String json) {
        return Objects.requireNonNull(gson.fromJson(json, DeviceTelemetryResponsePublicApiV2.class));
    }

    private String state(Map<Channel, State> result, String id) {
        return Objects.requireNonNull(result.get(channels.get(id))).toString();
    }

    private DeviceTelemetryResponseTransformerPublicApiV2 createTransformer() {
        ChannelProvider provider = mock(ChannelProvider.class);
        when(provider.getChannel(anyString(), anyString())).thenAnswer(invocation -> {
            String id = invocation.getArgument(1);
            return channels.computeIfAbsent(id, this::createChannel);
        });
        return new DeviceTelemetryResponseTransformerPublicApiV2(provider);
    }

    private Channel createChannel(String id) {
        Channel channel = mock(Channel.class);
        when(channel.getUID()).thenReturn(new ChannelUID(new ThingUID("solaredge:generic:test"), id));
        return channel;
    }
}
