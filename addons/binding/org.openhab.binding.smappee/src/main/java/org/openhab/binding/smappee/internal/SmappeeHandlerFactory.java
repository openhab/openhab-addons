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

import java.util.Set;

import org.eclipse.smarthome.config.core.Configuration;
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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link SmappeeHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Niko Tanghe - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.smappee", configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class SmappeeHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.union(
            SmappeeBindingConstants.SUPPORTED_BRIDGE_TYPES_UIDS, SmappeeBindingConstants.SUPPORTED_THING_TYPES_UIDS);

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
            return new SmappeeHandler((Bridge) thing);
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
