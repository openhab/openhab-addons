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
package org.openhab.binding.salus.internal.aws.http;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import java.security.MessageDigest;
import java.time.ZonedDateTime;
import java.util.HexFormat;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import software.amazon.awssdk.crt.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.crt.auth.signing.AwsSigningConfig;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpRequest;

/**
 * @author Martin Grześlowski - Initial contribution
 * @author Martin Grześlowski - Verify shadow read and write signatures
 */
@NonNullByDefault
class AwsSignerTest {
    @ParameterizedTest
    @ValueSource(strings = { "GET", "POST" })
    void shouldGenerateSameSignatureHeadersAsAWSSDK(String method) throws Exception {
        var path = "/things/test-device/shadow";
        var host = "example-ats.iot.eu-central-1.amazonaws.com";
        var time = ZonedDateTime.parse("2026-10-19T10:00:00Z");
        var credentials = new CogitoCredentials("example-access-key", "example-secret-key", "example-session-token");
        var body = "POST".equals(method)
                ? "{\"state\":{\"desired\":{\"11\":{\"properties\":{\"ep9:sIT600TH:SetHeatingSetpoint_x100\":2150}}}}}"
                : null;

        assertThat(AwsSigner.sign(method, host, path, time, credentials, "eu-central-1", "iotdevicegateway", body))
                .isEqualTo(rawAwsSign(method, host, path, time, credentials, body));
    }

    private static Map<String, String> rawAwsSign(String method, String host, String path, ZonedDateTime time,
            CogitoCredentials credentials, @Nullable String body) throws Exception {
        var request = new HttpRequest(method, path, new HttpHeader[] { new HttpHeader("host", host) }, null);
        try (var provider = new StaticCredentialsProvider.StaticCredentialsProviderBuilder()
                .withAccessKeyId(credentials.accessKeyId().getBytes(UTF_8))
                .withSecretAccessKey(credentials.secretKey().getBytes(UTF_8))
                .withSessionToken(credentials.sessionToken().getBytes(UTF_8)).build();
                var config = new AwsSigningConfig()) {
            config.setRegion("eu-central-1");
            config.setService("iotdevicegateway");
            config.setCredentialsProvider(provider);
            config.setTime(time.toInstant().toEpochMilli());
            config.setSignedBodyValue(HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest((body == null ? "" : body).getBytes(UTF_8))));
            return software.amazon.awssdk.crt.auth.signing.AwsSigner.sign(request, config).get(10, SECONDS)
                    .getSignedRequest().getHeaders().stream()
                    .collect(Collectors.toMap(HttpHeader::getName, HttpHeader::getValue));
        }
    }
}
