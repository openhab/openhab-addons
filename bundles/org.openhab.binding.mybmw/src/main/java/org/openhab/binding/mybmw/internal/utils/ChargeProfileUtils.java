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
package org.openhab.binding.mybmw.internal.utils;

import static org.openhab.binding.mybmw.internal.MyBMWConstants.*;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mybmw.internal.utils.ChargeProfileWrapper.ProfileKey;

/**
 * The {@link ChargeProfileUtils} utility functions for charging profiles
 *
 * @author Norbert Truchsess - initial contribution
 */
@NonNullByDefault
public class ChargeProfileUtils {

    // Charging
    public static class TimedChannel {
        public final String time;
        public final @Nullable String timer;
        public final boolean hasDays;

        TimedChannel(final String time, @Nullable final String timer, final boolean hasDays) {
            this.time = time;
            this.timer = timer;
            this.hasDays = hasDays;
        }
    }

    @SuppressWarnings("serial")
    private static final Map<ProfileKey, TimedChannel> TIMED_CHANNELS = new HashMap<>() {
        {
            put(ProfileKey.WINDOWSTART, new TimedChannel(CHARGE_WINDOW_START, null, false));
            put(ProfileKey.WINDOWEND, new TimedChannel(CHARGE_WINDOW_END, null, false));
            put(ProfileKey.TIMER1, new TimedChannel(CHARGE_TIMER1 + CHARGE_DEPARTURE, CHARGE_TIMER1, true));
            put(ProfileKey.TIMER2, new TimedChannel(CHARGE_TIMER2 + CHARGE_DEPARTURE, CHARGE_TIMER2, true));
            put(ProfileKey.TIMER3, new TimedChannel(CHARGE_TIMER3 + CHARGE_DEPARTURE, CHARGE_TIMER3, true));
            put(ProfileKey.TIMER4, new TimedChannel(CHARGE_TIMER4 + CHARGE_DEPARTURE, CHARGE_TIMER4, true));
        }
    };

    @SuppressWarnings("serial")
    private static final Map<DayOfWeek, String> DAY_CHANNELS = new HashMap<>() {
        {
            put(DayOfWeek.MONDAY, CHARGE_DAY_MON);
            put(DayOfWeek.TUESDAY, CHARGE_DAY_TUE);
            put(DayOfWeek.WEDNESDAY, CHARGE_DAY_WED);
            put(DayOfWeek.THURSDAY, CHARGE_DAY_THU);
            put(DayOfWeek.FRIDAY, CHARGE_DAY_FRI);
            put(DayOfWeek.SATURDAY, CHARGE_DAY_SAT);
            put(DayOfWeek.SUNDAY, CHARGE_DAY_SUN);
        }
    };

    public static class ChargeKeyDay {
        public final ProfileKey key;
        public final DayOfWeek day;

        ChargeKeyDay(final ProfileKey key, final DayOfWeek day) {
            this.key = key;
            this.day = day;
        }
    }

    @SuppressWarnings("serial")
    private static final Map<String, ProfileKey> CHARGE_ENABLED_CHANNEL_KEYS = new HashMap<>() {
        {
            TIMED_CHANNELS.forEach((key, channel) -> {
                put(channel.timer + CHARGE_ENABLED, key);
            });
            put(CHARGE_PROFILE_CLIMATE, ProfileKey.CLIMATE);
        }
    };

    @SuppressWarnings("serial")
    private static final Map<String, ProfileKey> CHARGE_TIME_CHANNEL_KEYS = new HashMap<>() {
        {
            TIMED_CHANNELS.forEach((key, channel) -> {
                put(channel.time, key);
            });
        }
    };

    @SuppressWarnings("serial")
    private static final Map<String, ChargeKeyDay> CHARGE_DAYS_CHANNEL_KEYS = new HashMap<>() {
        {
            DAY_CHANNELS.forEach((dayOfWeek, dayChannel) -> {
                put(CHARGE_TIMER1 + dayChannel, new ChargeKeyDay(ProfileKey.TIMER1, dayOfWeek));
                put(CHARGE_TIMER2 + dayChannel, new ChargeKeyDay(ProfileKey.TIMER2, dayOfWeek));
                put(CHARGE_TIMER3 + dayChannel, new ChargeKeyDay(ProfileKey.TIMER3, dayOfWeek));
                put(CHARGE_TIMER4 + dayChannel, new ChargeKeyDay(ProfileKey.TIMER3, dayOfWeek));
            });
        }
    };

    public static @Nullable TimedChannel getTimedChannel(ProfileKey key) {
        return TIMED_CHANNELS.get(key);
    }

    public static @Nullable String getDaysChannel(DayOfWeek day) {
        return DAY_CHANNELS.get(day);
    }

    public static @Nullable ProfileKey getEnableKey(final String id) {
        return CHARGE_ENABLED_CHANNEL_KEYS.get(id);
    }

    public static @Nullable ChargeKeyDay getKeyDay(final String id) {
        return CHARGE_DAYS_CHANNEL_KEYS.get(id);
    }

    public static @Nullable ProfileKey getTimeKey(final String id) {
        return CHARGE_TIME_CHANNEL_KEYS.get(id);
    }

    public static String formatDays(final Set<DayOfWeek> weekdays) {
        return weekdays.stream().map(day -> Constants.DAYS.get(day)).collect(Collectors.joining(Constants.COMMA));
    }
}
