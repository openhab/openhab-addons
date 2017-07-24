/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.s7.handler;

import java.util.Dictionary;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;

/**
 * The {@link S7BaseThingHandler} is an abstract class able to process
 * data sent by the S7BridgeHandler.
 *
 * @author Laurent Sibilla - Initial contribution
 */
public abstract class S7BaseThingHandler extends BaseThingHandler {

    public S7BaseThingHandler(Thing thing) {
        super(thing);
    }

    public void processNewData(Dictionary<Integer, byte[]> data) {

    }
}
