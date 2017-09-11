/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink.internal;

import static org.openhab.binding.nibeuplink.NibeUplinkBindingConstants.*;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.nibeuplink.handler.ChannelSet;
import org.openhab.binding.nibeuplink.handler.F1145Handler;
import org.openhab.binding.nibeuplink.handler.F1155Handler;
import org.openhab.binding.nibeuplink.handler.F750Handler;
import org.openhab.binding.nibeuplink.handler.VVM310Handler;
import org.openhab.binding.nibeuplink.handler.VVM320Handler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

/**
 * The {@link NibeUplinkHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author afriese - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPolicy = ConfigurationPolicy.OPTIONAL, name = "binding.nibeuplink")
public class NibeUplinkHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_VVM320)) {
            return new VVM320Handler(thing, ChannelSet.All);
        } else if (thingTypeUID.equals(THING_TYPE_VVM320_SENSORS)) {
            return new VVM320Handler(thing, ChannelSet.Sensors);
        } else if (thingTypeUID.equals(THING_TYPE_VVM320_SETTINGS)) {
            return new VVM320Handler(thing, ChannelSet.Settings);
        } else if (thingTypeUID.equals(THING_TYPE_VVM320_SPECIAL)) {
            return new VVM320Handler(thing, ChannelSet.Special);
        }

        if (thingTypeUID.equals(THING_TYPE_VVM310)) {
            return new VVM310Handler(thing, ChannelSet.All);
        } else if (thingTypeUID.equals(THING_TYPE_VVM310_SENSORS)) {
            return new VVM310Handler(thing, ChannelSet.Sensors);
        } else if (thingTypeUID.equals(THING_TYPE_VVM310_SETTINGS)) {
            return new VVM310Handler(thing, ChannelSet.Settings);
        } else if (thingTypeUID.equals(THING_TYPE_VVM310_SPECIAL)) {
            return new VVM310Handler(thing, ChannelSet.Special);
        }

        if (thingTypeUID.equals(THING_TYPE_F750)) {
            return new F750Handler(thing, ChannelSet.All);
        } else if (thingTypeUID.equals(THING_TYPE_F750_SENSORS)) {
            return new F750Handler(thing, ChannelSet.Sensors);
        } else if (thingTypeUID.equals(THING_TYPE_F750_SETTINGS)) {
            return new F750Handler(thing, ChannelSet.Settings);
        } else if (thingTypeUID.equals(THING_TYPE_F750_SPECIAL)) {
            return new F750Handler(thing, ChannelSet.Special);
        }

        if (thingTypeUID.equals(THING_TYPE_F1145)) {
            return new F1145Handler(thing, ChannelSet.All);
        } else if (thingTypeUID.equals(THING_TYPE_F1145_SENSORS)) {
            return new F1145Handler(thing, ChannelSet.Sensors);
        } else if (thingTypeUID.equals(THING_TYPE_F1145_SETTINGS)) {
            return new F1145Handler(thing, ChannelSet.Settings);
        } else if (thingTypeUID.equals(THING_TYPE_F1145_SPECIAL)) {
            return new F1145Handler(thing, ChannelSet.Special);
        }

        if (thingTypeUID.equals(THING_TYPE_F1155)) {
            return new F1155Handler(thing, ChannelSet.All);
        } else if (thingTypeUID.equals(THING_TYPE_F1155_SENSORS)) {
            return new F1155Handler(thing, ChannelSet.Sensors);
        } else if (thingTypeUID.equals(THING_TYPE_F1155_SETTINGS)) {
            return new F1155Handler(thing, ChannelSet.Settings);
        } else if (thingTypeUID.equals(THING_TYPE_F1155_SPECIAL)) {
            return new F1155Handler(thing, ChannelSet.Special);
        }

        return null;
    }
}
