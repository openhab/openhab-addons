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
package org.openhab.binding.knx.internal.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openhab.binding.knx.internal.factory.KNXHandlerFactory;
import org.openhab.binding.knx.internal.handler.IPBridgeThingHandler;
import org.openhab.binding.knx.internal.handler.SerialBridgeThingHandler;
import org.openhab.binding.knx.internal.i18n.KNXTranslationProvider;
import org.openhab.binding.knx.internal.i18n.MockedLocaleProvider;
import org.openhab.binding.knx.internal.i18n.PropertiesTranslationProvider;
import org.openhab.binding.knx.internal.security.KeyringFacts;
import org.openhab.binding.knx.internal.security.KeyringTestData;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;

/**
 * Checks the suggestions MainUI gets for the address parameters of a KNX IP bridge.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KNXConfigOptionProviderTest {
    private static final ThingUID BRIDGE = new ThingUID("knx", "ip", "bridge");
    private static final URI BRIDGE_URI = URI.create("thing:" + BRIDGE);

    private final KNXHandlerFactory factory = mock(KNXHandlerFactory.class);
    private final KNXConfigOptionProvider provider = new KNXConfigOptionProvider(factory);

    @BeforeEach
    void setUp() {
        KNXTranslationProvider.I18N.setProvider(new MockedLocaleProvider(), new PropertiesTranslationProvider());
    }

    @AfterEach
    void tearDown() {
        KNXTranslationProvider.I18N.setProvider(null, null);
    }

    private void bridgeWith(@Nullable KeyringFacts facts) {
        IPBridgeThingHandler handler = mock(IPBridgeThingHandler.class);
        Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(BRIDGE);
        when(handler.getThing()).thenReturn(bridge);
        when(handler.getKeyringFacts()).thenReturn(facts);
        when(factory.getBridges()).thenReturn(Set.of(handler));
    }

    private static KeyringFacts publicKeyring() {
        return new KeyringFacts(KeyringTestData.load("openhab6.knxkeys"));
    }

    @Test
    void tunnelSourceAddressOffersTheTunnellingInterfaces() {
        bridgeWith(publicKeyring());

        Collection<ParameterOption> options = provider.getParameterOptions(BRIDGE_URI, "tunnelSourceAddress", null,
                null);

        assertNotNull(options);
        List<ParameterOption> list = List.copyOf(options);
        assertEquals(16, list.size());
        assertEquals("1.1.2", list.get(0).getValue());
        assertEquals("1.1.2 on 1.1.1 - no secured group addresses linked", list.get(0).getLabel());
        assertEquals("1.1.17", list.get(15).getValue());
        assertEquals("1.1.17 on 1.1.0 - no secured group addresses linked", list.get(15).getLabel());
    }

    @Test
    void localSourceAddressOffersTheOtherInterfaceTypes() {
        bridgeWith(publicKeyring());

        Collection<ParameterOption> options = provider.getParameterOptions(BRIDGE_URI, "localSourceAddr", null, null);

        assertNotNull(options);
        List<ParameterOption> list = List.copyOf(options);
        assertEquals(1, list.size());
        assertEquals("1.0.128", list.get(0).getValue());
        assertEquals("1.0.128 - 2 secured group addresses linked", list.get(0).getLabel());
    }

    @Test
    void labelCountsLinkedAddresses(@TempDir Path dir) throws IOException {
        String interfaces = """
                <Interface IndividualAddress="7.7.251" Type="Tunneling" Host="7.7.0" UserID="2">
                <Group Address="%d" />
                </Interface>
                <Interface IndividualAddress="7.7.252" Type="Tunneling" Host="7.7.0" UserID="3">
                <Group Address="%d" />
                <Group Address="%d" />
                <Group Address="%d" />
                </Interface>
                """.formatted(KeyringTestData.rawGroup(3, 0, 0), KeyringTestData.rawGroup(3, 0, 0),
                KeyringTestData.rawGroup(3, 0, 1), KeyringTestData.rawGroup(3, 0, 2));
        String groups = KeyringTestData.groupWithKey(3, 0, 0) + KeyringTestData.groupWithKey(3, 0, 1)
                + KeyringTestData.groupWithKey(3, 0, 2);
        bridgeWith(new KeyringFacts(KeyringTestData.synthetic(dir, interfaces, groups)));

        Collection<ParameterOption> options = provider.getParameterOptions(BRIDGE_URI, "tunnelSourceAddress", null,
                null);

        assertNotNull(options);
        assertEquals(
                List.of("7.7.251 on 7.7.0 - 1 secured group address linked",
                        "7.7.252 on 7.7.0 - 3 secured group addresses linked"),
                options.stream().map(ParameterOption::getLabel).toList());
    }

    @Test
    void otherParametersGetNothing() {
        bridgeWith(publicKeyring());

        assertNull(provider.getParameterOptions(BRIDGE_URI, "keyringFile", null, null));
        assertNull(provider.getParameterOptions(BRIDGE_URI, "ipAddress", null, null));
    }

    @Test
    void otherUrisGetNothing() {
        bridgeWith(publicKeyring());

        assertNull(provider.getParameterOptions(URI.create("thing-type:knx:ip"), "tunnelSourceAddress", null, null));
        assertNull(provider.getParameterOptions(URI.create("thing:knx:ip:other"), "tunnelSourceAddress", null, null));
        assertNull(provider.getParameterOptions(URI.create("thing:nonsense"), "tunnelSourceAddress", null, null));
        assertNull(provider.getParameterOptions(URI.create("channel-type:knx:ip"), "tunnelSourceAddress", null, null));
    }

    @Test
    void bridgeWithoutKeyringGetsNothing() {
        bridgeWith(null);

        assertNull(provider.getParameterOptions(BRIDGE_URI, "tunnelSourceAddress", null, null));
        assertNull(provider.getParameterOptions(BRIDGE_URI, "localSourceAddr", null, null));
    }

    @Test
    void keyringWithoutInterfacesOfTheTypeGetsNothing() {
        bridgeWith(new KeyringFacts(KeyringTestData.load("openhab6-minimal-sipif.knxkeys")));

        assertNull(provider.getParameterOptions(BRIDGE_URI, "localSourceAddr", null, null));
    }

    @Test
    void serialBridgeGetsNothing() {
        SerialBridgeThingHandler handler = mock(SerialBridgeThingHandler.class);
        Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(BRIDGE);
        when(handler.getThing()).thenReturn(bridge);
        when(handler.getKeyringFacts()).thenReturn(publicKeyring());
        when(factory.getBridges()).thenReturn(Set.of(handler));

        assertNull(provider.getParameterOptions(BRIDGE_URI, "tunnelSourceAddress", null, null));
    }
}
