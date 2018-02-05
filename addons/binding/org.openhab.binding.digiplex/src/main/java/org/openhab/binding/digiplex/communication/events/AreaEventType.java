/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.digiplex.communication.events;

/**
 * Area event type.
 *
 * @author Robert Michalak - Initial contribution
 *
 */
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
