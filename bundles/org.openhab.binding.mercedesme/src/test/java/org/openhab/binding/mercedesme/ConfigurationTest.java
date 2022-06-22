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
package org.openhab.binding.mercedesme;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.internal.AccountConfiguration;

/**
 * The {@link ConfigurationTest} Test configuration settings
 *
 * @author Bernd Weymann - Initial contribution
 */
class ConfigurationTest {

    @Test
    void testScope() {
        AccountConfiguration ac = new AccountConfiguration();
        System.out.println(ac.getScope());
        assertEquals(
                "offline_access mb:vehicle:mbdata:payasyoudrive mb:vehicle:mbdata:vehiclestatus mb:vehicle:mbdata:vehiclelock mb:vehicle:mbdata:fuelstatus mb:vehicle:mbdata:evstatus",
                ac.getScope());
    }
}
