/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.solaredge.internal.callback.AbstractCommandCallback;
import org.openhab.binding.solaredge.internal.handler.SolarEdgeHandler;
import org.openhab.binding.solaredge.internal.model.LiveDataResponse;
import org.openhab.binding.solaredge.internal.model.LiveDataResponseTransformer;

/**
 * command that retrieves status values for live data channels via public API
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class LiveDataUpdatePublicApi extends AbstractCommandCallback implements SolarEdgeCommand {

    private final SolarEdgeHandler handler;
    private final LiveDataResponseTransformer transformer;
    private int retries = 0;

    public LiveDataUpdatePublicApi(SolarEdgeHandler handler) {
        super(handler.getConfiguration());
        this.handler = handler;
        this.transformer = new LiveDataResponseTransformer(handler);
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) {
        requestToPrepare.followRedirects(false);
        requestToPrepare.method(HttpMethod.GET);

        return requestToPrepare;
    }

    @Override
    protected String getURL() {
        return PUBLIC_DATA_API_URL + config.getSolarId() + PUBLIC_DATA_API_URL_LIVE_DATA_SUFFIX;
    }

    @Override
    public void onComplete(@Nullable Result result) {
        logger.debug("onComplete()");
        if (!HttpStatus.Code.OK.equals(getCommunicationStatus().getHttpCode())) {
            updateListenerStatus();
            if (retries++ < MAX_RETRIES) {
                handler.getWebInterface().enqueueCommand(this);
            }
        } else {
            String json = getContentAsString(StandardCharsets.UTF_8);
            if (json != null) {
                logger.debug("JSON String: {}", json);
                LiveDataResponse jsonObject = fromJson(json, LiveDataResponse.class);
                if (jsonObject != null) {
                    handler.updateChannelStatus(transformer.transform(jsonObject));
                }
            }
        }
    }
}
