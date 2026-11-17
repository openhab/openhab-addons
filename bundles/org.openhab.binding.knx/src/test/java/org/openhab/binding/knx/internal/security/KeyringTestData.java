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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

import io.calimero.secure.Keyring;

/**
 * Keyrings for tests: the public ones from the test resources, and made-up ones for what they do not cover.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class KeyringTestData {
    public static final String PASSWORD = "habopen";

    private KeyringTestData() {
    }

    /** Loads a public test keyring from the test resources, without its password. */
    @SuppressWarnings("null")
    public static Keyring load(String name) {
        return Keyring
                .load(KeyringTestData.class.getClassLoader().getResource("misc" + File.separator + name).toString());
    }

    /** The text of a public test keyring file. */
    @SuppressWarnings("null")
    public static String fileContent(String name) throws IOException, URISyntaxException {
        return Files.readString(
                new File(KeyringTestData.class.getClassLoader().getResource("misc" + File.separator + name).toURI())
                        .toPath(),
                StandardCharsets.UTF_8);
    }

    /** The folder that holds the test resources' {@code misc} folder, to use as the openHAB config folder. */
    @SuppressWarnings("null")
    public static String configFolder() throws URISyntaxException {
        return Objects.requireNonNull(
                new File(KeyringTestData.class.getClassLoader().getResource("misc").toURI()).getParent());
    }

    /** A keyring made up for the test; its keys and signature are filler and only readable without a password. */
    public static Keyring synthetic(Path dir, String interfaces, String groups) throws IOException {
        Path file = dir.resolve("made-up.knxkeys");
        String xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <Keyring Project="made up" CreatedBy="test" Created="2026-01-01T00:00:00Z" \
                Signature="AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=" xmlns="http://knx.org/xml/keyring/1">
                %s
                <GroupAddresses>
                %s
                </GroupAddresses>
                </Keyring>
                """.formatted(interfaces, groups);
        Files.writeString(file, xml, StandardCharsets.UTF_8);
        return Keyring.load(file.toUri().toString());
    }

    /** A {@code Group} element of the group address list, with a filler key. */
    public static String groupWithKey(int main, int middle, int sub) {
        return "<Group Address=\"" + rawGroup(main, middle, sub) + "\" Key=\"AAAAAAAAAAAAAAAAAAAAAA==\" />";
    }

    /** The raw value ETS writes for a group address. */
    public static int rawGroup(int main, int middle, int sub) {
        return (main << 11) | (middle << 8) | sub;
    }
}
