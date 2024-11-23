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
package org.openhab.binding.lgtvserial.internal;

import static org.openhab.binding.lgtvserial.internal.LgTvSerialBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgtvserial.internal.handler.LgTvSerialHandler;
import org.openhab.binding.lgtvserial.internal.protocol.serial.SerialCommunicatorFactory;
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
 * The {@link LgTvSerialHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Marius Bjoernstad - Initial contribution
 * @author Richard Lavoie - Added communicator to support daisy chained TV
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.lgtvserial")
public class LgTvSerialHandlerFactory extends BaseThingHandlerFactory {

    private static final SerialCommunicatorFactory FACTORY = new SerialCommunicatorFactory();

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(Stream
            .of(THING_TYPE_LGTV_GENERIC, THING_TYPE_LGTV_LV_SERIES, THING_TYPE_LGTV_LVX55_SERIES,
                    THING_TYPE_LGTV_LK_SERIES, THING_TYPE_LGTV_M6503C, THING_TYPE_LGTV_PW_SERIES)
            .collect(Collectors.toSet()));

    private final SerialPortManager serialPortManager;

    @Activate
    public LgTvSerialHandlerFactory(final @Reference SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (supportsThingType(thingTypeUID)) {
            return new LgTvSerialHandler(thing, FACTORY, serialPortManager);
        }

        return null;
    }
}
