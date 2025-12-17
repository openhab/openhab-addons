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
package org.openhab.binding.homekit.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homekit.internal.dto.Accessories;
import org.openhab.binding.homekit.internal.dto.Accessory;
import org.openhab.binding.homekit.internal.persistence.HomekitTypeProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.thing.type.ChannelGroupDefinition;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.osgi.framework.Bundle;

import com.google.gson.Gson;

/**
 * Test cases for loading channel creation data from JSON provided by Aqara presence sensors.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class TestChannelCreationForAqaraJson {

    // Aqara JSON dump
    private static final String TEST_JSON = """
            {
                "accessories": [
                    {
                        "aid": 1,
                        "services": [
                            {
                                "iid": 1,
                                "type": "3E",
                                "primary": false,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 2,
                                        "type": "14",
                                        "format": "bool",
                                        "perms": [
                                            "pw"
                                        ]
                                    },
                                    {
                                        "iid": 3,
                                        "type": "20",
                                        "format": "string",
                                        "value": "Aqara",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 4,
                                        "type": "21",
                                        "format": "string",
                                        "value": "PS-S02E",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 5,
                                        "type": "23",
                                        "format": "string",
                                        "value": "Presence-Sensor-FP2-DB0B",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 6,
                                        "type": "30",
                                        "format": "string",
                                        "value": "54EF447BDB0B",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 7,
                                        "type": "52",
                                        "format": "string",
                                        "value": "1.3.3",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 8,
                                        "type": "53",
                                        "format": "string",
                                        "value": "1.0.0",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 9,
                                        "type": "34AB8811-AC7F-4340-BAC3-FD6A85F9943B",
                                        "format": "string",
                                        "value": "6.1;6.1",
                                        "perms": [
                                            "pr",
                                            "hd"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 10,
                                        "type": "220",
                                        "format": "data",
                                        "value": "xDsGOzOmv1k=",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "maxDataLen": 8
                                    }
                                ]
                            },
                            {
                                "iid": 16,
                                "type": "A2",
                                "primary": false,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 18,
                                        "type": "37",
                                        "format": "string",
                                        "value": "1.1.0",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    }
                                ]
                            },
                            {
                                "iid": 64,
                                "type": "22A",
                                "primary": false,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 66,
                                        "type": "22B",
                                        "format": "bool",
                                        "value": 1,
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 67,
                                        "type": "22C",
                                        "format": "uint32",
                                        "value": 9,
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "minValue": 0,
                                        "maxValue": 15,
                                        "minStep": 1
                                    },
                                    {
                                        "iid": 68,
                                        "type": "22D",
                                        "format": "tlv8",
                                        "value": "",
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev",
                                            "tw",
                                            "wr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    }
                                ]
                            },
                            {
                                "iid": 2560,
                                "type": "239",
                                "primary": false,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 2562,
                                        "type": "23C",
                                        "format": "data",
                                        "value": "",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "maxDataLen": 0
                                    }
                                ]
                            },
                            {
                                "iid": 80,
                                "type": "9715BF53-AB63-4449-8DC7-2785D617390A",
                                "primary": false,
                                "hidden": true,
                                "characteristics": [
                                    {
                                        "iid": 81,
                                        "type": "7D943F6A-E052-4E96-A176-D17BF00E32CB",
                                        "format": "int",
                                        "value": -1,
                                        "perms": [
                                            "pr",
                                            "ev",
                                            "hd"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "description": "Firmware Update Status",
                                        "minValue": -128,
                                        "maxValue": 127,
                                        "minStep": 1
                                    },
                                    {
                                        "iid": 82,
                                        "type": "A45EFD52-0DB5-4C1A-9727-513FBCD8185F",
                                        "format": "string",
                                        "perms": [
                                            "pw",
                                            "hd"
                                        ],
                                        "description": "Firmware Update URL",
                                        "maxLen": 256
                                    },
                                    {
                                        "iid": 83,
                                        "type": "40F0124A-579D-40E4-865E-0EF6740EA64B",
                                        "format": "string",
                                        "perms": [
                                            "pw",
                                            "hd"
                                        ],
                                        "description": "Firmware Update Checksum"
                                    },
                                    {
                                        "iid": 85,
                                        "type": "96BF5F20-2996-4DB6-8D65-0E36314BCB6D",
                                        "format": "string",
                                        "value": "1.3.3",
                                        "perms": [
                                            "pr",
                                            "hd"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "description": "Firmware Version"
                                    },
                                    {
                                        "iid": 84,
                                        "type": "36B7A28B-3200-4783-A3FB-6714F11B1417",
                                        "format": "string",
                                        "value": "lumi.motion.agl001",
                                        "perms": [
                                            "pr",
                                            "hd"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "description": "Device Model"
                                    },
                                    {
                                        "iid": 86,
                                        "type": "F5329CB1-A50B-4225-BA9B-331449E7F7A9",
                                        "format": "uint8",
                                        "value": 1,
                                        "perms": [
                                            "pr",
                                            "hd"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "description": "Selected IoT Platform",
                                        "minValue": 0,
                                        "maxValue": 4,
                                        "minStep": 1
                                    }
                                ]
                            },
                            {
                                "iid": 96,
                                "type": "F49132D1-12DF-4119-87D3-A93E8D68531E",
                                "primary": false,
                                "hidden": true,
                                "characteristics": [
                                    {
                                        "iid": 101,
                                        "type": "23",
                                        "format": "string",
                                        "value": "AIOT",
                                        "perms": [
                                            "pr",
                                            "hd"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "description": "Name"
                                    },
                                    {
                                        "iid": 97,
                                        "type": "25D889CB-7135-4A29-B5B4-C1FFD6D2DD5C",
                                        "format": "string",
                                        "value": "",
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "hd"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "description": "Country Domain"
                                    },
                                    {
                                        "iid": 98,
                                        "type": "C7EECAA7-91D9-40EB-AD0C-FFDDE3143CB9",
                                        "format": "string",
                                        "value": "lumi1.54ef447bdb0b",
                                        "perms": [
                                            "pr",
                                            "hd"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "description": "AIOT did"
                                    },
                                    {
                                        "iid": 99,
                                        "type": "80FA747E-CB45-45A4-B7BE-AA7D9964859E",
                                        "format": "string",
                                        "perms": [
                                            "pw",
                                            "hd"
                                        ],
                                        "description": "AIOT bindkey"
                                    },
                                    {
                                        "iid": 100,
                                        "type": "C3B8A329-EF0C-4739-B773-E5B7AEA52C71",
                                        "format": "bool",
                                        "value": 1,
                                        "perms": [
                                            "pr",
                                            "hd"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "description": "AIOT bindstate"
                                    }
                                ]
                            },
                            {
                                "iid": 2672,
                                "type": "84",
                                "primary": false,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 2673,
                                        "type": "23",
                                        "format": "string",
                                        "value": "Light Sensor",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 2674,
                                        "type": "6B",
                                        "format": "float",
                                        "value": 9,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "unit": "lux",
                                        "minValue": 0,
                                        "maxValue": 100000,
                                        "minStep": 1
                                    }
                                ]
                            },
                            {
                                "iid": 2688,
                                "type": "86",
                                "primary": true,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 2689,
                                        "type": "23",
                                        "format": "string",
                                        "value": "Presence Sensor 1",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "maxLen": 20
                                    },
                                    {
                                        "iid": 2690,
                                        "type": "71",
                                        "format": "uint8",
                                        "value": 1,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "minValue": 0,
                                        "maxValue": 1,
                                        "minStep": 1
                                    },
                                    {
                                        "iid": 2691,
                                        "type": "C8622A33-826A-4DD3-9BE9-D496361F29BB",
                                        "format": "uint8",
                                        "value": 0,
                                        "perms": [
                                            "pr",
                                            "hd"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "description": "Sensor Index",
                                        "minValue": 0,
                                        "maxValue": 30,
                                        "minStep": 1
                                    }
                                ]
                            },
                            {
                                "iid": 2692,
                                "type": "86",
                                "primary": false,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 2693,
                                        "type": "23",
                                        "format": "string",
                                        "value": "Presence Sensor 2",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "maxLen": 20
                                    },
                                    {
                                        "iid": 2694,
                                        "type": "71",
                                        "format": "uint8",
                                        "value": 0,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "minValue": 0,
                                        "maxValue": 1,
                                        "minStep": 1
                                    },
                                    {
                                        "iid": 2695,
                                        "type": "C8622A33-826A-4DD3-9BE9-D496361F29BB",
                                        "format": "uint8",
                                        "value": 1,
                                        "perms": [
                                            "pr",
                                            "hd"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "description": "Sensor Index",
                                        "minValue": 0,
                                        "maxValue": 30,
                                        "minStep": 1
                                    }
                                ]
                            },
                            {
                                "iid": 2696,
                                "type": "86",
                                "primary": false,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 2697,
                                        "type": "23",
                                        "format": "string",
                                        "value": "Presence Sensor 3",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "maxLen": 20
                                    },
                                    {
                                        "iid": 2698,
                                        "type": "71",
                                        "format": "uint8",
                                        "value": 1,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "minValue": 0,
                                        "maxValue": 1,
                                        "minStep": 1
                                    },
                                    {
                                        "iid": 2699,
                                        "type": "C8622A33-826A-4DD3-9BE9-D496361F29BB",
                                        "format": "uint8",
                                        "value": 2,
                                        "perms": [
                                            "pr",
                                            "hd"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "description": "Sensor Index",
                                        "minValue": 0,
                                        "maxValue": 30,
                                        "minStep": 1
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
                                            """;

    private static final Gson GSON = new Gson();

    @Test
    void testGenericJsonParsing() {
        Accessories accessories = GSON.fromJson(TEST_JSON, Accessories.class);
        assertNotNull(accessories);
        assertNotNull(accessories.accessories);
        assertEquals(1, accessories.accessories.size());
        for (Accessory accessory : accessories.accessories) {
            assertNotNull(accessory.aid);
            assertNotNull(accessory.services);
            assertEquals(10, accessory.services.size());
            for (var service : accessory.services) {
                assertNotNull(service.type);
                assertNotNull(service.iid);
                assertNotNull(service.characteristics);
                assertTrue(!service.characteristics.isEmpty());
                for (var characteristic : service.characteristics) {
                    assertNotNull(characteristic.type);
                    assertNotNull(characteristic.iid);
                    assertNotNull(characteristic.perms);
                    assertTrue(!characteristic.perms.isEmpty());
                    assertNotNull(characteristic.format);
                }
            }
        }
    }

    @Test
    void testChannelDefinitions() {
        Accessories accessories = GSON.fromJson(TEST_JSON, Accessories.class);
        assertNotNull(accessories);

        HomekitTypeProvider typeProvider = mock(HomekitTypeProvider.class);
        TranslationProvider i18nProvider = mock(TranslationProvider.class);
        Bundle bundle = mock(Bundle.class);

        Map<ChannelGroupTypeUID, ChannelGroupType> channelGroupTypes = new HashMap<>();
        Map<ChannelTypeUID, ChannelType> channelTypes = new HashMap<>();

        doAnswer(invocation -> {
            ChannelGroupType arg = invocation.getArgument(0);
            channelGroupTypes.put(arg.getUID(), arg);
            return null;
        }).when(typeProvider).putChannelGroupType(any(ChannelGroupType.class));

        doAnswer(invocation -> {
            ChannelType arg = invocation.getArgument(0);
            channelTypes.put(arg.getUID(), arg);
            return null;
        }).when(typeProvider).putChannelType(any(ChannelType.class));

        ThingUID thingUID = new ThingUID("hhh", "aaa", "1234567890abcdef");
        Accessory accessory = accessories.getAccessory(1L);
        assertNotNull(accessory);
        List<ChannelGroupDefinition> channelGroupDefinitions = accessory.getChannelGroupDefinitions(thingUID,
                typeProvider, i18nProvider, bundle);

        assertNotNull(channelGroupDefinitions);
        assertEquals(4, channelGroupDefinitions.size());

        // Check that the channel group definition and its type UID and label are set
        for (ChannelGroupDefinition groupDef : channelGroupDefinitions) {
            assertNotNull(groupDef.getId());
            assertNotNull(groupDef.getTypeUID());
            assertNotNull(groupDef.getLabel());
        }

        // there should be 4 unique channel group types; 1 light sensor, and 3 presence sensors
        assertEquals(4, channelGroupTypes.size());

        // there should be 4 unique channel types; 1 light sensor, and 3 presence sensors
        assertEquals(4, channelTypes.size());

        // check the first presence sensor
        ChannelGroupTypeUID targetChannelGroupTypeUID = new ChannelGroupTypeUID(
                "homekit:channel-group-type-sensor-occupancy-2688-1234567890abcdef-1");
        assertNotNull(targetChannelGroupTypeUID);
        ChannelGroupType channelGroupType = channelGroupTypes.get(targetChannelGroupTypeUID);
        assertNotNull(channelGroupType);
        List<ChannelDefinition> channelDefinitions = channelGroupType.getChannelDefinitions();
        assertNotNull(channelDefinitions);
        assertEquals(1, channelDefinitions.size());
        ChannelDefinition channelDefinition = channelDefinitions.get(0);
        assertNotNull(channelDefinition);
        ChannelTypeUID channelTypeUID = channelDefinition.getChannelTypeUID();
        assertNotNull(channelTypeUID);
        ChannelType channelType = channelTypes.get(channelTypeUID);
        assertNotNull(channelType);
        assertEquals("channel-type-occupancy-detected-2690-1234567890abcdef-1", channelType.getUID().getId());

        // check the second presence sensor
        targetChannelGroupTypeUID = new ChannelGroupTypeUID(
                "homekit:channel-group-type-sensor-occupancy-2692-1234567890abcdef-1");
        assertNotNull(targetChannelGroupTypeUID);
        channelGroupType = channelGroupTypes.get(targetChannelGroupTypeUID);
        assertNotNull(channelGroupType);
        channelDefinitions = channelGroupType.getChannelDefinitions();
        assertNotNull(channelDefinitions);
        assertEquals(1, channelDefinitions.size());
        channelDefinition = channelDefinitions.get(0);
        assertNotNull(channelDefinition);
        channelTypeUID = channelDefinition.getChannelTypeUID();
        assertNotNull(channelTypeUID);
        channelType = channelTypes.get(channelTypeUID);
        assertNotNull(channelType);
        assertEquals("channel-type-occupancy-detected-2694-1234567890abcdef-1", channelType.getUID().getId());

        // check the third presence sensor
        targetChannelGroupTypeUID = new ChannelGroupTypeUID(
                "homekit:channel-group-type-sensor-occupancy-2696-1234567890abcdef-1");
        assertNotNull(targetChannelGroupTypeUID);
        channelGroupType = channelGroupTypes.get(targetChannelGroupTypeUID);
        assertNotNull(channelGroupType);
        channelDefinitions = channelGroupType.getChannelDefinitions();
        assertNotNull(channelDefinitions);
        assertEquals(1, channelDefinitions.size());
        channelDefinition = channelDefinitions.get(0);
        assertNotNull(channelDefinition);
        channelTypeUID = channelDefinition.getChannelTypeUID();
        assertNotNull(channelTypeUID);
        channelType = channelTypes.get(channelTypeUID);
        assertNotNull(channelType);
        assertEquals("channel-type-occupancy-detected-2698-1234567890abcdef-1", channelType.getUID().getId());
    }

    @Test
    void testProperties() {
        Accessories accessories = GSON.fromJson(TEST_JSON, Accessories.class);
        assertNotNull(accessories);
        HomekitTypeProvider typeProvider = mock(HomekitTypeProvider.class);
        TranslationProvider i18nProvider = mock(TranslationProvider.class);
        Bundle bundle = mock(Bundle.class);
        Accessory accessory = accessories.getAccessory(1L);
        assertNotNull(accessory);
        ThingUID thingUID = new ThingUID("hhh", "aaa", "1234567890abcdef");
        Map<String, String> properties = accessory.getProperties(thingUID, typeProvider, i18nProvider, bundle);
        assertNotNull(properties);
        assertEquals(7, properties.size());
        String name = properties.get("name");
        assertNotNull(name);
        String[] names = name.split(", ");
        assertEquals(6, names.length);
    }
}
