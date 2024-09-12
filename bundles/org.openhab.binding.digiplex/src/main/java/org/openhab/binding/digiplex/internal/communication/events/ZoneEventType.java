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
 * Type of zone-related event
 *
 * @author Robert Michalak - Initial contribution
 *
 */
@NonNullByDefault
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
