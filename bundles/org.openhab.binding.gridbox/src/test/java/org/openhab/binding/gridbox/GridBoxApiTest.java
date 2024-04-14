/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.gridbox;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.gridbox.internal.api.GridBoxApi;

/**
 * The {@link GridBoxApiTest} tests the parsing functions of the {@link GridBoxApi} class.
 *
 * @author Benedikt Kuntz - Initial contribution
 */
@NonNullByDefault
public class GridBoxApiTest {

    public static final String AUTH_RESPONSE = """
            {
                "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ik16UkRSakU1UVRrd1JEQXhOVU15UlRnMVFrRTNNemRCUmpZNE5rRTFOamRCTjBZd1FrWkdOQSJ9.eyJpc3MiOiJodHRwczovL2dyaWR4LmV1LmF1dGgwLmNvbS8iLCJzdWIiOiJhdXRoMHw2M2ViYmM5ZDUyNDZkOTEwZWYyYzBkYjEiLCJhdWQiOlsibXkuZ3JpZHgiLCJodHRwczovL2dyaWR4LmV1LmF1dGgwLmNvbS91c2VyaW5mbyJdLCJpYXQiOjE3MTM0NjUzMjMsImV4cCI6MTcxMzU1MTcyMywic2NvcGUiOiJlbWFpbCBvcGVuaWQiLCJndHkiOiJwYXNzd29yZCIsImF6cCI6Im9acHI5MzRJa244T1pPSFRKRWNyZ1hramlvMEkwUTdiIn0.Hapn-J94bjKenQUVqY3lnPYQb2QEUIS-pSWZe0tEKXRyOLFmC5u5AjMxlEoNd4eC1cqmu3xySyDoCJQaaSmWSF3xNfZsdQmokiOWPfptikNwecH9JdDhtJFobME8b_tfid7tpMk4TVKVNEm6Ns86w9QyrtMkXP3GlrayHlXCL_90lSfsOA2D0V-uSV1VL_1wz0p_-9_Scl7DUyQJP9qg-H6GVF6eyA0iieaYfbJrkfPSQjK5U6-srU37GY3Ync54hYog5WfFCXH5haU1Wv_DgmcvDjHvoP1X2UpYJxrKKHSYTI-xQI3iyYSk9lFxjGcfHuRdaQmXiWGvxlsGg2SUCw",
                "id_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ik16UkRSakU1UVRrd1JEQXhOVU15UlRnMVFrRTNNemRCUmpZNE5rRTFOamRCTjBZd1FrWkdOQSJ9.eyJlbWFpbCI6ImJlbmt1bnR6QGdteC5uZXQiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImlzcyI6Imh0dHBzOi8vZ3JpZHguZXUuYXV0aDAuY29tLyIsImF1ZCI6Im9acHI5MzRJa244T1pPSFRKRWNyZ1hramlvMEkwUTdiIiwiaWF0IjoxNzEzNDY1MzIzLCJleHAiOjE3MTM1MDEzMjMsInN1YiI6ImF1dGgwfDYzZWJiYzlkNTI0NmQ5MTBlZjJjMGRiMSJ9.Sox2ftNHzQ-P7FRRFAhdLWz5RrSdfCVp1EnAgCQGMyTFq9DKNyAyTbAgUoSnLSwoofXPDit_QBKrIGblSHLOHOH0-KUmCQHL-4PajbFDVUlUe-57J7s5_OeoHUcN0Hlj_9ypbiJ0oVknoojMIgmXjJOBHNiwel8VxWAq83sn9B2Ve2E_JxqENbydkuhy15CqNg3zL8FOORxkAduJ5LwanCohutHlpXj6lH_DVYqIUUjqlB32keAPvBV5kGagOPnrohwQxnFuZS6I5MoT4Foid15hDMMf9Z8wupu8OUzIopQZ1deWr1lC9twdui6BrLP4KFxvneyfTHoi5j1NYqap5Q",
                "scope": "email openid",
                "expires_in": 86400,
                "token_type": "Bearer"
            }
            """;

    public static final String GATEWAY_RESPONSE = """
            [
                {
                    "additionalIdentifiers": [
                        {
                            "identifier": "c8e78ebba2abcdefabcdef819a98eefade9e505",
                            "service": "EEBUS",
                            "type": "SKI"
                        }
                    ],
                    "applianceComposition": [
                        "EVSTATION",
                        "GRID",
                        "HEAT_PUMP",
                        "HYBRID"
                    ],
                    "connectionStatus": {
                        "contactedAt": "2024-04-18T18:37:27Z",
                        "status": "AVAILABLE"
                    },
                    "createdAt": "2023-02-14T16:54:45Z",
                    "debugModeUntil": "2023-02-17T16:54:45Z",
                    "id": "973abcdef-3f18-44b2-aed2-8a81068e1e75",
                    "internalDeviceID": "29a203a45b4cbabcdefg5f6adac7170cdf2e7908e1ccb812538bff07412353",
                    "manufacturer": "gridX",
                    "model": "4.50P-X",
                    "registeredAt": "2023-02-14T16:54:45Z",
                    "scanners": [
                        "SMA_INVERTER_IGMP_HOST_DISCOVERY",
                        "SMA_INVERTER_ARP_HOST_DISCOVERY",
                        "SMA_METER",
                        "BCONTROL_METER",
                        "SOLAREDGE_INVERTER_METER_MODBUS_TCP",
                        "SOLAREDGE_INVERTER_METER_MODBUS_RTU",
                        "SOLARLOG_MONITOR",
                        "KEBA_CHARGING_STATION",
                        "KOSTAL_INVERTER_PLENTICORE",
                        "EEBUS_GENERIC",
                        "ALFEN_NG9XX_MODBUS_CHARGING_STATION",
                        "MY_PV_AC_THOR_HEATER",
                        "BENDER_CHARGING_STATION",
                        "HEIDELBERG_ENERGY_CONTROL_MODBUS_RTU_CHARGING_STATION",
                        "RUTENBECK_TCR_IP4_IO_DEVICE"
                    ],
                    "serialnumber": "G505F88A-450-000-001-182-P-X",
                    "startcode": "4C2FE51B9EF",
                    "system": {
                        "createdAt": "2023-02-14T16:53:52Z",
                        "id": "09abb340-7739-494b-afcc-fbffecbe7ccc",
                        "metadata": {
                            "energy": {
                                "curtailment": -0.5,
                                "ems": {
                                    "agreedDynamicPVControlTerms": true,
                                    "agreedEMSTerms": true,
                                    "agreedForecastBasedEMSTerms": false,
                                    "agreedPriorityConfigurationTerms": true,
                                    "enabledDynamicPVControl": true,
                                    "enabledEMS": true,
                                    "enabledForecastBasedEMS": true,
                                    "enabledPriorityConfiguration": true
                                },
                                "heatingSystem": "Sonstige",
                                "installer": "MyInstaller",
                                "norminalPower": 12000
                            },
                            "energySupplier": {
                                "baseFee": 10,
                                "expectedConsumption": 9000,
                                "feedInTariff": 8,
                                "type": "OTHER",
                                "unitPrice": 35
                            },
                            "wizard": {
                                "step": "DONE"
                            }
                        },
                        "name": "Test User",
                        "operatingSince": "2023-02-14T16:55:51Z",
                        "solution": "HOME",
                        "updatedAt": "2024-04-07T11:39:06Z"
                    },
                    "type": "PHYSICAL",
                    "updatedAt": "2023-02-14T16:54:45Z",
                    "vendorID": "ae7c5770-df86-4b4c-8888-293sdfsd0531"
                }
            ]
            """;

    @Test
    public void testParseIdToken() {
        @SuppressWarnings("null")
        Optional<String> idTokenValue = GridBoxApi.parseIdTokenValue(AUTH_RESPONSE);
        assertTrue(idTokenValue.isPresent());
        assertEquals(
                "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ik16UkRSakU1UVRrd1JEQXhOVU15UlRnMVFrRTNNemRCUmpZNE5rRTFOamRCTjBZd1FrWkdOQSJ9.eyJlbWFpbCI6ImJlbmt1bnR6QGdteC5uZXQiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImlzcyI6Imh0dHBzOi8vZ3JpZHguZXUuYXV0aDAuY29tLyIsImF1ZCI6Im9acHI5MzRJa244T1pPSFRKRWNyZ1hramlvMEkwUTdiIiwiaWF0IjoxNzEzNDY1MzIzLCJleHAiOjE3MTM1MDEzMjMsInN1YiI6ImF1dGgwfDYzZWJiYzlkNTI0NmQ5MTBlZjJjMGRiMSJ9.Sox2ftNHzQ-P7FRRFAhdLWz5RrSdfCVp1EnAgCQGMyTFq9DKNyAyTbAgUoSnLSwoofXPDit_QBKrIGblSHLOHOH0-KUmCQHL-4PajbFDVUlUe-57J7s5_OeoHUcN0Hlj_9ypbiJ0oVknoojMIgmXjJOBHNiwel8VxWAq83sn9B2Ve2E_JxqENbydkuhy15CqNg3zL8FOORxkAduJ5LwanCohutHlpXj6lH_DVYqIUUjqlB32keAPvBV5kGagOPnrohwQxnFuZS6I5MoT4Foid15hDMMf9Z8wupu8OUzIopQZ1deWr1lC9twdui6BrLP4KFxvneyfTHoi5j1NYqap5Q",
                idTokenValue.get());
    }

    @Test
    public void testParseInvalidIdToken() {
        @SuppressWarnings("null")
        Optional<String> idTokenValue = GridBoxApi.parseIdTokenValue(AUTH_RESPONSE.replace('_', 'x'));
        assertFalse(idTokenValue.isPresent());

        Optional<String> idTokenValue2 = GridBoxApi.parseIdTokenValue("_");
        assertFalse(idTokenValue2.isPresent());
    }

    @Test
    public void testParseSystemId() {
        @SuppressWarnings("null")
        Optional<String> systemIdValue = GridBoxApi.parseSystemIdValue(GATEWAY_RESPONSE);
        assertTrue(systemIdValue.isPresent());
        assertEquals("09abb340-7739-494b-afcc-fbffecbe7ccc", systemIdValue.get());
    }

    @Test
    public void testParseInvalidSystemId() {
        @SuppressWarnings("null")
        Optional<String> systemIdValue = GridBoxApi.parseSystemIdValue(GATEWAY_RESPONSE.replace("system", "systemxxx"));
        assertFalse(systemIdValue.isPresent());

        Optional<String> systemIdValue2 = GridBoxApi.parseSystemIdValue("_");
        assertFalse(systemIdValue2.isPresent());
    }
}
