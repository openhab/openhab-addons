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
package org.openhab.binding.thekeys.internal;

import static org.openhab.binding.thekeys.internal.TheKeysBindingConstants.THING_TYPE_GATEWAY;
import static org.openhab.binding.thekeys.internal.TheKeysBindingConstants.THING_TYPE_SMARTLOCK;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.thekeys.internal.gateway.TheKeysGatewayHandler;
import org.openhab.binding.thekeys.internal.provider.TheKeyTranslationProvider;
import org.openhab.binding.thekeys.internal.smartlock.TheKeysSmartlockHandler;
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
 * The {@link TheKeysHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jordan Martin - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.thekeys", service = ThingHandlerFactory.class)
public class TheKeysHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_GATEWAY,
            THING_TYPE_SMARTLOCK);
    private final TheKeyTranslationProvider translationProvider;

    @Activate
    public TheKeysHandlerFactory(@Reference TheKeyTranslationProvider translationProvider) {
        this.translationProvider = translationProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_GATEWAY.equals(thingTypeUID)) {
            return new TheKeysGatewayHandler((Bridge) thing, translationProvider);
        } else if (THING_TYPE_SMARTLOCK.equals(thingTypeUID)) {
            return new TheKeysSmartlockHandler(thing);
        }

        return null;
    }
}
