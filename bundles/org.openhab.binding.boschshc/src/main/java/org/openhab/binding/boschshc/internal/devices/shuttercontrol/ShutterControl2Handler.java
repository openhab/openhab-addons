/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.shuttercontrol;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_CHILD_PROTECTION;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_ENERGY_CONSUMPTION;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_POWER_CONSUMPTION;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_SIGNAL_STRENGTH;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.childprotection.ChildProtectionService;
import org.openhab.binding.boschshc.internal.services.childprotection.dto.ChildProtectionServiceState;
import org.openhab.binding.boschshc.internal.services.communicationquality.CommunicationQualityService;
import org.openhab.binding.boschshc.internal.services.communicationquality.dto.CommunicationQualityServiceState;
import org.openhab.binding.boschshc.internal.services.powermeter.PowerMeterService;
import org.openhab.binding.boschshc.internal.services.powermeter.dto.PowerMeterServiceState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * Handler for second generation shutter controls.
 * <p>
 * This handler is used if Shutter/Light Control II devices are configured as shutter controls.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class ShutterControl2Handler extends ShutterControlHandler {

    private final ChildProtectionService childProtectionService;

    public ShutterControl2Handler(Thing thing) {
        super(thing);
        this.childProtectionService = new ChildProtectionService();
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        createService(CommunicationQualityService::new, this::updateChannels, List.of(CHANNEL_SIGNAL_STRENGTH), true);
        registerService(childProtectionService, this::updateChannels, List.of(CHANNEL_CHILD_PROTECTION), true);
        createService(PowerMeterService::new, this::updateChannels,
                List.of(CHANNEL_POWER_CONSUMPTION, CHANNEL_ENERGY_CONSUMPTION), true);
    }

    private void updateChannels(CommunicationQualityServiceState communicationQualityServiceState) {
        updateState(CHANNEL_SIGNAL_STRENGTH, communicationQualityServiceState.quality.toSystemSignalStrength());
    }

    private void updateChannels(ChildProtectionServiceState childProtectionServiceState) {
        updateState(CHANNEL_CHILD_PROTECTION, OnOffType.from(childProtectionServiceState.childLockActive));
    }

    private void updateChannels(PowerMeterServiceState state) {
        super.updateState(CHANNEL_POWER_CONSUMPTION, new QuantityType<>(state.powerConsumption, Units.WATT));
        super.updateState(CHANNEL_ENERGY_CONSUMPTION, new QuantityType<>(state.energyConsumption, Units.WATT_HOUR));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        if (BoschSHCBindingConstants.CHANNEL_CHILD_PROTECTION.equals(channelUID.getId())
                && (command instanceof OnOffType onOffCommand)) {
            updateChildProtectionState(onOffCommand);
        }
    }

    private void updateChildProtectionState(OnOffType onOffCommand) {
        ChildProtectionServiceState childProtectionServiceState = new ChildProtectionServiceState();
        childProtectionServiceState.childLockActive = onOffCommand == OnOffType.ON;
        updateServiceState(childProtectionService, childProtectionServiceState);
    }
}
