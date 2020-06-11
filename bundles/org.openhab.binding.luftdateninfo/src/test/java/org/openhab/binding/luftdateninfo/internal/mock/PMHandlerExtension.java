package org.openhab.binding.luftdateninfo.internal.mock;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.luftdateninfo.internal.handler.PMHandler;

public class PMHandlerExtension extends PMHandler {

    public PMHandlerExtension(Thing thing) {
        super(thing);
        // TODO Auto-generated constructor stub
    }

    public int getConfigStatus() {
        return configStatus;
    }

    public int getUpdateStatus() {
        return updateStatus;
    }

    public State getPM25Cache() {
        return pm25Cache;
    }

    public State getPM100Cache() {
        return pm100Cache;
    }
}
