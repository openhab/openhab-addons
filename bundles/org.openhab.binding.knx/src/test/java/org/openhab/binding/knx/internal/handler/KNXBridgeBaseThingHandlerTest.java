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
package org.openhab.binding.knx.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.OpenHAB;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

import io.calimero.secure.KnxSecureException;

/**
 *
 * @author Holger Friedrich - initial contribution
 *
 */
@NonNullByDefault
class KNXBridgeBaseThingHandlerTest {

    @Test
    void testSecurityHelpers() {
        // now check router settings:
        String bbKeyHex = "D947B12DDECAD528B1D5A88FD347F284";
        byte[] bbKeyParsedLower = KNXBridgeBaseThingHandler
                .secHelperParseBackboneKey(bbKeyHex.toLowerCase(Locale.ROOT));
        byte[] bbKeyParsedUpper = KNXBridgeBaseThingHandler.secHelperParseBackboneKey(bbKeyHex);
        assertEquals(16, bbKeyParsedUpper.length);
        assertArrayEquals(bbKeyParsedUpper, bbKeyParsedLower);
    }

    @Test
    @SuppressWarnings("null")
    void testInitializeSecurity() {
        Bridge bridge = mock(Bridge.class);
        NetworkAddressService nas = mock(NetworkAddressService.class);
        IPBridgeThingHandler handler = new IPBridgeThingHandler(bridge, nas);

        // no config given
        assertFalse(handler.initializeSecurity("", "", "", "", "", "", ""));

        // router password configured, length must be 16 bytes in hex notation
        assertTrue(handler.initializeSecurity("", "", "D947B12DDECAD528B1D5A88FD347F284", "", "", "", ""));
        assertTrue(handler.initializeSecurity("", "", "0xD947B12DDECAD528B1D5A88FD347F284", "", "", "", ""));
        assertThrows(KnxSecureException.class, () -> handler.initializeSecurity("", "", "wrongLength", "", "", "", ""));

        // tunnel configuration
        assertTrue(handler.initializeSecurity("", "", "", "da", "1", "pw", ""));
        // cTunnelUser is restricted to a number >0
        assertThrows(KnxSecureException.class, () -> handler.initializeSecurity("", "", "", "da", "0", "pw", ""));
        assertThrows(KnxSecureException.class, () -> handler.initializeSecurity("", "", "", "da", "eins", "pw", ""));
        // at least one setting for tunnel is given, count as try to configure secure tunnel
        // plausibility is checked during initialize()
        assertTrue(handler.initializeSecurity("", "", "", "da", "", "", ""));
        assertTrue(handler.initializeSecurity("", "", "", "", "1", "", ""));
        assertTrue(handler.initializeSecurity("", "", "", "", "", "pw", ""));

        assertThrows(KnxSecureException.class,
                () -> handler.initializeSecurity("nonExistingFile.xml", "", "", "", "", "", ""));

        Properties pBackup = new Properties(System.getProperties());
        try {
            final File testFile = new File(
                    getClass().getClassLoader().getResource("misc" + File.separator + "openhab6.knxkeys").toURI());
            final String passwordString = "habopen";

            Properties p = new Properties(System.getProperties());
            p.put(OpenHAB.CONFIG_DIR_PROG_ARGUMENT, testFile.getParent().replaceAll("misc$", ""));
            System.setProperties(p);

            assertTrue(handler.initializeSecurity(testFile.getName(), passwordString, "", "", "", "pw", ""));

            assertThrows(KnxSecureException.class,
                    () -> assertTrue(handler.initializeSecurity(testFile.getName(), "wrong", "", "", "", "pw", "")));
        } catch (URISyntaxException e) {
            fail("should never happen");
        } finally {
            // properties are not persistent, but change may interference with other tests -> restore
            System.setProperties(pBackup);
        }
    }

    @Test
    @SuppressWarnings("null")
    void testInitializeWithoutLocalIpv4RetriesUntilAnAddressAppears() throws Exception {
        Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(new ThingUID("knx:ip:test"));
        // loopback only, so the test never reaches the LAN; autoReconnectPeriod 0 must not stop the retry
        when(bridge.getConfiguration()).thenReturn(
                new Configuration(Map.of("type", "ROUTER", "ipAddress", "224.0.23.12", "autoReconnectPeriod", 0)));
        NetworkAddressService nas = mock(NetworkAddressService.class);
        when(nas.getPrimaryIpv4HostAddress()).thenReturn(null);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        IPBridgeThingHandler handler = new IPBridgeThingHandler(bridge, nas);
        handler.setCallback(callback);

        try {
            handler.initializeLater();

            verify(callback).statusUpdated(bridge, new ThingStatusInfo(ThingStatus.OFFLINE,
                    ThingStatusDetail.COMMUNICATION_ERROR, "@text/error.knx-no-local-ipv4"));
            Field field = IPBridgeThingHandler.class.getDeclaredField("initJob");
            field.setAccessible(true);
            ScheduledFuture<?> retry = (ScheduledFuture<?>) field.get(handler);
            assertNotNull(retry);
            assertFalse(retry.isDone());
            long delay = retry.getDelay(TimeUnit.SECONDS);
            assertTrue(delay > 0 && delay <= IPBridgeThingHandler.NO_LOCAL_IP_RETRY_SECONDS);

            // what the retry runs once the address is there
            retry.cancel(false);
            when(nas.getPrimaryIpv4HostAddress()).thenReturn("127.0.0.1");
            handler.initializeLater();

            verify(nas, times(2)).getPrimaryIpv4HostAddress();
            verify(callback).statusUpdated(bridge,
                    new ThingStatusInfo(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, null));
        } finally {
            handler.dispose();
        }
    }
}
