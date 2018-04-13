/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.spotify.internal;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.spotify.SpotifyBindingConstants;
import org.openhab.binding.spotify.handler.SpotifyDeviceHandler;
import org.openhab.binding.spotify.handler.SpotifyHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link SpotifyHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Andreas Stenlund - Initial contribution
 * @author Matthew Bowman - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.spotify", configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class SpotifyHandlerFactory extends BaseThingHandlerFactory {

    private SpotifyAuthService authService = null;

    public SpotifyAuthService getSpotifyAuthService() {
        return authService;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {

        if (thingTypeUID.equals(SpotifyBindingConstants.THING_TYPE_PLAYER)) {
            return true;
        }
        if (thingTypeUID.equals(SpotifyBindingConstants.THING_TYPE_DEVICE)) {
            return true;
        }
        return false;
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(SpotifyBindingConstants.THING_TYPE_PLAYER)) {
            return new SpotifyHandler((Bridge) thing, this);
        }
        if (thingTypeUID.equals(SpotifyBindingConstants.THING_TYPE_DEVICE)) {
            return new SpotifyDeviceHandler(thing);
        }

        return null;
    }

    @Reference
    public void bindAuthService(SpotifyAuthService service) {
        this.authService = service;
    }

    public void unbindAuthService(SpotifyAuthService service) {
        this.authService = null;
    }
}
