/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;

/**
 * This is an abstract base class for the "Thing" handlers (i.e. SmartthingsXxxxeHandler).
 * It is not used the the Bridge handler (SmartthingsBridgeHandler)
 *
 * @author Bob Raker - Initial contribution
 *
 */
public abstract class SmartthingsAbstractHandler extends BaseThingHandler {

    /**
     * The device name that corresponds to the name in the Smartthings Hub
     */
    protected String smartthingsDeviceName;

    /**
     * The device location
     */
    protected String deviceLocation;

    /**
     * The constructor
     *
     * @param thing The "Thing" to be handled
     */
    public SmartthingsAbstractHandler(Thing thing) {
        super(thing);
    }

    @Override
    public abstract void handleCommand(ChannelUID channelUID, Command command);
}
