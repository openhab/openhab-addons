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
package org.openhab.binding.boschshc.internal.devices.windowcontact;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_BYPASS_STATE;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_SIGNAL_STRENGTH;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.bypass.BypassService;
import org.openhab.binding.boschshc.internal.services.bypass.dto.BypassServiceState;
import org.openhab.binding.boschshc.internal.services.communicationquality.CommunicationQualityService;
import org.openhab.binding.boschshc.internal.services.communicationquality.dto.CommunicationQualityServiceState;
import org.openhab.core.thing.Thing;

/**
 * Handler for Door/Window Contact II
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class WindowContact2Handler extends WindowContactHandler {

    public WindowContact2Handler(Thing thing) {
        super(thing);
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        this.createService(BypassService::new, this::updateChannels, List.of(CHANNEL_BYPASS_STATE), true);
        this.createService(CommunicationQualityService::new, this::updateChannels, List.of(CHANNEL_SIGNAL_STRENGTH),
                true);
    }

    private void updateChannels(BypassServiceState bypassServiceState) {
        updateState(CHANNEL_BYPASS_STATE, bypassServiceState.state.toOnOffTypeOrUndef());
    }

    private void updateChannels(CommunicationQualityServiceState communicationQualityServiceState) {
        updateState(CHANNEL_SIGNAL_STRENGTH, communicationQualityServiceState.quality.toSystemSignalStrength());
    }
}
