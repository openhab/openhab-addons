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
import java.util.Objects;

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
        measure.pm02Compensated = 11d;
        measure.tvocIndex = 1d;
        measure.noxRaw = 2.5d;

        ThingBuilder returnedBuilder = DynamicChannelHelper.updateThingWithMeasurementChannels(thing, null,
                () -> builder, measure);

        assertThat(Objects.requireNonNull(returnedBuilder), is(builder));
        ArgumentCaptor<Channel> captor = ArgumentCaptor.forClass(Channel.class);
        verify(builder, Mockito.times(4)).withChannel(captor.capture());
        List<String> channelIds = captor.getAllValues().stream().map((channel) -> channel.getUID().getId()).toList();
        assertThat(channelIds, containsInAnyOrder("pm01-standard", "pm02-compensated", "tvoc-index", "nox-raw"));
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
