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
package org.openhab.binding.mercedesme;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.utils.UOMObserver;
import org.openhab.binding.mercedesme.internal.utils.Utils;
import org.openhab.core.library.unit.ImperialUnits;

/**
 * {@link UtilsTest} for helper functions
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class UtilsTest {

    @Test
    void testRound() {
        int socValue = 66;
        double batteryCapacity = 66.5;
        float chargedValue = Math.round(socValue * 1000 * (float) batteryCapacity / 1000) / (float) 100;
        assertEquals(43.89, chargedValue, 0.01);
        float unchargedValue = Math.round((100 - socValue) * 1000 * (float) batteryCapacity / 1000) / (float) 100;
        assertEquals(22.61, unchargedValue, 0.01);
        assertEquals(batteryCapacity, chargedValue + unchargedValue, 0.01);
    }

    @Test
    public void testDuration() {
        assertEquals("-1", Utils.getDurationString(-1), "negative");
        assertEquals("59m", Utils.getDurationString(59), "only minutes");
        assertEquals("2h 32m", Utils.getDurationString(2 * 60 + 32), "below one day");
        assertEquals("1d 0h 0m", Utils.getDurationString(24 * 60), "exact one day");
        assertEquals("1d 0h 14m", Utils.getDurationString(24 * 60 + 14), " one day no hour");
        assertEquals("11d 2h 40m", Utils.getDurationString(16000), "> eleven days");
    }

    @Test
    public void testPattern() {
        UOMObserver obersver = new UOMObserver(UOMObserver.LENGTH_KM_UNIT);
        assertEquals("%.0f km", obersver.getPattern(Constants.GROUP_RANGE, ""), "Range km as int");
        assertEquals("%.1f km", obersver.getPattern(Constants.GROUP_TRIP, ""), "Trip km with one decimal");
        assertEquals(Constants.KILOMETRE_UNIT, obersver.getUnit(), "Unit km");

        obersver = new UOMObserver(UOMObserver.LENGTH_MILES_UNIT);
        assertEquals("%.0f mi", obersver.getPattern(Constants.GROUP_RANGE, ""), "Range mi as int");
        assertEquals("%.1f mi", obersver.getPattern(Constants.GROUP_TRIP, ""), "Trip mi with one decimal");
        assertEquals(ImperialUnits.MILE, obersver.getUnit(), "Unit mi");
    }

    @Test
    public void testHVACZoneValues() {
        assertEquals(1, Utils.getZoneNumber("frontLeft"), "Front Left");
        assertEquals(2, Utils.getZoneNumber("frontRight"), "Front Right");
        assertEquals(3, Utils.getZoneNumber("frontCenter"), "Front Center");
        assertEquals(4, Utils.getZoneNumber("rearLeft"), "Rear Left");
        assertEquals(6, Utils.getZoneNumber("rearCenter"), "Rear Center");
        assertEquals(0, Utils.getZoneNumber("unknown"), "Unknown");
    }

    @Test
    public void testChargeProgramValues() {
        assertEquals(0, Utils.getChargeProgramNumber("DEFAULT_CHARGE_PROGRAM"), "Default");
        assertEquals(2, Utils.getChargeProgramNumber("HOME_CHARGE_PROGRAM"), "Home");
        assertEquals(3, Utils.getChargeProgramNumber("WORK_CHARGE_PROGRAM"), "Work");
        assertEquals(-1, Utils.getChargeProgramNumber("UNKNOWN"), "Unknown");
    }

    @Test
    public void testChannelNameLength() {
        File configDir = new File("src/main/resources/OH-INF/thing");
        File[] configFiles = configDir.listFiles();
        // ensure channel name <= 20
        for (int i = 0; i < configFiles.length; i++) {
            if (configFiles[i].getName().endsWith("group.xml")) {
                String content = FileReader.readFileInString(configFiles[i].toString());
                String[] splits = content.split("<channel id=");
                for (int j = 1; j < splits.length; j++) {
                    String[] furtherSplit = splits[j].split("\"");
                    assertTrue(furtherSplit[1].length() <= 20);
                }
            }
        }
    }

    @Test
    public void testChannelLabelLength() {
        File configDir = new File("src/main/resources/OH-INF/thing");
        File[] configFiles = configDir.listFiles();
        // ensure channel labels <= 25
        for (int i = 0; i < configFiles.length; i++) {
            if (configFiles[i].getName().endsWith("channel-types.xml")) {
                String content = FileReader.readFileInString(configFiles[i].toString());
                String[] splits = content.split("<label>");
                for (int j = 1; j < splits.length; j++) {
                    String[] furtherSplit = splits[j].split("</label>");
                    assertTrue(furtherSplit[0].length() <= 25, "Length violation " + furtherSplit[0]);
                }
            }
        }
    }
}
