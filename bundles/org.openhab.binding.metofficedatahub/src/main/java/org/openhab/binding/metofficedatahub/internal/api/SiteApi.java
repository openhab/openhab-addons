/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.metofficedatahub.internal.api;

import static org.openhab.binding.metofficedatahub.internal.MetOfficeDataHubBindingConstants.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.PointType;
import org.openhab.core.storage.StorageService;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This provides the communications layer for the Site API
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class SiteApi {

    protected final LookupWrapper<@Nullable IConnectionStatusListener> authenticationListeners = new LookupWrapper<>();
    protected final LookupWrapper<@Nullable IRateLimiterListener> rateLimiterListener = new LookupWrapper<>();

    private final Logger logger = LoggerFactory.getLogger(SiteApi.class);

    // Utilised for sending communciations
    private final HttpClientFactory httpClientFactory;

    // Utilised for persistance of rate limiter data between reboots
    private final StorageService storageService;
    private final TimeZoneProvider timeZoneProvider;
    private final ScheduledExecutorService scheduler;

    // Locale related functionality is below
    private final TranslationProvider translationProvider;
    private final LocaleProvider localeProvider;
    private final Bundle bundle;

    private final SiteApiAuthentication apiAuth;
    private final RequestLimiter requestLimiter;

    private final String usageId;

    @Activate
    public SiteApi(String usageId, @Reference HttpClientFactory httpClientFactory,
            @Reference StorageService storageService, @Reference TranslationProvider translationProvider,
            @Reference LocaleProvider localeProvider, @Reference TimeZoneProvider timeZoneProvider,
            @Reference ScheduledExecutorService scheduler) {
        this.usageId = usageId;
        this.httpClientFactory = httpClientFactory;
        this.storageService = storageService;
        this.timeZoneProvider = timeZoneProvider;
        this.translationProvider = translationProvider;
        this.localeProvider = localeProvider;
        this.bundle = FrameworkUtil.getBundle(getClass());
        this.apiAuth = new SiteApiAuthentication();
        this.requestLimiter = new RequestLimiter(usageId, storageService, timeZoneProvider, scheduler,
                translationProvider, localeProvider, bundle);
        this.scheduler = scheduler;
    }

    public void dispose() {
        requestLimiter.dispose();
        apiAuth.dispose();
    }

    public void setApiKey(final String apiKey) throws AuthTokenException {
        apiAuth.setApiKey(apiKey);
    }

    private void notifyRateLimiterListeners() {
        scheduler.schedule(() -> {
            rateLimiterListener.getItemlist().forEach(rateLimiterListener -> {
                if (rateLimiterListener != null) {
                    rateLimiterListener.processRateLimiterUpdated(requestLimiter);
                }
            });
        }, 1, TimeUnit.SECONDS);
    }

    private void notifyAuthenticationListeners(final boolean authenticated) {
        scheduler.schedule(() -> {
            authenticationListeners.getItemlist().forEach(authListener -> {
                if (authListener != null) {
                    authListener.processAuthenticationResult(authenticated);
                }
            });
        }, 1, TimeUnit.SECONDS);
    }

    public void sendRequest(final boolean daily, final PointType location,
            final ISiteResponseListener siteResponseListener) {
        /*
         * if (!getThing().getStatus().equals(ThingStatus.ONLINE) && !authFailedPreviously) {
         * logger.debug("Disabled requesting data - this thing is not ONLINE");
         * return;
         * }
         */

        if (requestLimiter.getCurrentRequestCount() == RequestLimiter.INVALID_REQUEST_ID) {
            logger.debug("{} - Disabled requesting data - request limit has been hit", usageId);
            return;
        }

        notifyRateLimiterListeners();

        final Response.CompleteListener listener = new BufferingResponseListener() { // 4.5kb buffer will cover both
            @Override
            public void onComplete(@Nullable Result result) {
                if (result != null) {
                    final boolean userAuthValidatedPreviously = apiAuth.getIsAuthenticated();
                    apiAuth.processResult(result);
                    if (!apiAuth.getIsAuthenticated()) {
                        // Callback Async function confirming authorization is completed.
                        notifyAuthenticationListeners(false);
                        return;
                    }

                    // User is authorized at this point
                    if (!userAuthValidatedPreviously) {
                        // Authorization token confirmed
                        // Callback Async function confirming authorization is completed.
                        notifyAuthenticationListeners(true);
                    }

                    if (result.isSucceeded()) {
                        final String response = getContentAsString();
                        if (response != null) {
                            scheduler.schedule(() -> {
                                if (daily) {
                                    siteResponseListener.processDailyResponse(response);
                                } else {
                                    siteResponseListener.processHourlyResponse(response);
                                }
                            }, 1, TimeUnit.SECONDS);
                        }
                    }
                }
            }
        };

        final String url = ((daily) ? GET_FORECAST_URL_DAILY : GET_FORECAST_URL_HOURLY)
                .replace(GET_FORECAST_KEY_LATITUDE, location.getLongitude().toString())
                .replace(GET_FORECAST_KEY_LONGITUDE, location.getLongitude().toString());

        final Request request = httpClientFactory.getCommonHttpClient().newRequest(url).method(HttpMethod.GET)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_TYPE.toString())
                .timeout(GET_FORECAST_REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        try {
            apiAuth.addAuthentication(request).send(listener);
        } catch (AuthTokenException ate) {
            notifyAuthenticationListeners(false);
        }
    }
}
