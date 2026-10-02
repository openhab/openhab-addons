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
package org.openhab.binding.ipcamera.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.types.StateOption;

/**
 * Tests for {@link ReolinkPtz}.
 *
 * @author Gerhard Braun - Initial contribution
 */
@NonNullByDefault
public class ReolinkPtzTest {

    @Test
    public void positionToPercentPanIsInverted() {
        assertTrue(ReolinkPtz.isInverted(ReolinkPtz.Axis.PAN));
        assertEquals(100, ReolinkPtz.positionToPercent(0, 0, 6000, true));
        assertEquals(71, ReolinkPtz.positionToPercent(1769, 0, 6000, true));
        assertEquals(0, ReolinkPtz.positionToPercent(6000, 0, 6000, true));
    }

    @Test
    public void positionToPercentTilt() {
        assertFalse(ReolinkPtz.isInverted(ReolinkPtz.Axis.TILT));
        assertEquals(0, ReolinkPtz.positionToPercent(150, 150, 1400, false));
        assertEquals(100, ReolinkPtz.positionToPercent(1400, 150, 1400, false));
        assertEquals(21, ReolinkPtz.positionToPercent(408, 150, 1400, false));
    }

    @Test
    public void positionToPercentIsClampedAndHandlesInvalidRange() {
        assertEquals(0, ReolinkPtz.positionToPercent(-50, 0, 6000, false));
        assertEquals(100, ReolinkPtz.positionToPercent(7000, 0, 6000, false));
        assertEquals(0, ReolinkPtz.positionToPercent(100, 500, 500, false));
    }

    @Test
    public void percentToPositionRoundTrip() {
        assertEquals(1500, ReolinkPtz.percentToPosition(75, 0, 6000, true));
        assertEquals(775, ReolinkPtz.percentToPosition(50, 150, 1400, false));
        assertEquals(1400, ReolinkPtz.percentToPosition(100, 150, 1400, false));
        assertEquals(6000, ReolinkPtz.percentToPosition(120, 0, 6000, false));
    }

    @Test
    public void moveOpMatchesOnvifDirections() {
        assertEquals("Left", ReolinkPtz.moveOp(ReolinkPtz.Axis.PAN, true));
        assertEquals("Right", ReolinkPtz.moveOp(ReolinkPtz.Axis.PAN, false));
        assertEquals("Down", ReolinkPtz.moveOp(ReolinkPtz.Axis.TILT, true));
        assertEquals("Up", ReolinkPtz.moveOp(ReolinkPtz.Axis.TILT, false));
    }

    @Test
    public void parsePosition() {
        String reply = "[ {\"cmd\" : \"GetPtzCurPos\", \"code\" : 0, \"value\" : { \"PtzCurPos\" : "
                + "{ \"Ppos\" : 1769, \"Tpos\" : 408, \"channel\" : 0 } } } ]";
        int[] position = ReolinkPtz.parsePosition(reply);
        assertNotNull(position);
        assertEquals(1769, position[0]);
        assertEquals(408, position[1]);
    }

    @Test
    public void parsePositionRejectsErrorsAndGarbage() {
        assertNull(ReolinkPtz.parsePosition(
                "[{\"cmd\":\"GetPtzCurPos\",\"code\":1,\"error\":{\"detail\":\"not support\",\"rspCode\":-9}}]"));
        assertNull(ReolinkPtz.parsePosition("not json"));
        assertNull(ReolinkPtz.parsePosition("[]"));
    }

    @Test
    public void parsePresetsSkipsDisabledAndNamesUnnamed() {
        String reply = "[{\"cmd\":\"GetPtzPreset\",\"code\":0,\"value\":{\"PtzPreset\":["
                + "{\"channel\":0,\"enable\":1,\"id\":0,\"name\":\"Hinten\"},"
                + "{\"channel\":0,\"enable\":0,\"id\":1,\"name\":\"\"},"
                + "{\"channel\":0,\"enable\":1,\"id\":2,\"name\":\"\"}]}}]";
        List<StateOption> presets = ReolinkPtz.parsePresets(reply);
        assertNotNull(presets);
        assertEquals(2, presets.size());
        assertEquals("0", presets.get(0).getValue());
        assertEquals("Hinten", presets.get(0).getLabel());
        assertEquals("2", presets.get(1).getValue());
        assertEquals("Preset 2", presets.get(1).getLabel());
    }

    @Test
    public void parsePresetsRejectsMissingList() {
        assertNull(ReolinkPtz.parsePresets("[{\"cmd\":\"GetPtzPreset\",\"code\":0,\"value\":{}}]"));
    }
}
