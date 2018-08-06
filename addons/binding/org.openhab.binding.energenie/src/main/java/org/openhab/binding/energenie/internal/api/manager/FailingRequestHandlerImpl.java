/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.internal.api.manager;

import java.io.IOException;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.energenie.internal.api.JsonResponseUtil;
import org.openhab.binding.energenie.internal.api.constants.JsonResponseConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * A {@link FailingRequestHandler} implementation that updates the status of a {@link Thing} using a
 * {@link ThingCallback} or just log an error message if a callback is missing.
 *
 * @author Mihaela Memova - Initial contribution
 */

public class FailingRequestHandlerImpl implements FailingRequestHandler {

    private final Logger logger = LoggerFactory.getLogger(FailingRequestHandlerImpl.class);
    private ThingCallback callback;

    public FailingRequestHandlerImpl(ThingCallback callback) {
        this.callback = callback;
    }

    public FailingRequestHandlerImpl() {
        this.callback = null;
    }

    @Override
    public void handleFailingHttpRequest(ContentResponse response) {
        if (callback != null) {
            callback.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    response.getReason());
        } else {
            logger.error("HTTP request failed: {}", response.getReason());
        }
    }

    @Override
    public void handleFailingJsonRequest(JsonObject jsonResponse) {
        String responseStatus = JsonResponseUtil.getResponseStatus(jsonResponse);
        JsonObject responseData = jsonResponse.get(JsonResponseConstants.DATA_KEY).getAsJsonObject();
        String errorMessage = JsonResponseUtil.getErrorMessageFromResponse(responseData);
        if (callback != null) {
            switch (responseStatus) {
                case JsonResponseConstants.RESPONSE_ACCESS_DENIED:
                    callback.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            errorMessage);
                    break;

                case JsonResponseConstants.RESPONSE_INTERNAL_SERVER_ERROR:
                case JsonResponseConstants.RESPONSE_MAINTENANCE:
                case JsonResponseConstants.RESPONSE_PARAMETER_ERROR:
                case JsonResponseConstants.RESPONSE_VALIDATION_ERROR:
                    callback.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            errorMessage);
                    break;
                case JsonResponseConstants.RESPONSE_NOT_FOUND:
                    logger.warn("{}", errorMessage);
                    break;
            }
        } else {
            logger.error(errorMessage);
        }
    }

    @Override
    public void handleIOException(String failedUrl, IOException exception) {
        if (callback != null) {
            callback.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    exception.getMessage());
        } else {
            logger.error("An error occured while trying to execute: {}. Please check your connection", failedUrl,
                    exception);
        }
    }
}
