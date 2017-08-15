/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openhab.binding.somfytahoma.SomfyTahomaBindingConstants.*;

/**
 * The {@link SomfyTahomaExteriorScreenHandler} is responsible for handling commands,
 * which are sent to one of the channels of the exterior screen thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaExteriorScreenHandler extends SomfyTahomaRollerShutterHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaExteriorScreenHandler.class);

    public SomfyTahomaExteriorScreenHandler(Thing thing) {
        super(thing);
    }

}
