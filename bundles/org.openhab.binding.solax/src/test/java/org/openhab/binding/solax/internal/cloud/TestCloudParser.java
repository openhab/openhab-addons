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
package org.openhab.binding.solax.internal.cloud;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solax.internal.connectivity.rawdata.cloud.CloudRawDataBean;
import org.openhab.binding.solax.internal.model.InverterType;

/**
 * The {@link TestCloudParser} Simple test that tests for proper parsing against a real data from the cloud
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class TestCloudParser {
    private static final String CLOUD_RESPONSE_SUCCESS = """
            {
                "success": true,
                "exception": "Query success!",
                "result": {
                    "inverterSN": "xxx",
                    "sn": "xxxx",
                    "acpower": 151.0,
                    "yieldtoday": 0.9,
                    "yieldtotal": 7339.5,
                    "feedinpower": -925.0,
                    "feedinenergy": 147.2,
                    "consumeenergy": 2536.4,
                    "feedinpowerM2": 0.0,
                    "soc": 27.0,
                    "peps1": 56.3,
                    "peps2": null,
                    "peps3": null,
                    "inverterType": "15",
                    "inverterStatus": "110",
                    "uploadTime": "2023-11-28 18:34:17",
                    "batPower": 1245.0,
                    "powerdc1": 55.0,
                    "powerdc2": 670.0,
                    "powerdc3": null,
                    "powerdc4": null,
                    "batStatus": "0"
                },
                "code": 0
            }
                                    """;

    private static final String CLOUD_RESPONSE_ERROR = """
            {"success":false,"exception":"error","result":null,"code":2001}
            """;

    @Test
    public void testPositiveScenario() throws IOException {
        CloudRawDataBean bean = CloudRawDataBean.fromJson(CLOUD_RESPONSE_SUCCESS);
        assertTrue(bean.isSuccess(), "Overall response success");
        assertEquals(bean.getOverallResult(), CloudRawDataBean.QUERY_SUCCESS, "Query success string response");

        InverterType type = bean.getInverterType();
        assertEquals(InverterType.X1_HYBRID_G4, type, "Inverter type not recognized properly");

        assertEquals(110, bean.getInverterWorkModeCode());
        assertEquals("110", bean.getInverterWorkMode());

        assertEquals("xxx", bean.getInverterSerialNumber());
        assertEquals("xxxx", bean.getWifiSerialNumber());

        assertEquals(151, bean.getInverterOutputPower(), "AC/Inverter output power");
        assertEquals(0.9, bean.getYieldToday(), "Yield today");
        assertEquals(7339.5, bean.getYieldTotal(), "Yield total");
        assertEquals(-925.0, bean.getFeedInPower(), "Feed-in power");
        assertEquals(147.2, bean.getFeedInEnergy(), "Feed-in energy");
        assertEquals(2536.4, bean.getConsumeEnergy(), "Consume energy");
        assertEquals(0, bean.getFeedInPowerM2(), "Feed in power M2");
        assertEquals(56.3, bean.getEPSPowerR(), "EPS power R");

        assertEquals(1245, bean.getBatteryPower(), "Battery power");
        assertEquals(55, bean.getPowerPv1(), "PV1");
        assertEquals(670, bean.getPowerPv2(), "PV2");
        assertEquals(0, bean.getBatteryStatus(), "Battery status");
        assertEquals(0, bean.getCode(), "Return code");

        ZoneId zoneId = ZoneId.of("CET");
        assertEquals(ZonedDateTime.of(LocalDateTime.of(2023, 11, 28, 18, 34, 17), zoneId), bean.getUploadTime(zoneId),
                "Upload time");
    }

    @Test
    public void testNegativeScenario() throws IOException {
        CloudRawDataBean bean = CloudRawDataBean.fromJson(CLOUD_RESPONSE_ERROR);
        assertFalse(bean.isSuccess(), "Overall response success");
        assertEquals(bean.getOverallResult(), CloudRawDataBean.ERROR, "Expected error as a response");
    }

    @Test
    public void testEmptyResponse() throws IOException {
        CloudRawDataBean bean = CloudRawDataBean.fromJson("");
        assertFalse(bean.isSuccess(), "Overall response success");
        assertEquals(bean.getOverallResult(), CloudRawDataBean.ERROR, "Expected error as a response");
    }
}
