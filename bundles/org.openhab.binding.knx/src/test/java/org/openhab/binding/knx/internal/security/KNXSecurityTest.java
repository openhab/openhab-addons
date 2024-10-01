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
package org.openhab.binding.knx.internal.security;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.knxnetip.SecureConnection;
import tuwien.auto.calimero.secure.Keyring;
import tuwien.auto.calimero.secure.KnxSecureException;
import tuwien.auto.calimero.secure.Security;

/**
 *
 * @author Holger Friedrich - initial contribution
 *
 */
@NonNullByDefault
public class KNXSecurityTest {

    @Test
    public void testCalimeroKeyring() {
        @SuppressWarnings("null")
        final String testFile = getClass().getClassLoader().getResource("misc" + File.separator + "openhab6.knxkeys")
                .toString();
        final String passwordString = "habopen";

        final char[] password = passwordString.toCharArray();
        assertNotEquals("", testFile);

        Keyring keys = Keyring.load(testFile);

        // System.out.println(keys.devices().toString());
        // System.out.println(keys.groups().toString());
        // System.out.println(keys.interfaces().toString());

        GroupAddress ga = new GroupAddress(8, 0, 0);
        byte[] key800enc = keys.groups().get(ga);
        assertNotNull(key800enc);
        if (key800enc != null) {
            assertNotEquals(0, key800enc.length);
        }
        byte[] key800dec = keys.decryptKey(key800enc, password);
        assertEquals(16, key800dec.length);

        IndividualAddress nopa = new IndividualAddress(2, 8, 20);
        Keyring.Device nodev = keys.devices().get(nopa);
        assertNull(nodev);

        IndividualAddress pa = new IndividualAddress(1, 1, 42);
        Keyring.Device dev = keys.devices().get(pa);
        assertNotNull(dev);
        // cannot check this for dummy test file, needs real device to be included
        // assertNotEquals(0, dev.sequenceNumber());

        Security openhabSecurity = Security.newSecurity();
        openhabSecurity.useKeyring(keys, password);
        Map<GroupAddress, byte[]> groupKeys = openhabSecurity.groupKeys();
        assertEquals(3, groupKeys.size());
        groupKeys.remove(ga);
        assertEquals(2, groupKeys.size());
        openhabSecurity.useKeyring(keys, password);
        Map<GroupAddress, byte[]> groupKeys2 = openhabSecurity.groupKeys();
        assertEquals(3, groupKeys2.size());
        assertEquals(3, groupKeys.size());
        ga = new GroupAddress(1, 0, 0);
        groupKeys.put(ga, new byte[1]);
        assertEquals(4, groupKeys2.size());
        assertEquals(4, groupKeys.size());
        openhabSecurity.useKeyring(keys, password);
        assertEquals(4, groupKeys2.size());
        assertEquals(4, groupKeys.size());
    }

    // check tunnel settings, this file does not contain any key
    @Test
    public void testSecurityHelperEmpty() {
        @SuppressWarnings("null")
        final String testFile = getClass().getClassLoader()
                .getResource("misc" + File.separator + "openhab6-minimal-ipif.knxkeys").toString();
        final String passwordString = "habopen";

        final char[] password = passwordString.toCharArray();
        assertNotEquals("", testFile);

        Keyring keys = Keyring.load(testFile);
        Security openhabSecurity = Security.newSecurity();
        openhabSecurity.useKeyring(keys, password);

        assertThrows(KnxSecureException.class, () -> {
            KNXBridgeBaseThingHandler.secHelperReadBackboneKey(Optional.empty(), passwordString);
        });
        assertTrue(KNXBridgeBaseThingHandler.secHelperReadBackboneKey(Optional.ofNullable(keys), passwordString)
                .isEmpty());

        // now check tunnel (expected to fail, not included)
        IndividualAddress secureTunnelSourceAddr = new IndividualAddress(2, 8, 20);
        assertThrows(KnxSecureException.class, () -> {
            KNXBridgeBaseThingHandler.secHelperReadTunnelConfig(Optional.empty(), passwordString,
                    secureTunnelSourceAddr);
        });
        assertTrue(KNXBridgeBaseThingHandler
                .secHelperReadTunnelConfig(Optional.ofNullable(keys), passwordString, secureTunnelSourceAddr)
                .isEmpty());
    }

    // check tunnel settings, this file does not contain any key
    @Test
    public void testSecurityHelperRouterKey() {
        @SuppressWarnings("null")
        final String testFile = getClass().getClassLoader()
                .getResource("misc" + File.separator + "openhab6-minimal-sipr.knxkeys").toString();
        final String passwordString = "habopen";

        final char[] password = passwordString.toCharArray();
        assertNotEquals("", testFile);

        Keyring keys = Keyring.load(testFile);
        Security openhabSecurity = Security.newSecurity();
        openhabSecurity.useKeyring(keys, password);

        assertThrows(KnxSecureException.class,
                () -> KNXBridgeBaseThingHandler.secHelperReadBackboneKey(Optional.empty(), passwordString));
        assertTrue(KNXBridgeBaseThingHandler.secHelperReadBackboneKey(Optional.ofNullable(keys), passwordString)
                .isPresent());

        // now check tunnel (expected to fail, not included)
        IndividualAddress secureTunnelSourceAddr = new IndividualAddress(2, 8, 20);
        assertThrows(KnxSecureException.class, () -> KNXBridgeBaseThingHandler
                .secHelperReadTunnelConfig(Optional.empty(), passwordString, secureTunnelSourceAddr));
        assertTrue(KNXBridgeBaseThingHandler
                .secHelperReadTunnelConfig(Optional.ofNullable(keys), passwordString, secureTunnelSourceAddr)
                .isEmpty());
    }

    // check tunnel settings, this file contains a secure interface, but no router password
    @Test
    public void testSecurityHelperTunnelKey() {
        @SuppressWarnings("null")
        final String testFile = getClass().getClassLoader()
                .getResource("misc" + File.separator + "openhab6-minimal-sipif.knxkeys").toString();
        final String passwordString = "habopen";

        final char[] password = passwordString.toCharArray();
        assertNotEquals("", testFile);

        Keyring keys = Keyring.load(testFile);
        Security openhabSecurity = Security.newSecurity();
        openhabSecurity.useKeyring(keys, password);

        assertThrows(KnxSecureException.class,
                () -> KNXBridgeBaseThingHandler.secHelperReadBackboneKey(Optional.empty(), passwordString));
        assertTrue(KNXBridgeBaseThingHandler.secHelperReadBackboneKey(Optional.ofNullable(keys), passwordString)
                .isEmpty());

        // now check tunnel
        IndividualAddress secureTunnelSourceAddr = new IndividualAddress(1, 1, 2);
        assertThrows(KnxSecureException.class, () -> KNXBridgeBaseThingHandler
                .secHelperReadTunnelConfig(Optional.empty(), passwordString, secureTunnelSourceAddr));
        assertTrue(KNXBridgeBaseThingHandler
                .secHelperReadTunnelConfig(Optional.ofNullable(keys), passwordString, secureTunnelSourceAddr)
                .isPresent());
    }

    @Test
    public void testSecurityHelpers() {
        @SuppressWarnings("null")
        final String testFile = getClass().getClassLoader().getResource("misc" + File.separator + "openhab6.knxkeys")
                .toString();
        final String passwordString = "habopen";

        final char[] password = passwordString.toCharArray();
        assertNotEquals("", testFile);

        Keyring keys = Keyring.load(testFile);
        // this is done during load() in v2.5, but check it once....
        assertTrue(keys.verifySignature(password));

        Security openhabSecurity = Security.newSecurity();
        openhabSecurity.useKeyring(keys, password);

        // now check router settings:
        assertThrows(KnxSecureException.class,
                () -> KNXBridgeBaseThingHandler.secHelperReadBackboneKey(Optional.empty(), passwordString));
        String bbKeyHex = "D947B12DDECAD528B1D5A88FD347F284";
        byte[] bbKeyParsedLower = KNXBridgeBaseThingHandler.secHelperParseBackboneKey(bbKeyHex.toLowerCase());
        byte[] bbKeyParsedUpper = KNXBridgeBaseThingHandler.secHelperParseBackboneKey(bbKeyHex);
        Optional<byte[]> bbKeyRead = KNXBridgeBaseThingHandler.secHelperReadBackboneKey(Optional.ofNullable(keys),
                passwordString);
        assertEquals(16, bbKeyParsedUpper.length);
        assertArrayEquals(bbKeyParsedUpper, bbKeyParsedLower);
        assertTrue(bbKeyRead.isPresent());
        assertArrayEquals(bbKeyParsedUpper, bbKeyRead.get());
        // System.out.print("Backbone key: \"");
        // for (byte i : backboneGroupKey)
        // System.out.print(String.format("%02X", i));
        // System.out.println("\"");

        // now check tunnel settings:
        IndividualAddress secureTunnelSourceAddr = new IndividualAddress(1, 1, 2);
        IndividualAddress noSecureTunnelSourceAddr = new IndividualAddress(2, 8, 20);
        assertThrows(KnxSecureException.class, () -> KNXBridgeBaseThingHandler
                .secHelperReadTunnelConfig(Optional.empty(), passwordString, secureTunnelSourceAddr));
        assertTrue(KNXBridgeBaseThingHandler
                .secHelperReadTunnelConfig(Optional.ofNullable(keys), passwordString, noSecureTunnelSourceAddr)
                .isEmpty());

        var config = KNXBridgeBaseThingHandler.secHelperReadTunnelConfig(Optional.ofNullable(keys), passwordString,
                secureTunnelSourceAddr);
        assertTrue(config.isPresent());
        assertEquals(2, config.get().user);

        assertArrayEquals(SecureConnection.hashUserPassword("mytunnel1".toCharArray()), config.get().userKey);
        assertArrayEquals(SecureConnection.hashDeviceAuthenticationPassword("myauthcode".toCharArray()),
                config.get().devKey);

        // secure group addresses should contain at least one address marked as "surrogate"
        final String secureAddresses = KNXBridgeBaseThingHandler.secHelperGetSecureGroupAddresses(openhabSecurity);
        assertTrue(secureAddresses.contains("(S)"));
        assertTrue(secureAddresses.contains("8/4/0"));
    }
}
