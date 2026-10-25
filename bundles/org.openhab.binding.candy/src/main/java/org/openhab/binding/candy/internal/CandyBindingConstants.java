/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.candy.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Identifiers shared by the Candy binding and its metadata.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public final class CandyBindingConstants {
    public static final String BINDING_ID = "candy";
    public static final ThingTypeUID THING_TYPE_BIANCA = new ThingTypeUID(BINDING_ID, "bianca");
    public static final String CHANNEL_MACHINE_STATE = "machine-state";
    public static final String CHANNEL_PROGRAM_PHASE = "program-phase";
    public static final String CHANNEL_PROGRAM_NUMBER = "program-number";
    public static final String CHANNEL_PROGRAM_CODE = "program-code";
    public static final String CHANNEL_REMAINING_TIME = "remaining-time";
    public static final String CHANNEL_TARGET_TEMPERATURE = "target-temperature";
    public static final String CHANNEL_TARGET_SPIN_SPEED = "target-spin-speed";
    public static final String CHANNEL_REMOTE_CONTROL_ENABLED = "remote-control-enabled";
    public static final String CHANNEL_ERROR_CODE = "error-code";
    public static final String CHANNEL_LAST_UPDATE = "last-update";
    public static final String CHANNEL_MACHINE_MODE_CODE = "machine-mode-code";
    public static final String CHANNEL_PROGRAM_PHASE_CODE = "program-phase-code";
    public static final String CHANNEL_PROGRAM_CATALOG = "program-catalog";
    public static final String CHANNEL_CATALOG_STATUS = "catalog-status";
    public static final String CHANNEL_SELECTED_PROGRAM = "selected-program";
    public static final String CHANNEL_START = "start";
    public static final String CHANNEL_START_PROGRAM = "start-program";
    public static final String CHANNEL_STOP = "stop";
    public static final String CHANNEL_LAST_COMMAND_RESULT = "last-command-result";
    public static final Set<String> LIVE_CHANNELS = Set.of(CHANNEL_MACHINE_STATE, CHANNEL_PROGRAM_PHASE,
            CHANNEL_PROGRAM_NUMBER, CHANNEL_PROGRAM_CODE, CHANNEL_REMAINING_TIME, CHANNEL_TARGET_TEMPERATURE,
            CHANNEL_TARGET_SPIN_SPEED, CHANNEL_REMOTE_CONTROL_ENABLED, CHANNEL_ERROR_CODE, CHANNEL_MACHINE_MODE_CODE,
            CHANNEL_PROGRAM_PHASE_CODE);

    private CandyBindingConstants() {
    }
}
