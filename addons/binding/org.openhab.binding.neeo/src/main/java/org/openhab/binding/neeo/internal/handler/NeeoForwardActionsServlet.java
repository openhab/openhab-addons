/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.neeo.internal.handler;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.neeo.internal.net.HttpRequest;
import org.openhab.binding.neeo.internal.net.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This servlet handles the forward actions events from the NEEO Brain. The forward actions will be posted to the
 * callback and then will be forwarded on to any URLs lised in {@link #forwardChain}
 *
 * @author Tim Roberts - Initial contribution
 *
 */
@NonNullByDefault
@SuppressWarnings("serial")
public class NeeoForwardActionsServlet extends HttpServlet {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoForwardActionsServlet.class);

    /** The event publisher */
    private final Callback callback;

    /** The forwarding chain */
    @Nullable
    private final String forwardChain;

    /** The scheduler to use to schedule recipe execution */
    private final ScheduledExecutorService scheduler;

    /**
     * Creates the servlet the will process foward action events from the NEEO brain.
     *
     * @param scheduler a non-null {@link ScheduledExecutorService} to schedule forward actions
     * @param callback a non-null {@link Callback}
     * @param forwardChain a possibly null, possibly empty forwarding chain
     */
    NeeoForwardActionsServlet(ScheduledExecutorService scheduler, Callback callback, @Nullable String forwardChain) {
        super();

        Objects.requireNonNull(scheduler, "scheduler cannot be null");
        Objects.requireNonNull(callback, "callback cannot be null");

        this.scheduler = scheduler;
        this.callback = callback;
        this.forwardChain = forwardChain;
    }

    /**
     * Processes the post action from the NEEO brain. Simply get's the specified json and then forwards it on (if
     * needed)
     *
     * @param req the non-null request
     * @param resp the non-null response
     */
    @Override
    protected void doPost(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        Objects.requireNonNull(req, "req cannot be null");
        Objects.requireNonNull(resp, "resp cannot be null");

        final String json = IOUtils.toString(req.getReader());
        logger.debug("handleForwardActions {}", json);

        callback.post(json);

        final String fc = forwardChain;
        if (fc != null && StringUtils.isNotEmpty(fc)) {
            scheduler.execute(() -> {
                try (final HttpRequest request = new HttpRequest()) {
                    for (final String forwardUrl : fc.split(",")) {
                        if (StringUtils.isNotEmpty(forwardUrl)) {
                            final HttpResponse httpResponse = request.sendPostJsonCommand(forwardUrl, json);
                            if (httpResponse.getHttpCode() != HttpStatus.OK_200) {
                                logger.debug("Cannot forward event {} to {}: {}", json, forwardUrl,
                                        httpResponse.getHttpCode());
                            }
                        }
                    }
                }
            });
        }
    }

    interface Callback {
        void post(String json);
    }
}
