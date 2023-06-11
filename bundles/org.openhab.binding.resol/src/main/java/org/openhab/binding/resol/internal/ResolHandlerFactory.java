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
package org.openhab.binding.resol.internal;

import static org.openhab.binding.resol.internal.ResolBindingConstants.SUPPORTED_THING_TYPES_UIDS;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.resol.handler.ResolBridgeHandler;
import org.openhab.binding.resol.handler.ResolEmuEMThingHandler;
import org.openhab.binding.resol.handler.ResolThingHandler;
import org.openhab.core.i18n.LocaleProvider;
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
 * The {@link ResolHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Raphael Mack - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.resol", service = ThingHandlerFactory.class)
public class ResolHandlerFactory extends BaseThingHandlerFactory {

    private final LocaleProvider localeProvider;

    private final ResolStateDescriptionOptionProvider stateDescriptionProvider;

    @Activate
    public ResolHandlerFactory(final @Reference ResolStateDescriptionOptionProvider stateDescriptionProvider,
            final @Reference LocaleProvider localeProvider) {
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.localeProvider = localeProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(ResolBindingConstants.THING_TYPE_UID_DEVICE)) {
            return new ResolThingHandler(thing, stateDescriptionProvider);
        }

        if (thingTypeUID.equals(ResolBindingConstants.THING_TYPE_UID_EMU_EM)) {
            return new ResolEmuEMThingHandler(thing);
        }

        if (thingTypeUID.equals(ResolBindingConstants.THING_TYPE_UID_BRIDGE)) {
            return new ResolBridgeHandler((Bridge) thing, localeProvider);
        }

        return null;
    }
}
