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
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.solaredge.internal.callback.AbstractCommandCallback;
import org.openhab.binding.solaredge.internal.handler.SolarEdgeHandler;
import org.openhab.binding.solaredge.internal.model.AggregateDataResponsePublicApi;
import org.openhab.binding.solaredge.internal.model.AggregateDataResponseTransformerPublicApi;
import org.openhab.binding.solaredge.internal.model.AggregatePeriod;

/**
 * command that retrieves status values for aggregate data channels via public API
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class AggregateDataUpdatePublicApi extends AbstractCommandCallback implements SolarEdgeCommand {

    /**
     * the solaredge handler
     */
    private final SolarEdgeHandler handler;
    private final AggregateDataResponseTransformerPublicApi transformer;

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
        this.transformer = new AggregateDataResponseTransformerPublicApi(handler);
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
                AggregateDataResponsePublicApi jsonObject = fromJson(json, AggregateDataResponsePublicApi.class);
                if (jsonObject != null) {
                    handler.updateChannelStatus(transformer.transform(jsonObject, period));
                }
            }
        }
    }
}
