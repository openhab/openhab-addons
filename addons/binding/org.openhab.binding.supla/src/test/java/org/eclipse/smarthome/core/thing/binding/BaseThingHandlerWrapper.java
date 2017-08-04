package org.eclipse.smarthome.core.thing.binding;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;

import static java.util.Objects.requireNonNull;

public final class BaseThingHandlerWrapper {
    private final BaseThingHandler baseThingHandler;

    public BaseThingHandlerWrapper(BaseThingHandler baseThingHandler) {
        this.baseThingHandler = requireNonNull(baseThingHandler);
    }

    public Thing getThing() {
        return baseThingHandler.thing;
    }

    public void setThing(Thing thing) {
        baseThingHandler.thing = thing;
    }

    public ThingRegistry getThingRegistry() {
        return baseThingHandler.thingRegistry;
    }

    public void setThingRegistry(ThingRegistry thingRegistry) {
        baseThingHandler.thingRegistry = thingRegistry;
    }
}
