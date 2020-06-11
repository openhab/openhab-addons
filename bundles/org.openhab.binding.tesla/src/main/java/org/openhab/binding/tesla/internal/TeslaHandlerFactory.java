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
package org.openhab.binding.tesla.internal;

import static org.openhab.binding.tesla.internal.TeslaBindingConstants.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.tesla.internal.handler.TeslaAccountHandler;
import org.openhab.binding.tesla.internal.handler.TeslaVehicleHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

/**
 * The {@link TeslaHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Karel Goderis - Initial contribution
 * @author Nicolai Grødum - Adding token based auth
 * @author Kai Kreuzer - Introduced account handler
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.tesla")
public class TeslaHandlerFactory extends BaseThingHandlerFactory {

    // TODO: Those constants are Jersey specific - once we move away from Jersey,
    // this can be removed and the client builder creation simplified.
    public static final String READ_TIMEOUT_JERSEY = "jersey.config.client.readTimeout";
    public static final String CONNECT_TIMEOUT_JERSEY = "jersey.config.client.connectTimeout";

    public static final String READ_TIMEOUT = "http.receive.timeout";
    public static final String CONNECT_TIMEOUT = "http.connection.timeout";

    private static final int EVENT_STREAM_CONNECT_TIMEOUT = 3000;
    private static final int EVENT_STREAM_READ_TIMEOUT = 200000;

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(THING_TYPE_ACCOUNT, THING_TYPE_MODELS, THING_TYPE_MODEL3, THING_TYPE_MODELX, THING_TYPE_MODELY)
            .collect(Collectors.toSet());

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    private ClientBuilder injectedClientBuilder;

    private ClientBuilder clientBuilder;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_ACCOUNT)) {
            return new TeslaAccountHandler((Bridge) thing, getClientBuilder().build());
        } else {
            return new TeslaVehicleHandler(thing, getClientBuilder());
        }
    }

    private synchronized ClientBuilder getClientBuilder() {
        if (clientBuilder == null) {
            try {
                clientBuilder = ClientBuilder.newBuilder();
                clientBuilder.property(CONNECT_TIMEOUT_JERSEY, EVENT_STREAM_CONNECT_TIMEOUT);
                clientBuilder.property(READ_TIMEOUT_JERSEY, EVENT_STREAM_READ_TIMEOUT);
            } catch (Exception e) {
                // we seem to have no Jersey, so let's hope for an injected builder by CXF
                if (this.injectedClientBuilder != null) {
                    clientBuilder = injectedClientBuilder;
                    clientBuilder.property(CONNECT_TIMEOUT, EVENT_STREAM_CONNECT_TIMEOUT);
                    clientBuilder.property(READ_TIMEOUT, EVENT_STREAM_READ_TIMEOUT);
                } else {
                    throw new IllegalStateException("No JAX RS Client Builder available.");
                }
            }
        }
        return clientBuilder;
    }
}
