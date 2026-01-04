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
package org.openhab.binding.homekit.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homekit.internal.dto.Accessories;
import org.openhab.binding.homekit.internal.dto.Accessory;
import org.openhab.binding.homekit.internal.dto.Content;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingUID;
import org.osgi.framework.Bundle;

import com.google.gson.Gson;

/**
 * Test cases for loading channel creation data from JSON provided by a NetAtmo weather station.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class TestChannelCreationForNetatmo {

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
                                        "value": "Weather Station"
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
                                        "value": "Netatmo Weather Station"
                                    },
                                    {
                                        "type": "30",
                                        "iid": 5,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "g2f1432"
                                    },
                                    {
                                        "type": "52",
                                        "iid": 6,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "300.0.0"
                                    },
                                    {
                                        "type": "14",
                                        "iid": 7,
                                        "perms": [
                                            "pw"
                                        ],
                                        "format": "bool"
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "A2",
                                "iid": 25,
                                "characteristics": [
                                    {
                                        "type": "37",
                                        "iid": 26,
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
                                "type": "8D",
                                "iid": 8,
                                "characteristics": [
                                    {
                                        "type": "95",
                                        "iid": 9,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 2,
                                        "minValue": 0,
                                        "maxValue": 5
                                    },
                                    {
                                        "type": "23",
                                        "iid": 10,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Air quality"
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "97",
                                "iid": 11,
                                "characteristics": [
                                    {
                                        "type": "92",
                                        "iid": 12,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 0,
                                        "minValue": 0,
                                        "maxValue": 1
                                    },
                                    {
                                        "type": "93",
                                        "iid": 13,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "float",
                                        "value": 463.0,
                                        "minValue": 0.0,
                                        "maxValue": 5000.0
                                    },
                                    {
                                        "type": "23",
                                        "iid": 14,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Carbon Dioxide"
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "82",
                                "iid": 15,
                                "characteristics": [
                                    {
                                        "type": "10",
                                        "iid": 16,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "float",
                                        "value": 32.0,
                                        "minValue": 0.0,
                                        "maxValue": 100.0,
                                        "unit": "percentage"
                                    },
                                    {
                                        "type": "23",
                                        "iid": 17,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Humidity"
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "8A",
                                "iid": 18,
                                "characteristics": [
                                    {
                                        "type": "11",
                                        "iid": 19,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "float",
                                        "value": 22.1,
                                        "minValue": 0.0,
                                        "maxValue": 50.0,
                                        "minStep": 0.1,
                                        "unit": "celsius"
                                    },
                                    {
                                        "type": "23",
                                        "iid": 20,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Temperature"
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "6237CEFC-9F4D-54B2-8033-2EDA0053B811",
                                "iid": 21,
                                "characteristics": [
                                    {
                                        "type": "B3BBFABC-D78C-5B8D-948C-5DAC1EE2CDE5",
                                        "iid": 22,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 35,
                                        "minValue": 0,
                                        "maxValue": 200,
                                        "minStep": 1
                                    },
                                    {
                                        "type": "627EA399-29D9-5DC8-9A02-08AE928F73D8",
                                        "iid": 23,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 1,
                                        "minValue": 0,
                                        "maxValue": 3,
                                        "minStep": 1
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "EA22EA53-6227-55EA-AC24-73ACF3EEA0E8",
                                "iid": 27,
                                "characteristics": [
                                    {
                                        "type": "4D05AE82-5A22-5BD6-A730-B7F8B4F3218D",
                                        "iid": 28,
                                        "perms": [
                                            "pw"
                                        ],
                                        "format": "bool"
                                    },
                                    {
                                        "type": "00F44C18-042E-5C4E-9A4C-561D44DCD804",
                                        "iid": 29,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "g2f1432"
                                    }
                                ],
                                "hidden": true,
                                "primary": false
                            }
                        ]
                    },
                    {
                        "aid": 50,
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
                                        "value": "Outdoor Module"
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
                                        "value": "Outdoor Module"
                                    },
                                    {
                                        "type": "30",
                                        "iid": 5,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "h2ecc62"
                                    },
                                    {
                                        "type": "52",
                                        "iid": 6,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "53.0.0"
                                    },
                                    {
                                        "type": "14",
                                        "iid": 7,
                                        "perms": [
                                            "pw"
                                        ],
                                        "format": "bool"
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "82",
                                "iid": 15,
                                "characteristics": [
                                    {
                                        "type": "10",
                                        "iid": 16,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "float",
                                        "value": 83.0,
                                        "minValue": 0.0,
                                        "maxValue": 100.0,
                                        "unit": "percentage"
                                    },
                                    {
                                        "type": "23",
                                        "iid": 17,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Humidity"
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "8A",
                                "iid": 18,
                                "characteristics": [
                                    {
                                        "type": "11",
                                        "iid": 19,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "float",
                                        "value": -3.7,
                                        "minValue": -40.0,
                                        "maxValue": 65.0,
                                        "minStep": 0.1,
                                        "unit": "celsius"
                                    },
                                    {
                                        "type": "23",
                                        "iid": 20,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Temperature"
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "96",
                                "iid": 27,
                                "characteristics": [
                                    {
                                        "type": "68",
                                        "iid": 28,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 25,
                                        "minValue": 0,
                                        "maxValue": 100,
                                        "minStep": 25,
                                        "unit": "percentage"
                                    },
                                    {
                                        "type": "79",
                                        "iid": 29,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 0,
                                        "minValue": 0,
                                        "maxValue": 1
                                    },
                                    {
                                        "type": "8F",
                                        "iid": 30,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 2,
                                        "minValue": 0,
                                        "maxValue": 2,
                                        "minStep": 1
                                    },
                                    {
                                        "type": "23",
                                        "iid": 31,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Battery"
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            }
                        ]
                    },
                    {
                        "aid": 51,
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
                                        "value": "Additional Indoor Module"
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
                                        "value": "Additional Indoor Module"
                                    },
                                    {
                                        "type": "30",
                                        "iid": 5,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "i071af4"
                                    },
                                    {
                                        "type": "52",
                                        "iid": 6,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "53.0.0"
                                    },
                                    {
                                        "type": "14",
                                        "iid": 7,
                                        "perms": [
                                            "pw"
                                        ],
                                        "format": "bool"
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "8D",
                                "iid": 8,
                                "characteristics": [
                                    {
                                        "type": "95",
                                        "iid": 9,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 2,
                                        "minValue": 0,
                                        "maxValue": 5
                                    },
                                    {
                                        "type": "23",
                                        "iid": 10,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Air quality"
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "97",
                                "iid": 11,
                                "characteristics": [
                                    {
                                        "type": "92",
                                        "iid": 12,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 0,
                                        "minValue": 0,
                                        "maxValue": 1
                                    },
                                    {
                                        "type": "93",
                                        "iid": 13,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "float",
                                        "value": 507.0,
                                        "minValue": 0.0,
                                        "maxValue": 5000.0
                                    },
                                    {
                                        "type": "23",
                                        "iid": 14,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Carbon Dioxide"
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "82",
                                "iid": 15,
                                "characteristics": [
                                    {
                                        "type": "10",
                                        "iid": 16,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "float",
                                        "value": 36.0,
                                        "minValue": 0.0,
                                        "maxValue": 100.0,
                                        "unit": "percentage"
                                    },
                                    {
                                        "type": "23",
                                        "iid": 17,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Humidity"
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "8A",
                                "iid": 18,
                                "characteristics": [
                                    {
                                        "type": "11",
                                        "iid": 19,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "float",
                                        "value": 20.3,
                                        "minValue": 0.0,
                                        "maxValue": 50.0,
                                        "minStep": 0.1,
                                        "unit": "celsius"
                                    },
                                    {
                                        "type": "23",
                                        "iid": 20,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Temperature"
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "96",
                                "iid": 27,
                                "characteristics": [
                                    {
                                        "type": "68",
                                        "iid": 28,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 50,
                                        "minValue": 0,
                                        "maxValue": 100,
                                        "minStep": 25,
                                        "unit": "percentage"
                                    },
                                    {
                                        "type": "79",
                                        "iid": 29,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 0,
                                        "minValue": 0,
                                        "maxValue": 1
                                    },
                                    {
                                        "type": "8F",
                                        "iid": 30,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 2,
                                        "minValue": 0,
                                        "maxValue": 2,
                                        "minStep": 1
                                    },
                                    {
                                        "type": "23",
                                        "iid": 31,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Battery"
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            }
                        ]
                    },
                    {
                        "aid": 52,
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
                                        "value": "Additional Indoor Module"
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
                                        "value": "Additional Indoor Module"
                                    },
                                    {
                                        "type": "30",
                                        "iid": 5,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "i07472c"
                                    },
                                    {
                                        "type": "52",
                                        "iid": 6,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "53.0.0"
                                    },
                                    {
                                        "type": "14",
                                        "iid": 7,
                                        "perms": [
                                            "pw"
                                        ],
                                        "format": "bool"
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "8D",
                                "iid": 8,
                                "characteristics": [
                                    {
                                        "type": "95",
                                        "iid": 9,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 2,
                                        "minValue": 0,
                                        "maxValue": 5
                                    },
                                    {
                                        "type": "23",
                                        "iid": 10,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Air quality"
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "97",
                                "iid": 11,
                                "characteristics": [
                                    {
                                        "type": "92",
                                        "iid": 12,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 0,
                                        "minValue": 0,
                                        "maxValue": 1
                                    },
                                    {
                                        "type": "93",
                                        "iid": 13,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "float",
                                        "value": 440.0,
                                        "minValue": 0.0,
                                        "maxValue": 5000.0
                                    },
                                    {
                                        "type": "23",
                                        "iid": 14,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Carbon Dioxide"
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "82",
                                "iid": 15,
                                "characteristics": [
                                    {
                                        "type": "10",
                                        "iid": 16,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "float",
                                        "value": 36.0,
                                        "minValue": 0.0,
                                        "maxValue": 100.0,
                                        "unit": "percentage"
                                    },
                                    {
                                        "type": "23",
                                        "iid": 17,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Humidity"
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "8A",
                                "iid": 18,
                                "characteristics": [
                                    {
                                        "type": "11",
                                        "iid": 19,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "float",
                                        "value": 21.7,
                                        "minValue": 0.0,
                                        "maxValue": 50.0,
                                        "minStep": 0.1,
                                        "unit": "celsius"
                                    },
                                    {
                                        "type": "23",
                                        "iid": 20,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Temperature"
                                    }
                                ],
                                "hidden": false,
                                "primary": false
                            },
                            {
                                "type": "96",
                                "iid": 27,
                                "characteristics": [
                                    {
                                        "type": "68",
                                        "iid": 28,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 25,
                                        "minValue": 0,
                                        "maxValue": 100,
                                        "minStep": 25,
                                        "unit": "percentage"
                                    },
                                    {
                                        "type": "79",
                                        "iid": 29,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 0,
                                        "minValue": 0,
                                        "maxValue": 1
                                    },
                                    {
                                        "type": "8F",
                                        "iid": 30,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "format": "uint8",
                                        "value": 2,
                                        "minValue": 0,
                                        "maxValue": 2,
                                        "minStep": 1
                                    },
                                    {
                                        "type": "23",
                                        "iid": 31,
                                        "perms": [
                                            "pr"
                                        ],
                                        "format": "string",
                                        "value": "Battery"
                                    }
                                ],
                                "hidden": false,
                                "primary": false
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
        assertEquals(4, accessories.accessories.size());
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
    void testAid1HasAtLeastOneChannelDefinition() {
        Accessories accessories = GSON.fromJson(TEST_JSON, Accessories.class);
        assertNotNull(accessories);
        Accessory accessory = accessories.getAccessory(1L);
        assertNotNull(accessory);
        assertEquals(1, accessory.aid);
        assertEquals(8, accessory.services.size());

        TranslationProvider i18n = mock(TranslationProvider.class);
        Bundle bundle = mock(Bundle.class);
        ThingUID uid = new ThingUID("netatmo", "test");

        boolean hasChannelDefinition = (Optional.ofNullable(accessory.services).stream().flatMap(List::stream)
                .flatMap(service -> Optional.ofNullable(service.characteristics).stream()).flatMap(List::stream)
                .map(characteristic -> characteristic.getContent(uid, null, i18n, bundle)))
                .anyMatch(Content.ChannelDefinition.class::isInstance);

        assertTrue(hasChannelDefinition);
    }
}
