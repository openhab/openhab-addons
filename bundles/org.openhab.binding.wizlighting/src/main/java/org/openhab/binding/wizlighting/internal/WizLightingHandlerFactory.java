/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.wizlighting.internal.WizLightingBindingConstants;
import org.openhab.binding.wizlighting.internal.handler.WizLightingHandler;
import org.openhab.binding.wizlighting.internal.handler.WizLightingMediator;
import org.openhab.binding.wizlighting.internal.exceptions.MacAddressNotValidException;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.service.component.annotations.Component;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link WizLightingHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Sriram Balakrishnan - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.wizlighting", service = ThingHandlerFactory.class)
public class WizLightingHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(WizLightingBindingConstants.THING_TYPE_WIZ_BULB);

    private final Logger logger = LoggerFactory.getLogger(WizLightingHandlerFactory.class);

    private @Nullable WizLightingMediator mediator;

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
    };

    /**
     * Used by OSGI to inject the mediator in the handler factory.
     *
     * @param mediator the mediator
     */
    public void setMediator(final WizLightingMediator mediator) {
        logger.trace("Mediator has been injected on handler factory service.");
        this.mediator = mediator;
    }

    /**
     * Used by OSGI to unsets the mediator from the handler factory.
     *
     * @param mediator the mediator
     */
    public void unsetMediator(final WizLightingMediator mitsubishiMediator) {
        logger.trace("Mediator has been unsetted from discovery service.");
        this.mediator = null;
    }

    @Override
    public boolean supportsThingType(final ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(final Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        logger.trace("Creating Handler Request for {}", thingTypeUID);

        if (thingTypeUID.equals(WizLightingBindingConstants.THING_TYPE_WIZ_BULB)) {
            WizLightingHandler handler;
            logger.debug("Creating a new WizLightingHandler...");
            try {
                handler = new WizLightingHandler(thing);
                logger.debug("WizLightingMediator will register the handler.");

                WizLightingMediator mediator = this.mediator;
                if (mediator != null) {
                    mediator.registerThingAndWizBulbHandler(thing, handler);
                } else {
                    logger.error(
                            "The mediator is missing on Handler factory. Without one mediator the handler cannot work!");
                    return null;
                }
                return handler;
            } catch (MacAddressNotValidException e) {
                logger.debug("The mac address passed by configurations is not valid.");
            }

        }
        return null;
    }

    @Override
    public void unregisterHandler(final Thing thing) {
        WizLightingMediator mediator = this.mediator;
        if (mediator != null) {
            mediator.unregisterWizBulbHandlerByThing(thing);
        }
        super.unregisterHandler(thing);
    }
}
