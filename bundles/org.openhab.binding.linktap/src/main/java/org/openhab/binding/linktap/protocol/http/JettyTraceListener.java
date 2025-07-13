/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linktap.protocol.http;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.slf4j.Logger;

/**
 * The {@link JettyTraceListener} defines a basic listener that can be utilised for logging jetty client states.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public final class JettyTraceListener implements Request.Listener {

    private final Logger logger;

    public JettyTraceListener(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public void onQueued(@Nullable Request request) {
        if (request != null) {
            logger.trace("HTTP Comms request is queued to be processed to {}", request.getURI());
        } else {
            logger.trace("HTTP Comms request is queued to be processed");
        }
        Request.Listener.super.onQueued(request);
    }

    @Override
    public void onBegin(@Nullable Request request) {
        if (request != null) {
            logger.trace("HTTP Comms request is beginning to be processed to {}", request.getURI());
        } else {
            logger.trace("HTTP Comms request is beginning to be processed");
        }
        Request.Listener.super.onBegin(request);
    }

    @Override
    public void onSuccess(@Nullable Request request) {
        if (request != null) {
            logger.trace("HTTP Comms request has been reported as successful to {}", request.getURI());
        } else {
            logger.trace("HTTP Comms request has been reported as successful");
        }
        Request.Listener.super.onSuccess(request);
    }

    @Override
    public void onFailure(@Nullable Request request, @Nullable Throwable failure) {
        if (request != null) {
            logger.trace("HTTP Comms request has failed {}", request.getHost());
        } else {
            logger.trace("HTTP Comms request has failed with error");
        }
        if (failure != null) {
            logger.trace("HTTP Comms request has failed due to cause {}", failure.toString());
        }
        Request.Listener.super.onFailure(request, failure);
    }
}
