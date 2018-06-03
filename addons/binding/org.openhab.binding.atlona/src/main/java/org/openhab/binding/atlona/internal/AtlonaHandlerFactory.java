/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.atlona.internal;

import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.atlona.AtlonaBindingConstants;
import org.openhab.binding.atlona.internal.pro3.AtlonaPro3Capabilities;
import org.openhab.binding.atlona.internal.pro3.AtlonaPro3Handler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link org.openhab.binding.atlona.internal.AtlonaHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Tim Roberts - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.atlona")
public class AtlonaHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(AtlonaHandlerFactory.class);

    /**
     * The set of supported Atlona products
     */
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(
            AtlonaBindingConstants.THING_TYPE_PRO3_44M, AtlonaBindingConstants.THING_TYPE_PRO3_66M,
            AtlonaBindingConstants.THING_TYPE_PRO3_88M, AtlonaBindingConstants.THING_TYPE_PRO3_1616M);

    /**
     * {@inheritDoc}
     *
     * Simply returns true if the given thingTypeUID is within {@link #SUPPORTED_THING_TYPES_UIDS}
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * {@inheritDoc}
     *
     * Creates the handler for the given thing given its thingTypeUID
     */
    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (thing == null) {
            logger.error("createHandler was given a null thing!");
            return null;
        }

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(AtlonaBindingConstants.THING_TYPE_PRO3_44M)) {
            return new AtlonaPro3Handler(thing, new AtlonaPro3Capabilities(5, 3, ImmutableSet.of(5)));
        }

        if (thingTypeUID.equals(AtlonaBindingConstants.THING_TYPE_PRO3_66M)) {
            return new AtlonaPro3Handler(thing, new AtlonaPro3Capabilities(8, 4, ImmutableSet.of(6, 8)));
        }

        if (thingTypeUID.equals(AtlonaBindingConstants.THING_TYPE_PRO3_88M)) {
            return new AtlonaPro3Handler(thing, new AtlonaPro3Capabilities(10, 6, ImmutableSet.of(8, 10)));
        }

        if (thingTypeUID.equals(AtlonaBindingConstants.THING_TYPE_PRO3_1616M)) {
            return new AtlonaPro3Handler(thing, new AtlonaPro3Capabilities(5, 3, ImmutableSet.of(17, 18, 19, 20)));
        }

        logger.warn("Unknown binding: {}: {}", thingTypeUID.getId(), thingTypeUID.getBindingId());
        return null;
    }
}
