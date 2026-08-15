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
 * Tests transformation of actual SolarEdge Monitoring API V2 response shapes.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class MeasurementsResponseTransformerPublicApiV2Test {
    private final Gson gson = new Gson();

    @Test
    public void transformsUppercaseWattHours() {
        Channel channel = createChannel();
        MeasurementsResponseTransformerPublicApiV2 transformer = createTransformer(channel);
        MeasurementsResponsePublicApiV2 response = Objects.requireNonNull(gson.fromJson("""
                {"unit":"WH","resolution":"DAY","values":[
                  {"timestamp":"2026-08-13T00:00:00+02:00","value":48432.227}
                ]}
                """, MeasurementsResponsePublicApiV2.class));

        Map<Channel, State> result = transformer.transformEnergy(response, AggregatePeriod.DAY);

        assertEquals("48432.227 Wh", Objects.requireNonNull(result.get(channel)).toString());
    }

    @Test
    public void usesLatestNonNullPowerBucket() {
        Channel channel = createChannel();
        MeasurementsResponseTransformerPublicApiV2 transformer = createTransformer(channel);
        MeasurementsResponsePublicApiV2 response = Objects.requireNonNull(gson.fromJson("""
                {"unit":"W","resolution":"QUARTER_HOUR","values":[
                  {"timestamp":"2026-08-13T20:00:00+02:00","value":327.33334},
                  {"timestamp":"2026-08-13T20:15:00+02:00","value":0.0},
                  {"timestamp":"2026-08-13T20:30:00+02:00","value":null}
                ]}
                """, MeasurementsResponsePublicApiV2.class));

        Map<Channel, State> result = transformer.transformPower(response);

        assertEquals("0 W", Objects.requireNonNull(result.get(channel)).toString());
    }

    @Test
    public void filtersDailyBucketsByAggregatePeriodStart() {
        Channel channel = createChannel();
        MeasurementsResponseTransformerPublicApiV2 transformer = createTransformer(channel);
        MeasurementsResponsePublicApiV2 response = Objects.requireNonNull(gson.fromJson("""
                {"unit":"WH","resolution":"DAY","values":[
                  {"timestamp":"2026-08-10T00:00:00+02:00","value":100.0},
                  {"timestamp":"2026-08-13T00:00:00+02:00","value":25.0}
                ]}
                """, MeasurementsResponsePublicApiV2.class));

        Map<Channel, State> result = transformer.transformEnergy(response, AggregatePeriod.DAY,
                OffsetDateTime.parse("2026-08-13T00:00:00+02:00"));

        assertEquals("25 Wh", Objects.requireNonNull(result.get(channel)).toString());
    }

    private Channel createChannel() {
        Channel channel = mock(Channel.class);
        when(channel.getUID()).thenReturn(new ChannelUID(new ThingUID("solaredge:generic:test"), "production"));
        return channel;
    }

    private MeasurementsResponseTransformerPublicApiV2 createTransformer(Channel channel) {
        ChannelProvider channelProvider = mock(ChannelProvider.class);
        when(channelProvider.getChannel(anyString(), anyString())).thenReturn(channel);
        return new MeasurementsResponseTransformerPublicApiV2(channelProvider);
    }
}
