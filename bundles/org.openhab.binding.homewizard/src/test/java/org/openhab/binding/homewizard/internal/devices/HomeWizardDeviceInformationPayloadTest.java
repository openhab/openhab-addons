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
package org.openhab.binding.homewizard.internal.devices;

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
public class HomeWizardDeviceInformationPayloadTest {

    private static final DataUtil DATA_UTIL = new DataUtil();

    @Test
    public void deserializeResponse() throws IOException {
        HomeWizardDeviceInformationPayload key = DATA_UTIL.fromJson("response-device-information-p1-meter.json",
                HomeWizardDeviceInformationPayload.class);
        assertThat(key, is(notNullValue()));

        assertThat(key.getProductName(), is("P1 Meter"));
        assertThat(key.getProductType(), is("HWE-P1"));
        assertThat(key.getSerialNumber(), is("3c39e7aabbcc"));
        assertThat(key.getFirmwareVersion(), is("5.18"));
        assertThat(key.getApiVersion(), is("v1"));
    }

    @Test
    public void deserializeResponseEmpty() throws IOException {
        HomeWizardDeviceInformationPayload key = DATA_UTIL.fromJson("response-empty.json",
                HomeWizardDeviceInformationPayload.class);
        assertThat(key, is(notNullValue()));

        assertThat(key.getProductName(), is(""));
        assertThat(key.getProductType(), is(""));
        assertThat(key.getSerialNumber(), is(""));
        assertThat(key.getFirmwareVersion(), is(""));
        assertThat(key.getApiVersion(), is(""));
    }
}
