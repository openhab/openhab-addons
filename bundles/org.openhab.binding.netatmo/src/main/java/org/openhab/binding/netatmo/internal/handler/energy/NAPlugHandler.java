/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.handler.energy;

import static org.openhab.binding.netatmo.internal.utils.NetatmoCalendarUtils.getSetpointEndTimeFromNow;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.doc.NetatmoConstants.SetpointMode;
import org.openhab.binding.netatmo.internal.api.energy.EnergyApi;
import org.openhab.binding.netatmo.internal.api.energy.NAPlug;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.handler.NetatmoDeviceHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;

/**
 * {@link NAPlugHandler} is the class used to handle the plug
 * device of a thermostat set
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
@NonNullByDefault
public class NAPlugHandler extends NetatmoDeviceHandler {
    private @Nullable EnergyApi api;

    public NAPlugHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            TimeZoneProvider timeZoneProvider) {
        super(bridge, channelHelpers, apiBridge, timeZoneProvider);
        api = apiBridge.getRestManager(EnergyApi.class);
    }

    private @NonNullByDefault({}) NAHomeEnergyHandler getHomeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            return (NAHomeEnergyHandler) bridge.getHandler();
        }
        return null;
    }

    @Override
    protected NAPlug updateReadings() throws NetatmoException {
        if (api != null) {
            return api.getThermostatData(config.id);
        }
        throw new NetatmoException("No restmanager available for Energy access");
    }

    public int getSetpointDefaultDuration() {
        NAHomeEnergyHandler bridgeHandler = getHomeHandler();
        return bridgeHandler != null ? bridgeHandler.getSetpointDefaultDuration() : 120;
    }

    public void callSetThermMode(String moduleId, SetpointMode targetMode) {
        tryApiCall(() -> api != null
                ? api.setthermpoint(config.id, moduleId, targetMode,
                        targetMode == SetpointMode.MAX ? getSetpointEndTimeFromNow(getSetpointDefaultDuration()) : 0, 0)
                : false);
    }

    public void callSetThermTemp(String moduleId, double temperature) {
        tryApiCall(() -> api != null ? api.setthermpoint(config.id, moduleId, SetpointMode.MANUAL,
                getSetpointEndTimeFromNow(getSetpointDefaultDuration()), temperature) : false);
    }

    public void callSwitchSchedule(String moduleId, String schedule) {
        tryApiCall(() -> api != null ? api.switchschedule(config.id, moduleId, schedule) : false);
    }
}
