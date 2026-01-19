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
package org.openhab.binding.jellyfin.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.jellyfin.internal.api.ApiClient;
import org.openhab.binding.jellyfin.internal.discovery.ClientDiscoveryService;
import org.openhab.binding.jellyfin.internal.events.ErrorEventBus;
import org.openhab.binding.jellyfin.internal.exceptions.ContextualExceptionHandler;
import org.openhab.binding.jellyfin.internal.handler.tasks.AbstractTask;
import org.openhab.binding.jellyfin.internal.handler.tasks.ConnectionTask;
import org.openhab.binding.jellyfin.internal.handler.tasks.DiscoveryTask;
import org.openhab.binding.jellyfin.internal.handler.tasks.ServerSyncTask;
import org.openhab.binding.jellyfin.internal.handler.tasks.TaskFactoryInterface;
import org.openhab.binding.jellyfin.internal.handler.tasks.UpdateTask;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.SystemInfo;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.UserDto;
import org.openhab.binding.jellyfin.internal.types.ServerState;

/**
 * Unit tests for {@link TaskManager}.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class TaskManagerTest {

    @Mock
    private TaskFactoryInterface mockTaskFactory = mock(TaskFactoryInterface.class);

    @Mock
    private ApiClient mockApiClient = mock(ApiClient.class);

    @Mock
    private ErrorEventBus mockErrorEventBus = mock(ErrorEventBus.class);

    @Mock
    private ServerHandler mockServerHandler = mock(ServerHandler.class);

    @Mock
    private ClientDiscoveryService mockDiscoveryService = mock(ClientDiscoveryService.class);

    @Mock
    private ScheduledExecutorService mockScheduler = mock(ScheduledExecutorService.class);

    @Mock
    private ConnectionTask mockConnectionTask = mock(ConnectionTask.class);

    @Mock
    private UpdateTask mockUpdateTask = mock(UpdateTask.class);

    @Mock
    private ServerSyncTask mockServerSyncTask = mock(ServerSyncTask.class);

    @Mock
    private DiscoveryTask mockDiscoveryTask = mock(DiscoveryTask.class);

    @Mock
    private ScheduledFuture<?> mockScheduledFuture = mock(ScheduledFuture.class);

    private @NonNullByDefault({}) TaskManager taskManager;

    @BeforeEach
    void setUp() {
        // Create TaskManager with mocked factory
        taskManager = new TaskManager(mockTaskFactory);
        // Setup default task IDs
        lenient().when(mockConnectionTask.getId()).thenReturn(ConnectionTask.TASK_ID);
        lenient().when(mockUpdateTask.getId()).thenReturn(UpdateTask.TASK_ID);
        lenient().when(mockServerSyncTask.getId()).thenReturn(ServerSyncTask.TASK_ID);
        lenient().when(mockDiscoveryTask.getId()).thenReturn(DiscoveryTask.TASK_ID);

        // Setup default task parameters
        lenient().when(mockConnectionTask.getStartupDelay()).thenReturn(0);
        lenient().when(mockConnectionTask.getInterval()).thenReturn(30);
        lenient().when(mockUpdateTask.getStartupDelay()).thenReturn(5);
        lenient().when(mockUpdateTask.getInterval()).thenReturn(60);
        lenient().when(mockServerSyncTask.getStartupDelay()).thenReturn(10);
        lenient().when(mockServerSyncTask.getInterval()).thenReturn(30);
        lenient().when(mockDiscoveryTask.getStartupDelay()).thenReturn(60);
        lenient().when(mockDiscoveryTask.getInterval()).thenReturn(60);

        // Setup task factory to return mock tasks
        lenient().when(mockTaskFactory.createConnectionTask(any(), any(), any())).thenReturn(mockConnectionTask);
        lenient().when(mockTaskFactory.createUpdateTask(any(), any())).thenReturn(mockUpdateTask);
        lenient().when(mockTaskFactory.createServerSyncTask(any(), any(), any())).thenReturn(mockServerSyncTask);
        lenient().when(mockTaskFactory.createDiscoveryTask(any(), any(), any())).thenReturn(mockDiscoveryTask);

        // Setup scheduler to return mock future
        lenient().when(
                mockScheduler.scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenAnswer(invocation -> mockScheduledFuture);
        lenient().when(mockScheduler.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class)))
                .thenAnswer(invocation -> mockScheduledFuture);
    }

    @Test
    void testConstructorWithValidTaskFactory() {
        // Arrange & Act
        TaskManager manager = new TaskManager(mockTaskFactory);

        // Assert
        assertNotNull(manager);
    }

    @Test
    void testInitializeTasks_CreatesExpectedTasks() {
        // Arrange
        @SuppressWarnings("unchecked")
        Consumer<SystemInfo> connectionHandler = mock(Consumer.class);
        @SuppressWarnings("unchecked")
        Consumer<List<UserDto>> usersHandler = mock(Consumer.class);

        // Act
        Map<String, AbstractTask> tasks = taskManager.initializeTasks(mockApiClient, mockErrorEventBus,
                connectionHandler, usersHandler, mockServerHandler, mockDiscoveryService);

        // Assert
        assertNotNull(tasks);
        assertEquals(3, tasks.size());
        assertTrue(tasks.containsKey(ConnectionTask.TASK_ID));
        assertTrue(tasks.containsKey(UpdateTask.TASK_ID));
        assertTrue(tasks.containsKey(ServerSyncTask.TASK_ID));
        assertFalse(tasks.containsKey(DiscoveryTask.TASK_ID)); // Not created during init

        // Verify factory calls
        verify(mockTaskFactory).createConnectionTask(eq(mockApiClient), eq(connectionHandler),
                any(ContextualExceptionHandler.class));
        verify(mockTaskFactory).createUpdateTask(eq(mockApiClient), any(ContextualExceptionHandler.class));
        verify(mockTaskFactory).createServerSyncTask(eq(mockApiClient), eq(usersHandler),
                any(ContextualExceptionHandler.class));
        verify(mockTaskFactory, never()).createDiscoveryTask(any(), any(), any());
    }

    @Test
    void testInitializeTasks_WithNullDiscoveryService() {
        // Arrange
        @SuppressWarnings("unchecked")
        Consumer<SystemInfo> connectionHandler = mock(Consumer.class);
        @SuppressWarnings("unchecked")
        Consumer<List<UserDto>> usersHandler = mock(Consumer.class);

        // Act
        Map<String, AbstractTask> tasks = taskManager.initializeTasks(mockApiClient, mockErrorEventBus,
                connectionHandler, usersHandler, mockServerHandler, null);

        // Assert
        assertNotNull(tasks);
        assertEquals(3, tasks.size());
        assertFalse(tasks.containsKey(DiscoveryTask.TASK_ID));
    }

    @Test
    void testProcessStateChange_ConfiguredState_StartsConnectionTask() {
        // Arrange
        Map<String, AbstractTask> availableTasks = new HashMap<>();
        availableTasks.put(ConnectionTask.TASK_ID, mockConnectionTask);
        availableTasks.put(ServerSyncTask.TASK_ID, mockServerSyncTask);

        Map<String, @Nullable ScheduledFuture<?>> scheduledTasks = new HashMap<>();

        // Act
        taskManager.processStateChange(ServerState.CONFIGURED, availableTasks, scheduledTasks, mockScheduler);

        // Assert
        assertTrue(scheduledTasks.containsKey(ConnectionTask.TASK_ID));
        assertFalse(scheduledTasks.containsKey(ServerSyncTask.TASK_ID));
        verify(mockScheduler).scheduleWithFixedDelay(eq(mockConnectionTask), eq(0L), eq(30L), eq(TimeUnit.SECONDS));
    }

    @Test
    void testProcessStateChange_ConnectedState_StartsServerSyncAndDiscoveryTasks() {
        // Arrange
        Map<String, AbstractTask> availableTasks = new HashMap<>();
        availableTasks.put(ConnectionTask.TASK_ID, mockConnectionTask);
        availableTasks.put(ServerSyncTask.TASK_ID, mockServerSyncTask);
        availableTasks.put(DiscoveryTask.TASK_ID, mockDiscoveryTask);

        Map<String, @Nullable ScheduledFuture<?>> scheduledTasks = new HashMap<>();

        // Act
        taskManager.processStateChange(ServerState.CONNECTED, availableTasks, scheduledTasks, mockScheduler);

        // Assert
        assertTrue(scheduledTasks.containsKey(ServerSyncTask.TASK_ID));
        assertTrue(scheduledTasks.containsKey(DiscoveryTask.TASK_ID));
        assertFalse(scheduledTasks.containsKey(ConnectionTask.TASK_ID));
        verify(mockScheduler).scheduleWithFixedDelay(eq(mockServerSyncTask), eq(10L), eq(30L), eq(TimeUnit.SECONDS));
        verify(mockScheduler).scheduleWithFixedDelay(eq(mockDiscoveryTask), eq(60L), eq(60L), eq(TimeUnit.SECONDS));
    }

    @Test
    void testProcessStateChange_InitializingState_NoTasksStarted() {
        // Arrange
        Map<String, AbstractTask> availableTasks = new HashMap<>();
        availableTasks.put(ConnectionTask.TASK_ID, mockConnectionTask);
        availableTasks.put(ServerSyncTask.TASK_ID, mockServerSyncTask);

        Map<String, @Nullable ScheduledFuture<?>> scheduledTasks = new HashMap<>();

        // Act
        taskManager.processStateChange(ServerState.INITIALIZING, availableTasks, scheduledTasks, mockScheduler);

        // Assert
        assertTrue(scheduledTasks.isEmpty());
        verify(mockScheduler, never()).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(),
                any(TimeUnit.class));
    }

    @Test
    void testProcessStateChange_ErrorState_NoTasksStarted() {
        // Arrange
        Map<String, AbstractTask> availableTasks = new HashMap<>();
        availableTasks.put(ConnectionTask.TASK_ID, mockConnectionTask);

        Map<String, @Nullable ScheduledFuture<?>> scheduledTasks = new HashMap<>();

        // Act
        taskManager.processStateChange(ServerState.ERROR, availableTasks, scheduledTasks, mockScheduler);

        // Assert
        assertTrue(scheduledTasks.isEmpty());
        verify(mockScheduler, never()).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(),
                any(TimeUnit.class));
    }

    @Test
    void testProcessStateChange_DisposedState_NoTasksStarted() {
        // Arrange
        Map<String, AbstractTask> availableTasks = new HashMap<>();
        availableTasks.put(ConnectionTask.TASK_ID, mockConnectionTask);

        Map<String, @Nullable ScheduledFuture<?>> scheduledTasks = new HashMap<>();

        // Act
        taskManager.processStateChange(ServerState.DISPOSED, availableTasks, scheduledTasks, mockScheduler);

        // Assert
        assertTrue(scheduledTasks.isEmpty());
        verify(mockScheduler, never()).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(),
                any(TimeUnit.class));
    }

    @Test
    void testProcessStateChange_StopsUnneededTasks() {
        // Arrange
        Map<String, AbstractTask> availableTasks = new HashMap<>();
        availableTasks.put(ConnectionTask.TASK_ID, mockConnectionTask);
        availableTasks.put(ServerSyncTask.TASK_ID, mockServerSyncTask);

        Map<String, @Nullable ScheduledFuture<?>> scheduledTasks = new HashMap<>();
        scheduledTasks.put(ConnectionTask.TASK_ID, mockScheduledFuture);

        when(mockScheduledFuture.isCancelled()).thenReturn(false);
        when(mockScheduledFuture.isDone()).thenReturn(false);

        // Act - transition from CONFIGURED (needs ConnectionTask) to CONNECTED (needs ServerSyncTask)
        taskManager.processStateChange(ServerState.CONNECTED, availableTasks, scheduledTasks, mockScheduler);

        // Assert
        verify(mockScheduledFuture).cancel(true);
        assertFalse(scheduledTasks.containsKey(ConnectionTask.TASK_ID));
        assertTrue(scheduledTasks.containsKey(ServerSyncTask.TASK_ID));
    }

    @Test
    void testProcessStateChange_TaskZeroInterval_ScheduledOnce() {
        // Arrange
        when(mockConnectionTask.getInterval()).thenReturn(0); // One-time task

        Map<String, AbstractTask> availableTasks = new HashMap<>();
        availableTasks.put(ConnectionTask.TASK_ID, mockConnectionTask);

        Map<String, @Nullable ScheduledFuture<?>> scheduledTasks = new HashMap<>();

        // Act
        taskManager.processStateChange(ServerState.CONFIGURED, availableTasks, scheduledTasks, mockScheduler);

        // Assert
        verify(mockScheduler).schedule(eq(mockConnectionTask), eq(0L), eq(TimeUnit.SECONDS));
        verify(mockScheduler, never()).scheduleWithFixedDelay(any(), anyLong(), anyLong(), any());
    }

    @Test
    void testStopAllTasks_StopsAllScheduledTasks() {
        // Arrange
        ScheduledFuture<?> future1 = mock(ScheduledFuture.class);
        ScheduledFuture<?> future2 = mock(ScheduledFuture.class);

        when(future1.isCancelled()).thenReturn(false);
        when(future1.isDone()).thenReturn(false);
        when(future2.isCancelled()).thenReturn(false);
        when(future2.isDone()).thenReturn(false);

        Map<String, @Nullable ScheduledFuture<?>> scheduledTasks = new HashMap<>();
        scheduledTasks.put(ConnectionTask.TASK_ID, future1);
        scheduledTasks.put(ServerSyncTask.TASK_ID, future2);

        // Act
        taskManager.stopAllTasks(scheduledTasks);

        // Assert
        verify(future1).cancel(true);
        verify(future2).cancel(true);
        assertTrue(scheduledTasks.isEmpty());
    }

    @Test
    void testStopAllTasks_SkipsAlreadyCancelledTasks() {
        // Arrange
        ScheduledFuture<?> cancelledFuture = mock(ScheduledFuture.class);
        when(cancelledFuture.isCancelled()).thenReturn(true);

        Map<String, @Nullable ScheduledFuture<?>> scheduledTasks = new HashMap<>();
        scheduledTasks.put(ConnectionTask.TASK_ID, cancelledFuture);

        // Act
        taskManager.stopAllTasks(scheduledTasks);

        // Assert
        verify(cancelledFuture, never()).cancel(anyBoolean());
        assertTrue(scheduledTasks.isEmpty());
    }

    @Test
    void testStopAllTasks_SkipsAlreadyDoneTasks() {
        // Arrange
        ScheduledFuture<?> doneFuture = mock(ScheduledFuture.class);
        when(doneFuture.isCancelled()).thenReturn(false);
        when(doneFuture.isDone()).thenReturn(true);

        Map<String, @Nullable ScheduledFuture<?>> scheduledTasks = new HashMap<>();
        scheduledTasks.put(ConnectionTask.TASK_ID, doneFuture);

        // Act
        taskManager.stopAllTasks(scheduledTasks);

        // Assert
        verify(doneFuture, never()).cancel(anyBoolean());
        assertTrue(scheduledTasks.isEmpty());
    }

    @Test
    void testStopAllTasks_HandlesNullFuture() {
        // Arrange
        Map<String, @Nullable ScheduledFuture<?>> scheduledTasks = new HashMap<>();
        scheduledTasks.put(ConnectionTask.TASK_ID, null);

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> taskManager.stopAllTasks(scheduledTasks));
        assertTrue(scheduledTasks.isEmpty());
    }

    @Test
    void testStopAllTasks_EmptyMap() {
        // Arrange
        Map<String, @Nullable ScheduledFuture<?>> scheduledTasks = new HashMap<>();

        // Act & Assert
        assertDoesNotThrow(() -> taskManager.stopAllTasks(scheduledTasks));
        assertTrue(scheduledTasks.isEmpty());
    }

    @Test
    void testCreateDiscoveryTask_CreatesTask() {
        // Act
        AbstractTask task = taskManager.createDiscoveryTask(mockServerHandler, mockDiscoveryService, mockErrorEventBus);

        // Assert
        assertNotNull(task);
        assertEquals(mockDiscoveryTask, task);
        verify(mockTaskFactory).createDiscoveryTask(eq(mockServerHandler), eq(mockDiscoveryService),
                any(ContextualExceptionHandler.class));
    }

    @Test
    void testProcessStateChange_DiscoveredState_NoTasksStarted() {
        // Arrange
        Map<String, AbstractTask> availableTasks = new HashMap<>();
        availableTasks.put(ConnectionTask.TASK_ID, mockConnectionTask);

        Map<String, @Nullable ScheduledFuture<?>> scheduledTasks = new HashMap<>();

        // Act
        taskManager.processStateChange(ServerState.DISCOVERED, availableTasks, scheduledTasks, mockScheduler);

        // Assert
        assertTrue(scheduledTasks.isEmpty());
        verify(mockScheduler, never()).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(),
                any(TimeUnit.class));
    }

    @Test
    void testProcessStateChange_NeedsAuthenticationState_NoTasksStarted() {
        // Arrange
        Map<String, AbstractTask> availableTasks = new HashMap<>();
        availableTasks.put(ConnectionTask.TASK_ID, mockConnectionTask);

        Map<String, @Nullable ScheduledFuture<?>> scheduledTasks = new HashMap<>();

        // Act
        taskManager.processStateChange(ServerState.NEEDS_AUTHENTICATION, availableTasks, scheduledTasks, mockScheduler);

        // Assert
        assertTrue(scheduledTasks.isEmpty());
        verify(mockScheduler, never()).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(),
                any(TimeUnit.class));
    }

    @Test
    void testProcessStateChange_WithWebSocketTask_InConnectedState() {
        // Arrange
        AbstractTask mockWebSocketTask = mock(AbstractTask.class);
        when(mockWebSocketTask.getId()).thenReturn(org.openhab.binding.jellyfin.internal.server.WebSocketTask.TASK_ID);
        when(mockWebSocketTask.getStartupDelay()).thenReturn(0);
        when(mockWebSocketTask.getInterval()).thenReturn(0);

        Map<String, AbstractTask> availableTasks = new HashMap<>();
        availableTasks.put(org.openhab.binding.jellyfin.internal.server.WebSocketTask.TASK_ID, mockWebSocketTask);
        availableTasks.put(DiscoveryTask.TASK_ID, mockDiscoveryTask);

        Map<String, @Nullable ScheduledFuture<?>> scheduledTasks = new HashMap<>();

        // Act
        taskManager.processStateChange(ServerState.CONNECTED, availableTasks, scheduledTasks, mockScheduler);

        // Assert
        assertTrue(scheduledTasks.containsKey(org.openhab.binding.jellyfin.internal.server.WebSocketTask.TASK_ID));
        assertTrue(scheduledTasks.containsKey(DiscoveryTask.TASK_ID));
    }
}
