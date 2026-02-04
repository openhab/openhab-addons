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
package org.openhab.binding.easee.internal.command.account;

import static org.openhab.binding.easee.internal.EaseeBindingConstants.*;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.easee.internal.command.AbstractCommand;
import org.openhab.binding.easee.internal.command.JsonResultProcessor;
import org.openhab.binding.easee.internal.handler.EaseeThingHandler;

import com.google.gson.JsonObject;

/**
 * implements the get total kWh amount by user api call.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class GetUserTotalConsumption extends AbstractCommand {
    private static final String EPOCH_START = "2018-01-01T00:00:00Z";

    private final String url;

    public GetUserTotalConsumption(EaseeThingHandler handler, String userId, JsonResultProcessor resultProcessor) {
        super(handler, RetryOnFailure.NO, ProcessFailureResponse.NO, resultProcessor);
        this.url = USER_TOTAL_CONSUMPTION_URL.replaceAll("\\{userId\\}", userId).replaceAll("\\{from\\}", EPOCH_START)
                .replaceAll("\\{to\\}", Instant.now().toString());
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) {
        requestToPrepare.method(HttpMethod.GET);
        return requestToPrepare;
    }

    @Override
    protected String getURL() {
        return url;
    }

    @Override
    protected String getChannelGroup() {
        return CHANNEL_GROUP_USER_CONSUMPTION;
    }

    @Override
    protected void onCompleteCodeOk(@Nullable String json) {
        if (json != null) {
            try {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty(CHANNEL_USER_TOTAL_CONSUMPTION, Double.parseDouble(json.trim()));
                handler.updateChannelStatus(transformer.transform(jsonObject, getChannelGroup()));
                processResult(jsonObject);
            } catch (NumberFormatException ex) {
                // response was not a plain number, try default handling
                super.onCompleteCodeOk(json);
            }
        }
    }
}
