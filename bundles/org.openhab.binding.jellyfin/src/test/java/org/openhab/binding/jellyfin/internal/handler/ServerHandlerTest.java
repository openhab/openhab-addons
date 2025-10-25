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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.jellyfin.internal.Configuration;
import org.openhab.binding.jellyfin.internal.Constants;
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
        private final Thing testThing;

        private static Configuration setConfigForCtor(Configuration config) {
            configForCtor = config;
            return config;
        }

        TestServerHandler(Configuration config, Thing thing, TaskManagerInterface taskManager) {
            super(mock(org.openhab.core.thing.Bridge.class), null, taskManager);
            this.testConfig = config;
            this.testThing = thing;
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
        when(mockTaskManager.initializeTasks(any(), any(), any(), any())).thenReturn(new java.util.HashMap<>());
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
}
