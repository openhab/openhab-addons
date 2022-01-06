/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.epsonprojector.internal;

import static org.openhab.binding.epsonprojector.internal.EpsonProjectorBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.epsonprojector.internal.handler.EpsonProjectorHandler;
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
 * The {@link EpsonProjectorHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Yannick Schaus - Initial contribution
 * @author Michael Lobstein - Improvements for OH3
 */
@NonNullByDefault
@Component(configurationPid = "binding.epsonprojector", service = ThingHandlerFactory.class)
public class EpsonProjectorHandlerFactory extends BaseThingHandlerFactory {
    private final SerialPortManager serialPortManager;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Activate
    public EpsonProjectorHandlerFactory(final @Reference SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_PROJECTOR_SERIAL.equals(thingTypeUID) || THING_TYPE_PROJECTOR_TCP.equals(thingTypeUID)) {
            return new EpsonProjectorHandler(thing, serialPortManager);
        }

        return null;
    }
}
