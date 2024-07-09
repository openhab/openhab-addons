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
import org.openhab.binding.pentair.internal.handler.helpers.PentairPumpStatus;
import org.openhab.binding.pentair.internal.parser.PentairStandardPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PentairPumpStatusTest}
 *
 * @author Jeff James - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class PentairPumpStatusTest {
    private final Logger logger = LoggerFactory.getLogger(PentairPumpStatus.class);

    //@formatter:off
    public static byte[][] packets = {
            parsehex("A5 00 22 60 07 0F 0A 02 02 00 E7 06 D6 00 00 00 00 00 01 02 03"),
            parsehex("A5 00 22 60 07 0F 0A 00 00 01 F9 07 D5 00 00 00 00 09 21 0A 3A"),          // SVRS alarm
            parsehex("a5 00 10 60 07 0f 0a 02 02 00 5a 02 ee 00 00 00 00 00 01 15 1f"),
            parsehex("A5 00 10 60 07 0F 04 00 00 00 00 00 00 00 00 00 00 00 00 14 1E")
    };
    //@formatter:on

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterAll
    public static void tearDownAfterClass() throws Exception {
    }

    @BeforeEach
    public void setUp() throws Exception {
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    public void test() {
        PentairPumpStatus ps = new PentairPumpStatus();

        PentairStandardPacket p = new PentairStandardPacket(packets[0], packets[0].length);
        ps.parsePacket(p);
        logger.debug(ps.toString());

        assertThat(ps.run, equalTo(true));
        assertThat(ps.mode, equalTo(2));
        assertThat(ps.power, equalTo(231));
        assertThat(ps.rpm, equalTo(1750));

        p = new PentairStandardPacket(packets[1], packets[1].length);
        ps.parsePacket(p);
        logger.debug(ps.toString());
        assertThat(ps.run, equalTo(true));
        assertThat(ps.mode, equalTo(0));
        assertThat(ps.power, equalTo(505));
        assertThat(ps.rpm, equalTo(2005));

        p = new PentairStandardPacket(packets[2], packets[2].length);
        ps.parsePacket(p);
        logger.debug(ps.toString());

        p = new PentairStandardPacket(packets[3], packets[3].length);
        ps.parsePacket(p);
        logger.debug(ps.toString());
        assertThat(ps.run, equalTo(false));
        assertThat(ps.mode, equalTo(0));
        assertThat(ps.power, equalTo(0));
        assertThat(ps.rpm, equalTo(0));
    }
}
