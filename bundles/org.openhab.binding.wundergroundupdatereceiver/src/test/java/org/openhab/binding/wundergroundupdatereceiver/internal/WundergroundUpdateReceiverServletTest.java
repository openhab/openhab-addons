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
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsIterableContaining.hasItems;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.openhab.binding.wundergroundupdatereceiver.internal.WundergroundUpdateReceiverBindingConstants.DATEUTC_DATETIME_CHANNELTYPEUID;
import static org.openhab.binding.wundergroundupdatereceiver.internal.WundergroundUpdateReceiverBindingConstants.HUMIDITY_GROUP;
import static org.openhab.binding.wundergroundupdatereceiver.internal.WundergroundUpdateReceiverBindingConstants.LAST_QUERY_STATE_CHANNELTYPEUID;
import static org.openhab.binding.wundergroundupdatereceiver.internal.WundergroundUpdateReceiverBindingConstants.LAST_QUERY_TRIGGER_CHANNELTYPEUID;
import static org.openhab.binding.wundergroundupdatereceiver.internal.WundergroundUpdateReceiverBindingConstants.LAST_RECEIVED_DATETIME_CHANNELTYPEUID;
import static org.openhab.binding.wundergroundupdatereceiver.internal.WundergroundUpdateReceiverBindingConstants.METADATA_GROUP;
import static org.openhab.binding.wundergroundupdatereceiver.internal.WundergroundUpdateReceiverBindingConstants.POLLUTION_GROUP;
import static org.openhab.binding.wundergroundupdatereceiver.internal.WundergroundUpdateReceiverBindingConstants.PRESSURE_GROUP;
import static org.openhab.binding.wundergroundupdatereceiver.internal.WundergroundUpdateReceiverBindingConstants.RAIN_GROUP;
import static org.openhab.binding.wundergroundupdatereceiver.internal.WundergroundUpdateReceiverBindingConstants.SUNLIGHT_GROUP;
import static org.openhab.binding.wundergroundupdatereceiver.internal.WundergroundUpdateReceiverBindingConstants.TEMPERATURE_GROUP;
import static org.openhab.binding.wundergroundupdatereceiver.internal.WundergroundUpdateReceiverBindingConstants.WIND_GROUP;

import java.io.IOException;
import java.util.List;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ManagedThingProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.osgi.service.http.NamespaceException;

/**
 * @author Daniel Demus - Initial contribution
 */
@NonNullByDefault({})
class WundergroundUpdateReceiverServletTest {

    private static final String STATION_ID_1 = "abcd1234";
    private static final String STATION_ID_2 = "1234abcd";
    private static final String REQ_STATION_ID = "dfggger";
    private static final ThingUID TEST_THING_UID = new ThingUID(
            WundergroundUpdateReceiverBindingConstants.THING_TYPE_UPDATE_RECEIVER, "test-receiver");

    private @Mock ChannelTypeRegistry channelTypeRegistry;
    private @Mock WundergroundUpdateReceiverDiscoveryService discoveryService;
    private @Mock ManagedThingProvider managedThingProvider;

    @BeforeEach
    public void setUp() {
        openMocks(this);
    }

    @Test
    void theServletIsActiveAfterTheFirstHandlerIsAdded() throws ServletException, NamespaceException {
        // Given
        WundergroundUpdateReceiverServlet sut = new WundergroundUpdateReceiverServlet(discoveryService);
        WundergroundUpdateReceiverHandler handler = mock(WundergroundUpdateReceiverHandler.class);
        when(handler.getStationId()).thenReturn(STATION_ID_1);

        // When
        sut.addHandler(handler);

        // Then
        assertThat(sut.isActive(), is(true));
    }

    @Test
    void theServletIsInactiveAfterTheLastHandlerIsRemovedAndBackgroundDiscoveryIsDisabled()
            throws ServletException, NamespaceException {
        // Given
        WundergroundUpdateReceiverServlet sut = new WundergroundUpdateReceiverServlet(discoveryService);
        WundergroundUpdateReceiverHandler handler = mock(WundergroundUpdateReceiverHandler.class);
        when(handler.getStationId()).thenReturn(STATION_ID_1);
        when(discoveryService.isBackgroundDiscoveryEnabled()).thenReturn(false);

        // When
        sut.addHandler(handler);

        // Then
        assertThat(sut.isActive(), is(true));

        // When
        sut.removeHandler(handler.getStationId());

        // Then
        assertThat(sut.isActive(), is(false));
    }

    @Test
    void theServletIsActiveAfterTheLastHandlerIsRemovedButBackgroundDiscoveryIsEnabled()
            throws ServletException, NamespaceException {
        // Given
        WundergroundUpdateReceiverServlet sut = new WundergroundUpdateReceiverServlet(discoveryService);
        WundergroundUpdateReceiverHandler handler = mock(WundergroundUpdateReceiverHandler.class);
        when(handler.getStationId()).thenReturn(STATION_ID_1);
        when(discoveryService.isBackgroundDiscoveryEnabled()).thenReturn(true);

        // When
        sut.addHandler(handler);

        // Then
        assertThat(sut.isActive(), is(true));

        // When
        sut.removeHandler(handler.getStationId());

        // Then
        assertThat(sut.isActive(), is(true));
    }

    @Test
    void onDisposeAllHandlersAreRemovedAndServletIsInactive() throws ServletException, NamespaceException {
        // Given
        WundergroundUpdateReceiverServlet sut = new WundergroundUpdateReceiverServlet(discoveryService);
        WundergroundUpdateReceiverHandler handler1 = mock(WundergroundUpdateReceiverHandler.class);
        when(handler1.getStationId()).thenReturn(STATION_ID_1);
        WundergroundUpdateReceiverHandler handler2 = mock(WundergroundUpdateReceiverHandler.class);
        when(handler2.getStationId()).thenReturn(STATION_ID_2);

        // When
        sut.addHandler(handler1);
        sut.addHandler(handler2);

        // Then
        assertThat(sut.isActive(), is(true));

        // When
        sut.dispose();

        // Then
        assertThat(sut.isActive(), is(false));
    }

    @Test
    void OnDisposeAllHandlersAreRemovedAndServletIsInactiveEvenThoughBackgroundDiscoveryIsEnabled()
            throws ServletException, NamespaceException {
        // Given
        WundergroundUpdateReceiverServlet sut = new WundergroundUpdateReceiverServlet(discoveryService);
        WundergroundUpdateReceiverHandler handler1 = mock(WundergroundUpdateReceiverHandler.class);
        when(handler1.getStationId()).thenReturn(STATION_ID_1);
        WundergroundUpdateReceiverHandler handler2 = mock(WundergroundUpdateReceiverHandler.class);
        when(handler2.getStationId()).thenReturn(STATION_ID_2);
        when(discoveryService.isBackgroundDiscoveryEnabled()).thenReturn(true);

        // When
        sut.addHandler(handler1);
        sut.addHandler(handler2);

        // Then
        assertThat(sut.isActive(), is(true));

        // When
        sut.dispose();

        // Then
        assertThat(sut.isActive(), is(false));
    }

    @Test
    void changedStationIdPropagatesToHandlerKey() throws ServletException, NamespaceException {
        // Given
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(TEST_THING_UID);
        when(thing.getConfiguration()).thenReturn(new Configuration(
                Map.of(WundergroundUpdateReceiverBindingConstants.REPRESENTATION_PROPERTY, STATION_ID_1)));
        when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(this.channelTypeRegistry.getChannelType(LAST_RECEIVED_DATETIME_CHANNELTYPEUID))
                .thenReturn(ChannelTypeBuilder.state(LAST_RECEIVED_DATETIME_CHANNELTYPEUID, "Label", "String").build());
        when(this.channelTypeRegistry.getChannelType(DATEUTC_DATETIME_CHANNELTYPEUID))
                .thenReturn(ChannelTypeBuilder.state(DATEUTC_DATETIME_CHANNELTYPEUID, "Label", "DateTime").build());
        when(this.channelTypeRegistry.getChannelType(LAST_QUERY_STATE_CHANNELTYPEUID))
                .thenReturn(ChannelTypeBuilder.state(LAST_QUERY_STATE_CHANNELTYPEUID, "Label", "String").build());
        when(this.channelTypeRegistry.getChannelType(LAST_QUERY_TRIGGER_CHANNELTYPEUID))
                .thenReturn(ChannelTypeBuilder.trigger(LAST_QUERY_TRIGGER_CHANNELTYPEUID, "Label").build());
        WundergroundUpdateReceiverServlet sut = new WundergroundUpdateReceiverServlet(discoveryService);
        WundergroundUpdateReceiverHandler handler = new WundergroundUpdateReceiverHandler(thing, sut, discoveryService,
                new WundergroundUpdateReceiverUnknownChannelTypeProvider(), channelTypeRegistry, managedThingProvider);
        ThingHandlerCallback mockCallback = mock(ThingHandlerCallback.class);
        handler.setCallback(mockCallback);

        // When
        handler.initialize();

        // Then
        assertThat(sut.isActive(), is(true));
        assertThat(sut.getStationIds(), hasItems(STATION_ID_1));

        // When
        handler.handleConfigurationUpdate(
                Map.of(WundergroundUpdateReceiverBindingConstants.REPRESENTATION_PROPERTY, STATION_ID_2));

        // Then
        assertThat(sut.isActive(), is(true));
        ArgumentCaptor<Thing> thingArg = ArgumentCaptor.forClass(Thing.class);
        verify(mockCallback).configurationUpdated(thingArg.capture());
        assertThat(thingArg.getValue().getConfiguration().getProperties()
                .get(WundergroundUpdateReceiverBindingConstants.REPRESENTATION_PROPERTY), is(STATION_ID_2));
    }

    @Test
    void aGetRequestIsCorrectlyParsed() throws IOException {
        // Given
        ThingUID testThingUID = new ThingUID(WundergroundUpdateReceiverBindingConstants.THING_TYPE_UPDATE_RECEIVER,
                "test-receiver");
        final String queryString = "ID=dfggger&PASSWORD=XXXXXX&tempf=26.1&humidity=74&dewptf=18.9&windchillf=26.1&winddir=14&windspeedmph=1.34&windgustmph=2.46&rainin=0.00&dailyrainin=0.00&weeklyrainin=0.00&monthlyrainin=0.08&yearlyrainin=3.06&solarradiation=42.24&UV=1&indoortempf=69.3&indoorhumidity=32&baromin=30.39&AqNOX=21&lowbatt=1&dateutc=2021-02-07%2014:04:03&softwaretype=WH2600%20V2.2.8&action=updateraw&realtime=1&rtfreq=5";
        WundergroundUpdateReceiverServlet sut = new WundergroundUpdateReceiverServlet(discoveryService);
        List<Channel> channels = List.of(
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, METADATA_GROUP,
                                WundergroundUpdateReceiverBindingConstants.DATEUTC), "String")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, METADATA_GROUP,
                                WundergroundUpdateReceiverBindingConstants.REALTIME_FREQUENCY), "Number")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, METADATA_GROUP,
                                WundergroundUpdateReceiverBindingConstants.LOW_BATTERY), "Switch")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, WIND_GROUP,
                                WundergroundUpdateReceiverBindingConstants.WIND_DIRECTION), "Number:Angle")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, WIND_GROUP,
                                WundergroundUpdateReceiverBindingConstants.WIND_SPEED), "Number:Speed")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, WIND_GROUP,
                                WundergroundUpdateReceiverBindingConstants.GUST_SPEED), "Number:Speed")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, TEMPERATURE_GROUP,
                                WundergroundUpdateReceiverBindingConstants.TEMPERATURE), "Number:Temperature")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, RAIN_GROUP,
                                WundergroundUpdateReceiverBindingConstants.RAIN_IN), "Number:Length")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, SUNLIGHT_GROUP,
                                WundergroundUpdateReceiverBindingConstants.SOLAR_RADIATION), "Number:Intensity")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, SUNLIGHT_GROUP,
                                WundergroundUpdateReceiverBindingConstants.UV), "Number")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, PRESSURE_GROUP,
                                WundergroundUpdateReceiverBindingConstants.BAROM_IN), "Number:Pressure")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, HUMIDITY_GROUP,
                                WundergroundUpdateReceiverBindingConstants.DEWPOINT), "Number:Temperature")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, HUMIDITY_GROUP,
                                WundergroundUpdateReceiverBindingConstants.HUMIDITY), "Number:Dimensionless")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, POLLUTION_GROUP,
                                WundergroundUpdateReceiverBindingConstants.AQ_NOX), "Number:Dimensionless")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, METADATA_GROUP,
                                WundergroundUpdateReceiverBindingConstants.LAST_QUERY_TRIGGER), "StringType")
                        .withKind(ChannelKind.TRIGGER).build());

        Configuration config = new Configuration(Map.of("stationId", REQ_STATION_ID));
        Thing testThing = ThingBuilder
                .create(WundergroundUpdateReceiverBindingConstants.THING_TYPE_UPDATE_RECEIVER, testThingUID)
                .withChannels(channels).withConfiguration(config).build();
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        WundergroundUpdateReceiverHandler handler = new WundergroundUpdateReceiverHandler(testThing, sut,
                discoveryService, new WundergroundUpdateReceiverUnknownChannelTypeProvider(), channelTypeRegistry,
                managedThingProvider);
        handler.setCallback(callback);
        handler.initialize();

        HttpChannel httpChannel = mock(HttpChannel.class);
        MetaData.Request request = new MetaData.Request("GET",
                new HttpURI("http://localhost" + WundergroundUpdateReceiverServlet.SERVLET_URL + "?" + queryString),
                HttpVersion.HTTP_1_1, new HttpFields());
        Request req = new Request(httpChannel, null);
        req.setMetaData(request);

        // When
        sut.doGet(req, mock(HttpServletResponse.class, Answers.RETURNS_MOCKS));

        // Then
        verify(callback).stateUpdated(
                new ChannelUID(TEST_THING_UID, METADATA_GROUP, WundergroundUpdateReceiverBindingConstants.DATEUTC),
                StringType.valueOf("2021-02-07 14:04:03"));
        verify(callback).stateUpdated(
                new ChannelUID(TEST_THING_UID, METADATA_GROUP, WundergroundUpdateReceiverBindingConstants.LOW_BATTERY),
                OnOffType.ON);
        verify(callback).stateUpdated(new ChannelUID(TEST_THING_UID, METADATA_GROUP,
                WundergroundUpdateReceiverBindingConstants.REALTIME_FREQUENCY), new DecimalType(5));
        verify(callback).stateUpdated(
                new ChannelUID(TEST_THING_UID, WIND_GROUP, WundergroundUpdateReceiverBindingConstants.WIND_DIRECTION),
                new QuantityType<>(14, Units.DEGREE_ANGLE));
        verify(callback).stateUpdated(
                new ChannelUID(TEST_THING_UID, WIND_GROUP, WundergroundUpdateReceiverBindingConstants.WIND_SPEED),
                new QuantityType<>(1.34, ImperialUnits.MILES_PER_HOUR));
        verify(callback).stateUpdated(
                new ChannelUID(TEST_THING_UID, WIND_GROUP, WundergroundUpdateReceiverBindingConstants.GUST_SPEED),
                new QuantityType<>(2.46, ImperialUnits.MILES_PER_HOUR));
        verify(callback).stateUpdated(
                new ChannelUID(TEST_THING_UID, TEMPERATURE_GROUP,
                        WundergroundUpdateReceiverBindingConstants.TEMPERATURE),
                new QuantityType<>(26.1, ImperialUnits.FAHRENHEIT));
        verify(callback).stateUpdated(
                new ChannelUID(TEST_THING_UID, RAIN_GROUP, WundergroundUpdateReceiverBindingConstants.RAIN_IN),
                new QuantityType<>(0, ImperialUnits.INCH));
        verify(callback).stateUpdated(
                new ChannelUID(TEST_THING_UID, SUNLIGHT_GROUP,
                        WundergroundUpdateReceiverBindingConstants.SOLAR_RADIATION),
                new QuantityType<>(42.24, Units.IRRADIANCE));
        verify(callback).stateUpdated(
                new ChannelUID(TEST_THING_UID, SUNLIGHT_GROUP, WundergroundUpdateReceiverBindingConstants.UV),
                new DecimalType(1));
        verify(callback).stateUpdated(
                new ChannelUID(TEST_THING_UID, PRESSURE_GROUP, WundergroundUpdateReceiverBindingConstants.BAROM_IN),
                new QuantityType<>(30.39, ImperialUnits.INCH_OF_MERCURY));
        verify(callback).stateUpdated(
                new ChannelUID(TEST_THING_UID, HUMIDITY_GROUP, WundergroundUpdateReceiverBindingConstants.DEWPOINT),
                new QuantityType<>(18.9, ImperialUnits.FAHRENHEIT));
        verify(callback).stateUpdated(
                new ChannelUID(TEST_THING_UID, HUMIDITY_GROUP, WundergroundUpdateReceiverBindingConstants.HUMIDITY),
                new QuantityType<>(74, Units.PERCENT));
        verify(callback).stateUpdated(
                new ChannelUID(TEST_THING_UID, POLLUTION_GROUP, WundergroundUpdateReceiverBindingConstants.AQ_NOX),
                new QuantityType<>(21, Units.PARTS_PER_BILLION));
        verify(callback, never()).stateUpdated(new ChannelUID(TEST_THING_UID, METADATA_GROUP,
                WundergroundUpdateReceiverBindingConstants.SOFTWARE_TYPE), StringType.valueOf("WH2600 V2.2.8"));
        verify(callback).stateUpdated(new ChannelUID(TEST_THING_UID, METADATA_GROUP,
                WundergroundUpdateReceiverBindingConstants.LAST_QUERY_STATE), StringType.valueOf(queryString));
        verify(callback).stateUpdated(eq(new ChannelUID(TEST_THING_UID, METADATA_GROUP,
                WundergroundUpdateReceiverBindingConstants.LAST_RECEIVED)), any(DateTimeType.class));
        verify(callback).channelTriggered(testThing, new ChannelUID(TEST_THING_UID, METADATA_GROUP,
                WundergroundUpdateReceiverBindingConstants.LAST_QUERY_TRIGGER), queryString);
    }

    @Test
    void aGetRequestWithIndexedParametresAreCorrectlyParsed() throws IOException {
        // Given
        ThingUID testThingUID = new ThingUID(WundergroundUpdateReceiverBindingConstants.THING_TYPE_UPDATE_RECEIVER,
                "test-receiver");
        final String queryString = "ID=dfggger&PASSWORD=XXXXXX&temp1f=26.1&humidity=74&temp2f=25.1&lowbatt=1&soilmoisture1=78&soilmoisture2=73&dateutc=2021-02-07%2014:04:03&softwaretype=WH2600%20V2.2.8&action=updateraw&realtime=1&rtfreq=5";
        WundergroundUpdateReceiverServlet sut = new WundergroundUpdateReceiverServlet(discoveryService);
        List<Channel> channels = List.of(
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, METADATA_GROUP,
                                WundergroundUpdateReceiverBindingConstants.DATEUTC), "String")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, METADATA_GROUP,
                                WundergroundUpdateReceiverBindingConstants.REALTIME_FREQUENCY), "Number")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, METADATA_GROUP,
                                WundergroundUpdateReceiverBindingConstants.LOW_BATTERY), "Switch")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder.create(new ChannelUID(testThingUID, TEMPERATURE_GROUP, "temp1f"), "Number:Temperature")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder.create(new ChannelUID(testThingUID, TEMPERATURE_GROUP, "temp2f"), "Number:Temperature")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, HUMIDITY_GROUP, "soilmoisture1"), "Number:Dimensionless")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, HUMIDITY_GROUP, "soilmoisture2"), "Number:Dimensionless")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, HUMIDITY_GROUP,
                                WundergroundUpdateReceiverBindingConstants.HUMIDITY), "Number:Dimensionless")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, METADATA_GROUP,
                                WundergroundUpdateReceiverBindingConstants.LAST_QUERY_TRIGGER), "StringType")
                        .withKind(ChannelKind.TRIGGER).build());

        Configuration config = new Configuration(Map.of("stationId", REQ_STATION_ID));
        Thing testThing = ThingBuilder
                .create(WundergroundUpdateReceiverBindingConstants.THING_TYPE_UPDATE_RECEIVER, testThingUID)
                .withChannels(channels).withConfiguration(config).build();
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        WundergroundUpdateReceiverHandler handler = new WundergroundUpdateReceiverHandler(testThing, sut,
                discoveryService, new WundergroundUpdateReceiverUnknownChannelTypeProvider(), channelTypeRegistry,
                managedThingProvider);
        handler.setCallback(callback);
        handler.initialize();

        HttpChannel httpChannel = mock(HttpChannel.class);
        MetaData.Request request = new MetaData.Request("GET",
                new HttpURI("http://localhost" + WundergroundUpdateReceiverServlet.SERVLET_URL + "?" + queryString),
                HttpVersion.HTTP_1_1, new HttpFields());
        Request req = new Request(httpChannel, null);
        req.setMetaData(request);

        // When
        sut.doGet(req, mock(HttpServletResponse.class, Answers.RETURNS_MOCKS));

        // Then
        verify(callback).stateUpdated(
                new ChannelUID(TEST_THING_UID, METADATA_GROUP, WundergroundUpdateReceiverBindingConstants.DATEUTC),
                StringType.valueOf("2021-02-07 14:04:03"));
        verify(callback).stateUpdated(
                new ChannelUID(TEST_THING_UID, METADATA_GROUP, WundergroundUpdateReceiverBindingConstants.LOW_BATTERY),
                OnOffType.ON);
        verify(callback).stateUpdated(new ChannelUID(TEST_THING_UID, METADATA_GROUP,
                WundergroundUpdateReceiverBindingConstants.REALTIME_FREQUENCY), new DecimalType(5));
        verify(callback).stateUpdated(new ChannelUID(TEST_THING_UID, TEMPERATURE_GROUP, "temp1f"),
                new QuantityType<>(26.1, ImperialUnits.FAHRENHEIT));
        verify(callback).stateUpdated(new ChannelUID(TEST_THING_UID, TEMPERATURE_GROUP, "temp2f"),
                new QuantityType<>(25.1, ImperialUnits.FAHRENHEIT));
        verify(callback).stateUpdated(
                new ChannelUID(TEST_THING_UID, HUMIDITY_GROUP, WundergroundUpdateReceiverBindingConstants.HUMIDITY),
                new QuantityType<>(74, Units.PERCENT));
        verify(callback).stateUpdated(new ChannelUID(TEST_THING_UID, HUMIDITY_GROUP, "soilmoisture1"),
                new QuantityType<>(78, Units.PERCENT));
        verify(callback).stateUpdated(new ChannelUID(TEST_THING_UID, HUMIDITY_GROUP, "soilmoisture2"),
                new QuantityType<>(73, Units.PERCENT));
        verify(callback, never()).stateUpdated(new ChannelUID(TEST_THING_UID, METADATA_GROUP,
                WundergroundUpdateReceiverBindingConstants.SOFTWARE_TYPE), StringType.valueOf("WH2600 V2.2.8"));
        verify(callback).stateUpdated(new ChannelUID(TEST_THING_UID, METADATA_GROUP,
                WundergroundUpdateReceiverBindingConstants.LAST_QUERY_STATE), StringType.valueOf(queryString));
        verify(callback).stateUpdated(eq(new ChannelUID(TEST_THING_UID, METADATA_GROUP,
                WundergroundUpdateReceiverBindingConstants.LAST_RECEIVED)), any(DateTimeType.class));
        verify(callback).channelTriggered(testThing, new ChannelUID(TEST_THING_UID, METADATA_GROUP,
                WundergroundUpdateReceiverBindingConstants.LAST_QUERY_TRIGGER), queryString);
    }
}
