package org.openhab.binding.omnilink.handler;

import org.eclipse.smarthome.core.thing.binding.ThingHandler;

import com.digitaldan.jomnilinkII.MessageTypes.statuses.UnitStatus;

public interface UnitHandler extends ThingHandler {
    public void handleUnitStatus(UnitStatus unitStatus);

}
