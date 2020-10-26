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
package org.openhab.binding.vigicrues.internal;

import static org.openhab.binding.vigicrues.internal.VigiCruesBindingConstants.SUPPORTED_THING_TYPES_UIDS;

import java.util.Hashtable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.vigicrues.internal.api.ApiHandler;
import org.openhab.binding.vigicrues.internal.discovery.VigiCruesDiscoveryService;
import org.openhab.binding.vigicrues.internal.handler.VigiCruesHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link VigiCruesHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.vigicrues")
@NonNullByDefault
public class VigiCruesHandlerFactory extends BaseThingHandlerFactory {
    private final LocationProvider locationProvider;
    private final ApiHandler apiHandler;
    private @Nullable ServiceRegistration<?> serviceReg;

    @Activate
    public VigiCruesHandlerFactory(@Reference TimeZoneProvider timeZoneProvider,
            @Reference LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
        this.apiHandler = new ApiHandler(timeZoneProvider);
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        VigiCruesDiscoveryService discoveryService = new VigiCruesDiscoveryService(apiHandler, locationProvider);
        serviceReg = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<>());
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        return supportsThingType(thingTypeUID) ? new VigiCruesHandler(thing, locationProvider, apiHandler) : null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (serviceReg != null) {
            serviceReg.unregister();
        }
    }
}
