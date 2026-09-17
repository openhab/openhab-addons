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
package org.openhab.binding.amazonechocontrol.internal.handler;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.CHANNEL_LAST_VOICE_COMMAND;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.CHANNEL_REFRESH_ACTIVITY;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.DEVICE_PROPERTY_SERIAL_NUMBER;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.THING_TYPE_ACCOUNT;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.THING_TYPE_ECHO;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlCommandDescriptionProvider;
import org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlStateDescriptionProvider;
import org.openhab.binding.amazonechocontrol.internal.ConnectionException;
import org.openhab.binding.amazonechocontrol.internal.connection.Connection;
import org.openhab.binding.amazonechocontrol.internal.connection.LoginData;
import org.openhab.binding.amazonechocontrol.internal.dto.response.CustomerHistoryRecordTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.CustomerHistoryRecordVoiceTO;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

import com.google.gson.Gson;

/**
 * The {@link AccountHandlerActivityDispatchTest} contains tests for the delivery of voice history records to the
 * echo handlers: the startup guard on the push path, the history an explicit refresh delivers, the state the refresh
 * switch shows while a request runs, and the optional polling for accounts without push.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
public class AccountHandlerActivityDispatchTest {
    private static final ThingUID ACCOUNT_UID = new ThingUID(THING_TYPE_ACCOUNT, "account1");
    private static final ThingUID ECHO_UID = new ThingUID(THING_TYPE_ECHO, "account1", "echo1");
    private static final String ECHO_SERIAL = "SERIAL_ECHO_1";
    private static final ChannelUID REFRESH_CHANNEL = new ChannelUID(ACCOUNT_UID, CHANNEL_REFRESH_ACTIVITY);
    private static final ChannelUID LAST_VOICE_CHANNEL = new ChannelUID(ECHO_UID, CHANNEL_LAST_VOICE_COMMAND);
    private static final long ONE_HOUR_BEFORE_START = System.currentTimeMillis() - 3_600_000;
    private static final long ONE_POLL_MILLIS = 1_000;

    private @NonNullByDefault({}) AccountHandler accountHandler;
    private @NonNullByDefault({}) EchoHandler echoHandler;
    private @NonNullByDefault({}) Bridge bridge;
    private @NonNullByDefault({}) Thing echoThing;
    private @NonNullByDefault({}) Connection connection;
    private @NonNullByDefault({}) ThingHandlerCallback accountCallback;
    private @NonNullByDefault({}) ThingHandlerCallback echoCallback;

    @BeforeEach
    public void setUp() throws ConnectionException {
        bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(ACCOUNT_UID);
        when(bridge.getConfiguration()).thenReturn(new Configuration());

        @SuppressWarnings("unchecked")
        Storage<String> storage = (Storage<String>) mock(Storage.class);
        accountHandler = new AccountHandler(bridge, storage, new Gson(), mock(HttpClient.class),
                mock(HTTP2Client.class), mock(AmazonEchoControlCommandDescriptionProvider.class));
        accountCallback = mock(ThingHandlerCallback.class);
        accountHandler.setCallback(accountCallback);
        when(bridge.getHandler()).thenReturn(accountHandler);

        connection = mock(Connection.class);
        when(connection.isLoggedIn()).thenReturn(true);
        LoginData loginData = mock(LoginData.class);
        when(loginData.serializeLoginData()).thenReturn("");
        when(connection.getLoginData()).thenReturn(loginData);
        when(connection.getActivities(anyLong(), anyLong())).thenReturn(List.of());
        accountHandler.setConnection(connection);

        echoThing = mock(Thing.class);
        when(echoThing.getUID()).thenReturn(ECHO_UID);
        when(echoThing.getBridgeUID()).thenReturn(ACCOUNT_UID);
        when(echoThing.getConfiguration())
                .thenReturn(new Configuration(Map.of(DEVICE_PROPERTY_SERIAL_NUMBER, ECHO_SERIAL)));

        echoCallback = mock(ThingHandlerCallback.class);
        when(echoCallback.getBridge(ACCOUNT_UID)).thenReturn(bridge);
        echoHandler = new EchoHandler(echoThing, new Gson(), mock(AmazonEchoControlStateDescriptionProvider.class));
        echoHandler.setCallback(echoCallback);
        echoHandler.initialize();
        accountHandler.childHandlerInitialized(echoHandler, echoThing);
    }

    @AfterEach
    public void tearDown() {
        accountHandler.dispose();
    }

    private void initializeWith(Map<String, Object> configuration) {
        when(bridge.getConfiguration()).thenReturn(new Configuration(configuration));
        accountHandler.initialize();
        accountHandler.setConnection(connection);
        clearInvocations(connection);
    }

    private static CustomerHistoryRecordTO record(String serialNumber, long timestamp, String transcript) {
        CustomerHistoryRecordVoiceTO voiceItem = new CustomerHistoryRecordVoiceTO();
        voiceItem.recordItemType = "CUSTOMER_TRANSCRIPT";
        voiceItem.transcriptText = transcript;
        CustomerHistoryRecordTO record = new CustomerHistoryRecordTO();
        record.recordKey = "CUSTOMER#VOICE#x#" + serialNumber;
        record.timestamp = timestamp;
        record.utteranceType = "GENERAL";
        record.voiceHistoryRecordItems = List.of(voiceItem);
        return record;
    }

    @Test
    public void testPushActivityDropsRecordsFromBeforeTheHandlerStarted() {
        echoHandler.handlePushActivity(record(ECHO_SERIAL, ONE_HOUR_BEFORE_START, "turn on the light"));

        verify(echoCallback, never()).stateUpdated(eq(LAST_VOICE_CHANNEL), any());
    }

    @Test
    public void testRequestedActivityDeliversRecordsFromBeforeTheHandlerStarted() {
        echoHandler.handleRequestedActivity(record(ECHO_SERIAL, ONE_HOUR_BEFORE_START, "turn on the light"));

        verify(echoCallback).stateUpdated(LAST_VOICE_CHANNEL, new StringType("turn on the light"));
    }

    @Test
    public void testPushActivityNeverGoesBackwards() {
        long afterStart = System.currentTimeMillis() + 5_000;
        echoHandler.handlePushActivity(record(ECHO_SERIAL, afterStart, "the newer command"));
        echoHandler.handlePushActivity(record(ECHO_SERIAL, afterStart - 1_000, "the older command"));

        verify(echoCallback, never()).stateUpdated(eq(LAST_VOICE_CHANNEL), eq(new StringType("the older command")));
    }

    @Test
    public void testRefreshSwitchDeliversStoredHistoryToTheMatchingEcho() throws ConnectionException {
        when(connection.getActivities(anyLong(), anyLong()))
                .thenReturn(List.of(record("SERIAL_OF_NOBODY", ONE_HOUR_BEFORE_START, "for a stranger"),
                        record(ECHO_SERIAL, ONE_HOUR_BEFORE_START + 1, "for this echo")));

        accountHandler.handleCommand(REFRESH_CHANNEL, OnOffType.ON);

        verify(echoCallback).stateUpdated(LAST_VOICE_CHANNEL, new StringType("for this echo"));
        verify(echoCallback, never()).stateUpdated(eq(LAST_VOICE_CHANNEL), eq(new StringType("for a stranger")));
    }

    @Test
    public void testRefreshSwitchDeliversOnlyTheNewestRecordOfADevice() throws ConnectionException {
        when(connection.getActivities(anyLong(), anyLong()))
                .thenReturn(List.of(record(ECHO_SERIAL, ONE_HOUR_BEFORE_START + 1_000, "the newest command"),
                        record(ECHO_SERIAL, ONE_HOUR_BEFORE_START, "the older command")));

        accountHandler.handleCommand(REFRESH_CHANNEL, OnOffType.ON);

        verify(echoCallback).stateUpdated(LAST_VOICE_CHANNEL, new StringType("the newest command"));
        verify(echoCallback, never()).stateUpdated(eq(LAST_VOICE_CHANNEL), eq(new StringType("the older command")));
    }

    @Test
    public void testRefreshSwitchShowsOnWhileTheRequestRunsAndOffAfterwards() throws ConnectionException {
        accountHandler.handleCommand(REFRESH_CHANNEL, OnOffType.ON);

        InOrder order = inOrder(accountCallback, connection);
        order.verify(accountCallback).stateUpdated(REFRESH_CHANNEL, OnOffType.ON);
        order.verify(connection).getActivities(anyLong(), anyLong());
        order.verify(accountCallback).stateUpdated(REFRESH_CHANNEL, OnOffType.OFF);
    }

    @Test
    public void testOffCommandAnswersOffWithoutARequest() throws ConnectionException {
        accountHandler.handleCommand(REFRESH_CHANNEL, OnOffType.OFF);

        verify(accountCallback).stateUpdated(REFRESH_CHANNEL, OnOffType.OFF);
        verify(connection, never()).getActivities(anyLong(), anyLong());
    }

    @Test
    public void testActivityRequestUsesTheConfiguredWindow() throws ConnectionException {
        initializeWith(Map.of("activityRequestWindow", 300));
        long windowMillis = 300_000;
        long futureCushionMillis = 30_000;

        long before = System.currentTimeMillis();
        accountHandler.handleCommand(REFRESH_CHANNEL, OnOffType.ON);
        long after = System.currentTimeMillis();

        ArgumentCaptor<Long> start = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> end = ArgumentCaptor.forClass(Long.class);
        verify(connection).getActivities(start.capture(), end.capture());
        assertTrue(start.getValue() >= before - windowMillis && start.getValue() <= after - windowMillis,
                "start is the request time minus the configured window");
        assertTrue(end.getValue() >= before + futureCushionMillis && end.getValue() <= after + futureCushionMillis,
                "end is the request time plus the cushion for clock drift");
    }

    @Test
    public void testAConfiguredIntervalPollsTheVoiceHistory() throws ConnectionException {
        initializeWith(Map.of("activityPollingInterval", 1));

        verify(connection, timeout(5 * ONE_POLL_MILLIS).atLeastOnce()).getActivities(anyLong(), anyLong());
    }

    @Test
    public void testWithoutAnIntervalNothingIsPolled() throws ConnectionException {
        initializeWith(Map.of());

        verify(connection, after(2 * ONE_POLL_MILLIS).never()).getActivities(anyLong(), anyLong());
    }

    @Test
    public void testDisposeStopsThePolling() throws ConnectionException {
        initializeWith(Map.of("activityPollingInterval", 1));
        verify(connection, timeout(5 * ONE_POLL_MILLIS).atLeastOnce()).getActivities(anyLong(), anyLong());

        accountHandler.dispose();
        clearInvocations(connection);

        verify(connection, after(3 * ONE_POLL_MILLIS).never()).getActivities(anyLong(), anyLong());
    }

    @Test
    public void testRequestedActivityNeverGoesBackBehindADeliveredPushRecord() {
        long afterStart = System.currentTimeMillis() + 5_000;
        echoHandler.handlePushActivity(record(ECHO_SERIAL, afterStart, "the newer command"));
        echoHandler.handleRequestedActivity(record(ECHO_SERIAL, afterStart - 1_000, "the older command"));

        verify(echoCallback, never()).stateUpdated(eq(LAST_VOICE_CHANNEL), eq(new StringType("the older command")));
    }

    @Test
    public void testActivityRecordsFetchedAcrossAReinitializationAreDiscarded() throws ConnectionException {
        when(connection.getActivities(anyLong(), anyLong())).thenAnswer(invocation -> {
            accountHandler.initialize();
            return List.of(record(ECHO_SERIAL, System.currentTimeMillis() + 5_000, "from the old lifecycle"));
        });

        accountHandler.handleCommand(REFRESH_CHANNEL, OnOffType.ON);

        verify(echoCallback, never()).stateUpdated(eq(LAST_VOICE_CHANNEL), any());
    }

    @Test
    public void testAMalformedRecordDoesNotStopTheWellFormedOnes() throws ConnectionException {
        CustomerHistoryRecordTO malformed = record(ECHO_SERIAL, ONE_HOUR_BEFORE_START + 1, "unreachable");
        malformed.recordKey = null;
        when(connection.getActivities(anyLong(), anyLong()))
                .thenReturn(List.of(malformed, record(ECHO_SERIAL, ONE_HOUR_BEFORE_START + 2, "the good one")));

        accountHandler.handleCommand(REFRESH_CHANNEL, OnOffType.ON);

        verify(echoCallback).stateUpdated(LAST_VOICE_CHANNEL, new StringType("the good one"));
    }

    @Test
    public void testPollingSurvivesAFailedRequest() throws ConnectionException {
        when(connection.getActivities(anyLong(), anyLong())).thenThrow(new ConnectionException("session expired"))
                .thenReturn(List.of());
        initializeWith(Map.of("activityPollingInterval", 1));

        verify(connection, timeout(8 * ONE_POLL_MILLIS).atLeast(2)).getActivities(anyLong(), anyLong());
    }

    @Test
    public void testFailedPollsDoubleTheIntervalUpToTheHourlyRefresh() {
        assertTrue(AccountHandler.failedPollTicksToSkip(1, 60) == 1, "the first failure skips one tick");
        assertTrue(AccountHandler.failedPollTicksToSkip(2, 60) == 3, "the second failure skips three ticks");
        assertTrue(AccountHandler.failedPollTicksToSkip(12, 60) == 59, "the skip is capped at the hourly refresh");
        assertTrue(AccountHandler.failedPollTicksToSkip(3, 3600) == 0, "an hourly poll never skips");
    }
}
