/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SomfyTahomaExteriorVenetianBlindHandler} is responsible for handling commands,
 * which are sent to one of the channels of the exterior venetian blind thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaExteriorVenetianBlindHandler extends SomfyTahomaRollerShutterHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaExteriorVenetianBlindHandler.class);

    public SomfyTahomaExteriorVenetianBlindHandler(Thing thing) {
        super(thing);
    }

}
