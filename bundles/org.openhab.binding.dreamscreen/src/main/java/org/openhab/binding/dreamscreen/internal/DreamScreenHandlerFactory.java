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
package org.openhab.binding.dreamscreen.internal;

import static org.openhab.binding.dreamscreen.internal.DreamScreenBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.binding.dreamscreen.internal.handler.DreamScreen4kHandler;
import org.openhab.binding.dreamscreen.internal.handler.DreamScreenConnectHandler;
import org.openhab.binding.dreamscreen.internal.handler.DreamScreenHdHandler;
import org.openhab.binding.dreamscreen.internal.handler.DreamScreenInputDescriptionProvider;
import org.openhab.binding.dreamscreen.internal.handler.DreamScreenSidekickHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link DreamScreenHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Bruce Brouwer - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.dreamscreen", service = ThingHandlerFactory.class)
public class DreamScreenHandlerFactory extends BaseThingHandlerFactory {
    private Map<ThingUID, @Nullable ServiceRegistration<?>> services = new HashMap<>();
    final DreamScreenServer server;

    @Activate
    public DreamScreenHandlerFactory(@Reference DreamScreenServer server) {
        this.server = server;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_HD.equals(thingTypeUID)) {
            return new DreamScreenHdHandler(server, thing, registerDescriptionProvider(thing.getUID()));
        } else if (THING_TYPE_4K.equals(thingTypeUID)) {
            return new DreamScreen4kHandler(server, thing, registerDescriptionProvider(thing.getUID()));
        } else if (THING_TYPE_SIDEKICK.equals(thingTypeUID)) {
            return new DreamScreenSidekickHandler(server, thing);
        } else if (THING_TYPE_CONNECT.equals(thingTypeUID)) {
            return new DreamScreenConnectHandler(server, thing, registerDescriptionProvider(thing.getUID()));
        }
        return null;
    }

    private DreamScreenInputDescriptionProvider registerDescriptionProvider(ThingUID thingUID) {
        final DreamScreenInputDescriptionProvider description = new DreamScreenInputDescriptionProvider(thingUID);
        this.services.put(thingUID,
                bundleContext.registerService(DynamicStateDescriptionProvider.class, description, new Hashtable<>()));
        return description;
    }

    /**
     * Removes the handler for the specific thing. This also handles disabling the discovery
     * service when the bridge is removed.
     */
    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        final @Nullable ServiceRegistration<?> reg = this.services.remove(thingHandler.getThing().getUID());
        if (reg != null) {
            reg.unregister();
        }
        super.removeHandler(thingHandler);
    }
}
