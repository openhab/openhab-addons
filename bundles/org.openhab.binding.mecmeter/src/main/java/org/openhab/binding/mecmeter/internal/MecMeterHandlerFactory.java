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
package org.openhab.binding.mecmeter.internal;

import static org.openhab.binding.mecmeter.MecMeterBindingConstants.THING_TYPE_METER;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.mecmeter.MecMeterBindingConstants;
import org.openhab.binding.mecmeter.handler.MecMeterHandler;
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
 * The {@link MecMeterHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Florian Pazour - Initial contribution
 * @author Klaus Berger - Initial contribution
 * @author Kai Kreuzer - Refactoring for openHAB 3
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.mecmeter")
public class MecMeterHandlerFactory extends BaseThingHandlerFactory {

    private final HttpClient httpClient;

    @Activate
    public MecMeterHandlerFactory(@Reference HttpClientFactory httpClientFactory) {
        httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return MecMeterBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_METER)) {
            return new MecMeterHandler(thing, httpClient);
        }
        return null;
    }
}
