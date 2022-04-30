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
    SD_CARD_MISSING(List.of(EventType.SD), 1),
    SD_CARD_INSERTED(List.of(EventType.SD), 2),
    SD_CARD_FORMATTED(List.of(EventType.SD), 3),
    SD_CARD_WORKING(List.of(EventType.SD), 4),
    SD_CARD_DEFECTIVE(List.of(EventType.SD), 5),
    SD_CARD_INCOMPATIBLE_SPEED(List.of(EventType.SD), 6),
    SD_CARD_INSUFFICIENT_SPACE(List.of(EventType.SD), 7),
    ALIM_INCORRECT_POWER(List.of(EventType.ALIM), 1),
    ALIM_CORRECT_POWER(List.of(EventType.ALIM), 2),

    // Artificially implemented by the binding subtypes
    PERSON_ARRIVAL(List.of(EventType.PERSON, EventType.PERSON_HOME), 1),
    PERSON_SEEN(List.of(EventType.PERSON), 2),
    PERSON_DEPARTURE(List.of(EventType.PERSON_AWAY), 1),
    MOVEMENT_HUMAN(List.of(EventType.MOVEMENT, EventType.HUMAN), 1),
    MOVEMENT_VEHICLE(List.of(EventType.MOVEMENT), 2),
    MOVEMENT_ANIMAL(List.of(EventType.MOVEMENT, EventType.ANIMAL), 3);

    public final List<EventType> types;
    public final int subType;

    EventSubType(List<EventType> types, int i) {
        this.types = types;
        this.subType = i;
    }
}
