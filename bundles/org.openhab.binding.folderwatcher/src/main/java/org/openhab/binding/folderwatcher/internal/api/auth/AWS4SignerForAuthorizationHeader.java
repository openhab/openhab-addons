/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.folderwatcher.internal.api.auth;

import java.net.URL;
import java.util.Date;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.folderwatcher.internal.api.util.BinaryUtils;

/**
 * The {@link AWS4SignerForAuthorizationHeader} class contains methods for AWS S3 API authentication using HTTP(S)
 * headers.
 * <p>
 * Based on offical AWS example {@see https://docs.aws.amazon.com/AmazonS3/latest/API/sig-v4-examples-using-sdks.html}
 * 
 * @author Alexandr Salamatov - Initial contribution
 */
@NonNullByDefault
public class AWS4SignerForAuthorizationHeader extends AWS4SignerBase {

    public AWS4SignerForAuthorizationHeader(URL endpointUrl, String httpMethod, String serviceName, String regionName) {
        super(endpointUrl, httpMethod, serviceName, regionName);
    }

    public String computeSignature(Map<String, String> headers, Map<String, String> queryParameters, String bodyHash,
            String awsAccessKey, String awsSecretKey) {
        Date now = new Date();
        String dateTimeStamp = dateTimeFormat.format(now);
        headers.put("x-amz-date", dateTimeStamp);
        String hostHeader = endpointUrl.getHost();
        int port = endpointUrl.getPort();
        if (port > -1) {
            hostHeader.concat(":" + Integer.toString(port));
        }
        headers.put("Host", hostHeader);

        String canonicalizedHeaderNames = getCanonicalizeHeaderNames(headers);
        String canonicalizedHeaders = getCanonicalizedHeaderString(headers);
        String canonicalizedQueryParameters = getCanonicalizedQueryString(queryParameters);
        String canonicalRequest = getCanonicalRequest(endpointUrl, httpMethod, canonicalizedQueryParameters,
                canonicalizedHeaderNames, canonicalizedHeaders, bodyHash);
        String dateStamp = dateStampFormat.format(now);
        String scope = dateStamp + "/" + regionName + "/" + serviceName + "/" + TERMINATOR;
        String stringToSign = getStringToSign(SCHEME, ALGORITHM, dateTimeStamp, scope, canonicalRequest);
        byte[] kSecret = (SCHEME + awsSecretKey).getBytes();
        byte[] kDate = sign(dateStamp, kSecret, "HmacSHA256");
        byte[] kRegion = sign(regionName, kDate, "HmacSHA256");
        byte[] kService = sign(serviceName, kRegion, "HmacSHA256");
        byte[] kSigning = sign(TERMINATOR, kService, "HmacSHA256");
        byte[] signature = sign(stringToSign, kSigning, "HmacSHA256");
        String credentialsAuthorizationHeader = "Credential=" + awsAccessKey + "/" + scope;
        String signedHeadersAuthorizationHeader = "SignedHeaders=" + canonicalizedHeaderNames;
        String signatureAuthorizationHeader = "Signature=" + BinaryUtils.toHex(signature);
        String authorizationHeader = SCHEME + "-" + ALGORITHM + " " + credentialsAuthorizationHeader + ", "
                + signedHeadersAuthorizationHeader + ", " + signatureAuthorizationHeader;
        return authorizationHeader;
    }
}
