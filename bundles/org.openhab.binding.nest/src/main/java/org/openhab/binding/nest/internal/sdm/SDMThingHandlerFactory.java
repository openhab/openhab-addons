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
package org.openhab.binding.nest.internal.sdm;

import static org.openhab.binding.nest.internal.sdm.SDMBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nest.internal.sdm.handler.SDMAccountHandler;
import org.openhab.binding.nest.internal.sdm.handler.SDMCameraHandler;
import org.openhab.binding.nest.internal.sdm.handler.SDMThermostatHandler;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
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
 * The {@link SDMThingHandlerFactory} is responsible for creating SDM thing handlers.
 *
 * @author Brian Higginbotham - Initial contribution
 * @author Wouter Born - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.nest")
@NonNullByDefault
public class SDMThingHandlerFactory extends BaseThingHandlerFactory {

    private HttpClientFactory httpClientFactory;
    private OAuthFactory oAuthFactory;
    private final TimeZoneProvider timeZoneProvider;

    @Activate
    public SDMThingHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference OAuthFactory oAuthFactory, final @Reference TimeZoneProvider timeZoneProvider) {
        this.httpClientFactory = httpClientFactory;
        this.oAuthFactory = oAuthFactory;
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_ACCOUNT)) {
            return new SDMAccountHandler((Bridge) thing, httpClientFactory, oAuthFactory);
        } else if (thingTypeUID.equals(THING_TYPE_CAMERA)) {
            return new SDMCameraHandler(thing, timeZoneProvider);
        } else if (thingTypeUID.equals(THING_TYPE_DISPLAY)) {
            return new SDMCameraHandler(thing, timeZoneProvider);
        } else if (thingTypeUID.equals(THING_TYPE_DOORBELL)) {
            return new SDMCameraHandler(thing, timeZoneProvider);
        } else if (thingTypeUID.equals(THING_TYPE_THERMOSTAT)) {
            return new SDMThermostatHandler(thing, timeZoneProvider);
        }

        return null;
    }
}
