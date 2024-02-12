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

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_CHILD_PROTECTION;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.devices.AbstractPowerSwitchHandler;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.childprotection.ChildProtectionService;
import org.openhab.binding.boschshc.internal.services.childprotection.dto.ChildProtectionServiceState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * Handler for light switches.
 * <p>
 * This handler is also used for logical light switch circuits that are part of
 * a larger parent device, for example a Light Control II unit which exposes two
 * light switches as child devices.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class LightSwitchHandler extends AbstractPowerSwitchHandler {

    private ChildProtectionService childProtectionService;

    public LightSwitchHandler(Thing thing) {
        super(thing);
        this.childProtectionService = new ChildProtectionService();
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        registerService(childProtectionService, this::updateChannels, List.of(CHANNEL_CHILD_PROTECTION), true);
    }

    private void updateChannels(ChildProtectionServiceState childProtectionServiceState) {
        updateState(CHANNEL_CHILD_PROTECTION, OnOffType.from(childProtectionServiceState.childLockActive));
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
