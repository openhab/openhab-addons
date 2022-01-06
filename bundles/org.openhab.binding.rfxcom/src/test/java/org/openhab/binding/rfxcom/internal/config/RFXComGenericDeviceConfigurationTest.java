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
package org.openhab.binding.rfxcom.internal.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComInvalidParameterException;

/**
 * Configuration class for generic devices.
 *
 * @author James Hewitt-Thomas - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
public class RFXComGenericDeviceConfigurationTest {

    private RFXComGenericDeviceConfiguration config;

    @BeforeEach
    public void before() {
        config = new RFXComGenericDeviceConfiguration();
    }

    @Test
    public void testNoDeviceId() {
        config.subType = "PT2262";
        assertThrows(RFXComInvalidParameterException.class, () -> config.parseAndValidate());
    }

    @Test
    public void testNoSubType() {
        config.deviceId = "90000";
        assertThrows(RFXComInvalidParameterException.class, () -> config.parseAndValidate());
    }

    @Test
    public void testValidConfig() {
        config.deviceId = "90000";
        config.subType = "PT2262";
        assertDoesNotThrow(() -> config.parseAndValidate());
    }
}
