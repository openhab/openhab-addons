/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.network.internal.handler.NetworkHandler;
import org.openhab.binding.network.internal.handler.SpeedTestHandler;
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
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.network")
public class NetworkHandlerFactory extends BaseThingHandlerFactory {
    final NetworkBindingConfiguration configuration = new NetworkBindingConfiguration();

    private final Logger logger = LoggerFactory.getLogger(NetworkHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return NetworkBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    // The activate component call is used to access the bindings configuration
    @Activate
    protected void activate(ComponentContext componentContext, Map<String, Object> config) {
        super.activate(componentContext);
        modified(config);
    }

    @Override
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
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
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(NetworkBindingConstants.PING_DEVICE)
                || thingTypeUID.equals(NetworkBindingConstants.BACKWARDS_COMPATIBLE_DEVICE)) {
            return new NetworkHandler(thing, false, configuration);
        } else if (thingTypeUID.equals(NetworkBindingConstants.SERVICE_DEVICE)) {
            return new NetworkHandler(thing, true, configuration);
        } else if (thingTypeUID.equals(NetworkBindingConstants.SPEEDTEST_DEVICE)) {
            return new SpeedTestHandler(thing);
        }
        return null;
    }
}
