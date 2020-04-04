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
package org.openhab.binding.daikin.internal.handler;

import java.io.IOException;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.daikin.internal.DaikinBindingConstants;
import org.openhab.binding.daikin.internal.DaikinCommunicationException;
import org.openhab.binding.daikin.internal.DaikinDynamicStateDescriptionProvider;
import org.openhab.binding.daikin.internal.api.ControlInfo;
import org.openhab.binding.daikin.internal.api.Enums.FanMovement;
import org.openhab.binding.daikin.internal.api.Enums.FanSpeed;
import org.openhab.binding.daikin.internal.api.Enums.HomekitMode;
import org.openhab.binding.daikin.internal.api.Enums.Mode;
import org.openhab.binding.daikin.internal.api.SensorInfo;

/**
 * Handles communicating with a Daikin air conditioning unit.
 *
 * @author Tim Waterhouse - Initial Contribution
 * @author Paul Smedley <paul@smedley.id.au> - Modifications to support Airbase Controllers
 *
 */
@NonNullByDefault
public class DaikinAcUnitHandler extends DaikinBaseHandler {
    public DaikinAcUnitHandler(Thing thing, DaikinDynamicStateDescriptionProvider stateDescriptionProvider) {
        super(thing, stateDescriptionProvider);
    }

    @Override
    protected void pollStatus() throws IOException {
        ControlInfo controlInfo = webTargets.getControlInfo();
        updateStatus(ThingStatus.ONLINE);
        if (controlInfo != null) {
            updateState(DaikinBindingConstants.CHANNEL_AC_POWER, controlInfo.power ? OnOffType.ON : OnOffType.OFF);
            updateTemperatureChannel(DaikinBindingConstants.CHANNEL_AC_TEMP, controlInfo.temp);

            updateState(DaikinBindingConstants.CHANNEL_AC_MODE, new StringType(controlInfo.mode.name()));
            updateState(DaikinBindingConstants.CHANNEL_AC_FAN_SPEED, new StringType(controlInfo.fanSpeed.name()));
            updateState(DaikinBindingConstants.CHANNEL_AC_FAN_DIR, new StringType(controlInfo.fanMovement.name()));

            if (!controlInfo.power) {
                updateState(DaikinBindingConstants.CHANNEL_AC_HOMEKITMODE, new StringType(HomekitMode.OFF.getValue()));
            } else if (controlInfo.mode == Mode.COLD) {
                updateState(DaikinBindingConstants.CHANNEL_AC_HOMEKITMODE, new StringType(HomekitMode.COOL.getValue()));
            } else if (controlInfo.mode == Mode.HEAT) {
                updateState(DaikinBindingConstants.CHANNEL_AC_HOMEKITMODE, new StringType(HomekitMode.HEAT.getValue()));
            } else if (controlInfo.mode == Mode.AUTO) {
                updateState(DaikinBindingConstants.CHANNEL_AC_HOMEKITMODE, new StringType(HomekitMode.AUTO.getValue()));
            }
        }

        SensorInfo sensorInfo = webTargets.getSensorInfo();
        if (sensorInfo != null) {
            updateTemperatureChannel(DaikinBindingConstants.CHANNEL_INDOOR_TEMP, sensorInfo.indoortemp);

            updateTemperatureChannel(DaikinBindingConstants.CHANNEL_OUTDOOR_TEMP, sensorInfo.outdoortemp);

            if (sensorInfo.indoorhumidity.isPresent()) {
                updateState(DaikinBindingConstants.CHANNEL_HUMIDITY, new DecimalType(sensorInfo.indoorhumidity.get()));
            } else {
                updateState(DaikinBindingConstants.CHANNEL_HUMIDITY, UnDefType.UNDEF);
            }
        }
    }

    @Override
    protected boolean handleCommandInternal(ChannelUID channelUID, Command command)
            throws DaikinCommunicationException {
        switch (channelUID.getId()) {
            case DaikinBindingConstants.CHANNEL_AC_FAN_DIR:
                if (command instanceof StringType) {
                    changeFanDir(((StringType) command).toString());
                    return true;
                }
                break;
        }
        return false;
    }

    @Override
    protected void changePower(boolean power) throws DaikinCommunicationException {
        ControlInfo info = webTargets.getControlInfo();
        info.power = power;
        webTargets.setControlInfo(info);
    }

    @Override
    protected void changeSetPoint(double newTemperature) throws DaikinCommunicationException {
        ControlInfo info = webTargets.getControlInfo();
        info.temp = Optional.of(newTemperature);
        webTargets.setControlInfo(info);
    }

    @Override
    protected void changeMode(String mode) throws DaikinCommunicationException {
        ControlInfo info = webTargets.getControlInfo();
        info.mode = Mode.valueOf(mode);
        webTargets.setControlInfo(info);
    }

    @Override
    protected void changeFanSpeed(String fanSpeed) throws DaikinCommunicationException {
        ControlInfo info = webTargets.getControlInfo();
        info.fanSpeed = FanSpeed.valueOf(fanSpeed);
        webTargets.setControlInfo(info);
    }

    protected void changeFanDir(String fanDir) throws DaikinCommunicationException {
        ControlInfo info = webTargets.getControlInfo();
        info.fanMovement = FanMovement.valueOf(fanDir);
        webTargets.setControlInfo(info);
    }
}
