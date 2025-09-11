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
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.folderwatcher.internal.api.exception.AuthException;
import org.openhab.binding.folderwatcher.internal.api.util.HttpUtilException;
import org.openhab.binding.folderwatcher.internal.api.util.HttpUtils;

/**
 * The {@link AzureSignerBase} class contains based methods for Azure Blob API authentication.
 * 
 * @author Alexandr Salamatov - Initial contribution
 */
@NonNullByDefault
public abstract class AzureSignerBase extends SignerBase {
    protected static final String PAIR_SEPARATOR = "\n";
    protected static final String VALEU_SEPARATOR = ":";
    protected static final String HEADER_FILTER = "x-ms-";
    protected URL endpointUrl;
    protected String httpMethod;
    protected String serviceName;
    protected String regionName;
    protected DateTimeFormatter dateTimeFormat;

    public AzureSignerBase(URL endpointUrl, String httpMethod, String serviceName, String regionName) {
        super(PAIR_SEPARATOR, VALEU_SEPARATOR);

        this.endpointUrl = endpointUrl;
        this.httpMethod = httpMethod;
        this.serviceName = serviceName;
        this.regionName = regionName;

        dateTimeFormat = java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
    }

    protected static String getCanonicalResource(URL endpoint, String queryParameters) throws HttpUtilException {
        return getCanonicalizedResourceName(endpoint) + getCanonicalizedResourcePath(endpoint) + "\n" + queryParameters;
    }

    protected static String getCanonicalizedResourceName(URL endpoint) throws HttpUtilException {
        if (endpoint == null) {
            return "/";
        }
        String path = endpoint.getHost().split("\\.")[0];
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

    protected static String getStringToSign(String VERB, String ContentEncoding, String ContentLanguage,
            String ContentLength, String ContentMD5, String ContentType, String Date, String IfModifiedSince,
            String IfMatch, String IfNoneMatch, String IfUnmodifiedSince, String Range, String CanonicalizedHeaders,
            String CanonicalizedResource) throws AuthException {
        return VERB + "\n" + ContentEncoding + "\n" + ContentLanguage + "\n" + ContentLength + "\n" + ContentMD5 + "\n"
                + ContentType + "\n" + Date + "\n" + IfModifiedSince + "\n" + IfMatch + "\n" + IfNoneMatch + "\n"
                + IfUnmodifiedSince + "\n" + Range + "\n" + CanonicalizedHeaders + CanonicalizedResource;
    }
}
