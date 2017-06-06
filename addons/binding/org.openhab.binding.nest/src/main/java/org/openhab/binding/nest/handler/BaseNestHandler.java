package org.openhab.binding.nest.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class BaseNestHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(BaseNestHandler.class);

    BaseNestHandler(Thing thing) {
        super(thing);
    }
}
