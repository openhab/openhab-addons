/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Response.CompleteListener;
import org.eclipse.jetty.client.api.Response.ContentListener;
import org.eclipse.jetty.client.api.Response.FailureListener;
import org.eclipse.jetty.client.api.Response.SuccessListener;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of Jetty ContextExchange to handle callbacks
 *
 * @author Robert Bausdorf - Initial contribution
 */
@NonNullByDefault
public class FritzAhaContentExchange extends BufferingResponseListener
        implements SuccessListener, FailureListener, ContentListener, CompleteListener {

    private final Logger logger = LoggerFactory.getLogger(FritzAhaContentExchange.class);

    /**
     * Callback to execute on complete response
     */
    private final FritzAhaCallback callback;

    /**
     * Constructor
     *
     * @param callback Callback which execute method has to be called.
     */
    public FritzAhaContentExchange(FritzAhaCallback callback) {
        this.callback = callback;
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
        String content = getContentAsString();
        logger.debug("{} response complete: {}", result.getRequest().getMethod(), content);
        callback.execute(result.getResponse().getStatus(), content);
    }
}
