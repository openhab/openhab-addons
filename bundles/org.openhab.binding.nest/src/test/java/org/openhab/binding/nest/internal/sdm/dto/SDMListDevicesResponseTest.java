/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.nest.internal.sdm.dto;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.openhab.binding.nest.internal.sdm.dto.SDMDataUtil.fromJson;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;

/**
 * Tests deserialization of {@link
 * org.openhab.binding.nest.internal.sdm.dto.SDMListDevicesResponse}s from JSON.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class SDMListDevicesResponseTest extends SDMDeviceTest {

    private List<SDMDevice> devices = List.of();

    @BeforeEach
    public void deserializeListDevicesResponse() throws IOException {
        SDMListDevicesResponse response = fromJson("list-devices-response.json", SDMListDevicesResponse.class);
        assertThat(response, is(notNullValue()));

        devices = response.devices;
        assertThat(devices, is(notNullValue()));
        assertThat(devices, hasSize(4));
    }

    @Override
    protected SDMDevice getThermostatDevice() throws IOException {
        return devices.get(0);
    }

    @Override
    protected SDMDevice getCameraDevice() throws IOException {
        return devices.get(1);
    }

    @Override
    protected SDMDevice getDisplayDevice() throws IOException {
        return devices.get(2);
    }

    @Override
    protected SDMDevice getDoorbellDevice() throws IOException {
        return devices.get(3);
    }
}
