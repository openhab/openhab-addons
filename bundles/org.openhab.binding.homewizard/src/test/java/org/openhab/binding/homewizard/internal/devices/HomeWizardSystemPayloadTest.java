/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.homewizard.internal.devices;

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
public class HomeWizardSystemPayloadTest {

    private static final DataUtil DATA_UTIL = new DataUtil();

    @Test
    public void deserializeResponse() throws IOException {
        HomeWizardSystemPayload key = DATA_UTIL.fromJson("response-system.json", HomeWizardSystemPayload.class);
        assertThat(key, is(notNullValue()));

        assertThat(key.getWifiSsid(), is("My Wi-Fi"));
        assertThat(key.getWifiRssi(), is(-77));
        assertThat(key.isCloudEnabled(), is(true));
        assertThat(key.getUptime(), is(356));
        assertThat(key.getStatusLedBrightness(), is(100));
    }

    @Test
    public void deserializeResponseEmpty() throws IOException {
        HomeWizardSystemPayload key = DATA_UTIL.fromJson("response-empty.json", HomeWizardSystemPayload.class);
        assertThat(key, is(notNullValue()));

        assertThat(key.getWifiSsid(), is(""));
        assertThat(key.getWifiRssi(), is(0));
        assertThat(key.isCloudEnabled(), is(false));
        assertThat(key.getUptime(), is(0));
        assertThat(key.getStatusLedBrightness(), is(0));
    }
}
