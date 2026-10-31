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
package org.openhab.binding.matter.internal.handler;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.matter.internal.MatterChannelTypeProvider;
import org.openhab.binding.matter.internal.MatterConfigDescriptionProvider;
import org.openhab.binding.matter.internal.MatterStateDescriptionOptionProvider;
import org.openhab.binding.matter.internal.client.dto.ws.OtaUpdateInfo;
import org.openhab.binding.matter.internal.controller.MatterControllerClient;
import org.openhab.binding.matter.internal.util.TranslationService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Tests for {@link NodeHandler}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class NodeHandlerTest {

    private static final ThingUID NODE_UID = new ThingUID("matter:node:main:42");

    @Mock
    @NonNullByDefault({})
    private Bridge bridge;
    @Mock
    @NonNullByDefault({})
    private BaseThingHandlerFactory thingHandlerFactory;
    @Mock
    @NonNullByDefault({})
    private MatterStateDescriptionOptionProvider stateDescriptionProvider;
    @Mock
    @NonNullByDefault({})
    private MatterChannelTypeProvider channelTypeProvider;
    @Mock
    @NonNullByDefault({})
    private MatterConfigDescriptionProvider configDescriptionProvider;
    @Mock
    @NonNullByDefault({})
    private TranslationService translationService;
    @Mock
    @NonNullByDefault({})
    private MatterControllerClient client;
    @Mock
    @NonNullByDefault({})
    private ThingHandlerCallback callback;
    @Mock
    @NonNullByDefault({})
    private Bridge controllerBridge;
    @Mock
    @NonNullByDefault({})
    private ControllerHandler controllerHandler;

    @NonNullByDefault({})
    private NodeHandler handler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ThingUID controllerUID = new ThingUID("matter:controller:main");
        when(bridge.getBridgeUID()).thenReturn(controllerUID);
        when(bridge.getUID()).thenReturn(NODE_UID);
        when(callback.getBridge(controllerUID)).thenReturn(controllerBridge);
        when(controllerBridge.getHandler()).thenReturn(controllerHandler);
        handler = new NodeHandler(bridge, thingHandlerFactory, stateDescriptionProvider, channelTypeProvider,
                configDescriptionProvider, translationService);
        handler.cachedClient = client;
        handler.nodeId = BigInteger.valueOf(42);
        handler.setCallback(callback);
    }

    @Test
    public void concurrentOtaChecksShareOneQuery() {
        CompletableFuture<@Nullable OtaUpdateInfo> pending = new CompletableFuture<>();
        when(client.otaQueryForUpdates(any(), anyBoolean())).thenReturn(pending);

        CompletableFuture<@Nullable OtaUpdateInfo> first = handler.checkForOTAUpdate();
        CompletableFuture<@Nullable OtaUpdateInfo> second = handler.checkForOTAUpdate();

        assertSame(first, second);
        verify(client, times(1)).otaQueryForUpdates(BigInteger.valueOf(42), true);
    }

    @Test
    public void otaCheckAfterCompletedQuerySendsNewQuery() {
        when(client.otaQueryForUpdates(any(), anyBoolean())).thenReturn(CompletableFuture.completedFuture(null))
                .thenReturn(new CompletableFuture<>());

        CompletableFuture<@Nullable OtaUpdateInfo> first = handler.checkForOTAUpdate();
        CompletableFuture<@Nullable OtaUpdateInfo> second = handler.checkForOTAUpdate();

        assertNotSame(first, second);
        verify(client, times(2)).otaQueryForUpdates(BigInteger.valueOf(42), true);
    }

    @Test
    public void otaQueryResultIsStoredForFirmwareLookups() {
        OtaUpdateInfo updateInfo = new OtaUpdateInfo(1, 2, 3, "1.2.3", null, null);
        when(client.otaQueryForUpdates(any(), anyBoolean())).thenReturn(CompletableFuture.completedFuture(updateInfo));

        handler.checkForOTAUpdate();

        verify(controllerHandler).updateFirmwareInfo(NODE_UID, updateInfo);
    }

    @Test
    public void otaQueryWithNoUpdateIsStoredForFirmwareLookups() {
        when(client.otaQueryForUpdates(any(), anyBoolean())).thenReturn(CompletableFuture.completedFuture(null));

        handler.checkForOTAUpdate();

        verify(controllerHandler).updateFirmwareInfo(NODE_UID, null);
    }
}
