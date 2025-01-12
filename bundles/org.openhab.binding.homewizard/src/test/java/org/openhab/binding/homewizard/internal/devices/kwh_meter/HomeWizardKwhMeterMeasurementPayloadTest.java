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
package org.openhab.binding.homewizard.internal.devices.kwh_meter;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homewizard.internal.devices.energy_socket.HomeWizardEnergySocketMeasurementPayload;
import org.openhab.binding.homewizard.internal.dto.DataUtil;

/**
 * Tests deserialization of HomeWizard API responses from JSON.
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 */
@NonNullByDefault
public class HomeWizardKwhMeterMeasurementPayloadTest {

    private static final DataUtil DATA_UTIL = new DataUtil();

    @Test
    public void deserializeResponse() throws IOException {
        HomeWizardEnergySocketMeasurementPayload key = DATA_UTIL.fromJson("response-measurement-kwh-meter.json",
                HomeWizardEnergySocketMeasurementPayload.class);
        assertThat(key, is(notNullValue()));

        assertThat(key.getCurrent(), is(30.999));
        assertThat(key.getCurrentL1(), is(0.0));
        assertThat(key.getCurrentL2(), is(15.521));
        assertThat(key.getCurrentL3(), is(15.477));

        assertThat(key.getPower(), is(-543.0));
        assertThat(key.getPowerL1(), is(0.0));
        assertThat(key.getPowerL2(), is(3547.015));
        assertThat(key.getPowerL3(), is(3553.263));

        assertThat(key.getVoltageL1(), is(230.751));
        assertThat(key.getVoltageL2(), is(228.391));
        assertThat(key.getVoltageL3(), is(229.612));

        assertThat(key.getEnergyExport(), is(0.0));
        assertThat(key.getEnergyExportT1(), is(8874.0));
        assertThat(key.getEnergyImport(), is(2940.101));
        assertThat(key.getEnergyImportT1(), is(10830.511));

        assertThat(key.getReactivePower(), is(-429.025));
        assertThat(key.getApparentPower(), is(7112.293));
        assertThat(key.getFrequency(), is(49.926));
    }

    @Test
    public void deserializeResponseEmpty() throws IOException {
        HomeWizardEnergySocketMeasurementPayload key = DATA_UTIL.fromJson("response-empty.json",
                HomeWizardEnergySocketMeasurementPayload.class);
        assertThat(key, is(notNullValue()));

        assertThat(key.getCurrent(), is(0.0));
        assertThat(key.getCurrentL1(), is(0.0));
        assertThat(key.getCurrentL2(), is(0.0));
        assertThat(key.getCurrentL3(), is(0.0));
        assertThat(key.getPower(), is(0.0));
        assertThat(key.getPowerL1(), is(0.0));
        assertThat(key.getPowerL2(), is(0.0));
        assertThat(key.getPowerL3(), is(0.0));
        assertThat(key.getVoltage(), is(0.0));
        assertThat(key.getVoltageL1(), is(0.0));
        assertThat(key.getVoltageL2(), is(0.0));
        assertThat(key.getVoltageL3(), is(0.0));

        assertThat(key.getEnergyExportT1(), is(0.0));
        assertThat(key.getEnergyExportT2(), is(0.0));
        assertThat(key.getEnergyImportT1(), is(0.0));
        assertThat(key.getEnergyImportT2(), is(0.0));

        assertThat(key.getReactivePower(), is(0.0));
        assertThat(key.getApparentPower(), is(0.0));
        assertThat(key.getFrequency(), is(0.0));
    }
}
