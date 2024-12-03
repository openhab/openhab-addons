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
package org.openhab.binding.myuplink.internal.command;

import static org.openhab.binding.myuplink.internal.MyUplinkBindingConstants.*;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.openhab.binding.myuplink.internal.connector.CommunicationStatus;
import org.openhab.binding.myuplink.internal.handler.MyUplinkThingHandler;
import org.openhab.binding.myuplink.internal.model.GenericResponseTransformer;
import org.openhab.binding.myuplink.internal.model.ResponseTransformer;
import org.openhab.binding.myuplink.internal.model.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.ToNumberPolicy;

/**
 * base class for all commands. common logic should be implemented here
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public abstract class AbstractCommand extends BufferingResponseListener implements MyUplinkCommand {

    public enum RetryOnFailure {
        YES,
        NO
    }

    public enum ProcessFailureResponse {
        YES,
        NO
    }

    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(AbstractCommand.class);

    /**
     * the configuration
     */
    protected final MyUplinkThingHandler handler;

    /**
     * JSON deserializer
     */
    protected final Gson gson;

    /**
     * status code of fulfilled request
     */
    private final CommunicationStatus communicationStatus;

    /**
     * generic transformer which just transfers all values in a plain map.
     */
    protected final ResponseTransformer transformer;

    /**
     * retry counter.
     */
    private int retries = 0;

    /**
     * retry active
     */
    private final RetryOnFailure retryOnFailure;

    /**
     * process error response, e.g. set handler offline on error
     */
    private final ProcessFailureResponse processFailureResponse;

    /**
     * allows further processing of the json result data, if set.
     */
    private final JsonResultProcessor resultProcessor;

    /**
     * the constructor
     */
    public AbstractCommand(MyUplinkThingHandler handler, RetryOnFailure retryOnFailure,
            ProcessFailureResponse processFailureResponse, JsonResultProcessor resultProcessor) {
        this(handler, new GenericResponseTransformer(handler), retryOnFailure, processFailureResponse, resultProcessor);
    }

    public AbstractCommand(MyUplinkThingHandler handler, ResponseTransformer responseTransformer,
            RetryOnFailure retryOnFailure, ProcessFailureResponse processFailureResponse,
            JsonResultProcessor resultProcessor) {
        this.gson = new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create();
        this.communicationStatus = new CommunicationStatus();
        this.transformer = responseTransformer;
        this.handler = handler;
        this.processFailureResponse = processFailureResponse;
        this.retryOnFailure = retryOnFailure;
        this.resultProcessor = resultProcessor;
    }

    /**
     * Log request success
     */
    @Override
    public final void onSuccess(@Nullable Response response) {
        if (response != null) {
            super.onSuccess(response);
            communicationStatus.setHttpCode(HttpStatus.getCode(response.getStatus()));
            logger.debug("[{}] HTTP response {}", getClass().getSimpleName(), response.getStatus());
        }
    }

    /**
     * Log request failure
     */
    @Override
    public final void onFailure(@Nullable Response response, @Nullable Throwable failure) {
        if (failure != null && response != null) {
            super.onFailure(response, failure);
        }
        if (failure != null) {
            logger.info("[{}] Request failed: {}", getClass().getSimpleName(), failure.toString());
            communicationStatus.setError((Exception) failure);
            if (failure instanceof SocketTimeoutException || failure instanceof TimeoutException) {
                communicationStatus.setHttpCode(Code.REQUEST_TIMEOUT);
            } else if (failure instanceof UnknownHostException) {
                communicationStatus.setHttpCode(Code.BAD_GATEWAY);
            } else {
                communicationStatus.setHttpCode(Code.INTERNAL_SERVER_ERROR);
            }
        } else {
            logger.warn("[{}] Request failed", getClass().getSimpleName());
        }
        if (response != null && response.getStatus() > 0) {
            communicationStatus.setHttpCode(HttpStatus.getCode(response.getStatus()));
        }
    }

    /**
     * just for logging of content
     */
    @Override
    public void onContent(@Nullable Response response, @Nullable ByteBuffer content) {
        if (response != null && content != null) {
            super.onContent(response, content);
        }
        var contentAsString = getContentAsString();
        var contentLength = contentAsString == null ? 0 : contentAsString.length();
        logger.debug("[{}] received content, length: {}, encoding: {}", getClass().getSimpleName(), contentLength,
                this.getEncoding());
    }

    /**
     * default handling of successful requests.
     */
    @Override
    public void onComplete(@Nullable Result result) {
        String json = getContentAsString(StandardCharsets.UTF_8);

        logger.debug("[{}] JSON String: {}", getClass().getSimpleName(), json);
        switch (getCommunicationStatus().getHttpCode()) {
            case OK:
            case ACCEPTED:
                onCompleteCodeOk(json);
                break;
            default:
                onCompleteCodeDefault(json);
        }
    }

    /**
     * handling of result in case of HTTP response OK.
     *
     * @param json
     */
    protected void onCompleteCodeOk(@Nullable String json) {
        JsonObject jsonObject = transform(json);
        if (jsonObject != null) {
            logger.debug("[{}] success", getClass().getSimpleName());
            handler.updateChannelStatus(transformer.transform(jsonObject, getChannelGroup()));
            processResult(jsonObject);
        }
    }

    /**
     * handling of result in default case, this means error handling of http codes where no specific handling applies.
     *
     * @param json
     */
    protected void onCompleteCodeDefault(@Nullable String json) {
        JsonObject jsonObject = transform(json);
        if (jsonObject == null) {
            jsonObject = new JsonObject();
        }
        if (processFailureResponse == ProcessFailureResponse.YES) {
            processResult(jsonObject);
        } else {
            logger.warn("command failed, url: {} - code: {} - result: {}", getURL(),
                    getCommunicationStatus().getHttpCode(), jsonObject.toString());
        }

        if (retryOnFailure == RetryOnFailure.YES && retries++ < MAX_RETRIES) {
            handler.enqueueCommand(this);
        }
    }

    /**
     * error safe json transformer.
     *
     * @param json
     * @return
     */
    protected @Nullable JsonObject transform(@Nullable String json) {
        if (json != null) {
            try {
                JsonElement jsonElement = gson.fromJson(json, JsonElement.class);
                JsonObject jsonObject;
                if (jsonElement instanceof JsonObject) {
                    jsonObject = jsonElement.getAsJsonObject();
                } else {
                    jsonObject = new JsonObject();
                    jsonObject.add(JSON_KEY_ROOT_DATA, jsonElement);
                }
                return jsonObject;
            } catch (Exception ex) {
                logger.debug("[{}] JSON could not be parsed: {}\nError: {}", getClass().getSimpleName(), json,
                        ex.getMessage());
            }
        }
        return null;
    }

    /**
     * preparation of the request. will call a hook (prepareRequest) that has to be implemented in the subclass to add
     * content to the request.
     *
     * @throws ValidationException
     */
    @Override
    public void performAction(HttpClient asyncclient, String accessToken) throws ValidationException {
        Request request = asyncclient.newRequest(getURL()).timeout(handler.getBridgeConfiguration().getAsyncTimeout(),
                TimeUnit.SECONDS);
        logger.debug("[{}] running command", getClass().getSimpleName());

        // we want to receive json only, so explicitely set this!
        request.header(HttpHeader.ACCEPT, "application/json");
        request.header(HttpHeader.ACCEPT_ENCODING, StandardCharsets.UTF_8.name());

        // this should be the default for myUplink Cloud API
        request.followRedirects(false);

        // add authentication data for every request. Handling this here makes it obsolete to implement for each and
        // every command
        if (!accessToken.isBlank()) {
            request.header(HttpHeader.AUTHORIZATION, WEB_REQUEST_BEARER_TOKEN_PREFIX + accessToken);
        }

        prepareRequest(request).send(this);
    }

    /**
     * @return returns Http Status Code
     */
    public CommunicationStatus getCommunicationStatus() {
        return communicationStatus;
    }

    /**
     * calls the registered resultProcessor.
     *
     * @param jsonObject
     */
    protected final void processResult(JsonObject jsonObject) {
        try {
            resultProcessor.processResult(getCommunicationStatus(), jsonObject);
        } catch (Exception ex) {
            // this should not happen
            logger.warn("[{}] Exception caught: {}", getClass().getSimpleName(), ex.getMessage(), ex);
        }
    }

    /**
     * default implementation just assumes that we want to retrieve data via GET.
     * can be overridden for any special case and has to prepare the requests with additional parameters, etc
     *
     * @param requestToPrepare the request to prepare
     * @return prepared Request object
     * @throws ValidationException
     */
    protected Request prepareRequest(Request requestToPrepare) throws ValidationException {
        requestToPrepare.method(HttpMethod.GET);
        return requestToPrepare;
    }

    /**
     * concrete implementation has to provide the channel group.
     *
     * @return
     */
    protected abstract String getChannelGroup();

    /**
     * concrete implementation has to provide the URL
     *
     * @return Url
     */
    protected abstract String getURL();
}
