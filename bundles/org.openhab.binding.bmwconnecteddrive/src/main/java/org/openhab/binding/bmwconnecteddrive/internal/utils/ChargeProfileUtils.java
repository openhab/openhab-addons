/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.utils;

import static org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.*;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bmwconnecteddrive.internal.utils.ChargeProfileWrapper.ProfileKey;

/**
 * The {@link ChargeProfileUtils} utility functions for charging profiles
 *
 * @author Norbert Truchsess - initial contribution
 */
@NonNullByDefault
public class ChargeProfileUtils {

    // Charging
    public static class TimedChannel {
        TimedChannel(final String time, @Nullable final String timer, final boolean hasDays) {
            this.time = time;
            this.timer = timer;
            this.hasDays = hasDays;
        }

        public final String time;
        public final @Nullable String timer;
        public final boolean hasDays;
    }

    @SuppressWarnings("serial")
    private static final Map<ProfileKey, TimedChannel> timedChannels = new HashMap<>() {
        {
            put(ProfileKey.WINDOWSTART, new TimedChannel(CHARGE_WINDOW_START, null, false));
            put(ProfileKey.WINDOWEND, new TimedChannel(CHARGE_WINDOW_END, null, false));
            put(ProfileKey.TIMER1, new TimedChannel(CHARGE_TIMER1 + CHARGE_DEPARTURE, CHARGE_TIMER1, true));
            put(ProfileKey.TIMER2, new TimedChannel(CHARGE_TIMER2 + CHARGE_DEPARTURE, CHARGE_TIMER2, true));
            put(ProfileKey.TIMER3, new TimedChannel(CHARGE_TIMER3 + CHARGE_DEPARTURE, CHARGE_TIMER3, true));
            put(ProfileKey.OVERRIDE, new TimedChannel(CHARGE_OVERRIDE + CHARGE_DEPARTURE, CHARGE_OVERRIDE, false));
        }
    };

    @SuppressWarnings("serial")
    private static final Map<DayOfWeek, String> dayChannels = new HashMap<>() {
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

    public static class ChargeKeyHour {
        ChargeKeyHour(final ProfileKey key, final boolean isHour) {
            this.key = key;
            this.isHour = isHour;
        }

        public final ProfileKey key;
        public final boolean isHour;
    }

    public static class ChargeKeyDay {
        ChargeKeyDay(final ProfileKey key, final DayOfWeek day) {
            this.key = key;
            this.day = day;
        }

        public final ProfileKey key;
        public final DayOfWeek day;
    }

    @SuppressWarnings("serial")
    private static final Map<String, ProfileKey> chargeEnableChannelKeys = new HashMap<>() {
        {
            timedChannels.forEach((key, channel) -> {
                put(channel.timer + CHARGE_ENABLED, key);
            });
            put(CHARGE_PROFILE_CLIMATE, ProfileKey.CLIMATE);
        }
    };

    @SuppressWarnings("serial")
    private static final Map<String, ChargeKeyHour> chargeTimeChannelKeys = new HashMap<>() {
        {
            timedChannels.forEach((key, channel) -> {
                put(channel.time + CHARGE_HOUR, new ChargeKeyHour(key, true));
                put(channel.time + CHARGE_MINUTE, new ChargeKeyHour(key, false));
            });
        }
    };

    @SuppressWarnings("serial")
    private static final Map<String, ChargeKeyDay> chargeDayChannelKeys = new HashMap<>() {
        {
            dayChannels.forEach((dayOfWeek, dayChannel) -> {
                put(CHARGE_TIMER1 + dayChannel, new ChargeKeyDay(ProfileKey.TIMER1, dayOfWeek));
                put(CHARGE_TIMER2 + dayChannel, new ChargeKeyDay(ProfileKey.TIMER2, dayOfWeek));
                put(CHARGE_TIMER3 + dayChannel, new ChargeKeyDay(ProfileKey.TIMER3, dayOfWeek));
            });
        }
    };

    public static @Nullable TimedChannel getTimedChannel(ProfileKey key) {
        return timedChannels.get(key);
    }

    public static @Nullable String getDaysChannel(DayOfWeek day) {
        return dayChannels.get(day);
    }

    public static @Nullable ProfileKey getEnableKey(final String id) {
        return chargeEnableChannelKeys.get(id);
    }

    public static @Nullable ChargeKeyDay getKeyDay(final String id) {
        return chargeDayChannelKeys.get(id);
    }

    public static @Nullable ChargeKeyHour getKeyHour(final String id) {
        return chargeTimeChannelKeys.get(id);
    }

    public static String formatDays(final Set<DayOfWeek> weekdays) {
        return weekdays.stream().map(day -> Constants.DAYS.get(day)).collect(Collectors.joining(Constants.COMMA));
    }
}
