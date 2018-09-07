/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gpstracker.internal.provider;

import org.openhab.binding.gpstracker.internal.discovery.TrackerDiscoveryService;
import org.openhab.binding.gpstracker.internal.handler.TrackerHandler;
import org.openhab.binding.gpstracker.internal.handler.TrackerRecorder;
import org.openhab.binding.gpstracker.internal.message.AbstractBaseMessage;
import org.openhab.binding.gpstracker.internal.message.Location;
import org.openhab.binding.gpstracker.internal.message.MessageUtil;
import org.openhab.binding.gpstracker.internal.message.Transition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.util.Collections;
import java.util.List;

/**
 * Abstract callback servlet used by the trackers.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public abstract class AbstractCallbackServlet extends HttpServlet {
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
    private TrackerRegistry trackerRegistryImpl;

    /**
     * Constructor called at binding startup.
     *
     * @param discoveryService Discovery service for new trackers.
     * @param trackerRegistry Tracker handler registry
     */
    protected AbstractCallbackServlet(TrackerDiscoveryService discoveryService, TrackerRegistry trackerRegistry) {
        this.discoveryService = discoveryService;
        this.trackerRegistryImpl = trackerRegistry;
    }

    protected abstract String getPath();

    /**
     * Process the HTTP requests from tracker applications
     *
     * @param req HTTP request
     * @param resp HTTP response
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            StringBuilder jb = new StringBuilder();
            BufferedReader reader = req.getReader();

            String line;
            while ((line = reader.readLine()) != null) {
                jb.append(line);
            }

            //clear the whitespaces from the message
            String json = jb.toString().replaceAll("\\p{Z}", "");
            logger.debug("Post message received from {} tracker: {}", getProvider(), json);

            AbstractBaseMessage message = messageUtil.fromJson(json);
            if (message != null) {
                List<? extends AbstractBaseMessage> response = processMessage(message);
                if (response != null) {
                    resp.getWriter().append(messageUtil.toJson(response)).flush();
                }
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                resp.setStatus(HttpServletResponse.SC_OK);
            }
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
    private List<? extends AbstractBaseMessage> processMessage(AbstractBaseMessage message) {
        String trackerId = message.getTrackerId();
        if (trackerId != null) {
            TrackerRecorder recorder = getHandlerById(trackerId);
            if (recorder != null) {
                if (message instanceof Location) {
                    Location lm = (Location) message;
                    recorder.updateLocation(lm);
                } else if (message instanceof Transition) {
                    Transition tm = (Transition) message;
                    recorder.doTransition(tm);
                }
                return recorder.getNotifications();
            } else {
                logger.debug("There is no handler for tracker {}. Check the inbox for the new tracker.",
                        trackerId);
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
    private TrackerRecorder getHandlerById(String trackerId) {
        if (trackerId != null) {
            TrackerHandler handler = trackerRegistryImpl.getTrackerHandler(trackerId);
            if (handler == null) {
                //handler was not found - adding the tracker to discovery service.
                discoveryService.addTracker(trackerId);
            } else {
                return handler;
            }
        }
        return null;
    }

    protected abstract String getProvider();
}
