/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.binding.vdr.internal.svdrp.SVDRPChannel;
import org.openhab.binding.vdr.internal.svdrp.SVDRPDiskStatus;
import org.openhab.binding.vdr.internal.svdrp.SVDRPException;
import org.openhab.binding.vdr.internal.svdrp.SVDRPParseResponseException;

/**
 * Specific unit tests to check if {@link SVDRPDiskStatus} parses SVDRP responses correctly
 *
 * @author Matthias Klocke - Initial contribution
 *
 */
@NonNullByDefault
public class SVDRPDiskStatusTest {

    private final String diskStatusResponseOk = "411266MB 30092MB 92%";
    private final String diskStatusResponseParseError1 = "411266MB  30092MB  92%";
    private final String diskStatusResponseParseError2 = "411266MB 30092 92%";
    private final String diskStatusResponseParseError3 = "42b3MB 30092MB 92%";

    @Test
    public void testParseDiskStatus() throws SVDRPException {
        SVDRPDiskStatus diskStatus = SVDRPDiskStatus.parse(diskStatusResponseOk);
        assertEquals(411266, diskStatus.getMegaBytesTotal());
        assertEquals(30092, diskStatus.getMegaBytesFree());
        assertEquals(92, diskStatus.getPercentUsed());
    }

    @Test
    public void testParseExceptionDiskStatus() {
        assertThrows(SVDRPParseResponseException.class, () -> {
            SVDRPChannel.parse(diskStatusResponseParseError1);
        });
        assertThrows(SVDRPParseResponseException.class, () -> {
            SVDRPChannel.parse(diskStatusResponseParseError2);
        });
        assertThrows(SVDRPParseResponseException.class, () -> {
            SVDRPChannel.parse(diskStatusResponseParseError3);
        });
    }
}
