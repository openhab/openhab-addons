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
package org.openhab.binding.myvaillant.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.myvaillant.internal.MyVaillantBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.myvaillant.internal.api.JsonUtil;
import org.openhab.binding.myvaillant.internal.api.MyVaillantApi;
import org.openhab.binding.myvaillant.internal.api.MyVaillantException;
import org.openhab.binding.myvaillant.internal.config.AccountConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests that an update of {@link SystemHandler} that is still running when polling stops does not publish its result.
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SystemHandlerPollingTest {

    private static final String SYSTEM_ID = "sys";
    private static final ThingUID ACCOUNT_UID = new ThingUID(THING_TYPE_ACCOUNT, "account");
    private static final ThingUID SYSTEM_UID = new ThingUID(THING_TYPE_SYSTEM, ACCOUNT_UID, "system");

    private @Mock @NonNullByDefault({}) Bridge system;
    private @Mock @NonNullByDefault({}) Bridge account;
    private @Mock @NonNullByDefault({}) AccountHandler accountHandler;
    private @Mock @NonNullByDefault({}) MyVaillantApi api;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;

    @Test
    void updateRunningWhileTheAccountGoesOfflineDoesNotSetTheSystemOnline() throws Exception {
        AtomicBoolean accountOnline = new AtomicBoolean(true);
        CountDownLatch requestStarted = new CountDownLatch(1);
        CountDownLatch releaseRequest = new CountDownLatch(1);

        when(system.getUID()).thenReturn(SYSTEM_UID);
        when(system.getBridgeUID()).thenReturn(ACCOUNT_UID);
        when(system.getConfiguration()).thenReturn(new Configuration(Map.of(CONFIG_SYSTEM_ID, SYSTEM_ID)));
        when(callback.getBridge(ACCOUNT_UID)).thenReturn(account);
        when(account.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(account.getHandler()).thenReturn(accountHandler);
        when(accountHandler.getAccountConfiguration()).thenReturn(new AccountConfiguration());
        when(accountHandler.isOnline()).thenAnswer(invocation -> accountOnline.get());
        when(accountHandler.request(any())).thenAnswer(invocation -> {
            if (!accountOnline.get()) {
                throw new MyVaillantException("The account is not online");
            }
            return invocation.<AccountHandler.ApiRequest<?>> getArgument(0).execute(api);
        });
        when(api.getControlIdentifier(SYSTEM_ID)).thenReturn(MyVaillantApi.CONTROL_IDENTIFIER_TLI);
        when(api.getHome(SYSTEM_ID)).thenReturn(JsonParser.parseString("{\"system_id\":\"sys\"}").getAsJsonObject());
        when(api.getTimeZone(SYSTEM_ID)).thenReturn(ZoneId.of("Europe/Berlin"));
        JsonObject tli = load("system.json");
        when(api.getSystem(SYSTEM_ID)).thenAnswer(invocation -> {
            requestStarted.countDown();
            assertTrue(releaseRequest.await(10, TimeUnit.SECONDS));
            return tli;
        });
        when(api.getCurrentSystem(SYSTEM_ID)).thenReturn(load("current_system.json"));

        SystemHandler handler = new SystemHandler(system);
        handler.setCallback(callback);
        try {
            handler.initialize();
            assertTrue(requestStarted.await(10, TimeUnit.SECONDS));

            // The account goes offline and online again, the system only got notified about the first change so far
            accountOnline.set(false);
            handler.bridgeStatusChanged(
                    new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, null));
            accountOnline.set(true);
            releaseRequest.countDown();

            verify(api, timeout(10_000)).getCurrentSystem(SYSTEM_ID);
            verify(callback, after(500).never()).statusUpdated(same(system),
                    argThat(status -> status.getStatus() == ThingStatus.ONLINE));
            verify(callback).statusUpdated(same(system), argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                    && status.getStatusDetail() == ThingStatusDetail.OFFLINE.BRIDGE_OFFLINE));
            assertNull(handler.getData());
        } finally {
            handler.dispose();
        }
    }

    private static JsonObject load(String name) throws IOException {
        try (InputStream stream = SystemHandlerPollingTest.class
                .getResourceAsStream("/org/openhab/binding/myvaillant/internal/api/" + name)) {
            assertNotNull(stream, name);
            return JsonUtil
                    .toSnakeCase(JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8)))
                    .getAsJsonObject();
        }
    }
}
