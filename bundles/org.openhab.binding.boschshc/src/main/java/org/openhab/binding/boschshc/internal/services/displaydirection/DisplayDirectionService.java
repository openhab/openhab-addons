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
package org.openhab.binding.boschshc.internal.services.displaydirection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;
import org.openhab.binding.boschshc.internal.services.displaydirection.dto.DisplayDirectionServiceState;
import org.openhab.binding.boschshc.internal.services.displaydirection.dto.DisplayDirectionState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;

/**
 * Service to configure the display direction of Thermostat II devices.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class DisplayDirectionService extends BoschSHCService<DisplayDirectionServiceState> {

    public DisplayDirectionService() {
        super("DisplayDirection", DisplayDirectionServiceState.class);
    }

    @Override
    public DisplayDirectionServiceState handleCommand(Command command) throws BoschSHCException {
        if (command instanceof OnOffType onOffCommand) {
            DisplayDirectionState displayDirection = DisplayDirectionState.fromOnOffCommand(onOffCommand);
            DisplayDirectionServiceState state = new DisplayDirectionServiceState();
            state.direction = displayDirection;
            return state;
        }

        return super.handleCommand(command);
    }
}
