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
import static org.mockito.Mockito.*;
import static org.openhab.binding.pentair.internal.TestUtilities.parsehex;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.pentair.internal.parser.PentairIntelliChlorPacket;
import org.openhab.binding.pentair.internal.parser.PentairParser;
import org.openhab.binding.pentair.internal.parser.PentairParser.CallbackPentairParser;
import org.openhab.binding.pentair.internal.parser.PentairStandardPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PentairParserTest
 *
 * @author Jeff James - Initial contribution
 *
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class PentairParserTest {
    private final Logger logger = LoggerFactory.getLogger(PentairParserTest.class);

    //@formatter:off
    public static byte[] stream = parsehex(
            "FF 00 FF A5 1E 0F 10 02 1D 09 1F 00 00 00 00 00 00 00 20 03 00 00 04 3F 3F 00 00 41 3C 00 00 07 00 00 6A B6 00 0D 03 7F"
                    + "FF 00 FF A5 10 0F 10 12 29 02 E3 02 AF 02 EE 02 BC 00 00 00 02 00 00 00 2A 00 04 00 5C 06 05 18 01 90 00 00 00 96 14 00 51 00 00 65 20 3C 01 00 00 00 07 50 "
                    + "FF 00 FF A5 01 0F 10 02 1D 0D 1D 20 00 00 00 00 00 00 00 33 00 00 04 4D 4D 00 00 51 6D 00 00 07 00 00 5E D5 00 0D 04 04");
    //@formatter:on

    PentairParser parser = new PentairParser();

    @Mock
    @NonNullByDefault({})
    CallbackPentairParser callback;

    @Captor
    @NonNullByDefault({})
    ArgumentCaptor<PentairStandardPacket> packetsStandard;

    @Captor
    @NonNullByDefault({})
    ArgumentCaptor<PentairIntelliChlorPacket> packetsIntellichlor;

    @NonNullByDefault({})
    Thread thread;

    @BeforeEach
    public void setUp() throws Exception {
        parser.setCallback(callback);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (thread != null) {
            thread.interrupt();
            thread.join();
        }
        thread = null;
    }

    @Test
    public void test() throws InterruptedException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(stream, 0, stream.length);

        parser.setInputStream(inputStream);

        thread = new Thread(parser);
        thread.start();

        Thread.sleep(2000);

        thread.interrupt();

        thread.join();
        thread = null;

        verify(callback, times(3)).onPentairPacket(packetsStandard.capture());

        List<PentairStandardPacket> allPackets = new ArrayList<PentairStandardPacket>();
        allPackets = packetsStandard.getAllValues();

        assertThat(allPackets.size(), equalTo(3));

        logger.info("1: {}", allPackets.get(0).getByte(PentairStandardPacket.ACTION));
        logger.info("2: {}", allPackets.get(1).getByte(PentairStandardPacket.ACTION));
        logger.info("3: {}", allPackets.get(2).getByte(PentairStandardPacket.ACTION));

        assertThat(allPackets.get(0).getByte(PentairStandardPacket.ACTION), equalTo(0x02));

        assertThat(allPackets.get(1).getByte(PentairStandardPacket.ACTION), equalTo(0x12));

        assertThat(allPackets.get(2).getByte(PentairStandardPacket.ACTION), equalTo(0x02));
    }

    @Test
    public void testNodeJSCapture() throws InterruptedException, IOException {
        byte[] array = parsehex(Files.readAllBytes(Paths.get("src/test/data/nodejs-capture.dat")));

        logger.info("testNodeJSCapture");

        ByteArrayInputStream inputStream = new ByteArrayInputStream(array, 0, array.length);

        parser.setInputStream(inputStream);

        thread = new Thread(parser);
        thread.start();

        Thread.sleep(2000);

        thread.interrupt();

        thread.join();
        thread = null;

        verify(callback, atLeast(1)).onPentairPacket(packetsStandard.capture());
        verify(callback, atLeast(1)).onIntelliChlorPacket(packetsIntellichlor.capture());

        List<PentairStandardPacket> allPackets = new ArrayList<PentairStandardPacket>();
        allPackets = packetsStandard.getAllValues();

        logger.info("Number of Pentair packets: {}", allPackets.size());

        assertThat(allPackets.size(), equalTo(281));

        List<PentairIntelliChlorPacket> allPacketsIntellichlor = new ArrayList<PentairIntelliChlorPacket>();
        allPacketsIntellichlor = packetsIntellichlor.getAllValues();

        logger.info("Number of Intellichlor packets: {}", allPacketsIntellichlor.size());

        assertThat(allPacketsIntellichlor.size(), equalTo(1));
    }

    @Test
    public void parseEasyTouch8() throws IOException, InterruptedException {
        byte[] array = parsehex(Files.readAllBytes(Paths.get("src/test/data/easytouch8.dat")));

        logger.info("parseEasyTouch8");

        // logger.debug("{}", javax.xml.bind.DatatypeConverter.printHexBinary(array));

        ByteArrayInputStream inputStream = new ByteArrayInputStream(array, 0, array.length);
        parser.setInputStream(inputStream);

        thread = new Thread(parser);
        thread.start();

        Thread.sleep(2000);

        thread.interrupt();

        thread.join();
        thread = null;

        verify(callback, atLeast(1)).onPentairPacket(packetsStandard.capture());
        verify(callback, atLeast(1)).onIntelliChlorPacket(packetsIntellichlor.capture());

        List<PentairStandardPacket> allPackets = new ArrayList<PentairStandardPacket>();
        allPackets = packetsStandard.getAllValues();

        logger.info("Number of Pentair packets: {}", allPackets.size());

        assertThat(allPackets.size(), equalTo(1032));

        List<PentairIntelliChlorPacket> allPacketsIntellichlor = new ArrayList<PentairIntelliChlorPacket>();
        allPacketsIntellichlor = packetsIntellichlor.getAllValues();

        logger.info("Number of Intellichlor packets: {}", allPacketsIntellichlor.size());

        assertThat(allPacketsIntellichlor.size(), equalTo(36));
    }
}
