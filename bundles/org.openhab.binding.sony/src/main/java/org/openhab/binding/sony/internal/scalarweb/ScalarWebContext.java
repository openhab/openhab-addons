/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.openhab.binding.sony.internal.providers.SonyDynamicStateProvider;

/**
 * Represents the context for scalar web classes to use. The context will contain various properties that are unique to
 * a thing and can be accessed by other objects
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ScalarWebContext {
    /** The supplier of a thing */
    private final Supplier<Thing> thingSupplier;

    /** The channel tracker */
    private final ScalarWebChannelTracker tracker;

    /** The scheduler to use */
    private final ScheduledExecutorService scheduler;

    /** The dynamic state provider */
    private final SonyDynamicStateProvider stateProvider;

    /** The websocket client to use (if specified) */
    private final @Nullable WebSocketClient webSocketClient;

    /** The transformation service to use (if specified) */
    private final @Nullable TransformationService transformService;

    /** The configuration for the thing */
    private final ScalarWebConfig config;

    /** The osgi properties */
    private final Map<String, String> osgiProperties;

    /**
     * Constructs the context from the parameters
     *
     * @param thingSupplier the non-null thing supplier
     * @param config the non-null configuration
     * @param tracker the non-null channel tracker
     * @param scheduler the non-null scheduler
     * @param stateProvider the non-null dynamic state provider
     * @param webSocketClient the possibly null websocket client
     * @param transformService the possibly null transformation service
     * @param properties the non-null OSGI properties
     */
    public ScalarWebContext(final Supplier<Thing> thingSupplier, final ScalarWebConfig config,
            final ScalarWebChannelTracker tracker, final ScheduledExecutorService scheduler,
            final SonyDynamicStateProvider stateProvider, final @Nullable WebSocketClient webSocketClient,
            final @Nullable TransformationService transformService, final Map<String, String> osgiProperties) {
        Objects.requireNonNull(thingSupplier, "thingSupplier cannot be null");
        Objects.requireNonNull(thingSupplier, "config cannot be null");
        Objects.requireNonNull(tracker, "tracker cannot be null");
        Objects.requireNonNull(scheduler, "scheduler cannot be null");
        Objects.requireNonNull(stateProvider, "stateProvider cannot be null");
        Objects.requireNonNull(osgiProperties, "osgiProperties cannot be null");

        this.thingSupplier = thingSupplier;
        this.config = config;
        this.tracker = tracker;
        this.scheduler = scheduler;
        this.stateProvider = stateProvider;
        this.webSocketClient = webSocketClient;
        this.transformService = transformService;
        this.osgiProperties = osgiProperties;
    }

    /**
     * Returns the thing for the context
     *
     * @return a non-null thing
     */
    public Thing getThing() {
        return thingSupplier.get();
    }

    /**
     * Returns the thing uid for the context
     *
     * @return the non-null thing uid
     */
    public ThingUID getThingUID() {
        return getThing().getUID();
    }

    /**
     * Returns the thing configuration
     *
     * @return the non-null configuration
     */
    public ScalarWebConfig getConfig() {
        return config;
    }

    /**
     * Returns the channel tracker for the thing
     *
     * @return a non-null channel tracker
     */
    public ScalarWebChannelTracker getTracker() {
        return tracker;
    }

    /**
     * Returns the thing's scheduler
     *
     * @return a non-null scheduler
     */
    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    /**
     * Returns the dynamic state provider
     * 
     * @return the non-null dynamic state provider
     */
    public SonyDynamicStateProvider getStateProvider() {
        return stateProvider;
    }

    /**
     * Returns the websocket client to use
     * 
     * @return the possibly null websocket client
     */
    public @Nullable WebSocketClient getWebSocketClient() {
        return webSocketClient;
    }

    /**
     * Returns the transformation service to use
     * 
     * @return the possibly null transformation service
     */
    public @Nullable TransformationService getTransformService() {
        return transformService;
    }

    /**
     * Returns the OSGI properties
     * 
     * @return the non-null, possibly empty OSGI properties
     */
    public Map<String, String> getOsgiProperties() {
        return osgiProperties;
    }
}
