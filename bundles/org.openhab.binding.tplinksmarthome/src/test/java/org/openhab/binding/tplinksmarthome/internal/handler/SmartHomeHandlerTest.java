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
package org.openhab.binding.tplinksmarthome.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.tplinksmarthome.internal.ChannelUIDConstants.CHANNEL_UID_SWITCH;
import static org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeBindingConstants.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.tplinkbridge.TPLinkAccount;
import org.openhab.binding.tplinkbridge.TPLinkCredentials;
import org.openhab.binding.tplinkbridge.klap.KlapAuthenticationException;
import org.openhab.binding.tplinksmarthome.internal.ChannelUIDConstants;
import org.openhab.binding.tplinksmarthome.internal.Commands;
import org.openhab.binding.tplinksmarthome.internal.Connection;
import org.openhab.binding.tplinksmarthome.internal.KlapConnection;
import org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeConfiguration;
import org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeDiscoveryService;
import org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeThingType;
import org.openhab.binding.tplinksmarthome.internal.TPLinkStateDescriptionProvider;
import org.openhab.binding.tplinksmarthome.internal.UnsupportedProtocolException;
import org.openhab.binding.tplinksmarthome.internal.device.SmartHomeDevice;
import org.openhab.binding.tplinksmarthome.internal.model.ModelTestUtil;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 * Tests cases for {@link SmartHomeHandler} class.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 * @author Christopher Smith - TP-Link account and KLAP tests
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class SmartHomeHandlerTest {

    private @NonNullByDefault({}) SmartHomeHandler handler;

    private @Mock @NonNullByDefault({}) Connection connection;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;
    private @Mock @NonNullByDefault({}) Thing thing;
    private @Mock @NonNullByDefault({}) SmartHomeDevice smartHomeDevice;
    private @Mock @NonNullByDefault({}) TPLinkSmartHomeDiscoveryService discoveryService;
    private @Mock @NonNullByDefault({}) TPLinkStateDescriptionProvider stateDescriptionProvider;
    private @Mock @NonNullByDefault({}) HttpClient httpClient;
    private @Mock @NonNullByDefault({}) ThingRegistry thingRegistry;
    private @Mock @NonNullByDefault({}) Thing accountThing;

    private static final ThingUID ACCOUNT_UID = new ThingUID("tplinkbridge", "account", "home");

    private final Configuration configuration = new Configuration();

    @BeforeEach
    public void setUp() throws IOException {
        configuration.put(CONFIG_IP, "localhost");
        configuration.put(CONFIG_REFRESH, 1);
        when(thing.getConfiguration()).thenReturn(configuration);
        lenient().when(smartHomeDevice.getUpdateCommand()).thenReturn(Commands.getSysinfo());
        lenient().when(connection.sendCommand(Commands.getSysinfo()))
                .thenReturn(ModelTestUtil.readJson("plug_get_sysinfo_response"));
        handler = new SmartHomeHandler(thing, smartHomeDevice, TPLinkSmartHomeThingType.HS100, discoveryService,
                stateDescriptionProvider, httpClient, thingRegistry) {
            @Override
            Connection createConnection(TPLinkSmartHomeConfiguration config) {
                return connection;
            }
        };
        lenient().when(smartHomeDevice.handleCommand(eq(CHANNEL_UID_SWITCH), any())).thenReturn(true);
        lenient().when(callback.isChannelLinked(any())).thenReturn(true);
        handler.setCallback(callback);
    }

    @AfterEach
    public void after() {
        handler.dispose();
    }

    @Test
    public void testInitializeShouldCallTheCallback() throws InterruptedException {
        handler.initialize();
        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);

        verify(callback).statusUpdated(eq(thing), statusInfoCaptor.capture());
        ThingStatusInfo thingStatusInfo = statusInfoCaptor.getValue();
        assertEquals(ThingStatus.UNKNOWN, thingStatusInfo.getStatus(), "Device should be unknown");
    }

    @Test
    public void testHandleCommandRefreshType() {
        handler.initialize();
        assertHandleCommandRefreshType(-53);
    }

    @Test
    public void testHandleCommandRefreshTypeRangeExtender() throws IOException {
        when(connection.sendCommand(Commands.getSysinfo()))
                .thenReturn(ModelTestUtil.readJson("rangeextender_get_sysinfo_response"));
        handler.initialize();
        assertHandleCommandRefreshType(-70);
    }

    private void assertHandleCommandRefreshType(int expectedRssi) {
        handler.initialize();
        ChannelUID channelUID = ChannelUIDConstants.CHANNEL_UID_RSSI;
        handler.handleCommand(channelUID, RefreshType.REFRESH);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(callback).stateUpdated(eq(channelUID), stateCaptor.capture());
        assertEquals(new QuantityType<>(expectedRssi + " dBm"), stateCaptor.getValue(),
                "State of RSSI channel should be set");
    }

    @Test
    public void testHandleCommandOther() throws InterruptedException {
        handler.initialize();
        ChannelUID channelUID = ChannelUIDConstants.CHANNEL_UID_SWITCH;
        Mockito.doReturn(OnOffType.ON).when(smartHomeDevice).updateChannel(eq(channelUID), any());
        handler.handleCommand(channelUID, RefreshType.REFRESH);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(callback).stateUpdated(eq(channelUID), stateCaptor.capture());
        assertSame(OnOffType.ON, stateCaptor.getValue(), "State of channel switch should be set");
    }

    @Test
    public void testThingWithoutAccountUsesLegacyConnection() {
        assertSame(Connection.class, newHandler().createConnection(new TPLinkSmartHomeConfiguration()).getClass());
    }

    @Test
    public void testThingWithAccountUsesKlapConnection() {
        TPLinkSmartHomeConfiguration config = new TPLinkSmartHomeConfiguration();
        config.ipAddress = "localhost";
        config.account = ACCOUNT_UID.getAsString();

        assertInstanceOf(KlapConnection.class, newHandler().createConnection(config));
    }

    @Test
    public void testOfflineAccountReportsConfigurationErrorWithoutContactingTheDevice() throws IOException {
        useAccount(ThingStatus.OFFLINE);
        handler.initialize();

        handler.handleCommand(ChannelUIDConstants.CHANNEL_UID_RSSI, RefreshType.REFRESH);

        verify(connection, never()).sendCommand(any());
        assertLastStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Test
    public void testMissingAccountReportsConfigurationError() throws IOException {
        configuration.put(CONFIG_ACCOUNT, ACCOUNT_UID.getAsString());
        handler.initialize();

        handler.handleCommand(ChannelUIDConstants.CHANNEL_UID_RSSI, RefreshType.REFRESH);

        verify(connection, never()).sendCommand(any());
        assertLastStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Test
    public void testThingRecoversOnceTheAccountIsOnline() throws IOException {
        useAccount(ThingStatus.OFFLINE);
        handler.initialize();
        handler.handleCommand(ChannelUIDConstants.CHANNEL_UID_RSSI, RefreshType.REFRESH);
        assertLastStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);

        when(accountThing.getStatus()).thenReturn(ThingStatus.ONLINE);
        // A command forces a refresh, like the next poll does
        handler.handleCommand(CHANNEL_UID_SWITCH, OnOffType.ON);

        assertLastStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
    }

    @Test
    public void testInvalidAccountUidReportsConfigurationError() {
        configuration.put(CONFIG_ACCOUNT, "home");

        handler.initialize();

        assertLastStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Test
    public void testRejectedCredentialsReportConfigurationErrorWithoutRetries() throws IOException {
        when(connection.sendCommand(Commands.getSysinfo()))
                .thenThrow(new KlapAuthenticationException("The device rejected the TP-Link account credentials"));
        handler.initialize();

        handler.handleCommand(ChannelUIDConstants.CHANNEL_UID_RSSI, RefreshType.REFRESH);

        verify(connection, times(1)).sendCommand(Commands.getSysinfo());
        assertLastStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Test
    public void testRejectedCredentialsAreNotTriedAgainForEveryChannel() throws IOException {
        configuration.put(CONFIG_REFRESH, 60);
        when(connection.sendCommand(Commands.getSysinfo()))
                .thenThrow(new KlapAuthenticationException("The device rejected the TP-Link account credentials"));
        handler.initialize();

        handler.handleCommand(ChannelUIDConstants.CHANNEL_UID_RSSI, RefreshType.REFRESH);
        handler.handleCommand(CHANNEL_UID_SWITCH, RefreshType.REFRESH);
        handler.refreshChannels();

        verify(connection, times(1)).sendCommand(Commands.getSysinfo());
        assertLastStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Test
    public void testRefreshChannelsReadsTheDeviceOnceForAllChannels() throws IOException {
        when(thing.getChannels()).thenReturn(List.of(ChannelBuilder.create(CHANNEL_UID_SWITCH).build(),
                ChannelBuilder.create(ChannelUIDConstants.CHANNEL_UID_RSSI).build()));
        when(connection.sendCommand(Commands.getSysinfo())).thenThrow(new IOException("timeout"));
        handler.initialize();

        handler.refreshChannels();

        // One refresh with its retries, not one per channel
        verify(connection, times(5)).sendCommand(Commands.getSysinfo());
    }

    @Test
    public void testCommandWhileAccountIsOfflineKeepsTheAccountError() throws IOException {
        useAccount(ThingStatus.OFFLINE);
        handler.initialize();

        handler.handleCommand(CHANNEL_UID_SWITCH, OnOffType.ON);

        verify(smartHomeDevice, never()).handleCommand(any(), any());
        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, atLeastOnce()).statusUpdated(eq(thing), statusInfoCaptor.capture());
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, statusInfoCaptor.getValue().getStatusDetail());
        assertEquals("The TP-Link account 'tplinkbridge:account:home' doesn't exist or isn't ONLINE.",
                statusInfoCaptor.getValue().getDescription());
    }

    @Test
    public void testSmartProtocolReportsConfigurationErrorWithoutRetries() throws IOException {
        when(connection.sendCommand(Commands.getSysinfo()))
                .thenThrow(new UnsupportedProtocolException("The device uses TP-Link's newer SMART protocol"));
        handler.initialize();

        handler.handleCommand(ChannelUIDConstants.CHANNEL_UID_RSSI, RefreshType.REFRESH);

        verify(connection, times(1)).sendCommand(Commands.getSysinfo());
        assertLastStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Test
    public void testCommunicationErrorsAreRetried() throws IOException {
        when(connection.sendCommand(Commands.getSysinfo())).thenThrow(new IOException("timeout"));
        handler.initialize();

        handler.handleCommand(ChannelUIDConstants.CHANNEL_UID_RSSI, RefreshType.REFRESH);

        verify(connection, times(5)).sendCommand(Commands.getSysinfo());
        assertLastStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
    }

    @Test
    public void testAccountWithoutIpAddressReportsConfigurationErrorAndIgnoresCommands() throws IOException {
        configuration.remove(CONFIG_IP);
        configuration.put(CONFIG_DEVICE_ID, "8006588E50AD389303FF31AB6302907A17442F16");
        useAccount(ThingStatus.ONLINE);
        handler.initialize();

        handler.handleCommand(ChannelUIDConstants.CHANNEL_UID_RSSI, RefreshType.REFRESH);
        handler.handleCommand(CHANNEL_UID_SWITCH, OnOffType.ON);
        handler.refreshChannels();

        verify(smartHomeDevice, never()).handleCommand(any(), any());
        verify(connection, never()).sendCommand(any());
        assertLastStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Test
    public void testInterruptedRefreshIsNotRetriedAndKeepsStatus() throws IOException {
        when(connection.sendCommand(Commands.getSysinfo()))
                .thenThrow(new InterruptedIOException("Interrupted while waiting for the device"));
        handler.initialize();

        try {
            handler.handleCommand(ChannelUIDConstants.CHANNEL_UID_RSSI, RefreshType.REFRESH);

            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
        verify(connection, times(1)).sendCommand(Commands.getSysinfo());
        assertLastStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);
    }

    private void useAccount(ThingStatus status) {
        configuration.put(CONFIG_ACCOUNT, ACCOUNT_UID.getAsString());
        ThingHandler accountHandler = mock(ThingHandler.class, withSettings().extraInterfaces(TPLinkAccount.class));
        lenient().when(((TPLinkAccount) accountHandler).getCredentials())
                .thenReturn(new TPLinkCredentials("user@example.com", "secret"));
        lenient().when(accountThing.getHandler()).thenReturn(accountHandler);
        lenient().when(accountThing.getStatus()).thenReturn(status);
        lenient().when(thingRegistry.get(ACCOUNT_UID)).thenReturn(accountThing);
    }

    private SmartHomeHandler newHandler() {
        SmartHomeHandler realHandler = new SmartHomeHandler(thing, smartHomeDevice, TPLinkSmartHomeThingType.HS100,
                discoveryService, stateDescriptionProvider, httpClient, thingRegistry);
        realHandler.setCallback(callback);
        return realHandler;
    }

    private void assertLastStatus(ThingStatus status, ThingStatusDetail detail) {
        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, atLeastOnce()).statusUpdated(eq(thing), statusInfoCaptor.capture());
        ThingStatusInfo last = statusInfoCaptor.getValue();
        assertEquals(status, last.getStatus());
        assertEquals(detail, last.getStatusDetail());
    }

    @Test
    public void testRefreshChannels() {
        handler.initialize();
        handler.refreshChannels();
    }
}
