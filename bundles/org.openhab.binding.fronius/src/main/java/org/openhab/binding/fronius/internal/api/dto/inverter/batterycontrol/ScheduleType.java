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
package org.openhab.binding.fronius.internal.api.dto.inverter.batterycontrol;

/**
 * Enum for the schedule type of the battery control.
 *
 * @author Florian Hotze - Initial contribution
 */
public enum ScheduleType {
    CHARGE_MIN,
    CHARGE_MAX,
    DISCHARGE_MIN,
    DISCHARGE_MAX
}
