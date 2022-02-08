/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.hdpowerview.internal.api.CoordinateSystem.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase.Capabilities;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Unit tests for Shade Position setting and getting.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ShadePositionTest {

    private final ShadeCapabilitiesDatabase db = new ShadeCapabilitiesDatabase();

    /**
     * General tests of the database of known types.
     */
    @Test
    public void testKnownTypesDatabase() {
        assertTrue(db.isTypeInDatabase(4));
        assertTrue(db.isCapabilitiesInDatabase(0));

        assertTrue(db.getCapabilities(0).supportsPrimary());
        assertTrue(db.getCapabilities(0).supportsTiltOnClosed());
        assertTrue(db.getCapabilities(1).supportsTiltOnClosed());
        assertTrue(db.getCapabilities(2).supportsTilt180());
        assertTrue(db.getCapabilities(3).supportsTiltOnClosed());
        assertTrue(db.getCapabilities(4).supportsTilt180());
        assertTrue(db.getCapabilities(5).supportsTilt180());
        assertFalse(db.getCapabilities(5).supportsPrimary());
        assertTrue(db.getCapabilities(6).isPrimaryStateInverted());
        assertTrue(db.getCapabilities(7).supportsSecondary());
        assertTrue(db.getCapabilities(8).supportsBlackoutShade());
        assertTrue(db.getCapabilities(9).supportsBlackoutShade());

        assertEquals(db.getType(4).getCapabilities(), 0);
        assertEquals(db.getType(-1).getCapabilities(), -1);

        assertFalse(db.isTypeInDatabase(99));
        assertFalse(db.isCapabilitiesInDatabase(99));

        assertFalse(db.getCapabilities(0).isPrimaryStateInverted());
        assertFalse(db.getCapabilities(-1).isPrimaryStateInverted());
        assertFalse(db.getCapabilities(99).isPrimaryStateInverted());

        assertFalse(db.getCapabilities(0).supportsSecondary());
        assertFalse(db.getCapabilities(-1).supportsSecondary());
        assertFalse(db.getCapabilities(99).supportsSecondary());
    }

    /**
     * Helper method; test if shade position is a PercentType and that its value is correct.
     *
     * @param position the shade position
     * @param value the test value to compare with
     */
    private void assertShadePosition(State position, int value) {
        assertEquals(PercentType.class, position.getClass());
        assertEquals(value, ((PercentType) position).intValue());
    }

    /**
     * Test parsing of ShadePosition (shade fully up).
     *
     */
    @Test
    public void testShadePositionParsingFullyUp() {
        Capabilities capabilities = db.getCapabilities(0);
        ShadePosition test = new ShadePosition().setPosition(capabilities, PRIMARY_POSITION, 0);
        assertNotNull(test);
        State pos = test.getState(capabilities, PRIMARY_POSITION);
        assertShadePosition(pos, 0);
        pos = test.getState(capabilities, VANE_TILT_POSITION);
        assertTrue(UnDefType.UNDEF.equals(pos));
    }

    /**
     * Test parsing of ShadePosition (shade fully down (method 1)).
     *
     */
    @Test
    public void testShadePositionParsingShadeFullyDown1() {
        Capabilities capabilities = db.getCapabilities(0);
        ShadePosition test = new ShadePosition().setPosition(capabilities, PRIMARY_POSITION, 100);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), 0);
    }

    /**
     * Test parsing of ShadePosition (shade fully down (method 2)).
     *
     */
    @Test
    public void testShadePositionParsingShadeFullyDown2() {
        Capabilities capabilities = db.getCapabilities(0);
        ShadePosition test = new ShadePosition().setPosition(capabilities, VANE_TILT_POSITION, 0);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), 0);
    }

    /**
     * Test parsing of ShadePosition (shade fully down (method 2) and vane fully open).
     *
     */
    @Test
    public void testShadePositionParsingShadeFullyDownVaneOpen() {
        Capabilities capabilities = db.getCapabilities(0);
        ShadePosition test = new ShadePosition().setPosition(capabilities, VANE_TILT_POSITION, 100);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), 100);
    }

    /**
     * On dual rail shades, it should not be possible to drive the upper rail below the lower rail, or vice-versa. So
     * the binding code applies constraints on setting such positions. This test checks that the constraint code is
     * working.
     */
    @Test
    public void testDualRailConstraints() {
        Capabilities capabilities = db.getCapabilities(7);
        ShadePosition test = new ShadePosition();

        // ==== OK !! primary at bottom, secondary at top ====
        test.setPosition(capabilities, PRIMARY_POSITION, 100).setPosition(capabilities, SECONDARY_POSITION, 0);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 0);

        // ==== OK !! primary at middle, secondary at top ====
        test.setPosition(capabilities, PRIMARY_POSITION, 50).setPosition(capabilities, SECONDARY_POSITION, 0);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 50);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 0);

        // ==== OK !! primary at middle, secondary at middle ====
        test.setPosition(capabilities, PRIMARY_POSITION, 50).setPosition(capabilities, SECONDARY_POSITION, 50);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 50);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 50);

        // ==== IMPOSSIBLE !! secondary at middle, primary above => test the constraining code ====
        test.setPosition(capabilities, SECONDARY_POSITION, 0).setPosition(capabilities, PRIMARY_POSITION, 100);
        test.setPosition(capabilities, SECONDARY_POSITION, 40).setPosition(capabilities, PRIMARY_POSITION, 25);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 40);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 40);

        // ==== OK !! secondary at middle, primary below ====
        test.setPosition(capabilities, SECONDARY_POSITION, 0).setPosition(capabilities, PRIMARY_POSITION, 100);
        test.setPosition(capabilities, SECONDARY_POSITION, 50).setPosition(capabilities, PRIMARY_POSITION, 75);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 75);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 50);

        // ==== IMPOSSIBLE !! primary at middle, secondary below => test the constraining code ====
        test.setPosition(capabilities, SECONDARY_POSITION, 0).setPosition(capabilities, PRIMARY_POSITION, 100);
        test.setPosition(capabilities, PRIMARY_POSITION, 60).setPosition(capabilities, SECONDARY_POSITION, 75);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 60);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 60);

        // ==== OK !! primary at middle, secondary above ====
        test.setPosition(capabilities, SECONDARY_POSITION, 0).setPosition(capabilities, PRIMARY_POSITION, 100);
        test.setPosition(capabilities, PRIMARY_POSITION, 60).setPosition(capabilities, SECONDARY_POSITION, 25);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 60);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 25);
    }

    /**
     * Test parsing of DuoLite shades having a secondary blackout shade.
     *
     */
    @Test
    public void testDuoliteShadePositionParsing() {
        // blackout shades have capabilities 8
        Capabilities capabilities = db.getCapabilities(8);
        ShadePosition test;

        // both shades up
        test = new ShadePosition().setPosition(capabilities, PRIMARY_POSITION, 0);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 0);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 0);

        // front shade 50% down
        test = new ShadePosition().setPosition(capabilities, PRIMARY_POSITION, 50);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 50);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 0);

        // front shade 100% down, back shade 0% down
        test = new ShadePosition().setPosition(capabilities, PRIMARY_POSITION, 100);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 0);

        // front shade 100% down, back shade 0% down (ALTERNATE)
        test = new ShadePosition().setPosition(capabilities, SECONDARY_POSITION, 0);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 0);

        // front shade 100% down, back shade 50% down
        test = new ShadePosition().setPosition(capabilities, SECONDARY_POSITION, 50);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 50);

        // front shade 100% down, back shade 100% down
        test = new ShadePosition().setPosition(capabilities, SECONDARY_POSITION, 100);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 100);
    }

    /**
     * Test parsing of DuoLite shades having both a secondary blackout shade, and tilt anywhere functionality.
     *
     */
    @Test
    public void testDuoliteTiltShadePositionParsing() {
        // blackout shades with tilt have capabilities 9
        Capabilities capabilities = db.getCapabilities(9);
        ShadePosition test;

        // both shades up, tilt 0%
        test = new ShadePosition().setPosition(capabilities, PRIMARY_POSITION, 0).setPosition(capabilities,
                VANE_TILT_POSITION, 0);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 0);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 0);

        // front shade 50% down, tilt 30%
        test = new ShadePosition().setPosition(capabilities, PRIMARY_POSITION, 50).setPosition(capabilities,
                VANE_TILT_POSITION, 30);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 50);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 0);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), 30);

        // front shade 100% down, back shade 0% down, tilt 30%
        test = new ShadePosition().setPosition(capabilities, PRIMARY_POSITION, 100).setPosition(capabilities,
                VANE_TILT_POSITION, 30);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 0);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), 30);

        // front shade 100% down, back shade 0% down, tilt 30% (ALTERNATE)
        test = new ShadePosition().setPosition(capabilities, SECONDARY_POSITION, 0).setPosition(capabilities,
                VANE_TILT_POSITION, 30);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 0);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), 30);

        // front shade 100% down, back shade 50% down, tilt 30%
        test = new ShadePosition().setPosition(capabilities, SECONDARY_POSITION, 50).setPosition(capabilities,
                VANE_TILT_POSITION, 30);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 50);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), 30);

        // front shade 100% down, back shade 100% down, tilt 70%
        test = new ShadePosition().setPosition(capabilities, SECONDARY_POSITION, 100).setPosition(capabilities,
                VANE_TILT_POSITION, 70);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), 70);
    }
}
