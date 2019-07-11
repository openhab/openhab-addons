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
package org.openhab.binding.paradoxalarm.internal.handlers;

import static org.openhab.binding.paradoxalarm.internal.handlers.ParadoxAlarmBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ParadoxAlarmHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.paradoxalarm", service = ThingHandlerFactory.class)
public class ParadoxAlarmHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(ParadoxAlarmHandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(Arrays
            .asList(COMMUNICATOR_THING_TYPE_UID, PANEL_THING_TYPE_UID, PARTITION_THING_TYPE_UID, ZONE_THING_TYPE_UID));

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (COMMUNICATOR_THING_TYPE_UID.equals(thingTypeUID)) {
            logger.debug("createHandler(): ThingHandler created for {}", thingTypeUID);
            return new ParadoxIP150BridgeHandler((Bridge) thing);
        } else if (PANEL_THING_TYPE_UID.equals(thingTypeUID)) {
            logger.debug("createHandler(): ThingHandler created for {}", thingTypeUID);
            return new ParadoxPanelHandler(thing);
        } else if (PARTITION_THING_TYPE_UID.equals(thingTypeUID)) {
            logger.debug("createHandler(): ThingHandler created for {}", thingTypeUID);
            return new ParadoxPartitionHandler(thing);
        } else if (ZONE_THING_TYPE_UID.equals(thingTypeUID)) {
            logger.debug("createHandler(): ThingHandler created for {}", thingTypeUID);
            return new ParadoxZoneHandler(thing);
        } else {
            logger.warn("Handler implementation not found for Thing: {}", thing.getLabel());
        }
        return null;
    }
}
