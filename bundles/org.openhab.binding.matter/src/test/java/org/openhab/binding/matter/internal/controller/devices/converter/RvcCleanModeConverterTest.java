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
import org.mockito.Mock;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.RvcCleanModeCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.RvcCleanModeCluster.ModeOptionStruct;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.StateDescription;

/**
 * Tests for {@link RvcCleanModeConverter}
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class RvcCleanModeConverterTest extends BaseMatterConverterTest {

    @Mock
    @NonNullByDefault({})
    private RvcCleanModeCluster mockCluster;

    @NonNullByDefault({})
    private RvcCleanModeConverter converter;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        List<RvcCleanModeCluster.ModeOptionStruct> modes = new ArrayList<>();
        modes.add(new ModeOptionStruct("Vacuum", 2, null));
        modes.add(new ModeOptionStruct("Mop", 5, null));
        modes.add(new ModeOptionStruct("Vacuum & Mop", 7, null));
        mockCluster.supportedModes = modes;
        mockCluster.currentMode = 7;
        converter = new RvcCleanModeConverter(mockCluster, mockHandler, 1, "Vacuum");
    }

    @Test
    @SuppressWarnings("null")
    void testCreateChannels() {
        ChannelGroupUID groupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(groupUID);
        assertEquals(1, channels.size());
        Channel channel = channels.keySet().iterator().next();
        assertEquals("matter:node:test:12345:1#rvccleanmode-mode", channel.getUID().toString());
    }

    @Test
    void testHandleCommand() {
        ChannelUID uid = new ChannelUID("matter:node:test:12345:1#rvccleanmode-mode");
        converter.handleCommand(uid, new DecimalType(5));
        verify(mockHandler, times(1)).sendClusterCommand(eq(1), eq(RvcCleanModeCluster.CLUSTER_NAME),
                eq(RvcCleanModeCluster.changeToMode(5)));
    }

    @Test
    void testOnEvent() {
        AttributeChangedMessage msg = new AttributeChangedMessage();
        msg.path = new Path();
        msg.path.attributeName = "currentMode";
        msg.value = 2;
        converter.onEvent(msg);
        verify(mockHandler, times(1)).updateState(eq(1), eq("rvccleanmode-mode"), eq(new DecimalType(2)));
    }

    @Test
    void testInitState() {
        converter.initState();
        verify(mockHandler, times(1)).updateState(eq(1), eq("rvccleanmode-mode"), eq(new DecimalType(7)));
    }

    @Test
    void testClusterDeserialization() {
        String json = """
                    {
                        "id": 85,
                        "name": "RvcCleanMode",
                        "supportedModes": [
                            {
                                "label": "Quiet, Vacuum Only",
                                "mode": 1,
                                "modeTags": [
                                    {
                                        "value": 2
                                    },
                                    {
                                        "value": 16385
                                    }
                                ]
                            },
                            {
                                "label": "Auto, Vacuum Only",
                                "mode": 2,
                                "modeTags": [
                                    {
                                        "value": 0
                                    },
                                    {
                                        "value": 16385
                                    }
                                ]
                            },
                            {
                                "label": "Deep Clean, Vacuum Only",
                                "mode": 3,
                                "modeTags": [
                                    {
                                        "value": 16384
                                    },
                                    {
                                        "value": 16385
                                    }
                                ]
                            },
                            {
                                "label": "Quiet, Mop Only",
                                "mode": 4,
                                "modeTags": [
                                    {
                                        "value": 2
                                    },
                                    {
                                        "value": 16386
                                    }
                                ]
                            },
                            {
                                "label": "Auto, Mop Only",
                                "mode": 5,
                                "modeTags": [
                                    {
                                        "value": 0
                                    },
                                    {
                                        "value": 16386
                                    }
                                ]
                            },
                            {
                                "label": "Deep Clean, Mop Only",
                                "mode": 6,
                                "modeTags": [
                                    {
                                        "value": 16384
                                    },
                                    {
                                        "value": 16386
                                    }
                                ]
                            },
                            {
                                "label": "Quiet, Vacuum and Mop",
                                "mode": 7,
                                "modeTags": [
                                    {
                                        "value": 2
                                    },
                                    {
                                        "value": 16385
                                    },
                                    {
                                        "value": 16386
                                    }
                                ]
                            },
                            {
                                "label": "Auto, Vacuum and Mop",
                                "mode": 8,
                                "modeTags": [
                                    {
                                        "value": 0
                                    },
                                    {
                                        "value": 16385
                                    },
                                    {
                                        "value": 16386
                                    }
                                ]
                            },
                            {
                                "label": "Deep Clean, Vacuum and Mop",
                                "mode": 9,
                                "modeTags": [
                                    {
                                        "value": 16384
                                    },
                                    {
                                        "value": 16385
                                    },
                                    {
                                        "value": 16386
                                    }
                                ]
                            }
                        ],
                        "currentMode": 8,
                        "clusterRevision": 3,
                        "featureMap": {
                            "onOff": false
                        },
                        "attributeList": [
                            0,
                            1,
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
                        ]
                    }
                """;

        RvcCleanModeCluster rvc = mockBridgeClient.getGson().fromJson(json, RvcCleanModeCluster.class);

        assertNotNull(rvc);
        assertEquals(85, rvc.id);
        assertEquals("RvcCleanMode", rvc.name);
        assertEquals(3, rvc.clusterRevision);
        assertEquals(false, rvc.featureMap.onOff);
    }
}
