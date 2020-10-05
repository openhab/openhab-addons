/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.wled.internal;

import static org.openhab.binding.wled.internal.WLedBindingConstants.SUPPORTED_THING_TYPES;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
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
 * The {@link WLedHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.wled", service = ThingHandlerFactory.class)
public class WLedHandlerFactory extends BaseThingHandlerFactory {
    private final HttpClient httpClient;
    private final WledDynamicStateDescriptionProvider stateDescriptionProvider;

    @Activate
    public WLedHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            final @Reference WledDynamicStateDescriptionProvider stateDescriptionProvider) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        if (SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return true;
        }
        return false;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new WLedHandler(thing, httpClient, stateDescriptionProvider);
        }
        return null;
    }
}
