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
import org.openhab.binding.pentair.internal.handler.helpers.PentairControllerStatus;
import org.openhab.binding.pentair.internal.parser.PentairStandardPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PentairControllerStatusTest}
 *
 * @author Jeff James - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class PentairControllerStatusTest {
    private final Logger logger = LoggerFactory.getLogger(PentairControllerStatusTest.class);

    //@formatter:off
    public static byte[][] packets = {
            parsehex("A5 1E 0F 10 02 1D 09 20 21 00 00 00 00 00 00 20 0F 00 00 04 3F 3F 00 00 41 3C 00 00 07 00 00 6A B6 00 0D"),
            parsehex("A5 24 0f 10 02 1d 08 3b 00 01 00 00 00 00 00 20 00 00 00 04 4a 4a 00 00 44 00 00 00 04 00 00 7c e6 00 0d 03 ba"),
            parsehex("a5 24 0f 10 02 1d 09 04 00 31 00 00 00 00 00 20 00 00 00 04 4a 4a 00 00 45 00 00 00 04 00 07 ce 60 00 0d 03 85"),
            parsehex("A5 1E 0F 10 02 1D 0A 0B 00 00 00 00 00 00 00 21 33 00 00 04 45 45 00 00 3F 3F 00 00 07 00 00 D9 89 00 0D")
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
        PentairControllerStatus pcs = new PentairControllerStatus();

        PentairStandardPacket p = new PentairStandardPacket(packets[0], packets[0].length);
        pcs.parsePacket(p);
        logger.debug(pcs.toString());

        assertThat(pcs.circuits[0], equalTo(true));
        assertThat(pcs.circuits[5], equalTo(true));
        assertThat(pcs.pool, equalTo(true));
        assertThat(pcs.poolTemp, equalTo(63));
        assertThat(pcs.spaTemp, equalTo(63));
        assertThat(pcs.airTemp, equalTo(65));
        assertThat(pcs.solarTemp, equalTo(60));

        p = new PentairStandardPacket(packets[1], packets[1].length);
        pcs.parsePacket(p);
        logger.debug(pcs.toString());

        assertThat(pcs.circuits[8], equalTo(true));
        assertThat(pcs.pool, equalTo(false));
        assertThat(pcs.poolTemp, equalTo(74));
        assertThat(pcs.spaTemp, equalTo(74));
        assertThat(pcs.airTemp, equalTo(68));
        assertThat(pcs.solarTemp, equalTo(0));

        p = new PentairStandardPacket(packets[2], packets[2].length);
        pcs.parsePacket(p);
        logger.debug(pcs.toString());

        assertThat(pcs.circuits[8], equalTo(true));
        assertThat(pcs.circuits[12], equalTo(true));
        assertThat(pcs.circuits[13], equalTo(true));
        assertThat(pcs.pool, equalTo(false));
        assertThat(pcs.poolTemp, equalTo(74));
        assertThat(pcs.spaTemp, equalTo(74));
        assertThat(pcs.airTemp, equalTo(69));
        assertThat(pcs.solarTemp, equalTo(0));

        p = new PentairStandardPacket(packets[3], packets[3].length);
        pcs.parsePacket(p);
        logger.debug(pcs.toString());
        assertThat(pcs.equip, equalTo(0));
        assertThat(pcs.pool, equalTo(false));
        assertThat(pcs.poolTemp, equalTo(69));
        assertThat(pcs.spaTemp, equalTo(69));
        assertThat(pcs.airTemp, equalTo(63));
        assertThat(pcs.solarTemp, equalTo(63));
        assertThat(pcs.serviceMode, equalTo(true));
    }
}
