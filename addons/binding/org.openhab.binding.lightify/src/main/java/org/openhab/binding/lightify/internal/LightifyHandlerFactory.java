/*
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lightify.internal;

import com.noctarius.lightify.model.Capability;
import com.noctarius.lightify.model.Device;
import com.noctarius.lightify.model.Luminary;
import com.noctarius.lightify.model.PowerSocket;
import com.noctarius.lightify.model.Zone;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.lightify.handler.DeviceHandler;
import org.openhab.binding.lightify.handler.GatewayHandler;
import org.openhab.binding.lightify.internal.discovery.LightifyDeviceDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

import static org.openhab.binding.lightify.LightifyConstants.SUPPORTED_THING_TYPES_UIDS;
import static org.openhab.binding.lightify.LightifyConstants.THING_TYPE_LIGHTIFY_BULB_RGBW;
import static org.openhab.binding.lightify.LightifyConstants.THING_TYPE_LIGHTIFY_BULB_SB;
import static org.openhab.binding.lightify.LightifyConstants.THING_TYPE_LIGHTIFY_BULB_TW;
import static org.openhab.binding.lightify.LightifyConstants.THING_TYPE_LIGHTIFY_POWERSOCKET;
import static org.openhab.binding.lightify.LightifyConstants.THING_TYPE_LIGHTIFY_ZONE;

/**
 * The {@link org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory} implementation
 * to create new instances of the {@link GatewayHandler} or {@link DeviceHandler} based on
 * the requested {@link Thing} type.
 *
 * @author Christoph Engelbert (@noctarius2k) - Initial contribution
 */
public class LightifyHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(LightifyHandlerFactory.class);

    private LightifyDeviceDiscoveryService discoveryService;
    private ServiceRegistration serviceRegistration;

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (GatewayHandler.SUPPORTED_TYPES.contains(thing.getThingTypeUID())) {
            GatewayHandler gatewayHandler = new GatewayHandler((Bridge) thing);
            registerDeviceDiscoveryService(gatewayHandler);
            return gatewayHandler;
        }
        if (DeviceHandler.SUPPORTED_TYPES.contains(thing.getThingTypeUID())) {
            return new DeviceHandler(thing);
        }
        return null;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        boolean supported = SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
        logger.debug("Will handle: {}[{}]", thingTypeUID, supported);
        return supported;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof GatewayHandler) {
            if (serviceRegistration != null) {
                if (discoveryService != null) {
                    discoveryService.deactivate();
                    discoveryService = null;
                }
                serviceRegistration.unregister();
                serviceRegistration = null;
            }
        }
    }

    private ServiceRegistration registerDeviceDiscoveryService(GatewayHandler gatewayHandler) {
        discoveryService = new LightifyDeviceDiscoveryService(gatewayHandler);
        discoveryService.activate();
        return registerService(DiscoveryService.class, discoveryService);
    }

    private <S> ServiceRegistration registerService(Class<S> serviceType, S service) {
        return bundleContext.registerService(serviceType.getName(), service, new Hashtable<>());
    }

    public static ThingTypeUID getThingTypeUID(Device device) {
        if (device instanceof Zone) {
            return THING_TYPE_LIGHTIFY_ZONE;
        }

        if (device instanceof Luminary) {
            Luminary luminary = (Luminary) device;
            if (luminary.hasCapability(Capability.PureWhite)) {
                return THING_TYPE_LIGHTIFY_BULB_SB;
            }

            if (luminary.hasCapability(Capability.TunableWhite) & !luminary.hasCapability(Capability.RGB)) {
                return THING_TYPE_LIGHTIFY_BULB_TW;
            }
        }

        if (device instanceof PowerSocket) {
            return THING_TYPE_LIGHTIFY_POWERSOCKET;
        }

        return THING_TYPE_LIGHTIFY_BULB_RGBW;
    }
}
