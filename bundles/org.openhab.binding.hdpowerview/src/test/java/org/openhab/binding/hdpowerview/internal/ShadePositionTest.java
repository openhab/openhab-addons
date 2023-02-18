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
package org.openhab.binding.hdpowerview.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.hdpowerview.internal.dto.CoordinateSystem.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase.Capabilities;
import org.openhab.binding.hdpowerview.internal.dto.ShadePosition;
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
        assertTrue(db.isCapabilitiesInDatabase(10));

        assertTrue(db.getCapabilities(0).supportsPrimary());
        assertTrue(db.getCapabilities(1).supportsTiltOnClosed());
        assertTrue(db.getCapabilities(2).supportsTilt180());
        assertTrue(db.getCapabilities(2).supportsTiltAnywhere());
        assertTrue(db.getCapabilities(4).supportsTilt180());
        assertTrue(db.getCapabilities(4).supportsTiltAnywhere());
        assertTrue(db.getCapabilities(5).supportsTilt180());
        assertFalse(db.getCapabilities(5).supportsPrimary());
        assertTrue(db.getCapabilities(6).isPrimaryInverted());
        assertTrue(db.getCapabilities(7).supportsSecondary());
        assertTrue(db.getCapabilities(8).supportsSecondaryOverlapped());
        assertTrue(db.getCapabilities(9).supportsSecondaryOverlapped());
        assertTrue(db.getCapabilities(9).supportsTiltOnClosed());

        assertEquals(db.getType(4).getCapabilities(), 0);
        assertEquals(db.getType(-1).getCapabilities(), -1);

        assertFalse(db.isTypeInDatabase(99));
        assertFalse(db.isCapabilitiesInDatabase(99));

        assertFalse(db.getCapabilities(0).isPrimaryInverted());
        assertFalse(db.getCapabilities(-1).isPrimaryInverted());
        assertFalse(db.getCapabilities(99).isPrimaryInverted());

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
     * Test parsing of Capabilities 1 ShadePosition (shade fully up).
     *
     */
    @Test
    public void testCaps1ShadePositionParsingFullyUp() {
        Capabilities capabilities = db.getCapabilities(1);
        ShadePosition test = new ShadePosition().setPosition(capabilities, PRIMARY_POSITION, 0);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 0);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), UnDefType.UNDEF);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), UnDefType.UNDEF);
    }

    /**
     * Test parsing of Capabilities 1 ShadePosition (shade fully down (method 1)).
     *
     */
    @Test
    public void testCaps1ShadePositionParsingShadeFullyDown1() {
        Capabilities capabilities = db.getCapabilities(1);
        ShadePosition test = new ShadePosition().setPosition(capabilities, PRIMARY_POSITION, 100);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), UnDefType.UNDEF);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), 0);
    }

    /**
     * Test parsing of Capabilities 1 ShadePosition (shade fully down (method 2)).
     *
     */
    @Test
    public void testCaps1ShadePositionParsingShadeFullyDown2() {
        Capabilities capabilities = db.getCapabilities(1);
        ShadePosition test = new ShadePosition().setPosition(capabilities, VANE_TILT_POSITION, 0);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), UnDefType.UNDEF);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), 0);
    }

    /**
     * Test parsing of Capabilities 1 ShadePosition (shade fully down (method 2) and vane fully open).
     *
     */
    @Test
    public void testCaps1ShadePositionParsingShadeFullyDownVaneOpen() {
        Capabilities capabilities = db.getCapabilities(1);
        ShadePosition test = new ShadePosition().setPosition(capabilities, VANE_TILT_POSITION, 88);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), UnDefType.UNDEF);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), 88);
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
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), UnDefType.UNDEF);

        // ==== OK !! primary at middle, secondary at top ====
        test.setPosition(capabilities, PRIMARY_POSITION, 50).setPosition(capabilities, SECONDARY_POSITION, 0);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 50);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 0);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), UnDefType.UNDEF);

        // ==== OK !! primary at middle, secondary at middle ====
        test.setPosition(capabilities, PRIMARY_POSITION, 50).setPosition(capabilities, SECONDARY_POSITION, 50);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 50);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 50);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), UnDefType.UNDEF);

        // ==== IMPOSSIBLE !! secondary at middle, primary above => test the constraining code ====
        test.setPosition(capabilities, SECONDARY_POSITION, 0).setPosition(capabilities, PRIMARY_POSITION, 100);
        test.setPosition(capabilities, SECONDARY_POSITION, 40).setPosition(capabilities, PRIMARY_POSITION, 25);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 40);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 40);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), UnDefType.UNDEF);

        // ==== OK !! secondary at middle, primary below ====
        test.setPosition(capabilities, SECONDARY_POSITION, 0).setPosition(capabilities, PRIMARY_POSITION, 100);
        test.setPosition(capabilities, SECONDARY_POSITION, 50).setPosition(capabilities, PRIMARY_POSITION, 75);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 75);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 50);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), UnDefType.UNDEF);

        // ==== IMPOSSIBLE !! primary at middle, secondary below => test the constraining code ====
        test.setPosition(capabilities, SECONDARY_POSITION, 0).setPosition(capabilities, PRIMARY_POSITION, 100);
        test.setPosition(capabilities, PRIMARY_POSITION, 60).setPosition(capabilities, SECONDARY_POSITION, 75);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 60);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 60);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), UnDefType.UNDEF);

        // ==== OK !! primary at middle, secondary above ====
        test.setPosition(capabilities, SECONDARY_POSITION, 0).setPosition(capabilities, PRIMARY_POSITION, 100);
        test.setPosition(capabilities, PRIMARY_POSITION, 60).setPosition(capabilities, SECONDARY_POSITION, 25);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 60);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 25);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), UnDefType.UNDEF);
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
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), UnDefType.UNDEF);

        // front shade 50% down
        test = new ShadePosition().setPosition(capabilities, PRIMARY_POSITION, 50);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 50);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 0);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), UnDefType.UNDEF);

        // front shade 100% down, back shade 0% down
        test = new ShadePosition().setPosition(capabilities, PRIMARY_POSITION, 100);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 0);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), UnDefType.UNDEF);

        // front shade 100% down, back shade 0% down (ALTERNATE)
        test = new ShadePosition().setPosition(capabilities, SECONDARY_POSITION, 0);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 0);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), UnDefType.UNDEF);

        // front shade 100% down, back shade 50% down
        test = new ShadePosition().setPosition(capabilities, SECONDARY_POSITION, 50);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 50);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), UnDefType.UNDEF);

        // front shade 100% down, back shade 100% down
        test = new ShadePosition().setPosition(capabilities, SECONDARY_POSITION, 100);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), UnDefType.UNDEF);
    }

    /**
     * Test parsing of DuoLite shades having both a secondary blackout shade, and tilt functionality.
     *
     */
    @Test
    public void testDuoliteTiltShadePositionParsing() {
        // blackout shades with tilt have capabilities 9
        Capabilities capabilities = db.getCapabilities(9);
        ShadePosition test;

        // front shade up
        test = new ShadePosition().setPosition(capabilities, PRIMARY_POSITION, 0);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 0);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 0);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), UnDefType.UNDEF);

        // front shade 30% down
        test = new ShadePosition().setPosition(capabilities, PRIMARY_POSITION, 30);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 30);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 0);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), UnDefType.UNDEF);

        // front shade 100% down
        test = new ShadePosition().setPosition(capabilities, PRIMARY_POSITION, 100);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 0);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), 0);

        // tilt 0%
        test = new ShadePosition().setPosition(capabilities, VANE_TILT_POSITION, 0);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 0);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), 0);

        // tilt 30%
        test = new ShadePosition().setPosition(capabilities, VANE_TILT_POSITION, 30);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 0);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), 30);

        // tilt 100%
        test = new ShadePosition().setPosition(capabilities, VANE_TILT_POSITION, 100);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 0);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), 100);

        // back shade 0% down
        test = new ShadePosition().setPosition(capabilities, SECONDARY_POSITION, 0);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 0);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), 100);

        // back shade 30% down
        test = new ShadePosition().setPosition(capabilities, SECONDARY_POSITION, 30);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 30);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), 100);

        // back shade 100% down
        test = new ShadePosition().setPosition(capabilities, SECONDARY_POSITION, 100);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), 100);

        // test constraints on impossible values: primary 30% => tilt 30%
        test = new ShadePosition().setPosition(capabilities, PRIMARY_POSITION, 30).setPosition(capabilities,
                VANE_TILT_POSITION, 30);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 0);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), 30);

        // test constraints on impossible values: primary 30% => tilt 30% => back shade 30% down
        test = new ShadePosition().setPosition(capabilities, PRIMARY_POSITION, 30)
                .setPosition(capabilities, VANE_TILT_POSITION, 30).setPosition(capabilities, SECONDARY_POSITION, 30);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), 30);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), 100);
    }

    /**
     * Test parsing of Capabilities 0 ShadePosition (shade fully up).
     *
     */
    @Test
    public void testCaps0ShadePositionParsingFullyUp() {
        Capabilities capabilities = db.getCapabilities(0);
        ShadePosition test = new ShadePosition().setPosition(capabilities, PRIMARY_POSITION, 0);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 0);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), UnDefType.UNDEF);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), UnDefType.UNDEF);
    }

    /**
     * Test parsing of Capabilities 0 ShadePosition (shade fully down).
     *
     */
    @Test
    public void testCap0ShadePositionParsingShadeFullyDown() {
        Capabilities capabilities = db.getCapabilities(0);
        ShadePosition test = new ShadePosition().setPosition(capabilities, PRIMARY_POSITION, 100);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), UnDefType.UNDEF);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), UnDefType.UNDEF);
    }

    /**
     * Helper method; test if shade State is correct.
     *
     * @param actual the shade State
     * @param target the test value to compare with
     */
    private void assertShadePosition(State actual, State target) {
        assertTrue(target.equals(actual));
    }

    /**
     * Test parsing of Type 44 ShadePosition (shade fully up).
     *
     */
    @Test
    public void testType44ShadePositionParsingFullyUp() {
        Capabilities capabilities = db.getCapabilities(44, null);
        ShadePosition test = new ShadePosition().setPosition(capabilities, PRIMARY_POSITION, 0);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 0);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), UnDefType.UNDEF);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), UnDefType.UNDEF);
    }

    /**
     * Test parsing of Type 44 ShadePosition (shade fully down (method 2) and vane fully open).
     *
     */
    @Test
    public void testType44ShadePositionParsingShadeFullyDownVaneOpen() {
        Capabilities capabilities = db.getCapabilities(44, null);
        ShadePosition test = new ShadePosition().setPosition(capabilities, VANE_TILT_POSITION, 88);
        assertNotNull(test);
        assertShadePosition(test.getState(capabilities, PRIMARY_POSITION), 100);
        assertShadePosition(test.getState(capabilities, SECONDARY_POSITION), UnDefType.UNDEF);
        assertShadePosition(test.getState(capabilities, VANE_TILT_POSITION), 88);
    }

    /**
     * Test the getCapabilities functionality.
     */
    @Test
    public void testGetCapabilities() {
        Capabilities caps;
        /*
         * - type not in database
         * - null external capabilities
         * => return default (0)
         */
        caps = db.getCapabilities(0, null);
        assertEquals(0, caps.getValue());
        /*
         * - type not in database
         * - valid external capabilities (1)
         * => return external capabilities (1)
         */
        caps = db.getCapabilities(0, 1);
        assertEquals(1, caps.getValue());
        /*
         * - type not in database
         * - external capabilities not in database (99)
         * => return default (0)
         */
        caps = db.getCapabilities(0, 99);
        assertEquals(0, caps.getValue());
        /*
         * - type 62 in database
         * - inherent capabilities (2)
         * - null external capabilities
         * => return inherent capabilities (2)
         */
        caps = db.getCapabilities(62, null);
        assertEquals(2, caps.getValue());
        /*
         * - type 62 in database
         * - inherent capabilities (2)
         * - non matching external capabilities (1)
         * => return external capabilities (1)
         */
        caps = db.getCapabilities(62, 1);
        assertEquals(1, caps.getValue());
        /*
         * - type 44 in database
         * - inherent capabilities (0)
         * - with capabilitiesOverride (1)
         * - non matching external capabilities (2)
         * => return capabilitiesOverride (1)
         */
        caps = db.getCapabilities(44, 2);
        assertEquals(1, caps.getValue());
    }
}
