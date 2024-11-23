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
package org.openhab.binding.boschshc.internal.devices.lightcontrol;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_BRIGHTNESS;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_CHILD_PROTECTION;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_SIGNAL_STRENGTH;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.devices.AbstractPowerSwitchHandler;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.childprotection.ChildProtectionService;
import org.openhab.binding.boschshc.internal.services.childprotection.dto.ChildProtectionServiceState;
import org.openhab.binding.boschshc.internal.services.communicationquality.CommunicationQualityService;
import org.openhab.binding.boschshc.internal.services.communicationquality.dto.CommunicationQualityServiceState;
import org.openhab.binding.boschshc.internal.services.multilevelswitch.MultiLevelSwitchService;
import org.openhab.binding.boschshc.internal.services.multilevelswitch.dto.MultiLevelSwitchServiceState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * Handler for Bosch Smart Home dimmers.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class DimmerHandler extends AbstractPowerSwitchHandler {

    private MultiLevelSwitchService multiLevelSwitchService;
    private ChildProtectionService childProtectionService;

    public DimmerHandler(Thing thing) {
        super(thing);

        this.multiLevelSwitchService = new MultiLevelSwitchService();
        this.childProtectionService = new ChildProtectionService();
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        createService(CommunicationQualityService::new, this::updateChannels, List.of(CHANNEL_SIGNAL_STRENGTH), true);
        registerService(multiLevelSwitchService, this::updateChannels, List.of(CHANNEL_BRIGHTNESS), true);
        registerService(childProtectionService, this::updateChannels, List.of(CHANNEL_CHILD_PROTECTION), true);
    }

    private void updateChannels(MultiLevelSwitchServiceState serviceState) {
        super.updateState(CHANNEL_BRIGHTNESS, serviceState.toPercentType());
    }

    private void updateChannels(CommunicationQualityServiceState communicationQualityServiceState) {
        updateState(CHANNEL_SIGNAL_STRENGTH, communicationQualityServiceState.quality.toSystemSignalStrength());
    }

    private void updateChannels(ChildProtectionServiceState childProtectionServiceState) {
        super.updateState(CHANNEL_CHILD_PROTECTION, OnOffType.from(childProtectionServiceState.childLockActive));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        if (CHANNEL_CHILD_PROTECTION.equals(channelUID.getId()) && (command instanceof OnOffType onOffCommand)) {
            updateChildProtectionState(onOffCommand);
        } else if (CHANNEL_BRIGHTNESS.equals(channelUID.getId()) && command instanceof PercentType percentCommand) {
            updateMultiLevelSwitchState(percentCommand);
        }
    }

    private void updateChildProtectionState(OnOffType onOffCommand) {
        ChildProtectionServiceState childProtectionServiceState = new ChildProtectionServiceState();
        childProtectionServiceState.childLockActive = onOffCommand == OnOffType.ON;
        updateServiceState(childProtectionService, childProtectionServiceState);
    }

    private void updateMultiLevelSwitchState(PercentType percentCommand) {
        MultiLevelSwitchServiceState serviceState = new MultiLevelSwitchServiceState();
        serviceState.level = percentCommand.intValue();
        this.updateServiceState(multiLevelSwitchService, serviceState);
    }
}
