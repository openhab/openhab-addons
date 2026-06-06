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
package org.openhab.binding.homewizard.internal.devices.plug_in_battery;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homewizard.internal.HomeWizardBindingConstants;
import org.openhab.binding.homewizard.internal.devices.HomeWizardDeviceHandler;
import org.openhab.binding.homewizard.internal.devices.HomeWizardEnergyMeterSubHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Thing;

/**
 * The {@link HomeWizardPlugInBatteryHandler} implements functionality to handle a HomeWizard P1 Meter.
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 */
@NonNullByDefault
public class HomeWizardPlugInBatteryHandler extends HomeWizardDeviceHandler {
    /**
     * Constructor
     *
     * @param thing The thing to handle
     *
     */
    public HomeWizardPlugInBatteryHandler(Thing thing) {
        super(thing);
        supportedTypes.add(HomeWizardBindingConstants.HWE_BAT);
        supportedApiVersions = Arrays.asList(API_V2);
    }

    /**
     * Device specific handling of the returned data.
     *
     * @param payload The data obtained form the API call
     */
    @Override
    protected void handleMeasurementData(String data) {
        HomeWizardEnergyMeterSubHandler.handleMeasurementData(data, this);

        var payload = gson.fromJson(data, HomeWizardPlugInBatteryMeasurementPayload.class);
        if (payload != null) {
            updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                    HomeWizardBindingConstants.CHANNEL_STATE_OF_CHARGE, new DecimalType(payload.getStateOfCharge()));
            updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY, HomeWizardBindingConstants.CHANNEL_CYCLES,
                    new DecimalType(payload.getCycles()));
        }
        super.handleMeasurementData(data);
    }
}
