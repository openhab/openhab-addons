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
package org.openhab.binding.tplinksmarthome.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tplinkbridge.TPLinkCredentials;

/**
 * Tests for {@link KlapConnection}.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public class KlapConnectionTest {

    @Test
    public void testTriesAccountThenSetupKeysThenDefaults() {
        TPLinkCredentials account = new TPLinkCredentials(" User@Example.com", "secret");

        assertEquals(
                List.of(account, new TPLinkCredentials(" User@Example.com", "kasaSetup"),
                        new TPLinkCredentials("user@example.com", "kasaSetup"),
                        new TPLinkCredentials("kasa@tp-link.net", "kasaSetup"),
                        new TPLinkCredentials("test@tp-link.net", "test"), new TPLinkCredentials("", "")),
                KlapConnection.candidates(account));
    }

    @Test
    public void testDoesNotRepeatTheSetupKeyForANormalizedEmail() {
        TPLinkCredentials account = new TPLinkCredentials("user@example.com", "secret");

        assertEquals(
                List.of(account, new TPLinkCredentials("user@example.com", "kasaSetup"),
                        new TPLinkCredentials("kasa@tp-link.net", "kasaSetup"),
                        new TPLinkCredentials("test@tp-link.net", "test"), new TPLinkCredentials("", "")),
                KlapConnection.candidates(account));
    }

    @Test
    public void testTriesNothingWithoutAccountCredentials() {
        assertEquals(List.of(), KlapConnection.candidates(new TPLinkCredentials("", "")));
    }
}
