/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fronius.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base Handler class for Fronius services that use device scope.
 *
 * @author Gerrit Beine - Initial contribution
 */
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
