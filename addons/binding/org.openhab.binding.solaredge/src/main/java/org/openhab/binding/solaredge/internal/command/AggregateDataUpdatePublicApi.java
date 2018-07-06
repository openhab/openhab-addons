/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.internal.command;

import static org.openhab.binding.solaredge.SolarEdgeBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.solaredge.handler.SolarEdgeHandler;
import org.openhab.binding.solaredge.internal.callback.AbstractCommandCallback;
import org.openhab.binding.solaredge.internal.model.AggregateDataResponsePublicApi;
import org.openhab.binding.solaredge.internal.model.AggregatePeriod;
import org.openhab.binding.solaredge.internal.model.DataResponse;

/**
 * command that retrieves status values for aggregate data channels via public API
 *
 * @author Alexander Friese - initial contribution
 */
public class AggregateDataUpdatePublicApi extends AbstractCommandCallback implements SolarEdgeCommand {

    /**
     * the solaredge handler
     */
    private final SolarEdgeHandler handler;

    /**
     * data aggregation level
     */
    private final AggregatePeriod period;

    /**
     * date format which is expected by the API
     */
    private final SimpleDateFormat dateFormat;
    private int retries = 0;

    /**
     * the constructor
     *
     * @param handler
     * @param period
     */
    public AggregateDataUpdatePublicApi(SolarEdgeHandler handler, AggregatePeriod period) {
        super(handler.getConfiguration());
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.handler = handler;
        this.period = period;
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) {
        requestToPrepare.followRedirects(false);
        requestToPrepare.method(HttpMethod.GET);

        String currentDate = dateFormat.format(Calendar.getInstance().getTime());

        requestToPrepare.param(PUBLIC_DATA_API_TIME_UNIT_FIELD, period.toString());
        requestToPrepare.param(PUBLIC_DATA_API_START_TIME_FIELD, currentDate + " " + BEGIN_OF_DAY_TIME);
        requestToPrepare.param(PUBLIC_DATA_API_END_TIME_FIELD, currentDate + " " + END_OF_DAY_TIME);

        return requestToPrepare;
    }

    @Override
    protected String getURL() {
        return PUBLIC_DATA_API_URL + config.getSolarId() + PUBLIC_DATA_API_URL_AGGREGATE_DATA_SUFFIX;
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
                DataResponse jsonObject = gson.fromJson(json, AggregateDataResponsePublicApi.class);
                if (jsonObject != null) {
                    handler.updateChannelStatus(jsonObject.getValues());
                }
            }
        }
    }
}
