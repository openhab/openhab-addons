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
package org.openhab.binding.tesla.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tesla.internal.protocol.dto.VehicleState;
import org.openhab.core.library.types.OnOffType;

import com.google.gson.Gson;

/**
 * Tests the combined tire pressure warning of {@link TeslaVehicleHandler}.
 *
 * @author Ronny Glöckner - Initial contribution
 */
@NonNullByDefault
public class TeslaVehicleHandlerTirePressureTest {

    private static final String NO_WARNINGS = "\"tpms_soft_warning_fl\":false,\"tpms_soft_warning_fr\":false,"
            + "\"tpms_soft_warning_rl\":false,\"tpms_soft_warning_rr\":false,\"tpms_hard_warning_fl\":false,"
            + "\"tpms_hard_warning_fr\":false,\"tpms_hard_warning_rl\":false,\"tpms_hard_warning_rr\":false";

    private final Gson gson = new Gson();

    @Test
    public void offWithoutWarnings() {
        assertEquals(OnOffType.OFF, TeslaVehicleHandler.tirePressureWarning(vehicleState("{" + NO_WARNINGS + "}")));
    }

    @Test
    public void onForASoftWarning() {
        String json = "{" + NO_WARNINGS.replace("\"tpms_soft_warning_rl\":false", "\"tpms_soft_warning_rl\":true")
                + "}";
        assertEquals(OnOffType.ON, TeslaVehicleHandler.tirePressureWarning(vehicleState(json)));
    }

    @Test
    public void onForAHardWarning() {
        String json = "{" + NO_WARNINGS.replace("\"tpms_hard_warning_fr\":false", "\"tpms_hard_warning_fr\":true")
                + "}";
        assertEquals(OnOffType.ON, TeslaVehicleHandler.tirePressureWarning(vehicleState(json)));
    }

    @Test
    public void nullIfTheVehicleReportsNoWarnings() {
        assertNull(TeslaVehicleHandler.tirePressureWarning(vehicleState("{\"locked\":true}")));
    }

    private VehicleState vehicleState(String json) {
        VehicleState vehicleState = gson.fromJson(json, VehicleState.class);
        assertNotNull(vehicleState);
        return vehicleState;
    }
}
