package org.openhab.binding.netatmo.handler;

import io.rudolph.netatmo.api.common.model.Device;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;

import java.time.ZoneOffset;

abstract public class BaseDeviceHandler extends NetatmoDeviceHandler<Device> {

    public BaseDeviceHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    protected @Nullable Long getDataTimestamp() {
        if (device != null && device.getLastStatusStore() != null) {
            return device.getLastStatusStore().toEpochSecond(ZoneOffset.UTC);
        }
        return null;
    }
}
