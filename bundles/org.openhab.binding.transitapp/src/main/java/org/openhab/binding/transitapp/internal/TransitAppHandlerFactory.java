package org.openhab.binding.transitapp.internal;

import static org.openhab.binding.transitapp.internal.TransitAppBindingConstants.SUPPORTED_THING_TYPES_UIDS;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.transitapp.internal.handler.TransitAppBridgeHandler;
import org.openhab.binding.transitapp.internal.handler.TransitAppStopHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;

@NonNullByDefault
@Component(service = org.openhab.core.thing.binding.ThingHandlerFactory.class, immediate = true, property = {
        "transitapp.handlerfactory=true" })
public class TransitAppHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(@Nullable Thing thing) {
        if (thing == null) {
            return null;
        }
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(TransitAppBindingConstants.THING_TYPE_BRIDGE)) {
            return new TransitAppBridgeHandler((Bridge) thing);
        } else if (thingTypeUID.equals(TransitAppBindingConstants.THING_TYPE_STOP)) {
            return new TransitAppStopHandler(thing);
        }
        return null;
    }
}
