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
package org.openhab.binding.sagercaster.internal;

import static org.openhab.binding.sagercaster.internal.SagerCasterBindingConstants.THING_TYPE_SAGERCASTER;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sagercaster.internal.caster.SagerWeatherCaster;
import org.openhab.binding.sagercaster.internal.handler.SagerCasterHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link SagerCasterHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.sagercaster")
@NonNullByDefault
public class SagerCasterHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_SAGERCASTER);
    private final WindDirectionStateDescriptionProvider stateDescriptionProvider;
    private final SagerWeatherCaster sagerWeatherCaster;

    @Activate
    public SagerCasterHandlerFactory(@Reference SagerWeatherCaster sagerWeatherCaster,
            @Reference WindDirectionStateDescriptionProvider provider) {
        this.stateDescriptionProvider = provider;
        this.sagerWeatherCaster = sagerWeatherCaster;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        return thingTypeUID.equals(THING_TYPE_SAGERCASTER)
                ? new SagerCasterHandler(thing, stateDescriptionProvider, sagerWeatherCaster)
                : null;
    }
}
