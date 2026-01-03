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
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.jellyfin.internal.Configuration;
import org.openhab.binding.jellyfin.internal.Constants;
import org.openhab.binding.jellyfin.internal.handler.tasks.AbstractTask;
import org.openhab.binding.jellyfin.internal.types.ServerState;
import org.openhab.core.thing.Thing;

/**
 * Unit tests for {@link ServerHandler#resolveServerUri()}.
 *
 * @author Patrik Gfeller - Initial contribution
 */
class ServerHandlerTest {
    private ServerHandler handler;
    private Configuration configuration;
    private Thing thing;

    // Test subclass to override getConfigAs
    private static class TestServerHandler extends ServerHandler {
        private static Configuration configForCtor;
        private final Configuration testConfig;

        private static Configuration setConfigForCtor(Configuration config) {
            configForCtor = config;
            return config;
        }

        TestServerHandler(Configuration config, Thing thing, TaskManagerInterface taskManager) {
            super(mock(org.openhab.core.thing.Bridge.class), null, taskManager);
            this.testConfig = config;
            configForCtor = null;
            // Set the 'thing' field in BaseBridgeHandler to the testThing mock
            try {
                java.lang.reflect.Field thingField = org.openhab.core.thing.binding.BaseThingHandler.class
                        .getDeclaredField("thing");
                thingField.setAccessible(true);
                thingField.set(this, thing);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set thing field in test subclass", e);
            }
        }

        @Override
        public <T> T getConfigAs(Class<T> configClass) {
            // Return the config for the base constructor, then instance field after
            if (configForCtor != null) {
                return configClass.cast(configForCtor);
            }
            return configClass.cast(testConfig);
        }
    }

    @BeforeEach
    void setUp() {
        configuration = mock(Configuration.class);
        thing = mock(Thing.class);
        TaskManagerInterface mockTaskManager = mock(TaskManagerInterface.class);
        when(mockTaskManager.initializeTasks(any(), any(), any(), any(), any(), any()))
                .thenReturn(new java.util.HashMap<>());
        handler = new TestServerHandler(TestServerHandler.setConfigForCtor(configuration), thing, mockTaskManager);
    }

    @Test
    void testResolveServerUri_UsesThingPropertyIfValid() throws Exception {
        Map<String, String> props = new HashMap<>();
        props.put(Constants.ServerProperties.SERVER_URI, "http://test:1234/path");
        when(thing.getProperties()).thenReturn(props);
        when(configuration.getServerURI()).thenReturn(new URI("http://fallback:8080/")); // fallback if needed
        var method = ServerHandler.class.getDeclaredMethod("resolveServerUri");
        method.setAccessible(true);
        URI uri = (URI) method.invoke(handler);
        assertEquals("http://test:1234/path", uri.toString());
    }

    @Test
    void testResolveServerUri_ThrowsIfThingPropertyInvalid() {
        Map<String, String> props = new HashMap<>();
        props.put(Constants.ServerProperties.SERVER_URI, "not a uri");
        when(thing.getProperties()).thenReturn(props);
        try {
            when(configuration.getServerURI()).thenReturn(new URI("http://fallback:8080/")); // fallback if needed
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Exception ex = assertThrows(Exception.class, () -> {
            try {
                var method = ServerHandler.class.getDeclaredMethod("resolveServerUri");
                method.setAccessible(true);
                method.invoke(handler);
            } catch (java.lang.reflect.InvocationTargetException ite) {
                throw ite.getCause();
            }
        });
        assertTrue(ex instanceof java.net.URISyntaxException || ex instanceof IllegalStateException
                || ex instanceof IllegalArgumentException);
    }

    @Test
    void testResolveServerUri_ThrowsIfThingPropertyUnsupportedScheme() {
        Map<String, String> props = new HashMap<>();
        props.put(Constants.ServerProperties.SERVER_URI, "ftp://test:1234/path");
        when(thing.getProperties()).thenReturn(props);
        try {
            when(configuration.getServerURI()).thenReturn(new URI("http://fallback:8080/")); // fallback if needed
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Throwable ex = assertThrows(Throwable.class, () -> {
            try {
                var method = ServerHandler.class.getDeclaredMethod("resolveServerUri");
                method.setAccessible(true);
                method.invoke(handler);
            } catch (java.lang.reflect.InvocationTargetException ite) {
                throw ite.getCause();
            }
        });
        assertTrue(ex instanceof IllegalArgumentException);
    }

    @Test
    void testResolveServerUri_FallsBackToConfigurationIfNoProperty() throws Exception {
        Map<String, String> props = new HashMap<>();
        when(thing.getProperties()).thenReturn(props);
        when(configuration.getServerURI()).thenReturn(new URI("http://fallback:8080/"));
        var method = ServerHandler.class.getDeclaredMethod("resolveServerUri");
        method.setAccessible(true);
        URI uri = (URI) method.invoke(handler);
        assertEquals("http://fallback:8080/", uri.toString());
    }

    @Test
    void testResolveServerUri_ThrowsIfConfigurationInvalid() throws Exception {
        Map<String, String> props = new HashMap<>();
        when(thing.getProperties()).thenReturn(props);
        when(configuration.getServerURI()).thenThrow(new java.net.URISyntaxException("bad", "reason"));
        var method = ServerHandler.class.getDeclaredMethod("resolveServerUri");
        method.setAccessible(true);
        Throwable ex = assertThrows(Throwable.class, () -> {
            try {
                method.invoke(handler);
            } catch (java.lang.reflect.InvocationTargetException ite) {
                throw ite.getCause();
            }
        });
        assertTrue(ex instanceof IllegalStateException);
    }

    @Test
    void testStateTransition_ConfiguredStateStartsTasks() throws Exception {
        // Setup
        TaskManagerInterface mockTaskManager = mock(TaskManagerInterface.class);
        Map<String, AbstractTask> tasks = new HashMap<>();
        when(mockTaskManager.initializeTasks(any(), any(), any(), any(), any(), any())).thenReturn(tasks);

        Configuration config = new Configuration();
        config.hostname = "test-server";
        config.port = 8096;
        config.ssl = false;
        config.token = "test-token";

        Thing mockThing = mock(Thing.class);
        Map<String, String> props = new HashMap<>();
        props.put(Constants.ServerProperties.SERVER_URI, "http://test-server:8096");
        when(mockThing.getProperties()).thenReturn(props);

        TestServerHandler testHandler = new TestServerHandler(TestServerHandler.setConfigForCtor(config), mockThing,
                mockTaskManager);

        // Use reflection to call setState
        var setStateMethod = ServerHandler.class.getDeclaredMethod("setState", ServerState.class);
        setStateMethod.setAccessible(true);

        // Get the scheduler field to pass to processStateChange
        var schedulerField = org.openhab.core.thing.binding.BaseThingHandler.class.getDeclaredField("scheduler");
        schedulerField.setAccessible(true);
        var scheduler = (ScheduledExecutorService) schedulerField.get(testHandler);

        // Act - transition to CONFIGURED state
        setStateMethod.invoke(testHandler, ServerState.CONFIGURED);

        // Assert - verify processStateChange was called with CONFIGURED state
        verify(mockTaskManager).processStateChange(eq(ServerState.CONFIGURED), eq(tasks), anyMap(), eq(scheduler));
    }

    @Test
    void testStateTransition_AuthenticationHappensAfterStateChange() throws Exception {
        // Setup
        TaskManagerInterface mockTaskManager = mock(TaskManagerInterface.class);
        Map<String, AbstractTask> tasks = new HashMap<>();
        when(mockTaskManager.initializeTasks(any(), any(), any(), any(), any(), any())).thenReturn(tasks);

        Configuration config = new Configuration();
        config.hostname = "test-server";
        config.port = 8096;
        config.ssl = false;
        config.token = "test-token";

        Thing mockThing = mock(Thing.class);
        Map<String, String> props = new HashMap<>();
        props.put(Constants.ServerProperties.SERVER_URI, "http://test-server:8096");
        when(mockThing.getProperties()).thenReturn(props);

        // Use the TestServerHandler that properly handles configuration
        TestServerHandler testHandler = new TestServerHandler(TestServerHandler.setConfigForCtor(config), mockThing,
                mockTaskManager);

        // Use reflection to call setState
        var setStateMethod = ServerHandler.class.getDeclaredMethod("setState", ServerState.class);
        setStateMethod.setAccessible(true);

        // Act - transition to CONFIGURED state (which should trigger task management)
        setStateMethod.invoke(testHandler, ServerState.CONFIGURED);

        // Assert - verify processStateChange was called (tasks started by setState)
        verify(mockTaskManager).processStateChange(eq(ServerState.CONFIGURED), any(), any(), any());
    }

    @Test
    void testGetState_ReturnsCurrentState() throws Exception {
        // Setup
        TaskManagerInterface mockTaskManager = mock(TaskManagerInterface.class);
        when(mockTaskManager.initializeTasks(any(), any(), any(), any(), any(), any())).thenReturn(new HashMap<>());

        Configuration config = new Configuration();
        config.hostname = "test-server";
        config.port = 8096;
        config.ssl = false;

        Thing mockThing = mock(Thing.class);
        Map<String, String> props = new HashMap<>();
        when(mockThing.getProperties()).thenReturn(props);

        // Use the TestServerHandler that properly handles configuration
        TestServerHandler testHandler = new TestServerHandler(TestServerHandler.setConfigForCtor(config), mockThing,
                mockTaskManager);

        // Use reflection to set state
        var stateField = ServerHandler.class.getDeclaredField("state");
        stateField.setAccessible(true);
        stateField.set(testHandler, ServerState.CONNECTED);

        // Act
        ServerState currentState = testHandler.getState();

        // Assert
        assertEquals(ServerState.CONNECTED, currentState);
    }

    @Test
    void testUpdateConfiguration_SystemInfo_UpdatesBothProperties() throws Exception {
        // Setup
        TaskManagerInterface mockTaskManager = mock(TaskManagerInterface.class);
        when(mockTaskManager.initializeTasks(any(), any(), any(), any(), any(), any())).thenReturn(new HashMap<>());

        Configuration config = new Configuration();
        config.serverName = "";
        config.hostname = "old-hostname";
        config.port = 8096;
        config.ssl = false;
        config.token = "test-token";

        Thing mockThing = mock(Thing.class);
        Map<String, String> props = new HashMap<>();
        when(mockThing.getProperties()).thenReturn(props);

        // Create a real Configuration object to capture updates
        org.openhab.core.config.core.Configuration thingConfig = new org.openhab.core.config.core.Configuration();
        when(mockThing.getConfiguration()).thenReturn(thingConfig);

        TestServerHandler testHandler = new TestServerHandler(TestServerHandler.setConfigForCtor(config), mockThing,
                mockTaskManager);

        // Create SystemInfo with new values
        var systemInfo = new org.openhab.binding.jellyfin.internal.api.generated.current.model.SystemInfo();
        systemInfo.setServerName("MyJellyfinServer");
        systemInfo.setLocalAddress("new-hostname");

        // Use reflection to call updateConfiguration(SystemInfo)
        var updateConfigMethod = ServerHandler.class.getDeclaredMethod("updateConfiguration",
                org.openhab.binding.jellyfin.internal.api.generated.current.model.SystemInfo.class);
        updateConfigMethod.setAccessible(true);

        // Act
        updateConfigMethod.invoke(testHandler, systemInfo);

        // Assert - verify both serverName and hostname were updated in the configuration object
        assertEquals("MyJellyfinServer", config.serverName);
        assertEquals("new-hostname", config.hostname);
    }

    @Test
    void testUpdateConfiguration_SystemInfo_PreservesUserSetServerName() throws Exception {
        // Setup
        TaskManagerInterface mockTaskManager = mock(TaskManagerInterface.class);
        when(mockTaskManager.initializeTasks(any(), any(), any(), any(), any(), any())).thenReturn(new HashMap<>());

        Configuration config = new Configuration();
        config.serverName = "UserSetName"; // User has already set a custom name
        config.hostname = "old-hostname";
        config.port = 8096;
        config.ssl = false;
        config.token = "test-token";

        Thing mockThing = mock(Thing.class);
        Map<String, String> props = new HashMap<>();
        when(mockThing.getProperties()).thenReturn(props);

        org.openhab.core.config.core.Configuration thingConfig = new org.openhab.core.config.core.Configuration();
        when(mockThing.getConfiguration()).thenReturn(thingConfig);

        TestServerHandler testHandler = new TestServerHandler(TestServerHandler.setConfigForCtor(config), mockThing,
                mockTaskManager);

        // Create SystemInfo with different server name
        var systemInfo = new org.openhab.binding.jellyfin.internal.api.generated.current.model.SystemInfo();
        systemInfo.setServerName("ServerAutoName");
        systemInfo.setLocalAddress("new-hostname");

        // Use reflection to call updateConfiguration(SystemInfo)
        var updateConfigMethod = ServerHandler.class.getDeclaredMethod("updateConfiguration",
                org.openhab.binding.jellyfin.internal.api.generated.current.model.SystemInfo.class);
        updateConfigMethod.setAccessible(true);

        // Act
        updateConfigMethod.invoke(testHandler, systemInfo);

        // Assert - verify serverName was preserved (user's custom name takes precedence)
        assertEquals("UserSetName", config.serverName);
        // But hostname should still be updated
        assertEquals("new-hostname", config.hostname);
    }

    @Test
    void testUpdateConfiguration_SystemInfo_NoChangesWhenValuesMatch() throws Exception {
        // Setup
        TaskManagerInterface mockTaskManager = mock(TaskManagerInterface.class);
        when(mockTaskManager.initializeTasks(any(), any(), any(), any(), any(), any())).thenReturn(new HashMap<>());

        Configuration config = new Configuration();
        config.serverName = "ExistingName";
        config.hostname = "existing-hostname";
        config.port = 8096;
        config.ssl = false;
        config.token = "test-token";

        Thing mockThing = mock(Thing.class);
        Map<String, String> props = new HashMap<>();
        when(mockThing.getProperties()).thenReturn(props);

        org.openhab.core.config.core.Configuration thingConfig = new org.openhab.core.config.core.Configuration();
        when(mockThing.getConfiguration()).thenReturn(thingConfig);

        TestServerHandler testHandler = new TestServerHandler(TestServerHandler.setConfigForCtor(config), mockThing,
                mockTaskManager);

        // Create SystemInfo with same values (hostname matches, serverName will be preserved since it's set)
        var systemInfo = new org.openhab.binding.jellyfin.internal.api.generated.current.model.SystemInfo();
        systemInfo.setServerName("DifferentName"); // This won't be applied since config.serverName is not empty
        systemInfo.setLocalAddress("existing-hostname"); // This matches

        // Use reflection to call updateConfiguration(SystemInfo)
        var updateConfigMethod = ServerHandler.class.getDeclaredMethod("updateConfiguration",
                org.openhab.binding.jellyfin.internal.api.generated.current.model.SystemInfo.class);
        updateConfigMethod.setAccessible(true);

        // Spy on the thing to verify updateConfiguration is NOT called
        Thing spyThing = spy(mockThing);
        try {
            java.lang.reflect.Field thingField = org.openhab.core.thing.binding.BaseThingHandler.class
                    .getDeclaredField("thing");
            thingField.setAccessible(true);
            thingField.set(testHandler, spyThing);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set thing field", e);
        }

        // Act
        updateConfigMethod.invoke(testHandler, systemInfo);

        // Assert - verify updateConfiguration was not called (no changes detected)
        verify(spyThing, never()).getConfiguration();
    }
}
