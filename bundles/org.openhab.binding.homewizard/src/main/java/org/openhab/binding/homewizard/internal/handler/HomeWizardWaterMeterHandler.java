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
package org.openhab.binding.homewizard.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homewizard.internal.HomeWizardBindingConstants;
import org.openhab.binding.homewizard.internal.dto.DataPayload;
import org.openhab.core.i18n.TimeZoneProvider;
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
 */
@NonNullByDefault
public class HomeWizardWaterMeterHandler extends HomeWizardP1MeterHandler {

    /**
     * Constructor
     *
     * @param thing The thing to handle
     * @param timeZoneProvider The TimeZoneProvider
     */
    public HomeWizardWaterMeterHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
    }

    /**
     * Not listening to any commands.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * Device specific handling of the returned payload.
     *
     * @param payload The data parsed from the Json file
     */
    @Override
    protected void handleDataPayload(DataPayload payload) {
        updateState(HomeWizardBindingConstants.CHANNEL_CURRENT_WATER,
                new QuantityType<>(payload.getCurrentWaterLPM(), Units.LITRE_PER_MINUTE));
        updateState(HomeWizardBindingConstants.CHANNEL_TOTAL_WATER,
                new QuantityType<>(payload.getTotalWaterM3(), SIUnits.CUBIC_METRE));
    }
}
