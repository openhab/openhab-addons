/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.internal.api.manager;

import java.io.IOException;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.mihome.internal.api.JSONResponseHandler;
import org.openhab.binding.mihome.internal.api.constants.JSONResponseConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * A {@link FailingRequestHandler} implementation that updates the status of a {@link Thing} using a
 * {@link ThingCallback}
 *
 * @author Mihaela Memova
 */

public class FailingRequestHandlerImpl implements FailingRequestHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private ThingCallback callback;

    public FailingRequestHandlerImpl(ThingCallback callback) {
        this.callback = callback;
    }

    @Override
    public void handleFailingHttpRequest(ContentResponse response) {
        callback.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, response.getReason());
    }

    @Override
    public void handleFailingJsonRequest(JsonObject jsonResponse) {
        String responseStatus = JSONResponseHandler.getResponseStatus(jsonResponse);
        JsonObject responseData = jsonResponse.get(JSONResponseConstants.DATA_KEY).getAsJsonObject();
        String errorMessage = JSONResponseHandler.getErrorMessageFromResponse(responseData);
        switch (responseStatus) {
            case JSONResponseConstants.RESPONSE_ACCESS_DENIED:
                logger.error("Access to the requested action was not permitted");
                callback.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMessage);
                break;

            case JSONResponseConstants.RESPONSE_INTERNAL_SERVER_ERROR:
                logger.error(
                        "An error outside of your control occurred. Please report these to the team if they persist");
                callback.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
                break;

            case JSONResponseConstants.RESPONSE_MAINTENANCE:
                logger.error("The API is currently unavailable for maintenance work.");
                callback.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
                break;

            case JSONResponseConstants.RESPONSE_PARAMETER_ERROR:
                logger.error("The parameters provided were not suitable for this action");
                callback.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
                break;

            case JSONResponseConstants.RESPONSE_NOT_FOUND:
                logger.warn(errorMessage);
                break;

            case JSONResponseConstants.RESPONSE_VALIDATION_ERROR:
                logger.error("A resource could not be created/updated/removed due to a validation error");
                // in this case the error message from the Mi|Home API's response is not well-formatted so we create a
                // custom message
                String customErrorMessage = "The gateway code was not recognized. Please ensure that the device is connected to the network";
                callback.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        customErrorMessage);
                break;
        }
    }

    @Override
    public void handleIOException(String failedUrl, IOException exception) {
        logger.error(
                "An error occured while trying to execute: {}. Please check your connection", failedUrl, exception);
        callback.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, exception.getMessage());
    }
}
