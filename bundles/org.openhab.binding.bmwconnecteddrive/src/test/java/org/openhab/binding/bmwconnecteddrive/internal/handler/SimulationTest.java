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
package org.openhab.binding.bmwconnecteddrive.internal.handler;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bmwconnecteddrive.internal.handler.simulation.Injector;

/**
 * The {@link SimulationTest} Assures simulation is off
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SimulationTest {

    @Test
    public void testSimulationOff() {
        assertFalse(Injector.isActive(), "Simulation off");
    }
}
