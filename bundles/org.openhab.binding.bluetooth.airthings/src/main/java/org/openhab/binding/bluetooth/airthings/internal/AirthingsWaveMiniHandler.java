/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AirthingsWaveMiniHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class AirthingsWaveMiniHandler extends AbstractAirthingsHandler {

    private static final String DATA_UUID = "b42e3b98-ade7-11e4-89d3-123b93f75cba";

    public AirthingsWaveMiniHandler(Thing thing) {
        super(thing);
    }

    private final Logger logger = LoggerFactory.getLogger(AirthingsWaveMiniHandler.class);

    private final UUID uuid = UUID.fromString(DATA_UUID);

    @Override
    protected void updateChannels(int[] is) {
        Map<String, Number> data;
        try {
            data = AirthingsDataParser.parseWaveMiniData(is);
            logger.debug("Parsed data: {}", data);
            Number humidity = data.get(AirthingsDataParser.HUMIDITY);
            if (humidity != null) {
                updateState(CHANNEL_ID_HUMIDITY, new QuantityType<Dimensionless>(humidity, Units.PERCENT));
            }
            Number temperature = data.get(AirthingsDataParser.TEMPERATURE);
            if (temperature != null) {
                updateState(CHANNEL_ID_TEMPERATURE, new QuantityType<Temperature>(temperature, SIUnits.CELSIUS));
            }
            Number tvoc = data.get(AirthingsDataParser.TVOC);
            if (tvoc != null) {
                updateState(CHANNEL_ID_TVOC, new QuantityType<Dimensionless>(tvoc, Units.PARTS_PER_BILLION));
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
