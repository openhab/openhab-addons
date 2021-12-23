/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This enum describes sub events in relation to a given event
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum EventSubType {
    SD_CARD_MISSING(EventType.SD, 1),
    SD_CARD_INSERTED(EventType.SD, 2),
    SD_CARD_FORMATTED(EventType.SD, 3),
    SD_CARD_WORKING(EventType.SD, 4),
    SD_CARD_DEFECTIVE(EventType.SD, 5),
    SD_CARD_INCOMPATIBLE_SPEED(EventType.SD, 6),
    SD_CARD_INSUFFICIENT_SPACE(EventType.SD, 7),
    ALIM_INCORRECT_POWER(EventType.ALIM, 1),
    ALIM_CORRECT_POWER(EventType.ALIM, 2),

    // Artificially implemented by the binding subtypes
    PERSON_ARRIVAL(EventType.PERSON, 1),
    MOVEMENT_HUMAN(EventType.MOVEMENT, 1),
    MOVEMENT_VEHICLE(EventType.MOVEMENT, 2),
    MOVEMENT_ANIMAL(EventType.MOVEMENT, 3);

    public final EventType type;
    public final int subType;

    EventSubType(EventType type, int i) {
        this.type = type;
        this.subType = i;
    }
}
