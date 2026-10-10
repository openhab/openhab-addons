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
package org.openhab.binding.shelly.internal.api2;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2AuthChallenge;

/**
 * Covers parsing of the WWW-Authenticate challenge of Gen2+ devices, whose parameter order differs between devices.
 *
 * @author Gerhard Braun - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
public class Shelly2ApiClientAuthChallengeTest {

    @Test
    void qopFirst() {
        Shelly2AuthChallenge challenge = Shelly2ApiClient.parseAuthChallenge(
                "Digest qop=\"auth\", realm=\"shellypro4pm-f008d1d8b8b8\", nonce=\"1698245525\", algorithm=SHA-256");

        assertEquals(SHELLY2_AUTHTTYPE_DIGEST, challenge.authType);
        assertEquals("shellypro4pm-f008d1d8b8b8", challenge.realm);
        assertEquals("1698245525", challenge.nonce);
        assertEquals(SHELLY2_AUTHALG_SHA256, challenge.algorithm);
    }

    @Test
    void realmFirstAsSentByWallDisplay() {
        Shelly2AuthChallenge challenge = Shelly2ApiClient.parseAuthChallenge(
                "Digest realm=\"ShellyWallDisplay-00082214DC9F\", qop=\"auth\", nonce=\"cba5e8d9\", opaque=\"\", algorithm=SHA-256");

        assertEquals(SHELLY2_AUTHTTYPE_DIGEST, challenge.authType);
        assertEquals("ShellyWallDisplay-00082214DC9F", challenge.realm);
        assertEquals("cba5e8d9", challenge.nonce);
        assertEquals(SHELLY2_AUTHALG_SHA256, challenge.algorithm);
    }

    @Test
    void schemeIsCaseInsensitiveAndQuotedAlgorithmAccepted() {
        Shelly2AuthChallenge challenge = Shelly2ApiClient
                .parseAuthChallenge("digest nonce=\"abc\", realm=\"dev\", algorithm=\"SHA-256\"");

        assertEquals(SHELLY2_AUTHTTYPE_DIGEST, challenge.authType);
        assertEquals("abc", challenge.nonce);
        assertEquals("dev", challenge.realm);
        assertEquals(SHELLY2_AUTHALG_SHA256, challenge.algorithm);
    }

    @Test
    void otherSchemeIsNotDigest() {
        Shelly2AuthChallenge challenge = Shelly2ApiClient.parseAuthChallenge("Basic realm=\"dev\"");

        assertNull(challenge.authType);
    }

    @Test
    void bodyChallengeAsSentByWallDisplay() {
        Shelly2AuthChallenge challenge = Shelly2ApiClient.parseBodyAuthChallenge(
                "{\"code\":401,\"message\":\"{\\\"auth_type\\\":\\\"digest\\\",\\\"nonce\\\":1791588805,\\\"nc\\\":\\\"1\\\",\\\"realm\\\":\\\"ShellyWallDisplay-00082214DC9F\\\",\\\"algorithm\\\":\\\"SHA-256\\\"}\"}");

        assertNotNull(challenge);
        assertEquals(SHELLY2_AUTHTTYPE_DIGEST, challenge.authType);
        assertEquals("1791588805", challenge.nonce);
        assertEquals("ShellyWallDisplay-00082214DC9F", challenge.realm);
        assertEquals(SHELLY2_AUTHALG_SHA256, challenge.algorithm);
    }

    @Test
    void bodyChallengeWrappedInErrorObjectIsNotUsed() {
        // other Gen2 devices wrap it in "error", their Authorization header is accepted, so nothing changes for them
        assertNull(Shelly2ApiClient.parseBodyAuthChallenge(
                "{\"error\":{\"code\":401,\"message\":\"{\\\"auth_type\\\":\\\"digest\\\",\\\"nonce\\\":1,\\\"realm\\\":\\\"shelly\\\"}\"}}"));
    }

    @Test
    void bodyWithoutChallengeIsIgnored() {
        assertNull(Shelly2ApiClient.parseBodyAuthChallenge(null));
        assertNull(Shelly2ApiClient.parseBodyAuthChallenge(""));
        assertNull(Shelly2ApiClient.parseBodyAuthChallenge("not json"));
        assertNull(Shelly2ApiClient.parseBodyAuthChallenge("{\"id\":1,\"result\":{}}"));
        assertNull(Shelly2ApiClient.parseBodyAuthChallenge("{\"code\":500,\"message\":\"{}\"}"));
        assertNull(Shelly2ApiClient.parseBodyAuthChallenge("{\"code\":401,\"message\":\"no json\"}"));
    }
}
