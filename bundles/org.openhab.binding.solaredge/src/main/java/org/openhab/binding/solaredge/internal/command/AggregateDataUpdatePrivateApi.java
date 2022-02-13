/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.solaredge.internal.model.AggregateDataResponsePrivateApi;
import org.openhab.binding.solaredge.internal.model.AggregateDataResponseTransformerPrivateApi;
import org.openhab.binding.solaredge.internal.model.AggregatePeriod;

/**
 * command that retrieves status values for aggregate data channels via private API
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class AggregateDataUpdatePrivateApi extends AbstractCommandCallback implements SolarEdgeCommand {

    /**
     * the solaredge handler
     */
    private final SolarEdgeHandler handler;
    private final AggregateDataResponseTransformerPrivateApi transformer;

    /**
     * data aggregation level
     */
    private final AggregatePeriod period;

    /**
     * url suffix depending on aggregation level
     */
    private final String urlSuffix;
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
        this.transformer = new AggregateDataResponseTransformerPrivateApi(handler);
        this.period = period;
        switch (period) {
            case DAY:
                this.urlSuffix = PRIVATE_DATA_API_URL_AGGREGATE_DATA_DAY_WEEK_SUFFIX;
                break;
            case WEEK:
                this.urlSuffix = PRIVATE_DATA_API_URL_AGGREGATE_DATA_DAY_WEEK_SUFFIX;
                break;
            case MONTH:
                this.urlSuffix = PRIVATE_DATA_API_URL_AGGREGATE_DATA_MONTH_YEAR_SUFFIX;
                break;
            case YEAR:
                this.urlSuffix = PRIVATE_DATA_API_URL_AGGREGATE_DATA_MONTH_YEAR_SUFFIX;
                break;
            default:
                this.urlSuffix = "";
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
                AggregateDataResponsePrivateApi jsonObject = fromJson(json, AggregateDataResponsePrivateApi.class);
                if (jsonObject != null) {
                    handler.updateChannelStatus(transformer.transform(jsonObject, period));
                }
            }
        }
    }
}
