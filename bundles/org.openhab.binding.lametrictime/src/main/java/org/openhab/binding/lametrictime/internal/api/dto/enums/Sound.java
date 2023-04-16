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
package org.openhab.binding.lametrictime.internal.api.dto.enums;

import org.openhab.binding.lametrictime.internal.api.dto.ApiValue;

/**
 * Enum for sound.
 *
 * @author Gregory Moyer - Initial contribution
 */
public enum Sound implements ApiValue {
    BICYCLE(SoundCategory.NOTIFICATIONS),
    CAR(SoundCategory.NOTIFICATIONS),
    CASH(SoundCategory.NOTIFICATIONS),
    CAT(SoundCategory.NOTIFICATIONS),
    DOG(SoundCategory.NOTIFICATIONS),
    DOG2(SoundCategory.NOTIFICATIONS),
    ENERGY(SoundCategory.NOTIFICATIONS),
    KNOCK_KNOCK(SoundCategory.NOTIFICATIONS, "knock-knock"),
    LETTER_EMAIL(SoundCategory.NOTIFICATIONS),
    LOSE1(SoundCategory.NOTIFICATIONS),
    LOSE2(SoundCategory.NOTIFICATIONS),
    NEGATIVE1(SoundCategory.NOTIFICATIONS),
    NEGATIVE2(SoundCategory.NOTIFICATIONS),
    NEGATIVE3(SoundCategory.NOTIFICATIONS),
    NEGATIVE4(SoundCategory.NOTIFICATIONS),
    NEGATIVE5(SoundCategory.NOTIFICATIONS),
    NOTIFICATION(SoundCategory.NOTIFICATIONS),
    NOTIFICATION2(SoundCategory.NOTIFICATIONS),
    NOTIFICATION3(SoundCategory.NOTIFICATIONS),
    NOTIFICATION4(SoundCategory.NOTIFICATIONS),
    OPEN_DOOR(SoundCategory.NOTIFICATIONS),
    POSITIVE1(SoundCategory.NOTIFICATIONS),
    POSITIVE2(SoundCategory.NOTIFICATIONS),
    POSITIVE3(SoundCategory.NOTIFICATIONS),
    POSITIVE4(SoundCategory.NOTIFICATIONS),
    POSITIVE5(SoundCategory.NOTIFICATIONS),
    POSITIVE6(SoundCategory.NOTIFICATIONS),
    STATISTIC(SoundCategory.NOTIFICATIONS),
    THUNDER(SoundCategory.NOTIFICATIONS),
    WATER1(SoundCategory.NOTIFICATIONS),
    WATER2(SoundCategory.NOTIFICATIONS),
    WIN(SoundCategory.NOTIFICATIONS),
    WIN2(SoundCategory.NOTIFICATIONS),
    WIND(SoundCategory.NOTIFICATIONS),
    WIND_SHORT(SoundCategory.NOTIFICATIONS),

    ALARM1(SoundCategory.ALARMS),
    ALARM2(SoundCategory.ALARMS),
    ALARM3(SoundCategory.ALARMS),
    ALARM4(SoundCategory.ALARMS),
    ALARM5(SoundCategory.ALARMS),
    ALARM6(SoundCategory.ALARMS),
    ALARM7(SoundCategory.ALARMS),
    ALARM8(SoundCategory.ALARMS),
    ALARM9(SoundCategory.ALARMS),
    ALARM10(SoundCategory.ALARMS),
    ALARM11(SoundCategory.ALARMS),
    ALARM12(SoundCategory.ALARMS),
    ALARM13(SoundCategory.ALARMS);

    private final SoundCategory category;
    private final String rawValue;

    private Sound(SoundCategory category) {
        this(category, null);
    }

    private Sound(SoundCategory category, String rawValue) {
        this.category = category;
        this.rawValue = rawValue;
    }

    public SoundCategory getCategory() {
        return category;
    }

    @Override
    public String toRaw() {
        return rawValue != null ? rawValue : name().toLowerCase();
    }

    public static Sound toEnum(String raw) {
        if (raw == null) {
            return null;
        }

        if (KNOCK_KNOCK.rawValue.equals(raw)) {
            return KNOCK_KNOCK;
        }

        try {
            return valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            // not a valid raw string
            return null;
        }
    }
}
