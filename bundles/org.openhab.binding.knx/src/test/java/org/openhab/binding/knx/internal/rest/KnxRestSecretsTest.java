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
package org.openhab.binding.knx.internal.rest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openhab.binding.knx.internal.handler.IPBridgeThingHandler;
import org.openhab.binding.knx.internal.monitor.ConnectionKind;
import org.openhab.binding.knx.internal.monitor.NotDecryptedReason;
import org.openhab.binding.knx.internal.monitor.SecurityState;
import org.openhab.binding.knx.internal.monitor.SendOutcome;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor;
import org.openhab.binding.knx.internal.monitor.TelegramType;
import org.openhab.binding.knx.internal.project.ProjectTestFiles;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore;
import org.openhab.binding.knx.internal.rest.dto.ChannelRequestDTO;
import org.openhab.binding.knx.internal.rest.dto.DeviceDTO;
import org.openhab.binding.knx.internal.rest.dto.GroupAddressDTO;
import org.openhab.binding.knx.internal.rest.dto.PreviewRequestDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposalDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposalRequestDTO;
import org.openhab.binding.knx.internal.security.KeyringFacts;
import org.openhab.binding.knx.internal.security.KeyringTestData;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;
import io.calimero.secure.Keyring;

/**
 * Checks that no read endpoint answers a key, a password or an authentication code: a bridge with the public test
 * keyring loaded and secrets in its configuration, the protected made-up project and a monitor with rows; every
 * answer is written as JSON and searched for every secret value, as written, as hex and decrypted.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxRestSecretsTest {
    private static final String TUNNEL_PASSWORD = "made-up-tunnel-secret";
    private static final String BACKBONE_KEY = "0f1e2d3c4b5a69788796a5b4c3d2e1f0";
    private static final IndividualAddress SENSOR = new IndividualAddress(7, 7, 20);

    private @TempDir Path folder = Path.of("");

    /** Every answer of every read endpoint, as JSON. */
    private String answers() throws Exception {
        KnxProjectStore store = new KnxProjectStore(folder, Clock.systemUTC());
        char[] password = ProjectTestFiles.PASSWORD.toCharArray();
        store.importProject(RestFixtures.uid("home"), ProjectTestFiles.protectedProject(), password, null,
                "made-up.knxproj");

        Keyring keyring = KeyringTestData.load("openhab6.knxkeys");
        KeyringFacts facts = new KeyringFacts(keyring);
        TelegramMonitor monitor = new TelegramMonitor();
        monitor.useProject(store.lookup(RestFixtures.uid("home")));
        IndividualAddress own = facts.interfaces().get(0).address();
        monitor.connected(ConnectionKind.SECURE_TUNNEL, own);
        GroupAddress secured = facts.securedGroupAddresses().iterator().next();
        monitor.received(SENSOR, new GroupAddress(31, 7, 2), TelegramType.WRITE, new byte[] { 0x0c, 0x33 },
                SecurityState.PLAIN);
        monitor.received(SENSOR, secured, TelegramType.WRITE, new byte[] { 1 }, SecurityState.SECURED);
        monitor.received(SENSOR, secured, TelegramType.WRITE, new byte[] { 0 }, SecurityState.PLAIN, true);
        monitor.notDecrypted(SENSOR, secured, NotDecryptedReason.AUTHENTICATION, address -> true);
        monitor.sent(own, new GroupAddress(31, 7, 1), TelegramType.WRITE, new byte[] { 1 }, "1.001", null,
                SecurityState.SECURED, SendOutcome.SENT);

        Bridge bridge = RestFixtures.bridgeThing("home", Map.of("keyringFile", "openhab6.knxkeys", "keyringPassword",
                KeyringTestData.PASSWORD, "tunnelUserPassword", TUNNEL_PASSWORD, "routerBackboneKey", BACKBONE_KEY));
        IPBridgeThingHandler handler = RestFixtures.handler(bridge, monitor);
        when(handler.getKeyringFacts()).thenReturn(facts);
        when(handler.getKeyringFileName()).thenReturn("openhab6.knxkeys");
        when(handler.isKeyringSignatureValid()).thenReturn(true);
        when(handler.getOwnAddress()).thenReturn(Optional.of(own));
        KnxRestService service = new KnxRestService(RestFixtures.factory(handler), store, mock(ThingRegistry.class),
                mock(ItemRegistry.class), mock(ItemChannelLinkRegistry.class));

        String b = "knx:ip:home";
        List<Object> answers = new ArrayList<>(List.of(service.bridges(), service.bridge(b), service.monitor(b),
                service.telegrams(b, Map.of(), "5000", null, null), service.lastValues(b), service.keyring(b),
                service.project(b), service.groupAddresses(b, null, null), service.devices(b, null, null),
                service.topology(b), service.locations(b), service.check(b)));
        for (DeviceDTO device : service.devices(b, null, null).items) {
            answers.add(service.device(b, device.id));
            answers.add(service.deviceChannels(b, device.id));
        }
        ChannelRequestDTO request = new ChannelRequestDTO();
        request.groupAddresses = service.groupAddresses(b, null, null).items.stream()
                .map((GroupAddressDTO address) -> address.address).toList();
        answers.add(service.channels(b, request));
        ProposalRequestDTO selection = new ProposalRequestDTO();
        selection.groupAddresses = request.groupAddresses;
        selection.devices = service.devices(b, null, null).items.stream().map((DeviceDTO device) -> device.id).toList();
        ProposalDTO proposal = service.proposal(b, selection);
        answers.add(proposal);
        PreviewRequestDTO preview = new PreviewRequestDTO();
        preview.things = proposal.things;
        preview.roomGroups = proposal.rooms;
        preview.readAtStart = proposal.readAtStart;
        answers.add(service.preview(b, preview));
        StringBuilder json = new StringBuilder();
        answers.forEach(answer -> json.append(RestFixtures.GSON.toJson(answer)).append('\n'));
        return json.toString();
    }

    /** {@return the secrets in text form: as written in the file, as hex, and decrypted} */
    static List<String> secrets() throws Exception {
        List<String> secrets = new ArrayList<>(
                List.of(TUNNEL_PASSWORD, BACKBONE_KEY, KeyringTestData.PASSWORD, ProjectTestFiles.PASSWORD, "MADEUP-"));
        secrets.addAll(ProjectTestFiles.madeUpValues());
        String xml = KeyringTestData.fileContent("openhab6.knxkeys");
        Matcher secret = Pattern
                .compile("(?:Key|Password|Authentication|ToolKey|ManagementPassword|Signature)=\"([^\"]+)\"")
                .matcher(xml);
        while (secret.find()) {
            secrets.add(secret.group(1));
            byte[] bytes;
            try {
                bytes = Base64.getDecoder().decode(secret.group(1));
            } catch (IllegalArgumentException e) {
                continue;
            }
            if (bytes.length >= 8) {
                secrets.add(HexFormat.of().formatHex(bytes));
                secrets.add(HexFormat.ofDelimiter(" ").formatHex(bytes));
            }
        }
        Keyring keyring = KeyringTestData.load("openhab6.knxkeys");
        char[] password = KeyringTestData.PASSWORD.toCharArray();
        for (byte[] key : keyring.groups().values()) {
            secrets.add(HexFormat.of().formatHex(keyring.decryptKey(key, password)));
        }
        keyring.backbone().flatMap(Keyring.Backbone::groupKey)
                .ifPresent(key -> secrets.add(HexFormat.of().formatHex(keyring.decryptKey(key, password))));
        List<Optional<byte[]>> encryptedPasswords = new ArrayList<>();
        keyring.interfaces().values().forEach(interfaces -> interfaces.forEach(iface -> {
            encryptedPasswords.add(iface.password());
            encryptedPasswords.add(iface.authentication());
        }));
        keyring.devices().values().forEach(device -> {
            encryptedPasswords.add(device.password());
            encryptedPasswords.add(device.authentication());
        });
        for (Optional<byte[]> encrypted : encryptedPasswords) {
            if (encrypted.isPresent()) {
                String plain = new String(keyring.decryptPassword(encrypted.get(), password));
                if (plain.length() >= 8) {
                    secrets.add(plain);
                    secrets.add(HexFormat.of().formatHex(plain.getBytes(StandardCharsets.UTF_8)));
                }
            }
        }
        return secrets;
    }

    @Test
    void noAnswerHoldsAKeyAPasswordOrAnAuthenticationCode() throws Exception {
        String json = answers().toLowerCase(Locale.ROOT);
        assertTrue(json.contains("made-up switch actuator"), "the answers hold the project");
        assertTrue(json.contains("\"loaded\":true"), "the answers hold the keyring facts");
        assertTrue(json.contains("\"pairing\":"), "the answers hold the proposal");
        assertTrue(json.contains("\"steps\":["), "the answers hold the create steps of the preview");

        List<String> secrets = secrets();
        int checked = 0;
        for (String secret : secrets) {
            if (json.contains(secret.toLowerCase(Locale.ROOT))) {
                fail("an answer holds secret number " + checked);
            }
            checked++;
        }
        assertTrue(checked > 40, "the keyring and the project have many secret values, found " + checked);
    }
}
