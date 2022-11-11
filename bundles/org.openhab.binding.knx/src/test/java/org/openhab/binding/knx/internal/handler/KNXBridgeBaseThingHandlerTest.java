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
package org.openhab.binding.knx.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;

import tuwien.auto.calimero.secure.KnxSecureException;

/**
 *
 * @author Holger Friedrich - initial contribution
 *
 */
@NonNullByDefault
public class KNXBridgeBaseThingHandlerTest {

    @Test
    public void testSecurityHelpers() {
        // now check router settings:
        String bbKeyHex = "D947B12DDECAD528B1D5A88FD347F284";
        byte[] bbKeyParsedLower = KNXBridgeBaseThingHandler.secHelperParseBackboneKey(bbKeyHex.toLowerCase());
        byte[] bbKeyParsedUpper = KNXBridgeBaseThingHandler.secHelperParseBackboneKey(bbKeyHex);
        assertEquals(16, bbKeyParsedUpper.length);
        assertArrayEquals(bbKeyParsedUpper, bbKeyParsedLower);
    }

    @Test
    @SuppressWarnings("null")
    public void testInitializeSecurity() {
        Bridge bridge = mock(Bridge.class);
        NetworkAddressService nas = mock(NetworkAddressService.class);
        IPBridgeThingHandler handler = new IPBridgeThingHandler(bridge, nas);

        // no config given
        assertFalse(handler.initializeSecurity("", "", "", ""));

        // router password configured, length must be 16 bytes in hex notation
        assertTrue(handler.initializeSecurity("D947B12DDECAD528B1D5A88FD347F284", "", "", ""));
        assertTrue(handler.initializeSecurity("0xD947B12DDECAD528B1D5A88FD347F284", "", "", ""));
        assertThrows(KnxSecureException.class, () -> {
            handler.initializeSecurity("wrongLength", "", "", "");
        });

        // tunnel configuration
        assertTrue(handler.initializeSecurity("", "da", "1", "pw"));
        // cTunnelUser is restricted to a number >0
        assertThrows(KnxSecureException.class, () -> {
            handler.initializeSecurity("", "da", "0", "pw");
        });
        assertThrows(KnxSecureException.class, () -> {
            handler.initializeSecurity("", "da", "eins", "pw");
        });
        // at least one setting for tunnel is given, count as try to configure secure tunnel
        // plausibility is checked during initialize()
        assertTrue(handler.initializeSecurity("", "da", "", ""));
        assertTrue(handler.initializeSecurity("", "", "1", ""));
        assertTrue(handler.initializeSecurity("", "", "", "pw"));
    }
}
