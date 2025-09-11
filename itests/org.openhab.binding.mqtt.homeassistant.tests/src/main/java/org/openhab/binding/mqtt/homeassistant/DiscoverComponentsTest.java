/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homeassistant;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.mqtt.generic.AvailabilityTracker;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.homeassistant.internal.DiscoverComponents;
import org.openhab.binding.mqtt.homeassistant.internal.DiscoverComponents.ComponentDiscovered;
import org.openhab.binding.mqtt.homeassistant.internal.HaID;
import org.openhab.binding.mqtt.homeassistant.internal.HandlerConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.HomeAssistantChannelLinkageChecker;
import org.openhab.binding.mqtt.homeassistant.internal.HomeAssistantPythonBridge;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.test.java.JavaOSGiTest;

import com.google.gson.Gson;

/**
 * Tests the {@link DiscoverComponents} class.
 *
 * @author David Graeff - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class DiscoverComponentsTest extends JavaOSGiTest {

    private @Mock @NonNullByDefault({}) MqttBrokerConnection connection;
    private @Mock @NonNullByDefault({}) ComponentDiscovered discovered;
    private @Mock @NonNullByDefault({}) ChannelStateUpdateListener channelStateUpdateListener;
    private @Mock @NonNullByDefault({}) HomeAssistantChannelLinkageChecker linkageChecker;
    private @Mock @NonNullByDefault({}) AvailabilityTracker availabilityTracker;

    private static final HomeAssistantPythonBridge python = new HomeAssistantPythonBridge();

    @BeforeEach
    public void beforeEach() {
        CompletableFuture<@Nullable Void> voidFutureComplete = new CompletableFuture<>();
        voidFutureComplete.complete(null);
        doReturn(voidFutureComplete).when(connection).unsubscribeAll();
        doReturn(CompletableFuture.completedFuture(true)).when(connection).subscribe(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connection).unsubscribe(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connection).publish(any(), any(), anyInt(),
                anyBoolean());
    }

    @Test
    public void discoveryTimeTest() throws InterruptedException, ExecutionException, TimeoutException {
        // Create a scheduler
        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);

        Gson gson = new Gson();
        UnitProvider unitProvider = mock(UnitProvider.class);

        DiscoverComponents discover = spy(
                new DiscoverComponents(ThingChannelConstants.TEST_HOME_ASSISTANT_THING, scheduler,
                        channelStateUpdateListener, linkageChecker, availabilityTracker, gson, python, unitProvider));

        HandlerConfiguration config = new HandlerConfiguration("homeassistant", List.of("switch/object"));

        Set<HaID> discoveryIds = new HashSet<>();
        discoveryIds.addAll(HaID.fromConfig(config));

        discover.startDiscovery(connection, 50, discoveryIds, discovered).get(100, TimeUnit.MILLISECONDS);
    }
}
