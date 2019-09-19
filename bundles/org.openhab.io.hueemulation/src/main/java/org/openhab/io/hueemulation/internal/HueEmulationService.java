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
package org.openhab.io.hueemulation.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Application;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
import org.openhab.io.hueemulation.internal.rest.ConfigurationAccess;
import org.openhab.io.hueemulation.internal.rest.LightsAndGroups;
import org.openhab.io.hueemulation.internal.rest.Rules;
import org.openhab.io.hueemulation.internal.rest.Scenes;
import org.openhab.io.hueemulation.internal.rest.Schedules;
import org.openhab.io.hueemulation.internal.rest.Sensors;
import org.openhab.io.hueemulation.internal.rest.StatusResource;
import org.openhab.io.hueemulation.internal.rest.UserManagement;
import org.openhab.io.hueemulation.internal.upnp.UpnpServer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a Hue compatible HTTP REST API on /api.
 * <p>
 * References all different rest endpoints implemented as JAX-RS annotated classes in the sub-package "rest".
 * Those are very modular and have (almost) no inter-dependencies. The UPnP related part is encapsulated in
 * the also referenced {@link UpnpServer}.
 * <p>
 * openHAB items via the {@link org.eclipse.smarthome.core.items.ItemRegistry} are for example mapped to
 * /api/{username}/lights and /api/{username}/groups in {@link LightsAndGroups}.
 * <p>
 * The user management is realized in the {@link UserManagement} component, that is referenced by almost all
 * other components and is the only inter-component dependency.
 *
 * @author David Graeff - Initial Contribution
 */
@NonNullByDefault
@Component(immediate = true, service = { HueEmulationService.class }, property = {
        "com.eclipsesource.jaxrs.publish=false" })
public class HueEmulationService implements EventHandler {

    public static final String CONFIG_PID = "org.openhab.hueemulation";
    public static final String RESTAPI_PATH = "/api";

    @ApplicationPath(RESTAPI_PATH)
    public static class JerseyApplication extends Application {

    }

    public class LogAccessInterceptor implements ContainerResponseFilter {
        @NonNullByDefault({})
        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
            if (!logger.isDebugEnabled()) {
                return;
            }

            logger.debug("REST request {} {}", requestContext.getMethod(), requestContext.getUriInfo().getPath());
            logger.debug("REST response: {}", responseContext.getEntity());
        }

    }

    private final Logger logger = LoggerFactory.getLogger(HueEmulationService.class);
    private final LogAccessInterceptor accessInterceptor = new LogAccessInterceptor();

    //// Required services ////
    // Don't fail the service if the upnp server does not come up
    // That part is required for discovery only but does not affect already configured hue applications
    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY)
    protected @Nullable UpnpServer discovery;
    @Reference
    protected @NonNullByDefault({}) ConfigStore cs;
    @Reference
    protected @NonNullByDefault({}) UserManagement userManagement;
    @Reference
    protected @NonNullByDefault({}) ConfigurationAccess configurationAccess;
    @Reference
    protected @NonNullByDefault({}) LightsAndGroups lightItems;
    @Reference
    protected @NonNullByDefault({}) Sensors sensors;
    @Reference
    protected @NonNullByDefault({}) Scenes scenes;
    @Reference
    protected @NonNullByDefault({}) Schedules schedules;
    @Reference
    protected @NonNullByDefault({}) Rules rules;
    @Reference
    protected @NonNullByDefault({}) StatusResource statusResource;

    @Reference
    protected @NonNullByDefault({}) HttpService httpService;
    private @NonNullByDefault({}) ServiceRegistration<?> eventHandler;

    @Activate
    protected void activate(BundleContext bc) {
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put(EventConstants.EVENT_TOPIC, ConfigStore.EVENT_ADDRESS_CHANGED);
        eventHandler = bc.registerService(EventHandler.class, this, properties);
        if (cs.isReady()) {
            handleEvent(null);
        }
    }

    // Don't restart the service on config change
    @Modified
    protected void modified() {
    }

    @Deactivate
    protected void deactivate() {
        try {
            if (eventHandler != null) {
                eventHandler.unregister();
            }
        } catch (IllegalStateException ignore) {
        }
        try {
            httpService.unregister(RESTAPI_PATH);
        } catch (IllegalArgumentException ignore) {
        }
    }

    /**
     * We have a hard dependency on the {@link ConfigStore} and that it has initialized the Hue DataStore config
     * completely. That initialization happens asynchronously and therefore we cannot rely on OSGi activate/modified
     * state changes. Instead the {@link EventAdmin} is used and we listen for the
     * {@link ConfigStore#EVENT_ADDRESS_CHANGED} event that is fired as soon as the config is ready.
     */
    @Override
    public void handleEvent(@Nullable Event event) {
        try { // Only receive this event once
            eventHandler.unregister();
            eventHandler = null;
        } catch (IllegalStateException ignore) {
        }

        ResourceConfig resourceConfig = ResourceConfig.forApplicationClass(JerseyApplication.class);
        resourceConfig.property(ServerProperties.APPLICATION_NAME, "HueEmulation");
        // don't look for implementations described by META-INF/services/*
        resourceConfig.property(ServerProperties.METAINF_SERVICES_LOOKUP_DISABLE, true);
        // disable auto discovery on server, as it's handled via OSGI
        resourceConfig.property(ServerProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true);

        resourceConfig.property(ServerProperties.PROCESSING_RESPONSE_ERRORS_ENABLED, true);

        resourceConfig.registerInstances(userManagement, configurationAccess, lightItems, sensors, scenes, schedules,
                rules, statusResource, accessInterceptor);

        try {
            Hashtable<String, String> initParams = new Hashtable<>();
            initParams.put("com.sun.jersey.api.json.POJOMappingFeature", "false");
            initParams.put(ServletProperties.PROVIDER_WEB_APP, "false");
            httpService.registerServlet(RESTAPI_PATH, new ServletContainer(resourceConfig), initParams, null);
            UpnpServer localDiscovery = discovery;
            if (localDiscovery == null) {
                logger.warn("The UPnP Server service has not been started!");
            } else if (!localDiscovery.upnpAnnouncementThreadRunning()) {
                localDiscovery.handleEvent(null);
            }
            statusResource.startUpnpSelfTest();
            logger.info("Hue Emulation service available under {}", RESTAPI_PATH);
        } catch (ServletException | NamespaceException e) {
            logger.warn("Could not start Hue Emulation service: {}", e.getMessage(), e);
        }
    }
}
