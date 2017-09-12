package org.openhab.binding.fronius.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FroniusDeviceThingHandler extends FroniusBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FroniusDeviceThingHandler.class);

    private int device;

    public FroniusDeviceThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        if (getConfig().get("device") == null) {
            logger.error("Service {} requires a device id", getServiceDescription());
            updateStatus(ThingStatus.OFFLINE);
        } else {
            device = Integer.parseInt(getConfig().get("device").toString());
            super.initialize();
        }
    }

    protected int getDevice() {
        return device;
    }
}
