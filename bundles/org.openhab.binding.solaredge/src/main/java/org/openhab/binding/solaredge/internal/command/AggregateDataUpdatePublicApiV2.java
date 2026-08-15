/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.solaredge.internal.connector.StatusUpdateListener;
import org.openhab.binding.solaredge.internal.handler.SolarEdgeHandler;
import org.openhab.binding.solaredge.internal.model.AggregatePeriod;
import org.openhab.binding.solaredge.internal.model.MeasurementsResponsePublicApiV2;
import org.openhab.binding.solaredge.internal.model.MeasurementsResponseTransformerPublicApiV2;

/**
 * Retrieves production energy from the SolarEdge Monitoring API V2 Basic Monitoring API.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class AggregateDataUpdatePublicApiV2 extends AbstractCommand {

    private final SolarEdgeHandler handler;
    private final boolean yearly;
    private final MeasurementsResponseTransformerPublicApiV2 transformer;
    private int retries;

    public AggregateDataUpdatePublicApiV2(SolarEdgeHandler handler, boolean yearly, StatusUpdateListener listener) {
        super(handler.getConfiguration(), listener, handler::getPublicApiV2Credential,
                handler::invalidatePublicApiV2Credential, handler::recordPublicApiV2Request,
                response -> handler.updatePublicApiV2RateLimit(response.getHeaders().get("x-ratelimit-limit-minute"),
                        response.getHeaders().get("x-ratelimit-remaining-minute"),
                        response.getHeaders().get("Retry-After")));
        this.handler = handler;
        this.yearly = yearly;
        this.transformer = new MeasurementsResponseTransformerPublicApiV2(handler);
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        OffsetDateTime from = yearly ? aggregateStart(now, AggregatePeriod.YEAR) : earliestRecentAggregateStart(now);
        return requestToPrepare.followRedirects(false).method(HttpMethod.GET)
                .param(PUBLIC_DATA_API_V2_FROM_FIELD, from.toString())
                .param(PUBLIC_DATA_API_V2_TO_FIELD, now.toString())
                .param(PUBLIC_DATA_API_V2_RESOLUTION_FIELD, yearly ? "MONTH" : "DAY")
                .param(PUBLIC_DATA_API_V2_UNIT_FIELD, "WH");
    }

    @Override
    protected String getURL() {
        return PUBLIC_DATA_API_V2_URL + config.getSolarId() + PUBLIC_DATA_API_V2_ENERGY_SUFFIX;
    }

    @Override
    public void onComplete(@Nullable Result result) {
        if (!HttpStatus.Code.OK.equals(getCommunicationStatus().getHttpCode())) {
            if (isRetryable() && retries++ < MAX_RETRIES) {
                handler.getWebInterface().enqueueCommand(this);
                return;
            }
        } else {
            String json = getContentAsString(StandardCharsets.UTF_8);
            if (json != null) {
                MeasurementsResponsePublicApiV2 response = fromJson(json, MeasurementsResponsePublicApiV2.class);
                if (response != null) {
                    OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
                    AggregatePeriod[] periods = yearly ? new AggregatePeriod[] { AggregatePeriod.YEAR }
                            : new AggregatePeriod[] { AggregatePeriod.DAY, AggregatePeriod.WEEK,
                                    AggregatePeriod.MONTH };
                    for (AggregatePeriod period : periods) {
                        OffsetDateTime from = aggregateStart(now, period);
                        handler.updateChannelStatus(transformer.transformEnergy(response, period, from));
                        handler.updatePublicApiV2AggregateProduction(period, transformer.totalValue(response, from));
                    }
                }
            }
        }
        updateListenerStatus();
    }

    private static OffsetDateTime earliestRecentAggregateStart(OffsetDateTime now) {
        OffsetDateTime week = aggregateStart(now, AggregatePeriod.WEEK);
        OffsetDateTime month = aggregateStart(now, AggregatePeriod.MONTH);
        return week.isBefore(month) ? week : month;
    }

    private static OffsetDateTime aggregateStart(OffsetDateTime now, AggregatePeriod period) {
        return switch (period) {
            case DAY -> now.truncatedTo(ChronoUnit.DAYS);
            case WEEK -> now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).truncatedTo(ChronoUnit.DAYS);
            case MONTH -> now.with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(ChronoUnit.DAYS);
            case YEAR -> now.with(TemporalAdjusters.firstDayOfYear()).truncatedTo(ChronoUnit.DAYS);
        };
    }
}
