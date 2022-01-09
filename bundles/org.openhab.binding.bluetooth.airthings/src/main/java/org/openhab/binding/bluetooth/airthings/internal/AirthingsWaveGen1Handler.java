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

import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AirthingsWaveGen1Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Davy Wong - Added Airthings Wave Gen 1 support
 */
@NonNullByDefault
public class AirthingsWaveGen1Handler extends AbstractAirthingsHandler {

    private static final String HUMIDITY_UUID = "00002a6f-0000-1000-8000-00805f9b34fb"; // 0x2A6F
    private static final String TEMPERATURE_UUID = "00002a6e-0000-1000-8000-00805f9b34fb"; // 0x2A6E
    private static final String RADON_STA_UUID = "b42e01aa-ade7-11e4-89d3-123b93f75cba";
    private static final String RADON_LTA_UUID = "b42e0a4c-ade7-11e4-89d3-123b93f75cba";

    private int intResult;
    private double dblResult;
    private volatile ReadSensor readSensor = ReadSensor.RADON_STA;

    private enum ReadSensor {
        TEMPERATURE,
        HUMIDITY,
        RADON_STA,
        RADON_LTA,
    }

    public AirthingsWaveGen1Handler(Thing thing) {
        super(thing);
    }

    private final Logger logger = LoggerFactory.getLogger(AirthingsWaveGen1Handler.class);

    @Override
    protected void updateChannels(int[] is) {
        int[] rawdata;
        rawdata = is;
        if (rawdata.length == 2) {
            switch (readSensor) {
                case TEMPERATURE:
                    dblResult = intFromBytes(rawdata[0], rawdata[1]) / 100D;
                    logger.debug("Parsed data 1: {}", String.format("[temperature=%.1f °C]", dblResult));
                    readSensor = ReadSensor.HUMIDITY;
                    logger.debug("Change next readSensor to: {}", readSensor);
                    logger.debug("Update channel 1");
                    updateState(CHANNEL_ID_TEMPERATURE,
                            QuantityType.valueOf(Double.valueOf(dblResult), SIUnits.CELSIUS));
                    logger.debug("Update channel 1 done");
                    break;
                case HUMIDITY:
                    dblResult = intFromBytes(rawdata[0], rawdata[1]) / 100D;
                    logger.debug("Parsed data 2: {}", String.format("[humidity=%.1f %%rH]", dblResult));
                    readSensor = ReadSensor.RADON_STA;
                    logger.debug("Change next readSensor to: {}", readSensor);
                    logger.debug("Update channel 2");
                    updateState(CHANNEL_ID_HUMIDITY, QuantityType.valueOf(Double.valueOf(dblResult), Units.PERCENT));
                    logger.debug("Update channel 2 done");
                    break;
                case RADON_STA:
                    intResult = intFromBytes(rawdata[0], rawdata[1]);
                    logger.debug("Parsed data 3: {}", String.format("[radonShortTermAvg=%d Bq/m3]", intResult));
                    readSensor = ReadSensor.RADON_LTA;
                    logger.debug("Change next readSensor to: {}", readSensor);
                    logger.debug("Update channel 3");
                    updateState(CHANNEL_ID_RADON_ST_AVG,
                            QuantityType.valueOf(Double.valueOf(intResult), BECQUEREL_PER_CUBIC_METRE));
                    logger.debug("Update channel 3 done");
                    break;
                case RADON_LTA:
                    intResult = intFromBytes(rawdata[0], rawdata[1]);
                    logger.debug("Parsed data 4: {}", String.format("[radonLongTermAvg=%d Bq/m3]", intResult));
                    readSensor = ReadSensor.TEMPERATURE;
                    logger.debug("Change next readSensor to: {}", readSensor);
                    logger.debug("Update channel 4");
                    updateState(CHANNEL_ID_RADON_LT_AVG,
                            QuantityType.valueOf(Double.valueOf(intResult), BECQUEREL_PER_CUBIC_METRE));
                    logger.debug("Update channel 4 done");
                    break;
            }
        } else {
            logger.debug("Illegal data structure length '{}'", String.valueOf(rawdata).length());
        }
    }

    @Override
    protected UUID getDataUUID() {
        switch (readSensor) {
            case TEMPERATURE:
                logger.debug("Return UUID Temperature");
                return UUID.fromString(TEMPERATURE_UUID);
            case HUMIDITY:
                logger.debug("Return UUID Humidity");
                return UUID.fromString(HUMIDITY_UUID);
            case RADON_STA:
                logger.debug("Return UUID Radon STA");
                return UUID.fromString(RADON_STA_UUID);
            case RADON_LTA:
                logger.debug("Return UUID Radon LTA");
                return UUID.fromString(RADON_LTA_UUID);
            default:
                logger.debug("Return UUID Default");
                return UUID.fromString(RADON_STA_UUID);
        }
    }

    private int intFromBytes(int lowByte, int highByte) {
        return (highByte & 0xFF) << 8 | (lowByte & 0xFF);
    }
}
