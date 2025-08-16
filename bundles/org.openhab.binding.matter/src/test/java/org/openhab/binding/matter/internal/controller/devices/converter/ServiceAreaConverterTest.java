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
package org.openhab.binding.matter.internal.controller.devices.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BaseCluster.Locationdesc;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ServiceAreaCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ServiceAreaCluster.AreaInfoStruct;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ServiceAreaCluster.AreaStruct;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.StateDescription;

/**
 * Tests for {@link ServiceAreaConverter}
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class ServiceAreaConverterTest extends BaseMatterConverterTest {

    @Mock
    @NonNullByDefault({})
    private ServiceAreaCluster mockCluster;
    @NonNullByDefault({})
    private ServiceAreaConverter converter;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        List<ServiceAreaCluster.AreaStruct> areas = new ArrayList<>();
        AreaInfoStruct info1 = new AreaInfoStruct(new Locationdesc("Kitchen", null, 47), null);
        AreaInfoStruct info2 = new AreaInfoStruct(new Locationdesc("Bathroom", null, 6), null);
        areas.add(new AreaStruct(2, 0, info1));
        areas.add(new AreaStruct(3, 0, info2));
        mockCluster.supportedAreas = areas;
        mockCluster.currentArea = 2;
        converter = new ServiceAreaConverter(mockCluster, mockHandler, 1, "Vacuum");
    }

    @Test
    @SuppressWarnings("null")
    void testCreateChannels() {
        ChannelGroupUID groupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(groupUID);
        assertEquals(2, channels.size());
    }

    @Test
    void testHandleSelectAreaCommand() {
        ChannelUID uid = new ChannelUID("matter:node:test:12345:1#servicearea-selectedarea-3");
        converter.handleCommand(uid, OnOffType.ON);
        ArgumentCaptor<org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand> capt = ArgumentCaptor
                .forClass(org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand.class);
        verify(mockHandler, times(1)).sendClusterCommand(eq(1), eq(ServiceAreaCluster.CLUSTER_NAME), capt.capture());
        String payload = capt.getValue().toString();
        assertEquals(true, payload.contains("2") && payload.contains("3"));
    }

    @Test
    void testOnEvent() {
        AttributeChangedMessage msg = new AttributeChangedMessage();
        msg.path = new Path();
        msg.path.attributeName = "selectedAreas";
        msg.value = List.of(3);
        converter.onEvent(msg);
        verify(mockHandler, times(1)).updateState(eq(1), eq("servicearea-selectedarea-2"), eq(OnOffType.OFF));
        verify(mockHandler, times(1)).updateState(eq(1), eq("servicearea-selectedarea-3"), eq(OnOffType.ON));
    }

    @Test
    void testInitState() {
        converter.initState();
        verify(mockHandler, times(1)).updateState(eq(1), eq("servicearea-selectedarea-2"), eq(OnOffType.OFF));
    }

    @Test
    void testClusterDeserialization() {
        String json = """
                    {
                        "id": 336,
                        "name": "ServiceArea",
                        "supportedAreas": [
                            {
                                "areaId": 1,
                                "mapId": 0,
                                "areaInfo": {
                                    "locationInfo": {
                                        "locationName": "Corridor",
                                        "floorNumber": null,
                                        "areaType": 16
                                    },
                                    "landmarkInfo": null
                                }
                            },
                            {
                                "areaId": 2,
                                "mapId": 0,
                                "areaInfo": {
                                    "locationInfo": {
                                        "locationName": "Kitchen",
                                        "floorNumber": null,
                                        "areaType": 47
                                    },
                                    "landmarkInfo": null
                                }
                            },
                            {
                                "areaId": 3,
                                "mapId": 0,
                                "areaInfo": {
                                    "locationInfo": {
                                        "locationName": "Bathroom",
                                        "floorNumber": null,
                                        "areaType": 6
                                    },
                                    "landmarkInfo": null
                                }
                            },
                            {
                                "areaId": 4,
                                "mapId": 0,
                                "areaInfo": {
                                    "locationInfo": {
                                        "locationName": "Living room",
                                        "floorNumber": null,
                                        "areaType": 52
                                    },
                                    "landmarkInfo": null
                                }
                            },
                            {
                                "areaId": 5,
                                "mapId": 0,
                                "areaInfo": {
                                    "locationInfo": {
                                        "locationName": "Dining room",
                                        "floorNumber": null,
                                        "areaType": 21
                                    },
                                    "landmarkInfo": null
                                }
                            },
                            {
                                "areaId": 6,
                                "mapId": 0,
                                "areaInfo": {
                                    "locationInfo": null,
                                    "landmarkInfo": {
                                        "landmarkTag": 46,
                                        "relativePositionTag": 2
                                    }
                                }
                            }
                        ],
                        "selectedAreas": [],
                        "clusterRevision": 1,
                        "featureMap": {
                            "selectWhileRunning": false,
                            "progressReporting": false,
                            "maps": true
                        },
                        "attributeList": [
                            0,
                            1,
                            2,
                            65528,
                            65529,
                            65531,
                            65532,
                            65533
                        ],
                        "acceptedCommandList": [
                            0
                        ],
                        "generatedCommandList": [
                            1
                        ],
                        "supportedMaps": [
                            {
                                "mapId": 0,
                                "name": "1.0 OG"
                            },
                            {
                                "mapId": 1,
                                "name": "1.5 OG"
                            }
                        ]
                    }
                """;

        ServiceAreaCluster sac = mockBridgeClient.getGson().fromJson(json, ServiceAreaCluster.class);

        assertNotNull(sac);
        assertEquals(336, sac.id);
        assertEquals("ServiceArea", sac.name);
        assertEquals(1, sac.clusterRevision);
    }
}
