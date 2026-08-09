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
package org.openhab.binding.dreame.internal;

import static org.openhab.binding.dreame.internal.DreameBindingConstants.THING_TYPE_ACCOUNT;
import static org.openhab.binding.dreame.internal.DreameBindingConstants.THING_TYPE_MOWER;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dreame.internal.api.DreameApiClient;
import org.openhab.binding.dreame.internal.handler.DreameAccountHandler;
import org.openhab.binding.dreame.internal.handler.DreameMowerHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Creates handlers for Dreame account bridges and robotic mowers.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.dreame", service = ThingHandlerFactory.class)
public class DreameHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_ACCOUNT, THING_TYPE_MOWER);
    private final HttpClientFactory httpClientFactory;

    @Activate
    public DreameHandlerFactory(@Reference HttpClientFactory httpClientFactory) {
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (THING_TYPE_ACCOUNT.equals(thing.getThingTypeUID()) && thing instanceof Bridge bridge) {
            return new DreameAccountHandler(bridge, new DreameApiClient(httpClientFactory.getCommonHttpClient()));
        }
        if (THING_TYPE_MOWER.equals(thing.getThingTypeUID())) {
            return new DreameMowerHandler(thing);
        }
        return null;
    }
}
