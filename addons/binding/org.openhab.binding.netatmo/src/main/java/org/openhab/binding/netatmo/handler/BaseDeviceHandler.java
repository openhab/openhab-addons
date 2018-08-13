/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;


import io.rudolph.netatmo.api.common.model.Device;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;

import java.time.ZoneOffset;

/**
 * {@link org.openhab.binding.netatmo.handler.BaseDeviceHandler} is the class used provide base functionality
 * for netatmo devices
 *
 * @author Michael Rudolph - Initial contribution
 */
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
