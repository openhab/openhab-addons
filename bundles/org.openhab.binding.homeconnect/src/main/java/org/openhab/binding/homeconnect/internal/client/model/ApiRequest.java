/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.homeconnect.internal.client.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 * API request model.
 *
 * @author Jonas Brüstel - Initial Contribution
 */
@NonNullByDefault
public class ApiRequest {
    private final String id;
    private final ZonedDateTime time;
    private final HomeConnectRequest homeConnectRequest;
    private final @Nullable HomeConnectResponse homeConnectResponse;

    public ApiRequest(ZonedDateTime time, HomeConnectRequest homeConnectRequest,
            @Nullable HomeConnectResponse homeConnectResponse) {
        this.id = UUID.randomUUID().toString();
        this.time = time;
        this.homeConnectRequest = homeConnectRequest;
        this.homeConnectResponse = homeConnectResponse;
    }

    public String getId() {
        return id;
    }

    public ZonedDateTime getTime() {
        return time;
    }

    public HomeConnectRequest getRequest() {
        return homeConnectRequest;
    }

    public @Nullable HomeConnectResponse getResponse() {
        return homeConnectResponse;
    }

    @Override
    public String toString() {
        return "ApiRequest [id=" + id + ", time=" + time + ", request=" + homeConnectRequest + ", response="
                + homeConnectResponse + "]";
    }
}
