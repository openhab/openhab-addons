/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.tapocontrol.internal;

import static org.openhab.binding.tapocontrol.internal.TapoControlBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tapocontrol.internal.device.TapoSmartPlug;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TapoControlHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Wild - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.tapocontrol")
@NonNullByDefault
public class TapoControlHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(TapoControlHandlerFactory.class);
    private TapoCredentials credentials;

    @Activate // @Reference TapoCredentials credentials,
    public TapoControlHandlerFactory(Map<String, Object> properties) {
        this.credentials = new TapoCredentials();
        @Nullable
        String username = (String) properties.get("username");
        @Nullable
        String password = (String) properties.get("password");

        if (username != null && password != null) {
            credentials.setCredectials(username, password);
        }
    }

    /**
     * Provides the supported thing types
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * Create handler of things.
     */
    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (SUPPORTED_SMART_PLUG_UIDS.contains(thingTypeUID)) {
            logger.trace("returns new TapoSmartPlug()");
            return new TapoSmartPlug(thing, credentials);
        } else {
            logger.error("ThingHandler not found for {}", thingTypeUID);
        }
        return null;
    }
}
