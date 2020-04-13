/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.coronastats.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.coronastats.internal.dto.CoronaStats;

/**
 * The {@link CoronaStatsCountryHandler} is the handler for country thing
 *
 * @author Johannes Ott - Initial contribution
 */
@NonNullByDefault
abstract public class CoronaStatsThingHandler extends BaseThingHandler {
    public CoronaStatsThingHandler(Thing thing) {
        super(thing);
    }

    protected synchronized @Nullable CoronaStatsBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof CoronaStatsBridgeHandler) {
                return (CoronaStatsBridgeHandler) handler;
            }
        }

        return null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refresh();
        }
    }

    private void refresh() {
        CoronaStatsBridgeHandler handler = getBridgeHandler();
        if (handler != null) {
            CoronaStats coronaStats = handler.getCoronaStats();
            if (coronaStats != null) {
                notifyOnUpdate(coronaStats);
            }
        }
    }

    abstract public void notifyOnUpdate(CoronaStats coronaStats);
}
