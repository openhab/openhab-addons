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
package org.openhab.binding.nikohomecontrol.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link NhcThermostatEvent} interface is used to pass thermostat events received from the Niko Home Control
 * controller to
 * the consuming client. It is designed to pass events to openHAB handlers that implement this interface. Because of
 * the design, the org.openhab.binding.nikohomecontrol.internal.protocol package can be extracted and used independent
 * of openHAB.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public interface NhcThermostatEvent {

    /**
     * This method is called when thermostat event is received from the Niko Home Control controller.
     *
     * @param measured current temperature in 0.1°C multiples
     * @param setpoint the setpoint temperature in 0.1°C multiples
     * @param mode thermostat mode 0 = day, 1 = night, 2 = eco, 3 = off, 4 = cool, 5 = prog1, 6 = prog2, 7 = prog3
     * @param overrule the overrule temperature in 0.1°C multiples
     * @param demand 0 if no demand, > 0 if heating, < 0 if cooling
     */
    public void thermostatEvent(int measured, int setpoint, int mode, int overrule, int demand);

    /**
     * Called to indicate the thermostat has been initialized.
     *
     */
    public void thermostatInitialized();

    /**
     * Called to indicate the thermostat has been removed from the Niko Home Control controller.
     *
     */
    public void thermostatRemoved();
}
