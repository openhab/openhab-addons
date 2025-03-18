/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.services.alarm.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Possible states of smoke detector alarms.
 * 
 * @author David Pace - Initial contribution
 *
 */
public enum AlarmState {
    IDLE_OFF,
    PRIMARY_ALARM,
    SECONDARY_ALARM,
    INTRUSION_ALARM,
    INTRUSION_ALARM_ON_REQUESTED,
    INTRUSION_ALARM_OFF_REQUESTED;

    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmState.class);

    public static AlarmState from(String identifier) {
        try {
            return valueOf(identifier);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Unsupported alarm state: {}", identifier);
            return IDLE_OFF;
        }
    }
}
