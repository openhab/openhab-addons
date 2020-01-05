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
package org.openhab.binding.loxone.internal.controls;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link LxControlIRoomController}
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
@NonNullByDefault
public class LxControlIRCDayTimerTest extends LxControlTest {
    private static final String CURRENT_TEMP_IDX_CHANNEL = " / Current Temperature";
    private static final String MODE_CHANNEL = " / Mode";

    private static final String MODE_LIST_1 = "0:mode=-3;name=\\\"Haus im Tiefschlaf\\\","
            + "1:mode=-4;name=\\\"Erhöhter Wärmebedarf\\\"," + "2:mode=-5;name=\\\"Party\\\","
            + "3:mode=3;name=\\\"Montag\\\"," + "4:mode=4;name=\\\"Dienstag\\\"," + "5:mode=5;name=\\\"Mittwoch\\\","
            + "6:mode=6;name=\\\"Donnerstag\\\"," + "7:mode=7;name=\\\"Freitag\\\"," + "8:mode=8;name=\\\"Samstag\\\","
            + "9:mode=9;name=\\\"Sonntag\\\"";
    private static final String MODE_LIST_2 = "0:mode=1;name=\\\"Mode 1\\\"," + "1:mode=2;name=\\\"Mode 2\\\","
            + "2:mode=3;name=\\\"Mode 3\\\"";

    @Before
    public void setup() {
        setupControl("147e7abc-01df-0ac5-ffffd2fc4ed5fdc3", "0b734138-037d-034e-ffff403fb0c34b9e",
                "0fb99a98-02df-46f1-ffff403fb0c34b9e", "Intelligent Room Controller V1 / Heating");
    }

    @Test
    public void testSubControlCreation() {
        testSubControlCreation(LxControlIRCDayTimer.class, 2, 0, 2, 2, 4);
    }

    @Test
    public void testChannels() {
        testChannel("String", CURRENT_TEMP_IDX_CHANNEL);
        testChannel("String", MODE_CHANNEL);
    }

    @Test
    public void testCurrentTempIdxChannel() {
        for (Double i = 0.0; i <= 7.0; i += 1.0) {
            changeLoxoneState("value", i);
            testChannelState(CURRENT_TEMP_IDX_CHANNEL,
                    new StringType(LxControlIRoomControllerTest.getTemperatureLabel(i.intValue())));
        }
        changeLoxoneState("value", -1.0);
        testChannelState(CURRENT_TEMP_IDX_CHANNEL, new StringType("Unknown"));
        changeLoxoneState("value", 8.0);
        testChannelState(CURRENT_TEMP_IDX_CHANNEL, new StringType("Unknown"));
    }

    @Test
    public void testModeStateChanges() {
        for (int i = 0; i < 3; i++) {
            changeLoxoneState("modelist", MODE_LIST_1);
            testChannelState(MODE_CHANNEL, UnDefType.UNDEF);
            changeLoxoneState("mode", -3.0);
            testChannelState(MODE_CHANNEL, new StringType("Haus im Tiefschlaf"));
            changeLoxoneState("mode", -4.0);
            testChannelState(MODE_CHANNEL, new StringType("Erhöhter Wärmebedarf"));
            changeLoxoneState("mode", -5.0);
            testChannelState(MODE_CHANNEL, new StringType("Party"));
            changeLoxoneState("mode", 3.0);
            testChannelState(MODE_CHANNEL, new StringType("Montag"));
            changeLoxoneState("mode", 4.0);
            testChannelState(MODE_CHANNEL, new StringType("Dienstag"));
            changeLoxoneState("mode", 5.0);
            testChannelState(MODE_CHANNEL, new StringType("Mittwoch"));
            changeLoxoneState("mode", 6.0);
            testChannelState(MODE_CHANNEL, new StringType("Donnerstag"));
            changeLoxoneState("mode", 7.0);
            testChannelState(MODE_CHANNEL, new StringType("Freitag"));
            changeLoxoneState("mode", 8.0);
            testChannelState(MODE_CHANNEL, new StringType("Samstag"));
            changeLoxoneState("mode", 9.0);
            testChannelState(MODE_CHANNEL, new StringType("Sonntag"));
            changeLoxoneState("mode", 10.0);
            testChannelState(MODE_CHANNEL, UnDefType.UNDEF);
            changeLoxoneState("mode", 9.0);
            testChannelState(MODE_CHANNEL, new StringType("Sonntag"));

            changeLoxoneState("modelist", MODE_LIST_2);
            changeLoxoneState("mode", -5.0);
            testChannelState(MODE_CHANNEL, UnDefType.UNDEF);
            changeLoxoneState("mode", -4.0);
            testChannelState(MODE_CHANNEL, UnDefType.UNDEF);
            changeLoxoneState("mode", -3.0);
            testChannelState(MODE_CHANNEL, UnDefType.UNDEF);
            changeLoxoneState("mode", 0.0);
            testChannelState(MODE_CHANNEL, UnDefType.UNDEF);
            changeLoxoneState("mode", 1.0);
            testChannelState(MODE_CHANNEL, new StringType("Mode 1"));
            changeLoxoneState("mode", 2.0);
            testChannelState(MODE_CHANNEL, new StringType("Mode 2"));
            changeLoxoneState("mode", 3.0);
            testChannelState(MODE_CHANNEL, new StringType("Mode 3"));
            changeLoxoneState("mode", 4.0);
            testChannelState(MODE_CHANNEL, UnDefType.UNDEF);
            changeLoxoneState("mode", 5.0);
            testChannelState(MODE_CHANNEL, UnDefType.UNDEF);
            changeLoxoneState("mode", 6.0);
            testChannelState(MODE_CHANNEL, UnDefType.UNDEF);
            changeLoxoneState("mode", 7.0);
            testChannelState(MODE_CHANNEL, UnDefType.UNDEF);
            changeLoxoneState("mode", 8.0);
            testChannelState(MODE_CHANNEL, UnDefType.UNDEF);
            changeLoxoneState("mode", 9.0);
            testChannelState(MODE_CHANNEL, UnDefType.UNDEF);
            changeLoxoneState("mode", 10.0);
            testChannelState(MODE_CHANNEL, UnDefType.UNDEF);
        }
        changeLoxoneState("modelist", "");
        changeLoxoneState("mode", 1.0);
        testChannelState(MODE_CHANNEL, UnDefType.UNDEF);
        changeLoxoneState("mode", 2.0);
        testChannelState(MODE_CHANNEL, UnDefType.UNDEF);
        changeLoxoneState("mode", 3.0);
        testChannelState(MODE_CHANNEL, UnDefType.UNDEF);
    }
}
