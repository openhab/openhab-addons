/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.openwebnet.internal;

import static org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants.ALL_SUPPORTED_THING_TYPES;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openwebnet.internal.handler.OpenWebNetAlarmHandler;
import org.openhab.binding.openwebnet.internal.handler.OpenWebNetAutomationHandler;
import org.openhab.binding.openwebnet.internal.handler.OpenWebNetAuxiliaryHandler;
import org.openhab.binding.openwebnet.internal.handler.OpenWebNetBridgeHandler;
import org.openhab.binding.openwebnet.internal.handler.OpenWebNetEnergyHandler;
import org.openhab.binding.openwebnet.internal.handler.OpenWebNetGenericHandler;
import org.openhab.binding.openwebnet.internal.handler.OpenWebNetLightingGroupHandler;
import org.openhab.binding.openwebnet.internal.handler.OpenWebNetLightingHandler;
import org.openhab.binding.openwebnet.internal.handler.OpenWebNetScenarioBasicHandler;
import org.openhab.binding.openwebnet.internal.handler.OpenWebNetScenarioHandler;
import org.openhab.binding.openwebnet.internal.handler.OpenWebNetThermoregulationHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetHandlerFactory} is responsible for creating thing handlers.
 *
 * @author Massimo Valla - Initial contribution, updates
 * @author Andrea Conte - Energy management, Thermoregulation
 * @author Gilberto Cocchi - Thermoregulation
 * @author Giovanni Fabiani - Auxiliary support
 */
@NonNullByDefault
@Component(configurationPid = "binding.openwebnet", service = ThingHandlerFactory.class)
public class OpenWebNetHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return ALL_SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (OpenWebNetBridgeHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("creating NEW BRIDGE Handler --- {}", thing.getUID());
            return new OpenWebNetBridgeHandler((Bridge) thing);
        } else if (OpenWebNetGenericHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("creating NEW GENERIC Handler --- {}", thing.getUID());
            return new OpenWebNetGenericHandler(thing);
        } else if (OpenWebNetLightingHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("creating NEW LIGHTING Handler --- {}", thing.getUID());
            return new OpenWebNetLightingHandler(thing);
        } else if (OpenWebNetLightingGroupHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("creating NEW LIGHTING GROUP Handler --- {}", thing.getUID());
            return new OpenWebNetLightingGroupHandler(thing);
        } else if (OpenWebNetAutomationHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("creating NEW AUTOMATION Handler --- {}", thing.getUID());
            return new OpenWebNetAutomationHandler(thing);
        } else if (OpenWebNetEnergyHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("creating NEW ENERGY Handler --- {}", thing.getUID());
            return new OpenWebNetEnergyHandler(thing);
        } else if (OpenWebNetThermoregulationHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("creating NEW THERMO Handler --- {}", thing.getUID());
            return new OpenWebNetThermoregulationHandler(thing);
        } else if (OpenWebNetScenarioHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("creating NEW SCENARIO Handler --- {}", thing.getUID());
            return new OpenWebNetScenarioHandler(thing);
        } else if (OpenWebNetAuxiliaryHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("Creating NEW AUXILIARY Handler");
            return new OpenWebNetAuxiliaryHandler(thing);
        } else if (OpenWebNetScenarioBasicHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("Creating NEW BASIC SCENARIO Handler");
            return new OpenWebNetScenarioBasicHandler(thing);
        } else if (OpenWebNetAlarmHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("creating NEW ALARM Handler");
            return new OpenWebNetAlarmHandler(thing);
        }
        logger.warn("ThingType {} is not supported by this binding", thing.getThingTypeUID());
        return null;
    }
}
