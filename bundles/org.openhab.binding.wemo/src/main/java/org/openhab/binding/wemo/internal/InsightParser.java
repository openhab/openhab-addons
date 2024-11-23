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
package org.openhab.binding.wemo.internal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Parser for WeMo Insight Switch values.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class InsightParser {

    private static final int INSIGHT_POSITION_STATE = 0;
    private static final int INSIGHT_POSITION_LASTCHANGEDAT = 1;
    private static final int INSIGHT_POSITION_LASTONFOR = 2;
    private static final int INSIGHT_POSITION_ONTODAY = 3;
    private static final int INSIGHT_POSITION_ONTOTAL = 4;
    private static final int INSIGHT_POSITION_TIMESPAN = 5;
    private static final int INSIGHT_POSITION_AVERAGEPOWER = 6;
    private static final int INSIGHT_POSITION_CURRENTPOWER = 7;
    private static final int INSIGHT_POSITION_ENERGYTODAY = 8;
    private static final int INSIGHT_POSITION_ENERGYTOTAL = 9;
    private static final int INSIGHT_POSITION_STANDBYLIMIT = 10;

    private final String value;

    public InsightParser(String value) {
        this.value = value;
    }

    /**
     * Parse provided string of values.
     *
     * @return Map of channel id's with states
     */
    public Map<String, State> parse() {
        HashMap<String, State> result = new HashMap<>();
        String[] params = value.split("\\|");
        for (int i = 0; i < params.length; i++) {
            String value = params[i];
            switch (i) {
                case INSIGHT_POSITION_STATE:
                    result.put(WemoBindingConstants.CHANNEL_STATE, getOnOff(value));
                    break;
                case INSIGHT_POSITION_LASTCHANGEDAT:
                    result.put(WemoBindingConstants.CHANNEL_LAST_CHANGED_AT, getDateTime(value));
                    break;
                case INSIGHT_POSITION_LASTONFOR:
                    result.put(WemoBindingConstants.CHANNEL_LAST_ON_FOR, getNumber(value));
                    break;
                case INSIGHT_POSITION_ONTODAY:
                    result.put(WemoBindingConstants.CHANNEL_ON_TODAY, getNumber(value));
                    break;
                case INSIGHT_POSITION_ONTOTAL:
                    result.put(WemoBindingConstants.CHANNEL_ON_TOTAL, getNumber(value));
                    break;
                case INSIGHT_POSITION_TIMESPAN:
                    result.put(WemoBindingConstants.CHANNEL_TIMESPAN, getNumber(value));
                    break;
                case INSIGHT_POSITION_AVERAGEPOWER:
                    result.put(WemoBindingConstants.CHANNEL_AVERAGE_POWER, getPowerFromWatt(value));
                    break;
                case INSIGHT_POSITION_CURRENTPOWER:
                    result.put(WemoBindingConstants.CHANNEL_CURRENT_POWER_RAW, getPowerFromMilliWatt(value));
                    break;
                case INSIGHT_POSITION_ENERGYTODAY:
                    result.put(WemoBindingConstants.CHANNEL_ENERGY_TODAY, getEnergy(value));
                    break;
                case INSIGHT_POSITION_ENERGYTOTAL:
                    result.put(WemoBindingConstants.CHANNEL_ENERGY_TOTAL, getEnergy(value));
                    break;
                case INSIGHT_POSITION_STANDBYLIMIT:
                    result.put(WemoBindingConstants.CHANNEL_STAND_BY_LIMIT, getPowerFromMilliWatt(value));
                    break;
            }
        }
        return result;
    }

    private State getOnOff(String value) {
        return OnOffType.from(!"0".equals(value));
    }

    private State getDateTime(String value) {
        long lastChangedAt = 0;
        try {
            lastChangedAt = Long.parseLong(value);
        } catch (NumberFormatException e) {
            return UnDefType.UNDEF;
        }

        State lastChangedAtState = new DateTimeType(
                ZonedDateTime.ofInstant(Instant.ofEpochSecond(lastChangedAt), ZoneId.systemDefault()));
        if (lastChangedAt == 0) {
            return UnDefType.UNDEF;
        }
        return lastChangedAtState;
    }

    private State getNumber(String value) {
        try {
            return DecimalType.valueOf(value);
        } catch (NumberFormatException e) {
            return UnDefType.UNDEF;
        }
    }

    private State getPowerFromWatt(String value) {
        return new QuantityType<>(DecimalType.valueOf(value), Units.WATT);
    }

    private State getPowerFromMilliWatt(String value) {
        return new QuantityType<>(new BigDecimal(value).divide(new BigDecimal(1000), 3, RoundingMode.HALF_UP),
                Units.WATT);
    }

    private State getEnergy(String value) {
        // recalculate mW-mins to Wh
        return new QuantityType<>(new BigDecimal(value).divide(new BigDecimal(60_000), 0, RoundingMode.HALF_UP),
                Units.WATT_HOUR);
    }
}
