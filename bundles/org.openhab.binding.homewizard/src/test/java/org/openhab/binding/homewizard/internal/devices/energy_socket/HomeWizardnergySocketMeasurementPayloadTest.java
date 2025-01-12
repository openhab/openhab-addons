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
package org.openhab.binding.homewizard.internal.devices.energy_socket;

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
public class HomeWizardnergySocketMeasurementPayloadTest {

    private static final DataUtil DATA_UTIL = new DataUtil();

    @Test
    public void deserializeResponse() throws IOException {
        HomeWizardEnergySocketMeasurementPayload key = DATA_UTIL.fromJson("response-measurement-energy-socket.json",
                HomeWizardEnergySocketMeasurementPayload.class);
        assertThat(key, is(notNullValue()));

        assertThat(key.getEnergyImport(), is(30.511));
        assertThat(key.getEnergyExport(), is(85.951));

        assertThat(key.getPower(), is(543.312));
        assertThat(key.getVoltage(), is(231.539));
        assertThat(key.getCurrent(), is(2.346));

        assertThat(key.getReactivePower(), is(123.456));
        assertThat(key.getApparentPower(), is(666.768));
        assertThat(key.getPowerFactor(), is(0.81688));
        assertThat(key.getFrequency(), is(50.005));
    }

    @Test
    public void deserializeResponseEmpty() throws IOException {
        HomeWizardEnergySocketMeasurementPayload key = DATA_UTIL.fromJson("response-empty.json",
                HomeWizardEnergySocketMeasurementPayload.class);
        assertThat(key, is(notNullValue()));

        assertThat(key.getEnergyImport(), is(0.0));
        assertThat(key.getEnergyExport(), is(0.0));

        assertThat(key.getPower(), is(0.0));
        assertThat(key.getVoltage(), is(0.0));
        assertThat(key.getCurrent(), is(0.0));

        assertThat(key.getReactivePower(), is(0.0));
        assertThat(key.getApparentPower(), is(0.0));
        assertThat(key.getPowerFactor(), is(0.0));
        assertThat(key.getFrequency(), is(0.0));
    }
}
