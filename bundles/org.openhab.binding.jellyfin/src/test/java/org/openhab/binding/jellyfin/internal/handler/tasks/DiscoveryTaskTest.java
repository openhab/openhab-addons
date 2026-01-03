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
package org.openhab.binding.jellyfin.internal.handler.tasks;

import static org.mockito.Mockito.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.jellyfin.internal.discovery.ClientDiscoveryService;
import org.openhab.binding.jellyfin.internal.handler.ServerHandler;
import org.openhab.binding.jellyfin.internal.types.ExceptionHandlerType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;

/**
 * Unit tests for {@link DiscoveryTask}.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DiscoveryTaskTest {

    private @Mock ServerHandler serverHandler;
    private @Mock ClientDiscoveryService discoveryService;
    private @Mock ExceptionHandlerType exceptionHandler;
    private @Mock Bridge bridge;

    private DiscoveryTask discoveryTask;

    @BeforeEach
    void setUp() {
        // Setup default mock behavior
        when(serverHandler.getThing()).thenReturn(bridge);
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);

        // Create task
        discoveryTask = new DiscoveryTask(serverHandler, discoveryService, exceptionHandler);
    }

    @Test
    void testGetId() {
        assert discoveryTask.getId().equals(DiscoveryTask.TASK_ID);
    }

    @Test
    void testGetStartupDelay() {
        assert discoveryTask.getStartupDelay() == 60;
    }

    @Test
    void testGetInterval() {
        assert discoveryTask.getInterval() == 60;
    }

    @Test
    void testRun_ServerOnline_TriggersDiscovery() {
        // Arrange
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);

        // Act
        discoveryTask.run();

        // Assert
        verify(discoveryService, times(1)).discoverClients();
        verify(exceptionHandler, never()).handle(any());
    }

    @Test
    void testRun_ServerOffline_SkipsDiscovery() {
        // Arrange
        when(bridge.getStatus()).thenReturn(ThingStatus.OFFLINE);

        // Act
        discoveryTask.run();

        // Assert
        verify(discoveryService, never()).discoverClients();
        verify(exceptionHandler, never()).handle(any());
    }

    @Test
    void testRun_ServerUninitialized_SkipsDiscovery() {
        // Arrange
        when(bridge.getStatus()).thenReturn(ThingStatus.UNINITIALIZED);

        // Act
        discoveryTask.run();

        // Assert
        verify(discoveryService, never()).discoverClients();
        verify(exceptionHandler, never()).handle(any());
    }

    @Test
    void testRun_ServerUnknown_SkipsDiscovery() {
        // Arrange
        when(bridge.getStatus()).thenReturn(ThingStatus.UNKNOWN);

        // Act
        discoveryTask.run();

        // Assert
        verify(discoveryService, never()).discoverClients();
        verify(exceptionHandler, never()).handle(any());
    }

    @Test
    void testRun_ServerRemoving_SkipsDiscovery() {
        // Arrange
        when(bridge.getStatus()).thenReturn(ThingStatus.REMOVING);

        // Act
        discoveryTask.run();

        // Assert
        verify(discoveryService, never()).discoverClients();
        verify(exceptionHandler, never()).handle(any());
    }

    @Test
    void testRun_DiscoveryThrowsException_HandledException() {
        // Arrange
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        RuntimeException exception = new RuntimeException("Discovery failed");
        doThrow(exception).when(discoveryService).discoverClients();

        // Act
        discoveryTask.run();

        // Assert
        verify(discoveryService, times(1)).discoverClients();
        verify(exceptionHandler, times(1)).handle(exception);
    }

    @Test
    void testRun_MultipleCallsWhenOnline_MultipleDiscoveries() {
        // Arrange
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);

        // Act
        discoveryTask.run();
        discoveryTask.run();
        discoveryTask.run();

        // Assert
        verify(discoveryService, times(3)).discoverClients();
        verify(exceptionHandler, never()).handle(any());
    }

    @Test
    void testRun_StatusChangesFromOfflineToOnline_DiscoveryTriggeredOnlyWhenOnline() {
        // Arrange - start OFFLINE
        when(bridge.getStatus()).thenReturn(ThingStatus.OFFLINE);

        // Act - first run (offline)
        discoveryTask.run();
        verify(discoveryService, never()).discoverClients();

        // Arrange - change to ONLINE
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);

        // Act - second run (online)
        discoveryTask.run();

        // Assert
        verify(discoveryService, times(1)).discoverClients();
    }
}
