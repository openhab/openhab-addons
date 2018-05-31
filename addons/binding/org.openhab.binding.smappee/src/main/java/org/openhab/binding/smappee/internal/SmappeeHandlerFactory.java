/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smappee.internal;

import static org.openhab.binding.smappee.SmappeeBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.smappee.SmappeeBindingConstants;
import org.openhab.binding.smappee.handler.SmappeeActuatorHandler;
import org.openhab.binding.smappee.handler.SmappeeApplianceHandler;
import org.openhab.binding.smappee.handler.SmappeeHandler;
import org.openhab.binding.smappee.handler.SmappeeSensorHandler;
import org.openhab.binding.smappee.internal.discovery.SmappeeDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmappeeHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Niko Tanghe - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.smappee", configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class SmappeeHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .concat(SmappeeBindingConstants.SUPPORTED_BRIDGE_TYPES_UIDS.stream(),
                    SmappeeBindingConstants.SUPPORTED_THING_TYPES_UIDS.stream())
            .collect(Collectors.toSet());

    private final Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(SmappeeHandlerFactory.class);

    public SmappeeHandlerFactory() {
        logger.debug("Creating new instance of SmappeeHandlerFactory");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        if (thingTypeUID.equals(THING_TYPE_SMAPPEE)) {
            ThingUID smappeeBridgeUID = getBridgeThingUID(thingTypeUID, thingUID, configuration);
            return super.createThing(thingTypeUID, configuration, smappeeBridgeUID, null);
        }
        if (thingTypeUID.equals(THING_TYPE_APPLIANCE) || thingTypeUID.equals(THING_TYPE_ACTUATOR)
                || thingTypeUID.equals(THING_TYPE_SENSOR)) {
            ThingUID smappeeApplianceUID = getApplianceUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, smappeeApplianceUID, bridgeUID);
        }
        throw new IllegalArgumentException(
                "The thing type " + thingTypeUID + " is not supported by the smappee binding.");
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_SMAPPEE)) {
            SmappeeHandler bridgeHandler = new SmappeeHandler((Bridge) thing);
            registerDiscoveryService(bridgeHandler);
            return bridgeHandler;
        } else if (thingTypeUID.equals(THING_TYPE_APPLIANCE)) {
            return new SmappeeApplianceHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_ACTUATOR)) {
            return new SmappeeActuatorHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SENSOR)) {
            return new SmappeeSensorHandler(thing);
        } else {
            logger.warn("ThingHandler not found for {}", thing.getThingTypeUID());
            return null;
        }
    }

    private synchronized void registerDiscoveryService(SmappeeHandler bridgeHandler) {
        SmappeeDiscoveryService discoveryService = new SmappeeDiscoveryService(bridgeHandler);
        discoveryService.activate();
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof SmappeeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                SmappeeDiscoveryService service = (SmappeeDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                if (service != null) {
                    service.deactivate();
                }
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }

    private ThingUID getBridgeThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration) {
        if (thingUID == null) {
            String hostID = (String) configuration.get(PARAMETER_SERVICE_LOCATION_NAME);
            return new ThingUID(thingTypeUID, hostID);
        }
        return thingUID;
    }

    private ThingUID getApplianceUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID) {
        if (thingUID == null) {
            String applianceId = (String) configuration.get("id");
            return new ThingUID(thingTypeUID, applianceId, bridgeUID.getId());
        }
        return thingUID;
    }

}
