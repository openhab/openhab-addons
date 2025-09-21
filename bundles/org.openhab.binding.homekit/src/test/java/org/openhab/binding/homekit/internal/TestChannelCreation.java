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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homekit.internal.dto.Accessories;
import org.openhab.binding.homekit.internal.dto.Accessory;
import org.openhab.binding.homekit.internal.dto.Characteristic;
import org.openhab.binding.homekit.internal.dto.Service;
import org.openhab.binding.homekit.internal.persistence.HomekitTypeProvider;
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.thing.type.ChannelGroupDefinition;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelType;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * Test cases for loading channel creation data from JSON.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class TestChannelCreation {

    // Chapter 6.6.4 Example Accessory Attribute Database in JSON
    private static final String TEST_JSON = """
            {
                "accessories": [
                    {
                        "aid": 1,
                        "services": [
                            {
                                "type": "3E",
                                "iid": 1,
                                "characteristics": [
                                    {
                                        "type": "23",
                                        "value": "Acme Light Bridge",
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "iid": 2
                                    },
                                    {
                                        "type": "20",
                                        "value": "Acme",
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "iid": 3
                                    },
                                    {
                                        "type": "30",
                                        "value": "037A2BABF19D",
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "iid": 4
                                    },
                                    {
                                        "type": "21",
                                        "value": "Bridge1,1",
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "iid": 5
                                    },
                                    {
                                        "type": "14",
                                        "value": null,
                                        "perms": [
                                            "pw"
                                        ],
                                        "format": "bool",
                                        "iid": 6
                                    },
                                    {
                                        "type": "52",
                                        "value": "100.1.1",
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "iid": 7
                                    }
                                ]
                            },
                            {
                                "type": "A2",
                                "iid": 8,
                                "characteristics": [
                                    {
                                        "type": "37",
                                        "value": "01.01.00",
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "iid": 9
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        "aid": 2,
                        "services": [
                            {
                                "type": "3E",
                                "iid": 1,
                                "characteristics": [
                                    {
                                        "type": "23",
                                        "value": "Acme LED Light Bulb",
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "iid": 2
                                    },
                                    {
                                        "type": "20",
                                        "value": "Acme",
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "iid": 3
                                    },
                                    {
                                        "type": "30",
                                        "value": "099DB48E9E28",
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "iid": 4
                                    },
                                    {
                                        "type": "21",
                                        "value": "LEDBulb1,1",
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "iid": 5
                                    },
                                    {
                                        "type": "14",
                                        "value": null,
                                        "perms": [
                                            "pw"
                                        ],
                                        "format": "bool",
                                        "iid": 6
                                    }
                                ]
                            },
                            {
                                "type": "43",
                                "iid": 7,
                                "characteristics": [
                                    {
                                        "type": "25",
                                        "value": true,
                                        "perms": [
                                            "pr",
                                            "pw"
                                        ],
                                        "format": "bool",
                                        "iid": 8
                                    },
                                    {
                                        "type": "8",
                                        "value": 50,
                                        "perms": [
                                            "pr",
                                            "pw"
                                        ],
                                        "iid": 9,
                                        "maxValue": 100,
                                        "minStep": 1,
                                        "minValue": 20,
                                        "format": "int",
                                        "unit": "percentage"
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        "aid": 3,
                        "services": [
                            {
                                "type": "3E",
                                "iid": 1,
                                "characteristics": [
                                    {
                                        "type": "23",
                                        "value": "Acme LED Light Bulb",
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "iid": 2
                                    },
                                    {
                                        "type": "20",
                                        "value": "Acme",
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "iid": 3
                                    },
                                    {
                                        "type": "30",
                                        "value": "099DB48E9E28",
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "iid": 4
                                    },
                                    {
                                        "type": "21",
                                        "value": "LEDBulb1,1",
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "iid": 5
                                    },
                                    {
                                        "type": "14",
                                        "value": null,
                                        "perms": [
                                            "pw"
                                        ],
                                        "format": "bool",
                                        "iid": 6
                                    }
                                ]
                            },
                            {
                                "type": "43",
                                "iid": 7,
                                "characteristics": [
                                    {
                                        "type": "25",
                                        "value": true,
                                        "perms": [
                                            "pr",
                                            "pw"
                                        ],
                                        "format": "bool",
                                        "iid": 8
                                    },
                                    {
                                        "type": "8",
                                        "value": 50,
                                        "perms": [
                                            "pr",
                                            "pw"
                                        ],
                                        "iid": 9,
                                        "maxValue": 100,
                                        "minStep": 1,
                                        "minValue": 20,
                                        "format": "int",
                                        "unit": "percentage"
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
        assertEquals(3, accessories.accessories.size());
        for (Accessory accessory : accessories.accessories) {
            assertNotNull(accessory.aid);
            assertNotNull(accessory.services);
            assertTrue(!accessory.services.isEmpty());
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
    void testDetailJsonParsing() {
        Accessories accessories = GSON.fromJson(TEST_JSON, Accessories.class);
        assertNotNull(accessories);
        Accessory accessory = accessories.getAccessory(1);
        assertNotNull(accessory);
        assertEquals(1, accessory.aid);
        assertEquals(2, accessory.services.size());
        Service service = accessory.getService(1);
        assertNotNull(service);
        assertEquals("3E", service.type);
        assertEquals(6, service.characteristics.size());
        Characteristic characteristic = service.getCharacteristic(2);
        assertNotNull(characteristic);
        JsonElement value = characteristic.value;
        assertNotNull(value);
        assertTrue(value.isJsonPrimitive());
        assertTrue(value.getAsJsonPrimitive().isString());
        String valueString = value.getAsString();
        assertEquals("Acme Light Bridge", valueString);
    }

    @Test
    void testChannelDefinitions() {
        Accessories accessories = GSON.fromJson(TEST_JSON, Accessories.class);
        assertNotNull(accessories);

        HomekitTypeProvider typeProvider = mock(HomekitTypeProvider.class);
        List<ChannelGroupType> channelGroupTypes = new ArrayList<>();
        List<ChannelType> channelTypes = new ArrayList<>();

        doAnswer(invocation -> {
            ChannelGroupType arg = invocation.getArgument(0);
            channelGroupTypes.add(arg);
            return null;
        }).when(typeProvider).putChannelGroupType(any(ChannelGroupType.class));

        doAnswer(invocation -> {
            ChannelType arg = invocation.getArgument(0);
            channelTypes.add(arg);
            return null;
        }).when(typeProvider).putChannelType(any(ChannelType.class));

        /*
         * Test the LED Light Bulb accessory #3 which has live data channels
         */
        Accessory accessory = accessories.getAccessory(3);
        assertNotNull(accessory);
        List<ChannelGroupDefinition> channelGroupDefinitions = accessory
                .buildAndRegisterChannelGroupDefinitions(typeProvider);

        // There should be one channel group definition for the Light Bulb service and one for the properties
        assertNotNull(channelGroupDefinitions);
        assertEquals(2, channelGroupDefinitions.size());

        // Check that the channel group definition and its type UID and label are set
        for (ChannelGroupDefinition groupDef : channelGroupDefinitions) {
            assertNotNull(groupDef.getId());
            assertNotNull(groupDef.getTypeUID());
            assertNotNull(groupDef.getLabel());
        }

        // There should be one channel group type for the Light Bulb service and one for the properties
        assertEquals(2, channelGroupTypes.size());

        // Check that the public-hap-service-accessory-information channel group type and its UID and label are set
        ChannelGroupType channelGroupType = channelGroupTypes.stream()
                .filter(cgt -> "public-hap-service-accessory-information".equals(cgt.getUID().getId())).findFirst()
                .orElse(null);
        assertNotNull(channelGroupType);
        // There should be four fake channel definitions for the Accessory Information service
        assertEquals(4, channelGroupType.getChannelDefinitions().size());

        // Check the Name fake channel definition
        ChannelDefinition channelDefinition = channelGroupType.getChannelDefinitions().stream()
                .filter(cd -> "name".equals(cd.getId())).findFirst().orElse(null);
        assertNotNull(channelDefinition);
        assertEquals("Acme LED Light Bulb", channelDefinition.getLabel());

        // Check the Serial Number fake channel definition
        channelDefinition = channelGroupType.getChannelDefinitions().stream()
                .filter(cd -> "serialNumber".equals(cd.getId())).findFirst().orElse(null);
        assertNotNull(channelDefinition);
        assertEquals("099DB48E9E28", channelDefinition.getLabel());

        // Check that the channel group type and its UID and label are set
        channelGroupType = channelGroupTypes.stream()
                .filter(cgt -> "public-hap-service-lightbulb".equals(cgt.getUID().getId())).findFirst().orElse(null);
        assertNotNull(channelGroupType);
        assertEquals("Channel group type: Light Bulb", channelGroupType.getLabel());
        assertEquals("public-hap-service-lightbulb", channelGroupType.getUID().getId());

        // There should be two channel definitions for the Light Bulb service: On and Brightness
        assertEquals(2, channelGroupType.getChannelDefinitions().size());

        // Check the Brightness channel definition and its properties
        channelDefinition = channelGroupType.getChannelDefinitions().stream()
                .filter(cd -> "Brightness".equals(cd.getLabel())).findFirst().orElse(null);
        assertNotNull(channelDefinition);
        assertEquals("public-hap-characteristic-brightness", channelDefinition.getChannelTypeUID().getId());
        assertEquals("Brightness", channelDefinition.getLabel());
        assertEquals("percent", channelDefinition.getProperties().get("unit"));
        assertEquals("int", channelDefinition.getProperties().get("format"));
        assertEquals("20.0", channelDefinition.getProperties().get("minValue"));
        assertEquals("100.0", channelDefinition.getProperties().get("maxValue"));
        assertEquals("1.0", channelDefinition.getProperties().get("minStep"));
        assertNotNull(channelDefinition.getProperties().get("perms"));

        // There should be two channel types for the Light Bulb service: On and Brightness
        assertEquals(2, channelTypes.size());

        // Check the Dimmer channel type and its properties
        ChannelType channelType = channelTypes.stream().filter(ct -> "Dimmer".equals(ct.getItemType())).findFirst()
                .orElse(null);
        assertNotNull(channelType);
        assertEquals("public-hap-characteristic-brightness", channelType.getUID().getId());
        assertEquals("Channel type: Brightness", channelType.getLabel());
        assertEquals("Dimmer", channelType.getItemType());
        assertEquals("light", channelType.getCategory());
        assertTrue(channelType.getTags().contains("Control"));
        assertTrue(channelType.getTags().contains("Brightness"));
    }
}
