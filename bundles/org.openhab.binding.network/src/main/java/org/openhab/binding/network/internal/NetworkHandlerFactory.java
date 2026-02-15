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
package org.openhab.binding.network.internal;

import static org.openhab.binding.network.internal.NetworkBindingConstants.*;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.network.internal.handler.NetworkHandler;
import org.openhab.binding.network.internal.handler.SpeedTestHandler;
import org.openhab.core.common.NamedThreadFactory;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The handler factory retrieves the binding configuration and is responsible for creating
 * PING_DEVICE and SERVICE_DEVICE handlers.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = BINDING_CONFIGURATION_PID)
public class NetworkHandlerFactory extends BaseThingHandlerFactory {
    final NetworkBindingConfiguration configuration = new NetworkBindingConfiguration();
    private static final String NETWORK_HANDLER_THREADPOOL_NAME = "networkBinding";
    private static final String NETWORK_RESOLVER_THREADPOOL_NAME = "binding-network-resolver";
    private final Logger logger = LoggerFactory.getLogger(NetworkHandlerFactory.class);
    private final ScheduledExecutorService executor = ThreadPoolManager
            .getScheduledPool(NETWORK_HANDLER_THREADPOOL_NAME);
    private volatile @Nullable ExecutorService resolver;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    // The activate component call is used to access the bindings configuration
    @Activate
    protected void activate(ComponentContext componentContext, Map<String, Object> config) {
        super.activate(componentContext);
        modified(config);
        ExecutorService resolver = this.resolver;
        if (resolver != null) {
            // This should not happen
            resolver.shutdownNow();
        }
        this.resolver = new ThreadPoolExecutor(1, Integer.MAX_VALUE, 20L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), new NamedThreadFactory(NETWORK_RESOLVER_THREADPOOL_NAME));
    }

    @Override
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
        ExecutorService resolver = this.resolver;
        if (resolver != null) {
            resolver.shutdownNow();
            this.resolver = null;
        }
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        // We update instead of replace the configuration object, so that if the user updates the
        // configuration, the values are automatically available in all handlers. Because they all
        // share the same instance.
        configuration.update(new Configuration(config).as(NetworkBindingConfiguration.class));
        logger.debug("Updated binding configuration to {}", configuration);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ExecutorService resolver = this.resolver;
        if (resolver == null) {
            // This should be impossible
            logger.error("Failed to create handler for Thing \"{}\" - handler factory hasn't been activated",
                    thing.getUID());
            return null;
        }
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(PING_DEVICE) || thingTypeUID.equals(BACKWARDS_COMPATIBLE_DEVICE)) {
            return new NetworkHandler(thing, executor, resolver, false, configuration);
        } else if (thingTypeUID.equals(SERVICE_DEVICE)) {
            return new NetworkHandler(thing, executor, resolver, true, configuration);
        } else if (thingTypeUID.equals(SPEEDTEST_DEVICE)) {
            return new SpeedTestHandler(thing);
        }
        return null;
    }
}
