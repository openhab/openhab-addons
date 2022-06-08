/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.wundergroundupdatereceiver.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.DefaultSystemChannelTypeProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.internal.type.StateChannelTypeBuilderImpl;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

/**
 * @author Daniel Demus - Initial contribution
 */
@NonNullByDefault
class WundergroundUpdateReceiverDiscoveryServiceTest {

    private static final String STATION_ID_1 = "abcd1234";
    private static final String REQ_STATION_ID = "dfggger";
    private static final ThingUID TEST_THING_UID = new ThingUID(
            WundergroundUpdateReceiverBindingConstants.THING_TYPE_UPDATE_RECEIVER, "test-receiver");

    @BeforeEach
    public void setUp() {
        openMocks(this);
    }

    @Test
    void a_request_with_an_unregistered_stationid_is_added_to_the_queue_once()
            throws ServletException, NamespaceException, IOException {
        // Given
        final String queryString = "ID=dfggger&PASSWORD=XXXXXX&tempf=26.1&humidity=74&dewptf=18.9&windchillf=26.1&winddir=14&windspeedmph=1.34&windgustmph=2.46&rainin=0.00&dailyrainin=0.00&weeklyrainin=0.00&monthlyrainin=0.08&yearlyrainin=3.06&solarradiation=42.24&UV=1&indoortempf=69.3&indoorhumidity=32&baromin=30.39&AqNOX=21&lowbatt=1&dateutc=2021-02-07%2014:04:03&softwaretype=WH2600%20V2.2.8&action=updateraw&realtime=1&rtfreq=5";
        WundergroundUpdateReceiverDiscoveryService discoveryService = mock(
                WundergroundUpdateReceiverDiscoveryService.class);
        HttpService httpService = mock(HttpService.class);
        WundergroundUpdateReceiverServlet sut = new WundergroundUpdateReceiverServlet(httpService, discoveryService);
        WundergroundUpdateReceiverHandler handler = mock(WundergroundUpdateReceiverHandler.class);
        when(handler.getStationId()).thenReturn(STATION_ID_1);
        sut.addHandler(handler);
        when(discoveryService.isBackgroundDiscoveryEnabled()).thenReturn(false);

        // Then
        verify(httpService).registerServlet(eq(WundergroundUpdateReceiverServlet.SERVLET_URL), eq(sut), any(), any());
        assertThat(sut.isActive(), is(true));

        HttpChannel httpChannel = mock(HttpChannel.class);
        MetaData.Request request = new MetaData.Request("GET",
                new HttpURI("http://localhost" + WundergroundUpdateReceiverServlet.SERVLET_URL + "?" + queryString),
                HttpVersion.HTTP_1_1, new HttpFields());
        Request req = new Request(httpChannel, null);
        req.setMetaData(request);

        // When
        sut.doGet(req, mock(HttpServletResponse.class, Answers.RETURNS_MOCKS));

        // Then
        verify(handler, never()).updateChannelStates(any());
        verify(discoveryService).addUnhandledStationId(eq("dfggger"), any());
        assertThat(sut.isActive(), is(true));
    }

    @Test
    void multiple_indexed_parameters_of_the_same_channeltype_are_correctly_discovered()
            throws ServletException, NamespaceException, IOException {
        // Given
        final String queryString = "ID=dfggger&PASSWORD=XXXXXX&temp1f=26.1&humidity=74&temp2f=25.1&lowbatt=1&soilmoisture1=78&soilmoisture2=73&dateutc=2021-02-07%2014:04:03&softwaretype=WH2600%20V2.2.8&action=updateraw&realtime=1&rtfreq=5";
        MetaData.Request request = new MetaData.Request("GET",
                new HttpURI("http://localhost" + WundergroundUpdateReceiverServlet.SERVLET_URL + "?" + queryString),
                HttpVersion.HTTP_1_1, new HttpFields());
        HttpChannel httpChannel = mock(HttpChannel.class);
        Request req = new Request(httpChannel, null);
        req.setMetaData(request);

        TestChannelTypeRegistry channelTypeRegistry = new TestChannelTypeRegistry();
        WundergroundUpdateReceiverDiscoveryService discoveryService = new WundergroundUpdateReceiverDiscoveryService(
                false);
        discoveryService.addUnhandledStationId(REQ_STATION_ID, req.getParameterMap());
        HttpService httpService = mock(HttpService.class);
        WundergroundUpdateReceiverServlet sut = new WundergroundUpdateReceiverServlet(httpService, discoveryService);
        Thing thing = ThingBuilder
                .create(WundergroundUpdateReceiverBindingConstants.SUPPORTED_THING_TYPES_UIDS.stream().findFirst()
                        .get(), TEST_THING_UID)
                .withConfiguration(new Configuration(
                        Map.of(WundergroundUpdateReceiverBindingConstants.REPRESENTATION_PROPERTY, REQ_STATION_ID)))
                .withLabel("test thing").withLocation("location").build();
        WundergroundUpdateReceiverHandler handler = new WundergroundUpdateReceiverHandler(thing, sut, discoveryService,
                new WundergroundUpdateReceiverUnknownChannelTypeProvider(), channelTypeRegistry);
        handler.setCallback(mock(ThingHandlerCallback.class));
        handler.initialize();
        sut.addHandler(handler);

        // When
        sut.activate();

        // Then
        assertThat(sut.isActive(), is(true));

        // When
        sut.doGet(req, mock(HttpServletResponse.class, Answers.RETURNS_MOCKS));

        // Then
        assertThat(sut.getHandlers().size(), is(1));
        assertThat(sut.getHandlers().containsKey(REQ_STATION_ID), is(true));
        assertThat(handler.getThing().getChannels().stream()
                .filter(channel -> channel
                        .getChannelTypeUID() == WundergroundUpdateReceiverBindingConstants.TEMPERATURE_CHANNELTYPEUID)
                .count(), is(2L));
    }

    class TestChannelTypeRegistry extends ChannelTypeRegistry {

        TestChannelTypeRegistry() {
            super();
            ChannelTypeProvider provider = mock(ChannelTypeProvider.class);
            when(provider.getChannelType(eq(WundergroundUpdateReceiverBindingConstants.TEMPERATURE_CHANNELTYPEUID),
                    any())).thenReturn(DefaultSystemChannelTypeProvider.SYSTEM_OUTDOOR_TEMPERATURE);
            when(provider
                    .getChannelType(eq(WundergroundUpdateReceiverBindingConstants.SOIL_MOISTURE_CHANNELTYPEUID), any()))
                            .thenReturn(new StateChannelTypeBuilderImpl(
                                    WundergroundUpdateReceiverBindingConstants.SOIL_MOISTURE_CHANNELTYPEUID,
                                    "Soilmoisture", "Number:Dimensionless").build());
            when(provider
                    .getChannelType(eq(WundergroundUpdateReceiverBindingConstants.SOFTWARETYPE_CHANNELTYPEUID), any()))
                            .thenReturn(new StateChannelTypeBuilderImpl(
                                    WundergroundUpdateReceiverBindingConstants.SOFTWARETYPE_CHANNELTYPEUID,
                                    "Software type", "String").build());
            when(provider.getChannelType(eq(WundergroundUpdateReceiverBindingConstants.HUMIDITY_CHANNELTYPEUID), any()))
                    .thenReturn(new StateChannelTypeBuilderImpl(
                            WundergroundUpdateReceiverBindingConstants.HUMIDITY_CHANNELTYPEUID, "Humidity",
                            "Number:Dimensionless").build());
            when(provider.getChannelType(eq(WundergroundUpdateReceiverBindingConstants.DATEUTC_CHANNELTYPEUID), any()))
                    .thenReturn(new StateChannelTypeBuilderImpl(
                            WundergroundUpdateReceiverBindingConstants.DATEUTC_CHANNELTYPEUID, "Last Updated", "String")
                                    .build());
            when(provider.getChannelType(
                    eq(WundergroundUpdateReceiverBindingConstants.REALTIME_FREQUENCY_CHANNELTYPEUID), any()))
                            .thenReturn(new StateChannelTypeBuilderImpl(
                                    WundergroundUpdateReceiverBindingConstants.REALTIME_FREQUENCY_CHANNELTYPEUID,
                                    "Realtime frequency", "Number").build());
            when(provider.getChannelType(eq(WundergroundUpdateReceiverBindingConstants.LOW_BATTERY_CHANNELTYPEUID),
                    any())).thenReturn(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_LOW_BATTERY);
            this.addChannelTypeProvider(provider);
            this.addChannelTypeProvider(new WundergroundUpdateReceiverUnknownChannelTypeProvider());
        }
    }
}
