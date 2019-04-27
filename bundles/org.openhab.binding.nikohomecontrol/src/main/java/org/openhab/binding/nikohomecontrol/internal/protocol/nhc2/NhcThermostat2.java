/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc2;

import static org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.THERMOSTATMODES;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcThermostat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NhcThermostat2} class represents the thermostat Niko Home Control II communication object. It contains all
 * fields representing a Niko Home Control thermostat and has methods to set the thermostat in Niko Home Control and
 * receive thermostat updates.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NhcThermostat2 extends NhcThermostat {

    private final Logger logger = LoggerFactory.getLogger(NhcThermostat2.class);

    protected NhcThermostat2(String id, String name, @Nullable String location) {
        super(id, name, location);
    }

    @Override
    public void executeMode(int mode) {
        logger.debug("Niko Home Control: execute thermostat mode {} for {}", mode, id);

        String program = THERMOSTATMODES[mode];

        if (nhcComm != null) {
            nhcComm.executeThermostat(id, program);
        }
    }

    @Override
    public void executeOverrule(int overrule, int overruletime) {
        logger.debug("Niko Home Control: execute thermostat overrule {} during {} min for {}", overrule, overruletime,
                id);

        if (nhcComm != null) {
            nhcComm.executeThermostat(id, overrule / 10, overruletime);
        }
    }
}
