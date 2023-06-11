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
package org.openhab.binding.surepetcare.internal;

import static org.openhab.binding.surepetcare.internal.SurePetcareConstants.*;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.surepetcare.internal.handler.SurePetcareBridgeHandler;
import org.openhab.binding.surepetcare.internal.handler.SurePetcareDeviceHandler;
import org.openhab.binding.surepetcare.internal.handler.SurePetcareHouseholdHandler;
import org.openhab.binding.surepetcare.internal.handler.SurePetcarePetHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SurePetcareHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Rene Scherer - Initial contribution
 *
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.surepetcare")
@NonNullByDefault
public class SurePetcareHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(SurePetcareHandlerFactory.class);

    private SurePetcareAPIHelper petcareAPI = new SurePetcareAPIHelper();

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(BRIDGE_THING_TYPES_UIDS, SurePetcareConstants.SUPPORTED_THING_TYPES_UIDS).flatMap(Collection::stream)
            .collect(Collectors.toSet());

    /**
     * Returns true if the factory supports the given thing type.
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * Returns a newly created thing handler for the given thing.
     */
    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        logger.debug("createHandler - create handler for {}", thing);
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_HOUSEHOLD)) {
            return new SurePetcareHouseholdHandler(thing, petcareAPI);
        } else if (thingTypeUID.equals(THING_TYPE_HUB_DEVICE)) {
            return new SurePetcareDeviceHandler(thing, petcareAPI);
        } else if (thingTypeUID.equals(THING_TYPE_FLAP_DEVICE)) {
            return new SurePetcareDeviceHandler(thing, petcareAPI);
        } else if (thingTypeUID.equals(THING_TYPE_FEEDER_DEVICE)) {
            return new SurePetcareDeviceHandler(thing, petcareAPI);
        } else if (thingTypeUID.equals(THING_TYPE_PET)) {
            return new SurePetcarePetHandler(thing, petcareAPI);
        } else if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
            return new SurePetcareBridgeHandler((Bridge) thing, petcareAPI);
        }
        return null;
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        petcareAPI.setHttpClient(httpClientFactory.getCommonHttpClient());
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        petcareAPI.setHttpClient(null);
    }
}
