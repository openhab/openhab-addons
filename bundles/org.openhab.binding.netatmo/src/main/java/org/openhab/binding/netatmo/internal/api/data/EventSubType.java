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
 * according to API documentation
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum EventSubType {
    MISSING_SD(EventType.SD, 1), // Missing SD Card
    SD_INSERTED(EventType.SD, 2), // SD Card inserted
    SD_FORMATED(EventType.SD, 3), // SD Card formated
    WORKING_SD(EventType.SD, 4), // Working SD Card
    DEFECTIVE_SD(EventType.SD, 5), // Defective SD Card
    INCOMPATIBLE_SD_SPEED(EventType.SD, 6), // Incompatible SD Card speed
    INSUFFICIENT_SD_SPACE(EventType.SD, 7), // Insufficient SD Card space
    INCORRECT_POWER(EventType.ALIM, 1), // incorrect power adapter
    CORRECT_POWER(EventType.ALIM, 2), // correct power adapter

    // Artificially implemented by the binding subtypes
    ARRIVAL(EventType.PERSON, 1), // Person arrived
    HUMAN(EventType.MOVEMENT, 1), // Human seen
    VEHICLE(EventType.MOVEMENT, 2), // Car seen
    ANIMAL(EventType.MOVEMENT, 3); // Animal seen

    public final EventType type;
    public final int subType;

    EventSubType(EventType sd, int i) {
        this.type = sd;
        this.subType = i;
    }
}
