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
package org.openhab.binding.gpstracker.internal.provider;

import java.io.BufferedReader;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gpstracker.internal.discovery.TrackerDiscoveryService;
import org.openhab.binding.gpstracker.internal.handler.TrackerHandler;
import org.openhab.binding.gpstracker.internal.message.MessageUtil;
import org.openhab.binding.gpstracker.internal.message.dto.LocationMessage;
import org.openhab.binding.gpstracker.internal.message.dto.TransitionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract callback servlet used by the trackers.
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractCallbackServlet extends HttpServlet {

    private static final long serialVersionUID = -2725161358635927815L;

    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(AbstractCallbackServlet.class);

    /**
     * Discovery service to handle new trackers
     */
    private TrackerDiscoveryService discoveryService;

    /**
     * Utility to process messages
     */
    private MessageUtil messageUtil = new MessageUtil();

    /**
     * Tracker registry
     */
    private TrackerRegistry trackerRegistry;

    /**
     * Constructor called at binding startup.
     *
     * @param discoveryService Discovery service for new trackers.
     * @param trackerRegistry Tracker handler registry
     */
    protected AbstractCallbackServlet(TrackerDiscoveryService discoveryService, TrackerRegistry trackerRegistry) {
        this.discoveryService = discoveryService;
        this.trackerRegistry = trackerRegistry;
    }

    protected abstract String getPath();

    /**
     * Process the HTTP requests from tracker applications
     *
     * @param req HTTP request
     * @param resp HTTP response
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            StringBuilder jb = new StringBuilder();
            BufferedReader reader = req.getReader();

            String line;
            while ((line = reader.readLine()) != null) {
                jb.append(line);
            }

            // clear the whitespaces from the message
            String json = jb.toString().replaceAll("\\p{Z}", "");
            logger.debug("Post message received from {} tracker: {}", getProvider(), json);

            LocationMessage message = messageUtil.fromJson(json);
            if (message != null) {
                List<? extends LocationMessage> response = processMessage(message);
                if (response != null) {
                    resp.getWriter().append(messageUtil.toJson(response)).flush();
                }
            }
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            logger.error("Error processing location report:", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Process the message received by the servlet. If the tracker is unknown the discovery service is notified
     * so that the next search will pop up the new tracker as result.
     *
     * @param message The message
     * @return Response message.
     */
    private List<? extends LocationMessage> processMessage(LocationMessage message) {
        String trackerId = message.getTrackerId();
        if (!trackerId.isEmpty()) {
            TrackerHandler recorder = getHandlerById(trackerId);
            if (recorder != null) {
                if (message instanceof TransitionMessage) {
                    TransitionMessage tm = (TransitionMessage) message;
                    recorder.doTransition(tm);
                } else {
                    recorder.updateLocation(message);
                }
                return recorder.getNotifications();
            } else {
                logger.debug("There is no handler for tracker {}. Check the inbox for the new tracker.", trackerId);
            }
        } else {
            logger.debug("Message without tracker id. Dropping message. {}", messageUtil.toJson(message));
        }
        return Collections.emptyList();
    }

    /**
     * Find handler for tracker. If the handler does not exist it is registered with discovery service.
     *
     * @param trackerId Tracker id.
     * @return Handler for tracker.
     */
    private @Nullable TrackerHandler getHandlerById(String trackerId) {
        TrackerHandler handler = trackerRegistry.getTrackerHandler(trackerId);
        if (handler == null) {
            // handler was not found - adding the tracker to discovery service.
            discoveryService.addTracker(trackerId);
        } else {
            return handler;
        }

        return null;
    }

    protected abstract String getProvider();
}
