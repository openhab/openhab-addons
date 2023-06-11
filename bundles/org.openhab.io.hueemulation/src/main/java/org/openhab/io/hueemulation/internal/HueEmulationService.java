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
package org.openhab.io.hueemulation.internal;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Set;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Application;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
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
import org.osgi.framework.FrameworkUtil;
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
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a Hue compatible HTTP REST API on /api.
 * <p>
 * References all different rest endpoints implemented as JAX-RS annotated classes in the sub-package "rest".
 * Those are very modular and have (almost) no inter-dependencies. The UPnP related part is encapsulated in
 * the also referenced {@link UpnpServer}.
 * <p>
 * openHAB items via the {@link org.openhab.core.items.ItemRegistry} are for example mapped to
 * /api/{username}/lights and /api/{username}/groups in {@link LightsAndGroups}.
 * <p>
 * The user management is realized in the {@link UserManagement} component, that is referenced by almost all
 * other components and is the only inter-component dependency.
 *
 * @author David Graeff - Initial Contribution
 */
@NonNullByDefault
@Component(immediate = true, service = HueEmulationService.class)
public class HueEmulationService implements EventHandler {

    public static final String CONFIG_PID = "org.openhab.hueemulation";
    public static final String RESTAPI_PATH = "/api";
    public static final String REST_APP_NAME = "HueEmulation";

    @PreMatching
    public class RequestInterceptor implements ContainerRequestFilter {
        @NonNullByDefault({})
        @Override
        public void filter(ContainerRequestContext requestContext) {
            /**
             * Jetty returns 415 on any GET request if a client sends the Content-Type header.
             * This is a workaround - stripping it away in the preMatching stage.
             */
            if (HttpMethod.GET.equals(requestContext.getMethod())
                    && requestContext.getHeaders().containsKey(HttpHeader.CONTENT_TYPE.asString())) {
                requestContext.getHeaders().remove(HttpHeader.CONTENT_TYPE.asString());
            }
        }
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

    private final ContainerRequestFilter requestCleaner = new RequestInterceptor();

    /**
     * The Jax-RS application that starts up all REST activities.
     * It registers itself as a Jax-RS Whiteboard service and all Jax-RS resources that are targeting REST_APP_NAME will
     * start up.
     */
    @JaxrsName(REST_APP_NAME)
    private class RESTapplication extends Application {
        private String root;

        RESTapplication(String root) {
            this.root = root;
        }

        @NonNullByDefault({})
        @Override
        public Set<Object> getSingletons() {
            return Set.of(userManagement, configurationAccess, lightItems, sensors, scenes, schedules, rules,
                    statusResource, accessInterceptor, requestCleaner);
        }

        Dictionary<String, String> serviceProperties() {
            Dictionary<String, String> dict = new Hashtable<>();
            dict.put(JaxrsWhiteboardConstants.JAX_RS_APPLICATION_BASE, root);
            return dict;
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

    private @Nullable ServiceRegistration<?> eventHandler;
    private @Nullable ServiceRegistration<Application> restService;

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
        unregisterEventHandler();

        ServiceRegistration<Application> localRestService = restService;
        if (localRestService != null) {
            localRestService.unregister();
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
        unregisterEventHandler();

        ServiceRegistration<Application> localRestService = restService;
        if (localRestService == null) {
            RESTapplication app = new RESTapplication(RESTAPI_PATH);
            BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
            restService = context.registerService(Application.class, app, app.serviceProperties());
            logger.info("Hue Emulation service available under {}", RESTAPI_PATH);
        }
    }

    private void unregisterEventHandler() {
        ServiceRegistration<?> localEventHandler = eventHandler;
        if (localEventHandler != null) {
            try {
                localEventHandler.unregister();
                eventHandler = null;
            } catch (IllegalStateException e) {
                logger.debug("EventHandler already unregistered", e);
            }
        }
    }
}
