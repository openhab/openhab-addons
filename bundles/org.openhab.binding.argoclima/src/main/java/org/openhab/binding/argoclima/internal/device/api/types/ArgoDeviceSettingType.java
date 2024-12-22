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
package org.openhab.binding.argoclima.internal.device.api.types;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Type representing the concrete Argo API element knob
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public enum ArgoDeviceSettingType {
    TARGET_TEMPERATURE,
    ACTUAL_TEMPERATURE,
    POWER,
    MODE,
    FAN_LEVEL,
    FLAP_LEVEL,
    I_FEEL_TEMPERATURE,
    FILTER_MODE,
    ECO_MODE,
    TURBO_MODE,
    NIGHT_MODE,
    LIGHT,
    ECO_POWER_LIMIT,
    RESET_TO_FACTORY_SETTINGS,
    UNIT_FIRMWARE_VERSION,
    DISPLAY_TEMPERATURE_SCALE,
    CURRENT_TIME,
    CURRENT_DAY_OF_WEEK,
    ACTIVE_TIMER,
    TIMER_0_DELAY_TIME,
    TIMER_N_ENABLED_DAYS,
    TIMER_N_ON_TIME,
    TIMER_N_OFF_TIME
}
