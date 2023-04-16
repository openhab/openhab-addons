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
package org.openhab.binding.boschshc.internal.services.silentmode;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;
import org.openhab.binding.boschshc.internal.services.silentmode.dto.SilentModeServiceState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;

/**
 * Service to get and set the silent mode of thermostats.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class SilentModeService extends BoschSHCService<SilentModeServiceState> {

    public SilentModeService() {
        super("SilentMode", SilentModeServiceState.class);
    }

    @Override
    public SilentModeServiceState handleCommand(Command command) throws BoschSHCException {
        if (command instanceof OnOffType onOffCommand) {
            SilentModeServiceState serviceState = new SilentModeServiceState();
            serviceState.mode = SilentModeState.fromOnOffType(onOffCommand);
            return serviceState;
        }
        return super.handleCommand(command);
    }
}
