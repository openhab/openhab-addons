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
package org.openhab.binding.netatmo.internal.api.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;

/**
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ScopeTest {
    @Test
    public void checkAllScopes() {
        assertEquals(
                "access_camera access_doorbell access_presence read_camera read_carbonmonoxidedetector read_doorbell read_homecoach read_presence read_smokedetector read_station read_thermostat write_camera write_carbonmonoxidedetector write_doorbell write_homecoach write_presence write_smokedetector write_station write_thermostat",
                FeatureArea.ALL_SCOPES);
    }
}
