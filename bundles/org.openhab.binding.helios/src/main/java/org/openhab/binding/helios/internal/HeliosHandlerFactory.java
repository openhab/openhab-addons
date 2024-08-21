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
package org.openhab.binding.helios.internal;

import static org.openhab.binding.helios.internal.HeliosBindingConstants.HELIOS_VARIO_IP_2_21_TYPE;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.ClientBuilder;

import org.openhab.binding.helios.internal.handler.HeliosHandler221;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link HeliosHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Karel Goderis - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.helios")
public class HeliosHandlerFactory extends BaseThingHandlerFactory {

    private static final int EVENT_STREAM_CONNECT_TIMEOUT = 3;
    private static final int EVENT_STREAM_READ_TIMEOUT = 200;
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(HELIOS_VARIO_IP_2_21_TYPE);

    private final ClientBuilder clientBuilder;

    @Activate
    public HeliosHandlerFactory(@Reference ClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder //
                .connectTimeout(EVENT_STREAM_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(EVENT_STREAM_READ_TIMEOUT, TimeUnit.SECONDS);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(HELIOS_VARIO_IP_2_21_TYPE)) {
            return new HeliosHandler221(thing, clientBuilder);
        }

        return null;
    }
}
