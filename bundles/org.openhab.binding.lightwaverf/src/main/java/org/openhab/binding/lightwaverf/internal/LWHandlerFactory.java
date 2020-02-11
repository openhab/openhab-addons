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
package org.openhab.binding.lightwaverf.internal;

import static org.openhab.binding.lightwaverf.internal.LWBindingConstants.*;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.lightwaverf.internal.handler.DeviceHandler;
import org.openhab.binding.lightwaverf.internal.handler.LWAccountHandler;
import org.osgi.service.component.annotations.Component;
 /**
 * The {@link lightwaverfHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.lightwaverf", service = {ThingHandlerFactory.class,
    })

public class LWHandlerFactory extends BaseThingHandlerFactory 
//implements ChannelTypeProvider, ChannelGroupTypeProvider
    {

    //private final List<ChannelType> channelTypes = new CopyOnWriteArrayList<>();
    //private final List<ChannelGroupType> channelGroupTypes = new CopyOnWriteArrayList<>();


    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPE_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_LIGHTWAVE_ACCOUNT.equals(thingTypeUID)) {
            LWAccountHandler handler = new LWAccountHandler((Bridge) thing);
            return handler;
        }
        else {
            DeviceHandler handler = new DeviceHandler(thing);
            return handler;
        }
        
    }  

/*
    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return channelTypes;
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        for (ChannelType channelType : channelTypes) {
            if (channelType.getUID().equals(channelTypeUID)) {
                return channelType;
            }
        }
        return null;
    }

    @Override
    public @Nullable ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID,
            @Nullable Locale locale) {
        for (ChannelGroupType channelGroupType : channelGroupTypes) {
            if (channelGroupType.getUID().equals(channelGroupTypeUID)) {
                return channelGroupType;
            }
        }
        return null;
    }

    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(@Nullable Locale locale) {
        return channelGroupTypes;
    }

    public void addChannelType(ChannelType type) {
        channelTypes.add(type);
    }

    public void removeChannelType(ChannelType type) {
        channelTypes.remove(type);
    }

    public void removeChannelTypesForThing(ThingUID uid) {
        List<ChannelType> removes = new ArrayList<>();
        for (ChannelType c : channelTypes) {
            if (c.getUID().getAsString().startsWith(uid.getAsString())) {
                removes.add(c);
            }
        }
        channelTypes.removeAll(removes);
    }
    
    */

}
