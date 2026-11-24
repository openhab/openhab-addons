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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.icloud.internal.FindMyIPhoneServiceManager;
import org.openhab.binding.icloud.internal.ICloudApiResponseException;
import org.openhab.binding.icloud.internal.ICloudService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.storage.Storage;
import org.openhab.core.test.storage.VolatileStorage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

/**
 * Tests the status reporting of {@link ICloudAccountBridgeHandler} during login and 2-FA handling.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
class ICloudAccountBridgeHandlerTest {

    private static final String PENDING = "Please provide 2-FA code in thing configuration.";
    private static final String INVALID = "Invalid 2-FA-code.";
    private static final ThingTypeUID BRIDGE_TYPE = new ThingTypeUID("icloud", "account");

    private ICloudService service = mock(ICloudService.class);
    private ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private Storage<String> storage = new VolatileStorage<>();
    private @Nullable ICloudAccountBridgeHandler handler;
    private Bridge bridge = BridgeBuilder.create(BRIDGE_TYPE, new ThingUID(BRIDGE_TYPE, "test")).build();

    @BeforeEach
    void setUp() throws Exception {
        // Keeps the scheduled refresh quiet: no device data is returned.
        FindMyIPhoneServiceManager devices = mock(FindMyIPhoneServiceManager.class);
        when(service.getDevices()).thenReturn(devices);
    }

    @AfterEach
    void tearDown() {
        ICloudAccountBridgeHandler local = handler;
        if (local != null) {
            local.dispose();
        }
    }

    private void createHandler(@Nullable String password, @Nullable String code) {
        Configuration config = new Configuration();
        config.put("appleId", "user@example.com");
        if (password != null) {
            config.put("password", password);
        }
        if (code != null) {
            config.put("code", code);
        }
        bridge = BridgeBuilder.create(BRIDGE_TYPE, new ThingUID(BRIDGE_TYPE, "test")).withConfiguration(config).build();
        ICloudAccountBridgeHandler h = new ICloudAccountBridgeHandler(bridge, storage) {
            @Override
            ICloudService createICloudService(String appleId, String password) {
                return service;
            }
        };
        h.setCallback(callback);
        handler = h;
    }

    /** Initializes the handler and waits (bounded) until the async init has finished. */
    private void initAndWait() throws InterruptedException, ExecutionException, TimeoutException {
        ICloudAccountBridgeHandler h = requireHandler();
        h.initialize();
        ScheduledFuture<?> task = h.initTask;
        assertNotNull(task);
        task.get(10, TimeUnit.SECONDS);
    }

    /**
     * Mimics the framework: a changed configuration replaces the thing, which disposes and re-initializes the handler.
     * Handler state (e.g. waiting for a code) survives, as in production.
     */
    private void initWithCode(String code) throws InterruptedException, ExecutionException, TimeoutException {
        ICloudAccountBridgeHandler h = requireHandler();
        Configuration config = new Configuration(bridge.getConfiguration().getProperties());
        config.put("code", code);
        bridge = BridgeBuilder.create(BRIDGE_TYPE, bridge.getUID()).withConfiguration(config).build();
        h.thingUpdated(bridge);
        ScheduledFuture<?> task = h.initTask;
        assertNotNull(task);
        task.get(10, TimeUnit.SECONDS);
    }

    private ICloudAccountBridgeHandler requireHandler() {
        ICloudAccountBridgeHandler h = handler;
        assertNotNull(h);
        return h;
    }

    private List<ThingStatusInfo> statuses() {
        ArgumentCaptor<ThingStatusInfo> captor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, atLeastOnce()).statusUpdated(eq(bridge), captor.capture());
        return captor.getAllValues();
    }

    private void assertStatuses(ThingStatusInfo... expected) {
        assertEquals(List.of(expected), statuses());
    }

    private static ThingStatusInfo offline(ThingStatusDetail detail, String description) {
        return new ThingStatusInfo(ThingStatus.OFFLINE, detail, description);
    }

    private static final ThingStatusInfo UNKNOWN = new ThingStatusInfo(ThingStatus.UNKNOWN, ThingStatusDetail.NONE,
            null);

    private void requireTwoFa() throws Exception {
        when(service.authenticate(false)).thenReturn(true);
        when(service.requires2fa()).thenReturn(true);
    }

    @Test
    void codeRequestedAtLogin() throws Exception {
        createHandler("secret", null);
        requireTwoFa();
        initAndWait();

        assertStatuses(UNKNOWN, offline(ThingStatusDetail.CONFIGURATION_ERROR, PENDING));
        verify(service, never()).validate2faCode(anyString());
    }

    @Test
    void reinitializeWhileWaitingDoesNotFlap() throws Exception {
        createHandler("secret", null);
        requireTwoFa();
        initAndWait();
        clearInvocations(callback);

        initAndWait();

        assertStatuses(UNKNOWN, offline(ThingStatusDetail.CONFIGURATION_ERROR, PENDING));
        verify(service, never()).validate2faCode(anyString());
    }

    @Test
    void blankCodeKeepsWaitingForCode() throws Exception {
        createHandler("secret", "   ");
        requireTwoFa();
        initAndWait();
        clearInvocations(callback);

        initAndWait();

        assertStatuses(UNKNOWN, offline(ThingStatusDetail.CONFIGURATION_ERROR, PENDING));
        verify(service, never()).validate2faCode(anyString());
    }

    @Test
    void invalidCodeIsReported() throws Exception {
        createHandler("secret", null);
        requireTwoFa();
        initAndWait();
        clearInvocations(callback);
        when(service.validate2faCode("111111")).thenReturn(false);

        initWithCode("111111");

        assertStatuses(UNKNOWN, offline(ThingStatusDetail.CONFIGURATION_ERROR, INVALID));
    }

    @Test
    void rejectedCodeIsNotSentAgain() throws Exception {
        createHandler("secret", null);
        requireTwoFa();
        initAndWait();
        when(service.validate2faCode("111111")).thenReturn(false);
        initWithCode("111111");
        // The rejected code stays in the configuration; the next login requests a fresh code.
        initAndWait();
        clearInvocations(callback);

        initAndWait();

        assertStatuses(UNKNOWN, offline(ThingStatusDetail.CONFIGURATION_ERROR, PENDING));
        verify(service, times(1)).validate2faCode("111111");
    }

    @Test
    void newCodeAfterRejectionBringsAccountOnline() throws Exception {
        createHandler("secret", null);
        requireTwoFa();
        initAndWait();
        when(service.validate2faCode("111111")).thenReturn(false);
        initWithCode("111111");
        // After a rejection Apple is asked for a fresh code during the next login.
        initAndWait();
        clearInvocations(callback);
        when(service.validate2faCode("222222")).thenReturn(true);

        initWithCode("222222");

        assertAccountOnlineAndCodeCleared();
    }

    @Test
    void validCodeBringsAccountOnline() throws Exception {
        createHandler("secret", null);
        requireTwoFa();
        initAndWait();
        clearInvocations(callback);
        when(service.validate2faCode("123456")).thenReturn(true);

        initWithCode("123456");

        assertAccountOnlineAndCodeCleared();
    }

    private void assertAccountOnlineAndCodeCleared() {
        List<ThingStatusInfo> all = statuses();
        assertEquals(UNKNOWN, all.get(0));
        // Order independent: the async refresh job may also report ONLINE.
        assertTrue(all.stream().skip(1)
                .allMatch(s -> s.getStatus() == ThingStatus.ONLINE && s.getStatusDetail() == ThingStatusDetail.NONE));
        assertTrue(all.size() > 1);
        assertNotNull(requireHandler().refreshJob);
        assertEquals("", requireHandler().getThing().getConfiguration().get("code"));
    }

    @Test
    void missingPasswordIsConfigurationError() throws Exception {
        createHandler(null, null);
        initAndWait();

        assertStatuses(UNKNOWN, offline(ThingStatusDetail.CONFIGURATION_ERROR, "Apple ID or password is not set!"));
    }

    @Test
    void invalidCredentialsAreConfigurationError() throws Exception {
        createHandler("secret", null);
        when(service.authenticate(false)).thenReturn(false);
        initAndWait();

        assertStatuses(UNKNOWN, offline(ThingStatusDetail.CONFIGURATION_ERROR, "Invalid credentials."));
    }

    @Test
    void sessionTrustFailureIsConfigurationError() throws Exception {
        createHandler("secret", null);
        when(service.authenticate(false)).thenReturn(true);
        when(service.requires2fa()).thenReturn(false);
        when(service.isTrustedSession()).thenReturn(false);
        when(service.trustSession()).thenReturn(false);
        initAndWait();

        assertStatuses(UNKNOWN, offline(ThingStatusDetail.CONFIGURATION_ERROR, "Session trust failed."));
    }

    @Test
    void unexpectedLoginExceptionIsConfigurationError() throws Exception {
        createHandler("secret", null);
        when(service.authenticate(false)).thenThrow(new IllegalArgumentException("boom"));
        initAndWait();

        assertStatuses(UNKNOWN, offline(ThingStatusDetail.CONFIGURATION_ERROR, "boom"));
    }

    @Test
    void failingCodeValidationIsConfigurationErrorAndRetried() throws Exception {
        createHandler("secret", null);
        requireTwoFa();
        initAndWait();
        clearInvocations(callback);
        when(service.validate2faCode("111111")).thenThrow(new IOException("down"),
                new ICloudApiResponseException("http://x", 500, "err"));

        initWithCode("111111");
        assertStatuses(UNKNOWN, offline(ThingStatusDetail.CONFIGURATION_ERROR, "Error while validating 2-FA code."));

        // The next initialization retries the validation.
        initAndWait();
        verify(service, times(2)).validate2faCode("111111");
    }
}
