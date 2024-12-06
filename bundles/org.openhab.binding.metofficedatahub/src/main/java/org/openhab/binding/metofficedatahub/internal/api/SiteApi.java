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

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
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
    protected final LookupWrapper<@Nullable IRateLimiterListener> rateLimiterListeners = new LookupWrapper<>();

    private final Logger logger = LoggerFactory.getLogger(SiteApi.class);

    // Utilised for sending communications
    private final HttpClient httpClient;

    // Utilised for persistence of rate limiter data between reboots
    private final ScheduledExecutorService scheduler;

    private final SiteApiAuthentication apiAuth;
    private final RequestLimiter requestLimiter;

    private final String usageId;

    private final TranslationProvider translationProvider;
    private final LocaleProvider localeProvider;
    private final Bundle bundle;

    @Activate
    public SiteApi(String usageId, @Reference HttpClientFactory httpClientFactory,
            @Reference StorageService storageService, @Reference TranslationProvider translationProvider,
            @Reference LocaleProvider localeProvider, @Reference TimeZoneProvider timeZoneProvider,
            @Reference ScheduledExecutorService scheduler) {
        this.usageId = usageId;
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.apiAuth = new SiteApiAuthentication();
        this.translationProvider = translationProvider;
        this.localeProvider = localeProvider;
        this.bundle = FrameworkUtil.getBundle(getClass());
        this.requestLimiter = new RequestLimiter(usageId, storageService, timeZoneProvider, scheduler,
                translationProvider, localeProvider, bundle);
        this.scheduler = scheduler;
    }

    public void registerListeners(final String id, final Object candidateListener) {
        if (candidateListener instanceof IConnectionStatusListener connectionStatusListener) {
            authenticationListeners.registerItem(id, connectionStatusListener, NO_OP);
        }
        if (candidateListener instanceof IRateLimiterListener rateLimiterListener) {
            rateLimiterListeners.registerItem(id, rateLimiterListener, NO_OP);
        }
    }

    public void deregisterListeners(final String id, final Object candidateListener) {
        if (candidateListener instanceof IConnectionStatusListener connectionStatusListener) {
            authenticationListeners.deregisterItem(id, connectionStatusListener, NO_OP);
        }
        if (candidateListener instanceof IRateLimiterListener rateLimiterListener) {
            rateLimiterListeners.deregisterItem(id, rateLimiterListener, NO_OP);
        }
    }

    public void dispose() {
        requestLimiter.dispose();
        apiAuth.dispose();
    }

    public void setLimits(final int maxDailyCallLimit) {
        requestLimiter.updateLimit(maxDailyCallLimit);
    }

    public void setApiKey(final String apiKey) {
        try {
            apiAuth.setApiKey(apiKey);
        } catch (AuthTokenException ate) {
            notifyAuthenticationListeners(false);
        }
    }

    private void notifyRateLimiterListeners() {
        scheduler.execute(() -> {
            rateLimiterListeners.getItemlist().forEach(rateLimiterListener -> {
                if (rateLimiterListener != null) {
                    rateLimiterListener.processRateLimiterUpdated(requestLimiter);
                }
            });
        });
    }

    private void notifyAuthenticationListeners(final boolean authenticated) {
        scheduler.execute(() -> {
            authenticationListeners.getItemlist().forEach(authListener -> {
                if (authListener != null) {
                    authListener.processAuthenticationResult(authenticated);
                }
            });
        });
    }

    private void notifyCommFailureListeners(final @Nullable Throwable e) {
        scheduler.execute(() -> {
            authenticationListeners.getItemlist().forEach(authListener -> {
                if (authListener != null) {
                    authListener.processCommunicationFailure(e);
                }
            });
        });
    }

    private void notifyConnectedListeners() {
        scheduler.execute(() -> {
            authenticationListeners.getItemlist().forEach(authListener -> {
                if (authListener != null) {
                    authListener.processConnected();
                }
            });
        });
    }

    public void validateSiteApi() {
        final PointType siteApiTestLocation = new PointType("51.5072,0.1276");
        final Response.CompleteListener siteResponseListener = new BufferingResponseListener() { // 4.5kb buffer
            @Override
            public void onComplete(@Nullable Result result) {
                if (result != null && !result.isFailed()) {
                    notifyConnectedListeners();
                } else {
                    if (result != null) {
                        notifyCommFailureListeners(result.getFailure());
                    } else {
                        notifyCommFailureListeners(new Throwable("Unknown"));
                    }
                }
            }
        };

        if (requestLimiter.getRequestCountIfAvailable() == RequestLimiter.INVALID_REQUEST_ID) {
            logger.warn("{}", getLocalizedText("comm.comm-check.no-quota-left"));
            notifyConnectedListeners();
            return;
        }

        sendAsyncSiteApiRequest(true, siteApiTestLocation, siteResponseListener, "validateSiteApiCheck");
    }

    public boolean sendRequest(final boolean daily, final PointType location,
            final ISiteResponseListener siteResponseListener, final String pollId) {
        if (requestLimiter.getRequestCountIfAvailable() == RequestLimiter.INVALID_REQUEST_ID) {
            logger.debug("{} - Disabled requesting data - request limit has been hit", usageId);
            return false;
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
                            logger.trace("Got response for poll ID: \"{}\"", pollId);
                            notifyConnectedListeners();
                            scheduler.execute(() -> {
                                if (daily) {
                                    siteResponseListener.processDailyResponse(response, pollId);
                                } else {
                                    siteResponseListener.processHourlyResponse(response, pollId);
                                }
                            });
                        }
                    } else {
                        notifyCommFailureListeners(result.getFailure());
                    }
                }
            }
        };

        sendAsyncSiteApiRequest(daily, location, listener, pollId);
        return true;
    }

    public void sendAsyncSiteApiRequest(final boolean daily, final PointType location,
            final Response.CompleteListener listener, final String pollId) {
        final String url = ((daily) ? GET_FORECAST_URL_DAILY : GET_FORECAST_URL_HOURLY)
                .replace(GET_FORECAST_KEY_LATITUDE, location.getLatitude().toString())
                .replace(GET_FORECAST_KEY_LONGITUDE, location.getLongitude().toString());

        final Request request = httpClient.newRequest(url).method(HttpMethod.GET)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_TYPE.toString())
                .timeout(GET_FORECAST_REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        logger.trace("Requesting using Poll ID \"{}\" URL: \"{}\"", pollId, url);

        try {
            apiAuth.addAuthentication(request).send(listener);
        } catch (AuthTokenException ate) {
            notifyAuthenticationListeners(false);
        }
    }

    // Localization functionality

    public String getLocalizedText(String key, @Nullable Object @Nullable... arguments) {
        String result = translationProvider.getText(bundle, key, key, localeProvider.getLocale(), arguments);
        return Objects.nonNull(result) ? result : key;
    }
}
