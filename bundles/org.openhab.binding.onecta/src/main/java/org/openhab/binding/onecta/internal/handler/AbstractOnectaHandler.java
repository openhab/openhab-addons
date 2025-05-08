package org.openhab.binding.onecta.internal.handler;

import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;

public abstract class AbstractOnectaHandler extends BaseThingHandler {

    public AbstractOnectaHandler(Thing thing) {
        super(thing);
    }

    public abstract void refreshDevice();
}
