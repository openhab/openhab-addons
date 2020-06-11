/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.mqtt;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.TransformationServiceProvider;
import org.openhab.binding.mqtt.homeassistant.internal.ChannelConfigurationTypeAdapterFactory;
import org.openhab.binding.mqtt.homeassistant.internal.DiscoverComponents;
import org.openhab.binding.mqtt.homeassistant.internal.DiscoverComponents.ComponentDiscovered;
import org.openhab.binding.mqtt.homeassistant.internal.HaID;
import org.openhab.binding.mqtt.homeassistant.internal.HandlerConfiguration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Tests the {@link DiscoverComponents} class.
 *
 * @author David Graeff - Initial contribution
 */
public class DiscoverComponentsTest extends JavaOSGiTest {
    @Mock
    MqttBrokerConnection connection;

    @Mock
    ComponentDiscovered discovered;

    @Mock
    TransformationServiceProvider transformationServiceProvider;

    @Before
    public void setUp() {
        initMocks(this);
        CompletableFuture<Void> voidFutureComplete = new CompletableFuture<>();
        voidFutureComplete.complete(null);
        doReturn(voidFutureComplete).when(connection).unsubscribeAll();
        doReturn(CompletableFuture.completedFuture(true)).when(connection).subscribe(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connection).unsubscribe(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connection).publish(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connection).publish(any(), any(), anyInt(),
                anyBoolean());
        doReturn(null).when(transformationServiceProvider).getTransformationService(any());
    }

    @Test
    public void discoveryTimeTest() throws InterruptedException, ExecutionException, TimeoutException {
        // Create a scheduler
        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);

        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new ChannelConfigurationTypeAdapterFactory()).create();

        DiscoverComponents discover = spy(new DiscoverComponents(ThingChannelConstants.testHomeAssistantThing,
                scheduler, new ChannelStateUpdateListener() {
                    @Override
                    public void updateChannelState(@NonNull ChannelUID channelUID, @NonNull State value) {
                    }

                    @Override
                    public void triggerChannel(@NonNull ChannelUID channelUID, @NonNull String eventPayload) {
                    }

                    @Override
                    public void postChannelCommand(@NonNull ChannelUID channelUID, @NonNull Command value) {
                    }
                }, gson, transformationServiceProvider));

        HandlerConfiguration config = new HandlerConfiguration("homeassistant",
                Collections.singletonList("switch/object"));

        Set<HaID> discoveryIds = new HashSet<>();
        discoveryIds.addAll(HaID.fromConfig(config));

        discover.startDiscovery(connection, 50, discoveryIds, discovered).get(100, TimeUnit.MILLISECONDS);

    }
}
