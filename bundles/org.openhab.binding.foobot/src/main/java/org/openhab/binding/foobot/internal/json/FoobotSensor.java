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
package org.openhab.binding.foobot.internal.json;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

/**
 * Enum for all specific sensor data returned by the Foobot device.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public enum FoobotSensor {
    TIME("time", "time", null),
    PM("pm", "pm", Units.MICROGRAM_PER_CUBICMETRE),
    TEMPERATURE("temperature", "tmp", "C", SIUnits.CELSIUS, ImperialUnits.FAHRENHEIT),
    HUMIDITY("humidity", "hum", null),
    CO2("co2", "co2", Units.PARTS_PER_MILLION),
    VOC("voc", "voc", null),
    GPI("gpi", "allpollu", null);

    private final String channelId;
    private final String dataKey;
    private final @Nullable String matchUnit;
    private final @Nullable Unit<?> unit;
    private final @Nullable Unit<?> alternativeUnit;

    private static final Map<String, FoobotSensor> CHANNEL_ID_MAP = Stream.of(values())
            .collect(Collectors.toMap(FoobotSensor::getChannelId, Function.identity()));

    /**
     * Constructor.
     *
     * @param channelId Id of the thing channel
     * @param dataKey key of the sensor data in the foobot sensor json data
     * @param unit Unit of the sensor data or null if no unit specified
     */
    private FoobotSensor(String channelId, String dataKey, @Nullable Unit<?> unit) {
        this(channelId, dataKey, null, unit, null);
    }

    /**
     * Constructor.
     *
     * @param channelId Id of the thing channel
     * @param dataKey key of the sensor data in the foobot sensor json data
     * @param matchUnit unit string to be matched with the foobot returned unit
     * @param unit Unit of the sensor data or null if no unit specified
     * @param alternativeUnit if foobot api unit doesn't match this unit is returned
     */
    private FoobotSensor(String channelId, String dataKey, @Nullable String matchUnit, @Nullable Unit<?> unit,
            @Nullable Unit<?> alternativeUnit) {
        this.channelId = channelId;
        this.dataKey = dataKey;
        this.matchUnit = matchUnit;
        this.unit = unit;
        this.alternativeUnit = alternativeUnit;
    }

    public static @Nullable FoobotSensor findSensorByChannelId(String channelId) {
        return CHANNEL_ID_MAP.get(channelId);
    }

    public String getChannelId() {
        return channelId;
    }

    /**
     * @return Returns the key of the sensor type as returned by the foobot api
     */
    public String getDataKey() {
        return dataKey;
    }

    /**
     * Returns the Unit of this sensor data type or null if no unit specified.
     *
     * @param unitToMath match the returned unit by the foobot api with the Unit to be returned
     * @return Unit or null if no unit available for the sensor
     */
    public @Nullable Unit<?> getUnit(String unitToMath) {
        return matchUnit == null ? unit : (matchUnit.equals(unitToMath) ? unit : alternativeUnit);
    }
}
