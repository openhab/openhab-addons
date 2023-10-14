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
package org.openhab.binding.neeo.internal.handler;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
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
    private final Consumer<String> callback;

    /** The forwarding chain */
    private final @Nullable String forwardChain;

    private final HttpClient httpClient;

    /** The scheduler to use to schedule recipe execution */
    private final ScheduledExecutorService scheduler;

    /**
     * Creates the servlet the will process forward action events from the NEEO brain.
     *
     * @param scheduler a non-null {@link ScheduledExecutorService} to schedule forward actions
     * @param callback a non-null String consumer
     * @param forwardChain a possibly null, possibly empty forwarding chain
     */
    NeeoForwardActionsServlet(ScheduledExecutorService scheduler, Consumer<String> callback,
            @Nullable String forwardChain, HttpClient httpClient) {
        super();

        this.scheduler = scheduler;
        this.callback = callback;
        this.forwardChain = forwardChain;
        this.httpClient = httpClient;
    }

    /**
     * Processes the post action from the NEEO brain. Simply gets the specified json and then forwards it on (if
     * needed)
     *
     * @param req the non-null request
     * @param resp the non-null response
     */
    @Override
    protected void doPost(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp) throws IOException {
        if (req == null || resp == null) {
            logger.warn("doPost called with req={}, resp={}, non-null required.", req, resp);
            return;
        }

        final String json = req.getReader().lines().collect(Collectors.joining("\n"));
        logger.debug("handleForwardActions {}", json);

        callback.accept(json);

        String forwardChain = this.forwardChain;
        if (forwardChain != null && !forwardChain.isEmpty()) {
            scheduler.execute(() -> {
                try (final HttpRequest request = new HttpRequest(httpClient)) {
                    for (final String forwardUrl : forwardChain.split(",")) {
                        if (!forwardUrl.isEmpty()) {
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
}
