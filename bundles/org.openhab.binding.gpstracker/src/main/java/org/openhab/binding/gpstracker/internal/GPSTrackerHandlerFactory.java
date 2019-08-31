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
package org.openhab.binding.gpstracker.internal;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.core.ConfigOptionProvider;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.core.i18n.LocationProvider;
import org.eclipse.smarthome.core.i18n.UnitProvider;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.gpstracker.internal.config.ConfigHelper;
import org.openhab.binding.gpstracker.internal.discovery.TrackerDiscoveryService;
import org.openhab.binding.gpstracker.internal.handler.Life360BridgeHandler;
import org.openhab.binding.gpstracker.internal.handler.TrackerHandler;
import org.openhab.binding.gpstracker.internal.message.NotificationBroker;
import org.openhab.binding.gpstracker.internal.provider.TrackerRegistry;
import org.openhab.binding.gpstracker.internal.provider.gpslogger.GPSLoggerCallbackServlet;
import org.openhab.binding.gpstracker.internal.provider.owntracks.OwnTracksCallbackServlet;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.net.URI;
import java.util.*;

import static org.openhab.binding.gpstracker.internal.GPSTrackerBindingConstants.BRIDGE_TYPE_LIFE360;
import static org.openhab.binding.gpstracker.internal.GPSTrackerBindingConstants.CONFIG_PID;

/**
 * Main component
 *
 * @author Gabor Bicskei - Initial contribution
 */
@Component(configurationPid = CONFIG_PID, service = {ThingHandlerFactory.class, ConfigOptionProvider.class})
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
    private TrackerDiscoveryService discoveryService;

    /**
     * Unit provider
     */
    private UnitProvider unitProvider;

    /**
     * Location provider
     */
    private LocationProvider locationProvider;

    /**
     * HTTP service reference
     */
    private HttpService httpService;

    /**
     * Endpoint called by tracker applications
     */
    private OwnTracksCallbackServlet otHTTPEndpoint;

    /**
     * Endpoint called by tracker applications
     */
    private GPSLoggerCallbackServlet glHTTPEndpoint;

    /**
     * Endpoint called by tracker applications
     */
    //private Life360CallbackServlet l360HTTPEndpoint;

    /**
     * Notification broker
     */
    private NotificationBroker notificationBroker = new NotificationBroker();

    /**
     * Handler registry
     */
    private Map<String, TrackerHandler> trackerHandlers = new HashMap<>();

    /**
     * All regions.
     */
    private Set<String> regions = new HashSet<>();

    /**
     * Common HTTP client
     */
    private HttpClient httpClient;

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
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (BRIDGE_TYPE_LIFE360.equals(thingTypeUID)) {
            return new Life360BridgeHandler((Bridge) thing, httpClient, discoveryService, trackerHandlers);
        } else if (GPSTrackerBindingConstants.THING_TYPE_TRACKER.equals(thingTypeUID)
                && ConfigHelper.getTrackerId(thing.getConfiguration()) != null) {
            TrackerHandler trackerHandler = new TrackerHandler(thing, notificationBroker, regions,
                    locationProvider != null ? locationProvider.getLocation(): null, unitProvider);
            discoveryService.removeTracker(trackerHandler.getTrackerId());
            trackerHandlers.put(trackerHandler.getTrackerId(), trackerHandler);
            //map the handler based on the login email as well for Life360 messages
            if (trackerHandler.getLoginEmail() != null) {
                trackerHandlers.put(trackerHandler.getLoginEmail(), trackerHandler);
            }
            return trackerHandler;
        } else {
            return null;
        }
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof TrackerHandler) {
            String trackerId = ConfigHelper.getTrackerId(thingHandler.getThing().getConfiguration());
            trackerHandlers.remove(trackerId);
        } else {
            Life360BridgeHandler bridgeHandler = (Life360BridgeHandler) thingHandler;
            discoveryService.clearLife360Results();
            bridgeHandler.stop();
        }
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
        try {
            otHTTPEndpoint = new OwnTracksCallbackServlet(discoveryService, this);
            this.httpService.registerServlet(otHTTPEndpoint.getPath(), otHTTPEndpoint, null,
                    this.httpService.createDefaultHttpContext());
            logger.debug("Started GPSTracker Callback servlet on {}", otHTTPEndpoint.getPath());

            glHTTPEndpoint = new GPSLoggerCallbackServlet(discoveryService, this);
            this.httpService.registerServlet(glHTTPEndpoint.getPath(), glHTTPEndpoint, null,
                    this.httpService.createDefaultHttpContext());
            logger.debug("Started GPSTracker Callback servlet on {}", glHTTPEndpoint.getPath());

            //l360HTTPEndpoint = new Life360CallbackServlet(discoveryService, this);
            //this.httpService.registerServlet(l360HTTPEndpoint.getPath(), l360HTTPEndpoint, null,
            //        this.httpService.createDefaultHttpContext());
            //logger.debug("Started Life360 Callback servlet on {}", l360HTTPEndpoint.getPath());
        } catch (NamespaceException | ServletException e) {
            logger.error("Failed to start Callback servlet: {}", e.getMessage(), e);
        }
    }

    /**
     * Deactivate the binding. It stops the HTTP callback endpoint and stops the tracker discovery service.
     *
     * @param componentContext Component context.
     */
    @Override
    protected void deactivate(ComponentContext componentContext) {
        logger.debug("Deactivating GPSTracker Binding");

        this.httpService.unregister(otHTTPEndpoint.getPath());
        logger.debug("GPSTracker callback servlet stopped on {}", otHTTPEndpoint.getPath());

        this.httpService.unregister(glHTTPEndpoint.getPath());
        logger.debug("GPSTracker callback servlet stopped on {}", glHTTPEndpoint.getPath());

        //this.httpService.unregister(l360HTTPEndpoint.getPath());
        //logger.debug("Life360 callback servlet stopped on {}", l360HTTPEndpoint.getPath());

        super.deactivate(componentContext);
    }

    @Override
    public Collection<ParameterOption> getParameterOptions(URI uri, String param, Locale locale) {
        if (URI_STR.equals(uri.toString()) && ConfigHelper.CONFIG_REGION_NAME.equals(param)) {
            Set<ParameterOption> ret = new HashSet<>();
            regions.forEach(r->ret.add(new ParameterOption(r, r)));
            return ret;
        }
        return Collections.emptyList();
    }

    @Reference
    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    @Reference
    protected void setTrackerDiscoveryService(TrackerDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    protected void unsetTrackerDiscoveryService(TrackerDiscoveryService discoveryService) {
        this.discoveryService = null;
    }

    @Reference
    protected void setUnitProvider(UnitProvider unitProvider) {
        this.unitProvider = unitProvider;
    }

    protected void unsetUnitProvider(UnitProvider unitProvider) {
        this.unitProvider = null;
    }

    @Reference
    protected void setLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }

    protected void unsetLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = null;
    }

    @Override
    public TrackerHandler getTrackerHandler(String trackerId) {
        return trackerHandlers.get(trackerId);
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        try {
            logger.debug("setHttpClientFactory this: {}", this.toString());
            httpClient = httpClientFactory.getCommonHttpClient();
            httpClient.start();
        } catch (Exception e) {
            logger.error("Error setting up http client", e);
        }
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        try {
            logger.debug("unsetHttpClientFactory this: {}", this.toString());
            httpClient.stop();
            httpClient = null;
        } catch (Exception e) {
            logger.error("Error stopping http client", e);
        }
    }
}
