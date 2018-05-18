/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smappee.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.smappee.internal.SmappeeService;

/**
 * The {@link AbstractSmappeeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Niko Tanghe - Initial contribution
 */
abstract class AbstractSmappeeHandler extends BaseThingHandler {

    AbstractSmappeeHandler(Thing thing) {
        super(thing);
    }

    protected SmappeeHandler getBridgeHandler() {
        return (SmappeeHandler) this.getBridge().getHandler();
    }

    protected SmappeeService getSmappeeService() {
        SmappeeHandler smappeeHandler = getBridgeHandler();

        if (smappeeHandler == null) {
            return null;
        }

        return smappeeHandler.getSmappeeService();
    }
}
