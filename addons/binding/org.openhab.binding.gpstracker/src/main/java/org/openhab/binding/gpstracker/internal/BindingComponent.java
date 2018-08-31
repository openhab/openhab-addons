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
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.gpstracker.internal.config.BindingConfiguration;
import org.openhab.binding.gpstracker.internal.discovery.TrackerDiscoveryService;
import org.openhab.binding.gpstracker.internal.handler.TrackerHandler;
import org.openhab.binding.gpstracker.internal.handler.TranslationUtil;
import org.openhab.binding.gpstracker.internal.message.NotificationBroker;
import org.openhab.binding.gpstracker.internal.provider.gpslogger.GPSLoggerCallbackServlet;
import org.openhab.binding.gpstracker.internal.provider.owntracks.OwnTracksCallbackServlet;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.openhab.binding.gpstracker.internal.BindingConstants.CONFIG_PID;

/**
 * Main component
 *
 * @author Gabor Bicskei - Initial contribution
 */
@Component(
        configurationPid = CONFIG_PID,
        immediate = true,
        service = {ThingHandlerFactory.class, ManagedService.class}
)
public class BindingComponent extends BaseThingHandlerFactory implements ManagedService {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(BindingComponent.class);

    /**
     * Discovery service registration
     */
    private ServiceRegistration<?> serviceRegistration = null;

    /**
     * Binding configuration
     */
    private BindingConfiguration config = new BindingConfiguration();

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
     * Reference
     */
    private ThingRegistry thingRegistry;

    /**
     * Notification broker
     */
    private NotificationBroker notificationBroker = new NotificationBroker();

    /**
     * Translation helper
     */
    private TranslationUtil translationUtil;

    /**
     * Called by the framework to find out if thing type is supported by the handler factory.
     *
     * @param thingTypeUID Thing type UID
     * @return True if supported.
     */
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return BindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * Creates new handler for tracker.
     *
     * @param thing Tracker thing
     * @return Handler instance
     */
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (BindingConstants.THING_TYPE_TRACKER.equals(thingTypeUID)) {
            return new TrackerHandler(thing, this.config, notificationBroker, translationUtil);
        } else {
            return null;
        }
    }

    /**
     * Activate the binding. It starts the tracker discovery service and the HTTP callback endpoint.
     *
     * @param componentContext Component context.
     */
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        logger.debug("Initializing the configuration.");
        updateConfiguration(componentContext.getProperties());
        updatePrimaryLocationFromSystemConfig();

        logger.debug("Initializing discovery service");
        TrackerDiscoveryService discoveryService = new TrackerDiscoveryService();
        this.serviceRegistration = this.bundleContext.registerService(DiscoveryService.class.getName(),
                discoveryService, new Hashtable<>());

        this.translationUtil = new TranslationUtil(getBundleContext());

        logger.debug("Initializing callback servlets");
        otHTTPEndpoint = new OwnTracksCallbackServlet(httpService, thingRegistry, discoveryService);
        otHTTPEndpoint.activate();

        glHTTPEndpoint = new GPSLoggerCallbackServlet(httpService, thingRegistry, discoveryService);
        glHTTPEndpoint.activate();
    }

    /**
     * Updates primary region location from system config.
     */
    private void updatePrimaryLocationFromSystemConfig() {
        ServiceReference<LocationProvider> serviceReference = bundleContext.getServiceReference(LocationProvider.class);
        if (serviceReference != null) {
            LocationProvider locationProv = bundleContext.getService(serviceReference);
            if (locationProv != null) {
                logger.debug("Location provider was found.");
                try {
                    ServiceReference<ConfigurationAdmin> configAdminRef = bundleContext.getServiceReference(ConfigurationAdmin.class);
                    if (configAdminRef != null) {
                        logger.debug("ConfigurationAdmin service was found.");
                        ConfigurationAdmin configAdmin = bundleContext.getService(configAdminRef);
                        org.osgi.service.cm.Configuration configuration = configAdmin.getConfiguration(CONFIG_PID);
                        Dictionary<String, Object> properties = copy(configuration.getProperties());

                        if (properties != null) {
                            logger.debug("Bundle configuration was found.");
                            PointType location = locationProv.getLocation();
                            String locationConfig = (String) properties.get(BindingConstants.CONFIG_LOCATION);
                            if (location != null) {
                                if (locationConfig == null || !location.toFullString().equals(locationConfig)) {
                                    logger.debug("Updating location from system config: {}", location.toFullString());
                                    properties.put(BindingConstants.CONFIG_LOCATION, location.toFullString());
                                    configuration.update(properties);
                                }
                            } else if (locationConfig != null) {
                                logger.debug("Clear primary location config as system location is missing");
                                properties.remove(BindingConstants.CONFIG_NAME);
                                properties.remove(BindingConstants.CONFIG_LOCATION);
                                properties.put(BindingConstants.CONFIG_RADIUS, 100);
                                configuration.update(properties);
                            }
                        }
                    }
                } catch (IOException e) {
                    logger.error("Unable to update primary location from system config.", e);
                }
            }
        }
    }

    private Dictionary<String, Object> copy(Dictionary<String, Object> properties) {
        if (properties != null) {
            Hashtable<String, Object> newProp = new Hashtable<>();

            Enumeration<String> keys = properties.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                newProp.put(key, properties.get(key));
            }

            return newProp;
        }
        return null;
    }

    /**
     * Deactivate the binding. It stops the HTTP callback endpoint and stops the tracker discovery service.
     *
     * @param componentContext Component context.
     */
    protected void deactivate(ComponentContext componentContext) {
        logger.info("Deactivating GPSTracker Binding");
        logger.debug("Stopping callback servlets");
        otHTTPEndpoint.deactivate();
        glHTTPEndpoint.deactivate();

        logger.debug("Stopping discovery service");
        this.serviceRegistration.unregister();

        super.deactivate(componentContext);
    }

    @Reference(unbind = "unsetHttpService")
    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    @SuppressWarnings("unused")
    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    @Reference(unbind = "unsetThingRegistry")
    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    @SuppressWarnings("unused")
    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = null;
    }

    /**
     * Updates the configuration on handlers
     *
     * @param dictionary Configuration dictionary
     */
    @Override
    public void updated(Dictionary<String, ?> dictionary) {
        updateConfiguration(dictionary);
    }

    private void updateConfiguration(Dictionary<String, ?> dictionary) {
        List<String> keys = Collections.list(dictionary.keys());
        Map<String, Object> map = keys.stream()
                .collect(Collectors.toMap(Function.identity(), dictionary::get));
        Configuration newConfig = new Configuration(map);
        config = newConfig.as(BindingConfiguration.class);
        config.init();
    }
}
