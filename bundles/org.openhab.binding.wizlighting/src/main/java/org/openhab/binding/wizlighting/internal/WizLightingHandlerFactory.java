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
package org.openhab.binding.wizlighting.internal;

import static org.openhab.binding.wizlighting.internal.WizLightingBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.wizlighting.internal.handler.WizLightingHandler;
import org.openhab.binding.wizlighting.internal.handler.WizLightingMediator;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WizLightingHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Sriram Balakrishnan - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.wizlighting", service = ThingHandlerFactory.class)
public class WizLightingHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(WizLightingHandlerFactory.class);

    private final WizLightingMediator mediator;

    @Activate
    public WizLightingHandlerFactory(@Reference WizLightingMediator mediator) {
        this.mediator = mediator;

        logger.info(
                "\n\n*************************************************\nWiZ Connected Lighting, binding version: {}\n*************************************************\n",
                CURRENT_BINDING_VERSION);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(final Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        logger.trace("Creating Handler Request for {}", thingTypeUID);

        if (supportsThingType(thing.getThingTypeUID())) {
            WizLightingHandler handler;

            logger.debug("Creating a new WizLightingHandler...");
            handler = new WizLightingHandler(thing, this.mediator.getRegistrationParams());
            logger.debug("WizLightingMediator will register the handler.");

            mediator.registerThingAndWizBulbHandler(thing, handler);
            return handler;
        } else {
            logger.info("Thing type {} not supported.", thingTypeUID);
        }
        return null;
    }

    @Override
    public void unregisterHandler(final Thing thing) {
        mediator.unregisterWizBulbHandlerByThing(thing);
    }
}
