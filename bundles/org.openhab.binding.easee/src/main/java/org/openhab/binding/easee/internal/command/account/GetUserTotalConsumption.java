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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.easee.internal.command.AbstractCommand;
import org.openhab.binding.easee.internal.command.JsonResultProcessor;
import org.openhab.binding.easee.internal.handler.EaseeThingHandler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * implements the get yearly consumption by user api call and computes a total sum.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class GetUserTotalConsumption extends AbstractCommand {

    private final String url;

    public GetUserTotalConsumption(EaseeThingHandler handler, String userId, JsonResultProcessor resultProcessor) {
        // retry does not make much sense as it is a polling command, command should always succeed therefore update
        // handler on failure.
        super(handler, RetryOnFailure.NO, ProcessFailureResponse.YES, resultProcessor);
        String siteId = handler.getBridgeConfiguration().getSiteId();
        this.url = USER_TOTAL_CONSUMPTION_URL.replaceAll("\\{siteId\\}", siteId).replaceAll("\\{userId\\}", userId);
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
        JsonArray yearlyData = transform(json, JsonArray.class);
        if (yearlyData != null) {
            double totalConsumption = 0;
            for (JsonElement element : yearlyData) {
                JsonObject yearEntry = element.getAsJsonObject();
                if (yearEntry.has(JSON_KEY_TOTAL_ENERGY_USAGE)) {
                    totalConsumption += yearEntry.get(JSON_KEY_TOTAL_ENERGY_USAGE).getAsDouble();
                }
            }

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(CHANNEL_USER_TOTAL_CONSUMPTION, totalConsumption);
            handler.updateChannelStatus(transformer.transform(jsonObject, getChannelGroup()));
            processResult(jsonObject);
        }
    }
}
