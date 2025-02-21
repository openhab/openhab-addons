/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.homewizard.internal.devices.kwh_meter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homewizard.internal.HomeWizardBindingConstants;
import org.openhab.binding.homewizard.internal.devices.HomeWizardEnergyMeterHandler;
import org.openhab.binding.homewizard.internal.devices.energy_socket.HomeWizardEnergySocketMeasurementPayload;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;

/**
 * The {@link HomeWizardKwhMeterHandler} implements functionality to handle a HomeWizard kWh Meter.
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 */
@NonNullByDefault
public class HomeWizardKwhMeterHandler extends HomeWizardEnergyMeterHandler {

    /**
     * Constructor
     *
     * @param thing The thing to handle
     *
     */
    public HomeWizardKwhMeterHandler(Thing thing) {
        super(thing);
        supportedTypes.add(HomeWizardBindingConstants.HWE_KWH1);
        supportedTypes.add(HomeWizardBindingConstants.SDM230_WIFI);
        supportedTypes.add(HomeWizardBindingConstants.HWE_KWH3);
        supportedTypes.add(HomeWizardBindingConstants.SDM630_WIFI);
    }

    /**
     * Device specific handling of the returned data.
     *
     * @param payload The data obtained form the API call
     */
    @Override
    protected void handleDataPayload(String data) {
        super.handleDataPayload(data);

        var payload = gson.fromJson(data, HomeWizardEnergySocketMeasurementPayload.class);
        if (payload != null) {
            updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                    HomeWizardBindingConstants.CHANNEL_REACTIVE_POWER,
                    new QuantityType<>(payload.getReactivePower(), Units.VAR));
            updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                    HomeWizardBindingConstants.CHANNEL_APPARENT_POWER,
                    new QuantityType<>(payload.getApparentPower(), Units.VOLT_AMPERE));
            updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                    HomeWizardBindingConstants.CHANNEL_POWER_FACTOR, new DecimalType(payload.getPowerFactor()));
        }
    }
}
