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
package org.openhab.binding.somneo.internal.model;

import java.time.LocalTime;
import java.time.ZonedDateTime;

import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the audio state from the API.
 *
 * @author Michael Myrcik - Initial contribution
 */
@NonNullByDefault
public class AlarmSettingsData {

    private static final int POWER_WAKE_ENABLED = 255;
    private static final int POWER_WAKE_DISABLED = 0;

    @SerializedName("prfnr")
    private @Nullable Integer position;

    @SerializedName("prfvs")
    private @Nullable Boolean configured;

    @SerializedName("prfen")
    private @Nullable Boolean enabled;

    /**
     * None = 0,
     * Monday = 2,
     * Tuesday = 4,
     * Wednesday = 8,
     * Thursday = 16,
     * Friday = 32,
     * Saturday = 64,
     * Sunday = 128
     */
    @SerializedName("daynm")
    private @Nullable Integer repeatDay;

    @SerializedName("almhr")
    private @Nullable Integer hour;

    @SerializedName("almmn")
    private @Nullable Integer minute;

    /**
     * Brightness range from 0 to 25.
     */
    @SerializedName("curve")
    private @Nullable Integer sunriseBrightness;

    @SerializedName("durat")
    private @Nullable Integer sunriseDurationInMin;

    @SerializedName("ctype")
    private @Nullable Integer sunriseSchema;

    @SerializedName("snddv")
    private @Nullable String soundSource;

    @SerializedName("sndch")
    private @Nullable String soundChannel;

    @SerializedName("sndlv")
    private @Nullable Integer soundVolume;

    @SerializedName("pwrsz")
    private @Nullable Integer powerWake;

    @SerializedName("pszhr")
    private @Nullable Integer powerWakeHour;

    @SerializedName("pszmn")
    private @Nullable Integer powerWakeMinute;

    public void setPosition(Integer position) {
        this.position = position;
    }

    public State getConfiguredState() {
        final Boolean configured = this.configured;
        if (configured == null) {
            return UnDefType.NULL;
        }
        return OnOffType.from(configured);
    }

    public void setConfigured(Boolean configured) {
        this.configured = configured;
    }

    public State getEnabledState() {
        final Boolean enabled = this.enabled;
        if (enabled == null) {
            return UnDefType.NULL;
        }
        return OnOffType.from(enabled);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setEnabledState(OnOffType enabled) {
        setEnabled(OnOffType.ON.equals(enabled));
    }

    public void setAlarmTime(LocalTime time) {
        Integer powerWakeDelay = getPowerWakeDelay();
        if (powerWakeDelay == null) {
            powerWakeDelay = 0;
        }
        this.hour = time.getHour();
        this.minute = time.getMinute();

        final Integer powerWake = this.powerWake;
        if (powerWake != null && powerWake.intValue() == POWER_WAKE_ENABLED) {
            LocalTime powerWakeTime = time.plusMinutes(powerWakeDelay);
            this.powerWakeHour = powerWakeTime.getHour();
            this.powerWakeMinute = powerWakeTime.getMinute();
        }
    }

    public void setAlarmTime(DateTimeType timeState) {
        final ZonedDateTime zonedTime = timeState.getZonedDateTime();
        final LocalTime time = LocalTime.of(zonedTime.getHour(), zonedTime.getMinute());
        if (time == null) {
            return;
        }
        setAlarmTime(time);
    }

    public State getRepeatDayState() {
        final Integer repeatDay = this.repeatDay;
        if (repeatDay == null) {
            return UnDefType.NULL;
        }
        return new DecimalType(repeatDay);
    }

    public void setRepeatDay(int days) {
        this.repeatDay = days;
    }

    public void setRepeatDayState(DecimalType days) {
        setRepeatDay(days.intValue());
    }

    public State getAlarmTimeState() {
        final Integer hour = this.hour;
        if (hour == null) {
            return UnDefType.NULL;
        }
        final Integer minute = this.minute;
        if (minute == null) {
            return UnDefType.NULL;
        }
        final String alarmTimeString = String.format("%02d:%02d:00", hour, minute);
        if (alarmTimeString == null) {
            return UnDefType.NULL;
        }
        return DateTimeType.valueOf(alarmTimeString);
    }

    public State getPowerWakeState() {
        final Integer powerWake = this.powerWake;
        if (powerWake == null) {
            return UnDefType.NULL;
        }
        return OnOffType.from((int) powerWake == POWER_WAKE_ENABLED);
    }

    public void setPowerWakeState(OnOffType state) {
        if (OnOffType.ON.equals(state)) {
            powerWake = POWER_WAKE_ENABLED;
            powerWakeHour = hour;
            powerWakeMinute = minute;
        } else {
            powerWake = POWER_WAKE_DISABLED;
            powerWakeHour = 0;
            powerWakeMinute = 0;
        }
    }

    public @Nullable Integer getPowerWakeDelay() {
        if (OnOffType.OFF.equals(getPowerWakeState())) {
            return Integer.valueOf(0);
        }
        final Integer hour = this.hour;
        if (hour == null) {
            return null;
        }
        final Integer minute = this.minute;
        if (minute == null) {
            return null;
        }
        final Integer powerWakeHour = this.powerWakeHour;
        if (powerWakeHour == null) {
            return null;
        }
        final Integer powerWakeMinute = this.powerWakeMinute;
        if (powerWakeMinute == null) {
            return null;
        }
        int delay = powerWakeMinute - minute;
        if (!powerWakeHour.equals(hour)) {
            // Simplify the algorithm, as the delta cannot be greater than 59 minutes.
            delay += 60;
        }
        return delay;
    }

    public void setPowerWakeDelay(int minutes) {
        final Integer hour = this.hour;
        if (hour == null) {
            return;
        }
        final Integer minute = this.minute;
        if (minute == null) {
            return;
        }

        LocalTime localTime = LocalTime.of(hour, minute);
        localTime = localTime.plusMinutes(minutes);

        this.powerWake = POWER_WAKE_ENABLED;
        this.powerWakeHour = localTime.getHour();
        this.powerWakeMinute = localTime.getMinute();
    }

    public void setPowerWakeDelayState(QuantityType<Time> time) {
        setPowerWakeDelay(time.intValue());
    }

    public State getPowerWakeDelayState() {
        final Integer delay = getPowerWakeDelay();
        if (delay == null) {
            return UnDefType.NULL;
        }
        return new QuantityType<>(delay, Units.MINUTE);
    }

    public State getSunriseBrightness() {
        final Integer sunriseBrightness = this.sunriseBrightness;
        if (sunriseBrightness == null) {
            return UnDefType.NULL;
        }
        return new PercentType(sunriseBrightness * 4);
    }

    public void setSunriseBrightnessState(PercentType percent) {
        sunriseBrightness = percent.intValue() / 4;
    }

    public State getSunriseDurationInMin() {
        final Integer sunriseDurationInMin = this.sunriseDurationInMin;
        if (sunriseDurationInMin == null) {
            return UnDefType.NULL;
        }
        return new QuantityType<>(sunriseDurationInMin, Units.MINUTE);
    }

    public void setSunriseDurationState(QuantityType<Time> duration) {
        sunriseDurationInMin = duration.intValue();
    }

    public State getSunriseSchema() {
        final Integer sunriseSchema = this.sunriseSchema;
        if (sunriseSchema == null) {
            return UnDefType.NULL;
        }
        return new DecimalType(sunriseSchema);
    }

    public void setSunriseSchemaState(DecimalType schema) {
        this.sunriseSchema = schema.intValue();
    }

    public State getSound() {
        final String soundSource = this.soundSource;
        if (soundSource == null) {
            return UnDefType.NULL;
        }
        final String suffix = "off".equals(soundSource) ? "" : "-" + soundChannel;
        return new StringType(soundSource + suffix);
    }

    public void setAlarmSoundState(StringType sound) {
        final String[] values = sound.toFullString().split("-");
        soundSource = values[0];
        soundChannel = values.length == 1 ? "" : values[1];
    }

    public State getSoundVolume() {
        final Integer soundVolume = this.soundVolume;
        if (soundVolume == null) {
            return UnDefType.NULL;
        }
        return new PercentType(soundVolume * 4);
    }

    public void setAlarmVolumeState(PercentType volume) {
        soundVolume = volume.intValue() / 4;
    }

    public static AlarmSettingsData withDefaultValues(int position) {
        final AlarmSettingsData data = new AlarmSettingsData();
        data.position = position;
        data.configured = false;
        data.enabled = false;
        data.hour = 7;
        data.minute = 30;
        data.powerWake = POWER_WAKE_DISABLED;
        data.powerWakeHour = 0;
        data.powerWakeMinute = 0;
        data.sunriseSchema = 0;
        data.sunriseBrightness = 20;
        data.sunriseDurationInMin = 30;
        data.repeatDay = 254;
        data.soundSource = "wus";
        data.soundChannel = "1";
        data.soundVolume = 12;
        return data;
    }
}
