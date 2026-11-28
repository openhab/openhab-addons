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
package org.openhab.binding.knx.internal.security;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openhab.binding.knx.internal.security.KeyringFacts.InterfaceFacts;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;
import io.calimero.secure.Keyring;
import io.calimero.secure.Keyring.Interface.Type;

/**
 * Checks what {@link KeyringFacts} reads from a keyring that was loaded without its password.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KeyringFactsTest {
    static final GroupAddress LINKED_TO_BACKBONE_1 = new GroupAddress(8, 0, 0);
    static final GroupAddress LINKED_TO_BACKBONE_2 = new GroupAddress(8, 4, 0);
    static final GroupAddress LINKED_TO_NOBODY = new GroupAddress(8, 4, 1);

    static Keyring loadWithoutPassword(String name) {
        return KeyringTestData.load(name);
    }

    @Test
    void interfacesAreReadWithoutThePassword() {
        KeyringFacts facts = new KeyringFacts(loadWithoutPassword("openhab6.knxkeys"));

        assertEquals(17, facts.interfaces().size());
        InterfaceFacts tunnel = facts.interfaceOf(new IndividualAddress(1, 1, 2)).orElseThrow();
        assertEquals(Type.Tunneling, tunnel.type());
        assertEquals(new IndividualAddress(1, 1, 1), tunnel.host());
        assertEquals(2, tunnel.userId());
        assertEquals(Set.of(), tunnel.linkedSecuredGroups());
        assertEquals(new IndividualAddress(1, 0, 128), facts.interfaces().get(0).address(), "sorted by address");
        assertEquals(new IndividualAddress(1, 1, 10), facts.interfaces().get(9).address());
    }

    @Test
    void backboneInterfaceCarriesItsLinks() {
        KeyringFacts facts = new KeyringFacts(loadWithoutPassword("openhab6.knxkeys"));

        InterfaceFacts backbone = facts.interfaceOf(new IndividualAddress(1, 0, 128)).orElseThrow();
        assertEquals(Type.Backbone, backbone.type());
        assertNull(backbone.host());
        assertEquals(0, backbone.userId());
        assertEquals(Set.of(LINKED_TO_BACKBONE_1, LINKED_TO_BACKBONE_2), backbone.linkedSecuredGroups());
        assertTrue(facts.isLinked(new IndividualAddress(1, 0, 128), LINKED_TO_BACKBONE_1));
        assertFalse(facts.isLinked(new IndividualAddress(1, 0, 128), LINKED_TO_NOBODY));
        assertFalse(facts.isLinked(new IndividualAddress(1, 1, 2), LINKED_TO_BACKBONE_1));
        assertFalse(facts.isLinked(new IndividualAddress(1, 1, 99), LINKED_TO_BACKBONE_1));
    }

    @Test
    void securedGroupAddressesAreTheOnesWithAKey() {
        KeyringFacts facts = new KeyringFacts(loadWithoutPassword("openhab6.knxkeys"));

        assertEquals(Set.of(LINKED_TO_BACKBONE_1, LINKED_TO_BACKBONE_2, LINKED_TO_NOBODY),
                facts.securedGroupAddresses());
    }

    @Test
    void backboneKeyIsReportedAsPresentOrNot() {
        assertTrue(new KeyringFacts(loadWithoutPassword("openhab6.knxkeys")).hasBackboneKey());
        assertTrue(new KeyringFacts(loadWithoutPassword("openhab6-minimal-sipr.knxkeys")).hasBackboneKey());
        assertFalse(new KeyringFacts(loadWithoutPassword("openhab6-minimal-sipif.knxkeys")).hasBackboneKey());
        assertFalse(new KeyringFacts(loadWithoutPassword("openhab6-minimal-ipif.knxkeys")).hasBackboneKey());
    }

    @Test
    void keyringWithoutInterfacesOrGroups() {
        KeyringFacts facts = new KeyringFacts(loadWithoutPassword("openhab6-minimal-ipif.knxkeys"));

        assertEquals(List.of(), facts.interfaces());
        assertEquals(Set.of(), facts.securedGroupAddresses());
        assertEquals(Optional.empty(), facts.interfaceOf(new IndividualAddress(1, 1, 2)));
    }

    @Test
    void signatureIsCheckedAgainstTheGivenPassword() {
        KeyringFacts facts = new KeyringFacts(loadWithoutPassword("openhab6.knxkeys"));

        assertTrue(facts.signatureMatches("habopen".toCharArray()));
        assertFalse(facts.signatureMatches("wrong".toCharArray()));
        assertFalse(facts.signatureMatches(new char[0]));
    }

    @Test
    void linksToAddressesWithoutAKeyDoNotCount(@TempDir Path dir) throws IOException {
        String interfaces = """
                <Interface IndividualAddress="7.7.250" Type="USB">
                <Group Address="%d" Senders="7.7.7" />
                <Group Address="%d" />
                </Interface>
                <Interface IndividualAddress="7.7.251" Type="Tunneling" Host="7.7.0" UserID="2" />
                """.formatted(KeyringTestData.rawGroup(9, 0, 0), KeyringTestData.rawGroup(9, 0, 1));
        KeyringFacts facts = new KeyringFacts(
                KeyringTestData.synthetic(dir, interfaces, KeyringTestData.groupWithKey(9, 0, 0)));

        assertEquals(Set.of(new GroupAddress(9, 0, 0)),
                facts.interfaceOf(new IndividualAddress(7, 7, 250)).orElseThrow().linkedSecuredGroups());
        assertEquals(Type.Usb, facts.interfaceOf(new IndividualAddress(7, 7, 250)).orElseThrow().type());
        assertEquals(new IndividualAddress(7, 7, 0),
                facts.interfaceOf(new IndividualAddress(7, 7, 251)).orElseThrow().host());
        assertEquals(Set.of(new GroupAddress(9, 0, 0)), facts.securedGroupAddresses());
    }
}
