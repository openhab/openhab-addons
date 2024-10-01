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
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.ZoneId;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.awattar.internal.AwattarBindingConstants;
import org.openhab.binding.awattar.internal.api.AwattarApi;
import org.openhab.binding.awattar.internal.api.AwattarApi.AwattarApiException;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * The {@link AwattarBridgeHandlerRefreshTest} contains tests for the
 * {@link AwattarBridgeHandler} refresh logic.
 *
 * @author Thomas Leber - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
class AwattarBridgeHandlerRefreshTest extends JavaTest {
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
    public void setUp() throws IllegalArgumentException, IllegalAccessException {
        when(timeZoneProviderMock.getTimeZone()).thenReturn(ZoneId.of("GMT+2"));

        when(bridgeMock.getUID()).thenReturn(BRIDGE_UID);
        bridgeHandler = new AwattarBridgeHandler(bridgeMock, httpClientMock, timeZoneProviderMock);
        bridgeHandler.setCallback(bridgeCallbackMock);

        List<Field> fields = ReflectionSupport.findFields(AwattarBridgeHandler.class,
                field -> field.getName().equals("awattarApi"), HierarchyTraversalMode.BOTTOM_UP);

        for (Field field : fields) {
            field.setAccessible(true);
            field.set(bridgeHandler, awattarApiMock);
        }
    }

    /**
     * Test the refreshIfNeeded method with a bridge that is offline.
     *
     * @throws SecurityException
     * @throws AwattarApiException
     */
    @Test
    void testRefreshIfNeeded_ThingOffline() throws SecurityException, AwattarApiException {
        when(bridgeMock.getStatus()).thenReturn(ThingStatus.OFFLINE);

        bridgeHandler.refreshIfNeeded();

        verify(bridgeCallbackMock).statusUpdated(bridgeMock,
                new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        verify(awattarApiMock).getData();
    }

    /**
     * Test the refreshIfNeeded method with a bridge that is online and the data is
     * empty.
     *
     * @throws SecurityException
     * @throws AwattarApiException
     */
    @Test
    void testRefreshIfNeeded_DataEmpty() throws SecurityException, AwattarApiException {
        when(bridgeMock.getStatus()).thenReturn(ThingStatus.ONLINE);

        bridgeHandler.refreshIfNeeded();

        verify(bridgeCallbackMock).statusUpdated(bridgeMock,
                new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        verify(awattarApiMock).getData();
    }

    @Test
    void testNeedRefresh_ThingOffline() throws SecurityException {
        when(bridgeMock.getStatus()).thenReturn(ThingStatus.OFFLINE);

        // get private method via reflection
        Method method = ReflectionSupport.findMethod(AwattarBridgeHandler.class, "needRefresh", "").get();

        boolean result = (boolean) ReflectionSupport.invokeMethod(method, bridgeHandler);

        assertThat(result, is(true));
    }

    @Test
    void testNeedRefresh_DataEmpty() throws SecurityException, IllegalArgumentException, IllegalAccessException {
        when(bridgeMock.getStatus()).thenReturn(ThingStatus.ONLINE);

        List<Field> fields = ReflectionSupport.findFields(AwattarBridgeHandler.class,
                field -> field.getName().equals("prices"), HierarchyTraversalMode.BOTTOM_UP);

        for (Field field : fields) {
            field.setAccessible(true);
            field.set(bridgeHandler, null);
        }

        // get private method via reflection
        Method method = ReflectionSupport.findMethod(AwattarBridgeHandler.class, "needRefresh", "").get();

        boolean result = (boolean) ReflectionSupport.invokeMethod(method, bridgeHandler);

        assertThat(result, is(true));
    }
}
