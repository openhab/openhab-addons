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
package org.openhab.binding.avmfritz.internal.hardware;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.BufferingResponseListener;
import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.client.Response;
import org.eclipse.jetty.client.Response.CompleteListener;
import org.eclipse.jetty.client.Response.ContentListener;
import org.eclipse.jetty.client.Response.FailureListener;
import org.eclipse.jetty.client.Response.SuccessListener;
import org.eclipse.jetty.client.Result;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of Jetty ContextExchange to handle callbacks
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Leo Siepel - Handle session loss on failed requests
 */
@NonNullByDefault
public class FritzAhaContentExchange extends BufferingResponseListener
        implements SuccessListener, FailureListener, ContentListener, CompleteListener {

    private final Logger logger = LoggerFactory.getLogger(FritzAhaContentExchange.class);

    /**
     * Callback to execute on complete response
     */
    private final FritzAhaCallback callback;
    private @Nullable Runnable invalidateSession;

    /**
     * Constructor
     *
     * @param callback Callback which execute method has to be called.
     */
    public FritzAhaContentExchange(FritzAhaCallback callback) {
        this.callback = callback;
    }

    void send(Request request, Runnable invalidateSession) {
        this.invalidateSession = invalidateSession;
        request.send(this);
    }

    @Override
    public void onSuccess(@NonNullByDefault({}) Response response) {
        logger.debug("{} response: {}", response.getRequest().getScheme().toUpperCase(), response.getStatus());
    }

    @Override
    public void onFailure(@NonNullByDefault({}) Response response, @NonNullByDefault({}) Throwable failure) {
        logger.debug("{} response failed: {}", response.getRequest().getMethod(), failure.getLocalizedMessage(),
                failure);
    }

    @Override
    public void onComplete(@NonNullByDefault({}) Result result) {
        Runnable invalidate = invalidateSession;
        Response response = result.getResponse();
        if (result.isFailed() || response == null) {
            if (invalidate != null) {
                invalidate.run();
            }
            return;
        }

        String content = Objects.requireNonNullElse(getContentAsString(), "");
        int status = response.getStatus();
        logger.debug("{} response complete: {}", result.getRequest().getMethod(), content);
        if (invalidate != null && !FritzAhaCallback.isValidResponse(status, content)) {
            invalidate.run();
        }
        callback.execute(status, content);
    }
}
