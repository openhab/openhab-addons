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
package org.openhab.binding.homewizard.internal.devices.p1_meter;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homewizard.internal.dto.DataUtil;

/**
 * Tests deserialization of HomeWizard API responses from JSON.
 *
 * @author Leo Siepel - Initial contribution
 * @author Gearrel Welvaart - Moved and renamed
 *
 */
@NonNullByDefault
public class HomeWizardP1MeterMeasurementPayloadTest {

    private static final DataUtil DATA_UTIL = new DataUtil();

    @Test
    public void deserializeResponse() throws IOException {
        HomeWizardP1MeterMeasurementPayload key = DATA_UTIL.fromJson("response-measurement-p1-meter.json",
                HomeWizardP1MeterMeasurementPayload.class);
        assertThat(key, is(notNullValue()));

        assertThat(key.getCurrentL1(), is(-4.0));
        assertThat(key.getCurrentL2(), is(2.0));
        assertThat(key.getCurrentL3(), is(333.0));
        assertThat(key.getPower(), is(-543.0));
        assertThat(key.getPowerL1(), is(-676.0));
        assertThat(key.getPowerL2(), is(133.0));
        assertThat(key.getPowerL3(), is(18.0));
        assertThat(key.getVoltageL1(), is(221.0));
        assertThat(key.getVoltageL2(), is(222.0));
        assertThat(key.getVoltageL3(), is(223.0));
        assertThat(key.getEnergyExportT1(), is(8874.0));
        assertThat(key.getEnergyExportT2(), is(7788.0));
        assertThat(key.getEnergyImportT1(), is(10830.511));
        assertThat(key.getEnergyImportT2(), is(2948.827));
        assertThat(key.getAnyPowerFailCount(), is(7));
        assertThat(key.getLongPowerFailCount(), is(2));
        assertThat(key.getGasTimestamp(ZoneId.systemDefault()),
                is(ZonedDateTime.of(2021, 6, 06, 14, 0, 10, 0, ZoneId.systemDefault())));
        assertThat(key.getTotalGasM3(), is(2569.646));

        assertThat(key.getMeterModel(), is("ISKRA  2M550T-101"));
        assertThat(key.getProtocolVersion(), is(50));
    }

    @Test
    public void deserializeResponseEmpty() throws IOException {
        HomeWizardP1MeterMeasurementPayload key = DATA_UTIL.fromJson("response-empty.json",
                HomeWizardP1MeterMeasurementPayload.class);
        assertThat(key, is(notNullValue()));

        assertThat(key.getCurrentL1(), is(0.0));
        assertThat(key.getCurrentL2(), is(0.0));
        assertThat(key.getCurrentL3(), is(0.0));
        assertThat(key.getPower(), is(0.0));
        assertThat(key.getPowerL1(), is(0.0));
        assertThat(key.getPowerL2(), is(0.0));
        assertThat(key.getPowerL3(), is(0.0));
        assertThat(key.getVoltageL1(), is(0.0));
        assertThat(key.getVoltageL2(), is(0.0));
        assertThat(key.getVoltageL3(), is(0.0));
        assertThat(key.getAnyPowerFailCount(), is(0));
        assertThat(key.getLongPowerFailCount(), is(0));
        assertThat(key.getEnergyExportT1(), is(0.0));
        assertThat(key.getEnergyExportT2(), is(0.0));
        assertThat(key.getEnergyImportT1(), is(0.0));
        assertThat(key.getEnergyImportT2(), is(0.0));
        assertThat(key.getGasTimestamp(ZoneId.systemDefault()), is(nullValue()));
        assertThat(key.getTotalGasM3(), is(0.0));

        assertThat(key.getMeterModel(), is(""));
        assertThat(key.getProtocolVersion(), is(0));
    }
}
