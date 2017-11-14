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

import java.nio.charset.StandardCharsets;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.solaredge.handler.SolarEdgeHandler;
import org.openhab.binding.solaredge.internal.callback.AbstractCommandCallback;
import org.openhab.binding.solaredge.internal.model.AbstractAggregateDataResponse;
import org.openhab.binding.solaredge.internal.model.DataResponse;
import org.openhab.binding.solaredge.internal.model.AggregateDayDataResponse;
import org.openhab.binding.solaredge.internal.model.AggregateMonthDataResponse;
import org.openhab.binding.solaredge.internal.model.AggregateWeekDataResponse;
import org.openhab.binding.solaredge.internal.model.AggregateYearDataResponse;

/**
 * command that retrieves status values for aggregate data channels
 *
 * @author Alexander Friese - initial contribution
 *
 */
public class AggregateDataUpdate extends AbstractCommandCallback implements SolarEdgeCommand {

    private final SolarEdgeHandler handler;
    private final AggregatePeriod period;
    private final String urlSuffix;
    private final Class<? extends AbstractAggregateDataResponse> responseClass;
    private int retries = 0;

    public AggregateDataUpdate(SolarEdgeHandler handler, AggregatePeriod period) {
        super(handler.getConfiguration());
        this.handler = handler;
        this.period = period;
        switch (period) {
            case DAY:
                this.responseClass = AggregateDayDataResponse.class;
                this.urlSuffix = DATA_API_URL_AGGREGATE_DATA_DAY_WEEK_SUFFIX;
                break;
            case WEEK:
                this.responseClass = AggregateWeekDataResponse.class;
                this.urlSuffix = DATA_API_URL_AGGREGATE_DATA_DAY_WEEK_SUFFIX;
                break;
            case MONTH:
                this.urlSuffix = DATA_API_URL_AGGREGATE_DATA_MONTH_YEAR_SUFFIX;
                this.responseClass = AggregateMonthDataResponse.class;
                break;
            case YEAR:
                this.urlSuffix = DATA_API_URL_AGGREGATE_DATA_MONTH_YEAR_SUFFIX;
                this.responseClass = AggregateYearDataResponse.class;
                break;
            default:
                this.urlSuffix = null;
                this.responseClass = null;
        }
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) {
        requestToPrepare.followRedirects(false);
        requestToPrepare.param(DATA_API_AGGREGATE_DATA_CHARTFIELD_FIELD, period.toString());
        requestToPrepare.method(HttpMethod.GET);

        return requestToPrepare;
    }

    @Override
    protected String getURL() {
        return DATA_API_URL + config.getSolarId() + urlSuffix;
    }

    @Override
    public void onComplete(Result result) {
        logger.debug("onComplete()");

        if (!HttpStatus.Code.OK.equals(getCommunicationStatus().getHttpCode()) && retries++ < MAX_RETRIES) {
            if (getListener() != null) {
                getListener().update(getCommunicationStatus());
            }
            handler.getWebInterface().executeCommand(this);

        } else {

            String json = getContentAsString(StandardCharsets.UTF_8);
            if (json != null) {
                DataResponse jsonObject = convertJson(json, responseClass);
                if (jsonObject != null) {
                    handler.updateChannelStatus(jsonObject.getValues());
                }
            }
        }
    }
}
