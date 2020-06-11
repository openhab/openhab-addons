/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.solaredge.internal.command;

import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.*;

import java.nio.charset.StandardCharsets;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.solaredge.internal.callback.AbstractCommandCallback;
import org.openhab.binding.solaredge.internal.handler.SolarEdgeHandler;
import org.openhab.binding.solaredge.internal.model.DataResponse;
import org.openhab.binding.solaredge.internal.model.LiveDataResponseMeterless;

/**
 * command that retrieves status values for live data channels via public API
 *
 * @author Alexander Friese - initial contribution
 */
public class LiveDataUpdateMeterless extends AbstractCommandCallback implements SolarEdgeCommand {

    private final SolarEdgeHandler handler;
    private int retries = 0;

    public LiveDataUpdateMeterless(SolarEdgeHandler handler) {
        super(handler.getConfiguration());
        this.handler = handler;
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) {
        requestToPrepare.followRedirects(false);
        requestToPrepare.method(HttpMethod.GET);

        return requestToPrepare;
    }

    @Override
    protected String getURL() {
        return PUBLIC_DATA_API_URL + config.getSolarId() + PUBLIC_DATA_API_URL_LIVE_DATA_METERLESS_SUFFIX;
    }

    @Override
    public void onComplete(Result result) {
        logger.debug("onComplete()");

        if (!HttpStatus.Code.OK.equals(getCommunicationStatus().getHttpCode())) {
            if (getListener() != null) {
                getListener().update(getCommunicationStatus());
            }
            if (retries++ < MAX_RETRIES) {
                handler.getWebInterface().enqueueCommand(this);
            }
        } else {
            String json = getContentAsString(StandardCharsets.UTF_8);
            if (json != null) {
                logger.debug("JSON String: {}", json);
                DataResponse jsonObject = gson.fromJson(json, LiveDataResponseMeterless.class);
                handler.updateChannelStatus(jsonObject.getValues());
            }
        }
    }
}
