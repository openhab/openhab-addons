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
package org.openhab.binding.tado.tests;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tado.internal.CapabilitiesSupport;
import org.openhab.binding.tado.internal.api.model.ACFanLevel;
import org.openhab.binding.tado.internal.api.model.ACVerticalSwing;
import org.openhab.binding.tado.internal.api.model.AcFanSpeed;
import org.openhab.binding.tado.internal.api.model.AcModeCapabilities;
import org.openhab.binding.tado.internal.api.model.AirConditioningCapabilities;
import org.openhab.binding.tado.internal.api.model.GenericZoneCapabilities;
import org.openhab.binding.tado.internal.api.model.Power;
import org.openhab.binding.tado.internal.api.model.TadoSystemType;

/**
 * The {@link CapabilitiesSupportTest} implements tests of the capabilities support evaluator.
 *
 * @author Andrew Fiddian-Green - Initial contributions
 *
 */
@NonNullByDefault
public class CapabilitiesSupportTest {

    /**
     * Test capabilities support (heating)
     */
    @Test
    void testCapabilitiesSupportHeating() {
        GenericZoneCapabilities caps = new GenericZoneCapabilities();
        caps.setType(TadoSystemType.HEATING);
        CapabilitiesSupport capabilitiesSupport = new CapabilitiesSupport(caps);

        assertTrue(capabilitiesSupport.heatingPower());

        assertFalse(capabilitiesSupport.fanLevel());
        assertFalse(capabilitiesSupport.fanSpeed());
        assertFalse(capabilitiesSupport.horizontalSwing());
        assertFalse(capabilitiesSupport.light());
        assertFalse(capabilitiesSupport.swing());
        assertFalse(capabilitiesSupport.verticalSwing());
        assertFalse(capabilitiesSupport.acPower());
    }

    /**
     * Test capabilities support (air conditioning)
     */
    @Test
    void testCapabilitiesSupportAirContitioning() {
        AirConditioningCapabilities caps = new AirConditioningCapabilities();
        caps.setType(TadoSystemType.AIR_CONDITIONING);

        AcModeCapabilities heat = new AcModeCapabilities();
        heat.addFanLevelItem(ACFanLevel.LEVEL1);
        heat.addSwingsItem(Power.OFF);
        caps.HEAT(heat);

        AcModeCapabilities cool = new AcModeCapabilities();
        cool.addFanSpeedsItem(AcFanSpeed.AUTO);
        cool.addVerticalSwingItem(ACVerticalSwing.DOWN);
        caps.COOL(cool);

        CapabilitiesSupport capabilitiesSupport = new CapabilitiesSupport(caps);

        assertTrue(capabilitiesSupport.fanLevel());
        assertTrue(capabilitiesSupport.verticalSwing());
        assertTrue(capabilitiesSupport.acPower());
        assertTrue(capabilitiesSupport.fanSpeed());
        assertTrue(capabilitiesSupport.swing());

        assertFalse(capabilitiesSupport.horizontalSwing());
        assertFalse(capabilitiesSupport.light());
        assertFalse(capabilitiesSupport.heatingPower());
    }
}
