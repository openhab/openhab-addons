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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.CHANNEL_NEXT_ALARM;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.CHANNEL_NEXT_MUSIC_ALARM;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.CHANNEL_NEXT_REMINDER;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.CHANNEL_NEXT_TIMER;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.CHANNEL_VOLUME;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.DEVICE_PROPERTY_SERIAL_NUMBER;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.THING_TYPE_ACCOUNT;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.THING_TYPE_ECHO;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlCommandDescriptionProvider;
import org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlStateDescriptionProvider;
import org.openhab.binding.amazonechocontrol.internal.ConnectionException;
import org.openhab.binding.amazonechocontrol.internal.connection.Connection;
import org.openhab.binding.amazonechocontrol.internal.connection.LoginData;
import org.openhab.binding.amazonechocontrol.internal.dto.push.PushCommandTO;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

import com.google.gson.Gson;

/**
 * The {@link AccountHandlerNotificationGateTest} contains tests for the link gate in front of the
 * notification poll of the {@link AccountHandler}.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
public class AccountHandlerNotificationGateTest {
    private static final ThingUID ACCOUNT_UID = new ThingUID(THING_TYPE_ACCOUNT, "account1");
    private static final ThingUID ECHO_UID = new ThingUID(THING_TYPE_ECHO, "account1", "echo1");
    private static final String ECHO_SERIAL = "SERIAL_ECHO_1";

    private @NonNullByDefault({}) AccountHandler accountHandler;
    private @NonNullByDefault({}) EchoHandler echoHandler;
    private @NonNullByDefault({}) Thing echoThing;
    private @NonNullByDefault({}) Connection connection;
    private @NonNullByDefault({}) ThingHandlerCallback echoCallback;

    @BeforeEach
    public void setUp() throws ConnectionException {
        Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(ACCOUNT_UID);

        @SuppressWarnings("unchecked")
        Storage<String> storage = (Storage<String>) mock(Storage.class);
        accountHandler = new AccountHandler(bridge, storage, new Gson(), mock(HttpClient.class),
                mock(HTTP2Client.class), mock(AmazonEchoControlCommandDescriptionProvider.class));
        accountHandler.setCallback(mock(ThingHandlerCallback.class));
        when(bridge.getHandler()).thenReturn(accountHandler);

        connection = mock(Connection.class);
        when(connection.isLoggedIn()).thenReturn(true);
        LoginData loginData = mock(LoginData.class);
        when(loginData.serializeLoginData()).thenReturn("");
        when(connection.getLoginData()).thenReturn(loginData);
        when(connection.getNotifications()).thenReturn(List.of());
        accountHandler.setConnection(connection);

        echoThing = mock(Thing.class);
        when(echoThing.getUID()).thenReturn(ECHO_UID);
        when(echoThing.getBridgeUID()).thenReturn(ACCOUNT_UID);
        when(echoThing.getConfiguration())
                .thenReturn(new Configuration(Map.of(DEVICE_PROPERTY_SERIAL_NUMBER, ECHO_SERIAL)));

        echoCallback = mock(ThingHandlerCallback.class);
        when(echoCallback.getBridge(ACCOUNT_UID)).thenReturn(bridge);
        echoHandler = spy(
                new EchoHandler(echoThing, new Gson(), mock(AmazonEchoControlStateDescriptionProvider.class)));
        echoHandler.setCallback(echoCallback);
        echoHandler.initialize();
    }

    private void registerEchoHandler() {
        accountHandler.childHandlerInitialized(echoHandler, echoThing);
    }

    private void linkChannel(String channelId) {
        when(echoCallback.isChannelLinked(new ChannelUID(ECHO_UID, channelId))).thenReturn(true);
    }

    private void pushNotificationChange() {
        PushCommandTO pushCommand = new PushCommandTO();
        pushCommand.command = "PUSH_NOTIFICATION_CHANGE";
        pushCommand.payload = "";
        accountHandler.onPushCommandReceived(pushCommand);
    }

    @Test
    public void testNoRequestWhileNoNotificationChannelIsLinked() throws ConnectionException {
        registerEchoHandler();

        accountHandler.refreshNotifications();
        accountHandler.refreshNotifications();

        verify(connection, never()).getNotifications();
    }

    @Test
    public void testNoRequestWhileOnlyOtherChannelsAreLinked() throws ConnectionException {
        registerEchoHandler();
        linkChannel(CHANNEL_VOLUME);

        accountHandler.refreshNotifications();

        verify(connection, never()).getNotifications();
    }

    @Test
    public void testPollsWhileANotificationChannelIsLinked() throws ConnectionException {
        registerEchoHandler();
        linkChannel(CHANNEL_NEXT_TIMER);

        accountHandler.refreshNotifications();

        verify(connection, times(1)).getNotifications();
        verify(echoHandler, times(1)).updateNotifications(any());
    }

    @Test
    public void testEachNotificationChannelOpensTheGate() throws ConnectionException {
        registerEchoHandler();
        int expectedPolls = 0;
        for (String channelId : List.of(CHANNEL_NEXT_ALARM, CHANNEL_NEXT_MUSIC_ALARM, CHANNEL_NEXT_REMINDER,
                CHANNEL_NEXT_TIMER)) {
            when(echoCallback.isChannelLinked(any(ChannelUID.class))).thenReturn(false);
            linkChannel(channelId);

            accountHandler.refreshNotifications();

            verify(connection, times(++expectedPolls)).getNotifications();
        }
    }

    @Test
    public void testPushRefreshIsGatedWhileUnlinked() throws ConnectionException {
        registerEchoHandler();

        pushNotificationChange();

        verify(connection, never()).getNotifications();
    }

    @Test
    public void testPushRefreshPollsWhileLinked() throws ConnectionException {
        registerEchoHandler();
        linkChannel(CHANNEL_NEXT_ALARM);

        pushNotificationChange();

        verify(connection, times(1)).getNotifications();
    }

    @Test
    public void testChannelLinkWhileSuspendedWakesTheNextTick() throws ConnectionException {
        registerEchoHandler();
        linkChannel(CHANNEL_NEXT_ALARM);
        accountHandler.refreshNotifications();
        verify(connection, times(1)).getNotifications();

        when(echoCallback.isChannelLinked(any(ChannelUID.class))).thenReturn(false);
        accountHandler.refreshNotifications();
        accountHandler.refreshNotificationsIfDue(System.currentTimeMillis());
        verify(connection, times(1)).getNotifications();

        linkChannel(CHANNEL_NEXT_ALARM);
        echoHandler.channelLinked(new ChannelUID(ECHO_UID, CHANNEL_NEXT_ALARM));
        verify(connection, times(1)).getNotifications();
        accountHandler.refreshNotificationsIfDue(System.currentTimeMillis());
        verify(connection, times(2)).getNotifications();

        accountHandler.refreshNotificationsIfDue(System.currentTimeMillis());
        verify(connection, times(2)).getNotifications();
    }

    @Test
    public void testRelinkBeforeTheNextAttemptStillWakesTheTick() throws ConnectionException {
        registerEchoHandler();
        linkChannel(CHANNEL_NEXT_ALARM);
        accountHandler.refreshNotifications();
        verify(connection, times(1)).getNotifications();

        when(echoCallback.isChannelLinked(any(ChannelUID.class))).thenReturn(false);
        echoHandler.channelUnlinked(new ChannelUID(ECHO_UID, CHANNEL_NEXT_ALARM));
        linkChannel(CHANNEL_NEXT_ALARM);
        echoHandler.channelLinked(new ChannelUID(ECHO_UID, CHANNEL_NEXT_ALARM));

        accountHandler.refreshNotificationsIfDue(System.currentTimeMillis());
        verify(connection, times(2)).getNotifications();
    }

    @Test
    public void testChannelLinkBurstWakesExactlyOnePoll() throws ConnectionException {
        registerEchoHandler();
        accountHandler.refreshNotifications();
        linkChannel(CHANNEL_NEXT_ALARM);
        linkChannel(CHANNEL_NEXT_TIMER);

        echoHandler.channelLinked(new ChannelUID(ECHO_UID, CHANNEL_NEXT_ALARM));
        echoHandler.channelLinked(new ChannelUID(ECHO_UID, CHANNEL_NEXT_TIMER));
        verify(connection, never()).getNotifications();

        accountHandler.refreshNotificationsIfDue(System.currentTimeMillis());

        verify(connection, times(1)).getNotifications();
    }

    @Test
    public void testChannelLinkWhileActiveDoesNotWakeAPoll() throws ConnectionException {
        registerEchoHandler();
        linkChannel(CHANNEL_NEXT_ALARM);
        accountHandler.refreshNotifications();
        verify(connection, times(1)).getNotifications();

        echoHandler.channelLinked(new ChannelUID(ECHO_UID, CHANNEL_NEXT_ALARM));
        accountHandler.refreshNotificationsIfDue(System.currentTimeMillis());

        verify(connection, times(1)).getNotifications();
    }

    @Test
    public void testLinkingANonNotificationChannelDoesNotPoll() throws ConnectionException {
        registerEchoHandler();
        accountHandler.refreshNotifications();
        linkChannel(CHANNEL_VOLUME);

        echoHandler.channelLinked(new ChannelUID(ECHO_UID, CHANNEL_VOLUME));
        accountHandler.refreshNotificationsIfDue(System.currentTimeMillis());

        verify(connection, never()).getNotifications();
    }

    @Test
    public void testChannelLinkWakeRespectsTheBackoff() throws ConnectionException {
        registerEchoHandler();
        linkChannel(CHANNEL_NEXT_ALARM);
        when(connection.getNotifications()).thenThrow(new ConnectionException("HTTP 429"));
        accountHandler.refreshNotifications();
        verify(connection, times(1)).getNotifications();

        when(echoCallback.isChannelLinked(any(ChannelUID.class))).thenReturn(false);
        accountHandler.refreshNotifications();
        linkChannel(CHANNEL_NEXT_ALARM);
        echoHandler.channelLinked(new ChannelUID(ECHO_UID, CHANNEL_NEXT_ALARM));
        accountHandler.refreshNotificationsIfDue(System.currentTimeMillis());

        verify(connection, times(1)).getNotifications();
    }

    @Test
    public void testEchoHandlerRegisteredAfterAccountStartOpensTheGate() throws ConnectionException {
        accountHandler.refreshNotifications();
        verify(connection, never()).getNotifications();

        linkChannel(CHANNEL_NEXT_REMINDER);
        registerEchoHandler();
        accountHandler.refreshNotifications();

        verify(connection, times(1)).getNotifications();
    }

    @Test
    public void testDisposedEchoHandlerNoLongerHoldsTheGateOpen() throws ConnectionException {
        registerEchoHandler();
        linkChannel(CHANNEL_NEXT_ALARM);
        accountHandler.refreshNotifications();
        verify(connection, times(1)).getNotifications();

        accountHandler.childHandlerDisposed(echoHandler, echoThing);
        accountHandler.refreshNotifications();

        verify(connection, times(1)).getNotifications();
    }

    @Test
    public void testEchoHandlerWithoutCallbackCountsAsUnlinked() throws ConnectionException {
        registerEchoHandler();
        linkChannel(CHANNEL_NEXT_ALARM);
        echoHandler.setCallback(null);

        accountHandler.refreshNotifications();

        verify(connection, never()).getNotifications();
    }

    @Test
    public void testSkipsLeaveTheBackoffUntouched() throws ConnectionException {
        registerEchoHandler();
        linkChannel(CHANNEL_NEXT_ALARM);
        when(connection.getNotifications()).thenThrow(new ConnectionException("HTTP 429"));
        accountHandler.refreshNotifications();
        verify(connection, times(1)).getNotifications();

        when(echoCallback.isChannelLinked(any(ChannelUID.class))).thenReturn(false);
        accountHandler.refreshNotifications();
        accountHandler.refreshNotifications();

        linkChannel(CHANNEL_NEXT_ALARM);
        accountHandler.refreshNotifications();

        verify(connection, times(1)).getNotifications();
    }
}
