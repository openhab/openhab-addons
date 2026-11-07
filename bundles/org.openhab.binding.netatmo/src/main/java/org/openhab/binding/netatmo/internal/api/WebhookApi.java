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
package org.openhab.binding.netatmo.internal.api;

import static org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.PATH_WEBHOOK;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.dto.Webhook;
import org.openhab.binding.netatmo.internal.api.dto.Webhook.WebhooksResponse;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;

/**
 * Base class for all Webhooks related endpoints
 * https://dev.netatmo.com/apidocumentation/webhooks#documentation
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class WebhookApi extends RestManager {
    private static final UriBuilder WEBHOOKS_URI_BUILDER = getApiBaseBuilder(PATH_WEBHOOK);
    private static final String PAYLOAD_POST_WEBHOOK = "{\"url\":\"%s\"}";

    public WebhookApi(ApiBridgeHandler apiClient) {
        super(apiClient, FeatureArea.NONE);
    }

    /**
     * Delete all webhooks
     *
     * @throws NetatmoException If fail to call the API, e.g. server or deserializing error
     */
    public void dropWebhook() throws NetatmoException {
        delete(WEBHOOKS_URI_BUILDER.clone(), ApiResponse.Ok.class);
    }

    /**
     * Appends a single webhook
     *
     * @param uri Your Webhook callback url (required)
     * @throws NetatmoException If fail to call the API, e.g. server or deserializing error
     */
    public boolean appendWebhook(URI uri) throws NetatmoException {
        String payload = PAYLOAD_POST_WEBHOOK.formatted(uri.toString());
        post(WEBHOOKS_URI_BUILDER.clone(), ApiResponse.Ok.class, payload);
        return true;
    }

    public List<Webhook> getWebhooks() throws NetatmoException {
        List<Webhook> result = get(WEBHOOKS_URI_BUILDER.clone(), WebhooksResponse.class).getBody();
        return result != null ? result : List.of();
    }
}
