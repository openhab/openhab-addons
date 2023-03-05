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
package org.openhab.binding.etherrain.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.etherrain.internal.handler.EtherRainHandler;
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
 * The {@link EtherRainHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Joe Inkenbrandt - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.etherrain")
@NonNullByDefault
public class EtherRainHandlerFactory extends BaseThingHandlerFactory {

    private final HttpClient httpClient;

    @Activate
    public EtherRainHandlerFactory(@Reference HttpClientFactory httpClientFactory) {
        httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return EtherRainBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(EtherRainBindingConstants.ETHERRAIN_THING)) {
            return new EtherRainHandler(thing, httpClient);
        }

        return null;
    }
}
