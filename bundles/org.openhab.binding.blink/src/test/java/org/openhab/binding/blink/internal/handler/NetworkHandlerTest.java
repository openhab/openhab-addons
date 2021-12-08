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
package org.openhab.binding.blink.internal.handler;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.blink.internal.BlinkTestUtil;
import org.openhab.binding.blink.internal.dto.BlinkAccount;
import org.openhab.binding.blink.internal.service.NetworkService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.internal.ThingImpl;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

import com.google.gson.Gson;

/**
 * Test class.
 *
 * @author Matthias Oesterheld - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
@NonNullByDefault
class NetworkHandlerTest {

    private static final long NETWORK_ID = 123L;
    private static final ThingTypeUID THING_TYPE_UID = new ThingTypeUID("blink", "network");
    @NonNullByDefault({})
    NetworkHandler networkHandler;

    @Spy
    @NonNullByDefault({})
    Thing thing = new ThingImpl(THING_TYPE_UID, Long.toString(NETWORK_ID));
    @Mock
    @NonNullByDefault({})
    ThingHandlerCallback callback;
    @Mock
    @NonNullByDefault({})
    Bridge account;
    @Mock
    @NonNullByDefault({})
    AccountHandler accountHandler;
    @Mock
    @NonNullByDefault({})
    NetworkService networkService;

    @NonNullByDefault({})
    @Mock
    HttpClientFactory httpClientFactory;
    @NonNullByDefault({})
    Gson gson = new Gson();

    private final Configuration config = new Configuration();

    @BeforeEach
    void setup() throws IOException {
        config.put("networkId", NETWORK_ID);
        when(thing.getConfiguration()).thenReturn(config);
        when(httpClientFactory.getCommonHttpClient()).thenReturn(new HttpClient());
        doReturn(accountHandler).when(account).getHandler();
        when(accountHandler.getNetworkArmed(anyString(), eq(false))).thenReturn(OnOffType.ON);
        doReturn(BlinkTestUtil.testBlinkAccount()).when(accountHandler).getBlinkAccount();
        // noinspection ConstantConditions
        networkHandler = new NetworkHandler(thing, httpClientFactory, gson) {
            @Override
            public @Nullable Bridge getBridge() {
                return account;
            }
        };
        verify(httpClientFactory).getCommonHttpClient();
        networkHandler.setCallback(callback);
        when(networkService.arm(any(), anyString(), anyBoolean())).thenReturn(456L);
        networkHandler.networkService = networkService;
    }

    @Test
    void testInitialize() {
        networkHandler.initialize();
        assertThat(networkHandler.config, is(notNullValue()));
        assertThat(networkHandler.config.networkId, is(NETWORK_ID));
        ArgumentCaptor<ThingStatusInfo> statusCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback).statusUpdated(eq(thing), statusCaptor.capture());
        assertThat(statusCaptor.getValue().getStatus(), is(ThingStatus.ONLINE));
    }

    @Test
    void testSetOfflineOnMissingBridge() {
        networkHandler = new NetworkHandler(thing, httpClientFactory, gson) {
            @Override
            public @Nullable Bridge getBridge() {
                return null;
            }
        };
        networkHandler.initialize();
        networkHandler.setCallback(callback);
        ChannelUID testedChannel = new ChannelUID(new ThingUID(THING_TYPE_UID, Long.toString(NETWORK_ID)), "armed");
        networkHandler.handleCommand(testedChannel, RefreshType.REFRESH);
        ArgumentCaptor<ThingStatusInfo> statusCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback).statusUpdated(eq(thing), statusCaptor.capture());
        assertThat(statusCaptor.getValue().getStatus(), is(ThingStatus.OFFLINE));
    }

    @Test
    void testHandleArmCommandRefresh() throws IOException {
        networkHandler.initialize();
        ChannelUID testedChannel = new ChannelUID(new ThingUID(THING_TYPE_UID, Long.toString(NETWORK_ID)), "armed");
        networkHandler.handleCommand(testedChannel, RefreshType.REFRESH);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(callback).stateUpdated(eq(testedChannel), stateCaptor.capture());
        verify(accountHandler).getNetworkArmed(eq(Long.toString(networkHandler.config.networkId)), eq(false));
        assertThat(stateCaptor.getValue(), is(OnOffType.ON));
    }

    @Test
    void testHandleArmCommandOn() throws IOException {
        networkHandler.initialize();
        ChannelUID testedChannel = new ChannelUID(new ThingUID(THING_TYPE_UID, Long.toString(NETWORK_ID)), "armed");
        networkHandler.handleCommand(testedChannel, OnOffType.ON);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(networkService).arm(accountHandler.getBlinkAccount(), Long.toString(NETWORK_ID), true);
        verify(callback).stateUpdated(eq(testedChannel), stateCaptor.capture());
        assertThat(stateCaptor.getValue(), is(OnOffType.ON));
    }

    @Test
    void testHandleArmCommandOff() throws IOException {
        networkHandler.initialize();
        ChannelUID testedChannel = new ChannelUID(new ThingUID(THING_TYPE_UID, Long.toString(NETWORK_ID)), "armed");
        networkHandler.handleCommand(testedChannel, OnOffType.OFF);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(networkService).arm(accountHandler.getBlinkAccount(), Long.toString(NETWORK_ID), false);
        verify(callback).stateUpdated(eq(testedChannel), stateCaptor.capture());
        assertThat(stateCaptor.getValue(), is(OnOffType.OFF));
    }

    @Test
    void testSetOfflineOnException() throws IOException {
        networkHandler.initialize();
        when(networkService.arm(any(BlinkAccount.class), anyString(), anyBoolean())).thenThrow(IOException.class);
        networkHandler.networkService = networkService;
        ChannelUID testedChannel = new ChannelUID(new ThingUID(THING_TYPE_UID, Long.toString(NETWORK_ID)), "armed");
        networkHandler.handleCommand(testedChannel, OnOffType.OFF);
        ArgumentCaptor<ThingStatusInfo> statusCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(networkService).arm(accountHandler.getBlinkAccount(), Long.toString(NETWORK_ID), false);
        verify(callback, atLeast(1)).statusUpdated(eq(thing), statusCaptor.capture());
        assertThat(statusCaptor.getValue().getStatus(), is(ThingStatus.OFFLINE));
    }
}
