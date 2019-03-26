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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openhab.binding.mqtt.TransformationServiceProvider;
import org.openhab.binding.mqtt.internal.handler.ThingChannelConstants;
import org.openhab.binding.mqtt.internal.homeassistant.ChannelConfigurationTypeAdapterFactory;
import org.openhab.binding.mqtt.internal.homeassistant.DiscoverComponents;
import org.openhab.binding.mqtt.internal.homeassistant.HaID;
import org.openhab.binding.mqtt.internal.homeassistant.HandlerConfiguration;
import org.openhab.binding.mqtt.internal.homeassistant.DiscoverComponents.ComponentDiscovered;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Tests the {@link DiscoverComponents} class.
 *
 * @author David Graeff - Initial contribution
 */
public class DiscoverComponentsTests extends JavaOSGiTest {
    @Mock
    MqttBrokerConnection connection;

    @Mock
    ComponentDiscovered discovered;

    @Mock
    TransformationServiceProvider transformationServiceProvider;

    @Before
    public void setUp() {
        initMocks(this);
        CompletableFuture<Void> voidFutureComplete = new CompletableFuture<Void>();
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
                scheduler, null, gson, transformationServiceProvider));

        HandlerConfiguration config = new HandlerConfiguration("homeassistant", "object");

        discover.startDiscovery(connection, 50, HaID.fromConfig(config), discovered).get(100, TimeUnit.MILLISECONDS);

    }
}
