/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tado.handler;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.tado.internal.api.TadoApiClient;

/**
 * Common base class for home-based thing-handler.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
abstract public class BaseHomeThingHandler extends BaseThingHandler {

    public BaseHomeThingHandler(Thing thing) {
        super(thing);
    }

    public Long getHomeId() {
        return getHomeHandler().getHomeId();
    }

    protected TadoHomeHandler getHomeHandler() {
        Bridge bridge = getBridge();
        return bridge != null ? (TadoHomeHandler) bridge.getHandler() : null;
    }

    protected TadoApiClient getApi() {
        return getHomeHandler().getApi();
    }
}
