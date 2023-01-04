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
package org.openhab.binding.openthermgateway.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openthermgateway.handler.BoilerHandler;
import org.openhab.binding.openthermgateway.handler.OpenThermGatewayHandler;
import org.openhab.binding.openthermgateway.handler.VentilationHeatRecoveryHandler;
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
 * The {@link OpenThermGatewayHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Arjen Korevaar - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.openthermgateway")
@NonNullByDefault
public class OpenThermGatewayHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(OpenThermGatewayHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return OpenThermGatewayBindingConstants.SUPPORTED_THING_TYPE_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(OpenThermGatewayBindingConstants.OPENTHERM_GATEWAY_THING_TYPE_UID)) {
            return new OpenThermGatewayHandler((Bridge) thing);
        } else if (thingTypeUID.equals(OpenThermGatewayBindingConstants.BOILER_THING_TYPE_UID)) {
            return new BoilerHandler(thing);
        } else if (thingTypeUID.equals(OpenThermGatewayBindingConstants.VENTILATION_HEATRECOVERY_THING_TYPE_UID)) {
            return new VentilationHeatRecoveryHandler(thing);
        } else if (thingTypeUID.equals(OpenThermGatewayBindingConstants.LEGACY_THING_TYPE_UID)) {
            logger.warn(
                    "Thing type {} is no longer supported by the OpenThermGateway binding. You need to create new Things according to the documentation.",
                    thingTypeUID);
        }

        return null;
    }
}
