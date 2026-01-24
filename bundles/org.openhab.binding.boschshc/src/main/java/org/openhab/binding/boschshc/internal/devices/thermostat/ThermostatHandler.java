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
import org.openhab.binding.boschshc.internal.services.silentmode.SilentModeService;
import org.openhab.binding.boschshc.internal.services.silentmode.dto.SilentModeServiceState;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * Handler for a thermostat device.
 *
 * @author Christian Oeing - Initial contribution
 * @author David Pace - Added silent mode service
 */
@NonNullByDefault
public final class ThermostatHandler extends AbstractThermostatHandler {

    private SilentModeService silentModeService;

    public ThermostatHandler(Thing thing) {
        super(thing);
        this.silentModeService = new SilentModeService();
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        this.registerService(this.silentModeService, this::updateChannels, List.of(CHANNEL_SILENT_MODE));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        if (CHANNEL_SILENT_MODE.equals(channelUID.getId())) {
            this.handleServiceCommand(this.silentModeService, command);
        }
    }

    /**
     * Updates the channels which are linked to the {@link SilentModeService} of the device.
     * 
     * @param state current state of {@link SilentModeService}
     */
    private void updateChannels(SilentModeServiceState state) {
        super.updateState(CHANNEL_SILENT_MODE, state.toOnOffType());
    }
}
