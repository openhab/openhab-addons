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
import java.util.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
    private static final RequestConfig requestConfig;
    static {
        int timeout = 5;
        requestConfig = RequestConfig.custom().setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();
    }

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
        String signUrl = (empData.size() > 0) ? reqUri.getPath() + "?" + reqUri.getRawQuery() : reqUri.getPath();
        String messageToSign = String.format("%s\n%s", signUrl, timestamp);
        return getOauth2Sig(messageToSign, secretKey);
    }

    public static RestResult getCall(String encodedUrl, @Nullable Map<String, String> headers,
            @Nullable Map<String, String> params) throws IOException {
        UriBuilder builder = UriBuilder.fromUri(encodedUrl);
        if (params != null) {
            params.forEach(builder::queryParam);
        }
        URI encodedUri = builder.build();

        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpGet request = new HttpGet(encodedUri);
            if (headers != null) {
                headers.forEach(request::setHeader);
            }
            HttpResponse resp = client.execute(request);
            return new RestResult(EntityUtils.toString(resp.getEntity(), "UTF-8"),
                    resp.getStatusLine().getStatusCode());
        }
    }

    @Nullable
    public static RestResult postCall(String encodedUrl, Map<String, String> headers, String jsonData)
            throws IOException {
        try {
            StringEntity entity = new StringEntity(jsonData);
            return postCall(encodedUrl, headers, entity);
        } catch (UnsupportedEncodingException e) {
            logger.error(
                    "Unexpected error. Character encoding from json informed not supported by this platform. Payload:{}",
                    jsonData, e);
            throw new IllegalStateException(
                    "Unexpected error. Character encoding from json informed not supported by this platform.", e);
        }
    }

    @Nullable
    public static RestResult postCall(String encodedUrl, Map<String, String> headers, Map<String, String> formParams)
            throws IOException {
        List<NameValuePair> pairs = new ArrayList<>();

        formParams.forEach((k, v) -> pairs.add(new BasicNameValuePair(k, v)));

        try {
            UrlEncodedFormEntity fe = new UrlEncodedFormEntity(pairs);
            return postCall(encodedUrl, headers, fe);
        } catch (UnsupportedEncodingException e) {
            logger.error(
                    "Unexpected error. Character encoding received from Form Parameters not supported by this platform. Form Parameters:{}",
                    pairs, e);
            throw new IllegalStateException(
                    "Unexpected error. Character encoding received from Form Parameters not supported by this platform.",
                    e);
        }
    }

    @Nullable
    private static RestResult postCall(String encodedUrl, Map<String, String> headers, HttpEntity entity)
            throws IOException {
        try (CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build()) {
            HttpPost request = new HttpPost(encodedUrl);
            headers.forEach(request::setHeader);
            request.setEntity(entity);
            int hardTimeout = 6000; // milliseconds
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if (request.expectContinue())
                        request.abort();
                }
            };
            new Timer(true).schedule(task, hardTimeout);
            HttpResponse resp = client.execute(request);
            if (request.isAborted()) {
                logger.warn("POST to LG API was aborted due to timeout waiting for connection or data");
            }
            return new RestResult(EntityUtils.toString(resp.getEntity(), "UTF-8"),
                    resp.getStatusLine().getStatusCode());
        } catch (java.net.SocketTimeoutException e) {
            if (logger.isDebugEnabled()) {
                logger.warn("Timeout reading post call result from LG API", e);
            } else {
                logger.warn("Timeout reading post call result from LG API");
            }
            // In SocketTimeout cases I'm considering that I have no response on time. Then, I return null data
            // forcing caller to retry.
            return null;
        }
    }
}
