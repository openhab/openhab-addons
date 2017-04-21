/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.antiferencematrix.internal;

import static org.openhab.binding.antiferencematrix.AntiferenceMatrixBindingConstants.*;

import java.util.Hashtable;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.antiferencematrix.AntiferenceMatrixBindingConstants;
import org.openhab.binding.antiferencematrix.handler.AntiferenceMatrixBridgeHandler;
import org.openhab.binding.antiferencematrix.handler.AntiferenceMatrixInputHandler;
import org.openhab.binding.antiferencematrix.handler.AntiferenceMatrixOutputHandler;
import org.openhab.binding.antiferencematrix.internal.discovery.AntiferenceMatrixDiscoveryService;
import org.osgi.framework.ServiceRegistration;

/**
 * The {@link AntiferenceMatrixHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Neil Renaud - Initial contribution
 */
public class AntiferenceMatrixHandlerFactory extends BaseThingHandlerFactory {

    private ServiceRegistration<?> discoveryServiceReg;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_MATRIX)) {
            AntiferenceMatrixBridgeHandler bridgeHandler = new AntiferenceMatrixBridgeHandler((Bridge) thing);
            registerDeviceDiscoveryService(bridgeHandler);
            return bridgeHandler;
        } else if (thingTypeUID.equals(THING_TYPE_MATRIX_OUTPUT)) {
            return new AntiferenceMatrixOutputHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_MATRIX_INPUT)) {
            return new AntiferenceMatrixInputHandler(thing);
        }
        return null;
    }

    private void registerDeviceDiscoveryService(AntiferenceMatrixBridgeHandler bridgeHandler) {
        AntiferenceMatrixDiscoveryService discoveryService = new AntiferenceMatrixDiscoveryService(bridgeHandler);
        discoveryServiceReg = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<String, Object>());
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (discoveryServiceReg != null && thingHandler.getThing().getThingTypeUID()
                .equals(AntiferenceMatrixBindingConstants.THING_TYPE_MATRIX)) {
            discoveryServiceReg.unregister();
            discoveryServiceReg = null;
        }
        super.removeHandler(thingHandler);
    }
}
