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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.openhab.binding.jellyfin.internal.api.ApiClient;
import org.openhab.binding.jellyfin.internal.discovery.ClientDiscoveryService;
import org.openhab.binding.jellyfin.internal.handler.ServerHandler;
import org.openhab.binding.jellyfin.internal.handler.TaskManager;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.SessionApi;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.SessionInfoDto;
import org.openhab.binding.jellyfin.internal.types.ServerState;
import org.openhab.core.thing.Bridge;

/**
 * Integration-style test that verifies the end-to-end flow:
 * DiscoveryTask.fetchUsers -> ServerHandler.handleUsersList -> ClientListUpdater -> discovery
 */
class DiscoveryIntegrationTest {

    private static class TestServerHandler extends ServerHandler {
        private static org.openhab.binding.jellyfin.internal.Configuration configForCtor;
        private final org.openhab.binding.jellyfin.internal.Configuration testConfig;

        private static org.openhab.binding.jellyfin.internal.Configuration setConfigForCtor(
                org.openhab.binding.jellyfin.internal.Configuration config) {
            configForCtor = config;
            return config;
        }

        TestServerHandler(org.openhab.binding.jellyfin.internal.Configuration config, Bridge bridge,
                ApiClient apiClient, org.openhab.binding.jellyfin.internal.handler.TaskManagerInterface taskManager) {
            super(bridge, apiClient, taskManager);
            this.testConfig = config;
            configForCtor = null;
            // set the 'thing' field in BaseThingHandler
            try {
                java.lang.reflect.Field thingField = org.openhab.core.thing.binding.BaseThingHandler.class
                        .getDeclaredField("thing");
                thingField.setAccessible(true);
                thingField.set(this, bridge);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public <T> T getConfigAs(Class<T> configClass) {
            if (configForCtor != null) {
                return configClass.cast(configForCtor);
            }
            return configClass.cast(testConfig);
        }
    }

    @Test
    void testDiscoveryUpdatesClientsAndTriggersDiscovery() throws Exception {
        // Arrange
        TaskManager taskManager = new TaskManager(new TaskFactory());

        ApiClient mockApiClient = mock(ApiClient.class);
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        when(mockApiClient.getBaseUri()).thenReturn("http://localhost");
        when(mockApiClient.getRequestInterceptor()).thenReturn(null);
        when(mockApiClient.getHttpClient()).thenReturn(mockHttpClient);
        when(mockResponse.statusCode()).thenReturn(200);
        // Single user JSON with Id and Policy fields that will be parsed into UserDto
        String userJson = """
                [{
                    "Id": "11111111-1111-1111-1111-111111111111",
                    "Policy": {
                        "IsDisabled": false,
                        "IsHidden": false
                    }
                }]
                """;
        when(mockResponse.body()).thenReturn(userJson);
        when(mockApiClient.getObjectMapper())
                .thenReturn(org.openhab.binding.jellyfin.internal.api.ApiClient.createDefaultObjectMapper());
        // Make HttpClient.send return our mock response
        when(mockHttpClient.send(any(), any())).thenAnswer(invocation -> mockResponse);

        // Prepare a session that matches the user id
        SessionInfoDto session = new SessionInfoDto();
        session.setId("session-1");
        session.setUserId(UUID.fromString("11111111-1111-1111-1111-111111111111"));

        // Mock construction of SessionApi so ClientListUpdater's new SessionApi(apiClient) returns a mock
        try (MockedConstruction<SessionApi> mocked = mockConstruction(SessionApi.class,
                (mock, ctx) -> when(mock.getSessions(any(), any(), any(), any()))
                        .thenReturn(Collections.singletonList(session)))) {

            // Create test server handler
            org.openhab.binding.jellyfin.internal.Configuration cfg = mock(
                    org.openhab.binding.jellyfin.internal.Configuration.class);
            Bridge mockBridge = mock(Bridge.class);
            // Mock bridge status to return ONLINE so DiscoveryTask proceeds
            when(mockBridge.getStatus()).thenReturn(org.openhab.core.thing.ThingStatus.ONLINE);
            TestServerHandler handler = new TestServerHandler(TestServerHandler.setConfigForCtor(cfg), mockBridge,
                    mockApiClient, taskManager);

            // Set handler state to CONNECTED so DiscoveryTask will be started/considered
            java.lang.reflect.Field stateField = ServerHandler.class.getDeclaredField("state");
            stateField.setAccessible(true);
            stateField.set(handler, ServerState.CONNECTED);

            ClientDiscoveryService discoveryService = mock(ClientDiscoveryService.class);

            // Act - register discovery service (this creates and registers the DiscoveryTask)
            handler.onDiscoveryServiceInitialized(discoveryService);

            // Access the discovery task from the handler's private task map and run it
            java.lang.reflect.Field tasksField = ServerHandler.class.getDeclaredField("tasks");
            tasksField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<String, org.openhab.binding.jellyfin.internal.handler.tasks.AbstractTask> tasks = (java.util.Map<String, org.openhab.binding.jellyfin.internal.handler.tasks.AbstractTask>) tasksField
                    .get(handler);

            org.openhab.binding.jellyfin.internal.handler.tasks.AbstractTask discoveryTask = tasks
                    .get(DiscoveryTask.TASK_ID);
            assertNotNull(discoveryTask, "DiscoveryTask should be present in task map");

            discoveryTask.run();

            // Assert - discovery was triggered twice:
            // 1. From updateClientList() after processing users
            // 2. From DiscoveryTask.run() after fetching users
            verify(discoveryService, times(2)).discoverClients();

            // Assert - clients map now contains our session
            java.util.Map<String, org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.SessionInfoDto> clients = handler
                    .getClients();
            assertTrue(clients.containsKey("session-1"));
            assertEquals(session.getUserId(), clients.get("session-1").getUserId());
        }
    }
}
