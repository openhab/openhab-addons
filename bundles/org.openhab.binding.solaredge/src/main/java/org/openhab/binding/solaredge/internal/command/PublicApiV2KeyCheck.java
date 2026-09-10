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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.solaredge.internal.connector.StatusUpdateListener;
import org.openhab.binding.solaredge.internal.handler.SolarEdgeHandler;

/**
 * Checks a SolarEdge Monitoring API V2 Fleet API key.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class PublicApiV2KeyCheck extends AbstractCommand {

    public PublicApiV2KeyCheck(SolarEdgeHandler handler, StatusUpdateListener listener) {
        super(handler.getConfiguration(), listener, handler::getPublicApiV2Credential,
                handler::invalidatePublicApiV2Credential, handler::recordPublicApiV2Request,
                response -> handler.updatePublicApiV2RateLimit(response.getHeaders().get("x-ratelimit-limit-minute"),
                        response.getHeaders().get("x-ratelimit-remaining-minute"),
                        response.getHeaders().get("Retry-After")));
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) {
        return requestToPrepare.followRedirects(false).method(HttpMethod.GET);
    }

    @Override
    protected String getURL() {
        return PUBLIC_DATA_API_V2_URL + config.getSolarId();
    }

    @Override
    public void onComplete(@Nullable Result result) {
        updateListenerStatus();
    }
}
