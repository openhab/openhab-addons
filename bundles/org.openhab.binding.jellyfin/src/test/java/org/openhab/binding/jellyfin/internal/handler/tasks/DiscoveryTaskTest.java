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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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

    private @Nullable @Mock ServerHandler serverHandler;
    private @Nullable @Mock ClientDiscoveryService discoveryService;
    private @Nullable @Mock ExceptionHandlerType exceptionHandler;
    private @Nullable @Mock Bridge bridge;

    private @Nullable DiscoveryTask discoveryTask;

    @BeforeEach
    void setUp() {
        // Setup default mock behavior
        when(Objects.requireNonNull(serverHandler).getThing()).thenReturn(Objects.requireNonNull(bridge));
        when(Objects.requireNonNull(bridge).getStatus()).thenReturn(ThingStatus.ONLINE);

        // Create task
        discoveryTask = new DiscoveryTask(Objects.requireNonNull(serverHandler),
                Objects.requireNonNull(discoveryService), Objects.requireNonNull(exceptionHandler));
    }

    @Test
    void testGetId() {
        assert Objects.requireNonNull(discoveryTask).getId().equals(DiscoveryTask.TASK_ID);
    }

    @Test
    void testGetStartupDelay() {
        assert Objects.requireNonNull(discoveryTask).getStartupDelay() == 60;
    }

    @Test
    void testGetInterval() {
        assert Objects.requireNonNull(discoveryTask).getInterval() == 60;
    }

    @Test
    void testRun_ServerOnline_TriggersDiscovery() {
        // Arrange
        when(Objects.requireNonNull(bridge).getStatus()).thenReturn(ThingStatus.ONLINE);

        // Act
        Objects.requireNonNull(discoveryTask).run();

        // Assert
        verify(Objects.requireNonNull(discoveryService), times(1)).discoverClients();
        verify(Objects.requireNonNull(exceptionHandler), never()).handle(any());
    }

    @Test
    void testRun_ServerOffline_SkipsDiscovery() {
        // Arrange
        when(Objects.requireNonNull(bridge).getStatus()).thenReturn(ThingStatus.OFFLINE);

        // Act
        Objects.requireNonNull(discoveryTask).run();

        // Assert
        verify(Objects.requireNonNull(discoveryService), never()).discoverClients();
        verify(Objects.requireNonNull(exceptionHandler), never()).handle(any());
    }

    @Test
    void testRun_ServerUninitialized_SkipsDiscovery() {
        // Arrange
        when(Objects.requireNonNull(bridge).getStatus()).thenReturn(ThingStatus.UNINITIALIZED);

        // Act
        Objects.requireNonNull(discoveryTask).run();

        // Assert
        verify(Objects.requireNonNull(discoveryService), never()).discoverClients();
        verify(Objects.requireNonNull(exceptionHandler), never()).handle(any());
    }

    @Test
    void testRun_ServerUnknown_SkipsDiscovery() {
        // Arrange
        when(Objects.requireNonNull(bridge).getStatus()).thenReturn(ThingStatus.UNKNOWN);

        // Act
        Objects.requireNonNull(discoveryTask).run();

        // Assert
        verify(Objects.requireNonNull(discoveryService), never()).discoverClients();
        verify(Objects.requireNonNull(exceptionHandler), never()).handle(any());
    }

    @Test
    void testRun_ServerRemoving_SkipsDiscovery() {
        // Arrange
        when(Objects.requireNonNull(bridge).getStatus()).thenReturn(ThingStatus.REMOVING);

        // Act
        Objects.requireNonNull(discoveryTask).run();

        // Assert
        verify(Objects.requireNonNull(discoveryService), never()).discoverClients();
        verify(Objects.requireNonNull(exceptionHandler), never()).handle(any());
    }

    @Test
    void testRun_DiscoveryThrowsException_HandledException() {
        // Arrange
        when(Objects.requireNonNull(bridge).getStatus()).thenReturn(ThingStatus.ONLINE);
        RuntimeException exception = new RuntimeException("Discovery failed");
        doThrow(exception).when(Objects.requireNonNull(discoveryService)).discoverClients();

        // Act
        Objects.requireNonNull(discoveryTask).run();

        // Assert
        verify(Objects.requireNonNull(discoveryService), times(1)).discoverClients();
        verify(Objects.requireNonNull(exceptionHandler), times(1)).handle(exception);
    }

    @Test
    void testRun_MultipleCallsWhenOnline_MultipleDiscoveries() {
        // Arrange
        when(Objects.requireNonNull(bridge).getStatus()).thenReturn(ThingStatus.ONLINE);

        // Act
        Objects.requireNonNull(discoveryTask).run();
        Objects.requireNonNull(discoveryTask).run();
        Objects.requireNonNull(discoveryTask).run();

        // Assert
        verify(Objects.requireNonNull(discoveryService), times(3)).discoverClients();
        verify(Objects.requireNonNull(exceptionHandler), never()).handle(any());
    }

    @Test
    void testRun_StatusChangesFromOfflineToOnline_DiscoveryTriggeredOnlyWhenOnline() {
        // Arrange - start OFFLINE
        when(Objects.requireNonNull(bridge).getStatus()).thenReturn(ThingStatus.OFFLINE);

        // Act - first run (offline)
        Objects.requireNonNull(discoveryTask).run();
        verify(Objects.requireNonNull(discoveryService), never()).discoverClients();

        // Arrange - change to ONLINE
        when(Objects.requireNonNull(bridge).getStatus()).thenReturn(ThingStatus.ONLINE);

        // Act - second run (online)
        Objects.requireNonNull(discoveryTask).run();

        // Assert
        verify(Objects.requireNonNull(discoveryService), times(1)).discoverClients();
    }
}
