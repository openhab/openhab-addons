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

import static org.eclipse.jdt.annotation.Checks.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.THING_TYPE_LOCAL;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openhab.binding.airgradient.internal.communication.AirGradientCommunicationException;
import org.openhab.binding.airgradient.internal.communication.RemoteAPIController;
import org.openhab.binding.airgradient.internal.model.LocalConfiguration;
import org.openhab.binding.airgradient.internal.model.Measure;
import org.openhab.core.config.core.Configuration;
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
public class AirGradientLocalHandlerTest {

    private static class TestAirGradientLocalHandler extends AirGradientLocalHandler {
        private final ThingBuilder thingBuilder;
        private int editThingCalls;

        public TestAirGradientLocalHandler(Thing thing, HttpClient httpClient, ThingBuilder thingBuilder) {
            super(thing, httpClient);
            this.thingBuilder = thingBuilder;
        }

        @Override
        protected ThingBuilder editThing() {
            editThingCalls++;
            return thingBuilder;
        }

        public int getEditThingCalls() {
            return editThingCalls;
        }
    }

    private static final Measure TEST_MEASURE = new Measure() {
        {
            firmware = "3.1.21";
            model = "I-9PSL";
            pm01Standard = 3d;
            tvocIndex = 1d;
        }
    };

    private static final LocalConfiguration TEST_CONFIGURATION = new LocalConfiguration() {
        {
            country = "NO";
            postDataToAirGradient = false;
        }
    };

    private @Nullable AirGradientLocalHandler sut;
    private @Nullable ThingHandlerCallback callbackMock;
    private @Nullable Thing thing;
    private @Nullable RemoteAPIController apiControllerMock;
    private @Nullable ThingBuilder thingBuilder;
    private @Nullable Thing updatedThing;
    private @Nullable Thing secondUpdatedThing;

    @SuppressWarnings("null")
    @BeforeEach
    public void setUp() throws Exception {
        callbackMock = Mockito.mock(ThingHandlerCallback.class);
        Mockito.when(callbackMock.isChannelLinked(any(ChannelUID.class))).thenReturn(true);
        Mockito.when(callbackMock.createChannelBuilder(any(ChannelUID.class), any(ChannelTypeUID.class)))
                .thenAnswer(invocation -> ChannelBuilder.create(invocation.getArgument(0, ChannelUID.class))
                        .withType(invocation.getArgument(1, ChannelTypeUID.class)));
        thing = Mockito.mock(Thing.class);
        updatedThing = Mockito.mock(Thing.class);
        secondUpdatedThing = Mockito.mock(Thing.class);
        apiControllerMock = Mockito.mock(RemoteAPIController.class);
        thingBuilder = Mockito.mock(ThingBuilder.class);

        sut = new TestAirGradientLocalHandler(requireNonNull(thing), Mockito.mock(HttpClient.class),
                requireNonNull(thingBuilder));
        sut.setCallback(callbackMock);
        sut.setApiController(requireNonNull(apiControllerMock));

        Mockito.when(thing.getUID()).thenReturn(new ThingUID(THING_TYPE_LOCAL, "1234"));
        Mockito.when(updatedThing.getUID()).thenReturn(new ThingUID(THING_TYPE_LOCAL, "1234"));
        Mockito.when(secondUpdatedThing.getUID()).thenReturn(new ThingUID(THING_TYPE_LOCAL, "1234"));
        Mockito.when(thing.getConfiguration()).thenReturn(new Configuration());
        Mockito.when(updatedThing.getConfiguration()).thenReturn(new Configuration());
        Mockito.when(secondUpdatedThing.getConfiguration()).thenReturn(new Configuration());
        Mockito.when(thing.getProperties()).thenReturn(Map.of());
        Mockito.when(updatedThing.getProperties()).thenReturn(Map.of());
        Mockito.when(secondUpdatedThing.getProperties()).thenReturn(Map.of());
        Mockito.when(thing.getChannel(any(ChannelUID.class))).thenReturn(null);
        Mockito.when(updatedThing.getChannel(any(ChannelUID.class))).thenReturn(null);
        Mockito.when(secondUpdatedThing.getChannel(any(ChannelUID.class))).thenReturn(Mockito.mock(Channel.class));
        Mockito.when(apiControllerMock.getMeasures()).thenReturn(List.of(TEST_MEASURE));
        Mockito.when(apiControllerMock.getConfig()).thenReturn(TEST_CONFIGURATION);
        Mockito.when(thingBuilder.withChannel(any())).thenReturn(Objects.requireNonNull(thingBuilder));
        Mockito.when(thingBuilder.build()).thenReturn(Objects.requireNonNull(updatedThing),
                Objects.requireNonNull(secondUpdatedThing));
    }

    @SuppressWarnings("null")
    @Test
    public void testPollingCodeSkipsThingUpdateAfterInitialCapabilityScan() throws Exception {
        sut.pollingCode();
        sut.pollingCode();

        verify(apiControllerMock, times(2)).getMeasures();
        verify(apiControllerMock, times(2)).getConfig();
        assertThat(((TestAirGradientLocalHandler) requireNonNull(sut)).getEditThingCalls(), is(2));
    }

    @SuppressWarnings({ "null", "unchecked" })
    @Test
    public void testPollingCodeAddsMeasurementChannelWhenValueAppearsOnLaterPoll() throws Exception {
        Measure firstMeasure = new Measure();
        firstMeasure.firmwareVersion = "3.1.21";
        firstMeasure.model = "I-9PSL";
        Measure secondMeasure = new Measure();
        secondMeasure.firmwareVersion = firstMeasure.firmwareVersion;
        secondMeasure.model = firstMeasure.model;
        secondMeasure.tvocRaw = 123d;
        Mockito.when(apiControllerMock.getMeasures()).thenReturn(List.of(firstMeasure), List.of(secondMeasure));
        Mockito.when(apiControllerMock.getConfig()).thenReturn(null);

        sut.pollingCode();
        sut.pollingCode();

        ArgumentCaptor<Channel> channelCaptor = ArgumentCaptor.forClass(Channel.class);
        verify(thingBuilder).withChannel(channelCaptor.capture());
        assertThat(channelCaptor.getValue().getUID().getId(), is("tvoc-raw"));
    }

    @SuppressWarnings("null")
    @Test
    public void testPollingCodeProcessesMeasurementsWhenConfigurationRequestFails() throws Exception {
        Mockito.when(apiControllerMock.getConfig()).thenThrow(new AirGradientCommunicationException("Config failed"));

        sut.pollingCode();

        verify(callbackMock).stateUpdated(eq(new ChannelUID(requireNonNull(thing).getUID(), "pm01-standard")), any());
        verify(callbackMock, times(2)).thingUpdated(requireNonNull(updatedThing));
    }
}
