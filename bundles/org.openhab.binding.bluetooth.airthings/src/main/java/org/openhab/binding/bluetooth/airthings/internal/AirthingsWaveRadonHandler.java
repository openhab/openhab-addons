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
package org.openhab.binding.bluetooth.airthings.internal;

import static org.openhab.binding.bluetooth.airthings.internal.AirthingsBindingConstants.*;

import java.util.Map;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AirthingsWaveRadonHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class AirthingsWaveRadonHandler extends AbstractAirthingsHandler {

    private static final String DATA_UUID = "b42e4dcc-ade7-11e4-89d3-123b93f75cba";

    public AirthingsWaveRadonHandler(Thing thing) {
        super(thing);
    }

    private final Logger logger = LoggerFactory.getLogger(AirthingsWaveRadonHandler.class);
    private final UUID uuid = UUID.fromString(DATA_UUID);

    @Override
    protected void updateChannels(int[] is) {
        Map<String, Number> data;
        try {
            data = AirthingsDataParser.parseWaveRadonData(is);
            logger.debug("Parsed data: {}", data);
            Number humidity = data.get(AirthingsDataParser.HUMIDITY);
            if (humidity != null) {
                updateState(CHANNEL_ID_HUMIDITY, new QuantityType<>(humidity, Units.PERCENT));
            }
            Number temperature = data.get(AirthingsDataParser.TEMPERATURE);
            if (temperature != null) {
                updateState(CHANNEL_ID_TEMPERATURE, new QuantityType<>(temperature, SIUnits.CELSIUS));
            }
            Number radonShortTermAvg = data.get(AirthingsDataParser.RADON_SHORT_TERM_AVG);
            if (radonShortTermAvg != null) {
                updateState(CHANNEL_ID_RADON_ST_AVG,
                        new QuantityType<>(radonShortTermAvg, Units.BECQUEREL_PER_CUBIC_METRE));
            }
            Number radonLongTermAvg = data.get(AirthingsDataParser.RADON_LONG_TERM_AVG);
            if (radonLongTermAvg != null) {
                updateState(CHANNEL_ID_RADON_LT_AVG,
                        new QuantityType<>(radonLongTermAvg, Units.BECQUEREL_PER_CUBIC_METRE));
            }
        } catch (AirthingsParserException e) {
            logger.warn("Failed to parse data received from Airthings sensor: {}", e.getMessage());
        }
    }

    @Override
    protected UUID getDataUUID() {
        return uuid;
    }
}
