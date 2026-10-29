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
import org.openhab.core.thing.binding.BaseThingHandlerFactory;

/**
 * Tests for {@link NodeHandler}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class NodeHandlerTest {

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

    @NonNullByDefault({})
    private NodeHandler handler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new NodeHandler(bridge, thingHandlerFactory, stateDescriptionProvider, channelTypeProvider,
                configDescriptionProvider, translationService);
        handler.cachedClient = client;
        handler.nodeId = BigInteger.valueOf(42);
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
}
