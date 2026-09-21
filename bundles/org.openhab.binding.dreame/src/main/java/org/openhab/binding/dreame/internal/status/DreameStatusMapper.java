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
package org.openhab.binding.dreame.internal.status;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Maps Dreame protocol status codes to stable openHAB channel values.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public final class DreameStatusMapper {

    private DreameStatusMapper() {
    }

    public static String stateName(int state) {
        return switch (state) {
            case 1 -> "mowing";
            case 2 -> "idle";
            case 3 -> "paused";
            case 4 -> "paused_due_to_error";
            case 5 -> "returning";
            case 6 -> "charging";
            case 11 -> "mapping";
            case 13 -> "charging_completed";
            case 14 -> "upgrading";
            case 15 -> "charging_paused_hot";
            case 16 -> "charging_paused_cold";
            case 75 -> "paused_at_maintenance_point";
            default -> "unknown";
        };
    }

    public static String taskOperationName(int operation) {
        return switch (operation) {
            case 6 -> "charging";
            case 100 -> "mowing";
            default -> "unknown";
        };
    }

    public static @Nullable Boolean taskActiveFromState(int state) {
        return switch (state) {
            case 1, 5 -> true;
            case 2, 3, 4, 6, 13, 75 -> false;
            default -> null;
        };
    }

    public static String chargingName(int status) {
        return switch (status) {
            case 1 -> "charging";
            case 0, 2 -> "not_charging";
            case 3 -> "charging_completed";
            case 5 -> "return_to_charge";
            default -> "unknown";
        };
    }

    public static String dockingName(int state) {
        return switch (state) {
            case 0 -> "in_station";
            case 1 -> "out_of_station";
            case 2 -> "pause_docking";
            case 3 -> "finish_docking";
            case 4 -> "docking_failed";
            case 5 -> "docking_in_base";
            default -> "unknown";
        };
    }

    public static boolean isDockedState(int state) {
        return state == 6 || state == 13;
    }

    public static boolean isRssiAvailable(int rssi) {
        return rssi != -128 && rssi != 127;
    }
}
