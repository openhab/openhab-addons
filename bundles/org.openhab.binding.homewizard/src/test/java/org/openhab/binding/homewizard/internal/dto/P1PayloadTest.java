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
package org.openhab.binding.homewizard.internal.dto;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests deserialization of HomeWizard API responses from JSON.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class P1PayloadTest {

    private static final DataUtil DATA_UTIL = new DataUtil();

    @Test
    public void deserializeResponse() throws IOException {
        DataPayload key = DATA_UTIL.fromJson("response.json", DataPayload.class);
        assertThat(key, is(notNullValue()));

        assertThat(key.getActiveCurrent(), is(567.0));
        assertThat(key.getActiveCurrentL1(), is(-4.0));
        assertThat(key.getActiveCurrentL2(), is(2.0));
        assertThat(key.getActiveCurrentL3(), is(333.0));
        assertThat(key.getActivePowerW(), is(-543));
        assertThat(key.getActivePowerL1W(), is(-676));
        assertThat(key.getActivePowerL2W(), is(133));
        assertThat(key.getActivePowerL3W(), is(18));
        assertThat(key.getActiveVoltage(), is(220.0));
        assertThat(key.getActiveVoltageL1(), is(221.0));
        assertThat(key.getActiveVoltageL2(), is(222.0));
        assertThat(key.getActiveVoltageL3(), is(223.0));
        assertThat(key.getTotalEnergyExportT1Kwh(), is(8874.0));
        assertThat(key.getTotalEnergyExportT2Kwh(), is(7788.0));
        assertThat(key.getTotalEnergyImportT1Kwh(), is(10830.511));
        assertThat(key.getTotalEnergyImportT2Kwh(), is(2948.827));
        assertThat(key.getAnyPowerFailCount(), is(7));
        assertThat(key.getLongPowerFailCount(), is(2));
        assertThat(key.getGasTimestamp(ZoneId.systemDefault()),
                is(ZonedDateTime.of(2021, 6, 06, 14, 0, 10, 0, ZoneId.systemDefault())));
        assertThat(key.getTotalGasM3(), is(2569.646));

        assertThat(key.getMeterModel(), is("ISKRA  2M550T-101"));
        assertThat(key.getSmrVersion(), is(50));
        assertThat(key.getWifiSsid(), is("My Wi-Fi"));
        assertThat(key.getWifiStrength(), is(100));
    }

    @Test
    public void deserializeResponseEmpty() throws IOException {
        DataPayload key = DATA_UTIL.fromJson("response-empty.json", DataPayload.class);
        assertThat(key, is(notNullValue()));

        assertThat(key.getActiveCurrent(), is(0.0));
        assertThat(key.getActiveCurrentL1(), is(0.0));
        assertThat(key.getActiveCurrentL2(), is(0.0));
        assertThat(key.getActiveCurrentL3(), is(0.0));
        assertThat(key.getActivePowerW(), is(0));
        assertThat(key.getActivePowerL1W(), is(0));
        assertThat(key.getActivePowerL2W(), is(0));
        assertThat(key.getActivePowerL3W(), is(0));
        assertThat(key.getActiveVoltage(), is(0.0));
        assertThat(key.getActiveVoltageL1(), is(0.0));
        assertThat(key.getActiveVoltageL2(), is(0.0));
        assertThat(key.getActiveVoltageL3(), is(0.0));
        assertThat(key.getAnyPowerFailCount(), is(0));
        assertThat(key.getLongPowerFailCount(), is(0));
        assertThat(key.getTotalEnergyExportT1Kwh(), is(0.0));
        assertThat(key.getTotalEnergyExportT2Kwh(), is(0.0));
        assertThat(key.getTotalEnergyImportT1Kwh(), is(0.0));
        assertThat(key.getTotalEnergyImportT2Kwh(), is(0.0));
        assertThat(key.getGasTimestamp(ZoneId.systemDefault()), is(nullValue()));
        assertThat(key.getTotalGasM3(), is(0.0));

        assertThat(key.getMeterModel(), is(""));
        assertThat(key.getSmrVersion(), is(0));
        assertThat(key.getWifiSsid(), is(""));
        assertThat(key.getWifiStrength(), is(0));
    }
}
