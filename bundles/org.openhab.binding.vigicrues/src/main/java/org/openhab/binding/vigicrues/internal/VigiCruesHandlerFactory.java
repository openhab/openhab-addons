/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.vigicrues.internal;

import static org.openhab.binding.vigicrues.internal.VigiCruesBindingConstants.*;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.vigicrues.internal.handler.VigiCruesHandler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

/**
 * The {@link VigiCruesHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.vigicrues")
@NonNullByDefault
public class VigiCruesHandlerFactory extends BaseThingHandlerFactory {
    private final Gson gson;
    // Needed for converting UTC time to local time
    private final TimeZoneProvider timeZoneProvider;

    @Activate
    public VigiCruesHandlerFactory(@Reference TimeZoneProvider timeZoneProvider) {
        this.timeZoneProvider = timeZoneProvider;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(ZonedDateTime.class, (JsonDeserializer<ZonedDateTime>) (json, type,
                        jsonDeserializationContext) -> ZonedDateTime.parse(json.getAsJsonPrimitive().getAsString()))
                .create();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_VIGI_CRUES)) {
            return new VigiCruesHandler(thing, timeZoneProvider, gson);
        }

        return null;
    }

}
