/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.matter.internal.client.dto.Endpoint;
import org.openhab.binding.matter.internal.client.dto.Node;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.DescriptorCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.LevelControlCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.OccupancySensingCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.OnOffCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.EventTriggeredMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Message;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Test class for the MatterWebsocketClient class.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class MatterWebsocketClientTest {

    @NonNullByDefault({})
    private MatterWebsocketClient client;

    @BeforeEach
    void setUp() {
        client = new MatterWebsocketClient();
    }

    @Test
    void testDeserializeNode() {
        String json = """
                {
                    "id": "1234567890",
                    "rootEndpoint": {
                        "number": 0,
                        "clusters": {
                            "Descriptor": {
                                "id": 29,
                                "name": "Descriptor"
                            },
                            "BasicInformation": {
                                "id": 40,
                                "name": "BasicInformation"
                            }
                        },
                        "children": [
                            {
                                "number": 1,
                                "clusters": {
                                    "Descriptor": {
                                        "id": 29,
                                        "name": "Descriptor"
                                    }
                                }
                            }
                        ]
                    }
                }
                    """;
        Node node = client.getGson().fromJson(json, Node.class);
        assertNotNull(node);
        assertEquals(new BigInteger("1234567890"), node.id);
        assertEquals(1, node.rootEndpoint.children.size());
        Endpoint endpoint = node.rootEndpoint.children.get(0);
        assertNotNull(endpoint);
        assertEquals(1, endpoint.clusters.size());
    }

    @Test
    void testDeserializeAttributeChangedMessage() {
        String json = """
                {
                    "path": {
                        "clusterId": 1,
                        "attributeName": "testAttribute"
                    },
                    "version": 1,
                    "value": "testValue"
                }
                """;
        AttributeChangedMessage message = client.getGson().fromJson(json, AttributeChangedMessage.class);
        assertNotNull(message);
        assertEquals("testAttribute", message.path.attributeName);
        assertEquals(1, message.version);
        assertEquals("testValue", message.value);
    }

    @Test
    void testDeserializeEventTriggeredMessage() {
        String json = """
                {
                    "path": {
                        "clusterId": 1,
                        "eventName": "testEvent"
                    },
                    "events": []
                }
                """;
        EventTriggeredMessage message = client.getGson().fromJson(json, EventTriggeredMessage.class);
        assertNotNull(message);
        assertEquals("testEvent", message.path.eventName);
        assertEquals(0, message.events.length);
    }

    @Test
    void testDeserializeGenericMessage() {
        String json = """
                {
                    "type": "response",
                    "message": {
                        "type": "resultSuccess",
                        "id": "1",
                        "result": {}
                    }
                }
                """;
        Message message = client.getGson().fromJson(json, Message.class);
        assertNotNull(message);
        assertEquals("response", message.type);
    }

    @Test
    void testDeserializeBasicCluster() {
        String json = """
                {
                    "type": "response",
                    "message": {
                        "type": "resultSuccess",
                        "id": "example-id",
                        "result": {
                            "id": "8507467286360628650",
                            "rootEndpoint": {
                                "number": 0,
                                "clusters": {
                                    "Descriptor": {
                                        "id": 29,
                                        "name": "Descriptor",
                                        "deviceTypeList": [
                                            {
                                                "deviceType": 22,
                                                "revision": 1
                                            }
                                        ]
                                    }
                                }
                            }
                        }
                    }
                }
                """;
        Message message = client.getGson().fromJson(json, Message.class);
        assertNotNull(message);
        JsonObject descriptorJson = message.message.getAsJsonObject("result").getAsJsonObject("rootEndpoint")
                .getAsJsonObject("clusters").getAsJsonObject("Descriptor");
        DescriptorCluster descriptorCluster = client.getGson().fromJson(descriptorJson, DescriptorCluster.class);
        assertNotNull(descriptorCluster);
        assertEquals(29, DescriptorCluster.CLUSTER_ID);
        assertEquals("Descriptor", DescriptorCluster.CLUSTER_NAME);
        assertNotNull(descriptorCluster.deviceTypeList);
        assertEquals(1, descriptorCluster.deviceTypeList.size());
        assertEquals(22, descriptorCluster.deviceTypeList.get(0).deviceType);
        assertEquals(1, descriptorCluster.deviceTypeList.get(0).revision);
    }

    @Test
    void testDeserializeOnOffCluster() {
        String json = """
                {
                    "type": "response",
                    "message": {
                        "type": "resultSuccess",
                        "id": "example-id",
                        "result": {
                            "id": "4596455042137293483",
                            "endpoints": {
                                "1": {
                                    "number": 1,
                                    "clusters": {
                                        "OnOff": {
                                            "id": 6,
                                            "name": "OnOff",
                                            "onOff": false,
                                            "clusterRevision": 4,
                                            "featureMap": {
                                                "lighting": true,
                                                "deadFrontBehavior": false,
                                                "offOnly": false
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                """;
        Message message = client.getGson().fromJson(json, Message.class);
        assertNotNull(message);
        JsonObject onOffClusterJson = message.message.getAsJsonObject("result").getAsJsonObject("endpoints")
                .getAsJsonObject("1").getAsJsonObject("clusters").getAsJsonObject("OnOff");
        OnOffCluster onOffCluster = client.getGson().fromJson(onOffClusterJson, OnOffCluster.class);
        assertNotNull(onOffCluster);
        assertEquals(6, onOffCluster.id);
        assertEquals("OnOff", onOffCluster.name);
        assertEquals(false, onOffCluster.onOff);
        assertEquals(4, onOffCluster.clusterRevision);
        assertNotNull(onOffCluster.featureMap);
        assertEquals(true, onOffCluster.featureMap.lighting);
    }

    @Test
    void testDeserializeLevelControlCluster() {
        String json = """
                {
                    "type": "response",
                    "message": {
                        "type": "resultSuccess",
                        "id": "example-id",
                        "result": {
                            "id": "4596455042137293483",
                            "endpoints": {
                                "1": {
                                    "number": 1,
                                    "clusters": {
                                        "LevelControl": {
                                            "id": 8,
                                            "name": "LevelControl",
                                            "currentLevel": 254,
                                            "maxLevel": 254,
                                            "options": {
                                                "executeIfOff": false,
                                                "coupleColorTempToLevel": false
                                            },
                                            "onOffTransitionTime": 5,
                                            "onLevel": 254,
                                            "defaultMoveRate": 50,
                                            "clusterRevision": 5,
                                            "featureMap": {
                                                "onOff": true,
                                                "lighting": true,
                                                "frequency": false
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                """;
        Message message = client.getGson().fromJson(json, Message.class);
        assertNotNull(message);
        JsonObject levelControlClusterJson = message.message.getAsJsonObject("result").getAsJsonObject("endpoints")
                .getAsJsonObject("1").getAsJsonObject("clusters").getAsJsonObject("LevelControl");

        LevelControlCluster levelControlCluster = client.getGson().fromJson(levelControlClusterJson,
                LevelControlCluster.class);
        assertNotNull(levelControlCluster);
        assertEquals(8, levelControlCluster.id);
        assertEquals("LevelControl", levelControlCluster.name);
        assertEquals(254, levelControlCluster.currentLevel);
        assertEquals(5, levelControlCluster.onOffTransitionTime);
        assertNotNull(levelControlCluster.featureMap);
        assertEquals(true, levelControlCluster.featureMap.onOff);
    }

    @Test
    void testDeserializeOccupancyAttributeChangedMessage() {
        String json = """
                {
                    "path": {
                        "nodeId": "4643639431978709653",
                        "endpointId": 6,
                        "clusterId": 1030,
                        "attributeId": 0,
                        "attributeName": "occupancy"
                    },
                    "version": 2038225370,
                    "value": {
                        "occupied": true
                    }
                }
                """;
        AttributeChangedMessage message = client.getGson().fromJson(json, AttributeChangedMessage.class);
        assertNotNull(message);
        assertEquals("occupancy", message.path.attributeName);
        assertEquals(1030, message.path.clusterId);
        assertEquals(0, message.path.attributeId);

        OccupancySensingCluster.OccupancyBitmap occupancyBitmap = (OccupancySensingCluster.OccupancyBitmap) message.value;
        assertNotNull(occupancyBitmap);
        assertEquals(true, occupancyBitmap.occupied);
    }

    /**
     * When sendMessage is called without an established WebSocket session, the returned future must fail fast with a
     * {@link MatterRequestException} ("No valid session") instead of hanging until the timeout. This is the path taken
     * while the node process is (re)starting or after a disconnect.
     */
    @Test
    void testSendMessageWithoutSessionCompletesExceptionally() {
        // No onWebSocketConnect() called, so there is no active session.
        CompletableFuture<JsonElement> future = client.sendMessage("nodes", "listNodes", new Object[0], 1);
        ExecutionException ex = assertThrows(ExecutionException.class, () -> future.get(5, TimeUnit.SECONDS));
        assertInstanceOf(MatterRequestException.class, ex.getCause());
        Throwable cause = ex.getCause();
        assertNotNull(cause);
        assertTrue(cause.getMessage().contains("No valid session"));
    }

    /**
     * A non-positive timeout must fall back to the default request timeout (REQUEST_TIMEOUT_SECONDS), preserving the
     * previous behaviour. We verify this indirectly: with a 0s timeout the request must stay pending well beyond a
     * short
     * wait (here 2s), proving 0 was not used as the actual delay.
     */
    @Test
    void testSendMessageNonPositiveTimeoutFallsBackToDefault() {
        connectMockSession();
        // A timeout <= 0 must fall back to the default, so the request stays pending well beyond 2s.
        CompletableFuture<JsonElement> future = client.sendMessage("nodes", "listNodes", new Object[0], 0);
        assertThrows(TimeoutException.class, () -> future.get(2, TimeUnit.SECONDS));
    }

    /**
     * Installs a mocked WebSocket session on the client so sendMessage reaches the request-scheduling path (instead of
     * failing early with "No valid session"). The remote endpoint is stubbed so the outgoing message is silently
     * discarded; no real socket or node process is involved.
     */
    private void connectMockSession() {
        Session session = mock(Session.class);
        when(session.getPolicy()).thenReturn(WebSocketPolicy.newClientPolicy());
        RemoteEndpoint remote = mock(RemoteEndpoint.class);
        when(session.getRemote()).thenReturn(remote);
        client.onWebSocketConnect(session);
    }
}
