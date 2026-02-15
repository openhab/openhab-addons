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
package org.openhab.binding.boschshc.internal.devices.presence;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_PRESENCE_SIMULATION_ENABLED;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.devices.BoschSHCHandler;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.presence.PresenceSimulationConfigurationService;
import org.openhab.binding.boschshc.internal.services.presence.dto.PresenceSimulationConfigurationServiceState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * Handler for the presence simulation system.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class PresenceSimulationHandler extends BoschSHCHandler {

    private PresenceSimulationConfigurationService presenceSimulationConfigurationService;

    public PresenceSimulationHandler(Thing thing) {
        super(thing);
        this.presenceSimulationConfigurationService = new PresenceSimulationConfigurationService();
    }

    @Override
    public @Nullable String getBoschID() {
        return BoschSHCBindingConstants.SERVICE_PRESENCE_SIMULATION;
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        registerService(this.presenceSimulationConfigurationService, this::updateChannels,
                List.of(CHANNEL_PRESENCE_SIMULATION_ENABLED));
    }

    private void updateChannels(PresenceSimulationConfigurationServiceState state) {
        super.updateState(CHANNEL_PRESENCE_SIMULATION_ENABLED, OnOffType.from(state.enabled));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        if (CHANNEL_PRESENCE_SIMULATION_ENABLED.equals(channelUID.getId())
                && command instanceof OnOffType onOffCommand) {
            PresenceSimulationConfigurationServiceState newState = new PresenceSimulationConfigurationServiceState();
            newState.enabled = onOffCommand == OnOffType.ON;
            this.updateServiceState(presenceSimulationConfigurationService, newState);
        }
    }
}
