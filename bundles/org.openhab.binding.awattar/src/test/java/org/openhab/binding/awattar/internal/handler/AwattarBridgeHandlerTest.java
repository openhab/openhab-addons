/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.awattar.internal.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.awattar.internal.AwattarBindingConstants.CHANNEL_END;
import static org.openhab.binding.awattar.internal.AwattarBindingConstants.CHANNEL_HOURS;
import static org.openhab.binding.awattar.internal.AwattarBindingConstants.CHANNEL_START;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.awattar.internal.AwattarBindingConstants;
import org.openhab.binding.awattar.internal.AwattarPrice;
import org.openhab.binding.awattar.internal.api.AwattarApi;
import org.openhab.binding.awattar.internal.api.AwattarApi.AwattarApiException;
import org.openhab.binding.awattar.internal.dto.AwattarApiData;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;

import com.google.gson.Gson;

/**
 * The {@link AwattarBridgeHandlerTest} contains tests for the {@link AwattarBridgeHandler}
 *
 * @author Jan N. Klug - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class AwattarBridgeHandlerTest extends JavaTest {
    public static final ThingUID BRIDGE_UID = new ThingUID(AwattarBindingConstants.THING_TYPE_BRIDGE, "testBridge");

    // bridge mocks
    private @Mock @NonNullByDefault({}) Bridge bridgeMock;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback bridgeCallbackMock;
    private @Mock @NonNullByDefault({}) HttpClient httpClientMock;
    private @Mock @NonNullByDefault({}) TimeZoneProvider timeZoneProviderMock;
    private @Mock @NonNullByDefault({}) AwattarApi awattarApiMock;

    // best price handler mocks
    private @Mock @NonNullByDefault({}) Thing bestpriceMock;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback bestPriceCallbackMock;

    private @NonNullByDefault({}) AwattarBridgeHandler bridgeHandler;

    @BeforeEach
    public void setUp() throws IOException, IllegalArgumentException, IllegalAccessException, AwattarApiException {
        // mock the API response
        try (InputStream inputStream = AwattarBridgeHandlerTest.class.getResourceAsStream("api_response.json")) {
            SortedSet<AwattarPrice> result = new TreeSet<>(Comparator.comparing(AwattarPrice::timerange));
            Gson gson = new Gson();

            String json = new String(inputStream.readAllBytes());

            // read json file into sorted set of AwattarPrices
            AwattarApiData apiData = gson.fromJson(json, AwattarApiData.class);
            apiData.data.forEach(datum -> result.add(new AwattarPrice(datum.marketprice, datum.marketprice,
                    datum.marketprice, datum.marketprice, new TimeRange(datum.startTimestamp, datum.endTimestamp))));
            when(awattarApiMock.getData()).thenReturn(result);
        }

        when(timeZoneProviderMock.getTimeZone()).thenReturn(ZoneId.of("GMT+2"));

        when(bridgeMock.getUID()).thenReturn(BRIDGE_UID);
        bridgeHandler = new AwattarBridgeHandler(bridgeMock, httpClientMock, timeZoneProviderMock);
        bridgeHandler.setCallback(bridgeCallbackMock);

        // mock the private field awattarApi
        List<Field> fields = ReflectionSupport.findFields(AwattarBridgeHandler.class,
                field -> field.getName().equals("awattarApi"), HierarchyTraversalMode.BOTTOM_UP);

        for (Field field : fields) {
            field.setAccessible(true);
            field.set(bridgeHandler, awattarApiMock);
        }

        bridgeHandler.refreshIfNeeded();
        when(bridgeMock.getHandler()).thenReturn(bridgeHandler);

        // other mocks
        when(bestpriceMock.getBridgeUID()).thenReturn(BRIDGE_UID);
        when(bestPriceCallbackMock.getBridge(any())).thenReturn(bridgeMock);
        when(bestPriceCallbackMock.isChannelLinked(any())).thenReturn(true);
    }

    @Test
    void testGetPriceForSuccess() {
        AwattarPrice price = bridgeHandler.getPriceFor(1718503200000L);

        assertThat(price, is(notNullValue()));
        Objects.requireNonNull(price);
        assertThat(price.netPrice(), is(closeTo(2.19, 0.001)));
    }

    @Test
    void testGetPriceForFail() {
        AwattarPrice price = bridgeHandler.getPriceFor(1518503200000L);

        assertThat(price, is(nullValue()));
    }

    @Test
    void testContainsPriceForTimestamp() {
        assertThat(bridgeHandler.containsPriceFor(new TimeRange(1618503200000L, 1718316000000L)), is(false));
        assertThat(bridgeHandler.containsPriceFor(new TimeRange(1618503200000L, 1718503200000L)), is(false));
        assertThat(bridgeHandler.containsPriceFor(new TimeRange(1718503200000L, 1718575200000L)), is(true));
    }

    @Test
    void testContainsPriceForRange() {
        assertThat(bridgeHandler.containsPriceFor(1618503200000L), is(false));
        assertThat(bridgeHandler.containsPriceFor(1718503200000L), is(true));
        assertThat(bridgeHandler.containsPriceFor(1718575200000L), is(false));
        assertThat(bridgeHandler.containsPriceFor(1818503200000L), is(false));
    }

    public static Stream<Arguments> testBestpriceHandler() {
        return Stream.of( //
                Arguments.of(1, true, CHANNEL_START, new DateTimeType("2024-06-15T14:00:00.000+0200")),
                Arguments.of(1, true, CHANNEL_END, new DateTimeType("2024-06-15T15:00:00.000+0200")),
                Arguments.of(1, true, CHANNEL_HOURS, new StringType("14")),
                Arguments.of(1, false, CHANNEL_START, new DateTimeType("2024-06-15T14:00:00.000+0200")),
                Arguments.of(1, false, CHANNEL_END, new DateTimeType("2024-06-15T15:00:00.000+0200")),
                Arguments.of(1, false, CHANNEL_HOURS, new StringType("14")),
                Arguments.of(2, true, CHANNEL_START, new DateTimeType("2024-06-15T13:00:00.000+0200")),
                Arguments.of(2, true, CHANNEL_END, new DateTimeType("2024-06-15T15:00:00.000+0200")),
                Arguments.of(2, true, CHANNEL_HOURS, new StringType("13,14")),
                Arguments.of(2, false, CHANNEL_START, new DateTimeType("2024-06-15T13:00:00.000+0200")),
                Arguments.of(2, false, CHANNEL_END, new DateTimeType("2024-06-15T15:00:00.000+0200")),
                Arguments.of(2, false, CHANNEL_HOURS, new StringType("13,14")));
    }

    @ParameterizedTest
    @MethodSource
    void testBestpriceHandler(int length, boolean consecutive, String channelId, State expectedState) {
        ThingUID bestPriceUid = new ThingUID(AwattarBindingConstants.THING_TYPE_BESTPRICE, "foo");
        Map<String, Object> config = Map.of("length", length, "consecutive", consecutive);
        when(bestpriceMock.getConfiguration()).thenReturn(new Configuration(config));

        AwattarBestPriceHandler handler = new AwattarBestPriceHandler(bestpriceMock, timeZoneProviderMock) {
            @Override
            protected TimeRange getRange(int start, int duration, ZoneId zoneId) {
                return new TimeRange(1718402400000L, 1718488800000L);
            }
        };

        handler.setCallback(bestPriceCallbackMock);

        ChannelUID channelUID = new ChannelUID(bestPriceUid, channelId);
        handler.refreshChannel(channelUID);
        verify(bestPriceCallbackMock).stateUpdated(channelUID, expectedState);
    }
}
