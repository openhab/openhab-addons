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

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.knx.internal.security.KeyringTestData;
import org.openhab.binding.knx.internal.security.SecureSetupException;
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
 * Checks the texts the KNX IP bridge shows for mistakes in its KNX Secure settings.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class IPBridgeThingHandlerTest {
    private static final String KEYRING = "openhab6.knxkeys";

    private @Nullable String previousConfigDir;

    @BeforeEach
    void pointConfigFolderAtTheTestKeyrings() throws URISyntaxException {
        previousConfigDir = System.getProperty(OpenHAB.CONFIG_DIR_PROG_ARGUMENT);
        System.setProperty(OpenHAB.CONFIG_DIR_PROG_ARGUMENT, KeyringTestData.configFolder());
    }

    @AfterEach
    void restoreConfigFolder() {
        String previous = previousConfigDir;
        if (previous == null) {
            System.clearProperty(OpenHAB.CONFIG_DIR_PROG_ARGUMENT);
        } else {
            System.setProperty(OpenHAB.CONFIG_DIR_PROG_ARGUMENT, previous);
        }
    }

    @Test
    void wrongKeyringPasswordHasItsOwnText() {
        ThingStatusInfo status = initialize(Map.of("type", "SECURETUNNEL", "keyringFile", KEYRING, "keyringPassword",
                "not the password", "tunnelSourceAddress", "1.1.2"));

        assertEquals(ThingStatus.OFFLINE, status.getStatus());
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, status.getStatusDetail());
        assertEquals("@text/error.knx-keyring-wrong-password", status.getDescription());
    }

    @Test
    void wrongKeyringPasswordStillLeavesTheInterfaceListReadable() {
        IPBridgeThingHandler handler = new IPBridgeThingHandler(mock(Bridge.class), mock(NetworkAddressService.class));

        SecureSetupException e = assertThrows(SecureSetupException.class,
                () -> handler.initializeSecurity(KEYRING, "not the password"));

        assertEquals("@text/error.knx-keyring-wrong-password", e.statusText());
        assertEquals(17, Objects.requireNonNull(handler.getKeyringFacts()).interfaces().size());
    }

    @Test
    void handlerKnowsFileNameAndSignatureForTheConsole() {
        IPBridgeThingHandler handler = new IPBridgeThingHandler(mock(Bridge.class), mock(NetworkAddressService.class));
        assertEquals("", handler.getKeyringFileName());
        assertFalse(handler.isKeyringSignatureValid());

        assertTrue(handler.initializeSecurity(KEYRING, KeyringTestData.PASSWORD));
        assertEquals(KEYRING, handler.getKeyringFileName());
        assertTrue(handler.isKeyringSignatureValid());

        assertThrows(SecureSetupException.class, () -> handler.initializeSecurity(KEYRING, "wrong"));
        assertEquals(KEYRING, handler.getKeyringFileName());
        assertFalse(handler.isKeyringSignatureValid());

        assertFalse(handler.initializeSecurity("", ""));
        assertEquals("", handler.getKeyringFileName());
        assertNull(handler.getKeyringFacts());
    }

    @Test
    void secureTunnelWithKeyringNeedsTheTunnelSourceAddress() {
        ThingStatusInfo status = initialize(
                Map.of("type", "SECURETUNNEL", "keyringFile", KEYRING, "keyringPassword", KeyringTestData.PASSWORD));

        assertEquals(ThingStatus.OFFLINE, status.getStatus());
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, status.getStatusDetail());
        assertEquals("@text/error.knx-secure-tunnel-source-address-missing", status.getDescription());
    }

    @Test
    void secureTunnelWithPartOfTheManualParametersIsIncomplete() {
        ThingStatusInfo status = initialize(Map.of("type", "SECURETUNNEL", "keyringFile", KEYRING, "keyringPassword",
                KeyringTestData.PASSWORD, "tunnelUserId", "2"));

        assertEquals("@text/error.knx-secure-tunnel-config-incomplete", status.getDescription());
    }

    @Test
    void tunnelUserIdMustBeFromOneTo127() {
        IPBridgeThingHandler handler = new IPBridgeThingHandler(mock(Bridge.class), mock(NetworkAddressService.class));

        for (String invalid : new String[] { "0", "-1", "128", "eins", "1000" }) {
            SecureSetupException e = assertThrows(SecureSetupException.class,
                    () -> handler.initializeSecurity("", "", "", "da", invalid, "pw", ""), invalid);
            assertEquals("@text/error.knx-tunnel-user-id-invalid", e.statusText());
        }
        assertTrue(handler.initializeSecurity("", "", "", "da", "1", "pw", ""));
        assertTrue(handler.initializeSecurity("", "", "", "da", "127", "pw", ""));
    }

    @Test
    void tunnelUserIdErrorReachesTheBridgeStatus() {
        ThingStatusInfo status = initialize(Map.of("type", "SECURETUNNEL", "tunnelUserId", "200", "tunnelUserPassword",
                "pw", "tunnelDeviceAuthentication", "da"));

        assertEquals("@text/error.knx-tunnel-user-id-invalid", status.getDescription());
    }

    @Test
    void otherSecureErrorsKeepTheirText() {
        ThingStatusInfo status = initialize(
                Map.of("type", "SECURETUNNEL", "tunnelSourceAddress", "1.1.2", "routerBackboneKey", "wrongLength"));

        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, status.getStatusDetail());
        String description = status.getDescription();
        assertNotNull(description);
        assertTrue(description.contains("KnxSecureException"), description);
        assertFalse(description.startsWith("@text/"), description);
        assertInstanceOf(KnxSecureException.class,
                assertThrows(KnxSecureException.class,
                        () -> new IPBridgeThingHandler(mock(Bridge.class), mock(NetworkAddressService.class))
                                .initializeSecurity("", "", "wrongLength", "", "", "", "")));
    }

    private ThingStatusInfo initialize(Map<String, Object> parameters) {
        Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(new ThingUID("knx", "ip", "test"));
        when(bridge.getConfiguration()).thenReturn(new Configuration(parameters));
        IPBridgeThingHandler handler = new IPBridgeThingHandler(bridge, mock(NetworkAddressService.class));
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.initializeLater(0);

        ArgumentCaptor<ThingStatusInfo> captor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, atLeastOnce()).statusUpdated(eq(bridge), captor.capture());
        return captor.getValue();
    }
}
