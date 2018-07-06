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

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.solaredge.handler.SolarEdgeHandler;
import org.openhab.binding.solaredge.internal.callback.AbstractCommandCallback;
import org.openhab.binding.solaredge.internal.model.AbstractAggregateDataResponsePrivateApi;
import org.openhab.binding.solaredge.internal.model.AggregateDayDataResponsePrivateApi;
import org.openhab.binding.solaredge.internal.model.AggregateMonthDataResponsePrivateApi;
import org.openhab.binding.solaredge.internal.model.AggregatePeriod;
import org.openhab.binding.solaredge.internal.model.AggregateWeekDataResponsePrivateApi;
import org.openhab.binding.solaredge.internal.model.AggregateYearDataResponsePrivateApi;
import org.openhab.binding.solaredge.internal.model.DataResponse;

/**
 * command that retrieves status values for aggregate data channels via private API
 *
 * @author Alexander Friese - initial contribution
 */
public class AggregateDataUpdatePrivateApi extends AbstractCommandCallback implements SolarEdgeCommand {

    /**
     * the solaredge handler
     */
    private final SolarEdgeHandler handler;

    /**
     * data aggregation level
     */
    private final AggregatePeriod period;

    /**
     * url suffix depending on aggregation level
     */
    private final String urlSuffix;

    /**
     * response class depending on aggregation level
     */
    private final Class<? extends AbstractAggregateDataResponsePrivateApi> responseClass;
    private int retries = 0;

    /**
     * the constructor
     *
     * @param handler
     * @param period
     */
    public AggregateDataUpdatePrivateApi(SolarEdgeHandler handler, AggregatePeriod period) {
        super(handler.getConfiguration());
        this.handler = handler;
        this.period = period;
        switch (period) {
            case DAY:
                this.responseClass = AggregateDayDataResponsePrivateApi.class;
                this.urlSuffix = PRIVATE_DATA_API_URL_AGGREGATE_DATA_DAY_WEEK_SUFFIX;
                break;
            case WEEK:
                this.responseClass = AggregateWeekDataResponsePrivateApi.class;
                this.urlSuffix = PRIVATE_DATA_API_URL_AGGREGATE_DATA_DAY_WEEK_SUFFIX;
                break;
            case MONTH:
                this.urlSuffix = PRIVATE_DATA_API_URL_AGGREGATE_DATA_MONTH_YEAR_SUFFIX;
                this.responseClass = AggregateMonthDataResponsePrivateApi.class;
                break;
            case YEAR:
                this.urlSuffix = PRIVATE_DATA_API_URL_AGGREGATE_DATA_MONTH_YEAR_SUFFIX;
                this.responseClass = AggregateYearDataResponsePrivateApi.class;
                break;
            default:
                this.urlSuffix = null;
                this.responseClass = null;
        }
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) {
        requestToPrepare.followRedirects(false);
        requestToPrepare.param(PRIVATE_DATA_API_AGGREGATE_DATA_CHARTFIELD_FIELD, period.toString());
        requestToPrepare.method(HttpMethod.GET);

        return requestToPrepare;
    }

    @Override
    protected String getURL() {
        return PRIVATE_DATA_API_URL + config.getSolarId() + urlSuffix;
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
                DataResponse jsonObject = gson.fromJson(json, responseClass);
                if (jsonObject != null) {
                    handler.updateChannelStatus(jsonObject.getValues());
                }
            }
        }
    }
}
