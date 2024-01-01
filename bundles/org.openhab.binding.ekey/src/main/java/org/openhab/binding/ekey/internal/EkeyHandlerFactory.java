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
package org.openhab.binding.ekey.internal;

import static org.openhab.binding.ekey.internal.EkeyBindingConstants.THING_TYPE_CVLAN;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ekey.internal.handler.EkeyHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link EkeyHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.ekey", service = ThingHandlerFactory.class)
public class EkeyHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_CVLAN);

    private final EkeyDynamicStateDescriptionProvider ekeyStateDescriptionProvider;

    @Activate
    public EkeyHandlerFactory(ComponentContext componentContext,
            final @Reference EkeyDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        this.ekeyStateDescriptionProvider = dynamicStateDescriptionProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_CVLAN.equals(thingTypeUID)) {
            return new EkeyHandler(thing, ekeyStateDescriptionProvider);
        }
        return null;
    }
}
