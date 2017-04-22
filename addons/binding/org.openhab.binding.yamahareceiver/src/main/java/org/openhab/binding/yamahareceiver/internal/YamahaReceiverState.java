/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.yamahareceiver.internal.protocol.YamahaReceiverCommunication;
import org.openhab.binding.yamahareceiver.internal.protocol.YamahaReceiverCommunication.Zone;

/**
 * Receiver state
 *
 * @author David Gräff
 */
public class YamahaReceiverState {

    public boolean power = false;
    public String input = "";
    public String surroundProgram = "";
    public float volume = 0.0f; // volume in percent
    public boolean mute = false;
    public List<String> inputNames = new ArrayList<String>();
    private final YamahaReceiverCommunication com;
    public int netRadioChannel = 0;

    // Some AVR information
    public String name = "";
    public String id = "";
    public String version = "";
    public List<Zone> additional_zones = new ArrayList<>();

    public YamahaReceiverState(YamahaReceiverCommunication com) {
        this.com = com;
    }

    /**
     * We need that called only once. Will give us name, id, version and
     * zone information.
     *
     * @throws IOException
     */
    public void updateDeviceInformation() throws IOException {
        com.updateDeviceInformation(this);
        com.updateInputsList(this);
    }

    /**
     * Update power, input, surround, volume and mute information
     *
     * @throws IOException
     */
    public void updateState() throws IOException {
        com.updateState(this);
    }

    public Zone getZone() {
        return com.getZone();
    }

    public boolean isPower() {
        return power;
    }

    public String getInput() {
        return input;
    }

    public String getSurroundProgram() {
        return surroundProgram;
    }

    public float getVolume() {
        return volume;
    }

    public boolean isMute() {
        return mute;
    }

    public List<String> getInputNames() {
        return inputNames;
    }

    public void setPower(boolean power) throws IOException {
        com.setPower(power);
        // If the device was off and no goes on, we refetch device information.
        // The user could have renamed some of the inputs etc.
        if (!this.power && power) {
            updateDeviceInformation();
        }
        this.power = power;
    }

    public void setInput(String input) throws IOException {
        com.setInput(input);
        this.input = input;
    }

    public void setSurroundProgram(String surroundProgram) throws IOException {
        com.setSurroundProgram(surroundProgram);
        this.surroundProgram = surroundProgram;
    }

    /**
     * Sets the absolute volume in decibel.
     *
     * @param volume Absolute value in decibel ([-80,+12]).
     * @throws IOException
     */
    public void setVolumeDB(float volume) throws IOException {
        com.setVolume(volume);
        // Compute value in percent
        this.volume = (volume + -YamahaReceiverCommunication.VOLUME_MIN) * 100.0f
                / YamahaReceiverCommunication.VOLUME_RANGE;
    }

    /**
     * Sets the volume in percent
     *
     * @param volume
     * @throws IOException
     */
    public void setVolume(float volume) throws IOException {
        if (volume < 0) {
            volume = 0;
        }
        if (volume > 100) {
            volume = 100;
        }
        // Compute value in db
        com.setVolume(
                volume * YamahaReceiverCommunication.VOLUME_RANGE / 100.0f + YamahaReceiverCommunication.VOLUME_MIN);
        // Assign percentage value to internal value (if no IOException occurred)
        this.volume = Math.round(volume * 100) / 100;
    }

    /**
     * Increase or decrease the volume by the given percentage.
     *
     * @param percent
     * @throws IOException
     */
    public void setVolumeRelative(float percent) throws IOException {
        setVolume(volume + percent);
    }

    public void setMute(boolean mute) throws IOException {
        com.setMute(mute);
        this.mute = mute;
    }

    public void setNetRadio(int netRadioChannel) throws IOException {
        com.setNetRadio(netRadioChannel);
        this.netRadioChannel = netRadioChannel;
    }

    public String getHost() {
        return com.getHost();
    }
}
