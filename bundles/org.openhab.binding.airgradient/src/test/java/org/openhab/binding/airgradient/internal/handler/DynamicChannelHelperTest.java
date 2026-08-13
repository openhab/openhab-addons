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
package org.openhab.binding.airgradient.internal.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openhab.binding.airgradient.internal.AirGradientBindingConstants;
import org.openhab.binding.airgradient.internal.model.LocalConfiguration;
import org.openhab.binding.airgradient.internal.model.Measure;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * @author Leo Siepel - Initial contribution
 */

@NonNullByDefault
public class DynamicChannelHelperTest {

    private Thing thing = Mockito.mock(Thing.class);
    private ThingBuilder builder = Mockito.mock(ThingBuilder.class);

    @BeforeEach
    public void setUp() {
        Mockito.when(thing.getUID()).thenReturn(new ThingUID(AirGradientBindingConstants.THING_TYPE_LOCAL, "1234"));
        Mockito.when(thing.getChannel(any(ChannelUID.class))).thenReturn(null);
        Mockito.when(builder.withChannel(any(Channel.class))).thenReturn(builder);
    }

    @Test
    public void testAddsOnlySupportedMeasurementChannels() {
        Measure measure = new Measure();
        measure.pm01Standard = 3d;
        measure.pm02Standard = 7d;
        measure.pm10Standard = 13d;
        measure.pm005Count = 17d;
        measure.pm01Count = 19d;
        measure.pm02Count = 23d;
        measure.pm50Count = 29d;
        measure.pm10Count = 31d;
        measure.pm02Compensated = 11d;
        measure.atmpCompensated = 20d;
        measure.rhumCompensated = 50d;
        measure.tvocIndex = 1d;
        measure.tvocRaw = 2d;
        measure.noxIndex = 2d;
        measure.noxRaw = 2.5d;

        ThingBuilder returnedBuilder = DynamicChannelHelper.updateThingWithMeasurementChannels(thing, null,
                () -> builder, measure);

        assertThat(Objects.requireNonNull(returnedBuilder), is(builder));
        ArgumentCaptor<Channel> captor = ArgumentCaptor.forClass(Channel.class);
        verify(builder, Mockito.times(15)).withChannel(captor.capture());
        List<String> channelIds = captor.getAllValues().stream().map((channel) -> channel.getUID().getId()).toList();
        assertThat(channelIds,
                containsInAnyOrder("pm01-standard", "pm02-standard", "pm10-standard", "pm005-count", "pm01-count",
                        "pm02-count", "pm50-count", "pm10-count", "pm02-compensated", "atmp-compensated",
                        "rhum-compensated", "tvoc-index", "tvoc-raw", "nox-index", "nox-raw"));

        Map<String, String> channelTypes = captor.getAllValues().stream()
                .collect(Collectors.toMap((channel) -> channel.getUID().getId(),
                        (channel) -> Objects.requireNonNull(channel.getChannelTypeUID()).toString()));
        assertThat(channelTypes, is(Map.ofEntries(Map.entry("pm01-standard", "airgradient:pm1"),
                Map.entry("pm02-standard", "airgradient:pm2"), Map.entry("pm10-standard", "airgradient:pm10"),
                Map.entry("pm005-count", "airgradient:particle-count"),
                Map.entry("pm01-count", "airgradient:particle-count"),
                Map.entry("pm02-count", "airgradient:particle-count"),
                Map.entry("pm50-count", "airgradient:particle-count"),
                Map.entry("pm10-count", "airgradient:particle-count"), Map.entry("pm02-compensated", "airgradient:pm2"),
                Map.entry("atmp-compensated", "system:outdoor-temperature"),
                Map.entry("rhum-compensated", "system:atmospheric-humidity"),
                Map.entry("tvoc-index", "airgradient:tvoc"), Map.entry("tvoc-raw", "airgradient:tvoc"),
                Map.entry("nox-index", "airgradient:nox"), Map.entry("nox-raw", "airgradient:nox"))));

        Map<String, String> acceptedItemTypes = captor.getAllValues().stream()
                .collect(Collectors.toMap((channel) -> channel.getUID().getId(),
                        (channel) -> Objects.requireNonNull(channel.getAcceptedItemType())));
        assertThat(acceptedItemTypes, is(Map.ofEntries(Map.entry("pm01-standard", "Number:Density"),
                Map.entry("pm02-standard", "Number:Density"), Map.entry("pm10-standard", "Number:Density"),
                Map.entry("pm005-count", "Number:Dimensionless"), Map.entry("pm01-count", "Number:Dimensionless"),
                Map.entry("pm02-count", "Number:Dimensionless"), Map.entry("pm50-count", "Number:Dimensionless"),
                Map.entry("pm10-count", "Number:Dimensionless"), Map.entry("pm02-compensated", "Number:Density"),
                Map.entry("atmp-compensated", "Number:Temperature"),
                Map.entry("rhum-compensated", "Number:Dimensionless"), Map.entry("tvoc-index", "Number:Dimensionless"),
                Map.entry("tvoc-raw", "Number:Dimensionless"), Map.entry("nox-index", "Number:Dimensionless"),
                Map.entry("nox-raw", "Number:Dimensionless"))));
    }

    @Test
    public void testAddsSupportedConfigurationChannels() {
        LocalConfiguration configuration = new LocalConfiguration();
        configuration.country = "NO";
        configuration.postDataToAirGradient = false;
        configuration.displayBrightness = 75L;

        ThingBuilder returnedBuilder = DynamicChannelHelper.updateThingWithConfigurationChannels(thing, null,
                () -> builder, configuration);

        assertThat(Objects.requireNonNull(returnedBuilder), is(builder));
        ArgumentCaptor<Channel> captor = ArgumentCaptor.forClass(Channel.class);
        verify(builder, Mockito.times(4)).withChannel(captor.capture());
        List<String> channelIds = captor.getAllValues().stream().map((channel) -> channel.getUID().getId()).toList();
        assertThat(channelIds,
                containsInAnyOrder("country-code", "post-to-cloud", "display-brightness", "led-bar-test"));
    }

    @Test
    public void testDoesNotReAddExistingSupportedDynamicChannel() {
        Measure measure = new Measure();
        measure.pm01Standard = 3d;

        ChannelUID existingChannelUid = new ChannelUID(thing.getUID(), "pm01-standard");
        Mockito.when(thing.getChannel(existingChannelUid)).thenReturn(Mockito.mock(Channel.class));

        ThingBuilder returnedBuilder = DynamicChannelHelper.updateThingWithMeasurementChannels(thing, null,
                () -> builder, measure);

        assertThat(returnedBuilder, is(nullValue()));
        verifyNoInteractions(builder);
    }
}
