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
package org.openhab.binding.asuswrt.internal.api;

import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingConstants.*;
import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingSettings.*;
import static org.openhab.binding.asuswrt.internal.constants.AsuswrtErrorConstants.*;
import static org.openhab.binding.asuswrt.internal.helpers.AsuswrtUtils.*;

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
 * ASUSWRT HTTP CLIENT
 * 
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtHttpClient {
    private final Logger logger = LoggerFactory.getLogger(AsuswrtHttpClient.class);

    private Gson gson = new Gson();
    protected String cookie = "";
    protected String token = "";
    protected Long cookieTimeStamp = 0L;
    protected AsuswrtRouter router;
    protected final String uid;

    /**
     * INIT CLASS
     * 
     * @param router asuswrt router thing
     */
    public AsuswrtHttpClient(AsuswrtRouter router) {
        this.router = router;
        this.uid = router.getUID().toString();
    }

    /***********************************
     *
     * HTTP-ACTIONS
     *
     ************************************/

    /**
     * SEND SYNCHRON HTTP-REQUEST
     * result will be handled in 'handleHttpSuccessResponse' or 'handleHttpResultError'
     * If response should be returned use 'getSyncRequest' instead
     * 
     * @param url url request is sent to
     * @param payload payload (String) to send
     * @param command command to perform
     */
    protected void sendSyncRequest(String url, String payload, String command) {
        ContentResponse response = getSyncRequest(url, payload);
        if (response != null) {
            handleHttpSuccessResponse(response.getContentAsString(), command);
        }
    }

    /**
     * SEND SYNCHRON HTTP-REQUEST
     * 
     * @param url url request is sent to
     * @param payload payload (String) to send
     * @return ContentResponse of request
     */
    @Nullable
    protected ContentResponse getSyncRequest(String url, String payload) {
        logger.trace("({}) sendRequest '{}' to '{}' with cookie '{}'", uid, payload, url, this.cookie);
        Request httpRequest = this.router.getHttpClient().newRequest(url).method(HttpMethod.POST.toString());

        /* set header */
        httpRequest = setHeaders(httpRequest);
        httpRequest.timeout(HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS);

        /* add request body */
        httpRequest.content(new StringContentProvider(payload, HTTP_CONTENT_CHARSET), HTTP_CONTENT_TYPE);
        try {
            return httpRequest.send();
        } catch (Exception e) {
            handleHttpResultError(e);
        }
        return null;
    }

    /**
     * SEND ASYNCHRONOUS HTTP-REQUEST
     * (don't wait for awnser with programm code)
     * result will be handled in 'handleHttpSuccessResponse' or 'handleHttpResultError'
     * 
     * @param url string url request is sent to
     * @param payload data-payload
     * @param command command executed - this will handle RepsonseType
     */
    protected void sendAsyncRequest(String url, String payload, String command) {
        logger.trace("({}) sendAsncRequest to '{}' with cookie '{}'", uid, url, this.cookie);
        try {
            Request httpRequest = router.getHttpClient().newRequest(url).method(HttpMethod.POST.toString());

            /* set header */
            httpRequest = setHeaders(httpRequest);

            /* add request body */
            httpRequest.content(new StringContentProvider(payload, HTTP_CONTENT_CHARSET), HTTP_CONTENT_TYPE);

            httpRequest.timeout(HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS).send(new BufferingResponseListener() {
                @NonNullByDefault({})
                @Override
                public void onComplete(Result result) {
                    final HttpResponse response = (HttpResponse) result.getResponse();
                    if (result.getFailure() != null) {
                        /* handle result errors */
                        handleHttpResultError(result.getFailure());
                    } else if (response.getStatus() != 200) {
                        logger.debug("({}) sendAsyncRequest response error'{}'", uid, response.getStatus());
                        router.errorHandler.raiseError(ERR_RESPONSE, getContentAsString());
                    } else {
                        /* request succesfull */
                        String rBody = getContentAsString();
                        logger.trace("({}) requestCompleted '{}'", uid, rBody);
                        /* handle result */
                        handleHttpSuccessResponse(rBody, command);
                    }
                }
            });
        } catch (Exception e) {
            router.errorHandler.raiseError(e);
        }
    }

    /**
     * SET HTTP-HEADERS
     */
    private Request setHeaders(Request httpRequest) {
        /* set header */
        httpRequest.header("content-type", HTTP_CONTENT_TYPE);
        httpRequest.header("user-agent", HTTP_USER_AGENT);
        if (!this.cookie.isBlank()) {
            httpRequest.header("cookie", this.cookie);
        }
        return httpRequest;
    }

    /***********************************
     * 
     * RESPONSE HANDLING
     *
     ************************************/

    /**
     * Handle HTTP-Result Failures
     * 
     * @param e Throwable exception
     * @param payload full payload for debugging
     */
    protected void handleHttpResultError(Throwable e, String payload) {
        String errorMessage = getValueOrDefault(e.getMessage(), "");

        if (e instanceof TimeoutException) {
            logger.debug("({}) sendAsyncRequest timeout'{}'", uid, errorMessage);
            router.errorHandler.raiseError(ERR_CONN_TIMEOUT, errorMessage);
        } else if (e instanceof InterruptedException) {
            logger.debug("({}) sending request interrupted: {}", uid, e.toString());
            router.errorHandler.raiseError(new Exception(e), payload);
        } else {
            logger.debug("({}) sendAsyncRequest failed'{}'", uid, errorMessage);
            router.errorHandler.raiseError(new Exception(e), errorMessage);
        }
    }

    protected void handleHttpResultError(Throwable e) {
        handleHttpResultError(e, "");
    }

    /**
     * Handle Sucessfull HTTP Response
     * delegated to connector-class
     * 
     * @param responseBody response body as string
     * @param command command constant which was sent
     */
    protected void handleHttpSuccessResponse(String responseBody, String command) {
    }

    /**
     * GET COOKIE FROM RESPONSE
     * 
     * @param response
     */
    protected void setCookieFromResponse(ContentResponse response) {
        resetToken();
        if (response.getStatus() == 200) {
            String rBody = response.getContentAsString();
            logger.trace("({}) received response '{}'", uid, rBody);
            try {
                /* get json object 'asus_token' */
                JsonObject jsonObject = gson.fromJson(rBody, JsonObject.class);
                if (jsonObject != null && jsonObject.has(JSON_MEMBER_TOKEN)) {
                    this.token = jsonObject.get(JSON_MEMBER_TOKEN).getAsString();
                    this.cookie = "asus_token=" + token;
                    this.cookieTimeStamp = System.currentTimeMillis();
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
     * reset Token (Logout)
     */
    protected void resetToken() {
        this.token = "";
        this.cookie = "";
    }

    /**
     * get Json from response
     * 
     * @param responseBody
     * @return JsonObject with result
     */
    protected JsonObject getJsonFromResponse(ContentResponse response) {
        return getJsonFromString(response.getContentAsString());
    }

    /**
     * get Json from response
     * 
     * @param responseBody
     * @return JsonObject with result
     */
    protected JsonObject getJsonFromString(String responseBody) {
        try {
            JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
            logger.trace("({}) received result: {}", uid, responseBody);
            /* get errocode (0=success) */
            if (jsonObject != null) {
                return jsonObject;
            }
        } catch (Exception e) {
            logger.debug("({}) {} {}", uid, ERR_JSON_FOMRAT, responseBody);
            router.getErrorHandler().raiseError(e);
        }
        return new JsonObject();
    }
}
