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
package org.openhab.binding.doorbird.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.doorbird.internal.api.SipStatus;

/**
 * The {@link SipStatusTest} is responsible for testing the functionality
 * of Doorbird "sipStatus" message parsing.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class SipStatusTest {

    private final String sipStatusJson =
    //@formatter:off
    """
    {\
    'BHA': {\
    'RETURNCODE': '1',\
    'SIP': [{\
    'ENABLE': '10',\
    'PRIORITIZE_APP': '1',\
    'REGISTER_URL': '192.168.178.1',\
    'REGISTER_USER': 'xxxxx',\
    'REGISTER_PASSWORD': 'yyyyy',\
    'AUTOCALL_MOTIONSENSOR_URL': 'motion-url',\
    'AUTOCALL_DOORBELL_URL': 'doorbell-url',\
    'SPK_VOLUME': '70',\
    'MIC_VOLUME': '33',\
    'DTMF': '1',\
    'relais:1': '0',\
    'relais:2': '1',\
    'LIGHT_PASSCODE': 'light-passcode',\
    'INCOMING_CALL_ENABLE': '0',\
    'INCOMING_CALL_USER': 'abcde',\
    'ANC': '1',\
    'LASTERRORCODE': '901',\
    'LASTERRORTEXT': 'OK',\
    'RING_TIME_LIMIT': '60',\
    'CALL_TIME_LIMIT': '180'\
    }]\
    }\
    }\
    """;
    //@formatter:on

    @Test
    public void testParsing() {
        SipStatus sipStatus = new SipStatus(sipStatusJson);
        assertEquals("70", sipStatus.getSpeakerVolume());
        assertEquals("33", sipStatus.getMicrophoneVolume());
        assertEquals("901", sipStatus.getLastErrorCode());
        assertEquals("OK", sipStatus.getLastErrorText());
        assertEquals("60", sipStatus.getRingTimeLimit());
        assertEquals("180", sipStatus.getCallTimeLimit());
    }
}
