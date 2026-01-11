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
package org.openhab.binding.boschshc.internal.devices.thermostat;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.displaydirection.DisplayDirectionService;
import org.openhab.binding.boschshc.internal.services.displaydirection.dto.DisplayDirectionServiceState;
import org.openhab.binding.boschshc.internal.services.displayedtemperatureconfiguration.DisplayedTemperatureConfigurationService;
import org.openhab.binding.boschshc.internal.services.displayedtemperatureconfiguration.dto.DisplayedTemperatureConfigurationServiceState;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * Handler for Thermostat II devices (including Thermostat II [+M] with Matter support).
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class Thermostat2Handler extends AbstractThermostatHandler {

    private DisplayDirectionService displayDirectionService;
    private DisplayedTemperatureConfigurationService displayedTemperatureConfigurationService;

    public Thermostat2Handler(Thing thing) {
        super(thing);
        this.displayDirectionService = new DisplayDirectionService();
        this.displayedTemperatureConfigurationService = new DisplayedTemperatureConfigurationService();
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        this.registerService(this.displayDirectionService, this::updateChannels, List.of(CHANNEL_DISPLAY_DIRECTION));
        this.registerService(this.displayedTemperatureConfigurationService, this::updateChannels,
                List.of(CHANNEL_DISPLAYED_TEMPERATURE));
    }

    private void updateChannels(DisplayDirectionServiceState displayDirectionServiceState) {
        super.updateState(CHANNEL_DISPLAY_DIRECTION, displayDirectionServiceState.direction.toOnOffCommand());
    }

    private void updateChannels(DisplayedTemperatureConfigurationServiceState displayedTemperatureConfigurationState) {
        super.updateState(CHANNEL_DISPLAYED_TEMPERATURE,
                displayedTemperatureConfigurationState.displayedTemperature.toOnOffCommand());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        if (CHANNEL_DISPLAY_DIRECTION.equals(channelUID.getId())) {
            this.handleServiceCommand(this.displayDirectionService, command);
        } else if (CHANNEL_DISPLAYED_TEMPERATURE.equals(channelUID.getId())) {
            this.handleServiceCommand(this.displayedTemperatureConfigurationService, command);
        }
    }
}
