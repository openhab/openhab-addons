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
package org.openhab.binding.broadlink.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.broadlink.handler.BroadlinkA1Handler;
import org.openhab.binding.broadlink.handler.BroadlinkRemoteHandler;
import org.openhab.binding.broadlink.handler.BroadlinkRemoteModel2Handler;
import org.openhab.binding.broadlink.handler.BroadlinkRemoteModel3V44057Handler;
import org.openhab.binding.broadlink.handler.BroadlinkRemoteModel4Handler;
import org.openhab.binding.broadlink.handler.BroadlinkSocketModel1Handler;
import org.openhab.binding.broadlink.handler.BroadlinkSocketModel2Handler;
import org.openhab.binding.broadlink.handler.BroadlinkSocketModel3Handler;
import org.openhab.binding.broadlink.handler.BroadlinkStripModel11K3S2UHandler;
import org.openhab.binding.broadlink.handler.BroadlinkStripModel1Handler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.transform.TransformationHelper;
import org.openhab.core.transform.TransformationService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BroadlinkHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.broadlink")
public class BroadlinkHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(BroadlinkHandlerFactory.class);

    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return BroadlinkBindingConstants.SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.keySet().contains(thingTypeUID);
    }

    private static final Set<ThingTypeUID> REMOTE_THING_UIDS = Set.of(BroadlinkBindingConstants.THING_TYPE_RM,
            BroadlinkBindingConstants.THING_TYPE_RM2, BroadlinkBindingConstants.THING_TYPE_RM3,
            BroadlinkBindingConstants.THING_TYPE_RM3Q, BroadlinkBindingConstants.THING_TYPE_RM4);

    // Remotes need the MAP transform injected into them:
    private @Nullable ThingHandler createRemoteHandler(Thing thing, ThingTypeUID thingTypeUID) {
        TransformationService transformService = TransformationHelper.getTransformationService(getBundleContext(),
                "MAP");
        if (transformService == null) {
            logger.error("Failed to get MAP transformation service for thing {}; is bundle installed?",
                    thing.getLabel());
            return null;
        }
        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_RM))
            return new BroadlinkRemoteModel2Handler(thing, transformService);
        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_RM2)) {
            if (logger.isDebugEnabled())
                logger.debug("RM 2 handler requested created");
            return new BroadlinkRemoteModel2Handler(thing, transformService);
        }
        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_RM3)) {
            if (logger.isDebugEnabled())
                logger.debug("RM 3 handler requested created");
            return new BroadlinkRemoteHandler(thing, transformService);
        }
        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_RM3Q)) {
            if (logger.isDebugEnabled())
                logger.debug("RM 3 v11057 handler requested created");
            return new BroadlinkRemoteModel3V44057Handler(thing, transformService);
        }
        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_RM4)) {
            if (logger.isDebugEnabled())
                logger.debug("RM 4 handler requested created");
            return new BroadlinkRemoteModel4Handler(thing, transformService);
        }
        logger.error("Can't create handler for Broadlink remote thing type UID: {}", thingTypeUID.getAsString());
        return null;
    }

    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (logger.isDebugEnabled())
            logger.debug("Creating Thing handler for '{}'", thingTypeUID.getAsString());

        if (REMOTE_THING_UIDS.contains(thingTypeUID)) {
            return createRemoteHandler(thing, thingTypeUID);
        }

        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_A1)) {
            if (logger.isDebugEnabled())
                logger.debug("A1 handler requested created");
            return new BroadlinkA1Handler(thing);
        }
        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_MP1))
            return new BroadlinkStripModel1Handler(thing);
        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_MP1_1K3S2U))
            return new BroadlinkStripModel11K3S2UHandler(thing);
        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_SP1))
            return new BroadlinkSocketModel1Handler(thing);
        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_SP2))
            return new BroadlinkSocketModel2Handler(thing);
        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_SP3))
            return new BroadlinkSocketModel3Handler(thing);
        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_SP3S))
            return new BroadlinkSocketModel3Handler(thing);
        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_MP2)) {
            return new BroadlinkStripModel1Handler(thing);
        } else {
            // thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_S1C);
            // thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_PIR);
            // thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_MAGNET);
            logger.error("Can't create handler for thing type UID: {}", thingTypeUID.getAsString());
            return null;
        }
    }
}
