/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gpstracker.internal;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.i18n.LocationProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.gpstracker.internal.config.GPSTrackerBindingConfiguration;
import org.openhab.binding.gpstracker.internal.discovery.TrackerDiscoveryService;
import org.openhab.binding.gpstracker.internal.handler.TrackerHandler;
import org.openhab.binding.gpstracker.internal.handler.TranslationUtil;
import org.openhab.binding.gpstracker.internal.message.NotificationBroker;
import org.openhab.binding.gpstracker.internal.provider.gpslogger.GPSLoggerCallbackServlet;
import org.openhab.binding.gpstracker.internal.provider.owntracks.OwnTracksCallbackServlet;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.openhab.binding.gpstracker.internal.GPSTrackerBindingConstants.CONFIG_PID;
import static org.openhab.binding.gpstracker.internal.GPSTrackerBindingConstants.CONFIG_TRACKER_ID;

/**
 * Main component
 *
 * @author Gabor Bicskei - Initial contribution
 */
@Component(
        configurationPid = CONFIG_PID,
        immediate = true,
        service = ThingHandlerFactory.class
)
public class GPSTrackerHandlerFactory extends BaseThingHandlerFactory {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(GPSTrackerHandlerFactory.class);

    /**
     * Discovery service registration
     */
    private ServiceRegistration<?> serviceRegistration = null;

    /**
     * Discovery service instance
     */
    private TrackerDiscoveryService discoveryService;

    /**
     * Binding configuration
     */
    private GPSTrackerBindingConfiguration config = new GPSTrackerBindingConfiguration();

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
     * Notification broker
     */
    private NotificationBroker notificationBroker = new NotificationBroker();

    /**
     * Translation helper
     */
    private TranslationUtil translationUtil;

    /**
     * Handler registry
     */
    private Map<String, TrackerHandler> trackerHandlers = new HashMap<>();

    /**
     * Called by the framework to find out if thing type is supported by the handler factory.
     *
     * @param thingTypeUID Thing type UID
     * @return True if supported.
     */
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return GPSTrackerBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * Creates new handler for tracker.
     *
     * @param thing Tracker thing
     * @return Handler instance
     */
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (GPSTrackerBindingConstants.THING_TYPE_TRACKER.equals(thingTypeUID) && thing.getConfiguration().get(CONFIG_TRACKER_ID) != null) {
            TrackerHandler trackerHandler = new TrackerHandler(thing, this.config, notificationBroker, translationUtil);
            discoveryService.removeTracker(trackerHandler.getTrackerId());
            trackerHandlers.put(trackerHandler.getTrackerId(), trackerHandler);
            return trackerHandler;
        } else {
            return null;
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
        logger.debug("Initializing the configuration.");
        updateConfiguration(componentContext.getProperties());

        logger.debug("Initializing discovery service");
        discoveryService = new TrackerDiscoveryService();
        this.serviceRegistration = this.bundleContext.registerService(DiscoveryService.class.getName(),
                discoveryService, new Hashtable<>());

        this.translationUtil = new TranslationUtil(getBundleContext());

        logger.debug("Initializing callback servlets");
        try {
            otHTTPEndpoint = new OwnTracksCallbackServlet(discoveryService, this::getTrackerHandler);
            this.httpService.registerServlet(otHTTPEndpoint.getPath(), otHTTPEndpoint, null, this.httpService.createDefaultHttpContext());
            logger.debug("Started GPSTracker Callback servlet on {}", otHTTPEndpoint.getPath());

            glHTTPEndpoint = new GPSLoggerCallbackServlet(discoveryService, this::getTrackerHandler);
            this.httpService.registerServlet(glHTTPEndpoint.getPath(), glHTTPEndpoint, null, this.httpService.createDefaultHttpContext());
            logger.debug("Started GPSTracker Callback servlet on {}", glHTTPEndpoint.getPath());
        } catch (NamespaceException | ServletException e) {
            logger.error("Could not start GPSTracker Callback servlet: {}", e.getMessage(), e);
        }
    }

    private TrackerHandler getTrackerHandler(String trackerId) {
        return trackerHandlers.get(trackerId);
    }

    /**
     * Deactivate the binding. It stops the HTTP callback endpoint and stops the tracker discovery service.
     *
     * @param componentContext Component context.
     */
    @Override
    protected void deactivate(ComponentContext componentContext) {
        logger.debug("Deactivating GPSTracker Binding");
        logger.debug("Stopping callback servlets");

        this.httpService.unregister(otHTTPEndpoint.getPath());
        logger.debug("GPSTracker callback servlet stopped on {}", otHTTPEndpoint.getPath());

        this.httpService.unregister(glHTTPEndpoint.getPath());
        logger.debug("GPSTracker callback servlet stopped on {}", glHTTPEndpoint.getPath());

        logger.debug("Stopping discovery service");
        if (serviceRegistration != null) {
            this.serviceRegistration.unregister();
            this.serviceRegistration = null;
        }

        super.deactivate(componentContext);
    }

    @Reference
    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    private void updateConfiguration(Dictionary<String, ?> dictionary) {
        List<String> keys = Collections.list(dictionary.keys());
        Map<String, Object> map = keys.stream()
                .collect(Collectors.toMap(Function.identity(), dictionary::get));
        Configuration newConfig = new Configuration(map);
        config = newConfig.as(GPSTrackerBindingConfiguration.class);

        ServiceReference<LocationProvider> serviceReference = bundleContext.getServiceReference(LocationProvider.class);
        if (serviceReference != null) {
            LocationProvider locationProv = bundleContext.getService(serviceReference);
            config.setLocation(locationProv.getLocation());
        }

        config.init();
    }
}
