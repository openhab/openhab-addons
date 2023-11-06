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
package org.openhab.binding.groupepsa.internal.things;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZoneId;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpContentResponse;
import org.eclipse.jetty.client.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.groupepsa.internal.bridge.GroupePSABridgeHandler;
import org.openhab.binding.groupepsa.internal.rest.api.GroupePSAConnectApi;
import org.openhab.binding.groupepsa.internal.rest.exceptions.GroupePSACommunicationException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;

/**
 * The {@link GroupePSAHandlerTest} is responsible for testing the binding
 *
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
public class GroupePSAHandlerTest {
    private @NonNullByDefault({}) AutoCloseable closeable;

    private @NonNullByDefault({}) GroupePSAConnectApi api;
    private @NonNullByDefault({}) GroupePSABridgeHandler bridgeHandler;
    private @NonNullByDefault({}) GroupePSAHandler thingHandler;

    private @NonNullByDefault({}) @Mock ThingHandlerCallback thingCallback;
    private @NonNullByDefault({}) @Mock ThingHandlerCallback bridgeCallback;
    private @NonNullByDefault({}) @Mock Thing thing;
    private @NonNullByDefault({}) @Mock Bridge bridge;

    private @NonNullByDefault({}) @Mock OAuthFactory oAuthFactory;
    private @NonNullByDefault({}) @Mock HttpClient httpClient;
    private @NonNullByDefault({}) @Mock TimeZoneProvider timeZoneProvider;

    static String getResourceFileAsString(String fileName) throws GroupePSACommunicationException {
        try (InputStream is = GroupePSAConnectApi.class.getResourceAsStream(fileName)) {
            try (InputStreamReader isr = new InputStreamReader(is); BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        } catch (Exception e) {
            throw new GroupePSACommunicationException(e);
        }
    }

    static HttpContentResponse createHttpResponse(String file) throws GroupePSACommunicationException {
        return new HttpContentResponse(new HttpResponse(null, null).status(200),
                getResourceFileAsString("/" + file).getBytes(), "json", "UTF-8");
    }

    @BeforeEach
    public void setUp() throws GroupePSACommunicationException {
        closeable = MockitoAnnotations.openMocks(this);

        // Create real objects
        bridgeHandler = spy(new GroupePSABridgeHandler(bridge, oAuthFactory, httpClient));
        thingHandler = spy(new GroupePSAHandler(thing, timeZoneProvider));
        api = spy(new GroupePSAConnectApi(httpClient, bridgeHandler, "clientId", "realm"));

        // Setup API mock
        doReturn(createHttpResponse("dummy_user.json")).when(api).executeRequest(contains("user"), anyString());
        doReturn(createHttpResponse("dummy_vehiclestatus3.json")).when(api).executeRequest(contains("status"),
                anyString());

        // Setup bridge handler mock
        bridgeHandler.setCallback(bridgeCallback);
        doReturn(api).when(bridgeHandler).getAPI();

        // Setup bridge mock
        Configuration bridgeConfig = new Configuration();
        bridgeConfig.put("vendor", "OPEL");
        bridgeConfig.put("userName", "user");
        bridgeConfig.put("password", "pwd");
        bridgeConfig.put("clientId", "clientIdValue");
        bridgeConfig.put("clientSecret", "clientSecretValue");
        doReturn(bridgeConfig).when(bridge).getConfiguration();
        doReturn(ThingStatus.ONLINE).when(bridge).getStatus();
        doReturn(bridgeHandler).when(bridge).getHandler();
        doReturn(new ThingUID("a:b:c")).when(bridge).getUID();

        // Setup thing mock
        Configuration thingConfig = new Configuration();
        thingConfig.put("id", "mock_id");
        doReturn(thingConfig).when(thing).getConfiguration();
        doReturn(new ThingUID("a:b:c")).when(thing).getUID();

        // Setup thing handler mock
        thingHandler.setCallback(thingCallback);
        doReturn(bridge).when(thingHandler).getBridge();
        doNothing().when(thingHandler).buildDoorChannels(any());
        doReturn(ZoneId.systemDefault()).when(timeZoneProvider).getTimeZone();
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Free any resources, like open database connections, files etc.
        thingHandler.dispose();
        bridgeHandler.dispose();
        closeable.close();
    }

    @Test
    public void intializeAndCheckChannels() throws InterruptedException {
        // Initialize the bridge
        bridgeHandler.initialize();

        // check that the bridge is online
        verify(bridgeCallback, timeout(10000)).statusUpdated(eq(bridge),
                argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

        // Initialize the thing
        thingHandler.initialize();

        // check that the thing is offline without detail (last update time is not
        // within 15 minutes)
        verify(thingCallback, timeout(10000)).statusUpdated(eq(thing),
                argThat(arg -> arg.getStatus().equals(ThingStatus.OFFLINE)
                        && arg.getStatusDetail().equals(ThingStatusDetail.NONE)));

        // check that the channels are updated
        verify(thingCallback, atLeast(30)).stateUpdated(any(ChannelUID.class), any(State.class));
        verify(thingCallback).stateUpdated(eq(new ChannelUID("a:b:c:electric#chargingStatus")),
                eq(new StringType("Disconnected")));
        verify(thingCallback).stateUpdated(eq(new ChannelUID("a:b:c:various#lastUpdated")), any(DateTimeType.class));
    }
}
