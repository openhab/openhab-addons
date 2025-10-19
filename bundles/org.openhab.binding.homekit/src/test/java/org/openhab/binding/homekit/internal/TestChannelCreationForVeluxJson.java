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
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelType;

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

        // TODO test channel definitions for Velux shade or window

        // TODO test channel definitions for a venetian blind with tilt support

        // TODO test channel definitions for Temperature, Humidity, and CO2 sensors

    }
}
