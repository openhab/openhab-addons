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
package org.openhab.binding.gpstracker.internal;

import static org.openhab.binding.gpstracker.internal.GPSTrackerBindingConstants.CONFIG_PID;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gpstracker.internal.config.ConfigHelper;
import org.openhab.binding.gpstracker.internal.discovery.TrackerDiscoveryService;
import org.openhab.binding.gpstracker.internal.handler.TrackerHandler;
import org.openhab.binding.gpstracker.internal.message.NotificationBroker;
import org.openhab.binding.gpstracker.internal.provider.TrackerRegistry;
import org.openhab.binding.gpstracker.internal.provider.gpslogger.GPSLoggerCallbackServlet;
import org.openhab.binding.gpstracker.internal.provider.owntracks.OwnTracksCallbackServlet;
import org.openhab.core.config.core.ConfigOptionProvider;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.servlet.whiteboard.HttpWhiteboardConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.Servlet;

/**
 * Main component
 *
 * @author Gabor Bicskei - Initial contribution
 */
@Component(configurationPid = CONFIG_PID, service = { ThingHandlerFactory.class, ConfigOptionProvider.class })
@NonNullByDefault
public class GPSTrackerHandlerFactory extends BaseThingHandlerFactory implements TrackerRegistry, ConfigOptionProvider {
    /**
     * Config URI
     */
    private static final String URI_STR = "profile:gpstracker:trigger-geofence";

    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(GPSTrackerHandlerFactory.class);

    /**
     * Discovery service instance
     */
    private final TrackerDiscoveryService discoveryService;

    /**
     * Unit provider
     */
    private final UnitProvider unitProvider;

    /**
     * Location provider
     */
    private final LocationProvider locationProvider;

    /**
     * Endpoint called by tracker applications
     */
    private @NonNullByDefault({}) OwnTracksCallbackServlet otHTTPEndpoint;

    /**
     * Endpoint called by tracker applications
     */
    private @NonNullByDefault({}) GPSLoggerCallbackServlet glHTTPEndpoint;

    /**
     * Notification broker
     */
    private final NotificationBroker notificationBroker = new NotificationBroker();

    /**
     * Handler registry
     */
    private final Map<String, TrackerHandler> trackerHandlers = new HashMap<>();

    /**
     * All regions.
     */
    private final Set<String> regions = new HashSet<>();

    private @Nullable ServiceRegistration<Servlet> ownTracksServletRegistration;
    private @Nullable ServiceRegistration<Servlet> gpsLoggerServletRegistration;

    @Activate
    public GPSTrackerHandlerFactory(final @Reference TrackerDiscoveryService discoveryService, //
            final @Reference UnitProvider unitProvider, //
            final @Reference LocationProvider locationProvider) {
        this.discoveryService = discoveryService;
        this.unitProvider = unitProvider;
        this.locationProvider = locationProvider;
    }

    /**
     * Called by the framework to find out if thing type is supported by the handler factory.
     *
     * @param thingTypeUID Thing type UID
     * @return True if supported.
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return GPSTrackerBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * Creates new handler for tracker.
     *
     * @param thing Tracker thing
     * @return Handler instance
     */
    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (GPSTrackerBindingConstants.THING_TYPE_TRACKER.equals(thingTypeUID)
                && ConfigHelper.getTrackerId(thing.getConfiguration()) != null) {
            TrackerHandler trackerHandler = new TrackerHandler(thing, notificationBroker, regions,
                    locationProvider.getLocation(), unitProvider);
            discoveryService.removeTracker(trackerHandler.getTrackerId());
            trackerHandlers.put(trackerHandler.getTrackerId(), trackerHandler);
            return trackerHandler;
        } else {
            return null;
        }
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        String trackerId = ConfigHelper.getTrackerId(thingHandler.getThing().getConfiguration());
        trackerHandlers.remove(trackerId);
    }

    /**
     * Activate the binding. It starts the tracker discovery service and the HTTP callback endpoint.
     *
     * @param componentContext Component context.
     */
    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);

        logger.debug("Initializing callback servlets");
        otHTTPEndpoint = new OwnTracksCallbackServlet(discoveryService, this);
        ownTracksServletRegistration = registerServlet(otHTTPEndpoint, otHTTPEndpoint.getPath());
        logger.debug("Started GPSTracker Callback servlet on {}", otHTTPEndpoint.getPath());

        glHTTPEndpoint = new GPSLoggerCallbackServlet(discoveryService, this);
        gpsLoggerServletRegistration = registerServlet(glHTTPEndpoint, glHTTPEndpoint.getPath());
        logger.debug("Started GPSTracker Callback servlet on {}", glHTTPEndpoint.getPath());
    }

    /**
     * Deactivate the binding. It stops the HTTP callback endpoint and stops the tracker discovery service.
     *
     * @param componentContext Component context.
     */
    @Override
    protected void deactivate(ComponentContext componentContext) {
        logger.debug("Deactivating GPSTracker Binding");

        ServiceRegistration<Servlet> ownTracksServletRegistration = this.ownTracksServletRegistration;
        if (ownTracksServletRegistration != null) {
            ownTracksServletRegistration.unregister();
            this.ownTracksServletRegistration = null;
            logger.debug("GPSTracker callback servlet stopped on {}", otHTTPEndpoint.getPath());
        }

        ServiceRegistration<Servlet> gpsLoggerServletRegistration = this.gpsLoggerServletRegistration;
        if (gpsLoggerServletRegistration != null) {
            gpsLoggerServletRegistration.unregister();
            this.gpsLoggerServletRegistration = null;
            logger.debug("GPSTracker callback servlet stopped on {}", glHTTPEndpoint.getPath());
        }

        super.deactivate(componentContext);
    }

    private ServiceRegistration<Servlet> registerServlet(Servlet servlet, String pattern) {
        Hashtable<String, Object> servletProperties = new Hashtable<>();
        servletProperties.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, pattern);
        return bundleContext.registerService(Servlet.class, servlet, servletProperties);
    }

    @Override
    public @Nullable Collection<ParameterOption> getParameterOptions(URI uri, String param, @Nullable String context,
            @Nullable Locale locale) {
        if (URI_STR.equals(uri.toString()) && ConfigHelper.CONFIG_REGION_NAME.equals(param)) {
            Set<ParameterOption> ret = new HashSet<>();
            regions.forEach(r -> ret.add(new ParameterOption(r, r)));
            return ret;
        }
        return null;
    }

    @Override
    public @Nullable TrackerHandler getTrackerHandler(String trackerId) {
        return trackerHandlers.get(trackerId);
    }
}
