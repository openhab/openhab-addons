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
package org.openhab.binding.vdr.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.vdr.internal.svdrp.SVDRPException;
import org.openhab.binding.vdr.internal.svdrp.SVDRPParseResponseException;
import org.openhab.binding.vdr.internal.svdrp.SVDRPVolume;
import org.openhab.binding.vdr.internal.svdrp.SVDRPWelcome;

/**
 * Specific unit tests to check if {@link SVDRPWelcome} parses SVDRP responses correctly
 *
 * @author Matthias Klocke - Initial contribution
 *
 */
@NonNullByDefault
public class SVDRPWelcomeTest {
    private final String welcomeResponseOk = "srv SVDRP VideoDiskRecorder 2.5.1; Mon Jan 11 19:46:54 2021; UTF-8";
    private final String welcomeResponseParseError1 = "srv SVDRP VideoDiskRecorder 2.5.1; Mon Jan 11 19:46:54 2021 UTF-8";
    private final String welcomeResponseParseError2 = "srv SVDRP VideoDiskRecorder2.5.1; Mon Jan 11 19:46:54 2021 UTF-8";

    @Test
    public void testParseWelcomeData() throws SVDRPException {
        SVDRPWelcome welcome = SVDRPWelcome.parse(welcomeResponseOk);
        assertEquals("UTF-8", welcome.getCharset());
        assertEquals("2.5.1", welcome.getVersion());
        assertEquals("Mon Jan 11 19:46:54 2021", welcome.getDateAndTime());
    }

    @Test
    public void testParseExceptionVolumeData() {
        assertThrows(SVDRPParseResponseException.class, () -> {
            SVDRPVolume.parse(welcomeResponseParseError1);
        });
        assertThrows(SVDRPParseResponseException.class, () -> {
            SVDRPVolume.parse(welcomeResponseParseError2);
        });
    }
}
