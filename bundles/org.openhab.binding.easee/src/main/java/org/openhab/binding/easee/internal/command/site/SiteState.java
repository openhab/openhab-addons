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
package org.openhab.binding.easee.internal.command.site;

import static org.openhab.binding.easee.internal.EaseeBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.easee.internal.Utils;
import org.openhab.binding.easee.internal.command.AbstractCommand;
import org.openhab.binding.easee.internal.command.JsonResultProcessor;
import org.openhab.binding.easee.internal.handler.EaseeChargerHandler;
import org.openhab.binding.easee.internal.handler.EaseeThingHandler;
import org.openhab.binding.easee.internal.model.GenericResponseTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * implements the state api call of the site in order to retrieve charger states.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class SiteState extends AbstractCommand {
    private final String url;
    private final Map<String, EaseeChargerHandler> chargerHandlers;

    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(SiteState.class);

    public SiteState(EaseeThingHandler handler, String siteId, Map<String, EaseeChargerHandler> chargerHandlers,
            JsonResultProcessor resultProcessor) {
        // retry does not make much sense as it is a polling command, command should always succeed therefore update
        // handler on failure.
        super(handler, RetryOnFailure.NO, ProcessFailureResponse.YES, resultProcessor);
        this.url = SITE_STATE_URL.replaceAll("\\{siteId\\}", siteId);
        this.chargerHandlers = chargerHandlers;
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
        return CHANNEL_GROUP_CHARGER_STATE;
    }

    /**
     * override default behaviour: extract ChargerState from SiteState
     */
    @Override
    protected void onCompleteCodeOk(@Nullable String json) {
        JsonObject jsonObject = transform(json);

        if (jsonObject != null) {
            logger.debug("success");
            JsonArray circuitStates = jsonObject.getAsJsonArray(JSON_KEY_CIRCUIT_STATES);
            for (JsonElement circuitState : circuitStates) {
                JsonArray chargerDataArray = circuitState.getAsJsonObject().getAsJsonArray(JSON_KEY_CHARGER_STATES);
                for (JsonElement chargerData : chargerDataArray) {
                    processChargerStateData(chargerData.getAsJsonObject());
                }
            }
        }
    }

    /**
     * processes charger data, also sets online status retrieved from API.
     *
     * @param chargerData
     */
    private void processChargerStateData(JsonObject chargerData) {
        JsonElement chargerId = chargerData.get(JSON_KEY_CHARGER_ID);
        String id = chargerId != null ? chargerId.getAsString() : null;
        if (id != null) {
            EaseeChargerHandler handler = chargerHandlers.get(id);
            if (handler != null) {
                GenericResponseTransformer transformer = new GenericResponseTransformer(handler);
                JsonElement chargerState = chargerData.getAsJsonObject().get(JSON_KEY_CHARGER_STATE);
                JsonObject jsonObject = chargerState.getAsJsonObject();
                Boolean isOnline = Utils.getAsBool(jsonObject, JSON_KEY_ONLINE);

                handler.updateChannelStatus(transformer.transform(jsonObject, getChannelGroup()));
                handler.setOnline(isOnline == null ? false : isOnline);
            }
        }
    }
}
