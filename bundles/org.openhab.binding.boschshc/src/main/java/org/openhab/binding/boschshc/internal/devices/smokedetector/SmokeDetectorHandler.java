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
package org.openhab.binding.boschshc.internal.devices.smokedetector;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_SMOKE_CHECK;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.devices.AbstractBatteryPoweredDeviceHandler;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.smokedetectorcheck.SmokeDetectorCheckService;
import org.openhab.binding.boschshc.internal.services.smokedetectorcheck.dto.SmokeDetectorCheckServiceState;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;

/**
 * The smoke detector warns you in case of fire.
 *
 * @author Christian Oeing - Initial contribution
 */
@NonNullByDefault
public class SmokeDetectorHandler extends AbstractBatteryPoweredDeviceHandler {

    public SmokeDetectorHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        this.createService(SmokeDetectorCheckService::new, this::updateChannels, List.of(CHANNEL_SMOKE_CHECK));
    }

    private void updateChannels(SmokeDetectorCheckServiceState state) {
        updateState(CHANNEL_SMOKE_CHECK, new StringType(state.value.toString()));
    }
}
