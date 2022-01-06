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
package org.openhab.binding.tradfri.internal;

import java.util.concurrent.CompletableFuture;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * The {@link TradfriCoapHandler} is used to handle the asynchronous coap reponses.
 * It can either be used with a callback class or with a future.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class TradfriCoapHandler implements CoapHandler {

    private final Logger logger = LoggerFactory.getLogger(TradfriCoapHandler.class);

    private @Nullable CoapCallback callback;
    private @Nullable CompletableFuture<String> future;

    /**
     * Constructor for using a callback
     *
     * @param callback the callback to use for responses
     */
    public TradfriCoapHandler(CoapCallback callback) {
        this.callback = callback;
    }

    /**
     * Constructor for using a future
     *
     * @param future the future to use for responses
     */
    public TradfriCoapHandler(CompletableFuture<String> future) {
        this.future = future;
    }

    @Override
    public void onLoad(@Nullable CoapResponse response) {
        if (response == null) {
            logger.trace("received empty CoAP response");
            return;
        }
        logger.debug("CoAP response\noptions: {}\npayload: {}", response.getOptions(), response.getResponseText());
        if (response.isSuccess()) {
            final CoapCallback callback = this.callback;
            if (callback != null) {
                try {
                    callback.onUpdate(JsonParser.parseString(response.getResponseText()));
                    callback.setStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                } catch (JsonParseException e) {
                    logger.warn("Observed value is no valid json: {}, {}", response.getResponseText(), e.getMessage());
                }
            }
            final CompletableFuture<String> future = this.future;
            if (future != null) {
                String data = response.getResponseText();
                future.complete(data);
            }
        } else {
            logger.debug("CoAP error {}", response.getCode());
            if (callback != null) {
                callback.setStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
            if (future != null) {
                future.completeExceptionally(new RuntimeException("Response " + response.getCode().toString()));
            }
        }
    }

    @Override
    public void onError() {
        logger.debug("CoAP onError");
        if (callback != null) {
            callback.setStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        if (future != null) {
            future.completeExceptionally(new RuntimeException("CoAP GET resulted in an error."));
        }
    }
}
