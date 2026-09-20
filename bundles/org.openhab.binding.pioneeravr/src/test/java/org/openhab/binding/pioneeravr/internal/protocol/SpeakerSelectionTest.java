/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.pioneeravr.internal.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.pioneeravr.internal.protocol.ParameterizedCommand.ParameterizedCommandType;
import org.openhab.binding.pioneeravr.internal.protocol.Response.ResponseType;
import org.openhab.binding.pioneeravr.internal.protocol.SimpleCommand.SimpleCommandType;
import org.openhab.binding.pioneeravr.internal.protocol.avr.AvrConnectionException;

/**
 * Tests the speaker selection command and response of the Pioneer protocol.
 *
 * Commands are terminated by CR, as expected by the AVR, while responses passed to the parser have their CR/LF
 * terminators removed by BufferedReader.readLine().
 *
 * @author william - Initial contribution
 */
@NonNullByDefault
public class SpeakerSelectionTest {

    private static final String COMMAND_TERMINATION = "\r";

    @Test
    public void queryCommandUsesTheSpeakerCommand() {
        assertEquals("?SPK" + COMMAND_TERMINATION,
                RequestResponseFactory.getIpControlCommand(SimpleCommandType.SPEAKER_SELECTION_QUERY).getCommand());
    }

    @Test
    public void setCommandCarriesTheSelectedValueInFrontOfTheCommand() {
        assertEquals("0SPK" + COMMAND_TERMINATION, RequestResponseFactory
                .getIpControlCommand(ParameterizedCommandType.SPEAKER_SELECTION_SET).setParameter("0").getCommand());
        assertEquals("1SPK" + COMMAND_TERMINATION, RequestResponseFactory
                .getIpControlCommand(ParameterizedCommandType.SPEAKER_SELECTION_SET).setParameter("1").getCommand());
        assertEquals("2SPK" + COMMAND_TERMINATION, RequestResponseFactory
                .getIpControlCommand(ParameterizedCommandType.SPEAKER_SELECTION_SET).setParameter("2").getCommand());
        assertEquals("3SPK" + COMMAND_TERMINATION, RequestResponseFactory
                .getIpControlCommand(ParameterizedCommandType.SPEAKER_SELECTION_SET).setParameter("3").getCommand());
    }

    @Test
    public void speakerSelectionResponseIsParsedWithItsValue() {
        Response response = RequestResponseFactory.getIpControlResponse("SPK3");
        assertEquals(ResponseType.SPEAKER_SELECTION, response.getResponseType());
        assertEquals("3", response.getParameterValue());
        assertNotNull(response.getZone());
    }

    @Test
    public void outOfRangeSpeakerSelectionIsNotParsed() {
        assertThrows(AvrConnectionException.class, () -> RequestResponseFactory.getIpControlResponse("SPK4"));
    }
}
