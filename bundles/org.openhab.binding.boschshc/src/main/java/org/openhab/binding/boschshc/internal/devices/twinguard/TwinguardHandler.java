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
package org.openhab.binding.boschshc.internal.devices.twinguard;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.*;

import java.util.List;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.devices.AbstractSmokeDetectorHandler;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.airqualitylevel.AirQualityLevelService;
import org.openhab.binding.boschshc.internal.services.airqualitylevel.dto.AirQualityLevelServiceState;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;

/**
 * The Twinguard smoke detector warns you in case of fire and constantly monitors the air.
 *
 * @author Stefan Kästle - Initial contribution
 * @author Christian Oeing - Use service instead of custom logic
 * @author Christian Oeing - Add smoke detector service
 * @author Gerd Zanker - AbstractSmokeDetectorHandler refactoring for reuse
 */
@NonNullByDefault
public class TwinguardHandler extends AbstractSmokeDetectorHandler {

    public TwinguardHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        this.createService(AirQualityLevelService::new, this::updateChannels,
                List.of(CHANNEL_TEMPERATURE, CHANNEL_TEMPERATURE_RATING, CHANNEL_HUMIDITY, CHANNEL_HUMIDITY_RATING,
                        CHANNEL_PURITY, CHANNEL_PURITY_RATING, CHANNEL_AIR_DESCRIPTION, CHANNEL_COMBINED_RATING));
    }

    private void updateChannels(AirQualityLevelServiceState state) {
        updateState(CHANNEL_TEMPERATURE, new QuantityType<Temperature>(state.temperature, SIUnits.CELSIUS));
        updateState(CHANNEL_TEMPERATURE_RATING, new StringType(state.temperatureRating));
        updateState(CHANNEL_HUMIDITY, new QuantityType<Dimensionless>(state.humidity, Units.PERCENT));
        updateState(CHANNEL_HUMIDITY_RATING, new StringType(state.humidityRating));
        updateState(CHANNEL_PURITY, new QuantityType<Dimensionless>(state.purity, Units.PARTS_PER_MILLION));
        updateState(CHANNEL_PURITY_RATING, new StringType(state.purityRating));
        updateState(CHANNEL_AIR_DESCRIPTION, new StringType(state.description));
        updateState(CHANNEL_COMBINED_RATING, new StringType(state.combinedRating));
    }
}
