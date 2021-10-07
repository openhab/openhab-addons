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
package org.openhab.binding.nadavr.internal;

import static org.openhab.binding.nadavr.internal.NADAvrBindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.openhab.binding.nadavr.internal.connector.CommandStates;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NADAvrState.java} class contains fields mapping thing configuration parameters.
 *
 * @author Dave J Schoepel - Initial contribution
 */
public class NADAvrState {

    private final Logger logger = LoggerFactory.getLogger(NADAvrState.class);

    private State mainPower;
    private State listeningMode;
    private State mute;
    private State mainVolume;
    private State mainVolumeDB;

    private NADAvrStateChangedListener handler;

    private CommandStates avrCommandStates = new CommandStates();

    /**
     *
     */
    public NADAvrState(NADAvrStateChangedListener handler) {
        this.handler = handler;
    }

    public void connectionError(String errorMessage) {
        handler.connectionError(errorMessage);
    }

    public State getStateForChannelID(String channelID) {
        switch (channelID) {
            case NADAvrBindingConstants.CHANNEL_MAIN_POWER:
                return mainPower;
            case NADAvrBindingConstants.CHANNEL_MAIN_LISTENING_MODE:
                return listeningMode;
            case NADAvrBindingConstants.CHANNEL_MAIN_MUTE:
                return mute;
            case CHANNEL_MAIN_VOLUME:
                return mainVolume;
            case CHANNEL_MAIN_VOLUME_DB:
                return mainVolumeDB;
            default:
                return null;
        }

    }

    public void setPower(boolean mainPower) {
        logger.debug("--> Main.Power current setting is: {}", avrCommandStates.getCommandState("Main.Power"));
        OnOffType newVal = mainPower ? OnOffType.ON : OnOffType.OFF;
        if (newVal != this.mainPower) {
            this.mainPower = newVal;
            handler.stateChanged(NADAvrBindingConstants.CHANNEL_MAIN_POWER, this.mainPower);
        }
    }

    public void setListeningMode(String listeningMode) {
        StringType newVal = StringType.valueOf(listeningMode);
        if (newVal != this.listeningMode) {
            this.listeningMode = newVal;

            handler.stateChanged(NADAvrBindingConstants.CHANNEL_MAIN_LISTENING_MODE, this.listeningMode);
        }
    }

    public void setMute(boolean mute) {
        OnOffType newVal = mute ? OnOffType.ON : OnOffType.OFF;
        if (newVal != this.mute) {
            this.mute = newVal;
            handler.stateChanged(NADAvrBindingConstants.CHANNEL_MAIN_MUTE, this.mute);
        }
    }

    // TODO: routine to set volume ranges... around -99 (min) and 19 (max)
    public void setMainVolume(BigDecimal volume) {
        DecimalType newVal = new DecimalType(volume);
        if (!newVal.equals(this.mainVolumeDB)) {
            this.mainVolumeDB = newVal;
            handler.stateChanged(CHANNEL_MAIN_VOLUME_DB, this.mainVolumeDB);
            // update the main volume percentage too
            // this.mainVolume = PercentType.valueOf(volume.subtract(DB_OFFSET).toString());
            BigDecimal volumePercent = (volume.subtract(VOLUME_DB_MIN)).multiply(ONE_HUNDRED).divide(VOLUME_DB_RANGE, 0,
                    RoundingMode.HALF_UP);
            PercentType newVolumePercent = new PercentType(volumePercent);
            if (!newVolumePercent.equals(this.mainVolume)) {
                this.mainVolume = newVolumePercent;
                handler.stateChanged(CHANNEL_MAIN_VOLUME, this.mainVolume);
            }
        }
    }
}
