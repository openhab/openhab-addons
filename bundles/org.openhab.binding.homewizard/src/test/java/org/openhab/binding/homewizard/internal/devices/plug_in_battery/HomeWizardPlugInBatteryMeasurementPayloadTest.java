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
package org.openhab.binding.homewizard.internal.devices.plug_in_battery;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homewizard.internal.dto.DataUtil;

/**
 * Tests deserialization of HomeWizard Device Information API responses from JSON.
 *
 * @author Gearrel Welvaart - Initial contribution
 */
@NonNullByDefault
public class HomeWizardPlugInBatteryMeasurementPayloadTest {

    private static final DataUtil DATA_UTIL = new DataUtil();

    @Test
    public void deserializeResponse() throws IOException {
        HomeWizardPlugInBatteryMeasurementPayload key = DATA_UTIL.fromJson("response-measurement-plug-in-battery.json",
                HomeWizardPlugInBatteryMeasurementPayload.class);
        assertThat(key, is(notNullValue()));

        assertThat(key.getEnergyImport(), is(123.456));
        assertThat(key.getEnergyExport(), is(123.456));

        assertThat(key.getPower(), is(123.0));
        assertThat(key.getVoltageL1(), is(230.0));
        assertThat(key.getCurrent(), is(1.5));

        assertThat(key.getFrequency(), is(50.0));
        assertThat(key.getStateOfCharge(), is(50.0));
        assertThat(key.getCycles(), is(123));
    }

    @Test
    public void deserializeResponseEmpty() throws IOException {
        HomeWizardPlugInBatteryMeasurementPayload key = DATA_UTIL.fromJson("response-empty.json",
                HomeWizardPlugInBatteryMeasurementPayload.class);
        assertThat(key, is(notNullValue()));

        assertThat(key.getEnergyImport(), is(0.0));
        assertThat(key.getEnergyExport(), is(0.0));

        assertThat(key.getPower(), is(0.0));
        assertThat(key.getVoltageL1(), is(0.0));
        assertThat(key.getCurrent(), is(0.0));

        assertThat(key.getFrequency(), is(0.0));
        assertThat(key.getStateOfCharge(), is(0.0));
        assertThat(key.getCycles(), is(0));
    }
}
