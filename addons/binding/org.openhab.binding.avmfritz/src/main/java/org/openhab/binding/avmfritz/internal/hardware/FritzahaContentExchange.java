/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.hardware;

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
 * @author Robert Bausdorf
 *
 */
public class FritzahaContentExchange extends BufferingResponseListener
        implements SuccessListener, FailureListener, ContentListener, CompleteListener {
    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Callback to execute on complete response
     */
    private FritzAhaCallback callback;

    /**
     * Constructor
     *
     * @param callback Callback which execute method has to be called.
     */
    public FritzahaContentExchange(FritzAhaCallback callback) {
        this.callback = callback;
    }

    /**
     * Log request success
     */
    @Override
    public void onSuccess(Response response) {
        logger.debug("HTTP response {}", response.getStatus());
    }

    /**
     * Log request failure
     */
    @Override
    public void onFailure(Response response, Throwable failure) {
        logger.debug("{}", failure.getLocalizedMessage());
    }

    /**
     * Call the callbacks execute method on request completion.
     */
    @Override
    public void onComplete(Result result) {
        logger.debug("response complete: {}", this.getContentAsString());
        this.callback.execute(result.getResponse().getStatus(), this.getContentAsString());
    }
}
