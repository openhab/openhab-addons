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
import org.openhab.binding.solaredge.internal.model.DeviceTelemetryResponsePublicApiV2;
import org.openhab.binding.solaredge.internal.model.DeviceTelemetryResponseTransformerPublicApiV2;
import org.openhab.binding.solaredge.internal.model.DeviceTelemetryResponseTransformerPublicApiV2.LivePowers;

/**
 * Retrieves live meter or storage telemetry from Monitoring API V2.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class LiveDeviceTelemetryUpdatePublicApiV2 extends AbstractCommand {
    private final SolarEdgeHandler handler;
    private final boolean storage;
    private final DeviceTelemetryResponseTransformerPublicApiV2 transformer;
    private int retries;

    public LiveDeviceTelemetryUpdatePublicApiV2(SolarEdgeHandler handler, boolean storage,
            StatusUpdateListener listener) {
        super(handler.getConfiguration(), listener, handler::getPublicApiV2Credential,
                handler::invalidatePublicApiV2Credential, handler::recordPublicApiV2Request,
                response -> handler.updatePublicApiV2RateLimit(response.getHeaders().get("x-ratelimit-limit-minute"),
                        response.getHeaders().get("x-ratelimit-remaining-minute"),
                        response.getHeaders().get("Retry-After")));
        this.handler = handler;
        this.storage = storage;
        transformer = new DeviceTelemetryResponseTransformerPublicApiV2(handler);
    }

    @Override
    protected Request prepareRequest(Request request) {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        return request.followRedirects(false).method(HttpMethod.GET)
                .param(PUBLIC_DATA_API_V2_FROM_FIELD, now.minusHours(1).toString())
                .param(PUBLIC_DATA_API_V2_TO_FIELD, now.toString())
                .param(PUBLIC_DATA_API_V2_RESOLUTION_FIELD, "QUARTER_HOUR");
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
                    handler.updateChannelStatus(transformer.transformLive(response));
                    LivePowers powers = transformer.extractLivePowers(response);
                    if (storage) {
                        handler.updatePublicApiV2Storage(powers.charged(), powers.discharged(), powers.level());
                    } else {
                        handler.updatePublicApiV2Grid(powers.imported(), powers.exported());
                    }
                }
            }
        }
        updateListenerStatus();
    }
}
