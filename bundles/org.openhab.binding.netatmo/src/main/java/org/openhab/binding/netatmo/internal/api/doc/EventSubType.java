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
package org.openhab.binding.netatmo.internal.api.doc;

/**
 * This enum describes sub events in relation to a given event
 * according to API documentation
 *
 * @author Gaël L'hopital - Initial contribution
 */
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

    // Left for future implementation
    // SOUNDING_STOPPED(EventType.SIREN_SOUNDING, 0),
    // SOUNDING_STARTED(EventType.SIREN_SOUNDING, 1),
    //
    // CHAMBER_CLEAN(EventType.DETECTION_CHAMBER_STATUS, 0),
    // CHAMBER_DUSTY(EventType.DETECTION_CHAMBER_STATUS, 1),
    //
    // SOUND_TEST_OK(EventType.SOUND_TEST, 0),
    // SOUND_TEST_ERROR(EventType.SOUND_TEST, 1),
    //
    // BATTERY_VERY_LOW(EventType.BATTERY_STATUS, 1),
    //
    // TAMPERED_READY(EventType.TAMPERED, 0),
    // TAMPERED_TAMPERED(EventType.TAMPERED, 1),
    //
    // SMOKE_CLEARED(EventType.SMOKE, 0),
    // SMOKE_DETECTED(EventType.SMOKE, 1),
    //
    // WIFI_ERROR(EventType.WIFI_STATUS, 0),
    // WIFI_OK(EventType.WIFI_STATUS, 1);

    EventType type;
    int subType;

    EventSubType(EventType sd, int i) {
        this.type = sd;
        this.subType = i;
    }

    public EventType getType() {
        return type;
    }

    public int getSubType() {
        return subType;
    }
}
