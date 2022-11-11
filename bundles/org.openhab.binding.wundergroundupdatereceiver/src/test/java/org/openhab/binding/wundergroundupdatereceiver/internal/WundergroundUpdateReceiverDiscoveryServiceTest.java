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
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.openhab.binding.wundergroundupdatereceiver.internal.WundergroundUpdateReceiverBindingConstants.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.DefaultSystemChannelTypeProvider;
import org.openhab.core.thing.ManagedThingProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.internal.type.StateChannelTypeBuilderImpl;
import org.openhab.core.thing.internal.type.TriggerChannelTypeBuilderImpl;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

/**
 * @author Daniel Demus - Initial contribution
 */
@NonNullByDefault({})
class WundergroundUpdateReceiverDiscoveryServiceTest {

    private static final String STATION_ID_1 = "abcd1234";
    private static final String REQ_STATION_ID = "dfggger";
    private static final ThingUID TEST_THING_UID = new ThingUID(THING_TYPE_UPDATE_RECEIVER, "test-receiver");

    @BeforeEach
    public void setUp() {
        openMocks(this);
    }

    @Test
    void aRequestWithAnUnregisteredStationidIsAddedToTheQueueOnce()
            throws ServletException, NamespaceException, IOException {
        // Given
        final String queryString = "ID=dfggger&" + "PASSWORD=XXXXXX&" + "tempf=26.1&" + "humidity=74&" + "dewptf=18.9&"
                + "windchillf=26.1&" + "winddir=14&" + "windspeedmph=1.34&" + "windgustmph=2.46&" + "rainin=0.00&"
                + "dailyrainin=0.00&" + "weeklyrainin=0.00&" + "monthlyrainin=0.08&" + "yearlyrainin=3.06&"
                + "solarradiation=42.24&" + "UV=1&indoortempf=69.3&" + "indoorhumidity=32&" + "baromin=30.39&"
                + "AqNOX=21&" + "lowbatt=1&" + "dateutc=2021-02-07%2014:04:03&" + "softwaretype=WH2600%20V2.2.8&"
                + "action=updateraw&" + "realtime=1&" + "rtfreq=5";
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
    void multipleIndexedParametersOfTheSameChanneltypeAreCorrectlyDiscovered() throws IOException {
        // Given
        final String queryString = "ID=dfggger&" + "PASSWORD=XXXXXX&" + "temp1f=26.1&" + "humidity=74&" + "temp2f=25.1&"
                + "lowbatt=1&" + "soilmoisture1=78&" + "soilmoisture2=73&" + "dateutc=2021-02-07%2014:04:03&"
                + "softwaretype=WH2600%20V2.2.8&" + "action=updateraw&" + "realtime=1&" + "rtfreq=5";
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
        Thing thing = ThingBuilder.create(SUPPORTED_THING_TYPES_UIDS.stream().findFirst().get(), TEST_THING_UID)
                .withConfiguration(new Configuration(Map.of(REPRESENTATION_PROPERTY, REQ_STATION_ID)))
                .withLabel("test thing").withLocation("location").build();
        ManagedThingProvider managedThingProvider = mock(ManagedThingProvider.class);
        when(managedThingProvider.get(TEST_THING_UID)).thenReturn(thing);
        WundergroundUpdateReceiverHandler handler = new WundergroundUpdateReceiverHandler(thing, sut, discoveryService,
                new WundergroundUpdateReceiverUnknownChannelTypeProvider(), channelTypeRegistry, managedThingProvider);
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
                .filter(channel -> channel.getChannelTypeUID() == TEMPERATURE_CHANNELTYPEUID).count(), is(2L));
    }

    @Test
    void unregisteredChannelsAreAddedOnTheFlyWhenDiscovered() throws IOException {
        // Given
        final String firstDeviceQueryString = "ID=dfggger&" + "PASSWORD=XXXXXX&" + "tempf=26.1&" + "humidity=74&"
                + "dateutc=2021-02-07%2014:04:03&" + "softwaretype=WH2600%20V2.2.8&" + "action=updateraw&"
                + "realtime=1&" + "rtfreq=5";
        MetaData.Request request1 = new MetaData.Request("GET", new HttpURI(
                "http://localhost" + WundergroundUpdateReceiverServlet.SERVLET_URL + "?" + firstDeviceQueryString),
                HttpVersion.HTTP_1_1, new HttpFields());
        HttpChannel httpChannel = mock(HttpChannel.class);
        Request req1 = new Request(httpChannel, null);
        req1.setMetaData(request1);

        TestChannelTypeRegistry channelTypeRegistry = new TestChannelTypeRegistry();
        WundergroundUpdateReceiverDiscoveryService discoveryService = new WundergroundUpdateReceiverDiscoveryService(
                true);
        discoveryService.addUnhandledStationId(REQ_STATION_ID, req1.getParameterMap());
        HttpService httpService = mock(HttpService.class);
        WundergroundUpdateReceiverServlet sut = new WundergroundUpdateReceiverServlet(httpService, discoveryService);
        Thing thing = ThingBuilder.create(SUPPORTED_THING_TYPES_UIDS.stream().findFirst().get(), TEST_THING_UID)
                .withConfiguration(new Configuration(Map.of(REPRESENTATION_PROPERTY, REQ_STATION_ID)))
                .withLabel("test thing").withLocation("location").build();
        ManagedThingProvider managedThingProvider = mock(ManagedThingProvider.class);
        when(managedThingProvider.get(any())).thenReturn(thing);
        WundergroundUpdateReceiverHandler handler = new WundergroundUpdateReceiverHandler(thing, sut, discoveryService,
                new WundergroundUpdateReceiverUnknownChannelTypeProvider(), channelTypeRegistry, managedThingProvider);
        handler.setCallback(mock(ThingHandlerCallback.class));

        // When
        handler.initialize();
        sut.addHandler(handler);

        // Then
        ChannelTypeUID[] expectedBefore = new ChannelTypeUID[] { TEMPERATURE_CHANNELTYPEUID, HUMIDITY_CHANNELTYPEUID,
                DATEUTC_CHANNELTYPEUID, SOFTWARETYPE_CHANNELTYPEUID, REALTIME_FREQUENCY_CHANNELTYPEUID,
                LAST_QUERY_STATE_CHANNELTYPEUID, LAST_RECEIVED_DATETIME_CHANNELTYPEUID,
                LAST_QUERY_TRIGGER_CHANNELTYPEUID };
        List<ChannelTypeUID> before = handler.getThing().getChannels().stream().map(Channel::getChannelTypeUID)
                .collect(Collectors.toList());
        assertThat(before, hasItems(expectedBefore));

        // When
        final String secondDeviceQueryString = "ID=dfggger&" + "PASSWORD=XXXXXX&" + "lowbatt=1&" + "soilmoisture1=78&"
                + "soilmoisture2=73&" + "solarradiation=42.24&" + "dateutc=2021-02-07%2014:04:03&"
                + "softwaretype=WH2600%20V2.2.8&" + "action=updateraw&" + "realtime=1&" + "rtfreq=5";
        MetaData.Request request = new MetaData.Request("GET", new HttpURI(
                "http://localhost" + WundergroundUpdateReceiverServlet.SERVLET_URL + "?" + secondDeviceQueryString),
                HttpVersion.HTTP_1_1, new HttpFields());
        Request req2 = new Request(httpChannel, null);
        req2.setMetaData(request);
        sut.activate();

        // Then
        assertThat(sut.isActive(), is(true));

        // When
        sut.doGet(req2, mock(HttpServletResponse.class, Answers.RETURNS_MOCKS));

        // Then
        ChannelTypeUID[] expectedActual = Arrays.copyOf(expectedBefore, expectedBefore.length + 3);
        System.arraycopy(new ChannelTypeUID[] { LOW_BATTERY_CHANNELTYPEUID, SOIL_MOISTURE_CHANNELTYPEUID,
                SOLARRADIATION_CHANNELTYPEUID }, 0, expectedActual, expectedBefore.length, 3);
        List<ChannelTypeUID> actual = handler.getThing().getChannels().stream().map(Channel::getChannelTypeUID)
                .collect(Collectors.toList());
        assertThat(actual, hasItems(expectedActual));
    }

    @Test
    void unregisteredChannelsAreNotAddedOnUnmanagedThings() throws IOException {
        // Given
        final String firstDeviceQueryString = "ID=dfggger&" + "PASSWORD=XXXXXX&" + "tempf=26.1&" + "humidity=74&"
                + "dateutc=2021-02-07%2014:04:03&" + "softwaretype=WH2600%20V2.2.8&" + "action=updateraw&"
                + "realtime=1&" + "rtfreq=5";
        MetaData.Request request1 = new MetaData.Request("GET", new HttpURI(
                "http://localhost" + WundergroundUpdateReceiverServlet.SERVLET_URL + "?" + firstDeviceQueryString),
                HttpVersion.HTTP_1_1, new HttpFields());
        HttpChannel httpChannel = mock(HttpChannel.class);
        Request req1 = new Request(httpChannel, null);
        req1.setMetaData(request1);

        TestChannelTypeRegistry channelTypeRegistry = new TestChannelTypeRegistry();
        WundergroundUpdateReceiverDiscoveryService discoveryService = new WundergroundUpdateReceiverDiscoveryService(
                true);
        discoveryService.addUnhandledStationId(REQ_STATION_ID, req1.getParameterMap());
        HttpService httpService = mock(HttpService.class);
        WundergroundUpdateReceiverServlet sut = new WundergroundUpdateReceiverServlet(httpService, discoveryService);
        Thing thing = ThingBuilder.create(SUPPORTED_THING_TYPES_UIDS.stream().findFirst().get(), TEST_THING_UID)
                .withConfiguration(new Configuration(Map.of(REPRESENTATION_PROPERTY, REQ_STATION_ID)))
                .withLabel("test thing").withLocation("location").build();
        ManagedThingProvider managedThingProvider = mock(ManagedThingProvider.class);
        when(managedThingProvider.get(any())).thenReturn(null);
        WundergroundUpdateReceiverHandler handler = new WundergroundUpdateReceiverHandler(thing, sut, discoveryService,
                new WundergroundUpdateReceiverUnknownChannelTypeProvider(), channelTypeRegistry, managedThingProvider);
        handler.setCallback(mock(ThingHandlerCallback.class));

        // When
        handler.initialize();
        sut.addHandler(handler);

        // Then
        ChannelTypeUID[] expectedBefore = new ChannelTypeUID[] { TEMPERATURE_CHANNELTYPEUID, HUMIDITY_CHANNELTYPEUID,
                DATEUTC_CHANNELTYPEUID, SOFTWARETYPE_CHANNELTYPEUID, REALTIME_FREQUENCY_CHANNELTYPEUID,
                LAST_QUERY_STATE_CHANNELTYPEUID, LAST_RECEIVED_DATETIME_CHANNELTYPEUID,
                LAST_QUERY_TRIGGER_CHANNELTYPEUID };
        List<ChannelTypeUID> before = handler.getThing().getChannels().stream().map(Channel::getChannelTypeUID)
                .collect(Collectors.toList());
        assertThat(before, hasItems(expectedBefore));

        // When
        final String secondDeviceQueryString = "ID=dfggger&" + "PASSWORD=XXXXXX&" + "lowbatt=1&" + "soilmoisture1=78&"
                + "soilmoisture2=73&" + "solarradiation=42.24&" + "dateutc=2021-02-07%2014:04:03&"
                + "softwaretype=WH2600%20V2.2.8&" + "action=updateraw&" + "realtime=1&" + "rtfreq=5";
        MetaData.Request request = new MetaData.Request("GET", new HttpURI(
                "http://localhost" + WundergroundUpdateReceiverServlet.SERVLET_URL + "?" + secondDeviceQueryString),
                HttpVersion.HTTP_1_1, new HttpFields());
        Request req2 = new Request(httpChannel, null);
        req2.setMetaData(request);
        sut.activate();

        // Then
        assertThat(sut.isActive(), is(true));

        // When
        sut.doGet(req2, mock(HttpServletResponse.class, Answers.RETURNS_MOCKS));

        // Then
        List<ChannelTypeUID> actual = handler.getThing().getChannels().stream().map(Channel::getChannelTypeUID)
                .collect(Collectors.toList());
        assertThat(actual, equalTo(before));
    }

    class TestChannelTypeRegistry extends ChannelTypeRegistry {

        TestChannelTypeRegistry() {
            super();
            ChannelTypeProvider provider = mock(ChannelTypeProvider.class);
            when(provider.getChannelType(eq(SOFTWARETYPE_CHANNELTYPEUID), any())).thenReturn(
                    new StateChannelTypeBuilderImpl(SOFTWARETYPE_CHANNELTYPEUID, "Software type", "String").build());
            when(provider.getChannelType(eq(TEMPERATURE_CHANNELTYPEUID), any()))
                    .thenReturn(DefaultSystemChannelTypeProvider.SYSTEM_OUTDOOR_TEMPERATURE);
            when(provider.getChannelType(eq(SOIL_MOISTURE_CHANNELTYPEUID), any()))
                    .thenReturn(new StateChannelTypeBuilderImpl(SOIL_MOISTURE_CHANNELTYPEUID, "Soilmoisture",
                            "Number:Dimensionless").build());
            when(provider.getChannelType(eq(SOLARRADIATION_CHANNELTYPEUID), any()))
                    .thenReturn(new StateChannelTypeBuilderImpl(SOLARRADIATION_CHANNELTYPEUID, "Solar Radiation",
                            "Number:Intensity").build());
            when(provider.getChannelType(eq(HUMIDITY_CHANNELTYPEUID), any())).thenReturn(
                    new StateChannelTypeBuilderImpl(HUMIDITY_CHANNELTYPEUID, "Humidity", "Number:Dimensionless")
                            .build());
            when(provider.getChannelType(eq(DATEUTC_CHANNELTYPEUID), any())).thenReturn(
                    new StateChannelTypeBuilderImpl(DATEUTC_CHANNELTYPEUID, "Last Updated", "String").build());
            when(provider.getChannelType(eq(LOW_BATTERY_CHANNELTYPEUID), any()))
                    .thenReturn(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_LOW_BATTERY);
            when(provider.getChannelType(eq(REALTIME_FREQUENCY_CHANNELTYPEUID), any())).thenReturn(
                    new StateChannelTypeBuilderImpl(REALTIME_FREQUENCY_CHANNELTYPEUID, "Realtime frequency", "Number")
                            .build());
            when(provider.getChannelType(eq(LAST_QUERY_STATE_CHANNELTYPEUID), any())).thenReturn(
                    new StateChannelTypeBuilderImpl(LAST_QUERY_STATE_CHANNELTYPEUID, "The last query", "String")
                            .build());
            when(provider.getChannelType(eq(LAST_RECEIVED_DATETIME_CHANNELTYPEUID), any())).thenReturn(
                    new StateChannelTypeBuilderImpl(LAST_RECEIVED_DATETIME_CHANNELTYPEUID, "Last Received", "DateTime")
                            .build());
            when(provider.getChannelType(eq(LAST_QUERY_TRIGGER_CHANNELTYPEUID), any())).thenReturn(
                    new TriggerChannelTypeBuilderImpl(LAST_QUERY_TRIGGER_CHANNELTYPEUID, "The last query").build());
            this.addChannelTypeProvider(provider);
            this.addChannelTypeProvider(new WundergroundUpdateReceiverUnknownChannelTypeProvider());
        }
    }
}
