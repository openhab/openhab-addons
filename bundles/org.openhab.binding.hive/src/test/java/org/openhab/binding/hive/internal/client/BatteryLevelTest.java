/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public class BatteryLevelTest {
    private static final int BATTERY_FULL = 100;
    private static final int BATTERY_NORMAL = 56;
    private static final int BATTERY_EMPTY = 0;

    @Test(expected = IllegalArgumentException.class)
    public void testTooBigBatteryLevel() {
        new BatteryLevel(102);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooNegativeBatteryLevel() {
        new BatteryLevel(-2);
    }

    @Test
    public void testFullBatteryLevel() {
        /* When */
        final BatteryLevel batteryLevel = new BatteryLevel(BATTERY_FULL);


        /* Then */
        assertThat(batteryLevel.intValue()).isEqualTo(BATTERY_FULL);
        assertThat(batteryLevel.toString()).isEqualTo("100%");
    }

    @Test
    public void testNormalBatteryLevel() {
        /* When */
        final BatteryLevel batteryLevel = new BatteryLevel(BATTERY_NORMAL);


        /* Then */
        assertThat(batteryLevel.intValue()).isEqualTo(BATTERY_NORMAL);
        assertThat(batteryLevel.toString()).isEqualTo("56%");
    }

    @Test
    public void testEmptyBatteryLevel() {
        /* When */
        final BatteryLevel batteryLevel = new BatteryLevel(BATTERY_EMPTY);


        /* Then */
        assertThat(batteryLevel.intValue()).isEqualTo(BATTERY_EMPTY);
        assertThat(batteryLevel.toString()).isEqualTo("0%");
    }

    @Test
    public void testEqualsContract() {
        EqualsVerifier.forClass(BatteryLevel.class)
                .verify();
    }
}
