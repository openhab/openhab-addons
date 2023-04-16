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
package org.openhab.binding.ojelectronics.internal;

import static org.openhab.binding.ojelectronics.internal.BindingConstants.THING_TYPE_OWD5;

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
 * The {@link ThermostatHandlerFactory} is responsible for creating {@link OJElectronicsThermostatHandler}.
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.ojelectronics", service = ThingHandlerFactory.class)
public class ThermostatHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_OWD5);
    private final TimeZoneProvider timeZoneProvider;

    /**
     * Creates a new factory
     *
     * @param httpClientFactory Factory for HttpClient
     */
    @Activate
    public ThermostatHandlerFactory(@Reference TimeZoneProvider timeZoneProvider) {
        this.timeZoneProvider = timeZoneProvider;
    }

    /**
     * Supported things of this factory.
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_OWD5.equals(thingTypeUID)) {
            return new ThermostatHandler(thing, timeZoneProvider);
        }

        return null;
    }
}
