package org.openhab.binding.s7.handler;

import java.util.Dictionary;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;

public abstract class S7BaseThingHandler extends BaseThingHandler {

    public S7BaseThingHandler(Thing thing) {
        super(thing);
        // TODO Auto-generated constructor stub
    }

    public void ProcessNewData(Dictionary<Integer, byte[]> data) {

    }
}
