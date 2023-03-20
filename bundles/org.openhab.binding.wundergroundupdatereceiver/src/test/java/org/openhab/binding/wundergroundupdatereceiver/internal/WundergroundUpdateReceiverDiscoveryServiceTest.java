/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.DefaultSystemChannelTypeProvider;
import org.openhab.core.thing.ManagedThingProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.internal.type.StateChannelTypeBuilderImpl;
import org.openhab.core.thing.internal.type.TriggerChannelTypeBuilderImpl;
import org.openhab.core.thing.type.ChannelKind;
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
    void programmaticChannelsAreAddedCorrectlyOnce() {
        // Given
        final String queryString = "ID=dfggger&" + "PASSWORD=XXXXXX&" + "humidity=74&" + "AqPM2.5=30&"
                + "windspdmph_avg2m=10&" + "dateutc=2021-02-07%2014:04:03&" + "softwaretype=WH2600%20V2.2.8&"
                + "action=updateraw&" + "realtime=1&" + "rtfreq=5";
        MetaData.Request request = new MetaData.Request("GET",
                new HttpURI("http://localhost" + WundergroundUpdateReceiverServlet.SERVLET_URL + "?" + queryString),
                HttpVersion.HTTP_1_1, new HttpFields());
        HttpChannel httpChannel = mock(HttpChannel.class);
        Request req = new Request(httpChannel, null);
        req.setMetaData(request);

        TestChannelTypeRegistry channelTypeRegistry = new TestChannelTypeRegistry();
        WundergroundUpdateReceiverDiscoveryService discoveryService = new WundergroundUpdateReceiverDiscoveryService(
                true);
        HttpService httpService = mock(HttpService.class);
        WundergroundUpdateReceiverServlet sut = new WundergroundUpdateReceiverServlet(discoveryService);
        discoveryService.addUnhandledStationId(REQ_STATION_ID, sut.normalizeParameterMap(req.getParameterMap()));
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
        var actual = handler.getThing().getChannels();

        // Then
        assertThat(actual.size(), is(9));

        assertChannel(actual, METADATA_GROUP, LAST_RECEIVED, LAST_RECEIVED_DATETIME_CHANNELTYPEUID, ChannelKind.STATE,
                is("DateTime"));
        assertChannel(actual, METADATA_GROUP, LAST_QUERY_TRIGGER, LAST_QUERY_TRIGGER_CHANNELTYPEUID,
                ChannelKind.TRIGGER, nullValue());
        assertChannel(actual, METADATA_GROUP, LAST_QUERY_STATE, LAST_QUERY_STATE_CHANNELTYPEUID, ChannelKind.STATE,
                is("String"));
        assertChannel(actual, METADATA_GROUP, DATEUTC, DATEUTC_CHANNELTYPEUID, ChannelKind.STATE, is("String"));
        assertChannel(actual, METADATA_GROUP, REALTIME_FREQUENCY, REALTIME_FREQUENCY_CHANNELTYPEUID, ChannelKind.STATE,
                is("Number"));
        assertChannel(actual, METADATA_GROUP, SOFTWARE_TYPE, SOFTWARETYPE_CHANNELTYPEUID, ChannelKind.STATE,
                is("String"));
        assertChannel(actual, HUMIDITY_GROUP, HUMIDITY, HUMIDITY_CHANNELTYPEUID, ChannelKind.STATE,
                is("Number:Dimensionless"));
        assertChannel(actual, WIND_GROUP, WIND_SPEED_AVG_2MIN, WIND_SPEED_AVG_2MIN_CHANNELTYPEUID, ChannelKind.STATE,
                is("Number:Speed"));
        assertChannel(actual, POLLUTION_GROUP, AQ_PM2_5, PM2_5_MASS_CHANNELTYPEUID, ChannelKind.STATE,
                is("Number:Density"));
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
        WundergroundUpdateReceiverServlet sut = new WundergroundUpdateReceiverServlet(discoveryService);
        WundergroundUpdateReceiverHandler handler = mock(WundergroundUpdateReceiverHandler.class);
        when(handler.getStationId()).thenReturn(STATION_ID_1);
        sut.addHandler(handler);
        when(discoveryService.isBackgroundDiscoveryEnabled()).thenReturn(false);

        // Then
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
        HttpService httpService = mock(HttpService.class);
        WundergroundUpdateReceiverServlet sut = new WundergroundUpdateReceiverServlet(discoveryService);
        discoveryService.addUnhandledStationId(REQ_STATION_ID, sut.normalizeParameterMap(req.getParameterMap()));
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
        sut.enable();

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
        HttpService httpService = mock(HttpService.class);
        WundergroundUpdateReceiverServlet sut = new WundergroundUpdateReceiverServlet(discoveryService);
        discoveryService.addUnhandledStationId(REQ_STATION_ID, sut.normalizeParameterMap(req1.getParameterMap()));
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
        sut.enable();

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
        HttpService httpService = mock(HttpService.class);
        WundergroundUpdateReceiverServlet sut = new WundergroundUpdateReceiverServlet(discoveryService);
        discoveryService.addUnhandledStationId(REQ_STATION_ID, sut.normalizeParameterMap(req1.getParameterMap()));
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
        sut.enable();

        // Then
        assertThat(sut.isActive(), is(true));

        // When
        sut.doGet(req2, mock(HttpServletResponse.class, Answers.RETURNS_MOCKS));

        // Then
        List<ChannelTypeUID> actual = handler.getThing().getChannels().stream().map(Channel::getChannelTypeUID)
                .collect(Collectors.toList());
        assertThat(actual, equalTo(before));
    }

    @Test
    void lastQueryTriggerIsMigratedSuccessfully() throws IOException {
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

        UpdatingChannelTypeRegistry channelTypeRegistry = new UpdatingChannelTypeRegistry();
        WundergroundUpdateReceiverDiscoveryService discoveryService = new WundergroundUpdateReceiverDiscoveryService(
                true);
        HttpService httpService = mock(HttpService.class);
        WundergroundUpdateReceiverServlet sut = new WundergroundUpdateReceiverServlet(discoveryService);
        discoveryService.addUnhandledStationId(REQ_STATION_ID, sut.normalizeParameterMap(req1.getParameterMap()));
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
        var actual = handler.getThing().getChannels();

        // Then
        assertThat(actual.size(), is(8));
        assertChannel(actual, METADATA_GROUP, LAST_QUERY_TRIGGER, LAST_QUERY_TRIGGER_CHANNELTYPEUID, ChannelKind.STATE,
                is("DateTime"));

        // When
        handler.dispose();
        handler.initialize();

        final String secondDeviceQueryString = "ID=dfggger&" + "PASSWORD=XXXXXX&" + "lowbatt=1&" + "soilmoisture1=78&"
                + "soilmoisture2=73&" + "solarradiation=42.24&" + "dateutc=2021-02-07%2014:04:03&"
                + "softwaretype=WH2600%20V2.2.8&" + "action=updateraw&" + "realtime=1&" + "rtfreq=5";
        MetaData.Request request = new MetaData.Request("GET", new HttpURI(
                "http://localhost" + WundergroundUpdateReceiverServlet.SERVLET_URL + "?" + secondDeviceQueryString),
                HttpVersion.HTTP_1_1, new HttpFields());
        Request req2 = new Request(httpChannel, null);
        req2.setMetaData(request);
        sut.enable();

        // Then
        assertThat(sut.isActive(), is(true));

        // When
        sut.doGet(req2, mock(HttpServletResponse.class, Answers.RETURNS_MOCKS));
        actual = handler.getThing().getChannels();

        // Then
        assertThat(actual.size(), is(8));
        assertChannel(actual, METADATA_GROUP, LAST_QUERY_TRIGGER, LAST_QUERY_TRIGGER_CHANNELTYPEUID,
                ChannelKind.TRIGGER, nullValue());
    }

    private void assertChannel(List<Channel> channels, String expectedGroup, String expectedName,
            ChannelTypeUID expectedUid, ChannelKind expectedKind, Matcher<Object> expectedItemType) {
        ChannelUID channelUID = new ChannelUID(TEST_THING_UID, expectedGroup, expectedName);
        Channel actual = channels.stream().filter(c -> channelUID.equals(c.getUID())).findFirst().orElse(null);
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getLabel() + " UID", actual.getUID(), is(channelUID));
        assertThat(actual.getLabel() + " ChannelTypeUID", actual.getChannelTypeUID(), is(expectedUid));
        assertThat(actual.getLabel() + " Kind", actual.getKind(), is(expectedKind));
        assertThat(actual.getLabel() + " AcceptedItemType", actual.getAcceptedItemType(), expectedItemType);
    }

    abstract class AbstractTestChannelTypeRegistry extends ChannelTypeRegistry {

        protected final ChannelTypeProvider provider;

        AbstractTestChannelTypeRegistry(ChannelTypeProvider mock) {
            super();
            this.provider = mock;
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
            when(provider.getChannelType(eq(WIND_SPEED_AVG_2MIN_CHANNELTYPEUID), any()))
                    .thenReturn(new StateChannelTypeBuilderImpl(WIND_SPEED_AVG_2MIN_CHANNELTYPEUID,
                            "Wind Speed 2min Average", "Number:Speed").build());
            when(provider.getChannelType(eq(PM2_5_MASS_CHANNELTYPEUID), any())).thenReturn(
                    new StateChannelTypeBuilderImpl(PM2_5_MASS_CHANNELTYPEUID, "PM2.5 Mass", "Number:Density").build());
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
            this.addChannelTypeProvider(provider);
            this.addChannelTypeProvider(new WundergroundUpdateReceiverUnknownChannelTypeProvider());
        }
    }

    class TestChannelTypeRegistry extends AbstractTestChannelTypeRegistry {

        TestChannelTypeRegistry() {
            super(mock(ChannelTypeProvider.class));
            when(provider.getChannelType(eq(LAST_QUERY_TRIGGER_CHANNELTYPEUID), any())).thenReturn(
                    new TriggerChannelTypeBuilderImpl(LAST_QUERY_TRIGGER_CHANNELTYPEUID, "The last query").build());
        }
    }

    class UpdatingChannelTypeRegistry extends AbstractTestChannelTypeRegistry {

        UpdatingChannelTypeRegistry() {
            super(mock(ChannelTypeProvider.class));
            when(provider.getChannelType(eq(LAST_QUERY_TRIGGER_CHANNELTYPEUID), any()))
                    .thenReturn(new StateChannelTypeBuilderImpl(LAST_QUERY_TRIGGER_CHANNELTYPEUID, "The last query",
                            "DateTime").build())
                    .thenReturn(new StateChannelTypeBuilderImpl(LAST_QUERY_TRIGGER_CHANNELTYPEUID, "The last query",
                            "DateTime").build())
                    .thenReturn(new TriggerChannelTypeBuilderImpl(LAST_QUERY_TRIGGER_CHANNELTYPEUID, "The last query")
                            .build());
        }
    }
}
