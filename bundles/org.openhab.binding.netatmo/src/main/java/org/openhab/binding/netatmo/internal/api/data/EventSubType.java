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
package org.openhab.binding.netatmo.internal.api.data;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This enum describes sub events in relation to a given event
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum EventSubType {
    // SD Card sub events
    SD_CARD_MISSING(1, EventType.SD),
    SD_CARD_INSERTED(2, EventType.SD),
    SD_CARD_FORMATTED(3, EventType.SD),
    SD_CARD_WORKING(4, EventType.SD),
    SD_CARD_DEFECTIVE(5, EventType.SD),
    SD_CARD_INCOMPATIBLE_SPEED(6, EventType.SD),
    SD_CARD_INSUFFICIENT_SPACE(7, EventType.SD),

    // Alimentation sub events
    ALIM_INCORRECT_POWER(1, EventType.ALIM),
    ALIM_CORRECT_POWER(2, EventType.ALIM),

    // Smoke detector sub events
    DETECTION_CHAMBER_CLEAN(0, EventType.DETECTION_CHAMBER_STATUS),
    DETECTION_CHAMBER_DIRTY(1, EventType.DETECTION_CHAMBER_STATUS),
    BATTERY_LOW(0, EventType.BATTERY_STATUS),
    BATTERY_VERY_LOW(1, EventType.BATTERY_STATUS),
    SMOKE_CLEARED(0, EventType.SMOKE),
    SMOKE_DETECTED(1, EventType.SMOKE),
    SOUND_TEST_OK(0, EventType.SOUND_TEST),
    SOUND_TEST_ERROR(1, EventType.SOUND_TEST),
    DETECTOR_READY(0, EventType.TAMPERED),
    DETECTOR_TAMPERED(1, EventType.TAMPERED),
    WIFI_STATUS_OK(1, EventType.WIFI_STATUS),
    WIFI_STATUS_ERROR(0, EventType.WIFI_STATUS),

    // Artificially implemented by the binding subtypes
    PERSON_ARRIVAL(1, EventType.PERSON, EventType.PERSON_HOME),
    PERSON_SEEN(2, EventType.PERSON),
    PERSON_DEPARTURE(1, EventType.PERSON_AWAY),
    MOVEMENT_HUMAN(1, EventType.MOVEMENT, EventType.HUMAN),
    MOVEMENT_VEHICLE(2, EventType.MOVEMENT),
    MOVEMENT_ANIMAL(3, EventType.MOVEMENT, EventType.ANIMAL);

    public final List<EventType> types;
    public final int subType;

    EventSubType(int i, EventType... types) {
        this.types = List.of(types);
        this.subType = i;
    }
}
