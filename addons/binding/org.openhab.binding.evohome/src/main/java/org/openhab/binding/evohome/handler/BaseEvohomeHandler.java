package org.openhab.binding.evohome.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.evohome.internal.api.EvohomeApiClient;

public abstract class BaseEvohomeHandler extends BaseThingHandler {

    public BaseEvohomeHandler(Thing thing) {
        super(thing);
    }

    public abstract void update(EvohomeApiClient client);
}
