/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.modbus.solaxx3mic.internal;

import static org.openhab.binding.modbus.solaxx3mic.internal.SolaxX3MicBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolaxX3MicHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Stanislaw Wawszczak - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.solaxx3mic", service = ThingHandlerFactory.class)
public class SolaxX3MicHandlerFactory extends BaseThingHandlerFactory {

    /**
     * Logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(SolaxX3MicHandlerFactory.class);

    @Override
    protected void activate(ComponentContext componentContext) {
        logger.debug("Called activate function");
        super.activate(componentContext);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        // logger.debug("Asked to support {}, our thingTypeUID is {}", thingTypeUID.getAsString(),
        // THING_TYPE_SOLAX_X3_MIC.getAsString());
        return THING_TYPE_SOLAX_X3_MIC.equals(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_SOLAX_X3_MIC.equals(thingTypeUID)) {
            logger.debug("New Solax X3 Mic Handler created");
            return new SolaxX3MicHandler(thing);
        }

        return null;
    }
}
