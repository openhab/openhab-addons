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
package org.openhab.binding.digiplex.internal.communication.events;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Area event type.
 *
 * @author Robert Michalak - Initial contribution
 *
 */
@NonNullByDefault
public enum AreaEventType {
    ARMED,
    ARMED_FORCE,
    ARMED_STAY,
    ARMED_INSTANT,

    DISARMED,

    ALARM_STROBE,
    ALARM_SILENT,
    ALARM_AUDIBLE,
    ALARM_FIRE,

    READY,
    EXIT_DELAY,
    ENTRY_DELAY,
    SYSTEM_IN_TROUBLE,
    ALARM_IN_MEMORY,
    ZONES_BYPASSED,
}
