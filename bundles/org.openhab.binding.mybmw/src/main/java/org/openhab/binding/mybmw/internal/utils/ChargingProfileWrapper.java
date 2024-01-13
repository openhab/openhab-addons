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
package org.openhab.binding.mybmw.internal.utils;

import static org.openhab.binding.mybmw.internal.utils.ChargingProfileWrapper.ProfileKey.TIMER1;
import static org.openhab.binding.mybmw.internal.utils.ChargingProfileWrapper.ProfileKey.TIMER2;
import static org.openhab.binding.mybmw.internal.utils.ChargingProfileWrapper.ProfileKey.TIMER3;
import static org.openhab.binding.mybmw.internal.utils.ChargingProfileWrapper.ProfileKey.TIMER4;
import static org.openhab.binding.mybmw.internal.utils.ChargingProfileWrapper.ProfileKey.WINDOWEND;
import static org.openhab.binding.mybmw.internal.utils.ChargingProfileWrapper.ProfileKey.WINDOWSTART;
import static org.openhab.binding.mybmw.internal.utils.Constants.NULL_LOCAL_TIME;
import static org.openhab.binding.mybmw.internal.utils.Constants.TIME_FORMATER;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mybmw.internal.MyBMWConstants.ChargingMode;
import org.openhab.binding.mybmw.internal.MyBMWConstants.ChargingPreference;
import org.openhab.binding.mybmw.internal.dto.charge.ChargingProfile;
import org.openhab.binding.mybmw.internal.dto.charge.ChargingSettings;
import org.openhab.binding.mybmw.internal.dto.charge.Time;
import org.openhab.binding.mybmw.internal.dto.charge.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ChargingProfileWrapper} Wrapper for ChargingProfiles
 *
 * @author Bernd Weymann - Initial contribution
 * @author Norbert Truchsess - add ChargeProfileActions
 * @author Martin Grassl - refactoring
 */
@NonNullByDefault
public class ChargingProfileWrapper {
    private final Logger logger = LoggerFactory.getLogger(ChargingProfileWrapper.class);

    private static final String CHARGING_WINDOW = "CHARGING_WINDOW";
    private static final String WEEKLY_PLANNER = "WEEKLY_PLANNER";
    private static final String ACTIVATE = "ACTIVATE";
    // not used private static final String DEACTIVATE = "DEACTIVATE";

    public enum ProfileKey {
        CLIMATE,
        TIMER1,
        TIMER2,
        TIMER3,
        TIMER4,
        WINDOWSTART,
        WINDOWEND
    }

    private Optional<ChargingMode> mode = Optional.empty();
    private Optional<ChargingPreference> preference = Optional.empty();
    private Optional<String> controlType = Optional.empty();
    private Optional<ChargingSettings> chargeSettings = Optional.empty();

    private final Map<ProfileKey, Boolean> enabled = new HashMap<>();
    private final Map<ProfileKey, LocalTime> times = new HashMap<>();
    private final Map<ProfileKey, Set<DayOfWeek>> daysOfWeek = new HashMap<>();

    public ChargingProfileWrapper(final ChargingProfile profile) {
        setPreference(profile.getChargingPreference());
        setMode(profile.getChargingMode());
        controlType = Optional.of(profile.getChargingControlType());
        chargeSettings = Optional.of(profile.getChargingSettings());
        setEnabled(ProfileKey.CLIMATE, profile.isClimatisationOn());

        addTimer(TIMER1, profile.getTimerId(1));
        addTimer(TIMER2, profile.getTimerId(2));
        if (profile.getChargingControlType().equals(WEEKLY_PLANNER)) {
            addTimer(TIMER3, profile.getTimerId(3));
            addTimer(TIMER4, profile.getTimerId(4));
        }

        if (CHARGING_WINDOW.equals(profile.getChargingPreference())) {
            addTime(WINDOWSTART, profile.getReductionOfChargeCurrent().getStart());
            addTime(WINDOWEND, profile.getReductionOfChargeCurrent().getEnd());
        } else {
            preference.ifPresent(pref -> {
                if (ChargingPreference.CHARGING_WINDOW.equals(pref)) {
                    addTime(WINDOWSTART, null);
                    addTime(WINDOWEND, null);
                }
            });
        }
    }

    public @Nullable Boolean isEnabled(final ProfileKey key) {
        return enabled.get(key);
    }

    public void setEnabled(final ProfileKey key, @Nullable final Boolean enabled) {
        if (enabled == null) {
            this.enabled.remove(key);
        } else {
            this.enabled.put(key, enabled);
        }
    }

    public @Nullable String getMode() {
        return mode.map(m -> m.name()).orElse(null);
    }

    public @Nullable String getControlType() {
        return controlType.get();
    }

    public @Nullable ChargingSettings getChargingSettings() {
        return chargeSettings.get();
    }

    public void setMode(final @Nullable String mode) {
        if (mode != null) {
            try {
                this.mode = Optional.of(ChargingMode.valueOf(mode));
                return;
            } catch (IllegalArgumentException iae) {
                logger.warn("unexpected value for chargingMode: {}", mode);
            }
        }
        this.mode = Optional.empty();
    }

    public @Nullable String getPreference() {
        return preference.map(pref -> pref.name()).orElse(null);
    }

    public void setPreference(final @Nullable String preference) {
        if (preference != null) {
            try {
                this.preference = Optional.of(ChargingPreference.valueOf(preference));
                return;
            } catch (IllegalArgumentException iae) {
                logger.warn("unexpected value for chargingPreference: {}", preference);
            }
        }
        this.preference = Optional.empty();
    }

    public @Nullable Set<DayOfWeek> getDays(final ProfileKey key) {
        return daysOfWeek.get(key);
    }

    public LocalTime getTime(final ProfileKey key) {
        LocalTime t = times.get(key);
        if (t != null) {
            return t;
        } else {
            logger.debug("Profile not valid - Key {} doesn't contain boolean value", key);
            return Constants.NULL_LOCAL_TIME;
        }
    }

    private void addTime(final ProfileKey key, @Nullable final Time time) {
        try {
            times.put(key, time == null ? NULL_LOCAL_TIME : LocalTime.parse(Converter.getTime(time), TIME_FORMATER));
        } catch (DateTimeParseException dtpe) {
            logger.warn("unexpected value for {} time: {}", key.name(), time);
        }
    }

    private void addTimer(final ProfileKey key, @Nullable final Timer timer) {
        if (timer == null) {
            enabled.put(key, false);
            addTime(key, null);
            daysOfWeek.put(key, EnumSet.noneOf(DayOfWeek.class));
        } else {
            enabled.put(key, ACTIVATE.equals(timer.action));
            addTime(key, timer.timeStamp);
            final EnumSet<DayOfWeek> daySet = EnumSet.noneOf(DayOfWeek.class);
            if (timer.timerWeekDays != null) {
                daysOfWeek.put(key, EnumSet.noneOf(DayOfWeek.class));
                for (String day : timer.timerWeekDays) {
                    try {
                        daySet.add(DayOfWeek.valueOf(day.toUpperCase()));
                    } catch (IllegalArgumentException iae) {
                        logger.warn("unexpected value for {} day: {}", key.name(), day);
                    }
                    daysOfWeek.put(key, daySet);
                }
            }
        }
    }
}
