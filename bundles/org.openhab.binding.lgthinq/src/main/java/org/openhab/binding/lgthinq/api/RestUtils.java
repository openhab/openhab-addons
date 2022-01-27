/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RestUtils} rest utilities
 *
 * @author Nemer Daud - Initial contribution
 */
public class RestUtils {
    private static final Logger logger = LoggerFactory.getLogger(RestUtils.class);

    public static String getPreLoginEncPwd(String pwdToEnc) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            logger.error("Definitively, it is unexpected.", e);
            return null;
        }
        digest.reset();
        digest.update(pwdToEnc.getBytes(StandardCharsets.UTF_8));

        return String.format("%0128x", new BigInteger(1, digest.digest()));
    }

    public static byte[] getOauth2Sig(String messageSign, String secret) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        byte[] messageCrypt = new HmacUtils(HmacAlgorithms.HMAC_SHA_1, secretBytes)
                .hmac(messageSign.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encode(messageCrypt);
    }

    public static byte[] getTokenSignature(String authUrl, String secretKey, Map<String, String> empData,
            String timestamp) {
        UriBuilder builder = UriBuilder.fromUri(authUrl);
        if (empData != null) {
            empData.forEach(builder::queryParam);
        }

        URI reqUri = builder.build();
        String signUrl = (empData != null && empData.size() > 0) ? reqUri.getPath() + "?" + reqUri.getRawQuery()
                : reqUri.getPath();
        String messageToSign = String.format("%s\n%s", signUrl, timestamp);
        byte[] oauthSig = getOauth2Sig(messageToSign, secretKey);
        return oauthSig;
    }

    public static RestResult getCall(String encodedUrl, Map<String, String> headers, Map<String, String> params)
            throws IOException {
        UriBuilder builder = UriBuilder.fromUri(encodedUrl);
        if (params != null) {
            params.forEach(builder::queryParam);
        }
        URI encodedUri = builder.build();

        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(encodedUri);
        if (headers != null) {
            headers.forEach(request::setHeader);
        }

        HttpResponse resp = client.execute(request);
        return new RestResult(EntityUtils.toString(resp.getEntity(), "UTF-8"), resp.getStatusLine().getStatusCode());
    }

    public static RestResult postCall(String encodedUrl, Map<String, String> headers, String jsonData)
            throws IOException {
        List<NameValuePair> pairs = new ArrayList<>();
        try {
            StringEntity entity = new StringEntity(jsonData);
            return postCall(encodedUrl, headers, entity);
        } catch (UnsupportedEncodingException e) {
            logger.error("Definitively, it is unexpected.", e);
            return null;
        }
    }

    public static RestResult postCall(String encodedUrl, Map<String, String> headers, Map<String, String> formParams)
            throws IOException {
        List<NameValuePair> pairs = new ArrayList<>();
        if (formParams != null)
            formParams.forEach((k, v) -> {
                pairs.add(new BasicNameValuePair(k, v));
            });
        try {
            UrlEncodedFormEntity fe = new UrlEncodedFormEntity(pairs);
            return postCall(encodedUrl, headers, fe);
        } catch (UnsupportedEncodingException e) {
            logger.error("Definitively, it is unexpected.", e);
            return null;
        }
    }

    private static RestResult postCall(String encodedUrl, Map<String, String> headers, HttpEntity entity)
            throws IOException {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(encodedUrl);
        if (headers != null)
            headers.forEach(request::setHeader);
        request.setEntity(entity);
        HttpResponse resp = client.execute(request);
        return new RestResult(EntityUtils.toString(resp.getEntity(), "UTF-8"), resp.getStatusLine().getStatusCode());
    }
}
