/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.smokedetector;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_SMOKE_CHECK;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.devices.BoschSHCHandler;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.smokedetector.SmokeDetectorService;
import org.openhab.binding.boschshc.internal.services.smokedetector.dto.SmokeDetectorServiceState;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The smoke detector warns you in case of fire.
 *
 * @author Christian Oeing - Initial contribution
 */
@NonNullByDefault
public class SmokeDetectorHandler extends BoschSHCHandler {

    private SmokeDetectorService smokeDetectorService;

    public SmokeDetectorHandler(Thing thing) {
        super(thing);
        this.smokeDetectorService = new SmokeDetectorService();
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();
        this.registerService(this.smokeDetectorService, this::updateChannels, List.of(CHANNEL_SMOKE_CHECK));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        switch (channelUID.getId()) {
            case CHANNEL_SMOKE_CHECK:
                this.handleServiceCommand(this.smokeDetectorService, command);
                break;
        }
    }

    private void updateChannels(SmokeDetectorServiceState state) {
        updateState(CHANNEL_SMOKE_CHECK, new StringType(state.value.toString()));
    }
}
