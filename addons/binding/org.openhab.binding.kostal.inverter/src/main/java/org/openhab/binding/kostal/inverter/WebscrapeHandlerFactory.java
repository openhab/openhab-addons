package org.openhab.binding.kostal.inverter;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

public class WebscrapeHandlerFactory extends BaseThingHandlerFactory {
    public final static ThingTypeUID KOSTAL_INVERTER = new ThingTypeUID("kostal", "kostal-inverter");

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return thingTypeUID.equals(KOSTAL_INVERTER);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (supportsThingType(thing.getThingTypeUID())) {
            return new WebscrapeHandler(thing);
        }
        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
    }

}
