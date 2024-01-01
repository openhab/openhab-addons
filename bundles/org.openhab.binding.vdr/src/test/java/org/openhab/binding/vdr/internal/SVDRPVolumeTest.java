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
package org.openhab.binding.vdr.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.vdr.internal.svdrp.SVDRPException;
import org.openhab.binding.vdr.internal.svdrp.SVDRPParseResponseException;
import org.openhab.binding.vdr.internal.svdrp.SVDRPVolume;

/**
 * Specific unit tests to check if {@link SVDRPVolume} parses SVDRP responses correctly
 *
 * @author Matthias Klocke - Initial contribution
 *
 */
@NonNullByDefault
public class SVDRPVolumeTest {
    private final String volumeResponseOk = "Audio volume is 255";
    private final String volumeResponseMute = "Audio is mute";
    private final String volumeResponseParseError1 = "Audiovolumeis255";
    private final String volumeResponseParseError2 = "Audio volume is 255x";

    @Test
    public void testParseVolumeData() throws SVDRPException {
        SVDRPVolume volume = SVDRPVolume.parse(volumeResponseOk);
        assertEquals(100, volume.getVolume());
        volume = SVDRPVolume.parse(volumeResponseMute);
        assertEquals(0, volume.getVolume());
    }

    @Test
    public void testParseExceptionVolumeData() {
        assertThrows(SVDRPParseResponseException.class, () -> {
            SVDRPVolume.parse(volumeResponseParseError1);
        });
        assertThrows(SVDRPParseResponseException.class, () -> {
            SVDRPVolume.parse(volumeResponseParseError2);
        });
    }
}
