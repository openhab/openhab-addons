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
package org.openhab.binding.lgthinq.lgservices.api;

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.*;

import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.util.Fields;
import org.openhab.core.i18n.CommunicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RestUtils} rest utilities
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class RestUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestUtils.class);

    public static String getPreLoginEncPwd(String pwdToEnc) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(MESSAGE_DIGEST_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warn("The required algorithm is not available.", e);
            throw new IllegalStateException("Unexpected error. SHA-512 algorithm must exists in JDK distribution", e);
        }
        digest.reset();
        digest.update(pwdToEnc.getBytes(StandardCharsets.UTF_8));

        return String.format("%0128x", new BigInteger(1, digest.digest()));
    }

    public static byte[] getOauth2Sig(String messageSign, String secret) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec signingKey = new SecretKeySpec(secretBytes, HMAC_SHA1_ALGORITHM);

        try {
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            return Base64.getEncoder().encode(mac.doFinal(messageSign.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            LOGGER.debug("Unexpected error. SHA1 algorithm must exists in JDK distribution.", e);
            throw new IllegalStateException("Unexpected error. SHA1 algorithm must exists in JDK distribution", e);
        } catch (InvalidKeyException e) {
            LOGGER.debug("Unexpected error.", e);
            throw new IllegalStateException("Unexpected error.", e);
        }
    }

    public static byte[] getTokenSignature(String authUrl, String secretKey, Map<String, String> empData,
            String timestamp) {
        UriBuilder builder = UriBuilder.fromUri(authUrl);
        empData.forEach(builder::queryParam);

        URI reqUri = builder.build();
        String signUrl = !empData.isEmpty() ? reqUri.getPath() + "?" + reqUri.getRawQuery() : reqUri.getPath();
        String messageToSign = String.format("%s\n%s", signUrl, timestamp);
        return getOauth2Sig(messageToSign, secretKey);
    }

    public static RestResult getCall(HttpClient httpClient, String encodedUrl, @Nullable Map<String, String> headers,
            @Nullable Map<String, String> params) {
        Request request = httpClient.newRequest(encodedUrl).method("GET");
        if (params != null) {
            params.forEach(request::param);
        }
        if (headers != null) {
            headers.forEach(request::header);
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("GET request: {}", request.getURI());
        }
        try {
            ContentResponse response = request.send();

            LOGGER.trace("GET response: {}", response.getContentAsString());

            return new RestResult(response.getContentAsString(), response.getStatus());
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            LOGGER.debug("Exception occurred during GET execution: {}", e.getMessage(), e);
            throw new CommunicationException(e);
        }
    }

    @Nullable
    public static RestResult postCall(HttpClient httpClient, String encodedUrl, Map<String, String> headers,
            String jsonData) {
        return postCall(httpClient, encodedUrl, headers, new StringContentProvider(jsonData));
    }

    @Nullable
    public static RestResult postCall(HttpClient httpClient, String encodedUrl, Map<String, String> headers,
            Map<String, String> formParams) {
        Fields fields = new Fields();
        formParams.forEach(fields::put);
        return postCall(httpClient, encodedUrl, headers, new FormContentProvider(fields));
    }

    @Nullable
    private static RestResult postCall(HttpClient httpClient, String encodedUrl, Map<String, String> headers,
            ContentProvider contentProvider) {
        try {
            Request request = httpClient.newRequest(encodedUrl).method("POST").content(contentProvider).timeout(10,
                    TimeUnit.SECONDS);
            headers.forEach(request::header);
            LOGGER.trace("POST request to URI: {}", request.getURI());

            ContentResponse response = request.content(contentProvider).timeout(10, TimeUnit.SECONDS).send();

            LOGGER.trace("POST response: {}", response.getContentAsString());

            return new RestResult(response.getContentAsString(), response.getStatus());
        } catch (TimeoutException e) {
            LOGGER.warn("Timeout reading post call result from LG API", e); // In SocketTimeout cases I'm considering
            // that I have no response on time. Then, I
            // return null data
            // forcing caller to retry.
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommunicationException(e);
        } catch (ExecutionException e) {
            throw new CommunicationException(e);
        }
    }
}
