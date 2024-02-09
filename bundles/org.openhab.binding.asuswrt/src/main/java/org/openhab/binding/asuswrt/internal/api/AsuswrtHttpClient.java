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
package org.openhab.binding.asuswrt.internal.api;

import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingConstants.JSON_MEMBER_TOKEN;
import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingSettings.*;
import static org.openhab.binding.asuswrt.internal.constants.AsuswrtErrorConstants.*;
import static org.openhab.binding.asuswrt.internal.helpers.AsuswrtUtils.getValueOrDefault;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpResponse;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.asuswrt.internal.helpers.AsuswrtUtils;
import org.openhab.binding.asuswrt.internal.things.AsuswrtRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The {@link AsuswrtHttpClient} is used for (a)synchronous HTTP requests.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtHttpClient {
    private final Logger logger = LoggerFactory.getLogger(AsuswrtHttpClient.class);
    private Gson gson = new Gson();
    protected AsuswrtRouter router;
    protected final String uid;
    public AsuswrtCookie cookieStore = new AsuswrtCookie();

    public AsuswrtHttpClient(AsuswrtRouter router) {
        this.router = router;
        uid = router.getUID().toString();
    }

    /*
     * HTTP actions
     */

    /**
     * Sends a synchronous HTTP request.
     *
     * The result will be handled in {@link #handleHttpSuccessResponse(String, String)} or
     * {@link #handleHttpResultError(Throwable)}.
     *
     * If the response should be returned use {@link #getSyncRequest(String, String)} instead.
     *
     * @param url the URL the request is sent to
     * @param payload the payload to send
     * @param command the command to perform
     */
    protected void sendSyncRequest(String url, String payload, String command) {
        ContentResponse response = getSyncRequest(url, payload);
        if (response != null) {
            handleHttpSuccessResponse(response.getContentAsString(), command);
        }
    }

    /**
     * Sends a synchronous HTTP request.
     *
     * @param url the URL the request is sent to
     * @param payload the payload to send
     * @return {@link ContentResponse} of the request
     */
    protected @Nullable ContentResponse getSyncRequest(String url, String payload) {
        logger.trace("({}) sendRequest '{}' to '{}' with cookie '{}'", uid, payload, url, cookieStore.getCookie());
        Request httpRequest = this.router.getHttpClient().newRequest(url).method(HttpMethod.POST.toString());

        // Set header
        httpRequest = setHeaders(httpRequest);
        httpRequest.timeout(HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS);

        // Add request body
        httpRequest.content(new StringContentProvider(payload, HTTP_CONTENT_CHARSET), HTTP_CONTENT_TYPE);
        try {
            return httpRequest.send();
        } catch (Exception e) {
            handleHttpResultError(e);
        }
        return null;
    }

    /**
     * Sends an asynchronous HTTP request so it does not wait for an answer.
     *
     * The result will be handled in {@link #handleHttpSuccessResponse(String, String)} or
     * {@link #handleHttpResultError(Throwable)}.
     *
     * @param url the URL to which the request is sent to
     * @param payload the payload data
     * @param command command to execute, this will handle ResponseType
     */
    protected void sendAsyncRequest(String url, String payload, String command) {
        logger.trace("({}) sendAsyncRequest to '{}' with cookie '{}'", uid, url, cookieStore.getCookie());
        try {
            Request httpRequest = router.getHttpClient().newRequest(url).method(HttpMethod.POST.toString());

            // Set header
            httpRequest = setHeaders(httpRequest);

            // Add request body
            httpRequest.content(new StringContentProvider(payload, HTTP_CONTENT_CHARSET), HTTP_CONTENT_TYPE);

            httpRequest.timeout(HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS).send(new BufferingResponseListener() {
                @NonNullByDefault({})
                @Override
                public void onComplete(Result result) {
                    final HttpResponse response = (HttpResponse) result.getResponse();
                    if (result.getFailure() != null) {
                        // Handle result errors
                        handleHttpResultError(result.getFailure());
                    } else if (response.getStatus() != 200) {
                        logger.debug("({}) sendAsyncRequest response error '{}'", uid, response.getStatus());
                        router.errorHandler.raiseError(ERR_RESPONSE, getContentAsString());
                    } else {
                        // Request successful
                        String rBody = getContentAsString();
                        logger.trace("({}) requestCompleted '{}'", uid, rBody);
                        // Handle result
                        handleHttpSuccessResponse(rBody, command);
                    }
                }
            });
        } catch (Exception e) {
            router.errorHandler.raiseError(e);
        }
    }

    /**
     * Sets HTTP headers.
     */
    private Request setHeaders(Request httpRequest) {
        // Set header
        httpRequest.header("content-type", HTTP_CONTENT_TYPE);
        httpRequest.header("user-agent", HTTP_USER_AGENT);
        if (cookieStore.isValid()) {
            httpRequest.header("cookie", cookieStore.getCookie());
        }
        return httpRequest;
    }

    /*
     * Response handling
     */

    /**
     * Handles HTTP result failures.
     *
     * @param e the exception
     * @param payload full payload for debugging
     */
    protected void handleHttpResultError(Throwable e, String payload) {
        String errorMessage = getValueOrDefault(e.getMessage(), "");

        if (e instanceof TimeoutException) {
            logger.debug("({}) sendAsyncRequest timeout'{}'", uid, errorMessage);
        } else if (e instanceof InterruptedException) {
            logger.debug("({}) sending request interrupted: {}", uid, e.toString());
        } else {
            logger.debug("({}) sendAsyncRequest failed'{}'", uid, errorMessage);
        }
    }

    protected void handleHttpResultError(Throwable e) {
        handleHttpResultError(e, "");
    }

    /**
     * Handles a successful HTTP response.
     *
     * @param responseBody response body as string
     * @param command command constant which was sent
     */
    protected void handleHttpSuccessResponse(String responseBody, String command) {
    }

    /**
     * Sets a cookie from a response.
     */
    protected void setCookieFromResponse(ContentResponse response) {
        cookieStore.resetCookie();
        if (response.getStatus() == 200) {
            String rBody = response.getContentAsString();
            logger.trace("({}) received response '{}'", uid, rBody);
            try {
                /* get json object 'asus_token' */
                JsonObject jsonObject = gson.fromJson(rBody, JsonObject.class);
                if (jsonObject != null && jsonObject.has(JSON_MEMBER_TOKEN)) {
                    String token = jsonObject.get(JSON_MEMBER_TOKEN).getAsString();
                    this.cookieStore.setCookie("asus_token=" + token);
                }
            } catch (Exception e) {
                logger.debug("({}) {} on login request '{}'", uid, ERR_RESPONSE, e.getMessage());
                router.errorHandler.raiseError(ERR_RESPONSE, e.getMessage());
            }
        } else {
            String reason = AsuswrtUtils.getValueOrDefault(response.getReason(), "");
            router.errorHandler.raiseError(ERR_RESPONSE, reason);
        }
    }

    /**
     * Gets JSON from a response.
     */
    protected JsonObject getJsonFromResponse(ContentResponse response) {
        return getJsonFromString(response.getContentAsString());
    }

    /**
     * Gets JSON from a response.
     */
    protected JsonObject getJsonFromString(String responseBody) {
        try {
            JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
            logger.trace("({}) received result: {}", uid, responseBody);
            /* get error code (0=success) */
            if (jsonObject != null) {
                return jsonObject;
            }
        } catch (Exception e) {
            logger.debug("({}) {} {}", uid, ERR_JSON_FORMAT, responseBody);
            router.getErrorHandler().raiseError(e);
        }
        return new JsonObject();
    }
}
