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
package org.openhab.binding.openuv.internal;

import static org.openhab.binding.openuv.internal.OpenUVBindingConstants.*;

import java.util.Hashtable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.i18n.LocationProvider;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.openuv.internal.discovery.OpenUVDiscoveryService;
import org.openhab.binding.openuv.internal.handler.OpenUVBridgeHandler;
import org.openhab.binding.openuv.internal.handler.OpenUVReportHandler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link OpenUVHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.openuv")
public class OpenUVHandlerFactory extends BaseThingHandlerFactory {

    private final LocationProvider locationProvider;

    @Activate
    public OpenUVHandlerFactory(@Reference LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID) || BRIDGE_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (APIBRIDGE_THING_TYPE.equals(thingTypeUID)) {
            OpenUVBridgeHandler handler = new OpenUVBridgeHandler((Bridge) thing);
            registerOpenUVDiscoveryService(handler);
            return handler;
        } else if (LOCATION_REPORT_THING_TYPE.equals(thingTypeUID)) {
            return new OpenUVReportHandler(thing);
        }

        return null;
    }

    private void registerOpenUVDiscoveryService(OpenUVBridgeHandler bridgeHandler) {
        OpenUVDiscoveryService discoveryService = new OpenUVDiscoveryService(bridgeHandler, locationProvider);
        bridgeHandler.getDiscoveryServiceRegs().put(bridgeHandler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

}
