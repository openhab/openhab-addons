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
package org.openhab.binding.icloud.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.icloud.internal.FindMyIPhoneServiceManager;
import org.openhab.binding.icloud.internal.ICloudApiAuthenticationException;
import org.openhab.binding.icloud.internal.ICloudApiResponseException;
import org.openhab.binding.icloud.internal.ICloudDeviceInformationListener;
import org.openhab.binding.icloud.internal.ICloudService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

/**
 * Tests that {@link ICloudAccountBridgeHandler} keeps refreshing and recovers after communication failures.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
class ICloudAccountBridgeHandlerRefreshTest {

    private static final ThingTypeUID BRIDGE_TYPE = new ThingTypeUID("icloud", "account");
    private static final String DEVICE_DATA = """
            {"statusCode":"200","content":[{"deviceDiscoveryId":"device-1","name":"Phone","deviceStatus":"200",\
            "batteryLevel":0.5,"batteryStatus":"Charging"}]}
            """;
    private static final ThingStatusInfo ONLINE = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);

    private final ICloudService service = mock(ICloudService.class);
    private final FindMyIPhoneServiceManager devices = mock(FindMyIPhoneServiceManager.class);
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private final ICloudDeviceInformationListener listener = mock(ICloudDeviceInformationListener.class);
    @SuppressWarnings("unchecked")
    private final Storage<String> storage = mock(Storage.class);
    private final AtomicBoolean serverDown = new AtomicBoolean();
    private final Bridge bridge = BridgeBuilder.create(BRIDGE_TYPE, new ThingUID(BRIDGE_TYPE, "test"))
            .withConfiguration(new Configuration(Map.of("appleId", "user@example.com", "password", "secret"))).build();
    private final ICloudAccountBridgeHandler handler = new ICloudAccountBridgeHandler(bridge, storage) {
        @Override
        ICloudService createICloudService(String appleId, String password) {
            return service;
        }
    };

    @BeforeEach
    void setUp() throws Exception {
        when(service.getDevices()).thenReturn(devices);
        when(devices.refreshClient()).thenAnswer(invocation -> {
            if (serverDown.get()) {
                throw new ICloudApiResponseException("https://example.com/refreshClient", 503, "");
            }
            return DEVICE_DATA;
        });
        when(service.authenticate(false)).thenReturn(true);
        when(service.requires2fa()).thenReturn(false);
        when(service.isTrustedSession()).thenReturn(true);
        handler.setCallback(callback);
        handler.registerListener(listener);
    }

    @AfterEach
    void tearDown() {
        handler.dispose();
    }

    /** Initializes the handler and waits (bounded) until the async init has finished. */
    private void initAndWait() throws Exception {
        handler.initialize();
        ScheduledFuture<?> task = handler.initTask;
        assertNotNull(task);
        task.get(10, TimeUnit.SECONDS);
    }

    private List<ThingStatusInfo> statuses() {
        ArgumentCaptor<ThingStatusInfo> captor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, atLeastOnce()).statusUpdated(eq(bridge), captor.capture());
        return captor.getAllValues();
    }

    private ThingStatusInfo lastStatus() {
        List<ThingStatusInfo> all = statuses();
        return all.get(all.size() - 1);
    }

    private static ThingStatusInfo offline(ThingStatusDetail detail, String description) {
        return new ThingStatusInfo(ThingStatus.OFFLINE, detail, description);
    }

    @Test
    void serverErrorDuringRefreshIsReportedAndRecovers() throws Exception {
        serverDown.set(true);
        initAndWait();

        // An exception escaping here would cancel the periodic refresh job.
        assertDoesNotThrow(handler::refreshData);
        assertEquals(offline(ThingStatusDetail.COMMUNICATION_ERROR, "iCloud request failed with HTTP status 503"),
                lastStatus());
        verify(listener, never()).deviceInformationUpdate(anyList());

        serverDown.set(false);
        handler.refreshData();

        assertEquals(ONLINE, lastStatus());
        verify(listener, atLeastOnce()).deviceInformationUpdate(argThat(list -> list.size() == 1));
    }

    @Test
    void networkErrorDuringLoginIsCommunicationErrorAndRetried() throws Exception {
        when(service.authenticate(false)).thenThrow(new IOException("Network is unreachable")).thenReturn(true);
        initAndWait();

        assertEquals(offline(ThingStatusDetail.COMMUNICATION_ERROR, "Network is unreachable"), lastStatus());
        assertNotNull(handler.refreshJob, "login must be retried by the periodic refresh");

        handler.refreshData();

        assertEquals(ONLINE, lastStatus());
        verify(service, times(2)).authenticate(false);
        verify(listener).deviceInformationUpdate(anyList());
    }

    @Test
    void serverErrorDuringLoginIsCommunicationError() throws Exception {
        when(service.authenticate(false)).thenThrow(new ICloudApiResponseException("https://example.com", 503, ""));
        initAndWait();

        assertEquals(offline(ThingStatusDetail.COMMUNICATION_ERROR, "iCloud login failed with HTTP status 503"),
                lastStatus());
        assertNotNull(handler.refreshJob);
    }

    @Test
    void invalidCredentialsAreNotRetriedByRefresh() throws Exception {
        when(service.authenticate(false)).thenReturn(false);
        initAndWait();

        handler.refreshData();

        assertEquals(offline(ThingStatusDetail.CONFIGURATION_ERROR, "Invalid credentials."), lastStatus());
        verify(service, times(1)).authenticate(false);
    }

    @Test
    void rejectedLoginIsNotRetriedByRefresh() throws Exception {
        when(service.authenticate(false)).thenThrow(new ICloudApiResponseException("https://example.com", 401, ""));
        initAndWait();

        handler.refreshData();

        assertEquals(offline(ThingStatusDetail.CONFIGURATION_ERROR, "iCloud login rejected with HTTP status 401"),
                lastStatus());
        verify(service, times(1)).authenticate(false);
    }

    @Test
    void unusableSessionTriggersNewLogin() throws Exception {
        when(service.getDevices()).thenThrow(new IllegalStateException("Webservice URLs not set.")).thenReturn(devices);
        initAndWait();

        // The refresh job started after the login logs in again and then gets the device data.
        verify(callback, timeout(10_000)).statusUpdated(eq(bridge), eq(ONLINE));
        verify(service, times(2)).authenticate(false);
        assertTrue(statuses().stream().noneMatch(s -> s.getStatus() == ThingStatus.OFFLINE), statuses()::toString);
    }

    @Test
    void requestReportsBridgeStatusOnlyOnce() throws Exception {
        when(devices.refreshClient()).thenThrow(new ICloudApiAuthenticationException("https://example.com", 421, ""))
                .thenThrow(new ICloudApiResponseException("https://example.com", 503, ""));
        initAndWait();

        // The re-login after the 421 succeeds, the request then fails: no intermediate ONLINE is reported.
        ThingStatusInfo failed = offline(ThingStatusDetail.COMMUNICATION_ERROR,
                "iCloud request failed with HTTP status 503");
        verify(callback, timeout(10_000)).statusUpdated(eq(bridge), eq(failed));
        assertEquals(List.of(new ThingStatusInfo(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, null), failed),
                statuses());
        verify(service, times(2)).authenticate(false);
    }
}
