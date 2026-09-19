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
import java.time.ZoneId;
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
import org.openhab.binding.solaredge.internal.model.DeviceTelemetryResponsePublicApiV2;
import org.openhab.binding.solaredge.internal.model.DeviceTelemetryResponseTransformerPublicApiV2;
import org.openhab.binding.solaredge.internal.model.DeviceTelemetryResponseTransformerPublicApiV2.AggregateEnergies;

/**
 * Retrieves aggregate meter or storage telemetry from Monitoring API V2.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class AggregateDeviceTelemetryUpdatePublicApiV2 extends AbstractCommand {
    private final SolarEdgeHandler handler;
    private final long cycleId;
    private final boolean storage;
    private final boolean yearly;
    private final DeviceTelemetryResponseTransformerPublicApiV2 transformer;
    private int retries;

    public AggregateDeviceTelemetryUpdatePublicApiV2(SolarEdgeHandler handler, long cycleId, boolean storage,
            boolean yearly, StatusUpdateListener listener) {
        super(handler.getConfiguration(), listener, handler::getPublicApiV2Credential,
                handler::invalidatePublicApiV2Credential, handler::recordPublicApiV2Request,
                response -> handler.updatePublicApiV2RateLimit(response.getHeaders().get("x-ratelimit-limit-minute"),
                        response.getHeaders().get("x-ratelimit-remaining-minute"),
                        response.getHeaders().get("Retry-After")));
        this.handler = handler;
        this.cycleId = cycleId;
        this.storage = storage;
        this.yearly = yearly;
        transformer = new DeviceTelemetryResponseTransformerPublicApiV2(handler);
    }

    @Override
    protected Request prepareRequest(Request request) {
        OffsetDateTime now = OffsetDateTime.now(ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
        OffsetDateTime from = yearly ? aggregateStart(now, AggregatePeriod.YEAR) : earliestRecentAggregateStart(now);
        return request.followRedirects(false).method(HttpMethod.GET)
                .param(PUBLIC_DATA_API_V2_FROM_FIELD, from.toString())
                .param(PUBLIC_DATA_API_V2_TO_FIELD, now.toString())
                .param(PUBLIC_DATA_API_V2_RESOLUTION_FIELD, yearly ? "MONTH" : "DAY");
    }

    @Override
    protected String getURL() {
        return PUBLIC_DATA_API_V2_URL + config.getSolarId()
                + (storage ? PUBLIC_DATA_API_V2_STORAGE_TELEMETRY_SUFFIX : PUBLIC_DATA_API_V2_METER_TELEMETRY_SUFFIX);
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
                DeviceTelemetryResponsePublicApiV2 response = fromJson(json, DeviceTelemetryResponsePublicApiV2.class);
                if (response != null) {
                    OffsetDateTime now = OffsetDateTime.now(ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
                    AggregatePeriod[] periods = yearly ? new AggregatePeriod[] { AggregatePeriod.YEAR }
                            : new AggregatePeriod[] { AggregatePeriod.DAY, AggregatePeriod.WEEK,
                                    AggregatePeriod.MONTH };
                    for (AggregatePeriod period : periods) {
                        OffsetDateTime from = aggregateStart(now, period);
                        handler.updateChannelStatus(transformer.transformAggregate(response, period, from));
                        AggregateEnergies energies = transformer.extractAggregateEnergies(response, from);
                        if (storage) {
                            handler.updatePublicApiV2AggregateStorage(cycleId, period, energies.charged(),
                                    energies.discharged());
                        } else {
                            handler.updatePublicApiV2AggregateGrid(cycleId, period, energies.imported(),
                                    energies.exported(), energies.consumption());
                        }
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
