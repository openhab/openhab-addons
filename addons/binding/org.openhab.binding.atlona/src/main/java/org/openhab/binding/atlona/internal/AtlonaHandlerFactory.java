/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.atlona.internal;

import static org.openhab.binding.atlona.internal.AtlonaBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.atlona.internal.pro3.AtlonaPro3Capabilities;
import org.openhab.binding.atlona.internal.pro3.AtlonaPro3Handler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link org.openhab.binding.atlona.internal.AtlonaHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Tim Roberts - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.atlona")
public class AtlonaHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(AtlonaHandlerFactory.class);

    /**
     * The set of supported Atlona products
     */
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(THING_TYPE_PRO3_44M, THING_TYPE_PRO3_66M, THING_TYPE_PRO3_88M, THING_TYPE_PRO3_1616M)
                    .collect(Collectors.toSet()));

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

        if (thingTypeUID.equals(THING_TYPE_PRO3_44M)) {
            return new AtlonaPro3Handler(thing, new AtlonaPro3Capabilities(5, 3, Collections.singleton(5)));
        }

        if (thingTypeUID.equals(THING_TYPE_PRO3_66M)) {
            return new AtlonaPro3Handler(thing, new AtlonaPro3Capabilities(8, 4,
                    Collections.unmodifiableSet(Stream.of(6, 8).collect(Collectors.toSet()))));
        }

        if (thingTypeUID.equals(THING_TYPE_PRO3_88M)) {
            return new AtlonaPro3Handler(thing, new AtlonaPro3Capabilities(10, 6,
                    Collections.unmodifiableSet(Stream.of(8, 10).collect(Collectors.toSet()))));
        }

        if (thingTypeUID.equals(THING_TYPE_PRO3_1616M)) {
            return new AtlonaPro3Handler(thing, new AtlonaPro3Capabilities(5, 3,
                    Collections.unmodifiableSet(Stream.of(17, 18, 19, 20).collect(Collectors.toSet()))));
        }

        logger.warn("Unknown binding: {}: {}", thingTypeUID.getId(), thingTypeUID.getBindingId());
        return null;
    }
}
