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
package org.openhab.binding.pushsafer.internal.factory;

import static org.openhab.binding.pushsafer.internal.PushsaferBindingConstants.PUSHSAFER_ACCOUNT;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.pushsafer.internal.handler.PushsaferAccountHandler;
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
 * The {@link PushsaferHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Kevin Siml - Initial contribution, forked from Christoph Weitkamp
 */
@Component(configurationPid = "binding.pushsafer", service = ThingHandlerFactory.class)
@NonNullByDefault
public class PushsaferHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(PUSHSAFER_ACCOUNT);

    private final HttpClient httpClient;

    @Activate
    public PushsaferHandlerFactory(final @Reference HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (PUSHSAFER_ACCOUNT.equals(thingTypeUID)) {
            return new PushsaferAccountHandler(thing, httpClient);
        }

        return null;
    }
}
