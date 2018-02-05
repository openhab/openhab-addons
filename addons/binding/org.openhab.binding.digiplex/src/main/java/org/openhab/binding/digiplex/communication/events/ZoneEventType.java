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
 * Type of zone-related event
 *
 * @author Robert Michalak - Initial contribution
 *
 */
public enum ZoneEventType {
    TX_DELAY_ZONE_ALARM,
    BYPASSED,
    ALARM,
    FIRE_ALARM,
    ALARM_RESTORE,
    FIRE_ALARM_RESTORE,
    SHUTDOWN,
    TAMPER,
    TAMPER_RESTORE,
    LOW_BATTERY,
    LOW_BATTERY_RESTORE,
    SUPERVISION_TROUBLE,
    SUPERVISION_TROUBLE_RESTORE,
    INTELLIZONE_TRIGGERED
}
