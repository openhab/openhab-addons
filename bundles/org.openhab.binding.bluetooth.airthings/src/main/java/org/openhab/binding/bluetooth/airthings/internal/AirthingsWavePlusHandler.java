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
package org.openhab.binding.bluetooth.airthings.internal;

import static org.openhab.binding.bluetooth.airthings.internal.AirthingsBindingConstants.*;

import java.util.Map;
import java.util.UUID;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.dimension.Density;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AirthingsWavePlusHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Kai Kreuzer - Added Airthings Wave Mini support
 */
@NonNullByDefault
public class AirthingsWavePlusHandler extends AbstractAirthingsHandler {

    private static final String DATA_UUID = "b42e2a68-ade7-11e4-89d3-123b93f75cba";

    public AirthingsWavePlusHandler(Thing thing) {
        super(thing);
    }

    private final Logger logger = LoggerFactory.getLogger(AirthingsWavePlusHandler.class);
    private final UUID uuid = UUID.fromString(DATA_UUID);

    @Override
    protected void updateChannels(int[] is) {
        Map<String, Number> data;
        try {
            data = AirthingsDataParser.parseWavePlusData(is);
            logger.debug("Parsed data: {}", data);
            Number humidity = data.get(AirthingsDataParser.HUMIDITY);
            if (humidity != null) {
                updateState(CHANNEL_ID_HUMIDITY, new QuantityType<Dimensionless>(humidity, Units.PERCENT));
            }
            Number temperature = data.get(AirthingsDataParser.TEMPERATURE);
            if (temperature != null) {
                updateState(CHANNEL_ID_TEMPERATURE, new QuantityType<Temperature>(temperature, SIUnits.CELSIUS));
            }
            Number pressure = data.get(AirthingsDataParser.PRESSURE);
            if (pressure != null) {
                updateState(CHANNEL_ID_PRESSURE, new QuantityType<Pressure>(pressure, Units.MILLIBAR));
            }
            Number co2 = data.get(AirthingsDataParser.CO2);
            if (co2 != null) {
                updateState(CHANNEL_ID_CO2, new QuantityType<Dimensionless>(co2, Units.PARTS_PER_MILLION));
            }
            Number tvoc = data.get(AirthingsDataParser.TVOC);
            if (tvoc != null) {
                updateState(CHANNEL_ID_TVOC, new QuantityType<Dimensionless>(tvoc, PARTS_PER_BILLION));
            }
            Number radonShortTermAvg = data.get(AirthingsDataParser.RADON_SHORT_TERM_AVG);
            if (radonShortTermAvg != null) {
                updateState(CHANNEL_ID_RADON_ST_AVG,
                        new QuantityType<Density>(radonShortTermAvg, BECQUEREL_PER_CUBIC_METRE));
            }
            Number radonLongTermAvg = data.get(AirthingsDataParser.RADON_LONG_TERM_AVG);
            if (radonLongTermAvg != null) {
                updateState(CHANNEL_ID_RADON_LT_AVG,
                        new QuantityType<Density>(radonLongTermAvg, BECQUEREL_PER_CUBIC_METRE));
            }
        } catch (AirthingsParserException e) {
            logger.error("Failed to parse data received from Airthings sensor: {}", e.getMessage());
        }
    }

    @Override
    protected UUID getDataUUID() {
        return uuid;
    }
}
