/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.geofence.internal.provider;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.geofence.internal.BindingConstants;
import org.openhab.binding.geofence.internal.message.AbstractBaseMessage;
import org.openhab.binding.geofence.internal.message.Location;
import org.openhab.binding.geofence.internal.message.MessageUtil;
import org.openhab.binding.geofence.internal.message.Transition;
import org.openhab.binding.geofence.internal.discovery.DeviceDiscoveryService;
import org.openhab.binding.geofence.internal.handler.TrackerRecorder;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
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
     * Core HTTP service
     */
    private HttpService httpService;

    /**
     * Thing registry used to find things for existing devices
     */
    private ThingRegistry thingRegistry;

    /**
     * Discovery service to handle new devices
     */
    private DeviceDiscoveryService discoveryService;

    /**
     * Utility to process messages
     */
    private MessageUtil messageUtil = new MessageUtil();

    /**
     * Constructor called at binding startup.
     *
     * @param httpService      HTTP service that runs the servlet.
     * @param thingRegistry    Thing registry.
     * @param discoveryService Discovery service for new devices.
     */
    protected AbstractCallbackServlet(HttpService httpService, ThingRegistry thingRegistry, DeviceDiscoveryService discoveryService) {
        this.httpService = httpService;
        this.thingRegistry = thingRegistry;
        this.discoveryService = discoveryService;
    }

    /**
     * Register the callback servlet.
     */
    public void activate() {
        try {
            this.httpService.registerServlet(getPath(), this, null, this.httpService.createDefaultHttpContext());
            logger.debug("Started Geofence Callback servlet on {}", getPath());
        } catch (NamespaceException | ServletException e) {
            logger.error("Could not start Geofence Callback servlet: {}", e.getMessage(), e);
        }
    }

    protected abstract String getPath();

    /**
     * Stops callback servlet.
     */
    public void deactivate() {
        this.httpService.unregister(getPath());
        logger.debug("Geofence callback servlet stopped on {}", getPath());
    }

    /**
     * Process the HTTP requests from devices running the tracker application
     *
     * @param req  HTTP request
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

            //get the remote address of the tracker
            String deviceIPAddress =  req.getRemoteAddr();

            //clear the whitespaces from the message
            String json = jb.toString().replaceAll("\\p{Z}", "");
            logger.debug("Post message received from {} tracker: {}", getProvider(), json);

            AbstractBaseMessage message = messageUtil.fromJson(json);
            if (message != null) {
                List<? extends AbstractBaseMessage> response = processMessage(message, deviceIPAddress);
                if (response != null) {
                    resp.getWriter().append(messageUtil.toJson(response)).flush();
                }
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                resp.setStatus(HttpServletResponse.SC_OK);
            }
        } catch (Exception e) {
            logger.error("Error processing location report:", e);
        }

        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    /**
     * Process the message received by the servlet. If the device is unknown the discovery service is notified
     * so that the next search will pop up the new device as result.
     *
     * @param message The message
     * @param deviceIPAddress Device IP address
     * @return Response message.
     */
    private List<? extends AbstractBaseMessage> processMessage(AbstractBaseMessage message, String deviceIPAddress) {
        String trackerId = message.getTrackerId();
        if (trackerId != null) {
            TrackerRecorder recorder = getHandlerById(trackerId);
            if (recorder != null) {
                recorder.updateDeviceIPAddress(deviceIPAddress);
                if (message instanceof Location) {
                    Location lm = (Location) message;
                    String[] regions = lm.getRegionsInside();
                    if (regions != null) {
                        for (String regionName : regions) {
                            recorder.maintainExternalRegion(regionName);
                        }
                    }
                    recorder.updateLocation(lm);
                } else if (message instanceof Transition) {
                    Transition tm = (Transition) message;
                    recorder.maintainExternalRegion(tm.getRegionName());
                    recorder.doTransition(tm);
                }
                return recorder.getNotifications();
            } else {
                logger.info("There is no handler for tracker {}. Check the inbox for the new tracker device.",
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
            ThingUID thingUuid = new ThingUID(BindingConstants.THING_TYPE_DEVICE, trackerId);
            Thing device = this.thingRegistry.get(thingUuid);
            if (device == null) {
                //thing was not found - adding the device to discovery service.
                discoveryService.addDevice(trackerId);
            } else {
                return (TrackerRecorder) device.getHandler();
            }
        }
        return null;
    }

    protected abstract String getProvider();
}
