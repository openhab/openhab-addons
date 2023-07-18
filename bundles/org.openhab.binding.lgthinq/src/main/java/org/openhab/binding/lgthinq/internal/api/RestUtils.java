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
package org.openhab.binding.lgthinq.internal.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

    private static final Logger logger = LoggerFactory.getLogger(RestUtils.class);
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    public static String getPreLoginEncPwd(String pwdToEnc) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            logger.error("Definitively, it is unexpected.", e);
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
            logger.error("Unexpected error. SHA1 algorithm must exists in JDK distribution.", e);
            throw new IllegalStateException("Unexpected error. SHA1 algorithm must exists in JDK distribution", e);
        } catch (InvalidKeyException e) {
            logger.error("Unexpected error.", e);
            throw new IllegalStateException("Unexpected error.", e);
        }
    }

    public static byte[] getTokenSignature(String authUrl, String secretKey, Map<String, String> empData,
            String timestamp) {
        UriBuilder builder = UriBuilder.fromUri(authUrl);
        empData.forEach(builder::queryParam);

        URI reqUri = builder.build();
        String signUrl = empData.size() > 0 ? reqUri.getPath() + "?" + reqUri.getRawQuery() : reqUri.getPath();
        String messageToSign = String.format("%s\n%s", signUrl, timestamp);
        return getOauth2Sig(messageToSign, secretKey);
    }

    public static RestResult getCall(HttpClient httpClient, String encodedUrl, @Nullable Map<String, String> headers,
            @Nullable Map<String, String> params) throws IOException {

        Request request = httpClient.newRequest(encodedUrl).method("GET");
        if (params != null) {
            params.forEach(request::param);
        }
        if (headers != null) {
            headers.forEach(request::header);
        }
        ContentResponse response;
        try {
            response = request.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("Exception occurred during GET execution: {}", e.getMessage(), e);
            throw new CommunicationException(e);
        }
        return new RestResult(response.getContentAsString(), response.getStatus());
    }

    @Nullable
    public static RestResult postCall(HttpClient httpClient, String encodedUrl, Map<String, String> headers, String jsonData)
            throws IOException {
        try {
            return postCall(httpClient, encodedUrl, headers, new StringContentProvider(jsonData));
        } catch (UnsupportedEncodingException e) {
            logger.error(
                    "Unexpected error. Character encoding from json informed not supported by this platform. Payload:{}",
                    jsonData, e);
            throw new IllegalStateException(
                    "Unexpected error. Character encoding from json informed not supported by this platform.", e);
        }
    }

    @Nullable
    public static RestResult postCall(HttpClient httpClient, String encodedUrl, Map<String, String> headers, Map<String, String> formParams)
            throws IOException {
        Fields fields = new Fields();
        formParams.forEach(fields::put);
        try {
            return postCall(httpClient, encodedUrl, headers, new FormContentProvider(fields));
        } catch (UnsupportedEncodingException e) {
            logger.error(
                    "Unexpected error. Character encoding received from Form Parameters not supported by this platform. Form Parameters:{}",
                    formParams, e);
            throw new IllegalStateException(
                    "Unexpected error. Character encoding received from Form Parameters not supported by this platform.",
                    e);
        }
    }

    @Nullable
    private static RestResult postCall(HttpClient httpClient, String encodedUrl, Map<String, String> headers, ContentProvider contentProvider)
            throws IOException {

        try {
            Request request = httpClient.newRequest(encodedUrl)
                    .method("POST")
                    .content(contentProvider)
                    .timeout(10, TimeUnit.SECONDS);
            if (headers != null) {
                headers.forEach(request::header);
            }
            ContentResponse response = request.content(contentProvider).timeout(10, TimeUnit.SECONDS)
                    .send();
            return new RestResult(response.getContentAsString(), response.getStatus());
        } catch (TimeoutException e) {
            if (logger.isDebugEnabled()) {
                logger.warn("Timeout reading post call result from LG API", e);
            } else {
                logger.warn("Timeout reading post call result from LG API");
            }
            // In SocketTimeout cases I'm considering that I have no response on time. Then, I return null data
            // forcing caller to retry.
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("InterruptedException occurred during POST execution: {}", e.getMessage(), e);
            throw new CommunicationException(e);
        } catch (ExecutionException e) {
            logger.error("ExecutionException occurred during POST execution: {}", e.getMessage(), e);
            throw new CommunicationException(e);
        }
    }

}
