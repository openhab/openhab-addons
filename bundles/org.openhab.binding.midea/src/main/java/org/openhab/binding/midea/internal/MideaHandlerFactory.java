/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.midea.internal;

import static org.openhab.binding.midea.internal.MideaBindingConstants.SUPPORTED_THING_TYPES_UIDS;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.midea.internal.handler.MideaACHandler;
import org.openhab.binding.midea.internal.handler.MideaDehumidifierHandler;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link MideaHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jacek Dobrowolski - Initial contribution
 * @author Bob Eckhoff - OH addons changes, added Dehumidifier handler
 */
@NonNullByDefault
@Component(configurationPid = "binding.midea", service = ThingHandlerFactory.class)
public class MideaHandlerFactory extends BaseThingHandlerFactory {

    private final HttpClientFactory httpClientFactory;
    private final UnitProvider unitProvider;
    private static final ThingTypeUID THING_TYPE_AC = MideaBindingConstants.THING_TYPE_AC;
    private static final ThingTypeUID THING_TYPE_DEHUMIDIFIER = MideaBindingConstants.THING_TYPE_DEHUMIDIFIER;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * The MideaHandlerFactory class parameters
     * 
     * @param unitProvider OH unitProvider
     * @param httpClientFactory OH httpClientFactory
     */
    @Activate
    public MideaHandlerFactory(@Reference UnitProvider unitProvider, @Reference HttpClientFactory httpClientFactory) {
        this.httpClientFactory = httpClientFactory;
        this.unitProvider = unitProvider;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID typeUID = thing.getThingTypeUID();

        if (typeUID.equals(THING_TYPE_AC)) {
            return new MideaACHandler(thing, unitProvider, httpClientFactory.getCommonHttpClient());
        } else if (typeUID.equals(THING_TYPE_DEHUMIDIFIER)) {
            return new MideaDehumidifierHandler(thing, unitProvider, httpClientFactory.getCommonHttpClient());
        }

        return null;
    }
}
