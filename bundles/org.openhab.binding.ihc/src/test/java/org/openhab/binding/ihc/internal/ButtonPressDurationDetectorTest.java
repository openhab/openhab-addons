/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.ihc.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.Test;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class ButtonPressDurationDetectorTest {

    @Test
    public void testShortPress() {
        Duration duration = Duration.ofMillis(450);
        long longPressTime = 1000;
        long longPressMaxTime = 2000;

        ButtonPressDurationDetector button = new ButtonPressDurationDetector(duration, longPressTime, longPressMaxTime);

        assertTrue(button.isShortPress());
        assertFalse(button.isLongPress());
    }

    @Test
    public void testLongPress() {
        Duration duration = Duration.ofMillis(1003);
        long longPressTime = 1000;
        long longPressMaxTime = 2000;

        ButtonPressDurationDetector button = new ButtonPressDurationDetector(duration, longPressTime, longPressMaxTime);

        assertFalse(button.isShortPress());
        assertTrue(button.isLongPress());
    }

    @Test
    public void testExtraLongPress() {
        Duration duration = Duration.ofMillis(2423);
        long longPressTime = 1000;
        long longPressMaxTime = 2000;

        ButtonPressDurationDetector button = new ButtonPressDurationDetector(duration, longPressTime, longPressMaxTime);

        assertFalse(button.isShortPress());
        assertFalse(button.isLongPress());
    }

    @Test
    public void testTooLongPress() {
        Duration duration = Duration.ofMillis(5001);
        long longPressTime = 1000;
        long longPressMaxTime = 2000;

        ButtonPressDurationDetector button = new ButtonPressDurationDetector(duration, longPressTime, longPressMaxTime);

        assertFalse(button.isShortPress());
        assertFalse(button.isLongPress());
    }
}
