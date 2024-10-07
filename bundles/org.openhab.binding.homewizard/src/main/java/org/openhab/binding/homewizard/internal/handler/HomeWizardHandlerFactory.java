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
package org.openhab.binding.homewizard.internal.handler;

import static org.openhab.binding.homewizard.internal.HomeWizardBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link HomeWizardHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Daniël van Os - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.homewizard", service = ThingHandlerFactory.class)
public class HomeWizardHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_P1_METER,
            THING_TYPE_ENERGY_SOCKET, THING_TYPE_WATERMETER);

    private final TimeZoneProvider timeZoneProvider;

    @Activate
    public HomeWizardHandlerFactory(final @Reference TimeZoneProvider timeZoneProvider) {
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_P1_METER.equals(thingTypeUID)) {
            return new HomeWizardP1MeterHandler(thing, timeZoneProvider);
        }

        if (THING_TYPE_ENERGY_SOCKET.equals(thingTypeUID)) {
            return new HomeWizardEnergySocketHandler(thing, timeZoneProvider);
        }

        if (THING_TYPE_WATERMETER.equals(thingTypeUID)) {
            return new HomeWizardWaterMeterHandler(thing, timeZoneProvider);
        }

        return null;
    }
}
