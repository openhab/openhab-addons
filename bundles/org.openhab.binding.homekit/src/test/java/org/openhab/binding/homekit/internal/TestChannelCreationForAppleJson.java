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
import static org.openhab.binding.homekit.internal.HomekitBindingConstants.FAKE_PROPERTY_CHANNEL_TYPE_UID;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homekit.internal.dto.Accessories;
import org.openhab.binding.homekit.internal.dto.Accessory;
import org.openhab.binding.homekit.internal.dto.Characteristic;
import org.openhab.binding.homekit.internal.dto.Service;
import org.openhab.binding.homekit.internal.enums.ServiceType;
import org.openhab.binding.homekit.internal.persistence.HomekitTypeProvider;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.thing.type.ChannelGroupDefinition;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.types.StateDescription;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * Test cases for loading channel creation data from JSON provided in the Apple specification.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class TestChannelCreationForAppleJson {

    // Apple HomeKit Specification Chapter 6.6.4 Example Accessory Attribute Database in JSON
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
        ThingUID thingUID = new ThingUID("hhh", "aaa", "bridge1", "accessory3");
        Accessory accessory = accessories.getAccessory(3);
        assertNotNull(accessory);
        List<ChannelGroupDefinition> channelGroupDefinitions = accessory
                .buildAndRegisterChannelGroupDefinitions(thingUID, typeProvider);

        // There should be just one channel group definition for the Light Bulb service
        assertNotNull(channelGroupDefinitions);
        assertEquals(1, channelGroupDefinitions.size());

        // Check that the channel group definition and its type UID and label are set
        for (ChannelGroupDefinition groupDef : channelGroupDefinitions) {
            assertNotNull(groupDef.getId());
            assertNotNull(groupDef.getTypeUID());
            assertNotNull(groupDef.getLabel());
        }

        // There should be just one channel group type for the Light Bulb service
        assertEquals(1, channelGroupTypes.size());

        // Check that the channel group type and its UID and label are set
        ChannelGroupType channelGroupType = channelGroupTypes.stream()
                .filter(cgt -> "channel-group-type-lightbulb".equals(cgt.getUID().getId())).findFirst().orElse(null);
        assertNotNull(channelGroupType);
        assertEquals("Light Bulb", channelGroupType.getLabel());
        assertEquals("channel-group-type-lightbulb", channelGroupType.getUID().getId());

        // There should be two channel definitions for the Light Bulb service: On and Brightness
        assertEquals(2, channelGroupType.getChannelDefinitions().size());

        // Check the Brightness channel definition and its properties
        ChannelDefinition channelDefinition = channelGroupType.getChannelDefinitions().stream()
                .filter(cd -> "Brightness".equals(cd.getLabel())).findFirst().orElse(null);
        assertNotNull(channelDefinition);
        assertEquals("channel-type-brightness-bridge1-accessory3", channelDefinition.getChannelTypeUID().getId());
        assertEquals("Brightness", channelDefinition.getLabel());
        assertEquals("int", channelDefinition.getProperties().get("format"));

        // There should be two channel types for the Light Bulb service: On and Brightness
        assertEquals(2, channelTypes.size());

        // Check the Dimmer channel type and its properties
        ChannelType channelType = channelTypes.stream().filter(ct -> "Dimmer".equals(ct.getItemType())).findFirst()
                .orElse(null);
        assertNotNull(channelType);
        assertEquals("channel-type-brightness-bridge1-accessory3", channelType.getUID().getId());
        assertEquals("Brightness", channelType.getLabel());
        assertEquals("Dimmer", channelType.getItemType());
        assertEquals("light", channelType.getCategory());
        assertTrue(channelType.getTags().contains("Control"));
        assertTrue(channelType.getTags().contains("Brightness"));

        StateDescription state = channelType.getState();
        assertNotNull(state);
        assertEquals("%.0f %%", state.getPattern());
        assertFalse(state.isReadOnly());
        assertEquals(BigDecimal.valueOf(20.0), state.getMinimum());
        assertEquals(BigDecimal.valueOf(100.0), state.getMaximum());
        assertEquals(BigDecimal.valueOf(1.0), state.getStep());

        // get the accessory information for the bridge (accessory 1) and create properties from it
        accessory = accessories.getAccessory(1);
        assertNotNull(accessory);
        Map<String, String> properties = new HashMap<>();
        for (Service service : accessory.services) {
            if (ServiceType.ACCESSORY_INFORMATION == service.getServiceType()) {
                for (Characteristic characteristic : service.characteristics) {
                    ChannelDefinition channelDef = characteristic.buildAndRegisterChannelDefinition(thingUID,
                            typeProvider);
                    if (channelDef != null && FAKE_PROPERTY_CHANNEL_TYPE_UID.equals(channelDef.getChannelTypeUID())) {
                        String name = channelDef.getId();
                        String value = channelDef.getLabel();
                        if (value != null) {
                            properties.put(name, value);
                        }
                    }
                }
                break;
            }
        }

        // there should be five properties
        assertEquals(5, properties.size());
        assertEquals("Acme Light Bridge", properties.get("name"));
        assertEquals("Acme", properties.get("manufacturer"));
        assertEquals("037A2BABF19D", properties.get("serialNumber"));
        assertEquals("Bridge1,1", properties.get("model"));
        assertEquals("100.1.1", properties.get("firmwareRevision"));
    }
}
