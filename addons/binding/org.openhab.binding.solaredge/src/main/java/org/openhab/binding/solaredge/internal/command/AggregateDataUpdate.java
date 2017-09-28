/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.internal.command;

import static org.openhab.binding.solaredge.SolarEdgeBindingConstants.*;

import java.nio.charset.Charset;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.solaredge.handler.SolarEdgeHandler;
import org.openhab.binding.solaredge.internal.callback.AbstractCommandCallback;
import org.openhab.binding.solaredge.internal.model.AggregateDataResponse;

/**
 * generic command that retrieves status values for all channels defined in {@link VVM320Channels}
 *
 * @author afriese
 *
 */
public class AggregateDataUpdate extends AbstractCommandCallback implements SolarEdgeCommand {

    private final SolarEdgeHandler handler;
    private int retries = 0;

    public AggregateDataUpdate(SolarEdgeHandler handler) {
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
        return DATA_API_URL + config.getSolarId() + DATA_API_URL_AGGREGATE_DATA_SUFFIX;
    }

    @Override
    public void onComplete(Result result) {
        logger.debug("onComplete()");

        if (!getCommunicationStatus().getHttpCode().equals(HttpStatus.OK_200) && retries++ < MAX_RETRIES) {
            if (getListener() != null) {
                getListener().update(getCommunicationStatus());
            }
            handler.getWebInterface().executeCommand(this);
        }

        String json = getContentAsString(Charset.forName("UTF-8"));
        if (json != null) {
            AggregateDataResponse jsonObject = convertJson(json, AggregateDataResponse.class);
            if (jsonObject != null) {
                handler.updateChannelStatus(jsonObject.getValues());
            }
        }
    }
}
