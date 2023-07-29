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
package org.openhab.binding.oceanic.internal;

import static org.openhab.binding.oceanic.internal.OceanicBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openhab.binding.oceanic.internal.handler.NetworkOceanicThingHandler;
import org.openhab.binding.oceanic.internal.handler.SerialOceanicThingHandler;
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
 * The {@link OceanicHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Karel Goderis - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.oceanic")
public class OceanicHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_SERIAL, THING_TYPE_NETWORK).collect(Collectors.toSet()));

    private final SerialPortManager serialPortManager;

    @Activate
    public OceanicHandlerFactory(final @Reference SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_SERIAL)) {
            return new SerialOceanicThingHandler(thing, serialPortManager);
        }
        if (thingTypeUID.equals(THING_TYPE_NETWORK)) {
            return new NetworkOceanicThingHandler(thing);
        }

        return null;
    }
}
