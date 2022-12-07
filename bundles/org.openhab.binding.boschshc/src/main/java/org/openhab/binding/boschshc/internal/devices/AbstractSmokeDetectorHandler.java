/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_SMOKE_CHECK;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.smokedetectorcheck.SmokeDetectorCheckService;
import org.openhab.binding.boschshc.internal.services.smokedetectorcheck.dto.SmokeDetectorCheckServiceState;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * Abstract handler implementation for devices with a smoke detector.
 *
 * @author Christian Oeing - Initial contribution
 * @author Gerd Zanker - AbstractSmokeDetectorHandler refactoring for reuse
 */
@NonNullByDefault
public abstract class AbstractSmokeDetectorHandler extends AbstractBatteryPoweredDeviceHandler {

    private SmokeDetectorCheckService smokeDetectorCheckService;

    public AbstractSmokeDetectorHandler(Thing thing) {
        super(thing);
        this.smokeDetectorCheckService = new SmokeDetectorCheckService();
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        this.registerService(smokeDetectorCheckService, this::updateChannels, List.of(CHANNEL_SMOKE_CHECK));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        switch (channelUID.getId()) {
            case CHANNEL_SMOKE_CHECK:
                this.handleServiceCommand(this.smokeDetectorCheckService, command);
                break;
        }
    }

    private void updateChannels(SmokeDetectorCheckServiceState state) {
        updateState(CHANNEL_SMOKE_CHECK, new StringType(state.value.toString()));
    }
}
