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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openhab.binding.pentair.internal.TestUtilities.parsehex;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.pentair.internal.handler.helpers.PentairControllerSchedule;
import org.openhab.binding.pentair.internal.parser.PentairStandardPacket;

/**
 * PentairControllerSchduleTest
 *
 * @author Jeff James - Initial contribution
 *
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class PentairControllerScheduleTest {

    //@formatter:off
    public static byte[][] packets = {
            parsehex("A5 1E 0F 10 11 07 01 06 0A 00 10 00 7F"),
            parsehex("A5 1E 0F 10 11 07 02 05 0A 00 0B 00 7F"),
            parsehex("A5 1E 0F 10 11 07 03 07 08 00 1A 00 08"),
            parsehex("A5 1E 0F 10 11 07 04 09 19 00 02 15 0F")
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
    public void parseTest() {
        PentairControllerSchedule pcs = new PentairControllerSchedule();

        PentairStandardPacket p = new PentairStandardPacket(packets[0]);

        pcs.parsePacket(p);
        assertThat(pcs.circuit, equalTo(6));
        assertThat(pcs.start, equalTo(10 * 60));
        assertThat(pcs.end, equalTo(16 * 60));
        assertThat(pcs.days, equalTo(0x7F));
        assertThat(pcs.type, equalTo(PentairControllerSchedule.ScheduleType.NORMAL));
        assertThat(pcs.id, equalTo(1));

        PentairStandardPacket p2 = new PentairStandardPacket(packets[1]);
        pcs.parsePacket(p2);
        assertThat(pcs.circuit, equalTo(5));
        assertThat(pcs.start, equalTo(10 * 60));
        assertThat(pcs.end, equalTo(11 * 60));
        assertThat(pcs.days, equalTo(0x7F));
        assertThat(pcs.type, equalTo(PentairControllerSchedule.ScheduleType.NORMAL));
        assertThat(pcs.id, equalTo(2));

        PentairStandardPacket p3 = new PentairStandardPacket(packets[2]);
        pcs.parsePacket(p3);
        assertThat(pcs.circuit, equalTo(7));
        assertThat(pcs.start, equalTo(8 * 60));
        assertThat(pcs.days, equalTo(0x08));
        assertThat(pcs.type, equalTo(PentairControllerSchedule.ScheduleType.ONCEONLY));
        assertThat(pcs.id, equalTo(3));

        PentairStandardPacket p4 = new PentairStandardPacket(packets[3]);
        pcs.parsePacket(p4);
        assertThat(pcs.circuit, equalTo(9));
        assertThat(pcs.end, equalTo(0x02 * 60 + 0x15));
        assertThat(pcs.days, equalTo(0x0F));
        assertThat(pcs.type, equalTo(PentairControllerSchedule.ScheduleType.EGGTIMER));
        assertThat(pcs.id, equalTo(4));
    }

    @Test
    public void setTest() {
        PentairControllerSchedule pcs = new PentairControllerSchedule();

        pcs.id = 1;
        pcs.circuit = 4;
        pcs.start = 5 * 60 + 15; // 5:15
        pcs.end = 10 * 60 + 30; // 10:30
        pcs.type = PentairControllerSchedule.ScheduleType.NORMAL;
        pcs.days = 0x07;

        PentairStandardPacket p = Objects.requireNonNull(pcs.getWritePacket(0x10, 0x00));

        assertThat(p.buf, is(parsehex("A5 00 10 00 91 07 01 04 05 0F 0A 1E 07")));
    }
}
