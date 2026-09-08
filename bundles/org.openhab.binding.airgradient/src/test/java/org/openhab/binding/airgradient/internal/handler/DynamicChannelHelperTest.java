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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * @author Leo Siepel - Initial contribution
 */

@NonNullByDefault
public class DynamicChannelHelperTest {

    private Thing thing = Mockito.mock(Thing.class);
    private ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
    private ThingBuilder builder = Mockito.mock(ThingBuilder.class);

    @BeforeEach
    public void setUp() {
        Mockito.when(thing.getUID()).thenReturn(new ThingUID(AirGradientBindingConstants.THING_TYPE_LOCAL, "1234"));
        Mockito.when(thing.getChannel(any(ChannelUID.class))).thenReturn(null);
        Mockito.when(callback.createChannelBuilder(any(ChannelUID.class), any(ChannelTypeUID.class)))
                .thenAnswer(invocation -> {
                    ChannelUID channelUID = invocation.getArgument(0, ChannelUID.class);
                    ChannelTypeUID channelTypeUID = invocation.getArgument(1, ChannelTypeUID.class);
                    Set<String> defaultTags = switch (channelTypeUID.getId()) {
                        case "pm1", "pm2", "pm10", "particle-count" -> Set.of("Measurement", "ParticulateMatter");
                        case "tvoc" -> Set.of("Measurement", "VOC");
                        default -> Set.of();
                    };
                    return ChannelBuilder.create(channelUID).withType(channelTypeUID).withDefaultTags(defaultTags)
                            .withLabel("Inherited label").withDescription("Inherited description");
                });
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
        measure.tvocIndex = 1d;
        measure.tvocRaw = 2d;
        measure.noxIndex = 2d;
        measure.noxRaw = 2.5d;

        ThingBuilder returnedBuilder = DynamicChannelHelper.updateThingWithMeasurementChannels(thing, callback, null,
                () -> builder, measure);

        assertThat(Objects.requireNonNull(returnedBuilder), is(builder));
        ArgumentCaptor<Channel> captor = ArgumentCaptor.forClass(Channel.class);
        verify(builder, Mockito.times(13)).withChannel(captor.capture());
        List<String> channelIds = captor.getAllValues().stream().map((channel) -> channel.getUID().getId()).toList();
        assertThat(channelIds,
                containsInAnyOrder("pm01-standard", "pm02-standard", "pm10-standard", "pm005-count", "pm01-count",
                        "pm02-count", "pm50-count", "pm10-count", "pm02-compensated", "tvoc-index", "tvoc-raw",
                        "nox-index", "nox-raw"));

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
                Map.entry("tvoc-index", "Number:Dimensionless"), Map.entry("tvoc-raw", "Number:Dimensionless"),
                Map.entry("nox-index", "Number:Dimensionless"), Map.entry("nox-raw", "Number:Dimensionless"))));
        Map<String, Set<String>> defaultTags = captor.getAllValues().stream()
                .collect(Collectors.toMap((channel) -> channel.getUID().getId(), Channel::getDefaultTags));
        assertThat(defaultTags.get("pm01-standard"), is(Set.of("Measurement", "ParticulateMatter")));
        assertThat(defaultTags.get("pm02-standard"), is(Set.of("Measurement", "ParticulateMatter")));
        assertThat(defaultTags.get("pm10-standard"), is(Set.of("Measurement", "ParticulateMatter")));
        assertThat(defaultTags.get("pm005-count"), is(Set.of("Measurement", "ParticulateMatter")));
        assertThat(defaultTags.get("tvoc-index"), is(Set.of("Measurement", "VOC")));
        assertThat(defaultTags.get("tvoc-raw"), is(Set.of("Measurement", "VOC")));

        Map<String, String> labels = captor.getAllValues().stream().collect(Collectors
                .toMap((channel) -> channel.getUID().getId(), (channel) -> Objects.requireNonNull(channel.getLabel())));
        assertEquals(Map.ofEntries(Map.entry("pm01-standard", "PM1 Standard"),
                Map.entry("pm02-standard", "PM2.5 Standard"), Map.entry("pm10-standard", "PM10 Standard"),
                Map.entry("pm005-count", "PM0.5 Particle Count"), Map.entry("pm01-count", "PM1 Particle Count"),
                Map.entry("pm02-count", "PM2.5 Particle Count"), Map.entry("pm50-count", "PM5 Particle Count"),
                Map.entry("pm10-count", "PM10 Particle Count"), Map.entry("pm02-compensated", "PM2.5 Compensated"),
                Map.entry("tvoc-index", "TVOC Index"), Map.entry("tvoc-raw", "TVOC Raw"),
                Map.entry("nox-index", "NOx Index"), Map.entry("nox-raw", "NOx Raw")), labels);

        Map<String, String> descriptions = captor.getAllValues().stream().collect(Collectors.toMap(
                (channel) -> channel.getUID().getId(), (channel) -> Objects.requireNonNull(channel.getDescription())));
        assertEquals(
                Map.ofEntries(Map.entry("pm01-standard", "PM1.0 concentration (standard particle)"),
                        Map.entry("pm02-standard", "PM2.5 concentration (standard particle)"),
                        Map.entry("pm10-standard", "PM10 concentration (standard particle)"),
                        Map.entry("pm005-count", "Particle count for particles >= 0.5 microns per deciliter air"),
                        Map.entry("pm01-count", "Particle count for particles >= 1.0 microns per deciliter air"),
                        Map.entry("pm02-count", "Particle count for particles >= 2.5 microns per deciliter air"),
                        Map.entry("pm50-count", "Particle count for particles >= 5.0 microns per deciliter air"),
                        Map.entry("pm10-count", "Particle count for particles >= 10 microns per deciliter air"),
                        Map.entry("pm02-compensated", "PM2.5 concentration with correction applied"),
                        Map.entry("tvoc-index", "TVOC index value"), Map.entry("tvoc-raw", "Raw TVOC value"),
                        Map.entry("nox-index", "NOx index value"), Map.entry("nox-raw", "Raw NOx value")),
                descriptions);
    }

    @Test
    public void testAddsSupportedConfigurationChannels() {
        LocalConfiguration configuration = new LocalConfiguration();
        configuration.country = "NO";
        configuration.postDataToAirGradient = false;
        configuration.displayBrightness = 75L;

        ThingBuilder returnedBuilder = DynamicChannelHelper.updateThingWithConfigurationChannels(thing, callback, null,
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

        ThingBuilder returnedBuilder = DynamicChannelHelper.updateThingWithMeasurementChannels(thing, callback, null,
                () -> builder, measure);

        assertThat(returnedBuilder, is(nullValue()));
        verifyNoInteractions(builder);
    }
}
