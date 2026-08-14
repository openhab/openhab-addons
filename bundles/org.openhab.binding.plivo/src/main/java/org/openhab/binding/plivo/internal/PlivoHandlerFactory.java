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
package org.openhab.binding.plivo.internal;

import static org.openhab.binding.plivo.internal.PlivoBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plivo.internal.handler.PlivoAccountHandler;
import org.openhab.binding.plivo.internal.handler.PlivoPhoneHandler;
import org.openhab.binding.plivo.internal.service.PlivoCloudWebhookService;
import org.openhab.binding.plivo.internal.servlet.PlivoCallbackServlet;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.items.ItemRegistry;
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
 * The {@link PlivoHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Sarvesh Patil - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.plivo", service = ThingHandlerFactory.class)
public class PlivoHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCOUNT, THING_TYPE_PHONE);

    private final HttpClientFactory httpClientFactory;
    private final PlivoCallbackServlet callbackServlet;
    private final ItemRegistry itemRegistry;
    private final PlivoCloudWebhookService cloudWebhookService;

    @Activate
    public PlivoHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference PlivoCallbackServlet callbackServlet, final @Reference ItemRegistry itemRegistry,
            final @Reference PlivoCloudWebhookService cloudWebhookService) {
        this.callbackServlet = callbackServlet;
        this.itemRegistry = itemRegistry;
        this.cloudWebhookService = cloudWebhookService;
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ACCOUNT.equals(thingTypeUID)) {
            return new PlivoAccountHandler((Bridge) thing, httpClientFactory.getCommonHttpClient(),
                    cloudWebhookService);
        } else if (THING_TYPE_PHONE.equals(thingTypeUID)) {
            return new PlivoPhoneHandler(thing, callbackServlet, itemRegistry);
        }

        return null;
    }
}
