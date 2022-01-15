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
package org.openhab.binding.nest.internal.wwn.handler;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.nest.internal.wwn.WWNBindingConstants;
import org.openhab.binding.nest.internal.wwn.exceptions.FailedResolvingWWNUrlException;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Supplies resolved redirect URLs of {@link WWNBindingConstants#NEST_URL} so they can be used with HTTP clients that
 * do not pass Authorization headers after redirects like the Jetty client used by {@link HttpUtil}.
 *
 * @author Wouter Born - Initial contribution
 * @author Wouter Born - Extract resolving redirect URL from NestBridgeHandler into NestRedirectUrlSupplier
 */
@NonNullByDefault
public class WWNRedirectUrlSupplier {

    private final Logger logger = LoggerFactory.getLogger(WWNRedirectUrlSupplier.class);

    protected String cachedUrl = "";

    protected Properties httpHeaders;

    public WWNRedirectUrlSupplier(Properties httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    public String getRedirectUrl() throws FailedResolvingWWNUrlException {
        if (cachedUrl.isEmpty()) {
            cachedUrl = resolveRedirectUrl();
        }
        return cachedUrl;
    }

    public void resetCache() {
        cachedUrl = "";
    }

    /**
     * Resolves the redirect URL for calls using the {@link WWNBindingConstants#NEST_URL}.
     *
     * The Jetty client used by {@link HttpUtil} will not pass the Authorization header after a redirect resulting in
     * "401 Unauthorized error" issues.
     *
     * Note that this workaround currently does not use any configured proxy like {@link HttpUtil} does.
     *
     * @see https://developers.nest.com/documentation/cloud/how-to-handle-redirects
     */
    private String resolveRedirectUrl() throws FailedResolvingWWNUrlException {
        HttpClient httpClient = new HttpClient(new SslContextFactory.Client());
        httpClient.setFollowRedirects(false);

        Request request = httpClient.newRequest(WWNBindingConstants.NEST_URL).method(HttpMethod.GET).timeout(30,
                TimeUnit.SECONDS);
        for (String httpHeaderKey : httpHeaders.stringPropertyNames()) {
            request.header(httpHeaderKey, httpHeaders.getProperty(httpHeaderKey));
        }

        ContentResponse response;
        try {
            httpClient.start();
            response = request.send();
            httpClient.stop();
        } catch (Exception e) {
            throw new FailedResolvingWWNUrlException("Failed to resolve redirect URL: " + e.getMessage(), e);
        }

        int status = response.getStatus();
        String redirectUrl = response.getHeaders().get(HttpHeader.LOCATION);

        if (status != HttpStatus.TEMPORARY_REDIRECT_307) {
            logger.debug("Redirect status: {}", status);
            logger.debug("Redirect response: {}", response.getContentAsString());
            throw new FailedResolvingWWNUrlException("Failed to get redirect URL, expected status "
                    + HttpStatus.TEMPORARY_REDIRECT_307 + " but was " + status);
        } else if (redirectUrl == null || redirectUrl.isEmpty()) {
            throw new FailedResolvingWWNUrlException("Redirect URL is empty");
        }

        redirectUrl = redirectUrl.endsWith("/") ? redirectUrl.substring(0, redirectUrl.length() - 1) : redirectUrl;
        logger.debug("Redirect URL: {}", redirectUrl);
        return redirectUrl;
    }
}
