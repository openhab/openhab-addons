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
package org.openhab.binding.airgradient.internal.handler;

import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.airgradient.internal.model.Measure;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Helper class to reduce code duplication across things.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class MeasureHelper {

    public static Map<String, String> createProperties(Measure measure) {
        Map<String, String> properties = new HashMap<>(4);
        String firmwareVersion = measure.firmwareVersion;
        if (firmwareVersion != null) {
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, firmwareVersion);
        }

        String locationName = measure.locationName;
        if (locationName != null) {
            properties.put(PROPERTY_NAME, locationName);
        }

        String serialNo = measure.serialno;
        if (serialNo != null) {
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, serialNo);
        }

        String model = measure.getModel();
        if (model != null) {
            properties.put(Thing.PROPERTY_MODEL_ID, model);
        }

        return properties;
    }

    public static Map<String, State> createStates(Measure measure) {
        Map<String, State> states = new HashMap<>(11);

        states.put(CHANNEL_ATMP, toQuantityType(measure.getTemperature(), SIUnits.CELSIUS));
        states.put(CHANNEL_ATMP_COMPENSATED, toQuantityType(measure.atmpCompensated, SIUnits.CELSIUS));
        states.put(CHANNEL_PM_003_COUNT, toQuantityType(measure.pm003Count, Units.ONE));
        states.put(CHANNEL_PM_01, toQuantityType(measure.pm01, Units.MICROGRAM_PER_CUBICMETRE));
        states.put(CHANNEL_PM_02, toQuantityType(measure.pm02, Units.MICROGRAM_PER_CUBICMETRE));
        states.put(CHANNEL_PM02_COMPENSATED, toQuantityType(measure.pm02Compensated, Units.MICROGRAM_PER_CUBICMETRE));
        states.put(CHANNEL_PM_10, toQuantityType(measure.pm10, Units.MICROGRAM_PER_CUBICMETRE));
        states.put(CHANNEL_RHUM, toQuantityType(measure.getHumidity(), Units.PERCENT));
        states.put(CHANNEL_RHUM_COMPENSATED, toQuantityType(measure.rhumCompensated, Units.PERCENT));
        states.put(CHANNEL_UPLOADS_SINCE_BOOT, toQuantityType(measure.getBootCount(), Units.ONE));

        Double rco2 = measure.rco2;
        if (rco2 != null) {
            states.put(CHANNEL_RCO2, toQuantityType(rco2.longValue(), Units.PARTS_PER_MILLION));
        }

        Double tvoc = measure.tvoc;
        if (tvoc != null) {
            states.put(CHANNEL_TVOC, toQuantityType(tvoc.longValue(), Units.PARTS_PER_BILLION));
        }

        states.put(CHANNEL_WIFI, toQuantityType(measure.wifi, Units.DECIBEL_MILLIWATTS));
        states.put(CHANNEL_LEDS_MODE, toStringType(measure.ledMode));

        states.put(CHANNEL_PM01_STANDARD, toQuantityType(measure.pm01Standard, Units.MICROGRAM_PER_CUBICMETRE));
        states.put(CHANNEL_PM02_STANDARD, toQuantityType(measure.pm02Standard, Units.MICROGRAM_PER_CUBICMETRE));
        states.put(CHANNEL_PM10_STANDARD, toQuantityType(measure.pm10Standard, Units.MICROGRAM_PER_CUBICMETRE));

        states.put(CHANNEL_PM005_COUNT, toQuantityType(measure.pm005Count, Units.ONE));
        states.put(CHANNEL_PM01_COUNT, toQuantityType(measure.pm01Count, Units.ONE));
        states.put(CHANNEL_PM02_COUNT, toQuantityType(measure.pm02Count, Units.ONE));
        states.put(CHANNEL_PM50_COUNT, toQuantityType(measure.pm50Count, Units.ONE));
        states.put(CHANNEL_PM10_COUNT, toQuantityType(measure.pm10Count, Units.ONE));

        states.put(CHANNEL_TVOC_INDEX, toQuantityType(measure.tvocIndex, Units.ONE));
        states.put(CHANNEL_TVOC_RAW, toQuantityType(measure.tvocRaw, Units.ONE));
        states.put(CHANNEL_NOX_INDEX, toQuantityType(measure.noxIndex, Units.ONE));
        states.put(CHANNEL_NOX_RAW, toQuantityType(measure.noxRaw, Units.ONE));

        return states;
    }

    private static State toQuantityType(@Nullable Number value, Unit<?> unit) {
        return value == null ? UnDefType.NULL : new QuantityType<>(value, unit);
    }

    private static State toStringType(@Nullable String value) {
        return value == null ? UnDefType.NULL : StringType.valueOf(value);
    }
}
