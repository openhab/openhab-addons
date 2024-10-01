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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.salus.internal.rest.exceptions.SalusApiException;

import uk.co.lucasweb.aws.v4.signer.HttpRequest;
import uk.co.lucasweb.aws.v4.signer.Signer;
import uk.co.lucasweb.aws.v4.signer.credentials.AwsCredentials;
import uk.co.lucasweb.aws.v4.signer.hash.Sha256;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
class AwsSigner {
    static Map<String, String> sign(String pathAndQuery, ZonedDateTime time, CogitoCredentials cogitoCredentials,
            String region, String service, @Nullable String body) throws SalusApiException {
        try {
            var contentSha256 = Sha256.get(body != null ? body : "", UTF_8);
            var request = new HttpRequest("GET", pathAndQuery);
            var isoDate = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").format(time);
            var signer = Signer.builder().region(region)//
                    .awsCredentials(new AwsCredentials(cogitoCredentials.accessKeyId(), cogitoCredentials.secretKey()))//
                    .header("host", "")//
                    .header("X-Amz-Date", isoDate)//
                    .header("X-Amz-Security-Token", cogitoCredentials.sessionToken())//
                    .build(request, service, contentSha256).getSignature();
            return Map.of(//
                    "Authorization", signer, //
                    "X-Amz-Date", isoDate, //
                    "host", "", //
                    "X-Amz-Security-Token", cogitoCredentials.sessionToken());
        } catch (Exception e) {
            throw new SalusApiException("Cannot build AWS signature!", e);
        }
    }
}
