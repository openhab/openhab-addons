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
package org.openhab.binding.wifiled.internal.handler;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Test for LEDStateDTO
 *
 * @author Ries van Twisk - Prevent flashes during classic driver color + white updates
 */
public class LEDStateDTOTest {

    @Test
    public void RGBTest() {
        for (int r = 0; r < 256 - 3; r = r + 3) {
            for (int g = 0; g < 256 - 5; g = g + 5) {
                for (int b = 0; b < 256 - 7; b = b + 7) {
                    LEDStateDTO ledState = LEDStateDTO.valueOf(0, 0, 0, r, g, b, 0, 0);
                    assertThat(ledState.getRGB() & 0xffffff, is(r << 16 | g << 8 | b));
                }
            }
        }
    }

    @Test
    public void RGBTest1() {
        LEDStateDTO ledState = LEDStateDTO.valueOf(0, 0, 0, 0xff, 0xff, 0xff, 0, 0);
        assertThat(ledState.getRGB() & 0xffffff, is(0xffffff));
    }

    @Test
    public void programSpeedtest() {
        for (int ps = 0; ps < 0x20; ps++) {
            LEDStateDTO ledState = LEDStateDTO.valueOf(0, 0, ps, 0, 0, 0, 0, 0);
            assertThat(ledState.getProgramSpeed().intValue(), is((int) Math.round(100.0 - (100.0 / 0x1f * ps))));
        }
    }

    @Test
    public void whiteTest() {
        for (int wt = 0; wt < 256; wt++) {
            LEDStateDTO ledState = LEDStateDTO.valueOf(0, 0, 0, 0, 0, 0, wt, wt);
            assertThat((int) Math.round(ledState.getWhite().doubleValue() * 2.55), is(wt));
            assertThat(255 - (int) Math.round(ledState.getWhite2().doubleValue() * 2.55), is(255 - wt));
        }
    }

    @Test
    public void programTest() {
        for (int p = 0; p < 256; p++) {
            LEDStateDTO ledState = LEDStateDTO.valueOf(0, p, 0, 0, 0, 0, 0, 0);
            assertThat(ledState.getProgram().toString(), is(String.valueOf(p)));
        }
    }
}
