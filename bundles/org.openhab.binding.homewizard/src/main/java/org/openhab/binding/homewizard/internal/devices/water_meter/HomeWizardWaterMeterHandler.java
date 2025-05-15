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
package org.openhab.binding.homewizard.internal.devices.water_meter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homewizard.internal.HomeWizardBindingConstants;
import org.openhab.binding.homewizard.internal.devices.HomeWizardDeviceHandler;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The {@link HomeWizardWaterMeterHandler} implements functionality to handle a HomeWizard Watermeter.
 *
 * @author Daniël van Os - Initial contribution
 * @author Gearrel Welvaart - Some minor structural changes
 *
 */
@NonNullByDefault
public class HomeWizardWaterMeterHandler extends HomeWizardDeviceHandler {

    /**
     * Constructor
     *
     * @param thing The thing to handle
     *
     */
    public HomeWizardWaterMeterHandler(Thing thing) {
        super(thing);
        supportedTypes.add(HomeWizardBindingConstants.HWE_WTR);
    }

    /**
     * Not listening to any commands.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * Device specific handling of the returned data.
     *
     * @param payload The data obtained form the API call
     */
    @Override
    protected void handleDataPayload(String data) {
        var payload = gson.fromJson(data, HomeWizardWaterMeterMeasurementPayload.class);
        if (payload != null) {
            if (!thing.getThingTypeUID().equals(HomeWizardBindingConstants.THING_TYPE_WATERMETER)) {
                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_WATER,
                        HomeWizardBindingConstants.CHANNEL_ACTIVE_LITER,
                        new QuantityType<>(payload.getActiveLiter(), Units.LITRE_PER_MINUTE));
                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_WATER,
                        HomeWizardBindingConstants.CHANNEL_TOTAL_LITER,
                        new QuantityType<>(payload.getTotalLiter(), SIUnits.CUBIC_METRE));
            } else {
                updateState(HomeWizardBindingConstants.LEGACY_CHANNEL_ACTIVE_LITER,
                        new QuantityType<>(payload.getActiveLiter(), Units.LITRE_PER_MINUTE));
                updateState(HomeWizardBindingConstants.LEGACY_CHANNEL_TOTAL_LITER,
                        new QuantityType<>(payload.getTotalLiter(), SIUnits.CUBIC_METRE));
            }
        }
    }
}
