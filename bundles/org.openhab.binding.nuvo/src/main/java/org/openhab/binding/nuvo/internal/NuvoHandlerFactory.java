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
package org.openhab.binding.nuvo.internal;

import static org.openhab.binding.nuvo.internal.NuvoBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.nuvo.internal.handler.NuvoHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link NuvoHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.nuvo", service = ThingHandlerFactory.class)
public class NuvoHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_AMP);

    private final SerialPortManager serialPortManager;

    private final NuvoStateDescriptionOptionProvider stateDescriptionProvider;

    private final HttpClient httpClient;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Activate
    public NuvoHandlerFactory(final @Reference NuvoStateDescriptionOptionProvider provider,
            final @Reference SerialPortManager serialPortManager,
            final @Reference HttpClientFactory httpClientFactory) {
        this.stateDescriptionProvider = provider;
        this.serialPortManager = serialPortManager;
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new NuvoHandler(thing, stateDescriptionProvider, serialPortManager, httpClient);
        }

        return null;
    }
}
