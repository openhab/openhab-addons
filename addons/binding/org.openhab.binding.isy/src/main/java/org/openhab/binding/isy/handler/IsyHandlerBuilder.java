package org.openhab.binding.isy.handler;

import org.eclipse.smarthome.core.thing.Thing;

public class IsyHandlerBuilder {

    IsyInsteonDeviceHandler handler;

    protected IsyHandlerBuilder(Thing thing) {
        this.handler = new IsyInsteonDeviceHandler(thing);
    }

    public static IsyHandlerBuilder builder(Thing thing) {
        return new IsyHandlerBuilder(thing);
    }

    public IsyHandlerBuilder forChannel(String channel, int deviceId) {
        this.handler.addChannelToDevice(channel, deviceId);
        return this;
    }

    public IsyInsteonDeviceHandler build() {
        return this.handler;
    }

}
