/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

/**
 * @author Daniel Demus - Initial contribution
 */
class WundergroundUpdateReceiverServletTest {

    private static final String stationId1 = "abcd1234";
    private static final String stationId2 = "1234abcd";
    private static final String reqStationId = "dfggger";
    private static final ThingUID testThindUid = new ThingUID(
            WundergroundUpdateReceiverBindingConstants.THING_TYPE_UPDATE_RECEIVER, "test-receiver");

    private @Mock HttpService httpService;

    @BeforeEach
    public void setUp() {
        openMocks(this);
    }

    @Test
    void the_servlet_is_active_after_the_first_handler_is_added() throws ServletException, NamespaceException {
        // Given
        WundergroundUpdateReceiverServlet sut = new WundergroundUpdateReceiverServlet(httpService);
        WundergroundUpdateReceiverHandler handler = mock(WundergroundUpdateReceiverHandler.class);
        when(handler.getStationId()).thenReturn(stationId1);

        // When
        sut.addHandler(handler);

        // Then
        verify(httpService).registerServlet(eq(WundergroundUpdateReceiverServlet.SERVLET_URL), eq(sut), any(), any());
        assertThat(sut.isActive(), is(true));
    }

    @Test
    void the_servlet_is_inactive_after_the_last_handler_is_removed() throws ServletException, NamespaceException {
        // Given
        WundergroundUpdateReceiverServlet sut = new WundergroundUpdateReceiverServlet(httpService);
        WundergroundUpdateReceiverHandler handler = mock(WundergroundUpdateReceiverHandler.class);
        when(handler.getStationId()).thenReturn(stationId1);

        // When
        sut.addHandler(handler);

        // Then
        verify(httpService).registerServlet(eq(WundergroundUpdateReceiverServlet.SERVLET_URL), eq(sut), any(), any());
        assertThat(sut.isActive(), is(true));

        // When
        sut.removeHandler(handler.getStationId());

        // Then
        verify(httpService).unregister(WundergroundUpdateReceiverServlet.SERVLET_URL);
        assertThat(sut.isActive(), is(false));
    }

    @Test
    void on_dispose_all_handlers_are_removed_and_servlet_is_inactive() throws ServletException, NamespaceException {
        // Given
        WundergroundUpdateReceiverServlet sut = new WundergroundUpdateReceiverServlet(httpService);
        WundergroundUpdateReceiverHandler handler1 = mock(WundergroundUpdateReceiverHandler.class);
        when(handler1.getStationId()).thenReturn(stationId1);
        WundergroundUpdateReceiverHandler handler2 = mock(WundergroundUpdateReceiverHandler.class);
        when(handler2.getStationId()).thenReturn(stationId2);

        // When
        sut.addHandler(handler1);
        sut.addHandler(handler2);

        // Then
        verify(httpService).registerServlet(eq(WundergroundUpdateReceiverServlet.SERVLET_URL), eq(sut), any(), any());
        assertThat(sut.isActive(), is(true));

        // When
        sut.dispose();

        // Then
        verify(httpService).unregister(WundergroundUpdateReceiverServlet.SERVLET_URL);
        assertThat(sut.isActive(), is(false));
    }

    @Test
    void changed_station_id_propagates_to_handler_key() throws ServletException, NamespaceException {
        // Given
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(testThindUid);
        when(thing.getConfiguration()).thenReturn(new Configuration(Map.of("stationId", stationId1)));
        when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);
        WundergroundUpdateReceiverServlet sut = new WundergroundUpdateReceiverServlet(httpService);
        WundergroundUpdateReceiverHandler handler = new WundergroundUpdateReceiverHandler(thing, sut);

        // When
        handler.initialize();

        // Then
        verify(httpService).registerServlet(eq(WundergroundUpdateReceiverServlet.SERVLET_URL), eq(sut), any(), any());
        assertThat(sut.isActive(), is(true));
        assertThat(sut.getStationIds(), hasItems(stationId1));

        // When
        handler.handleConfigurationUpdate(Map.of("stationId", stationId2));

        // Then
        assertThat(sut.isActive(), is(true));
        assertThat(sut.getStationIds(), hasItems(stationId2));
    }

    @Test
    void a_get_request_is_correctly_parsed() throws IOException {
        // Given
        ThingUID testThingUID = new ThingUID(WundergroundUpdateReceiverBindingConstants.THING_TYPE_UPDATE_RECEIVER,
                "test-receiver");
        final String queryString = "ID=dfggger&PASSWORD=XXXXXX&tempf=26.1&humidity=74&dewptf=18.9&windchillf=26.1&winddir=14&windspeedmph=1.34&windgustmph=2.46&rainin=0.00&dailyrainin=0.00&weeklyrainin=0.00&monthlyrainin=0.08&yearlyrainin=3.06&solarradiation=42.24&UV=1&indoortempf=69.3&indoorhumidity=32&baromin=30.39&AqNOX=21&dateutc=2021-02-07%2014:04:03&softwaretype=WH2600%20V2.2.8&action=updateraw&realtime=1&rtfreq=5";
        WundergroundUpdateReceiverServlet sut = new WundergroundUpdateReceiverServlet(httpService);
        List<Channel> channels = List.of(
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, "metadata",
                                WundergroundUpdateReceiverBindingConstants.DATEUTC), "String")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, "wind",
                                WundergroundUpdateReceiverBindingConstants.WIND_DIRECTION), "Number:Angle")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, "wind",
                                WundergroundUpdateReceiverBindingConstants.WIND_SPEED), "Number:Speed")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, "wind",
                                WundergroundUpdateReceiverBindingConstants.GUST_SPEED), "Number:Speed")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, "temperature",
                                WundergroundUpdateReceiverBindingConstants.TEMPERATURE), "Number:Temperature")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, "rain",
                                WundergroundUpdateReceiverBindingConstants.RAIN_IN), "Number:Length")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, "sunlight",
                                WundergroundUpdateReceiverBindingConstants.SOLAR_RADIATION), "Number:Intensity")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, "sunlight", WundergroundUpdateReceiverBindingConstants.UV),
                                "Number")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, "pressure",
                                WundergroundUpdateReceiverBindingConstants.BAROM_IN), "Number:Pressure")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, "humidity",
                                WundergroundUpdateReceiverBindingConstants.DEWPOINT), "Number:Temperature")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, "humidity",
                                WundergroundUpdateReceiverBindingConstants.HUMIDITY), "Number:Dimensionless")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, "pollution",
                                WundergroundUpdateReceiverBindingConstants.AQ_NOX), "Number:Dimensionless")
                        .withKind(ChannelKind.STATE).build(),
                ChannelBuilder
                        .create(new ChannelUID(testThingUID, "metadata",
                                WundergroundUpdateReceiverBindingConstants.LAST_QUERY + "-trigger"), "StringType")
                        .withKind(ChannelKind.TRIGGER).build());

        Configuration config = new Configuration(Map.of("stationId", reqStationId));
        Thing testThing = ThingBuilder
                .create(WundergroundUpdateReceiverBindingConstants.THING_TYPE_UPDATE_RECEIVER, testThingUID)
                .withChannels(channels).withConfiguration(config).build();
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        WundergroundUpdateReceiverHandler handler = new WundergroundUpdateReceiverHandler(testThing, sut);
        handler.setCallback(callback);
        handler.initialize();

        HttpChannel httpChannel = mock(HttpChannel.class);
        MetaData.Request request = new MetaData.Request("GET",
                new HttpURI("http://localhost/" + WundergroundUpdateReceiverServlet.SERVLET_URL + "?" + queryString),
                HttpVersion.HTTP_1_1, new HttpFields());
        Request req = new Request(httpChannel, null);
        req.setMetaData(request);

        // When
        sut.doGet(req, mock(HttpServletResponse.class, Answers.RETURNS_MOCKS));

        // Then
        verify(callback).stateUpdated(
                new ChannelUID(testThindUid, "metadata", WundergroundUpdateReceiverBindingConstants.DATEUTC),
                StringType.valueOf("2021-02-07 14:04:03"));
        verify(callback).stateUpdated(
                new ChannelUID(testThindUid, "wind", WundergroundUpdateReceiverBindingConstants.WIND_DIRECTION),
                new QuantityType<>(14, Units.DEGREE_ANGLE));
        verify(callback).stateUpdated(
                new ChannelUID(testThindUid, "wind", WundergroundUpdateReceiverBindingConstants.WIND_SPEED),
                new QuantityType<>(1.34, ImperialUnits.MILES_PER_HOUR));
        verify(callback).stateUpdated(
                new ChannelUID(testThindUid, "wind", WundergroundUpdateReceiverBindingConstants.GUST_SPEED),
                new QuantityType<>(2.46, ImperialUnits.MILES_PER_HOUR));
        verify(callback).stateUpdated(
                new ChannelUID(testThindUid, "temperature", WundergroundUpdateReceiverBindingConstants.TEMPERATURE),
                new QuantityType<>(26.1, ImperialUnits.FAHRENHEIT));
        verify(callback).stateUpdated(
                new ChannelUID(testThindUid, "rain", WundergroundUpdateReceiverBindingConstants.RAIN_IN),
                new QuantityType<>(0, ImperialUnits.INCH));
        verify(callback).stateUpdated(
                new ChannelUID(testThindUid, "sunlight", WundergroundUpdateReceiverBindingConstants.SOLAR_RADIATION),
                new QuantityType<>(42.24, Units.IRRADIANCE));
        verify(callback).stateUpdated(
                new ChannelUID(testThindUid, "sunlight", WundergroundUpdateReceiverBindingConstants.UV),
                new DecimalType(1));
        verify(callback).stateUpdated(
                new ChannelUID(testThindUid, "pressure", WundergroundUpdateReceiverBindingConstants.BAROM_IN),
                new QuantityType<>(30.39, ImperialUnits.INCH_OF_MERCURY));
        verify(callback).stateUpdated(
                new ChannelUID(testThindUid, "humidity", WundergroundUpdateReceiverBindingConstants.DEWPOINT),
                new QuantityType<>(18.9, ImperialUnits.FAHRENHEIT));
        verify(callback).stateUpdated(
                new ChannelUID(testThindUid, "humidity", WundergroundUpdateReceiverBindingConstants.HUMIDITY),
                new QuantityType<>(74, Units.PERCENT));
        verify(callback).stateUpdated(
                new ChannelUID(testThindUid, "pollution", WundergroundUpdateReceiverBindingConstants.AQ_NOX),
                new QuantityType<>(21, Units.PARTS_PER_BILLION));
        verify(callback, never()).stateUpdated(
                new ChannelUID(testThindUid, "metadata", WundergroundUpdateReceiverBindingConstants.SOFTWARE_TYPE),
                StringType.valueOf("WH2600 V2.2.8"));
        verify(callback)
                .stateUpdated(
                        eq(new ChannelUID(testThindUid, "metadata",
                                WundergroundUpdateReceiverBindingConstants.LAST_RECEIVED_DATETIME)),
                        any(StringType.class));
        verify(callback).stateUpdated(
                new ChannelUID(testThindUid, "metadata",
                        WundergroundUpdateReceiverBindingConstants.LAST_QUERY + "-state"),
                StringType.valueOf(queryString));
        verify(callback).channelTriggered(testThing, new ChannelUID(testThindUid, "metadata",
                WundergroundUpdateReceiverBindingConstants.LAST_QUERY + "-trigger"), queryString);
    }
}
