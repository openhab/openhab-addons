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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homewizard.internal.dto.DataUtil;

/**
 * Tests deserialization of HomeWizard API responses from JSON.
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 *
 */
@NonNullByDefault
public class HomeWizardP1MeterBatteriesPayloadTest {

    private static final DataUtil DATA_UTIL = new DataUtil();

    @Test
    public void deserializeResponse() throws IOException {
        HomeWizardP1MeterBatteriesPayload key = DATA_UTIL.fromJson("response-batteries-p1-meter.json",
                HomeWizardP1MeterBatteriesPayload.class);
        assertThat(key, is(notNullValue()));

        assertThat(key.getMode(), is("to_full"));
        assertThat(key.getPower(), is(1599));
        assertThat(key.getTargetPower(), is(1600));
        assertThat(key.getMaxConsumption(), is(1600));
        assertThat(key.getMaxProduction(), is(800));
    }

    @Test
    public void deserializeResponseEmpty() throws IOException {
        HomeWizardP1MeterBatteriesPayload key = DATA_UTIL.fromJson("response-empty.json",
                HomeWizardP1MeterBatteriesPayload.class);
        assertThat(key, is(notNullValue()));

        assertThat(key.getMode(), is(""));
        assertThat(key.getPower(), is(0));
        assertThat(key.getTargetPower(), is(0));
        assertThat(key.getMaxConsumption(), is(0));
        assertThat(key.getMaxProduction(), is(0));
    }
}
