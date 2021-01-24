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
package org.openhab.binding.netatmo.internal.handler;

import static org.openhab.binding.netatmo.internal.utils.NetatmoCalendarUtils.getSetpointEndTimeFromNow;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.EnergyApi;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.SetpointMode;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.dto.NAPlug;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;

/**
 * {@link PlugHandler} is the class used to handle the plug
 * device of a thermostat set
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class PlugHandler extends NetatmoDeviceHandler {

    public PlugHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            TimeZoneProvider timeZoneProvider, NetatmoDescriptionProvider descriptionProvider) {
        super(bridge, channelHelpers, apiBridge, timeZoneProvider, descriptionProvider);
    }

    private @NonNullByDefault({}) HomeEnergyHandler getHomeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            return (HomeEnergyHandler) bridge.getHandler();
        }
        return null;
    }

    @Override
    protected NAPlug updateReadings() throws NetatmoException {
        EnergyApi api = apiBridge.getRestManager(EnergyApi.class);
        if (api != null) {
            return api.getThermostatData(config.id);
        }
        throw new NetatmoException("No restmanager available for Energy access");
    }

    public int getSetpointDefaultDuration() {
        HomeEnergyHandler bridgeHandler = getHomeHandler();
        return bridgeHandler != null ? bridgeHandler.getSetpointDefaultDuration() : 120;
    }

    public void callSetThermMode(String moduleId, SetpointMode targetMode) {
        EnergyApi api = apiBridge.getRestManager(EnergyApi.class);
        tryApiCall(() -> api != null
                ? api.setthermpoint(config.id, moduleId, targetMode,
                        targetMode == SetpointMode.MAX ? getSetpointEndTimeFromNow(getSetpointDefaultDuration()) : 0, 0)
                : false);
    }

    public void callSetThermTemp(String moduleId, double temperature) {
        EnergyApi api = apiBridge.getRestManager(EnergyApi.class);
        tryApiCall(() -> api != null ? api.setthermpoint(config.id, moduleId, SetpointMode.MANUAL,
                getSetpointEndTimeFromNow(getSetpointDefaultDuration()), temperature) : false);
    }

    public void callSwitchSchedule(String moduleId, String schedule) {
        EnergyApi api = apiBridge.getRestManager(EnergyApi.class);
        tryApiCall(() -> api != null ? api.switchschedule(config.id, moduleId, schedule) : false);
    }
}
