/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import software.amazon.awssdk.crt.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.crt.auth.signing.AwsSigningConfig;
import software.amazon.awssdk.crt.http.HttpRequest;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
class AwsSignerTest {
    @Test
    @DisplayName("should generate same signature headers as AWS SDK")
    void shouldGenerateSameSignatureHeadersAsAWSSDK() throws Exception {
        // given
        var pathAndQuery = "/things/xyz/shadow";
        var time = ZonedDateTime.now(ZoneId.of("UTC"));
        var credentials = new CogitoCredentials("AKIAIOSFODNN7EXAMPLE", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY",
                "IQoJb3JpZ2luX2VjEAIaDGV1LWNlbnRyYWwtMSJIMEYCIQDE7EzyAzhN1zhbH6cHEyA3pc0V2wDHnUyPxRd57WwDAQIhAK6exf3NjDynJT68N8oQVzm3HAC0hEKLJDFy/Lq0c2XeKt8ECIv//////////wEQAhoMMDU2NzE2MDkxODE0Igzmsy2iRkqAqUqV4LwqswTGsPbNATSsxQ8epT4uD8xEgdQJ3KANDsqRWPi/u2Nr7oBcnFH0KbqChpSO8FEshdBLpKgCju0VEghg/K0N79qFqvD0fRvij4G8k6zyLsS51y4MpW2TSe0i9rMOSB0yN4I7Gp3a4u96GUiZs/8b+S1wN3H9bTjMeCO7zC0VXWj7icWIv9UckgX9IRaCBj0GQ0Q+oHwzgtVKK4onwWxZO/7r0n39WLIBf0SQHsybWfK3YEj/OwVudsISUWxSfwoBK56PvqxUqUfx9ASKroTaS41K45j9/v7HaKFIp/6RKsP9Ls8jc0kTExar/Ch3ZNfCRK3TLP2XjDe/DfSWr5VdihwF4E3vJQ4L05/rN8lieEZPuWJEbz+8i/EiRBjDgtzl+Rt3R2Esa4bzRfK4UywZjVpUMatMpKk/+MooXaOE8SC8yMWK4GgEorVMcQUJGdZ+KH/3sO5IARplVrOiynwksTIgFIJ1NKIDMfmm966U1q7ClaotOCRt0kqsTXU+0cllAXksc67T0d1Pc4tt7Q+yw/HSyKVZlK1bvQ4LLU1NnUDcJiCUe5Q+A61wWSGjEWxAXjggxhro+1W0gRHgXILZnr/GkM8/kT/UczkAnGb0LFTh1haFlXYgqxlA3SzAiXMDVyzWqD7EOq1S/fSYZ9vrxDJPYuiYVBdDsQDlUGGePdHPmxZBfZC7tnHJkzOgRfijYA7TXfVkAftYLxbldAA/I5Wd6Xlw9OjytBn8MOXNifVZjgsURTDUmPayBjqEAq0ZbjC4sf7hQE+2wwSot9oANqJQq6nB/RitNWtENuEss02L1Fk/GmD+tWW3AVY/+a/8xrN8reyQDSUaKb39UesTxaBQ7/MdJQNgGkdIVmSF7rBedOXzjqaqvqLylQR1NnsVl3veAnsmGnE03m0punceAxH2V0S6iAcjYyMwVBeTYpJ3jQbEYvvtQqyoo7koiR2MkdqSD5YND5D8CoaWlWPvI4Oy326srm2eeQVpALyKzEu5XKWL45mnYpLLFDYzAdErjkuMDY6tBZIKnADSoPPj17fbjVFOwL44c1xXKkA7xvaMATCeNl3pkwxHCg1LpXW2vVkzWE/jB2NNYZmHjayb8x1G");
        var region = "eu-central-1";

        // when
        var sign = AwsSigner.sign(pathAndQuery, time, credentials, region, "iotdevicegateway", null);

        // then
        assertThat(sign).isEqualTo(rawAwsSign(pathAndQuery, time, credentials, region));
    }

    public static Map<String, String> rawAwsSign(String pathAndQuery, ZonedDateTime time,
            CogitoCredentials cogitoCredentials, String region) throws Exception {
        HttpRequest httpRequest = new HttpRequest("GET", pathAndQuery,
                new software.amazon.awssdk.crt.http.HttpHeader[] {
                        new software.amazon.awssdk.crt.http.HttpHeader("host", "") },
                null);
        var localCredentials = requireNonNull(cogitoCredentials);
        try (var config = new AwsSigningConfig()) {
            config.setRegion(region);
            config.setService("iotdevicegateway");
            config.setCredentialsProvider(new StaticCredentialsProvider.StaticCredentialsProviderBuilder()
                    .withAccessKeyId(localCredentials.accessKeyId().getBytes(UTF_8))
                    .withSecretAccessKey(localCredentials.secretKey().getBytes(UTF_8))
                    .withSessionToken(localCredentials.sessionToken().getBytes(UTF_8)).build());
            config.setTime(time.toInstant().toEpochMilli());
            return software.amazon.awssdk.crt.auth.signing.AwsSigner.sign(httpRequest, config).get().getSignedRequest()
                    .getHeaders().stream().collect(Collectors.toMap(software.amazon.awssdk.crt.http.HttpHeader::getName,
                            software.amazon.awssdk.crt.http.HttpHeader::getValue));
        }
    }
}
