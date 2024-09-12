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
import org.openhab.binding.vdr.internal.svdrp.SVDRPChannel;
import org.openhab.binding.vdr.internal.svdrp.SVDRPException;
import org.openhab.binding.vdr.internal.svdrp.SVDRPParseResponseException;

/**
 * Specific unit tests to check if {@link SVDRPChannel} parses SVDRP responses correctly
 *
 * @author Matthias Klocke - Initial contribution
 *
 */
@NonNullByDefault
public class SVDRPChannelTest {

    private final String channelResponseOk = "3 WDR HD Bielefeld";
    private final String channelResponseParseError = "250WDR HD Bielefeld";

    @Test
    public void testParseChannelData() throws SVDRPException {
        SVDRPChannel channel = SVDRPChannel.parse(channelResponseOk);
        assertEquals("WDR HD Bielefeld", channel.getName());
        assertEquals(3, channel.getNumber());
    }

    @Test
    public void testParseExceptionChannelData() {
        assertThrows(SVDRPParseResponseException.class, () -> {
            SVDRPChannel.parse(channelResponseParseError);
        });
    }
}
