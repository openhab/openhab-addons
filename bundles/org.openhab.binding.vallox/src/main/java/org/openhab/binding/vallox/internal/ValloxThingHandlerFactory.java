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
package org.openhab.binding.vallox.internal;

import static org.openhab.binding.vallox.internal.se.ValloxSEConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.vallox.internal.se.handler.ValloxSEHandler;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link ValloxHandlerFactory} is responsible for creating things and thing
 * handlers for Vallox binding.
 *
 * @author Hauke Fuhrmann - Initial contribution
 * @author Miika Jukka - Rewrite
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.vallox")
public class ValloxThingHandlerFactory extends BaseThingHandlerFactory {

    private final SerialPortManager serialPortManager;

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(THING_TYPE_VALLOX_SE_IP, THING_TYPE_VALLOX_SE_SERIAL).collect(Collectors.toSet()));

    @Activate
    public ValloxThingHandlerFactory(@Reference SerialPortManager serialPortManager,
            ComponentContext componentContext) {
        super.activate(componentContext);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_VALLOX_SE_IP.equals(thingTypeUID) || THING_TYPE_VALLOX_SE_SERIAL.equals(thingTypeUID)) {
            return new ValloxSEHandler(thing, serialPortManager);
        }
        return null;
    }
}
