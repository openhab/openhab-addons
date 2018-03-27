/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.internal;

import static org.openhab.binding.network.NetworkBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.network.handler.NetworkHandler;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

/**
 * The handler factory retrieves the binding configuration and is responsible for creating
 * PING_DEVICE and SERVICE_DEVICE handlers.
 *
 * @author David Graeff
 */
@Component(immediate = true, service = ThingHandlerFactory.class)
public class NetworkHandlerFactory extends BaseThingHandlerFactory {
    @NonNull
    final NetworkBindingConfiguration configuration = new NetworkBindingConfiguration();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    // The activate component call is used to access the bindings configuration
    @Activate
    protected void activate(@NonNull ComponentContext componentContext, Map<String, Object> config) {
        super.activate(componentContext);
        modified(config);
    };

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
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(PING_DEVICE) || thingTypeUID.equals(BACKWARDS_COMPATIBLE_DEVICE)) {
            return new NetworkHandler(thing, false, configuration);
        } else if (thingTypeUID.equals(SERVICE_DEVICE)) {
            return new NetworkHandler(thing, true, configuration);
        }
        return null;
    }
}
