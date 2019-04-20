/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.apcupsd.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.apcupsd.ApcUpsDBindingConstants;
import org.openhab.binding.apcupsd.internal.handler.ApcUpsThingHandler;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link ApcUpsDHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Aitor Iturrioz - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.apcupsd")
@NonNullByDefault
public class ApcUpsDHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(ApcUpsDBindingConstants.THING_TYPE_APCUPSTCP);

    private @NonNullByDefault({}) SerialPortManager serialPortManager;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(ApcUpsDBindingConstants.THING_TYPE_APCUPSTCP)) {
            return new ApcUpsThingHandler(thing);
        }

        return null;
    }
}
