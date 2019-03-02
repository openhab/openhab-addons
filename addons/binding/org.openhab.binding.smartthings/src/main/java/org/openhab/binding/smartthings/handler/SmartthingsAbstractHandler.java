/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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
