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
package org.openhab.binding.octopusenergy.internal;

import static org.openhab.binding.octopusenergy.internal.OctopusEnergyBindingConstants.*;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.octopusenergy.internal.handler.OctopusEnergyBridgeHandler;
import org.openhab.binding.octopusenergy.internal.handler.OctopusEnergyElectricityMeterPointHandler;
import org.openhab.binding.octopusenergy.internal.handler.OctopusEnergyGasMeterPointHandler;
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
 * The {@link OctopusEnergyHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.octopusenergy", service = ThingHandlerFactory.class)
public class OctopusEnergyHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(OctopusEnergyHandlerFactory.class);

    private OctopusEnergyApiHelper apiHelper = new OctopusEnergyApiHelper();

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(BRIDGE_THING_TYPES_UIDS, OctopusEnergyBindingConstants.SUPPORTED_THING_TYPES_UIDS)
            .flatMap(Collection::stream).collect(Collectors.toSet());

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

        if (thingTypeUID.equals(THING_TYPE_ELECTRICITY_METER_POINT)) {
            return new OctopusEnergyElectricityMeterPointHandler(thing, apiHelper);
        } else if (thingTypeUID.equals(THING_TYPE_GAS_METER_POINT)) {
            return new OctopusEnergyGasMeterPointHandler(thing, apiHelper);
        } else if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
            return new OctopusEnergyBridgeHandler((Bridge) thing, apiHelper);
        }
        return null;
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        apiHelper.setHttpClient(httpClientFactory.getCommonHttpClient());
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        apiHelper.setHttpClient(null);
    }
}
