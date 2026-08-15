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
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.solaredge.internal.connector.StatusUpdateListener;
import org.openhab.binding.solaredge.internal.handler.SolarEdgeHandler;
import org.openhab.binding.solaredge.internal.model.MeasurementsResponsePublicApiV2;
import org.openhab.binding.solaredge.internal.model.MeasurementsResponseTransformerPublicApiV2;

/**
 * Retrieves production power from the SolarEdge Monitoring API V2 Basic Monitoring API.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class LiveDataUpdatePublicApiV2 extends AbstractCommand {

    private final SolarEdgeHandler handler;
    private final MeasurementsResponseTransformerPublicApiV2 transformer;
    private int retries;

    public LiveDataUpdatePublicApiV2(SolarEdgeHandler handler, StatusUpdateListener listener) {
        super(handler.getConfiguration(), listener, handler::getPublicApiV2Credential,
                handler::invalidatePublicApiV2Credential, handler::recordPublicApiV2Request,
                response -> handler.updatePublicApiV2RateLimit(response.getHeaders().get("x-ratelimit-limit-minute"),
                        response.getHeaders().get("x-ratelimit-remaining-minute"),
                        response.getHeaders().get("Retry-After")));
        this.handler = handler;
        this.transformer = new MeasurementsResponseTransformerPublicApiV2(handler);
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) {
        OffsetDateTime now = OffsetDateTime.now();
        return requestToPrepare.followRedirects(false).method(HttpMethod.GET)
                .param(PUBLIC_DATA_API_V2_FROM_FIELD, now.minusHours(1).truncatedTo(ChronoUnit.SECONDS).toString())
                .param(PUBLIC_DATA_API_V2_TO_FIELD, now.truncatedTo(ChronoUnit.SECONDS).toString())
                .param(PUBLIC_DATA_API_V2_RESOLUTION_FIELD, "QUARTER_HOUR").param(PUBLIC_DATA_API_V2_UNIT_FIELD, "W");
    }

    @Override
    protected String getURL() {
        return PUBLIC_DATA_API_V2_URL + config.getSolarId() + PUBLIC_DATA_API_V2_POWER_SUFFIX;
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
                    handler.updateChannelStatus(transformer.transformPower(response));
                    handler.updatePublicApiV2Production(transformer.latestValue(response));
                }
            }
        }
        updateListenerStatus();
    }
}
