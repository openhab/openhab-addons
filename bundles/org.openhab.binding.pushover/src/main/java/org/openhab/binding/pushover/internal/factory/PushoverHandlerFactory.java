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
package org.openhab.binding.pushover.internal.factory;

import static org.openhab.binding.pushover.internal.PushoverBindingConstants.PUSHOVER_ACCOUNT;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.pushover.internal.PushoverBindingConstants;
import org.openhab.binding.pushover.internal.handler.PushoverAccountHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.HttpClientInitializationException;
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
 * The {@link PushoverHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@Component(configurationPid = "binding.pushover", service = ThingHandlerFactory.class)
@NonNullByDefault
public class PushoverHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(PUSHOVER_ACCOUNT);

    private final HttpClient httpClient;

    @Activate
    public PushoverHandlerFactory(final @Reference HttpClientFactory httpClientFactory) {
        httpClient = httpClientFactory.createHttpClient(PushoverBindingConstants.BINDING_ID);
        try {
            httpClient.start();
        } catch (final Exception e) {
            throw new HttpClientInitializationException("Could not start HttpClient", e);
        }
    }

    @Override
    protected void deactivate(final ComponentContext componentContext) {
        try {
            httpClient.stop();
        } catch (final Exception e) {
            // Eat http client stop exception.
        } finally {
            super.deactivate(componentContext);
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (PUSHOVER_ACCOUNT.equals(thingTypeUID)) {
            return new PushoverAccountHandler(thing, httpClient);
        }

        return null;
    }
}
