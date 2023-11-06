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
package org.openhab.binding.max.internal.message;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.max.internal.device.DeviceConfiguration;
import org.openhab.binding.max.internal.device.DeviceType;

/**
 * Tests cases for {@link DeviceConfiguration}.
 *
 * @author Andreas Heil - Initial contribution
 * @author Marcel Verpaalen - OH2 Version and updates
 */
@NonNullByDefault
public class ConfigurationTest {

    public static final String RAW_DATA = "C:0b0da3,0gsNowIBEABLRVEwNTQ0MjQyLCQ9CQcYAzAM/wBIYViRSP1ZFE0gTSBNIEUgRSBFIEUgRSBFIEhhWJFQ/VkVUSBRIFEgRSBFIEUgRSBFIEUgSFBYWkj+WRRNIE0gTSBFIEUgRSBFIEUgRSBIUFhaSP5ZFE0gTSBNIEUgRSBFIEUgRSBFIEhQWFpI/lkUTSBNIE0gRSBFIEUgRSBFIEUgSFBYWkj+WRRNIE0gTSBFIEUgRSBFIEUgRSBIUFhaSP5ZFE0gTSBNIEUgRSBFIEUgRSBFIA==";

    private final CMessage message = new CMessage(RAW_DATA);
    private @Nullable DeviceConfiguration configuration;

    @BeforeEach
    public void before() {
        configuration = DeviceConfiguration.create(message);
    }

    @Test
    public void createTest() {
        assertNotNull(configuration);
    }

    @Test
    public void getRfAddressTest() {
        final DeviceConfiguration configuration = this.configuration;
        if (configuration != null) {
            String rfAddress = configuration.getRFAddress();
            assertEquals("0b0da3", rfAddress);
        } else {
            fail("Configuration missing");
        }
    }

    @Test
    public void getDeviceTypeTest() {
        final DeviceConfiguration configuration = this.configuration;
        if (configuration != null) {
            DeviceType deviceType = configuration.getDeviceType();
            assertEquals(DeviceType.HeatingThermostatPlus, deviceType);
        } else {
            fail("Configuration missing");
        }
    }

    @Test
    public void getSerialNumberTest() {
        final DeviceConfiguration configuration = this.configuration;
        if (configuration != null) {
            String serialNumber = configuration.getSerialNumber();
            assertEquals("KEQ0544242", serialNumber);
        } else {
            fail("Configuration missing");
        }
    }
}
