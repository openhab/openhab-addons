/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.webservice.api.json;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class DeviceIdentLabelTest {
    @Test
    public void testNullSwidsInJsonAreConvertedToEmptyList() throws IOException {
        // given:
        String json = "{ \"swids\": null }";

        // when:
        DeviceIdentLabel deviceIdentLabel = new Gson().fromJson(json, DeviceIdentLabel.class);

        // then:
        assertNotNull(deviceIdentLabel.getSwids());
        assertTrue(deviceIdentLabel.getSwids().isEmpty());
    }
}
