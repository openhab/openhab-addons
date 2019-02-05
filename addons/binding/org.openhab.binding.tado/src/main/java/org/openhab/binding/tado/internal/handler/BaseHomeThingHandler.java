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
package org.openhab.binding.tado.internal.handler;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.tado.internal.api.client.HomeApi;

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

    protected HomeApi getApi() {
        return getHomeHandler().getApi();
    }

    protected void onSuccessfulOperation() {
        // update without error -> we're back online
        if (getThing().getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
    }
}
