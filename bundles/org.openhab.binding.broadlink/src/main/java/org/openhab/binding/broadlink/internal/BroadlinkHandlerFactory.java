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
package org.openhab.binding.broadlink.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.broadlink.internal.handler.BroadlinkA1Handler;
import org.openhab.binding.broadlink.internal.handler.BroadlinkRemoteModel3Handler;
import org.openhab.binding.broadlink.internal.handler.BroadlinkRemoteModel3V44057Handler;
import org.openhab.binding.broadlink.internal.handler.BroadlinkRemoteModel4MiniHandler;
import org.openhab.binding.broadlink.internal.handler.BroadlinkRemoteModel4ProHandler;
import org.openhab.binding.broadlink.internal.handler.BroadlinkRemoteModelProHandler;
import org.openhab.binding.broadlink.internal.handler.BroadlinkSocketModel1Handler;
import org.openhab.binding.broadlink.internal.handler.BroadlinkSocketModel2Handler;
import org.openhab.binding.broadlink.internal.handler.BroadlinkSocketModel3Handler;
import org.openhab.binding.broadlink.internal.handler.BroadlinkSocketModel3SHandler;
import org.openhab.binding.broadlink.internal.handler.BroadlinkStripModel11K3S2UHandler;
import org.openhab.binding.broadlink.internal.handler.BroadlinkStripModel1Handler;
import org.openhab.core.storage.StorageService;
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
 * The {@link BroadlinkHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.broadlink")
public class BroadlinkHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(BroadlinkHandlerFactory.class);
    private final BroadlinkRemoteDynamicCommandDescriptionProvider commandDescriptionProvider;
    private final StorageService storageService;

    @Activate
    public BroadlinkHandlerFactory(
            final @Reference BroadlinkRemoteDynamicCommandDescriptionProvider commandDescriptionProvider,
            @Reference StorageService storageService) {
        this.commandDescriptionProvider = commandDescriptionProvider;
        this.storageService = storageService;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return BroadlinkBindingConstants.SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.keySet().contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        logger.debug("Creating Thing handler for '{}'", thingTypeUID.getAsString());

        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_RM_PRO)) {
            return new BroadlinkRemoteModelProHandler(thing, commandDescriptionProvider, storageService);
        }
        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_RM3)) {
            return new BroadlinkRemoteModel3Handler(thing, commandDescriptionProvider, storageService);
        }
        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_RM3Q)) {
            return new BroadlinkRemoteModel3V44057Handler(thing, commandDescriptionProvider, storageService);
        }
        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_RM4_MINI)) {
            return new BroadlinkRemoteModel4MiniHandler(thing, commandDescriptionProvider, storageService);
        }
        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_RM4_PRO)) {
            return new BroadlinkRemoteModel4ProHandler(thing, commandDescriptionProvider, storageService);
        }
        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_A1)) {
            return new BroadlinkA1Handler(thing);
        }
        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_MP1)) {
            return new BroadlinkStripModel1Handler(thing);
        }
        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_MP1_1K3S2U)) {
            return new BroadlinkStripModel11K3S2UHandler(thing);
        }
        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_SP1)) {
            return new BroadlinkSocketModel1Handler(thing);
        }
        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_SP2)) {
            return new BroadlinkSocketModel2Handler(thing, false);
        }
        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_SP2S)) {
            return new BroadlinkSocketModel2Handler(thing, true);
        }
        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_SP3)) {
            return new BroadlinkSocketModel3Handler(thing);
        }
        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_SP3S)) {
            return new BroadlinkSocketModel3SHandler(thing);
        }
        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_MP2)) {
            return new BroadlinkStripModel1Handler(thing);
        } else {
            logger.warn("Can't create handler for thing type UID: {}", thingTypeUID.getAsString());
            return null;
        }
    }
}
