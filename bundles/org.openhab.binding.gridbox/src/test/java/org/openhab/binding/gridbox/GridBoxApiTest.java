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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpTimeoutException;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.gridbox.internal.GridBoxConfiguration;
import org.openhab.binding.gridbox.internal.api.GridBoxApi;
import org.openhab.binding.gridbox.internal.api.GridBoxApi.GridBoxApiAuthenticationException;
import org.openhab.binding.gridbox.internal.api.GridBoxApi.GridBoxApiException;
import org.openhab.binding.gridbox.internal.api.GridBoxApi.GridBoxApiSystemNotFoundException;
import org.openhab.binding.gridbox.internal.model.LiveData;

/**
 * The {@link GridBoxApiTest} tests the parsing functions of the {@link GridBoxApi} class.
 *
 * @author Benedikt Kuntz - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class GridBoxApiTest {

    private static final String AUTH_RESPONSE = """
            {
                "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ik16UkRSakU1UVRrd1JEQXhOVU15UlRnMVFrRTNNemRCUmpZNE5rRTFOamRCTjBZd1FrWkdOQSJ9.eyJpc3MiOiJodHRwczovL2dyaWR4LmV1LmF1dGgwLmNvbS8iLCJzdWIiOiJhdXRoMHw2M2ViYmM5ZDUyNDZkOTEwZWYyYzBkYjEiLCJhdWQiOlsibXkuZ3JpZHgiLCJodHRwczovL2dyaWR4LmV1LmF1dGgwLmNvbS91c2VyaW5mbyJdLCJpYXQiOjE3MTM0NjUzMjMsImV4cCI6MTcxMzU1MTcyMywic2NvcGUiOiJlbWFpbCBvcGVuaWQiLCJndHkiOiJwYXNzd29yZCIsImF6cCI6Im9acHI5MzRJa244T1pPSFRKRWNyZ1hramlvMEkwUTdiIn0.Hapn-J94bjKenQUVqY3lnPYQb2QEUIS-pSWZe0tEKXRyOLFmC5u5AjMxlEoNd4eC1cqmu3xySyDoCJQaaSmWSF3xNfZsdQmokiOWPfptikNwecH9JdDhtJFobME8b_tfid7tpMk4TVKVNEm6Ns86w9QyrtMkXP3GlrayHlXCL_90lSfsOA2D0V-uSV1VL_1wz0p_-9_Scl7DUyQJP9qg-H6GVF6eyA0iieaYfbJrkfPSQjK5U6-srU37GY3Ync54hYog5WfFCXH5haU1Wv_DgmcvDjHvoP1X2UpYJxrKKHSYTI-xQI3iyYSk9lFxjGcfHuRdaQmXiWGvxlsGg2SUCw",
                "id_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ik16UkRSakU1UVRrd1JEQXhOVU15UlRnMVFrRTNNemRCUmpZNE5rRTFOamRCTjBZd1FrWkdOQSJ9.eyJlbWFpbCI6ImJlbmt1bnR6QGdteC5uZXQiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImlzcyI6Imh0dHBzOi8vZ3JpZHguZXUuYXV0aDAuY29tLyIsImF1ZCI6Im9acHI5MzRJa244T1pPSFRKRWNyZ1hramlvMEkwUTdiIiwiaWF0IjoxNzEzNDY1MzIzLCJleHAiOjE3MTM1MDEzMjMsInN1YiI6ImF1dGgwfDYzZWJiYzlkNTI0NmQ5MTBlZjJjMGRiMSJ9.Sox2ftNHzQ-P7FRRFAhdLWz5RrSdfCVp1EnAgCQGMyTFq9DKNyAyTbAgUoSnLSwoofXPDit_QBKrIGblSHLOHOH0-KUmCQHL-4PajbFDVUlUe-57J7s5_OeoHUcN0Hlj_9ypbiJ0oVknoojMIgmXjJOBHNiwel8VxWAq83sn9B2Ve2E_JxqENbydkuhy15CqNg3zL8FOORxkAduJ5LwanCohutHlpXj6lH_DVYqIUUjqlB32keAPvBV5kGagOPnrohwQxnFuZS6I5MoT4Foid15hDMMf9Z8wupu8OUzIopQZ1deWr1lC9twdui6BrLP4KFxvneyfTHoi5j1NYqap5Q",
                "scope": "email openid",
                "expires_in": 86400,
                "token_type": "Bearer"
            }
            """;

    private static final String GATEWAY_RESPONSE = """
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

    private static final String LIVE_DATA_RESPONSE = """
                        {
                "batteries": [
                    {
                        "applianceID": "xxxxxxx-xxxxx-xxxxx-xxxxx-xxxxxx",
                        "capacity": 10000,
                        "nominalCapacity": 10000,
                        "power": 266,
                        "remainingCharge": 7800,
                        "stateOfCharge": 0.78
                    }
                ],
                "battery": {
                    "capacity": 10000,
                    "nominalCapacity": 10000,
                    "power": 266,
                    "remainingCharge": 7800,
                    "stateOfCharge": 0.78
                },
                "consumption": 581,
                "directConsumption": 311,
                "directConsumptionEV": 0,
                "directConsumptionHeatPump": 0,
                "directConsumptionHeater": 0,
                "directConsumptionHousehold": 311,
                "directConsumptionRate": 1,
                "evChargingStation": {
                    "power": 0
                },
                "evChargingStations": [
                    {
                        "applianceID": "xxxx-xxxxxx-xxxxxx-xxxxx-xxxxx",
                        "currentL1": 0,
                        "currentL2": 0,
                        "currentL3": 0,
                        "plugState": "PLUGGED_ON_STATION",
                        "power": 0,
                        "readingTotal": 4394
                    }
                ],
                "grid": 4,
                "gridMeterReadingNegative": 28015560000,
                "gridMeterReadingPositive": 48789000000,
                "heatPump": 0,
                "heatPumps": [
                    {
                        "applianceID": "xxxx-xxxxxx-xxxxxx-xxxxx-xxxxx",
                        "power": 0
                    }
                ],
                "measuredAt": "2024-05-04T17:26:01Z",
                "photovoltaic": 311,
                "production": 311,
                "selfConsumption": 311,
                "selfConsumptionRate": 1,
                "selfSufficiencyRate": 0.9931153184165232,
                "selfSupply": 577,
                "totalConsumption": 581
            }
                        """;
    private static final String EMPTY_LIVE_DATA_RESPONSE = """
            {
            }
            """;

    @Mock
    @NonNullByDefault({})
    private HttpResponse<String> response;

    @Spy
    @NonNullByDefault({})
    private HttpClient httpClient;

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

    @Test
    public void testGetIdToken() throws IOException, InterruptedException, GridBoxApiAuthenticationException {
        @SuppressWarnings("unchecked")
        Class<BodyHandler<String>> clazz = (Class<BodyHandler<String>>) HttpResponse.BodyHandlers.ofString().getClass();
        when(httpClient.send(any(), any(clazz))).thenReturn(response);
        GridBoxApi api = new GridBoxApi(httpClient);
        GridBoxConfiguration config = new GridBoxConfiguration();

        prepareResponse(200, AUTH_RESPONSE);
        String idToken = api.getIdToken(config);
        assertEquals(
                "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ik16UkRSakU1UVRrd1JEQXhOVU15UlRnMVFrRTNNemRCUmpZNE5rRTFOamRCTjBZd1FrWkdOQSJ9.eyJlbWFpbCI6ImJlbmt1bnR6QGdteC5uZXQiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImlzcyI6Imh0dHBzOi8vZ3JpZHguZXUuYXV0aDAuY29tLyIsImF1ZCI6Im9acHI5MzRJa244T1pPSFRKRWNyZ1hramlvMEkwUTdiIiwiaWF0IjoxNzEzNDY1MzIzLCJleHAiOjE3MTM1MDEzMjMsInN1YiI6ImF1dGgwfDYzZWJiYzlkNTI0NmQ5MTBlZjJjMGRiMSJ9.Sox2ftNHzQ-P7FRRFAhdLWz5RrSdfCVp1EnAgCQGMyTFq9DKNyAyTbAgUoSnLSwoofXPDit_QBKrIGblSHLOHOH0-KUmCQHL-4PajbFDVUlUe-57J7s5_OeoHUcN0Hlj_9ypbiJ0oVknoojMIgmXjJOBHNiwel8VxWAq83sn9B2Ve2E_JxqENbydkuhy15CqNg3zL8FOORxkAduJ5LwanCohutHlpXj6lH_DVYqIUUjqlB32keAPvBV5kGagOPnrohwQxnFuZS6I5MoT4Foid15hDMMf9Z8wupu8OUzIopQZ1deWr1lC9twdui6BrLP4KFxvneyfTHoi5j1NYqap5Q",
                idToken);

        prepareResponse(200, "    ");
        assertThrows(GridBoxApiAuthenticationException.class, () -> api.getIdToken(config));

        prepareResponse(404, null);
        assertThrows(GridBoxApiAuthenticationException.class, () -> api.getIdToken(config));

        when(httpClient.send(any(), any(clazz))).thenThrow(HttpTimeoutException.class);
        assertThrows(HttpTimeoutException.class, () -> api.getIdToken(config));
    }

    @Test
    public void testGetSystemId()
            throws IOException, InterruptedException, GridBoxApiAuthenticationException, GridBoxApiException {
        @SuppressWarnings("unchecked")
        Class<BodyHandler<String>> clazz = (Class<BodyHandler<String>>) HttpResponse.BodyHandlers.ofString().getClass();
        when(httpClient.send(any(), any(clazz))).thenReturn(response);
        GridBoxApi api = new GridBoxApi(httpClient);
        GridBoxConfiguration config = new GridBoxConfiguration();

        prepareResponse(200, GATEWAY_RESPONSE);
        String systemId = api.getSystemId(config);
        assertEquals("09abb340-7739-494b-afcc-fbffecbe7ccc", systemId);

        prepareResponse(200, "    ");
        assertThrows(GridBoxApiException.class, () -> api.getSystemId(config));

        prepareResponse(403, null);
        assertThrows(GridBoxApiAuthenticationException.class, () -> api.getSystemId(config));

        when(httpClient.send(any(), any(clazz))).thenThrow(HttpTimeoutException.class);
        assertThrows(HttpTimeoutException.class, () -> api.getSystemId(config));
    }

    @Test
    public void testRetrieveLiveData() throws IOException, InterruptedException, GridBoxApiAuthenticationException,
            GridBoxApiException, GridBoxApiSystemNotFoundException {
        @SuppressWarnings("unchecked")
        Class<BodyHandler<String>> clazz = (Class<BodyHandler<String>>) HttpResponse.BodyHandlers.ofString().getClass();
        when(httpClient.send(any(), any(clazz))).thenReturn(response);
        GridBoxApi api = new GridBoxApi(httpClient);
        GridBoxConfiguration config = new GridBoxConfiguration();

        prepareResponse(200, LIVE_DATA_RESPONSE);
        api.retrieveLiveData(config, d -> {
            assertNotNull(d);
            assertEquals(311, d.getSelfConsumption());
        });

        // check that empty responses (all values zero) are ignored
        prepareResponse(200, EMPTY_LIVE_DATA_RESPONSE);
        api.retrieveLiveData(config, d -> {
            // make sure that the responseHandler is not called
            fail();
        });

        Consumer<LiveData> doNothing = d -> {

        };

        prepareResponse(200, LIVE_DATA_RESPONSE.replace('{', '<'));
        assertThrows(GridBoxApiException.class, () -> api.retrieveLiveData(config, doNothing));

        prepareResponse(404, null);
        assertThrows(GridBoxApiSystemNotFoundException.class, () -> api.retrieveLiveData(config, doNothing));

        prepareResponse(403, null);
        assertThrows(GridBoxApiAuthenticationException.class, () -> api.retrieveLiveData(config, doNothing));

        when(httpClient.send(any(), any(clazz))).thenThrow(HttpTimeoutException.class);
        assertThrows(HttpTimeoutException.class, () -> api.retrieveLiveData(config, doNothing));
    }

    private void prepareResponse(int statusCode, @Nullable String body) {
        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);
    }
}
