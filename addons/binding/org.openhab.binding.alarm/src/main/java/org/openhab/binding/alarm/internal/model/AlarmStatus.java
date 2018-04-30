/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.alarm.internal.model;

/**
 * Enumeration of all alarm status.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public enum AlarmStatus {
    DISARMED,
    INTERNALLY_ARMED,
    EXTERNALLY_ARMED,
    ENTRY,
    EXIT,
    PASSTHROUGH,
    PREALARM,
    ALARM,
    SABOTAGE_ALARM
}
