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
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.folderwatcher.internal.api.exception.AuthException;
import org.openhab.binding.folderwatcher.internal.api.util.BinaryUtils;
import org.openhab.binding.folderwatcher.internal.api.util.HttpUtilException;
import org.openhab.binding.folderwatcher.internal.api.util.HttpUtils;

/**
 * The {@link AWS4SignerBase} class contains based methods for AWS S3 API authentication.
 * <p>
 * Based on offical AWS example {@see https://docs.aws.amazon.com/AmazonS3/latest/API/sig-v4-examples-using-sdks.html}
 * 
 * @author Alexandr Salamatov - Initial contribution
 */
@NonNullByDefault
public abstract class AWS4SignerBase {

    public static final String EMPTY_BODY_SHA256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    public static final String UNSIGNED_PAYLOAD = "UNSIGNED-PAYLOAD";
    public static final String SCHEME = "AWS4";
    public static final String ALGORITHM = "HMAC-SHA256";
    public static final String TERMINATOR = "aws4_request";
    public static final String ISO8601_BASIC_FORMAT = "yyyyMMdd'T'HHmmss'Z'";
    public static final String DATESTRING_FORMAT = "yyyyMMdd";
    protected URL endpointUrl;
    protected String httpMethod;
    protected String serviceName;
    protected String regionName;
    protected final SimpleDateFormat dateTimeFormat;
    protected final SimpleDateFormat dateStampFormat;

    public AWS4SignerBase(URL endpointUrl, String httpMethod, String serviceName, String regionName) {
        this.endpointUrl = endpointUrl;
        this.httpMethod = httpMethod;
        this.serviceName = serviceName;
        this.regionName = regionName;

        dateTimeFormat = new SimpleDateFormat(ISO8601_BASIC_FORMAT);
        dateTimeFormat.setTimeZone(new SimpleTimeZone(0, "UTC"));
        dateStampFormat = new SimpleDateFormat(DATESTRING_FORMAT);
        dateStampFormat.setTimeZone(new SimpleTimeZone(0, "UTC"));
    }

    protected static String getCanonicalizeHeaderNames(Map<String, String> headers) {
        List<String> sortedHeaders = new ArrayList<String>();
        sortedHeaders.addAll(headers.keySet());
        Collections.sort(sortedHeaders, String.CASE_INSENSITIVE_ORDER);

        StringBuilder buffer = new StringBuilder();
        for (String header : sortedHeaders) {
            if (buffer.length() > 0) {
                buffer.append(";");
            }
            buffer.append(header.toLowerCase());
        }
        return buffer.toString();
    }

    protected static String getCanonicalizedHeaderString(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return "";
        }

        List<String> sortedHeaders = new ArrayList<String>();
        sortedHeaders.addAll(headers.keySet());
        Collections.sort(sortedHeaders, String.CASE_INSENSITIVE_ORDER);

        StringBuilder buffer = new StringBuilder();
        for (String key : sortedHeaders) {
            buffer.append(key.toLowerCase().replaceAll("\\s+", " ") + ":" + headers.get(key).replaceAll("\\s+", " "));
            buffer.append("\n");
        }
        return buffer.toString();
    }

    protected static String getCanonicalRequest(URL endpoint, String httpMethod, String queryParameters,
            String canonicalizedHeaderNames, String canonicalizedHeaders, String bodyHash) throws HttpUtilException {
        return httpMethod + "\n" + getCanonicalizedResourcePath(endpoint) + "\n" + queryParameters + "\n"
                + canonicalizedHeaders + "\n" + canonicalizedHeaderNames + "\n" + bodyHash;
    }

    protected static String getCanonicalizedResourcePath(URL endpoint) throws HttpUtilException {
        if (endpoint == null) {
            return "/";
        }
        String path = endpoint.getPath();
        if (path == null || path.isEmpty()) {
            return "/";
        }

        String encodedPath = HttpUtils.urlEncode(path, true);
        if (encodedPath.startsWith("/")) {
            return encodedPath;
        } else {
            return "/".concat(encodedPath);
        }
    }

    public static String getCanonicalizedQueryString(Map<String, String> parameters) throws HttpUtilException {
        if (parameters == null || parameters.isEmpty()) {
            return "";
        }

        SortedMap<String, String> sorted = new TreeMap<String, String>();
        Iterator<Map.Entry<String, String>> pairs = parameters.entrySet().iterator();

        while (pairs.hasNext()) {
            Map.Entry<String, String> pair = pairs.next();
            String key = pair.getKey();
            String value = pair.getValue();
            sorted.put(HttpUtils.urlEncode(key, false), HttpUtils.urlEncode(value, false));
        }

        StringBuilder builder = new StringBuilder();
        pairs = sorted.entrySet().iterator();
        while (pairs.hasNext()) {
            Map.Entry<String, String> pair = pairs.next();
            builder.append(pair.getKey());
            builder.append("=");
            builder.append(pair.getValue());
            if (pairs.hasNext()) {
                builder.append("&");
            }
        }
        return builder.toString();
    }

    protected static String getStringToSign(String scheme, String algorithm, String dateTime, String scope,
            String canonicalRequest) throws AuthException {
        return scheme + "-" + algorithm + "\n" + dateTime + "\n" + scope + "\n"
                + BinaryUtils.toHex(hash(canonicalRequest));
    }

    public static byte[] hash(String text) throws AuthException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(text.getBytes("UTF-8"));
            return md.digest();
        } catch (Exception e) {
            throw new AuthException("Unable to compute hash while signing request: " + e.getMessage(), e);
        }
    }

    public static byte[] hash(byte[] data) throws AuthException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(data);
            return md.digest();
        } catch (Exception e) {
            throw new AuthException("Unable to compute hash while signing request: " + e.getMessage(), e);
        }
    }

    protected static byte[] sign(String stringData, byte[] key, String algorithm) throws AuthException {
        try {
            byte[] data = stringData.getBytes("UTF-8");
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(key, algorithm));
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new AuthException("Unable to calculate a request signature: " + e.getMessage(), e);
        }
    }
}
