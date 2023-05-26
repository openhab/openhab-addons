/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.romyrobot.internal;

import static org.openhab.binding.romyrobot.internal.RomyRobotBindingConstants.ROMYROBOT_DEVICE;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.romyrobot.internal.api.RomyApiFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RomyRobotHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Bernhard Kreuz - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.romyrobot", service = ThingHandlerFactory.class)
public class RomyRobotHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(ROMYROBOT_DEVICE);
    private RomyApiFactory apiFactory;
    private RomyRobotStateDescriptionOptionsProvider stateDescriptionProvider;
    private final Logger logger = LoggerFactory.getLogger(RomyRobotHandlerFactory.class);

    @Activate
    public RomyRobotHandlerFactory(@Reference RomyApiFactory apiFactory,
            @Reference RomyRobotStateDescriptionOptionsProvider stateDescriptionProvider) {
        this.apiFactory = apiFactory;
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (ROMYROBOT_DEVICE.equals(thingTypeUID)) {
            try {
                return new RomyRobotHandler(thing, apiFactory, stateDescriptionProvider);
            } catch (Exception e) {
                logger.error("could not create handler {}", e);
            }
        }

        return null;
    }
}
