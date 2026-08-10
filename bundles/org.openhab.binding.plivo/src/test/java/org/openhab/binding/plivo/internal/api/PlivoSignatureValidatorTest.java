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
package org.openhab.binding.plivo.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PlivoSignatureValidator}.
 * <p>
 * The reference signatures below were produced with Plivo's published V3 algorithm (the same
 * {@code construct_post_url} / {@code get_signature_v3} logic used by the plivo-python SDK):
 * take the URL without its query string, append {@code ?}, then the sorted query parameters as
 * {@code key=value} pairs joined by {@code &} plus a {@code .} when a query is present, then the
 * sorted POST body parameters as a {@code key}+{@code value} concatenation, then {@code .} and the
 * nonce, and finally {@code Base64(HMAC-SHA256(authToken, ...))}.
 * <p>
 * The V2 reference signature was produced with Plivo's V2 algorithm ({@code validate_signature} in
 * the plivo-python SDK): the URL with its query string removed, followed by the nonce, then
 * {@code Base64(HMAC-SHA256(authToken, ...))}.
 *
 * @author Sarvesh Patil - Initial contribution
 */
@NonNullByDefault
public class PlivoSignatureValidatorTest {

    private static final String AUTH_TOKEN = "my_auth_token";
    private static final String NONCE = "12345";
    private static final Map<String, String> PARAMS = Map.of("From", "+14156667777", "To", "+14158889999", "CallUUID",
            "abcd-1234");

    private static final String URL_NO_QUERY = "https://example.com/answer";
    private static final String URL_WITH_QUERY = "https://example.com/answer?token=xyz";

    // Reference V3 signatures for the values above (canonical strings:
    // https://example.com/answer?CallUUIDabcd-1234From+14156667777To+14158889999.12345 and
    // https://example.com/answer?token=xyz.CallUUIDabcd-1234From+14156667777To+14158889999.12345).
    private static final String SIG_NO_QUERY = "QUFNUHBhaE5m20p3UFtVHCb66PSliiukfx8M6MyiJwk=";
    private static final String SIG_WITH_QUERY = "IryQ8ucGk4J35wJRxh5K0iVvDCSZpHflFICcZxwx7Cc=";

    // Reference V2 signature (canonical string https://example.com/answer12345). Plivo V2 strips the
    // query string, so both URLs above produce this same signature.
    private static final String SIG_V2 = "JTLtnh4xskVxvDEWnLeMYs8IkO4HTZJ8JPdRAmNGgso=";

    @Test
    public void computeSignatureMatchesPlivoReferenceWithoutQuery() {
        assertEquals(SIG_NO_QUERY, PlivoSignatureValidator.computeV3Signature(URL_NO_QUERY, PARAMS, NONCE, AUTH_TOKEN));
    }

    @Test
    public void computeSignatureMatchesPlivoReferenceWithQuery() {
        assertEquals(SIG_WITH_QUERY,
                PlivoSignatureValidator.computeV3Signature(URL_WITH_QUERY, PARAMS, NONCE, AUTH_TOKEN));
    }

    @Test
    public void validateAcceptsMatchingSignature() {
        assertTrue(PlivoSignatureValidator.validateV3(URL_NO_QUERY, PARAMS, SIG_NO_QUERY, NONCE, AUTH_TOKEN));
        assertTrue(PlivoSignatureValidator.validateV3(URL_WITH_QUERY, PARAMS, SIG_WITH_QUERY, NONCE, AUTH_TOKEN));
    }

    @Test
    public void validateAcceptsSignatureAmongCommaSeparatedList() {
        String header = "notavalidsignaturevalue==," + SIG_NO_QUERY;
        assertTrue(PlivoSignatureValidator.validateV3(URL_NO_QUERY, PARAMS, header, NONCE, AUTH_TOKEN));
    }

    @Test
    public void validateRejectsWrongSignature() {
        assertFalse(PlivoSignatureValidator.validateV3(URL_NO_QUERY, PARAMS, SIG_WITH_QUERY, NONCE, AUTH_TOKEN));
    }

    @Test
    public void validateRejectsWrongAuthToken() {
        assertFalse(PlivoSignatureValidator.validateV3(URL_NO_QUERY, PARAMS, SIG_NO_QUERY, NONCE, "wrong_token"));
    }

    @Test
    public void validateRejectsMissingSignatureOrNonce() {
        assertFalse(PlivoSignatureValidator.validateV3(URL_NO_QUERY, PARAMS, null, NONCE, AUTH_TOKEN));
        assertFalse(PlivoSignatureValidator.validateV3(URL_NO_QUERY, PARAMS, SIG_NO_QUERY, null, AUTH_TOKEN));
        assertFalse(PlivoSignatureValidator.validateV3(URL_NO_QUERY, PARAMS, "  ", NONCE, AUTH_TOKEN));
    }

    @Test
    public void computeV2SignatureMatchesPlivoReferenceAndIgnoresQuery() {
        assertEquals(SIG_V2, PlivoSignatureValidator.computeV2Signature(URL_NO_QUERY, NONCE, AUTH_TOKEN));
        assertEquals(SIG_V2, PlivoSignatureValidator.computeV2Signature(URL_WITH_QUERY, NONCE, AUTH_TOKEN));
    }

    @Test
    public void validateV2AcceptsMatchingSignature() {
        assertTrue(PlivoSignatureValidator.validateV2(URL_NO_QUERY, SIG_V2, NONCE, AUTH_TOKEN));
        assertTrue(PlivoSignatureValidator.validateV2(URL_WITH_QUERY, SIG_V2, NONCE, AUTH_TOKEN));
    }

    @Test
    public void validateV2RejectsWrongSignatureTokenOrMissing() {
        assertFalse(PlivoSignatureValidator.validateV2(URL_NO_QUERY, "wrongsignature==", NONCE, AUTH_TOKEN));
        assertFalse(PlivoSignatureValidator.validateV2(URL_NO_QUERY, SIG_V2, NONCE, "wrong_token"));
        assertFalse(PlivoSignatureValidator.validateV2(URL_NO_QUERY, null, NONCE, AUTH_TOKEN));
        assertFalse(PlivoSignatureValidator.validateV2(URL_NO_QUERY, SIG_V2, null, AUTH_TOKEN));
    }
}
