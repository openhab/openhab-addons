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
package org.openhab.binding.daikin.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.daikin.internal.handler.DaikinAcUnitHandler;
import org.openhab.binding.daikin.internal.handler.DaikinAirbaseUnitHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link DaikinHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Tim Waterhouse - Initial contribution
 * @author Paul Smedley - Modifications to support Airbase Controllers
 * 
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.daikin")
@NonNullByDefault
public class DaikinHandlerFactory extends BaseThingHandlerFactory {

    private final DaikinDynamicStateDescriptionProvider stateDescriptionProvider;
    private final @Nullable HttpClient httpClient;

    @Activate
    public DaikinHandlerFactory(@Reference DaikinDynamicStateDescriptionProvider stateDescriptionProvider,
            @Reference DaikinHttpClientFactory httpClientFactory) {
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.httpClient = httpClientFactory.getHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return DaikinBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(DaikinBindingConstants.THING_TYPE_AC_UNIT)) {
            return new DaikinAcUnitHandler(thing, stateDescriptionProvider, httpClient);
        } else if (thingTypeUID.equals(DaikinBindingConstants.THING_TYPE_AIRBASE_AC_UNIT)) {
            return new DaikinAirbaseUnitHandler(thing, stateDescriptionProvider, httpClient);
        }
        return null;
    }
}
