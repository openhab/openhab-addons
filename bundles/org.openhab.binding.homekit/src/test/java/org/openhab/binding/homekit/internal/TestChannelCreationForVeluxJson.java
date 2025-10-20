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
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateOption;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * Test cases for loading channel creation data from JSON provided by a Velux KIG 300.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class TestChannelCreationForVeluxJson {

    // Velux KIG 300 JSON dump
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
                                        "iid": 2,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "VELUX Gateway"
                                    },
                                    {
                                        "type": "20",
                                        "iid": 3,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Netatmo"
                                    },
                                    {
                                        "type": "21",
                                        "iid": 4,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "VELUX Gateway"
                                    },
                                    {
                                        "type": "30",
                                        "iid": 5,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "g373a63"
                                    },
                                    {
                                        "type": "14",
                                        "iid": 6,
                                        "perms": [
                                            "pw"
                                        ],
                                        "format": "bool"
                                    },
                                    {
                                        "type": "52",
                                        "iid": 7,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "202.0.0"
                                    },
                                    {
                                        "type": "220",
                                        "iid": 13,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "data",
                                        "value": "+nvrOv1cCQU="
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "A2",
                                "iid": 8,
                                "characteristics": [
                                    {
                                        "type": "37",
                                        "iid": 9,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "1.1.0"
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "EA22EA53-6227-55EA-AC24-73ACF3EEA0E8",
                                "iid": 65535,
                                "characteristics": [
                                    {
                                        "type": "4D05AE82-5A22-5BD6-A730-B7F8B4F3218D",
                                        "iid": 32,
                                        "perms": [
                                            "pw"
                                        ],
                                        "format": "bool"
                                    },
                                    {
                                        "type": "00F44C18-042E-5C4E-9A4C-561D44DCD804",
                                        "iid": 30,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "g373a63"
                                    }
                                ],
                                "hidden": true,
                                "primary": false
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
                                        "iid": 2,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "VELUX Sensor"
                                    },
                                    {
                                        "type": "20",
                                        "iid": 3,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Netatmo"
                                    },
                                    {
                                        "type": "21",
                                        "iid": 4,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "VELUX Sensor"
                                    },
                                    {
                                        "type": "30",
                                        "iid": 5,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "p005519"
                                    },
                                    {
                                        "type": "14",
                                        "iid": 7,
                                        "perms": [
                                            "pw"
                                        ],
                                        "format": "bool"
                                    },
                                    {
                                        "type": "52",
                                        "iid": 6,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "16.0.0"
                                    },
                                    {
                                        "type": "220",
                                        "iid": 18,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "data",
                                        "value": "+nvrOv1cCQU="
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "8A",
                                "iid": 8,
                                "characteristics": [
                                    {
                                        "type": "23",
                                        "iid": 9,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Temperature sensor"
                                    },
                                    {
                                        "type": "11",
                                        "iid": 10,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "float",
                                        "value": 18.4,
                                        "minValue": 0.0,
                                        "maxValue": 50.0,
                                        "minStep": 0.1,
                                        "unit": "celsius"
                                    }
                                ],
                                "hidden": false,
                                "primary": true
                            },
                            {
                                "type": "82",
                                "iid": 11,
                                "characteristics": [
                                    {
                                        "type": "23",
                                        "iid": 12,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Humidity sensor"
                                    },
                                    {
                                        "type": "10",
                                        "iid": 13,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "float",
                                        "value": 65.0,
                                        "minValue": 0.0,
                                        "maxValue": 100.0,
                                        "minStep": 1.0,
                                        "unit": "percentage"
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "97",
                                "iid": 14,
                                "characteristics": [
                                    {
                                        "type": "23",
                                        "iid": 15,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Carbon Dioxide sensor"
                                    },
                                    {
                                        "type": "92",
                                        "iid": 16,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 0,
                                        "maxValue": 1,
                                        "minValue": 0,
                                        "minStep": 1
                                    },
                                    {
                                        "type": "93",
                                        "iid": 17,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "float",
                                        "value": 846.0,
                                        "minValue": 0.0,
                                        "maxValue": 5000.0
                                    }
                                ],
                                "hidden": false,
                                "primary": false
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
                                        "iid": 2,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "VELUX Sensor"
                                    },
                                    {
                                        "type": "20",
                                        "iid": 3,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Netatmo"
                                    },
                                    {
                                        "type": "21",
                                        "iid": 4,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "VELUX Sensor"
                                    },
                                    {
                                        "type": "30",
                                        "iid": 5,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "p01448d"
                                    },
                                    {
                                        "type": "14",
                                        "iid": 7,
                                        "perms": [
                                            "pw"
                                        ],
                                        "format": "bool"
                                    },
                                    {
                                        "type": "52",
                                        "iid": 6,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "16.0.0"
                                    },
                                    {
                                        "type": "220",
                                        "iid": 18,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "data",
                                        "value": "+nvrOv1cCQU="
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "8A",
                                "iid": 8,
                                "characteristics": [
                                    {
                                        "type": "23",
                                        "iid": 9,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Temperature sensor"
                                    },
                                    {
                                        "type": "11",
                                        "iid": 10,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "float",
                                        "value": 19.7,
                                        "minValue": 0.0,
                                        "maxValue": 50.0,
                                        "minStep": 0.1,
                                        "unit": "celsius"
                                    }
                                ],
                                "hidden": false,
                                "primary": true
                            },
                            {
                                "type": "82",
                                "iid": 11,
                                "characteristics": [
                                    {
                                        "type": "23",
                                        "iid": 12,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Humidity sensor"
                                    },
                                    {
                                        "type": "10",
                                        "iid": 13,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "float",
                                        "value": 60.0,
                                        "minValue": 0.0,
                                        "maxValue": 100.0,
                                        "minStep": 1.0,
                                        "unit": "percentage"
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "97",
                                "iid": 14,
                                "characteristics": [
                                    {
                                        "type": "23",
                                        "iid": 15,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Carbon Dioxide sensor"
                                    },
                                    {
                                        "type": "92",
                                        "iid": 16,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 0,
                                        "maxValue": 1,
                                        "minValue": 0,
                                        "minStep": 1
                                    },
                                    {
                                        "type": "93",
                                        "iid": 17,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "float",
                                        "value": 836.0,
                                        "minValue": 0.0,
                                        "maxValue": 5000.0
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            }
                        ]
                    },
                    {
                        "aid": 4,
                        "services": [
                            {
                                "type": "3E",
                                "iid": 1,
                                "characteristics": [
                                    {
                                        "type": "23",
                                        "iid": 2,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "VELUX Window"
                                    },
                                    {
                                        "type": "20",
                                        "iid": 3,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Netatmo"
                                    },
                                    {
                                        "type": "21",
                                        "iid": 4,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "VELUX Window"
                                    },
                                    {
                                        "type": "30",
                                        "iid": 5,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "5636132610170cda"
                                    },
                                    {
                                        "type": "14",
                                        "iid": 7,
                                        "perms": [
                                            "pw"
                                        ],
                                        "format": "bool"
                                    },
                                    {
                                        "type": "52",
                                        "iid": 6,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "48.0.0"
                                    },
                                    {
                                        "type": "220",
                                        "iid": 13,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "data",
                                        "value": "+nvrOv1cCQU="
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "8B",
                                "iid": 8,
                                "characteristics": [
                                    {
                                        "type": "23",
                                        "iid": 9,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Roof Window"
                                    },
                                    {
                                        "type": "7C",
                                        "iid": 11,
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 0,
                                        "maxValue": 100,
                                        "minValue": 0,
                                        "unit": "percentage",
                                        "minStep": 1
                                    },
                                    {
                                        "type": "6D",
                                        "iid": 10,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 0,
                                        "maxValue": 100,
                                        "minValue": 0,
                                        "unit": "percentage",
                                        "minStep": 1
                                    },
                                    {
                                        "type": "72",
                                        "iid": 12,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 2,
                                        "maxValue": 2,
                                        "minValue": 0,
                                        "minStep": 1
                                    }
                                ],
                                "hidden": false,
                                "primary": true
                            }
                        ]
                    },
                    {
                        "aid": 5,
                        "services": [
                            {
                                "type": "3E",
                                "iid": 1,
                                "characteristics": [
                                    {
                                        "type": "23",
                                        "iid": 2,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "VELUX External Cover"
                                    },
                                    {
                                        "type": "20",
                                        "iid": 3,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Netatmo"
                                    },
                                    {
                                        "type": "21",
                                        "iid": 4,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "VELUX External Cover"
                                    },
                                    {
                                        "type": "30",
                                        "iid": 5,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "56233d26092b0923"
                                    },
                                    {
                                        "type": "14",
                                        "iid": 7,
                                        "perms": [
                                            "pw"
                                        ],
                                        "format": "bool"
                                    },
                                    {
                                        "type": "52",
                                        "iid": 6,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "71.0.0"
                                    },
                                    {
                                        "type": "220",
                                        "iid": 13,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "data",
                                        "value": "+nvrOv1cCQU="
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "8C",
                                "iid": 8,
                                "characteristics": [
                                    {
                                        "type": "23",
                                        "iid": 9,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Roller Shutter"
                                    },
                                    {
                                        "type": "7C",
                                        "iid": 11,
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 100,
                                        "maxValue": 100,
                                        "minValue": 0,
                                        "unit": "percentage",
                                        "minStep": 1
                                    },
                                    {
                                        "type": "6D",
                                        "iid": 10,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 100,
                                        "maxValue": 100,
                                        "minValue": 0,
                                        "unit": "percentage",
                                        "minStep": 1
                                    },
                                    {
                                        "type": "72",
                                        "iid": 12,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 2,
                                        "maxValue": 2,
                                        "minValue": 0,
                                        "minStep": 1
                                    }
                                ],
                                "hidden": false,
                                "primary": true
                            }
                        ]
                    },
                    {
                        "aid": 6,
                        "services": [
                            {
                                "type": "3E",
                                "iid": 1,
                                "characteristics": [
                                    {
                                        "type": "23",
                                        "iid": 2,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "VELUX External Cover"
                                    },
                                    {
                                        "type": "20",
                                        "iid": 3,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Netatmo"
                                    },
                                    {
                                        "type": "21",
                                        "iid": 4,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "VELUX External Cover"
                                    },
                                    {
                                        "type": "30",
                                        "iid": 5,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "56321426101f0e39"
                                    },
                                    {
                                        "type": "14",
                                        "iid": 7,
                                        "perms": [
                                            "pw"
                                        ],
                                        "format": "bool"
                                    },
                                    {
                                        "type": "52",
                                        "iid": 6,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "16.0.0"
                                    },
                                    {
                                        "type": "220",
                                        "iid": 13,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "data",
                                        "value": "+nvrOv1cCQU="
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "8C",
                                "iid": 8,
                                "characteristics": [
                                    {
                                        "type": "23",
                                        "iid": 9,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Roller Shutter"
                                    },
                                    {
                                        "type": "7C",
                                        "iid": 11,
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 100,
                                        "maxValue": 100,
                                        "minValue": 0,
                                        "unit": "percentage",
                                        "minStep": 1
                                    },
                                    {
                                        "type": "6D",
                                        "iid": 10,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 100,
                                        "maxValue": 100,
                                        "minValue": 0,
                                        "unit": "percentage",
                                        "minStep": 1
                                    },
                                    {
                                        "type": "72",
                                        "iid": 12,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 2,
                                        "maxValue": 2,
                                        "minValue": 0,
                                        "minStep": 1
                                    }
                                ],
                                "hidden": false,
                                "primary": true
                            }
                        ]
                    },
                    {
                        "aid": 7,
                        "services": [
                            {
                                "type": "3E",
                                "iid": 1,
                                "characteristics": [
                                    {
                                        "type": "23",
                                        "iid": 2,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "VELUX External Cover"
                                    },
                                    {
                                        "type": "20",
                                        "iid": 3,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Netatmo"
                                    },
                                    {
                                        "type": "21",
                                        "iid": 4,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "VELUX External Cover"
                                    },
                                    {
                                        "type": "30",
                                        "iid": 5,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "56321426101e16af"
                                    },
                                    {
                                        "type": "14",
                                        "iid": 7,
                                        "perms": [
                                            "pw"
                                        ],
                                        "format": "bool"
                                    },
                                    {
                                        "type": "52",
                                        "iid": 6,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "16.0.0"
                                    },
                                    {
                                        "type": "220",
                                        "iid": 13,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "data",
                                        "value": "+nvrOv1cCQU="
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "8C",
                                "iid": 8,
                                "characteristics": [
                                    {
                                        "type": "23",
                                        "iid": 9,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Roller Shutter"
                                    },
                                    {
                                        "type": "7C",
                                        "iid": 11,
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 100,
                                        "maxValue": 100,
                                        "minValue": 0,
                                        "unit": "percentage",
                                        "minStep": 1
                                    },
                                    {
                                        "type": "6D",
                                        "iid": 10,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 100,
                                        "maxValue": 100,
                                        "minValue": 0,
                                        "unit": "percentage",
                                        "minStep": 1
                                    },
                                    {
                                        "type": "72",
                                        "iid": 12,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 2,
                                        "maxValue": 2,
                                        "minValue": 0,
                                        "minStep": 1
                                    }
                                ],
                                "hidden": false,
                                "primary": true
                            }
                        ]
                    },
                    {
                        "aid": 8,
                        "services": [
                            {
                                "type": "3E",
                                "iid": 1,
                                "characteristics": [
                                    {
                                        "type": "23",
                                        "iid": 2,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "VELUX Window"
                                    },
                                    {
                                        "type": "20",
                                        "iid": 3,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Netatmo"
                                    },
                                    {
                                        "type": "21",
                                        "iid": 4,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "VELUX Window"
                                    },
                                    {
                                        "type": "30",
                                        "iid": 5,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "5636135a103004bc"
                                    },
                                    {
                                        "type": "14",
                                        "iid": 7,
                                        "perms": [
                                            "pw"
                                        ],
                                        "format": "bool"
                                    },
                                    {
                                        "type": "52",
                                        "iid": 6,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "48.0.0"
                                    },
                                    {
                                        "type": "220",
                                        "iid": 13,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "data",
                                        "value": "+nvrOv1cCQU="
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "8B",
                                "iid": 8,
                                "characteristics": [
                                    {
                                        "type": "23",
                                        "iid": 9,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Roof Window"
                                    },
                                    {
                                        "type": "7C",
                                        "iid": 11,
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 0,
                                        "maxValue": 100,
                                        "minValue": 0,
                                        "unit": "percentage",
                                        "minStep": 1
                                    },
                                    {
                                        "type": "6D",
                                        "iid": 10,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 0,
                                        "maxValue": 100,
                                        "minValue": 0,
                                        "unit": "percentage",
                                        "minStep": 1
                                    },
                                    {
                                        "type": "72",
                                        "iid": 12,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 2,
                                        "maxValue": 2,
                                        "minValue": 0,
                                        "minStep": 1
                                    }
                                ],
                                "hidden": false,
                                "primary": true
                            }
                        ]
                    },
                    {
                        "aid": 9,
                        "services": [
                            {
                                "type": "3E",
                                "iid": 1,
                                "characteristics": [
                                    {
                                        "type": "23",
                                        "iid": 2,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "VELUX Internal Cover"
                                    },
                                    {
                                        "type": "20",
                                        "iid": 3,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Netatmo"
                                    },
                                    {
                                        "type": "21",
                                        "iid": 4,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "VELUX Internal Cover"
                                    },
                                    {
                                        "type": "30",
                                        "iid": 5,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "56251d261028006a"
                                    },
                                    {
                                        "type": "14",
                                        "iid": 7,
                                        "perms": [
                                            "pw"
                                        ],
                                        "format": "bool"
                                    },
                                    {
                                        "type": "52",
                                        "iid": 6,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "77.0.0"
                                    },
                                    {
                                        "type": "220",
                                        "iid": 15,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "data",
                                        "value": "+nvrOv1cCQU="
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "8C",
                                "iid": 8,
                                "characteristics": [
                                    {
                                        "type": "23",
                                        "iid": 9,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Venetian Blinds"
                                    },
                                    {
                                        "type": "7C",
                                        "iid": 11,
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 100,
                                        "maxValue": 100,
                                        "minValue": 0,
                                        "unit": "percentage",
                                        "minStep": 1
                                    },
                                    {
                                        "type": "6D",
                                        "iid": 10,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 100,
                                        "maxValue": 100,
                                        "minValue": 0,
                                        "unit": "percentage",
                                        "minStep": 1
                                    },
                                    {
                                        "type": "72",
                                        "iid": 12,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 2,
                                        "maxValue": 2,
                                        "minValue": 0,
                                        "minStep": 1
                                    },
                                    {
                                        "type": "6C",
                                        "iid": 13,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "int",
                                        "value": -90,
                                        "maxValue": 90,
                                        "minValue": -90,
                                        "unit": "arcdegrees",
                                        "minStep": 1
                                    },
                                    {
                                        "type": "7B",
                                        "iid": 14,
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev"
                                        ],
                                        "format": "int",
                                        "value": -90,
                                        "maxValue": 90,
                                        "minValue": -90,
                                        "unit": "arcdegrees",
                                        "minStep": 1
                                    }
                                ],
                                "hidden": false,
                                "primary": true
                            }
                        ]
                    },
                    {
                        "aid": 10,
                        "services": [
                            {
                                "type": "3E",
                                "iid": 1,
                                "characteristics": [
                                    {
                                        "type": "23",
                                        "iid": 2,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "VELUX Internal Cover"
                                    },
                                    {
                                        "type": "20",
                                        "iid": 3,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Netatmo"
                                    },
                                    {
                                        "type": "21",
                                        "iid": 4,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "VELUX Internal Cover"
                                    },
                                    {
                                        "type": "30",
                                        "iid": 5,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "56251d26102d0139"
                                    },
                                    {
                                        "type": "14",
                                        "iid": 7,
                                        "perms": [
                                            "pw"
                                        ],
                                        "format": "bool"
                                    },
                                    {
                                        "type": "52",
                                        "iid": 6,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "77.0.0"
                                    },
                                    {
                                        "type": "220",
                                        "iid": 15,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "data",
                                        "value": "+nvrOv1cCQU="
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "8C",
                                "iid": 8,
                                "characteristics": [
                                    {
                                        "type": "23",
                                        "iid": 9,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Venetian Blinds"
                                    },
                                    {
                                        "type": "7C",
                                        "iid": 11,
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 20,
                                        "maxValue": 100,
                                        "minValue": 0,
                                        "unit": "percentage",
                                        "minStep": 1
                                    },
                                    {
                                        "type": "6D",
                                        "iid": 10,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 20,
                                        "maxValue": 100,
                                        "minValue": 0,
                                        "unit": "percentage",
                                        "minStep": 1
                                    },
                                    {
                                        "type": "72",
                                        "iid": 12,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 2,
                                        "maxValue": 2,
                                        "minValue": 0,
                                        "minStep": 1
                                    },
                                    {
                                        "type": "6C",
                                        "iid": 13,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "int",
                                        "value": -90,
                                        "maxValue": 90,
                                        "minValue": -90,
                                        "unit": "arcdegrees",
                                        "minStep": 1
                                    },
                                    {
                                        "type": "7B",
                                        "iid": 14,
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev"
                                        ],
                                        "format": "int",
                                        "value": -90,
                                        "maxValue": 90,
                                        "minValue": -90,
                                        "unit": "arcdegrees",
                                        "minStep": 1
                                    }
                                ],
                                "hidden": false,
                                "primary": true
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
        assertEquals(10, accessories.accessories.size());
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
        assertEquals(3, accessory.services.size());
        Service service = accessory.getService(1);
        assertNotNull(service);
        assertEquals("3E", service.type);
        assertEquals(7, service.characteristics.size());
        Characteristic characteristic = service.getCharacteristic(2);
        assertNotNull(characteristic);
        JsonElement value = characteristic.value;
        assertNotNull(value);
        assertTrue(value.isJsonPrimitive());
        assertTrue(value.getAsJsonPrimitive().isString());
        String valueString = value.getAsString();
        assertEquals("VELUX Gateway", valueString);
    }

    @Test
    void testBridge() {
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

        // get the accessory information for the bridge (accessory 1) and create properties from it
        ThingUID thingUID = new ThingUID("hhh", "aaa", "bridge1", "accessory1");
        Accessory accessory = accessories.getAccessory(1);
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
        assertEquals("VELUX Gateway", properties.get("name"));
        assertEquals("Netatmo", properties.get("manufacturer"));
        assertEquals("g373a63", properties.get("serialNumber"));
        assertEquals("VELUX Gateway", properties.get("model"));
        assertEquals("202.0.0", properties.get("firmwareRevision"));
    }

    @Test
    void testSensors() {
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

        // test channel definitions for Temperature, Humidity, and CO2 sensors
        ThingUID thingUID = new ThingUID("hhh", "aaa", "bridge1", "accessory2");
        Accessory accessory = accessories.getAccessory(2);
        assertNotNull(accessory);
        List<ChannelGroupDefinition> channelGroupDefinitions = accessory
                .buildAndRegisterChannelGroupDefinitions(thingUID, typeProvider);

        // There should be three channel group definitions for the temperature, humidity and co2 sensors
        assertNotNull(channelGroupDefinitions);
        assertEquals(3, channelGroupDefinitions.size());

        // There should be four channel types for the temperature, humidity, co2 sensors and co2 detector
        assertEquals(4, channelTypes.size());

        // There should be three channel group types for the temperature, humidity and co2 sensors
        assertEquals(3, channelGroupTypes.size());

        // check the temperature sensor
        ChannelGroupType groupType = channelGroupTypes.get(0);
        assertNotNull(groupType);

        // Check the temperature sensor channel definition and properties
        ChannelDefinition channelDefinition = groupType.getChannelDefinitions().stream()
                .filter(cd -> "10".equals(cd.getProperties().get("iid"))).findFirst().orElse(null);
        assertNotNull(channelDefinition);
        assertEquals("Temperature Current", channelDefinition.getLabel());

        ChannelType channelType = channelTypes.stream().filter(ct -> "Temperature Current".equals(ct.getLabel()))
                .findFirst().orElse(null);
        assertNotNull(channelType);
        assertEquals("Number:Temperature", channelType.getItemType());
        assertEquals(ChannelKind.STATE, channelType.getKind());
        assertTrue(channelType.getTags().contains("Temperature"));
        assertTrue(channelType.getTags().contains("Measurement"));
        assertEquals("°C", channelType.getUnitHint());
        StateDescription state = channelType.getState();
        assertNotNull(state);
        BigDecimal max = state.getMaximum();
        BigDecimal min = state.getMinimum();
        BigDecimal step = state.getStep();
        assertNotNull(max);
        assertNotNull(min);
        assertNotNull(step);
        assertEquals(50.0, max.doubleValue());
        assertEquals(0.0, min.doubleValue());
        assertEquals(0.1, step.doubleValue());
        assertTrue(state.isReadOnly());

        // check the humidity sensor
        groupType = channelGroupTypes.get(1);
        assertNotNull(groupType);

        // Check the humidity sensor channel definition and properties
        channelDefinition = groupType.getChannelDefinitions().stream()
                .filter(cd -> "13".equals(cd.getProperties().get("iid"))).findFirst().orElse(null);
        assertNotNull(channelDefinition);
        assertEquals("Relative Humidity Current", channelDefinition.getLabel());

        channelType = channelTypes.stream().filter(ct -> "Relative Humidity Current".equals(ct.getLabel())).findFirst()
                .orElse(null);
        assertNotNull(channelType);
        assertEquals("Number:Dimensionless", channelType.getItemType());
        assertEquals(ChannelKind.STATE, channelType.getKind());
        assertTrue(channelType.getTags().contains("Humidity"));
        assertTrue(channelType.getTags().contains("Measurement"));
        assertEquals("%", channelType.getUnitHint());
        state = channelType.getState();
        assertNotNull(state);
        max = state.getMaximum();
        min = state.getMinimum();
        step = state.getStep();
        assertNotNull(max);
        assertNotNull(min);
        assertNotNull(step);
        assertEquals(100.0, max.doubleValue());
        assertEquals(0.0, min.doubleValue());
        assertEquals(1.0, step.doubleValue());
        assertTrue(state.isReadOnly());

        // check the co2 sensor
        groupType = channelGroupTypes.get(2);
        assertNotNull(groupType);

        // Check the co2 detected channel definition and properties
        channelDefinition = groupType.getChannelDefinitions().stream()
                .filter(cd -> "16".equals(cd.getProperties().get("iid"))).findFirst().orElse(null);
        assertNotNull(channelDefinition);
        assertEquals("Carbon Dioxide Detected", channelDefinition.getLabel());

        channelType = channelTypes.stream().filter(ct -> "Carbon Dioxide Detected".equals(ct.getLabel())).findFirst()
                .orElse(null);
        assertNotNull(channelType);
        assertEquals("Contact", channelType.getItemType());
        assertEquals(ChannelKind.STATE, channelType.getKind());
        assertTrue(channelType.getTags().contains("Alarm"));
        assertTrue(channelType.getTags().contains("CO2"));
        state = channelType.getState();
        assertNotNull(state);
        max = state.getMaximum();
        min = state.getMinimum();
        step = state.getStep();
        assertNotNull(max);
        assertNotNull(min);
        assertNotNull(step);
        assertEquals(1.0, max.doubleValue());
        assertEquals(0.0, min.doubleValue());
        assertEquals(1.0, step.doubleValue());
        assertTrue(state.isReadOnly());

        // Check the co2 level channel definition and properties
        channelDefinition = groupType.getChannelDefinitions().stream()
                .filter(cd -> "17".equals(cd.getProperties().get("iid"))).findFirst().orElse(null);
        assertNotNull(channelDefinition);
        assertEquals("Carbon Dioxide Level", channelDefinition.getLabel());

        channelType = channelTypes.stream().filter(ct -> "Carbon Dioxide Level".equals(ct.getLabel())).findFirst()
                .orElse(null);
        assertNotNull(channelType);
        assertEquals("Number:Dimensionless", channelType.getItemType());
        assertEquals(ChannelKind.STATE, channelType.getKind());
        assertTrue(channelType.getTags().contains("CO2"));
        assertTrue(channelType.getTags().contains("Measurement"));
        assertEquals("ppm", channelType.getUnitHint());
        state = channelType.getState();
        assertNotNull(state);
        max = state.getMaximum();
        min = state.getMinimum();
        step = state.getStep();
        assertNotNull(max);
        assertNotNull(min);
        assertNull(step);
        assertEquals(5000.0, max.doubleValue());
        assertEquals(0.0, min.doubleValue());
        assertTrue(state.isReadOnly());
    }

    @Test
    void testVenetianBlind() {
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

        ThingUID thingUID = new ThingUID("hhh", "aaa", "bridge1", "accessory9");
        Accessory accessory = accessories.getAccessory(9);
        assertNotNull(accessory);
        List<ChannelGroupDefinition> channelGroupDefinitions = accessory
                .buildAndRegisterChannelGroupDefinitions(thingUID, typeProvider);

        // There should be one channel group definition for the blind
        assertNotNull(channelGroupDefinitions);
        assertEquals(1, channelGroupDefinitions.size());

        // There should be five channel types for position target/actual, tilt target/actual, and state
        assertEquals(5, channelTypes.size());

        // There should be one channel group type for the blind
        assertEquals(1, channelGroupTypes.size());

        // check the channels for the blind
        ChannelGroupType groupType = channelGroupTypes.get(0);
        assertNotNull(groupType);

        // target position channel
        ChannelDefinition channelDefinition = groupType.getChannelDefinitions().stream()
                .filter(cd -> "11".equals(cd.getProperties().get("iid"))).findFirst().orElse(null);
        assertNotNull(channelDefinition);
        assertEquals("Position Target", channelDefinition.getLabel());

        ChannelType channelType = channelTypes.stream().filter(ct -> "Position Target".equals(ct.getLabel()))
                .findFirst().orElse(null);
        assertNotNull(channelType);
        assertEquals("Rollershutter", channelType.getItemType());
        assertEquals(ChannelKind.STATE, channelType.getKind());
        assertTrue(channelType.getTags().contains("Control"));
        assertTrue(channelType.getTags().contains("Opening"));
        assertNull(channelType.getUnitHint());
        StateDescription state = channelType.getState();
        assertNotNull(state);
        BigDecimal max = state.getMaximum();
        BigDecimal min = state.getMinimum();
        BigDecimal step = state.getStep();
        assertNotNull(max);
        assertNotNull(min);
        assertNotNull(step);
        assertEquals(100.0, max.doubleValue());
        assertEquals(0.0, min.doubleValue());
        assertEquals(1.0, step.doubleValue());
        assertFalse(state.isReadOnly());

        // current position channel
        channelDefinition = groupType.getChannelDefinitions().stream()
                .filter(cd -> "10".equals(cd.getProperties().get("iid"))).findFirst().orElse(null);
        assertNotNull(channelDefinition);
        assertEquals("Position Current", channelDefinition.getLabel());

        channelType = channelTypes.stream().filter(ct -> "Position Current".equals(ct.getLabel())).findFirst()
                .orElse(null);
        assertNotNull(channelType);
        assertEquals("Rollershutter", channelType.getItemType());
        assertEquals(ChannelKind.STATE, channelType.getKind());
        assertTrue(channelType.getTags().contains("Status"));
        assertTrue(channelType.getTags().contains("Opening"));
        assertNull(channelType.getUnitHint());
        state = channelType.getState();
        assertNotNull(state);
        max = state.getMaximum();
        min = state.getMinimum();
        step = state.getStep();
        assertNotNull(max);
        assertNotNull(min);
        assertNotNull(step);
        assertEquals(100.0, max.doubleValue());
        assertEquals(0.0, min.doubleValue());
        assertEquals(1.0, step.doubleValue());
        assertTrue(state.isReadOnly());

        // current tilt channel
        channelDefinition = groupType.getChannelDefinitions().stream()
                .filter(cd -> "13".equals(cd.getProperties().get("iid"))).findFirst().orElse(null);
        assertNotNull(channelDefinition);
        assertEquals("Horizontal Tilt Current", channelDefinition.getLabel());

        channelType = channelTypes.stream().filter(ct -> "Horizontal Tilt Current".equals(ct.getLabel())).findFirst()
                .orElse(null);
        assertNotNull(channelType);
        assertEquals("Number:Angle", channelType.getItemType());
        assertEquals(ChannelKind.STATE, channelType.getKind());
        assertTrue(channelType.getTags().contains("Measurement"));
        assertTrue(channelType.getTags().contains("Tilt"));
        assertEquals("°", channelType.getUnitHint());
        state = channelType.getState();
        assertNotNull(state);
        max = state.getMaximum();
        min = state.getMinimum();
        step = state.getStep();
        assertNotNull(max);
        assertNotNull(min);
        assertNotNull(step);
        assertEquals(90.0, max.doubleValue());
        assertEquals(-90.0, min.doubleValue());
        assertEquals(1.0, step.doubleValue());
        assertTrue(state.isReadOnly());

        // target tilt channel
        channelDefinition = groupType.getChannelDefinitions().stream()
                .filter(cd -> "14".equals(cd.getProperties().get("iid"))).findFirst().orElse(null);
        assertNotNull(channelDefinition);
        assertEquals("Horizontal Tilt Target", channelDefinition.getLabel());

        channelType = channelTypes.stream().filter(ct -> "Horizontal Tilt Target".equals(ct.getLabel())).findFirst()
                .orElse(null);
        assertNotNull(channelType);
        assertEquals("Number:Angle", channelType.getItemType());
        assertEquals(ChannelKind.STATE, channelType.getKind());
        assertTrue(channelType.getTags().contains("Setpoint"));
        assertTrue(channelType.getTags().contains("Tilt"));
        assertEquals("°", channelType.getUnitHint());
        state = channelType.getState();
        assertNotNull(state);
        max = state.getMaximum();
        min = state.getMinimum();
        step = state.getStep();
        assertNotNull(max);
        assertNotNull(min);
        assertNotNull(step);
        assertEquals(90.0, max.doubleValue());
        assertEquals(-90.0, min.doubleValue());
        assertEquals(1.0, step.doubleValue());
        assertFalse(state.isReadOnly());

        // position status channel
        channelDefinition = groupType.getChannelDefinitions().stream()
                .filter(cd -> "12".equals(cd.getProperties().get("iid"))).findFirst().orElse(null);
        assertNotNull(channelDefinition);
        assertEquals("Position State", channelDefinition.getLabel());

        channelType = channelTypes.stream().filter(ct -> "Position State".equals(ct.getLabel())).findFirst()
                .orElse(null);
        assertNotNull(channelType);
        assertEquals("String", channelType.getItemType());
        assertEquals(ChannelKind.STATE, channelType.getKind());
        assertTrue(channelType.getTags().contains("Status"));
        assertTrue(channelType.getTags().contains("Opening"));
        assertNull(channelType.getUnitHint());
        state = channelType.getState();
        assertNotNull(state);
        max = state.getMaximum();
        min = state.getMinimum();
        step = state.getStep();
        assertNotNull(max);
        assertNotNull(min);
        assertNotNull(step);
        assertEquals(2.0, max.doubleValue());
        assertEquals(0.0, min.doubleValue());
        assertEquals(1.0, step.doubleValue());
        assertTrue(state.isReadOnly());
        List<StateOption> options = state.getOptions();
        assertNotNull(options);
        assertEquals(3, options.size());
        assertEquals("2", options.get(2).getValue());
    }
}
