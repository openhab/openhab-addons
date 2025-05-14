/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.folderwatcher.internal.api.exception.AuthException;
import org.openhab.binding.folderwatcher.internal.api.util.HttpUtilException;

/**
 * The {@link Azure4SignerForAuthorizationHeader} class contains methods for Azure Blob API authentication using HTTPS
 * headers.
 * 
 * @author Alexandr Salamatov - Initial contribution
 */
@NonNullByDefault
public class Azure4SignerForAuthorizationHeader extends AzureSignerBase {

    public Azure4SignerForAuthorizationHeader(String httpMethod, URL endpointUrl) {
        super(endpointUrl, httpMethod, "serviceName", "regionName");
    }

    public String computeSignature(Map<String, String> headers, Map<String, String> queryParameters,
            String AzureAccount, String AzureSecretKey, String azureContainerName)
            throws AuthException, HttpUtilException {
        String dateTimeStamp = dateTimeFormat.format(java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC));
        headers.put("x-ms-date", dateTimeStamp);
        headers.put("x-ms-version", "2020-08-04");

        Map<String, String> filteredHeaders = new HashMap<>();
        filteredHeaders.putAll(headers);
        Iterator<Map.Entry<String, String>> iterator = filteredHeaders.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            if (!entry.getKey().startsWith(HEADER_FILTER)) {
                iterator.remove();
            }
        }
        String canonicalizedHeaders = getCanonicalizedHeaderString(filteredHeaders);
        String canonicalizedQueryParameters = getCanonicalizedQueryString(queryParameters);
        String canonicalilezdResource = getCanonicalResource(endpointUrl, canonicalizedQueryParameters);
        String stringToSign = getStringToSign("GET", "", "", "", "", "", "", "", "", "", "", "", canonicalizedHeaders,
                canonicalilezdResource);
        byte[] kSecret = Base64.getDecoder().decode(AzureSecretKey);
        byte[] signed = sign(stringToSign, kSecret, "HmacSHA256");
        return "SharedKey" + " " + AzureAccount + ":" + Base64.getEncoder().encodeToString(signed);
    }
}
