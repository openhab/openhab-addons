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
package org.openhab.binding.boschshc.internal.services.displayedtemperatureconfiguration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;
import org.openhab.binding.boschshc.internal.services.displayedtemperatureconfiguration.dto.DisplayedTemperatureConfigurationServiceState;
import org.openhab.binding.boschshc.internal.services.displayedtemperatureconfiguration.dto.DisplayedTemperatureState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;

/**
 * Service to configure whether the measured temperature or the setpoint temperature is displayed.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class DisplayedTemperatureConfigurationService
        extends BoschSHCService<DisplayedTemperatureConfigurationServiceState> {

    public DisplayedTemperatureConfigurationService() {
        super("DisplayedTemperatureConfiguration", DisplayedTemperatureConfigurationServiceState.class);
    }

    @Override
    public DisplayedTemperatureConfigurationServiceState handleCommand(Command command) throws BoschSHCException {
        if (command instanceof OnOffType onOffCommand) {
            DisplayedTemperatureState displayedTemperatureState = DisplayedTemperatureState
                    .fromOnOffCommand(onOffCommand);
            DisplayedTemperatureConfigurationServiceState state = new DisplayedTemperatureConfigurationServiceState();
            state.displayedTemperature = displayedTemperatureState;
            return state;
        }

        return super.handleCommand(command);
    }
}
