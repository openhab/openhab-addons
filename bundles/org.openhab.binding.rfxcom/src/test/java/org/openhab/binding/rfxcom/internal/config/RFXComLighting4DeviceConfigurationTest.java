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
package org.openhab.binding.rfxcom.internal.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Configuration class for Lighting 4 devices.
 *
 * @author James Hewitt-Thomas - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
public class RFXComLighting4DeviceConfigurationTest {

    private RFXComLighting4DeviceConfiguration config;

    @BeforeEach
    public void before() {
        config = new RFXComLighting4DeviceConfiguration();
        config.deviceId = "90000";
        config.subType = "PT2262";
    }

    @Test
    public void testConfig() {
        assertDoesNotThrow(() -> config.parseAndValidate());
    }
}
