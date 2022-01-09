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
package org.openhab.binding.energenie.internal;

import static org.openhab.binding.energenie.internal.EnergenieBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.energenie.internal.handler.EnergenieHandler;
import org.openhab.binding.energenie.internal.handler.EnergeniePWMHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link EnergenieHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.energenie", service = ThingHandlerFactory.class)
public class EnergenieHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = EnergenieBindingConstants.SUPPORTED_THING_TYPES_UIDS;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_PMSLAN.equals(thingTypeUID)) {
            return new EnergenieHandler(thing, EnergenieProtocolEnum.V20);
        } else if (THING_TYPE_PM2LAN.equals(thingTypeUID)) {
            return new EnergenieHandler(thing, EnergenieProtocolEnum.V20);
        } else if (THING_TYPE_PMS2LAN.equals(thingTypeUID)) {
            return new EnergenieHandler(thing, EnergenieProtocolEnum.V21);
        } else if (THING_TYPE_PMSWLAN.equals(thingTypeUID)) {
            return new EnergenieHandler(thing, EnergenieProtocolEnum.WLAN);
        } else if (THING_TYPE_PWMLAN.equals(thingTypeUID)) {
            return new EnergeniePWMHandler(thing);
        }
        return null;
    }
}
