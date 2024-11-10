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
package org.openhab.binding.nikohomecontrol.internal;

import static org.openhab.binding.nikohomecontrol.internal.NikoHomeControlBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.handler.NikoHomeControlAccessHandler;
import org.openhab.binding.nikohomecontrol.internal.handler.NikoHomeControlActionHandler;
import org.openhab.binding.nikohomecontrol.internal.handler.NikoHomeControlAlarmHandler;
import org.openhab.binding.nikohomecontrol.internal.handler.NikoHomeControlBridgeHandler1;
import org.openhab.binding.nikohomecontrol.internal.handler.NikoHomeControlBridgeHandler2;
import org.openhab.binding.nikohomecontrol.internal.handler.NikoHomeControlMeterHandler;
import org.openhab.binding.nikohomecontrol.internal.handler.NikoHomeControlThermostatHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link NikoHomeControlHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Mark Herwege - Initial Contribution
 */

@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.nikohomecontrol")
public class NikoHomeControlHandlerFactory extends BaseThingHandlerFactory {

    private final NetworkAddressService networkAddressService;
    private final TimeZoneProvider timeZoneProvider;

    @Activate
    public NikoHomeControlHandlerFactory(final @Reference NetworkAddressService networkAddressService,
            final @Reference TimeZoneProvider timeZoneProvider) {
        super();
        this.networkAddressService = networkAddressService;
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID) || BRIDGE_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (BRIDGE_THING_TYPES_UIDS.contains(thing.getThingTypeUID())) {
            if (BRIDGEII_THING_TYPE.equals(thing.getThingTypeUID())) {
                return new NikoHomeControlBridgeHandler2((Bridge) thing, networkAddressService, timeZoneProvider);
            } else {
                return new NikoHomeControlBridgeHandler1((Bridge) thing, networkAddressService, timeZoneProvider);
            }
        } else if (ACTION_THING_TYPES_UIDS.contains(thing.getThingTypeUID())) {
            return new NikoHomeControlActionHandler(thing);
        } else if (THERMOSTAT_THING_TYPES_UIDS.contains(thing.getThingTypeUID())) {
            return new NikoHomeControlThermostatHandler(thing);
        } else if (METER_THING_TYPES_UIDS.contains(thing.getThingTypeUID())) {
            return new NikoHomeControlMeterHandler(thing);
        } else if (ACCESS_THING_TYPES_UIDS.contains(thing.getThingTypeUID())) {
            return new NikoHomeControlAccessHandler(thing);
        } else if (ALARM_THING_TYPES_UIDS.contains(thing.getThingTypeUID())) {
            return new NikoHomeControlAlarmHandler(thing);
        }

        return null;
    }
}
