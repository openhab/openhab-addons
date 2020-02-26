/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openhab.binding.hive.internal.HiveBindingConstants;
import org.openhab.binding.hive.internal.MultithreadedTestBase;
import org.openhab.binding.hive.internal.client.HiveClient;
import org.openhab.binding.hive.internal.client.HiveClientFactory;
import org.openhab.binding.hive.internal.client.exception.HiveApiAuthenticationException;
import org.openhab.binding.hive.internal.client.exception.HiveApiUnknownException;
import org.openhab.binding.hive.internal.client.exception.HiveException;
import org.openhab.binding.hive.internal.discovery.HiveDiscoveryService;
import org.openhab.binding.hive.internal.discovery.HiveDiscoveryServiceFactory;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public class DefaultHiveAccountHandlerTest extends MultithreadedTestBase {
    private final Bridge bridge;

    @NonNullByDefault({})
    @Mock
    private HiveClientFactory hiveClientFactory;

    @NonNullByDefault({})
    @Mock
    private HiveClient hiveClient;

    @NonNullByDefault({})
    @Mock
    private HiveDiscoveryServiceFactory hiveDiscoveryServiceFactory;

    @NonNullByDefault({})
    @Mock
    private HiveDiscoveryService hiveDiscoveryService;

    @NonNullByDefault({})
    @Mock
    private ThingHandlerCallback thingHandlerCallback;

    public DefaultHiveAccountHandlerTest() {
        super(200, TimeUnit.MILLISECONDS);

        final Configuration bridgeConfig = new Configuration();
        bridgeConfig.put("username", "hiveuser@example.com");
        bridgeConfig.put("password", "supersecretpassword");
        bridgeConfig.put("pollingInterval", "10");

        this.bridge = BridgeBuilder.create(HiveBindingConstants.THING_TYPE_ACCOUNT, "dummy-account")
                .withConfiguration(bridgeConfig)
                .build();
    }

    @Before
    public void setUp() throws HiveException {
        initMocks(this);

        when(this.hiveClient.getAllNodes()).thenReturn(Collections.emptySet());

        when(this.hiveDiscoveryServiceFactory.create(any())).thenReturn(this.hiveDiscoveryService);
    }

    @Test
    public void testDiscoveryServiceCreated() {
        /* When */
        final DefaultHiveAccountHandler accountHandler = new DefaultHiveAccountHandler(
                this.hiveClientFactory,
                this.hiveDiscoveryServiceFactory,
                this.bridge
        );


        /* Then */
        // Check the discovery service was created.
        verify(this.hiveDiscoveryServiceFactory).create(this.bridge.getUID());

        // Check the discovery service was stored.
        assertThat(accountHandler.getDiscoveryService()).isEqualTo(this.hiveDiscoveryService);
    }

    @Test
    public void testInitGood() throws TimeoutException, InterruptedException, HiveException {
        /* Given */
        final DefaultHiveAccountHandler accountHandler = new DefaultHiveAccountHandler(
                this.hiveClientFactory,
                this.hiveDiscoveryServiceFactory,
                this.bridge,
                this.getTestingPhaser()
        );

        accountHandler.setCallback(this.thingHandlerCallback);

        // Throw an authentication exception when we try to create hive client.
        when(this.hiveClientFactory.newClient(any(), any())).thenReturn(this.hiveClient);


        /* When */
        // Try to initialise handler
        accountHandler.initialize();


        /* Then */
        // Wait for all initialize() tasks to execute
        this.awaitTestingPhaser();

        // Check the thing status was updated correctly.
        final ArgumentCaptor<ThingStatusInfo> capturedThingStatusInfo = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(this.thingHandlerCallback, atLeastOnce()).statusUpdated(eq(this.bridge), capturedThingStatusInfo.capture());
        final ThingStatusInfo lastStatusInfo = capturedThingStatusInfo.getValue();
        assertThat(lastStatusInfo.getStatus()).isEqualTo(ThingStatus.ONLINE);
    }

    @Test
    public void testInitBadCredentials() throws TimeoutException, InterruptedException, HiveException {
        /* Given */
        final DefaultHiveAccountHandler accountHandler = new DefaultHiveAccountHandler(
                this.hiveClientFactory,
                this.hiveDiscoveryServiceFactory,
                this.bridge,
                this.getTestingPhaser()
        );

        accountHandler.setCallback(this.thingHandlerCallback);

        // Throw an authentication exception when we try to create hive client.
        when(this.hiveClientFactory.newClient(any(), any())).thenThrow(
                new HiveApiAuthenticationException()
        );


        /* When */
        // Try to initialise handler
        accountHandler.initialize();


        /* Then */
        // Wait for all initialize() tasks to execute
        this.awaitTestingPhaser();

        // Check the thing status was updated correctly.
        final ArgumentCaptor<ThingStatusInfo> capturedThingStatusInfo = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(this.thingHandlerCallback, atLeastOnce()).statusUpdated(eq(this.bridge), capturedThingStatusInfo.capture());
        final ThingStatusInfo lastStatusInfo = capturedThingStatusInfo.getValue();
        assertThat(lastStatusInfo.getStatus()).isEqualTo(ThingStatus.OFFLINE);
        assertThat(lastStatusInfo.getStatusDetail()).isEqualTo(ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Test
    public void testInitApiException() throws TimeoutException, InterruptedException, HiveException {
        /* Given */
        final DefaultHiveAccountHandler accountHandler = new DefaultHiveAccountHandler(
                this.hiveClientFactory,
                this.hiveDiscoveryServiceFactory,
                this.bridge,
                this.getTestingPhaser()
        );

        accountHandler.setCallback(this.thingHandlerCallback);

        // Throw an authentication exception when we try to create hive client.
        when(this.hiveClientFactory.newClient(any(), any())).thenThrow(
                new HiveApiUnknownException()
        );


        /* When */
        // Try to initialise handler
        accountHandler.initialize();


        /* Then */
        // Wait for all initialize() tasks to execute
        this.awaitTestingPhaser();

        // Check the thing status was updated correctly.
        final ArgumentCaptor<ThingStatusInfo> capturedThingStatusInfo = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(this.thingHandlerCallback, atLeastOnce()).statusUpdated(eq(this.bridge), capturedThingStatusInfo.capture());
        final ThingStatusInfo lastStatusInfo = capturedThingStatusInfo.getValue();
        assertThat(lastStatusInfo.getStatus()).isEqualTo(ThingStatus.OFFLINE);
        assertThat(lastStatusInfo.getStatusDetail()).isEqualTo(ThingStatusDetail.COMMUNICATION_ERROR);
    }
}
