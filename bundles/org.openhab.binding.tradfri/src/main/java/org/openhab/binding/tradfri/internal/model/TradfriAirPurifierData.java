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
package org.openhab.binding.tradfri.internal.model;

import static org.openhab.binding.tradfri.internal.TradfriBindingConstants.*;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * The {@link TradfriAirPurifierData} class is a Java wrapper for the raw JSON data about the air purifier state.
 *
 * @author Vivien Boistuaud - Initial contribution
 */
@NonNullByDefault
public class TradfriAirPurifierData extends TradfriDeviceData {

    private final Logger logger = LoggerFactory.getLogger(TradfriAirPurifierData.class);

    public TradfriAirPurifierData() {
        super(AIR_PURIFIER);
    }

    public TradfriAirPurifierData(JsonElement json) {
        super(AIR_PURIFIER, json);
    }

    public String getJsonString() {
        return root.toString();
    }

    public @Nullable DecimalType getFanMode() {
        JsonElement fanMode = attributes.get(FAN_MODE);
        if (fanMode != null) {
            int modeValue = fanMode.getAsInt();
            if (AIR_PURIFIER_FANMODE.contains(modeValue)) {
                return new DecimalType(modeValue);
            } else {
                logger.debug("Invalid speedMode is '{}': unknown value", modeValue);
                return null;
            }
        } else {
            return null;
        }
    }

    public TradfriAirPurifierData setFanMode(Number speedValue) {
        int speed = speedValue.intValue();
        if (AIR_PURIFIER_FANMODE.contains(speed)) {
            attributes.add(FAN_MODE, new JsonPrimitive(speed));
        } else {
            logger.debug("Could not set fanMode to '{}': unknown value", speed);
        }
        return this;
    }

    public @Nullable DecimalType getFanSpeed() {
        JsonElement fanSpeed = attributes.get(FAN_SPEED);
        if (fanSpeed != null) {
            int speedValue = fanSpeed.getAsInt();
            return new DecimalType(speedValue);
        } else {
            return null;
        }
    }

    public @Nullable OnOffType getDisableLed() {
        JsonElement ledOnOff = attributes.get(LED_DISABLE);
        if (ledOnOff != null) {
            boolean ledStatus = ledOnOff.getAsInt() != 0;
            return OnOffType.from(ledStatus);
        } else {
            return null;
        }
    }

    public TradfriAirPurifierData setDisableLed(OnOffType disableOnOff) {
        attributes.add(LED_DISABLE, new JsonPrimitive(OnOffType.ON.equals(disableOnOff) ? 1 : 0));
        return this;
    }

    public @Nullable OnOffType getLockPhysicalButton() {
        JsonElement lockPhysicalButton = attributes.get(LOCK_PHYSICAL_BUTTON);
        if (lockPhysicalButton != null) {
            boolean isLocked = lockPhysicalButton.getAsInt() != 0;
            return OnOffType.from(isLocked);
        } else {
            return null;
        }
    }

    public TradfriAirPurifierData setLockPhysicalButton(OnOffType lockPhysicalButton) {
        attributes.add(LOCK_PHYSICAL_BUTTON, new JsonPrimitive(OnOffType.ON.equals(lockPhysicalButton) ? 1 : 0));
        return this;
    }

    public @Nullable State getAirQualityPM25() {
        JsonElement airQuality = attributes.get(AIR_QUALITY);
        if (airQuality != null) {
            int pm25InPpm = airQuality.getAsInt();
            if (pm25InPpm != AIR_PURIFIER_AIR_QUALITY_UNDEFINED) {
                return new QuantityType<Dimensionless>(pm25InPpm, Units.PARTS_PER_MILLION);
            } else {
                return UnDefType.UNDEF;
            }
        } else {
            return null;
        }
    }

    public @Nullable State getAirQualityRating() {
        State pm25State = getAirQualityPM25();
        if (pm25State != null) {
            if (pm25State instanceof Number) {
                int pm25Value = ((Number) pm25State).intValue();
                int qualityRating = 1;

                if (pm25Value >= AIR_PURIFIER_AIR_QUALITY_BAD) {
                    qualityRating = 3;
                } else if (pm25Value >= AIR_PURIFIER_AIR_QUALITY_OK) {
                    qualityRating = 2;
                }

                return new DecimalType(qualityRating);
            }
            return UnDefType.UNDEF;
        } else {
            return null;
        }
    }

    public @Nullable QuantityType<Time> getNextFilterCheckTTL() {
        JsonElement nextFilterCheckTTL = attributes.get(NEXT_FILTER_CHECK);
        if (nextFilterCheckTTL != null) {
            int remainingMinutes = nextFilterCheckTTL.getAsInt();
            return new QuantityType<Time>(remainingMinutes, Units.MINUTE);
        } else {
            return null;
        }
    }

    public @Nullable OnOffType getFilterCheckAlarm() {
        QuantityType<Time> ttl = getNextFilterCheckTTL();
        if (ttl != null) {
            int ttlValue = ttl.intValue();
            return ttlValue < 0 ? OnOffType.ON : OnOffType.OFF;
        } else {
            return null;
        }
    }

    public @Nullable QuantityType<Time> getFilterUptime() {
        JsonElement filterUptime = attributes.get(FILTER_UPTIME);
        if (filterUptime != null) {
            int filterUptimeMinutes = filterUptime.getAsInt();
            return new QuantityType<Time>(filterUptimeMinutes, Units.MINUTE);
        } else {
            return null;
        }
    }
}
