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
import java.text.SimpleDateFormat;
import java.util.SimpleTimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.folderwatcher.internal.api.exception.AuthException;
import org.openhab.binding.folderwatcher.internal.api.util.BinaryUtils;
import org.openhab.binding.folderwatcher.internal.api.util.HttpUtilException;

/**
 * The {@link AWS4SignerBase} class contains based methods for AWS S3 API authentication.
 * <p>
 * Based on offical AWS example {@see https://docs.aws.amazon.com/AmazonS3/latest/API/sig-v4-examples-using-sdks.html}
 * 
 * @author Alexandr Salamatov - Initial contribution
 */
@NonNullByDefault
public abstract class AWS4SignerBase extends SignerBase {

    public static final String SCHEME = "AWS4";
    public static final String TERMINATOR = "aws4_request";
    public static final String ISO8601_BASIC_FORMAT = "yyyyMMdd'T'HHmmss'Z'";
    public static final String DATESTRING_FORMAT = "yyyyMMdd";
    protected static final String PAIR_SEPARATOR = "&";
    protected static final String VALEU_SEPARATOR = "=";
    protected URL endpointUrl;
    protected String httpMethod;
    protected String serviceName;
    protected String regionName;
    protected final SimpleDateFormat dateTimeFormat;
    protected final SimpleDateFormat dateStampFormat;

    public AWS4SignerBase(URL endpointUrl, String httpMethod, String serviceName, String regionName) {
        super(PAIR_SEPARATOR, VALEU_SEPARATOR);
        this.endpointUrl = endpointUrl;
        this.httpMethod = httpMethod;
        this.serviceName = serviceName;
        this.regionName = regionName;

        dateTimeFormat = new SimpleDateFormat(ISO8601_BASIC_FORMAT);
        dateTimeFormat.setTimeZone(new SimpleTimeZone(0, "UTC"));
        dateStampFormat = new SimpleDateFormat(DATESTRING_FORMAT);
        dateStampFormat.setTimeZone(new SimpleTimeZone(0, "UTC"));
    }

    protected static String getCanonicalRequest(URL endpoint, String httpMethod, String queryParameters,
            String canonicalizedHeaderNames, String canonicalizedHeaders, String bodyHash) throws HttpUtilException {
        return httpMethod + "\n" + getCanonicalizedResourcePath(endpoint) + "\n" + queryParameters + "\n"
                + canonicalizedHeaders + "\n" + canonicalizedHeaderNames + "\n" + bodyHash;
    }

    protected static String getStringToSign(String scheme, String algorithm, String dateTime, String scope,
            String canonicalRequest) throws AuthException {
        return scheme + "-" + algorithm + "\n" + dateTime + "\n" + scope + "\n"
                + BinaryUtils.toHex(hash(canonicalRequest));
    }
}
