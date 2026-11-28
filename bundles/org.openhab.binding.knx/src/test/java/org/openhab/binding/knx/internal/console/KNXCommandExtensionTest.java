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
package org.openhab.binding.knx.internal.console;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.knx.internal.factory.KNXHandlerFactory;
import org.openhab.binding.knx.internal.handler.IPBridgeThingHandler;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore;
import org.openhab.binding.knx.internal.security.KeyringFacts;
import org.openhab.binding.knx.internal.security.KeyringTestData;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.io.console.Console;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;

import io.calimero.IndividualAddress;

/**
 * Checks the {@code keyring} console command.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KNXCommandExtensionTest {
    private static final String KEYRING = "openhab6.knxkeys";
    private static final String BACKBONE_KEY_IN_THE_TEST_KEYRING = "D947B12DDECAD528B1D5A88FD347F284";

    private final KNXHandlerFactory factory = mock(KNXHandlerFactory.class);
    private final Console console = mock(Console.class);
    private final KNXCommandExtension extension = new KNXCommandExtension(factory,
            new KnxProjectStore(Path.of("target", "no-such-knx-folder"), Clock.systemUTC()),
            mock(LocaleProvider.class));

    private KNXBridgeBaseThingHandler bridge(String id, @Nullable String label, @Nullable KeyringFacts facts,
            String fileName, boolean signatureValid, @Nullable IndividualAddress own) {
        IPBridgeThingHandler handler = mock(IPBridgeThingHandler.class);
        Bridge thing = mock(Bridge.class);
        when(thing.getUID()).thenReturn(new ThingUID("knx", "ip", id));
        when(thing.getLabel()).thenReturn(label);
        when(handler.getThing()).thenReturn(thing);
        when(handler.getKeyringFacts()).thenReturn(facts);
        when(handler.getKeyringFileName()).thenReturn(fileName);
        when(handler.isKeyringSignatureValid()).thenReturn(signatureValid);
        when(handler.getOwnAddress()).thenReturn(Optional.ofNullable(own));
        return handler;
    }

    private KNXBridgeBaseThingHandler withKeyring(boolean signatureValid, @Nullable IndividualAddress own) {
        return bridge("home", "Home KNX", new KeyringFacts(KeyringTestData.load(KEYRING)), KEYRING, signatureValid,
                own);
    }

    private KNXBridgeBaseThingHandler withoutKeyring() {
        return bridge("plain", "Plain bridge", null, "", false, null);
    }

    private void bridges(KNXBridgeBaseThingHandler... handlers) {
        when(factory.getBridges()).thenReturn(Set.of(handlers));
    }

    private List<String> run(String... args) {
        extension.execute(args, console);
        ArgumentCaptor<String> lines = ArgumentCaptor.forClass(String.class);
        verify(console, atLeast(0)).println(lines.capture());
        return new ArrayList<>(lines.getAllValues());
    }

    @Test
    void withoutArgumentEveryBridgeWithAKeyringIsListed() throws Exception {
        bridges(withoutKeyring(), withKeyring(true, new IndividualAddress(1, 1, 2)));

        List<String> out = run("keyring");

        assertEquals("KNX bridge \"Home KNX\" (knx:ip:home)", out.get(0));
        assertTrue(out.contains("  keyring file: " + KEYRING), out.toString());
        assertTrue(out.contains("  signature: valid for the configured password"), out.toString());
        assertTrue(out.contains("  backbone key: present"), out.toString());
        assertTrue(out.contains("  secured group addresses: 3"), out.toString());
        assertTrue(out.contains("  interfaces: 17"), out.toString());
        assertFalse(String.join("\n", out).contains("Plain bridge"));
    }

    @Test
    void oneLinePerInterfaceWithTheOwnAddressMarked() throws Exception {
        bridges(withKeyring(true, new IndividualAddress(1, 1, 2)));

        List<String> out = run("keyring");

        List<String> interfaces = out.stream().filter(l -> l.startsWith("    1.")).toList();
        assertEquals(17, interfaces.size());
        assertEquals("    1.0.128  Backbone   host -         user -    linked secured group addresses: 2",
                interfaces.get(0));
        assertEquals(
                "    1.1.2    Tunneling  host 1.1.1     user 2    linked secured group addresses: 0  <- openHAB sends from here",
                interfaces.get(1));
        assertEquals(1, interfaces.stream().filter(l -> l.contains("<-")).count());
    }

    @Test
    void noMarkWithoutConnection() throws Exception {
        bridges(withKeyring(true, null));

        assertTrue(run("keyring").stream().noneMatch(l -> l.contains("<-")));
    }

    @Test
    void wrongPasswordIsSaidSo() throws Exception {
        bridges(withKeyring(false, null));

        List<String> out = run("keyring");

        assertTrue(out.contains("  signature: NOT valid for the configured password"), out.toString());
    }

    @Test
    void aBridgeIsPickedByUidIdOrLabel() throws Exception {
        bridges(withoutKeyring(), withKeyring(true, null));

        for (String name : new String[] { "knx:ip:home", "home", "Home KNX", "HOME" }) {
            assertEquals("KNX bridge \"Home KNX\" (knx:ip:home)", run("keyring", name).get(0), name);
            clearInvocations(console);
        }
    }

    @Test
    void aBridgeWithoutKeyringSaysSo() throws Exception {
        bridges(withoutKeyring());

        assertEquals(List.of("KNX bridge \"Plain bridge\" (knx:ip:plain) has no keyring loaded."),
                run("keyring", "plain"));
    }

    @Test
    void unknownBridgeAndEmptyListAreSaid() throws Exception {
        bridges(withoutKeyring());

        assertEquals(List.of("No KNX bridge has a keyring loaded."), run("keyring"));
        clearInvocations(console);
        assertEquals(List.of("No KNX bridge \"nope\". Known bridges: knx:ip:plain"), run("keyring", "nope"));
    }

    @Test
    void outputHoldsNoPasswordKeyOrHash() throws Exception {
        bridges(withKeyring(true, new IndividualAddress(1, 1, 2)));

        String output = String.join("\n", run("keyring")).toLowerCase(java.util.Locale.ROOT);

        String xml = KeyringTestData.fileContent(KEYRING);
        Matcher secret = Pattern
                .compile("(?:Key|Password|Authentication|ToolKey|ManagementPassword|Signature)=\"([^\"]+)\"")
                .matcher(xml);
        int checked = 0;
        int checkedAsHex = 0;
        while (secret.find()) {
            assertFalse(output.contains(secret.group(1).toLowerCase(java.util.Locale.ROOT)),
                    "a secret value is printed");
            checked++;
            byte[] bytes;
            try {
                bytes = Base64.getDecoder().decode(secret.group(1));
            } catch (IllegalArgumentException e) {
                continue;
            }
            if (bytes.length >= 8) {
                assertFalse(output.contains(HexFormat.of().formatHex(bytes)), "a secret value is printed as hex");
                checkedAsHex++;
            }
        }
        assertTrue(checked > 40, "the keyring file has many secret values, found " + checked);
        assertTrue(checkedAsHex > 40, "the keyring file has many secret values, found " + checkedAsHex + " as hex");
        assertFalse(output.contains(KeyringTestData.PASSWORD));
        assertFalse(output.contains(BACKBONE_KEY_IN_THE_TEST_KEYRING.toLowerCase(java.util.Locale.ROOT)));
    }

    @Test
    void usageAndCompleterKnowTheCommand() throws IOException {
        bridges(withKeyring(true, null), withoutKeyring());

        assertTrue(extension.getUsages().stream().anyMatch(u -> u.contains("keyring [<bridge>]")),
                extension.getUsages().toString());

        List<String> candidates = new ArrayList<>();
        assertTrue(extension.complete(new String[] { "ke" }, 0, 2, candidates));
        assertTrue(candidates.contains("keyring "), candidates.toString());

        candidates.clear();
        assertTrue(extension.complete(new String[] { "keyring", "" }, 1, 0, candidates));
        assertEquals(List.of("knx:ip:home ", "knx:ip:plain "), candidates.stream().sorted().toList());

        candidates.clear();
        assertFalse(extension.complete(new String[] { "list-unknown-ga", "" }, 1, 0, candidates));
    }

    @Test
    void unknownCommandPrintsUsage() {
        extension.execute(new String[] { "keyring", "a", "b" }, console);

        verify(console, atLeastOnce()).printUsage(anyString());
    }
}
