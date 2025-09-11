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
package org.openhab.binding.homewizard.internal.devices.water_meter;

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
public class HomeWizardWaterMeterMeasurementPayloadTest {

    private static final DataUtil DATA_UTIL = new DataUtil();

    @Test
    public void deserializeResponse() throws IOException {
        HomeWizardWaterMeterMeasurementPayload key = DATA_UTIL.fromJson("response-measurement-water-meter.json",
                HomeWizardWaterMeterMeasurementPayload.class);
        assertThat(key, is(notNullValue()));

        assertThat(key.getActiveLiter(), is(7.2));
        assertThat(key.getTotalLiter(), is(123.456));
    }

    @Test
    public void deserializeResponseEmpty() throws IOException {
        HomeWizardWaterMeterMeasurementPayload key = DATA_UTIL.fromJson("response-empty.json",
                HomeWizardWaterMeterMeasurementPayload.class);
        assertThat(key, is(notNullValue()));

        assertThat(key.getActiveLiter(), is(0.0));
        assertThat(key.getTotalLiter(), is(0.0));
    }
}
