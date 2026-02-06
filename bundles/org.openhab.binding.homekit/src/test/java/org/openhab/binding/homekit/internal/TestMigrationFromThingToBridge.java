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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.openhab.binding.homekit.internal.HomekitBindingConstants.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.openhab.binding.homekit.internal.discovery.HomekitMdnsDiscoveryParticipant;
import org.openhab.binding.homekit.internal.dto.Accessories;
import org.openhab.binding.homekit.internal.dto.Accessory;
import org.openhab.binding.homekit.internal.handler.HomekitAccessoryHandler;
import org.openhab.binding.homekit.internal.persistence.HomekitKeyStore;
import org.openhab.binding.homekit.internal.persistence.HomekitTypeProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.semantics.model.DefaultSemanticTags.Equipment;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ManagedThingProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.type.ChannelGroupTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.osgi.framework.Bundle;

import com.google.gson.Gson;

/**
 * Tests for the migration from a Thing to a Bridge.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class TestMigrationFromThingToBridge {

    // Aqara Doorbell plus Repeater CH-C11E
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
                                        "iid": 65537,
                                        "type": "14",
                                        "format": "bool",
                                        "perms": [
                                            "pw"
                                        ]
                                    },
                                    {
                                        "iid": 65538,
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
                                        "iid": 65539,
                                        "type": "21",
                                        "format": "string",
                                        "value": "CH-C11E",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 65540,
                                        "type": "23",
                                        "format": "string",
                                        "value": "Doorbell Repeater-F667",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 65541,
                                        "type": "30",
                                        "format": "string",
                                        "value": "a9275cbecbe8f667",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 65542,
                                        "type": "52",
                                        "format": "string",
                                        "value": "4.5.20",
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 65543,
                                        "type": "53",
                                        "format": "string",
                                        "value": "1.1",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 65544,
                                        "type": "34AB8811-AC7F-4340-BAC3-FD6A85F9943B",
                                        "format": "string",
                                        "value": "6.3;e6e82026",
                                        "perms": [
                                            "pr",
                                            "hd"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 65545,
                                        "type": "220",
                                        "format": "data",
                                        "value": "xDsGO6gtae8=",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "maxDataLen": 8
                                    },
                                    {
                                        "iid": 65546,
                                        "type": "A6",
                                        "format": "uint32",
                                        "value": 0,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    }
                                ]
                            },
                            {
                                "iid": 2,
                                "type": "A2",
                                "primary": false,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 131074,
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
                                "iid": 4,
                                "type": "22A",
                                "primary": false,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 262145,
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
                                        "iid": 262146,
                                        "type": "22C",
                                        "format": "uint32",
                                        "value": 11,
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
                                        "iid": 262147,
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
                                "iid": 5,
                                "type": "239",
                                "primary": false,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 327681,
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
                                "iid": 16,
                                "type": "7E",
                                "primary": false,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 1048578,
                                        "type": "23",
                                        "format": "string",
                                        "value": "Security System",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 1048579,
                                        "type": "66",
                                        "format": "uint8",
                                        "value": 3,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "minValue": 0,
                                        "maxValue": 4,
                                        "minStep": 1,
                                        "valid-values": [
                                            0,
                                            1,
                                            2,
                                            3,
                                            4
                                        ]
                                    },
                                    {
                                        "iid": 1048580,
                                        "type": "67",
                                        "format": "uint8",
                                        "value": 3,
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "minValue": 0,
                                        "maxValue": 3,
                                        "minStep": 1,
                                        "valid-values": [
                                            0,
                                            1,
                                            2,
                                            3
                                        ]
                                    },
                                    {
                                        "iid": 1048581,
                                        "type": "60CDDE6C-42B6-4C72-9719-AB2740EABE2A",
                                        "format": "tlv8",
                                        "value": "",
                                        "perms": [
                                            "pr",
                                            "pw"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "description": "Stay Arm Trigger Devices"
                                    },
                                    {
                                        "iid": 1048582,
                                        "type": "4AB2460A-41E4-4F05-97C3-CCFDAE1BE324",
                                        "format": "tlv8",
                                        "value": "",
                                        "perms": [
                                            "pr",
                                            "pw"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "description": "Alarm Trigger Devices"
                                    },
                                    {
                                        "iid": 1048583,
                                        "type": "F8296386-5A30-4AA7-838C-ED0DA9D807DF",
                                        "format": "tlv8",
                                        "value": "",
                                        "perms": [
                                            "pr",
                                            "pw"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "description": "Night Arm Trigger Devices"
                                    }
                                ]
                            },
                            {
                                "iid": 17,
                                "type": "9715BF53-AB63-4449-8DC7-2785D617390A",
                                "primary": false,
                                "hidden": true,
                                "characteristics": [
                                    {
                                        "iid": 1114121,
                                        "type": "B1C09E4C-E202-4827-B863-B0F32F727CFF",
                                        "format": "bool",
                                        "value": 0,
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev",
                                            "hd"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "description": "New Accessory Permission"
                                    },
                                    {
                                        "iid": 1114122,
                                        "type": "75D19FA9-218B-4943-997E-341E5D1C60CC",
                                        "format": "string",
                                        "perms": [
                                            "pw",
                                            "hd"
                                        ],
                                        "description": "Remove Accessory"
                                    },
                                    {
                                        "iid": 1114123,
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
                                        "description": "Software New Status",
                                        "minValue": -65535,
                                        "maxValue": 65535,
                                        "minStep": 1
                                    },
                                    {
                                        "iid": 1114124,
                                        "type": "A45EFD52-0DB5-4C1A-9727-513FBCD8185F",
                                        "format": "string",
                                        "perms": [
                                            "pw",
                                            "hd"
                                        ],
                                        "description": "Software New URL",
                                        "maxLen": 256
                                    },
                                    {
                                        "iid": 1114125,
                                        "type": "40F0124A-579D-40E4-865E-0EF6740EA64B",
                                        "format": "string",
                                        "perms": [
                                            "pw",
                                            "hd"
                                        ],
                                        "description": "Software New Checksum"
                                    },
                                    {
                                        "iid": 1114126,
                                        "type": "96BF5F20-2996-4DB6-8D65-0E36314BCB6D",
                                        "format": "string",
                                        "value": "4.5.20_0026.0092",
                                        "perms": [
                                            "pr",
                                            "hd"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "description": "Software Number"
                                    },
                                    {
                                        "iid": 1114127,
                                        "type": "36B7A28B-3200-4783-A3FB-6714F11B1417",
                                        "format": "string",
                                        "value": "lumi.camera.agl006",
                                        "perms": [
                                            "pr",
                                            "hd"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "description": "Software Behavior"
                                    },
                                    {
                                        "iid": 1114128,
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
                                    },
                                    {
                                        "iid": 1114129,
                                        "type": "ED080A16-3A60-433A-B983-F6CB5228B138",
                                        "format": "string",
                                        "perms": [
                                            "pw",
                                            "hd"
                                        ],
                                        "description": "Run Scene"
                                    }
                                ]
                            },
                            {
                                "iid": 18,
                                "type": "F49132D1-12DF-4119-87D3-A93E8D68531E",
                                "primary": false,
                                "hidden": true,
                                "characteristics": [
                                    {
                                        "iid": 1179650,
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
                                        "iid": 1179651,
                                        "type": "C7EECAA7-91D9-40EB-AD0C-FFDDE3143CB9",
                                        "format": "string",
                                        "value": "lumi3.a9275cbecbe8f667",
                                        "perms": [
                                            "pr",
                                            "hd"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "description": "Lumi Did"
                                    },
                                    {
                                        "iid": 1179652,
                                        "type": "80FA747E-CB45-45A4-B7BE-AA7D9964859E",
                                        "format": "string",
                                        "perms": [
                                            "pw",
                                            "hd"
                                        ],
                                        "description": "Lumi Bindkey"
                                    },
                                    {
                                        "iid": 1179653,
                                        "type": "C3B8A329-EF0C-4739-B773-E5B7AEA52C71",
                                        "format": "bool",
                                        "value": 1,
                                        "perms": [
                                            "pr",
                                            "hd"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "description": "Lumi Bindstate"
                                    },
                                    {
                                        "iid": 1179654,
                                        "type": "18E85FEB-C219-43AB-8CD4-15ED1D2B5086",
                                        "format": "uint8",
                                        "value": 1,
                                        "perms": [
                                            "pr",
                                            "hd"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "description": "Selected Lumi Bindtype",
                                        "minValue": 0,
                                        "maxValue": 5,
                                        "minStep": 1
                                    }
                                ]
                            },
                            {
                                "iid": 22,
                                "type": "96",
                                "primary": false,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 1441794,
                                        "type": "23",
                                        "format": "string",
                                        "value": "Battery Sensor",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 1441795,
                                        "type": "68",
                                        "format": "uint8",
                                        "value": 100,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "unit": "percentage",
                                        "minValue": 0,
                                        "maxValue": 100,
                                        "minStep": 1
                                    },
                                    {
                                        "iid": 1441797,
                                        "type": "79",
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
                                        "minStep": 1,
                                        "valid-values": [
                                            0,
                                            1
                                        ]
                                    },
                                    {
                                        "iid": 1441796,
                                        "type": "8F",
                                        "format": "uint8",
                                        "value": 2,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "minValue": 2,
                                        "maxValue": 2,
                                        "minStep": 1,
                                        "valid-values": [
                                            2
                                        ]
                                    }
                                ]
                            },
                            {
                                "iid": 26,
                                "type": "129",
                                "primary": false,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 1703937,
                                        "type": "130",
                                        "format": "tlv8",
                                        "value": "AQMBAQA=",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 1703938,
                                        "type": "131",
                                        "format": "tlv8",
                                        "value": "",
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "wr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 1703939,
                                        "type": "37",
                                        "format": "string",
                                        "value": "1.0",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    }
                                ]
                            },
                            {
                                "iid": 27,
                                "type": "110",
                                "primary": false,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 1769474,
                                        "type": "B0",
                                        "format": "uint8",
                                        "value": 1,
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev",
                                            "tw"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "minValue": 0,
                                        "maxValue": 1,
                                        "minStep": 1
                                    },
                                    {
                                        "iid": 1769475,
                                        "type": "120",
                                        "format": "tlv8",
                                        "value": "AQEA",
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 1769476,
                                        "type": "114",
                                        "format": "tlv8",
                                        "value": "AVwBAQACDgEBAQIBAAAAAgECAwEAAwsBAkAGAgKwBAMBHgAAAwsBAgAFAgLAAwMBHgAAAwsBAoACAgLgAQMBHgAAAwsBAuABAgJoAQMBHgAAAwsBAkABAgLwAAMBHg==",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 1769477,
                                        "type": "115",
                                        "format": "tlv8",
                                        "value": "AQ4BAQICCQEBAQIBAAMBAQIBAA==",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 1769478,
                                        "type": "116",
                                        "format": "tlv8",
                                        "value": "AgEAAAACAQEAAAIBAg==",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 1769479,
                                        "type": "118",
                                        "format": "tlv8",
                                        "value": "",
                                        "perms": [
                                            "pr",
                                            "pw"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 1769480,
                                        "type": "117",
                                        "format": "tlv8",
                                        "value": "",
                                        "perms": [
                                            "pr",
                                            "pw"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    }
                                ]
                            },
                            {
                                "iid": 28,
                                "type": "110",
                                "primary": false,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 1835010,
                                        "type": "B0",
                                        "format": "uint8",
                                        "value": 1,
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev",
                                            "tw"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "minValue": 0,
                                        "maxValue": 1,
                                        "minStep": 1
                                    },
                                    {
                                        "iid": 1835011,
                                        "type": "120",
                                        "format": "tlv8",
                                        "value": "AQEA",
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 1835012,
                                        "type": "114",
                                        "format": "tlv8",
                                        "value": "AU0BAQACDgEBAQIBAAAAAgECAwEAAwsBAgAFAgLAAwMBHgAAAwsBAoACAgLgAQMBHgAAAwsBAuABAgJoAQMBHgAAAwsBAkABAgLwAAMBHg==",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 1835013,
                                        "type": "115",
                                        "format": "tlv8",
                                        "value": "AQ4BAQICCQEBAQIBAAMBAQIBAA==",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 1835014,
                                        "type": "116",
                                        "format": "tlv8",
                                        "value": "AgEAAAACAQEAAAIBAg==",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 1835015,
                                        "type": "118",
                                        "format": "tlv8",
                                        "value": "",
                                        "perms": [
                                            "pr",
                                            "pw"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 1835016,
                                        "type": "117",
                                        "format": "tlv8",
                                        "value": "",
                                        "perms": [
                                            "pr",
                                            "pw"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    }
                                ]
                            },
                            {
                                "iid": 29,
                                "type": "204",
                                "primary": false,
                                "hidden": false,
                                "linked": [
                                    26,
                                    33
                                ],
                                "characteristics": [
                                    {
                                        "iid": 1900545,
                                        "type": "B0",
                                        "format": "uint8",
                                        "value": 0,
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev",
                                            "tw"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "minValue": 0,
                                        "maxValue": 1,
                                        "minStep": 1
                                    },
                                    {
                                        "iid": 1900546,
                                        "type": "205",
                                        "format": "tlv8",
                                        "value": "AQSgDwAAAggDAAAAAAAAAAMLAQEAAgYBBKAPAAA=",
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 1900547,
                                        "type": "206",
                                        "format": "tlv8",
                                        "value": "AUoBAQACCwEBAQIBAAAAAgECAwsBAkAGAgKwBAMBHgAAAwsBAgAFAgLAAwMBHgAAAwsBAkAGAgKwBAMBDwAAAwsBAgAFAgLAAwMBDw==",
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 1900548,
                                        "type": "207",
                                        "format": "tlv8",
                                        "value": "AQ4BAQACCQEBAQIBAAMBAQ==",
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 1900549,
                                        "type": "209",
                                        "format": "tlv8",
                                        "value": "",
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 1900550,
                                        "type": "226",
                                        "format": "uint8",
                                        "value": 0,
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev",
                                            "tw"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "minValue": 0,
                                        "maxValue": 1,
                                        "minStep": 1
                                    }
                                ]
                            },
                            {
                                "iid": 30,
                                "type": "21A",
                                "primary": false,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 1966081,
                                        "type": "223",
                                        "format": "uint8",
                                        "value": 1,
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "minValue": 0,
                                        "maxValue": 1,
                                        "minStep": 1
                                    },
                                    {
                                        "iid": 1966082,
                                        "type": "225",
                                        "format": "uint8",
                                        "value": 1,
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "minValue": 0,
                                        "maxValue": 1,
                                        "minStep": 1
                                    },
                                    {
                                        "iid": 1966083,
                                        "type": "21B",
                                        "format": "uint8",
                                        "value": 1,
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "minValue": 0,
                                        "maxValue": 1,
                                        "minStep": 1
                                    },
                                    {
                                        "iid": 1966085,
                                        "type": "21D",
                                        "format": "uint8",
                                        "value": 1,
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "minValue": 0,
                                        "maxValue": 1,
                                        "minStep": 1
                                    },
                                    {
                                        "iid": 1966086,
                                        "type": "11B",
                                        "format": "bool",
                                        "value": 1,
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    }
                                ]
                            },
                            {
                                "iid": 31,
                                "type": "112",
                                "primary": false,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 2031618,
                                        "type": "23",
                                        "format": "string",
                                        "value": "Microphone",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 2031619,
                                        "type": "11A",
                                        "format": "bool",
                                        "value": 0,
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 2031620,
                                        "type": "119",
                                        "format": "uint8",
                                        "value": 53,
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "unit": "percentage",
                                        "minValue": 0,
                                        "maxValue": 100,
                                        "minStep": 1
                                    }
                                ]
                            },
                            {
                                "iid": 32,
                                "type": "113",
                                "primary": false,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 2097154,
                                        "type": "23",
                                        "format": "string",
                                        "value": "Speaker",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 2097155,
                                        "type": "11A",
                                        "format": "bool",
                                        "value": 0,
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 2097156,
                                        "type": "119",
                                        "format": "uint8",
                                        "value": 78,
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "unit": "percentage",
                                        "minValue": 0,
                                        "maxValue": 100,
                                        "minStep": 1
                                    }
                                ]
                            },
                            {
                                "iid": 33,
                                "type": "85",
                                "primary": false,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 2162690,
                                        "type": "23",
                                        "format": "string",
                                        "value": "Motion Sensor",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 2162691,
                                        "type": "22",
                                        "format": "bool",
                                        "value": 0,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 2162692,
                                        "type": "75",
                                        "format": "bool",
                                        "value": 1,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    }
                                ]
                            },
                            {
                                "iid": 34,
                                "type": "121",
                                "primary": true,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 2228226,
                                        "type": "23",
                                        "format": "string",
                                        "value": "Video Doorbell",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 2228227,
                                        "type": "73",
                                        "format": "uint8",
                                        "value": null,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "minValue": 0,
                                        "maxValue": 0,
                                        "minStep": 1,
                                        "valid-values": [
                                            0
                                        ]
                                    },
                                    {
                                        "iid": 2228228,
                                        "type": "11A",
                                        "format": "bool",
                                        "value": 0,
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 2228229,
                                        "type": "232",
                                        "format": "tlv8",
                                        "value": "AQEA",
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 2228230,
                                        "type": "119",
                                        "format": "uint8",
                                        "value": 50,
                                        "perms": [
                                            "pr",
                                            "pw",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "unit": "percentage",
                                        "minValue": 0,
                                        "maxValue": 100,
                                        "minStep": 1
                                    }
                                ]
                            },
                            {
                                "iid": 35,
                                "type": "236",
                                "primary": false,
                                "hidden": true,
                                "characteristics": [
                                    {
                                        "iid": 2293762,
                                        "type": "234",
                                        "format": "tlv8",
                                        "value": "",
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 2293763,
                                        "type": "235",
                                        "format": "tlv8",
                                        "value": "AQEAAgIsAQ==",
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        "aid": 65,
                        "services": [
                            {
                                "iid": 1,
                                "type": "3E",
                                "primary": false,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 65537,
                                        "type": "14",
                                        "format": "bool",
                                        "perms": [
                                            "pw"
                                        ]
                                    },
                                    {
                                        "iid": 65538,
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
                                        "iid": 65539,
                                        "type": "21",
                                        "format": "string",
                                        "value": "AS074",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 65540,
                                        "type": "23",
                                        "format": "string",
                                        "value": "Presence Sensor",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 65541,
                                        "type": "30",
                                        "format": "string",
                                        "value": "54ef441000dba772",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 65542,
                                        "type": "52",
                                        "format": "string",
                                        "value": "0029",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 65543,
                                        "type": "53",
                                        "format": "string",
                                        "value": "1",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    }
                                ]
                            },
                            {
                                "iid": 4,
                                "type": "86",
                                "primary": true,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 262146,
                                        "type": "23",
                                        "format": "string",
                                        "value": "Occupancy Sensor",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 262148,
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
                                        "minStep": 1,
                                        "valid-values": [
                                            0,
                                            1
                                        ]
                                    },
                                    {
                                        "iid": 262150,
                                        "type": "75",
                                        "format": "bool",
                                        "value": 1,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        "aid": 66,
                        "services": [
                            {
                                "iid": 1,
                                "type": "3E",
                                "primary": false,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 65537,
                                        "type": "14",
                                        "format": "bool",
                                        "perms": [
                                            "pw"
                                        ]
                                    },
                                    {
                                        "iid": 65538,
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
                                        "iid": 65539,
                                        "type": "21",
                                        "format": "string",
                                        "value": "AS077",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 65540,
                                        "type": "23",
                                        "format": "string",
                                        "value": "Presence Sensor",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 65541,
                                        "type": "30",
                                        "format": "string",
                                        "value": "54ef44100146ec93",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 65542,
                                        "type": "52",
                                        "format": "string",
                                        "value": "5234",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 65543,
                                        "type": "53",
                                        "format": "string",
                                        "value": "1.1",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    }
                                ]
                            },
                            {
                                "iid": 4,
                                "type": "86",
                                "primary": true,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 262146,
                                        "type": "23",
                                        "format": "string",
                                        "value": "Occupancy Sensor",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 262148,
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
                                        "minStep": 1,
                                        "valid-values": [
                                            0,
                                            1
                                        ]
                                    }
                                ]
                            },
                            {
                                "iid": 5,
                                "type": "84",
                                "primary": false,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 327682,
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
                                        "iid": 327683,
                                        "type": "6B",
                                        "format": "float",
                                        "value": 94,
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
                                "iid": 6,
                                "type": "8A",
                                "primary": false,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 393218,
                                        "type": "23",
                                        "format": "string",
                                        "value": "Temperature Sensor",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 393219,
                                        "type": "11",
                                        "format": "float",
                                        "value": 25.6,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "unit": "celsius",
                                        "minValue": -50,
                                        "maxValue": 100,
                                        "minStep": 0.1
                                    }
                                ]
                            },
                            {
                                "iid": 7,
                                "type": "82",
                                "primary": false,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 458754,
                                        "type": "23",
                                        "format": "string",
                                        "value": "Humidity Sensor",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 458755,
                                        "type": "10",
                                        "format": "float",
                                        "value": 42,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "unit": "percentage",
                                        "minValue": 0,
                                        "maxValue": 100,
                                        "minStep": 1
                                    }
                                ]
                            },
                            {
                                "iid": 8,
                                "type": "96",
                                "primary": false,
                                "hidden": false,
                                "characteristics": [
                                    {
                                        "iid": 524290,
                                        "type": "23",
                                        "format": "string",
                                        "value": "Battery Sensor",
                                        "perms": [
                                            "pr"
                                        ],
                                        "ev": false,
                                        "enc": false
                                    },
                                    {
                                        "iid": 524291,
                                        "type": "68",
                                        "format": "uint8",
                                        "value": 100,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "unit": "percentage",
                                        "minValue": 0,
                                        "maxValue": 100,
                                        "minStep": 1
                                    },
                                    {
                                        "iid": 524293,
                                        "type": "79",
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
                                        "minStep": 1,
                                        "valid-values": [
                                            0,
                                            1
                                        ]
                                    },
                                    {
                                        "iid": 524292,
                                        "type": "8F",
                                        "format": "uint8",
                                        "value": 2,
                                        "perms": [
                                            "pr",
                                            "ev"
                                        ],
                                        "ev": false,
                                        "enc": false,
                                        "minValue": 2,
                                        "maxValue": 2,
                                        "minStep": 1,
                                        "valid-values": [
                                            2
                                        ]
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
                        """;

    private static final Gson GSON = new Gson();

    /**
     * Test subclass to allow injection of a test scheduler.
     */
    protected class TestHomekitAccessoryHandler extends HomekitAccessoryHandler {
        private final ScheduledExecutorService injectedTestScheduler;

        public TestHomekitAccessoryHandler(Thing thing, HomekitTypeProvider typeProvider,
                ChannelTypeRegistry channelTypeRegistry, ChannelGroupTypeRegistry channelGroupTypeRegistry,
                HomekitKeyStore keyStore, TranslationProvider i18nProvider, Bundle bundle,
                ScheduledExecutorService scheduler, ManagedThingProvider thingProvider,
                HomekitMdnsDiscoveryParticipant discoveryParticipant) {
            super(thing, typeProvider, channelTypeRegistry, channelGroupTypeRegistry, keyStore, i18nProvider, bundle,
                    thingProvider, discoveryParticipant);
            this.injectedTestScheduler = scheduler;
        }

        @Override
        protected ScheduledExecutorService getScheduler() {
            return injectedTestScheduler;
        }
    }

    private Accessory createAccessory(Long aid) {
        Accessory accessory = new Accessory();
        accessory.aid = aid;
        accessory.services = new ArrayList<>();
        return accessory;
    }

    private void injectField(Object target, String fieldName, Object value) {
        Class<?> current = target.getClass();
        while (current != null && current != Object.class) {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            } catch (IllegalAccessException e) {
                fail("Failed to inject field: " + fieldName, e);
            }
        }
        fail("Could not find field '" + fieldName + "' in class hierarchy");
    }

    private void invokeMethod(Object target, String methodName) {
        Class<?> current = target.getClass();
        while (current != null && current != Object.class) {
            try {
                Method method = current.getDeclaredMethod(methodName);
                method.setAccessible(true);
                method.invoke(target);
                return;
            } catch (NoSuchMethodException e) {
                current = current.getSuperclass();
            } catch (Exception e) {
                fail("Failed to invoke method: " + methodName, e);
            }
        }
        fail("Could not find method '" + methodName + "' in class hierarchy");
    }

    private @Nullable Object getField(Object target, String name) throws Exception {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field f = type.getDeclaredField(name);
                f.setAccessible(true);
                return f.get(target);
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private HomekitAccessoryHandler createHandler(Map<Long, Accessory> accessories, ThingTypeUID thingTypeUID,
            String thingId, List<Runnable> capturedRunnables, ManagedThingProvider thingProvider,
            ThingHandlerCallback callback, HomekitMdnsDiscoveryParticipant discoveryParticipant) {

        Thing thing = mock(Thing.class);
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        ScheduledFuture<?> scheduledFuture = mock(ScheduledFuture.class);
        HomekitTypeProvider typeProvider = mock(HomekitTypeProvider.class);
        ChannelTypeRegistry channelTypeRegistry = mock(ChannelTypeRegistry.class);
        ChannelGroupTypeRegistry channelGroupTypeRegistry = mock(ChannelGroupTypeRegistry.class);
        HomekitKeyStore keyStore = mock(HomekitKeyStore.class);
        TranslationProvider translationProvider = mock(TranslationProvider.class);
        Bundle bundle = mock(Bundle.class);

        when(thingProvider.get(new ThingUID(THING_TYPE_BRIDGE, thingId))).thenReturn(null);
        when(thingProvider.get(new ThingUID(THING_TYPE_BRIDGED_ACCESSORY, thingId))).thenReturn(null);
        when(thingProvider.get(new ThingUID(THING_TYPE_ACCESSORY, thingId))).thenReturn(thing);

        ThingUID thingUID = new ThingUID(thingTypeUID, thingId);
        when(thing.getUID()).thenReturn(thingUID);
        when(thing.getThingTypeUID()).thenReturn(thingTypeUID);
        when(thing.getLabel()).thenReturn("Test Accessory (AA:BB:CC:DD)");
        when(thing.getLocation()).thenReturn("Living Room");
        when(thing.getBridgeUID()).thenReturn(null);
        when(thing.getSemanticEquipmentTag()).thenReturn("DOORBELL");

        Configuration config = new Configuration();
        config.put(HomekitBindingConstants.CONFIG_IP_ADDRESS, "192.168.1.100:1234");
        config.put(HomekitBindingConstants.CONFIG_UNIQUE_ID, "test-unique-id");
        config.put(HomekitBindingConstants.CONFIG_REFRESH_INTERVAL, BigDecimal.valueOf(60));
        when(thing.getConfiguration()).thenReturn(config);

        Map<String, String> properties = new HashMap<>();
        properties.put(HomekitBindingConstants.PROPERTY_ACCESSORY_CATEGORY, "test-category");
        when(thing.getProperties()).thenReturn(properties);

        when(callback.isChannelLinked(ArgumentMatchers.any(ChannelUID.class))).thenReturn(false);
        when(callback.getBridge(ArgumentMatchers.any(ThingUID.class))).thenReturn(null);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            capturedRunnables.add(runnable);
            return scheduledFuture;
        }).when(scheduler).schedule(ArgumentMatchers.<Runnable> any(), anyLong(), ArgumentMatchers.any(TimeUnit.class));

        HomekitAccessoryHandler handler = new TestHomekitAccessoryHandler(thing, typeProvider, channelTypeRegistry,
                channelGroupTypeRegistry, keyStore, translationProvider, bundle, scheduler, thingProvider,
                discoveryParticipant);

        // Inject accessories map
        injectField(handler, "accessories", accessories);

        // Set callback and inject scheduler
        handler.setCallback(callback);

        // Ensure isBridgedAccessory is false (stand-alone accessory, not a child of a bridge)
        injectField(handler, "isBridgedAccessory", false);

        return handler;
    }

    @Test
    public void testMigrationNotTriggeredForSingleAccessory() {
        List<Runnable> capturedRunnables = new ArrayList<>();
        ManagedThingProvider thingProvider = mock(ManagedThingProvider.class);
        HomekitMdnsDiscoveryParticipant discoveryService = mock(HomekitMdnsDiscoveryParticipant.class);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.isChannelLinked(ArgumentMatchers.any(ChannelUID.class))).thenReturn(false);

        Map<Long, Accessory> accessories = new ConcurrentHashMap<>();
        accessories.put(1L, createAccessory(1L));

        HomekitAccessoryHandler handler = createHandler(accessories, HomekitBindingConstants.THING_TYPE_ACCESSORY,
                "test-accessory", capturedRunnables, thingProvider, callback, discoveryService);

        invokeMethod(handler, "onConnectedThingAccessoriesLoaded");

        assertEquals(0, capturedRunnables.size(), "No migration task should be scheduled for single accessory");
    }

    @Test
    public void testMigrationNotTriggeredForBridgeType() {
        List<Runnable> capturedRunnables = new ArrayList<>();
        ManagedThingProvider thingProvider = mock(ManagedThingProvider.class);
        HomekitMdnsDiscoveryParticipant discoveryService = mock(HomekitMdnsDiscoveryParticipant.class);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.isChannelLinked(ArgumentMatchers.any(ChannelUID.class))).thenReturn(false);

        Map<Long, Accessory> accessories = new ConcurrentHashMap<>();
        accessories.put(1L, createAccessory(1L));
        accessories.put(2L, createAccessory(2L));

        HomekitAccessoryHandler handler = createHandler(accessories, HomekitBindingConstants.THING_TYPE_BRIDGE,
                "test-bridge", capturedRunnables, thingProvider, callback, discoveryService);

        invokeMethod(handler, "onConnectedThingAccessoriesLoaded");

        assertEquals(0, capturedRunnables.size(), "No migration task should be scheduled for bridge type");
    }

    @Test
    public void testMigrationNotTriggeredForBridgedAccessoryType() {
        List<Runnable> capturedRunnables = new ArrayList<>();
        ManagedThingProvider thingProvider = mock(ManagedThingProvider.class);
        HomekitMdnsDiscoveryParticipant discoveryService = mock(HomekitMdnsDiscoveryParticipant.class);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.isChannelLinked(ArgumentMatchers.any(ChannelUID.class))).thenReturn(false);

        Map<Long, Accessory> accessories = new ConcurrentHashMap<>();
        accessories.put(1L, createAccessory(1L));
        accessories.put(2L, createAccessory(2L));

        HomekitAccessoryHandler handler = createHandler(accessories,
                HomekitBindingConstants.THING_TYPE_BRIDGED_ACCESSORY, "test-bridged-accessory", capturedRunnables,
                thingProvider, callback, discoveryService);

        invokeMethod(handler, "onConnectedThingAccessoriesLoaded");

        assertEquals(0, capturedRunnables.size(), "No migration task should be scheduled for bridged accessory type");
    }

    @Test
    public void testMigrationTriggeredForMultipleAccessories() {
        List<Runnable> capturedRunnables = new ArrayList<>();
        ManagedThingProvider thingProvider = mock(ManagedThingProvider.class);
        HomekitMdnsDiscoveryParticipant discoveryService = mock(HomekitMdnsDiscoveryParticipant.class);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.isChannelLinked(ArgumentMatchers.any(ChannelUID.class))).thenReturn(false);

        Map<Long, Accessory> accessories = new ConcurrentHashMap<>();
        accessories.put(1L, createAccessory(1L));
        accessories.put(2L, createAccessory(2L));

        HomekitAccessoryHandler handler = createHandler(accessories, HomekitBindingConstants.THING_TYPE_ACCESSORY,
                "test-accessory", capturedRunnables, thingProvider, callback, discoveryService);

        invokeMethod(handler, "onConnectedThingAccessoriesLoaded");

        assertEquals(1, capturedRunnables.size(), "Migration task should be scheduled");
    }

    @Test
    public void testBridgeAndThingInheritThingProperties() {
        List<Runnable> capturedRunnables = new ArrayList<>();
        ManagedThingProvider thingProvider = mock(ManagedThingProvider.class);
        HomekitMdnsDiscoveryParticipant discoveryService = mock(HomekitMdnsDiscoveryParticipant.class);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.isChannelLinked(ArgumentMatchers.any(ChannelUID.class))).thenReturn(false);

        Map<Long, Accessory> accessories = new ConcurrentHashMap<>();
        accessories.put(1L, createAccessory(1L));
        accessories.put(2L, createAccessory(2L));

        HomekitAccessoryHandler handler = createHandler(accessories, HomekitBindingConstants.THING_TYPE_ACCESSORY,
                "test-accessory", capturedRunnables, thingProvider, callback, discoveryService);

        invokeMethod(handler, "onConnectedThingAccessoriesLoaded");

        assertEquals(1, capturedRunnables.size(), "Migration task should be captured");

        capturedRunnables.get(0).run();

        verify(thingProvider).remove(ArgumentMatchers.any(ThingUID.class));
        verify(discoveryService, times(1)).suppressId("test-unique-id", true);
        verifyNoMoreInteractions(discoveryService);

        ArgumentCaptor<Thing> captor = ArgumentCaptor.forClass(Thing.class);
        verify(thingProvider, times(2)).add(captor.capture());
        List<Thing> added = captor.getAllValues();

        assertTrue(added.get(0) instanceof Bridge);
        Bridge newBridge = (Bridge) added.get(0);
        assertEquals(HomekitBindingConstants.THING_TYPE_BRIDGE, newBridge.getThingTypeUID());
        assertEquals("Test Accessory (AA:BB:CC:DD)", newBridge.getLabel());
        assertEquals("Living Room", newBridge.getLocation());
        assertEquals(handler.getThing().getProperties(), newBridge.getProperties());
        assertEquals(handler.getThing().getConfiguration().getProperties(),
                newBridge.getConfiguration().getProperties());
        assertEquals(Equipment.NETWORK_APPLIANCE.getName(), newBridge.getSemanticEquipmentTag());

        assertFalse(added.get(1) instanceof Bridge);
        assertTrue(added.get(1) instanceof Thing);
        Thing newThing = (Thing) added.get(1);
        assertEquals(newBridge.getUID(), newThing.getBridgeUID());
        assertEquals(HomekitBindingConstants.THING_TYPE_BRIDGED_ACCESSORY, newThing.getThingTypeUID());
        assertEquals("Test Accessory (AA:BB:CC:DD-1)", newThing.getLabel());
        assertEquals("Living Room", newThing.getLocation());
        assertEquals("DOORBELL", newThing.getSemanticEquipmentTag());
        assertEquals("1", newThing.getConfiguration().getProperties().get(CONFIG_ACCESSORY_ID));
        assertEquals("test-unique-id-1", newThing.getProperties().get(PROPERTY_UNIQUE_ID));
    }

    @Test
    public void testMigratingFlagUpdatedDuringMigration() {
        List<Runnable> capturedRunnables = new ArrayList<>();
        ManagedThingProvider thingProvider = mock(ManagedThingProvider.class);
        HomekitMdnsDiscoveryParticipant discoveryService = mock(HomekitMdnsDiscoveryParticipant.class);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.isChannelLinked(ArgumentMatchers.any(ChannelUID.class))).thenReturn(false);

        Map<Long, Accessory> accessories = new ConcurrentHashMap<>();
        accessories.put(1L, createAccessory(1L));
        accessories.put(2L, createAccessory(2L));

        HomekitAccessoryHandler handler = createHandler(accessories, HomekitBindingConstants.THING_TYPE_ACCESSORY,
                "test-accessory", capturedRunnables, thingProvider, callback, discoveryService);

        invokeMethod(handler, "onConnectedThingAccessoriesLoaded");

        assertEquals(1, capturedRunnables.size(), "Migration task should be captured");

        Object migrating = assertDoesNotThrow(() -> getField(handler, "migrating"));
        assertTrue(migrating instanceof AtomicBoolean);
        assertTrue(((AtomicBoolean) migrating).get());

        capturedRunnables.get(0).run();

        verify(discoveryService, times(1)).suppressId("test-unique-id", true);
        verifyNoMoreInteractions(discoveryService);
    }

    @Test
    public void testStatusUpdatedDuringMigration() {
        List<Runnable> capturedRunnables = new ArrayList<>();
        ManagedThingProvider thingProvider = mock(ManagedThingProvider.class);
        HomekitMdnsDiscoveryParticipant discoveryService = mock(HomekitMdnsDiscoveryParticipant.class);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.isChannelLinked(ArgumentMatchers.any(ChannelUID.class))).thenReturn(false);

        Map<Long, Accessory> accessories = new ConcurrentHashMap<>();
        accessories.put(1L, createAccessory(1L));
        accessories.put(2L, createAccessory(2L));

        HomekitAccessoryHandler handler = createHandler(accessories, HomekitBindingConstants.THING_TYPE_ACCESSORY,
                "test-accessory", capturedRunnables, thingProvider, callback, discoveryService);

        invokeMethod(handler, "onConnectedThingAccessoriesLoaded");

        ArgumentCaptor<ThingStatusInfo> statusCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, atLeastOnce()).statusUpdated(ArgumentMatchers.any(Thing.class), statusCaptor.capture());

        boolean foundMigrationStatus = statusCaptor.getAllValues().stream()
                .anyMatch(status -> status.getStatus() == ThingStatus.OFFLINE
                        && status.getStatusDetail() == ThingStatusDetail.CONFIGURATION_PENDING);

        assertTrue(foundMigrationStatus, "Status should be OFFLINE with CONFIGURATION_PENDING during migration");
    }

    @Test
    public void testUnpairBlockedDuringMigration() {
        List<Runnable> capturedRunnables = new ArrayList<>();
        ManagedThingProvider thingProvider = mock(ManagedThingProvider.class);
        HomekitMdnsDiscoveryParticipant discoveryService = mock(HomekitMdnsDiscoveryParticipant.class);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.isChannelLinked(ArgumentMatchers.any(ChannelUID.class))).thenReturn(false);

        Map<Long, Accessory> accessories = new ConcurrentHashMap<>();
        accessories.put(1L, createAccessory(1L));
        accessories.put(2L, createAccessory(2L));

        HomekitAccessoryHandler handler = createHandler(accessories, HomekitBindingConstants.THING_TYPE_ACCESSORY,
                "test-accessory", capturedRunnables, thingProvider, callback, discoveryService);

        invokeMethod(handler, "onConnectedThingAccessoriesLoaded");

        assertEquals(1, capturedRunnables.size(), "Migration task should be scheduled");

        String result = handler.unpair();

        assertTrue(result.contains("ERROR"));
        assertTrue(result.contains("migration in progress"));
    }

    @Test
    public void testMigrationHandlesRegistryError() {
        List<Runnable> capturedRunnables = new ArrayList<>();
        ManagedThingProvider thingProvider = mock(ManagedThingProvider.class);
        HomekitMdnsDiscoveryParticipant discoveryService = mock(HomekitMdnsDiscoveryParticipant.class);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.isChannelLinked(ArgumentMatchers.any(ChannelUID.class))).thenReturn(false);

        Map<Long, Accessory> accessories = new ConcurrentHashMap<>();
        accessories.put(1L, createAccessory(1L));
        accessories.put(2L, createAccessory(2L));

        HomekitAccessoryHandler handler = createHandler(accessories, HomekitBindingConstants.THING_TYPE_ACCESSORY,
                "test-accessory", capturedRunnables, thingProvider, callback, discoveryService);

        invokeMethod(handler, "onConnectedThingAccessoriesLoaded");

        assertEquals(1, capturedRunnables.size(), "Migration task should be scheduled");

        doThrow(new IllegalArgumentException("Registry error")).when(thingProvider)
                .add(ArgumentMatchers.any(Bridge.class));

        capturedRunnables.get(0).run();

        verify(thingProvider).add(ArgumentMatchers.any(Bridge.class));
        verify(thingProvider).add(ArgumentMatchers.any(Thing.class));
        verify(thingProvider, never()).remove(ArgumentMatchers.any(ThingUID.class));

        Object migrating = assertDoesNotThrow(() -> getField(handler, "migrating"));
        assertTrue(migrating instanceof AtomicBoolean);
        assertFalse(((AtomicBoolean) migrating).get());
    }

    @Test
    void testRealAccessoryJSONParsing() {
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
    public void testMigrationTriggeredForRealAccessoryJSON() {
        List<Runnable> capturedRunnables = new ArrayList<>();
        ManagedThingProvider thingProvider = mock(ManagedThingProvider.class);
        HomekitMdnsDiscoveryParticipant discoveryService = mock(HomekitMdnsDiscoveryParticipant.class);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.isChannelLinked(ArgumentMatchers.any(ChannelUID.class))).thenReturn(false);

        Accessories accessoriesDTO = GSON.fromJson(TEST_JSON, Accessories.class);
        assertNotNull(accessoriesDTO);
        assertNotNull(accessoriesDTO.accessories);

        Map<Long, Accessory> accessories = accessoriesDTO.accessories.stream()
                .collect(Collectors.toMap(acc -> acc.aid, acc -> acc));

        assertEquals(3, accessories.size());

        HomekitAccessoryHandler handler = createHandler(accessories, HomekitBindingConstants.THING_TYPE_ACCESSORY,
                "test-accessory", capturedRunnables, thingProvider, callback, discoveryService);

        invokeMethod(handler, "onConnectedThingAccessoriesLoaded");

        assertEquals(1, capturedRunnables.size(), "Migration task should be scheduled");

        capturedRunnables.get(0).run();

        verify(discoveryService, times(1)).suppressId("test-unique-id", true);
        verifyNoMoreInteractions(discoveryService);

        verify(thingProvider, times(2)).add(ArgumentMatchers.any(Thing.class));
        verify(thingProvider).remove(ArgumentMatchers.any(ThingUID.class));
    }
}
