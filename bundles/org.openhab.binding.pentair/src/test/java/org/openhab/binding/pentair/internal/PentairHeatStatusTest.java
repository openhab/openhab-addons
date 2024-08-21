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
package org.openhab.binding.pentair.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openhab.binding.pentair.internal.TestUtilities.parsehex;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.pentair.internal.handler.helpers.PentairHeatStatus;
import org.openhab.binding.pentair.internal.parser.PentairStandardPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PentairHeatStatusTest}
 *
 * @author Jeff James - Initial contribution
 */

@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class PentairHeatStatusTest {
    private final Logger logger = LoggerFactory.getLogger(PentairHeatStatusTest.class);

    //@formatter:off
    public static byte[][] packets = {
            parsehex("A5 01 0F 10 08 0D 4B 4B 4D 55 5E 07 00 00 58 00 00 00")
    };
    //@formatter:on

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
    }

    @BeforeEach
    void setUp() throws Exception {
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void test() {
        PentairHeatStatus hs = new PentairHeatStatus();

        PentairStandardPacket p = new PentairStandardPacket(packets[0], packets[0].length);
        hs.parsePacket(p);

        assertThat(hs.poolSetPoint, equalTo(85));
        assertThat(hs.poolHeatMode, equalTo(PentairHeatStatus.HeatMode.SOLAR));
        assertThat(hs.spaSetPoint, equalTo(94));
        assertThat(hs.spaHeatMode, equalTo(PentairHeatStatus.HeatMode.HEATER));
    }
}
