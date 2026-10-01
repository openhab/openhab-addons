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
package org.openhab.binding.motionblinds.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SlatTracker}, using calibration runs recorded with a real venetian blind: reported position and
 * motor angle, and the observed slats (closed/bottom visible = 0%, horizontal = 50%, closed/top visible = 100%).
 *
 * @author Oleksandr Mishchuk - Initial contribution
 */
@NonNullByDefault
public class SlatTrackerTest {

    /** The slats could be observed to about half a quarter swing */
    private static final int TOLERANCE = 15;

    private final SlatTracker tracker = new SlatTracker(3, 2);

    @Test
    public void unknownUntilFirstStatus() {
        assertNull(tracker.getTilt());
        assertNull(tracker.tiltCommand(50));
    }

    @Test
    public void upFromHorizontalAfterLowering() {
        observe(100, 180, 100);
        observe(98, 118, 50); // horizontal
        observe(97, 98, 25); // 45°, bottom visible
        observe(96, 78, 0); // closed
        observe(96, 58, 0);
        observe(95, 38, 0);
    }

    @Test
    public void downFromHorizontalTurnsSlowlyOnTheSill() {
        observe(100, 180, 100);
        observe(98, 118, 50); // horizontal
        observe(98, 140, 55); // 10°, top visible
        observe(99, 162, 75); // 45°, top visible
        observe(100, 180, 100);
    }

    @Test
    public void downAtMidHeight() {
        observe(44, 0, 5); // almost closed, bottom visible
        observe(45, 20, 15); // 30°, bottom visible
        observe(46, 42, 50); // about horizontal
        observe(46, 62, 75); // 45°, top visible
        observe(47, 84, 100); // closed
        observe(48, 104, 100);
        observe(49, 124, 100);
    }

    @Test
    public void slatsDropToHorizontalWhenBottomRailLifts() {
        // separate trials, each from fully lowered
        observe(100, 176, 100);
        observe(98, 118, 50); // horizontal
        observe(100, 176, 100);
        observe(97, 108, 45); // almost horizontal, a bit of bottom visible
        observe(100, 177, 100);
        observe(97, 98, 35); // a bit tilted, bottom visible
        observe(100, 176, 100);
        observe(97, 86, 25); // 45°, bottom visible
    }

    @Test
    public void roundedFullyLoweredPositionDoesNotResetSlack() {
        tracker.update(100, 176);
        // the blind started to move up, but the position is still rounded to 100
        tracker.update(100, 150);
        assertEquals(100, tracker.getTilt());
        tracker.update(99, 118);
        assertEquals(50, tracker.getTilt());
    }

    @Test
    public void motorStoppingShortOfTheSlackCountsAsHorizontal() {
        tracker.update(100, 176);
        // the motor overshot less than expected and stopped 4° before the end of the slack
        tracker.update(98, 122);
        assertEquals(50, tracker.getTilt());
    }

    @Test
    public void tiltHorizontalAndClosedFromFullyLowered() {
        tracker.update(100, 176);
        // up through the complete slack, minus the overshoot of the motor: observed horizontal
        assertEquals(Map.of("targetAngle", 176 - 58 + SlatTracker.MOTOR_OVERSHOOT), tracker.tiltCommand(50));
        tracker.update(98, 118);
        assertEquals(Map.of(), tracker.tiltCommand(50));
        // half a swing up, with extra travel to close
        assertTargetAngle(118 - 44 - SlatTracker.CLOSE_MARGIN + SlatTracker.MOTOR_OVERSHOOT, tracker.tiltCommand(0));
    }

    @Test
    public void tiltAboveHorizontalFromFullyLoweredTakesTwoMoves() {
        tracker.update(100, 176);
        assertEquals(Map.of(), tracker.tiltCommand(100));
        // first up to horizontal
        assertEquals(Map.of("targetAngle", 176 - 58 + SlatTracker.MOTOR_OVERSHOOT), tracker.tiltCommand(75));
        tracker.update(98, 118);
        // then down, at half the rate on the sill: a quarter swing takes 44° instead of 22°
        assertTargetAngle(118 + 44 - SlatTracker.MOTOR_OVERSHOOT, tracker.tiltCommand(75));
    }

    @Test
    public void tiltAtMidHeight() {
        tracker.update(49, 0);
        assertTargetAngle(44 - SlatTracker.MOTOR_OVERSHOOT, tracker.tiltCommand(50));
        assertTargetAngle(65 - SlatTracker.MOTOR_OVERSHOOT, tracker.tiltCommand(75));
        assertEquals(Map.of(), tracker.tiltCommand(0));
        // too small for the motor
        assertEquals(Map.of(), tracker.tiltCommand(5));

        tracker.update(55, 180);
        assertTargetAngle(180 - 44 + SlatTracker.MOTOR_OVERSHOOT, tracker.tiltCommand(50));
    }

    @Test
    public void closingGetsExtraTravel() {
        tracker.update(60, 180);
        tracker.update(59, 136);
        assertEquals(49, tracker.getTilt());
        assertTargetAngle(136 - 43 - SlatTracker.CLOSE_MARGIN + SlatTracker.MOTOR_OVERSHOOT, tracker.tiltCommand(0));
        assertEquals(Map.of("targetAngle", 180), tracker.tiltCommand(100));
        // no extra travel when the slats are already closed
        tracker.update(58, 92);
        assertEquals(0, tracker.getTilt());
        assertEquals(Map.of(), tracker.tiltCommand(0));
    }

    @Test
    public void firstStatusIsEstimatedFromMotorAngle() {
        tracker.update(97, 92);
        assertEquals(52, tracker.getTilt());
    }

    private void observe(int position, int angle, int observedTilt) {
        tracker.update(position, angle);
        @Nullable
        Integer tilt = tracker.getTilt();
        assertNotNull(tilt);
        assertTrue(Math.abs(tilt - observedTilt) <= TOLERANCE, "at " + position + "%, motor angle " + angle
                + "°: tracked tilt " + tilt + "%, observed " + observedTilt + "%");
    }

    /**
     * The best move is chosen in whole degrees, so allow for rounding.
     */
    private static void assertTargetAngle(int expected, @Nullable Map<String, Object> command) {
        assertNotNull(command);
        Object targetAngle = command.get("targetAngle");
        assertTrue(targetAngle instanceof Integer angle && Math.abs(angle - expected) <= 2,
                "expected targetAngle " + expected + " ± 2, got " + command);
    }
}
