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
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc1;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcThermostat;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NhcThermostat1} class represents the thermostat Niko Home Control I communication object. It contains all
 * fields representing a Niko Home Control thermostat and has methods to set the thermostat in Niko Home Control and
 * receive thermostat updates.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NhcThermostat1 extends NhcThermostat {

    private final Logger logger = LoggerFactory.getLogger(NhcThermostat1.class);

    NhcThermostat1(String id, String name, @Nullable String location, NikoHomeControlCommunication nhcComm) {
        super(id, name, location, nhcComm);
    }

    /**
     * Sends thermostat mode to Niko Home Control.
     *
     * @param mode
     */
    @Override
    public void executeMode(int mode) {
        logger.debug("execute thermostat mode {} for {}", mode, id);

        nhcComm.executeThermostat(id, Integer.toString(mode));
    }

    /**
     * Sends thermostat setpoint to Niko Home Control.
     *
     * @param overrule temperature to overrule the setpoint in 0.1°C multiples
     * @param overruletime time duration in min for overrule
     */
    @Override
    public void executeOverrule(int overrule, int overruletime) {
        logger.debug("execute thermostat overrule {} during {} min for {}", overrule, overruletime, id);

        nhcComm.executeThermostat(id, overrule, overruletime);
    }
}
