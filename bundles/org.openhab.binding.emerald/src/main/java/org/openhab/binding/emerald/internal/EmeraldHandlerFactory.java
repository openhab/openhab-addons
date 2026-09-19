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
package org.openhab.binding.emerald.internal;

import static org.openhab.binding.emerald.internal.EmeraldBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link EmeraldHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.emerald", service = ThingHandlerFactory.class)
public class EmeraldHandlerFactory extends BaseThingHandlerFactory {

    private final HttpClient httpClient;

    @Activate
    public EmeraldHandlerFactory(@Reference HttpClientFactory httpClientFactory, ComponentContext componentContext) {
        super.activate(componentContext);
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCOUNT, THING_TYPE_HWS);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ACCOUNT.equals(thingTypeUID)) {
            return new EmeraldAccountHandler((Bridge) thing, httpClient);
        } else {
            return new EmeraldHWSHandler(thing);
        }
    }
}
