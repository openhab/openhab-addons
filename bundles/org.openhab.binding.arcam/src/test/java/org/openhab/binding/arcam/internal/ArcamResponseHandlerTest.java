/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.arcam.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.arcam.internal.connection.ArcamResponse;
import org.openhab.binding.arcam.internal.connection.ArcamResponseHandler;

/**
 * Tests for the response handler
 *
 * @author Joep Admiraal - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class ArcamResponseHandlerTest {

    @InjectMocks
    @NonNullByDefault({})
    private ArcamResponseHandler sut;

    @Test
    public void parseByteShouldParsePowerOnResponse() {
        byte[] command = new byte[] { 0x21, 0x01, 0x00, 0x00, 0x01, 0x01, 0x0D };

        ArcamResponse result = new ArcamResponse();
        for (byte b : command) {
            result = sut.parseByte(b);
        }

        assertNotNull(result);
        assertEquals(1, result.zn);
        assertEquals((byte) 0x00, result.cc);
        assertEquals((byte) 0x00, result.ac);
        assertEquals(1, result.dl);
        assertEquals((byte) 0x01, result.data.get(0));
        assertEquals(1, result.data.size());
    }

    @Test
    public void parseByteShouldBeAbleToParseStartByteInData() {
        byte[] command = new byte[] { 0x21, 0x01, 0x00, 0x00, 0x04, 0x01, 0x21, 0x03, 0x04, 0x0D };

        ArcamResponse result = new ArcamResponse();
        for (byte b : command) {
            result = sut.parseByte(b);
        }

        assertNotNull(result);
        assertEquals(1, result.zn);
        assertEquals((byte) 0x00, result.cc);
        assertEquals((byte) 0x00, result.ac);
        assertEquals(4, result.dl);
        assertEquals((byte) 0x01, result.data.get(0));
        assertEquals(4, result.data.size());
    }

    @Test
    public void parseByteShouldBeAbleToParseMultipleResponses() {
        byte[] firstCommand = new byte[] { 0x21, 0x01, 0x02, 0x03, 0x01, 0x05, 0x0D };

        ArcamResponse result = new ArcamResponse();
        for (byte b : firstCommand) {
            result = sut.parseByte(b);
        }

        byte[] secondCommand = new byte[] { 0x21, 0x06, 0x07, 0x08, 0x01, 0x09, 0x0D };

        for (byte b : secondCommand) {
            result = sut.parseByte(b);
        }

        assertNotNull(result);
        assertEquals(6, result.zn);
        assertEquals((byte) 0x07, result.cc);
        assertEquals((byte) 0x08, result.ac);
        assertEquals(1, result.dl);
        assertEquals((byte) 0x09, result.data.get(0));
        assertEquals(1, result.data.size());
    }
}
