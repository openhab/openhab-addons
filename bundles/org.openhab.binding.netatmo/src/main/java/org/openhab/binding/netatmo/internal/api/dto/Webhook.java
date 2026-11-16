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
package org.openhab.binding.netatmo.internal.api.dto;

import java.net.URI;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.ApiResponse;

/**
 * The {@link Webhook} is the answer to API getWebhooks
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class Webhook {
    public class WebhooksResponse extends ApiResponse<List<Webhook>> {
    }

    private String url = "";

    public URI getUri() {
        return URI.create(url);
    }
}
