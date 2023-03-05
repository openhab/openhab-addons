/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.fsinternetradio.internal.radio;

import static org.openhab.binding.fsinternetradio.internal.radio.FrontierSiliconRadioConstants.*;

import java.io.IOException;

import org.eclipse.jetty.client.HttpClient;

/**
 * Class representing an internet radio based on the frontier silicon chipset. Tested with "hama IR110" and Medion
 * MD87180" internet radios.
 *
 * @author Rainer Ostendorf
 * @author Patrick Koenemann
 * @author Mihaela Memova - removed duplicated check for the percent value range
 */
public class FrontierSiliconRadio {

    /** The http connection/session used for controlling the radio. */
    private final FrontierSiliconRadioConnection conn;

    /** the volume of the radio. we cache it for fast increase/decrease. */
    private int currentVolume = 0;

    /**
     * Constructor for the Radio class
     *
     * @param hostname Host name of the Radio addressed, e.g. "192.168.0.100"
     * @param port Port number, default: 80 (http)
     * @param pin Access PIN number of the radio. Must be 4 digits, e.g. "1234"
     * @param httpClient http client instance to use
     *
     * @author Rainer Ostendorf
     */
    public FrontierSiliconRadio(String hostname, int port, String pin, HttpClient httpClient) {
        this.conn = new FrontierSiliconRadioConnection(hostname, port, pin, httpClient);
    }

    public boolean isLoggedIn() {
        return conn.isLoggedIn();
    }

    /**
     * Perform login to the radio and establish new session
     *
     * @author Rainer Ostendorf
     * @throws IOException if communication with the radio failed, e.g. because the device is not reachable.
     */
    public boolean login() throws IOException {
        return conn.doLogin();
    }

    /**
     * get the radios power state
     *
     * @return true when radio is on, false when radio is off
     * @throws IOException if communication with the radio failed, e.g. because the device is not reachable.
     */
    public boolean getPower() throws IOException {
        final FrontierSiliconRadioApiResult result = conn.doRequest(REQUEST_GET_POWER);
        return result.getValueU8AsBoolean();
    }

    /**
     * Turn radio on/off
     *
     * @param powerOn
     *            true turns on the radio, false turns it off
     * @throws IOException if communication with the radio failed, e.g. because the device is not reachable.
     */
    public void setPower(boolean powerOn) throws IOException {
        final String params = "value=" + (powerOn ? "1" : "0");
        conn.doRequest(REQUEST_SET_POWER, params);
    }

    /**
     * read the volume (as absolute value, 0-32)
     *
     * @return volume: 0=muted, 32=max. volume
     * @throws IOException if communication with the radio failed, e.g. because the device is not reachable.
     */
    public int getVolumeAbsolute() throws IOException {
        FrontierSiliconRadioApiResult result = conn.doRequest(REQUEST_GET_VOLUME);
        currentVolume = result.getValueU8AsInt();
        return currentVolume;
    }

    /**
     * read the volume (as percent value, 0-100)
     *
     * @return volume: 0=muted, 100=max. volume (100 corresponds 32 absolute value)
     * @throws IOException if communication with the radio failed, e.g. because the device is not reachable.
     */
    public int getVolumePercent() throws IOException {
        FrontierSiliconRadioApiResult result = conn.doRequest(REQUEST_GET_VOLUME);
        currentVolume = result.getValueU8AsInt();
        return (currentVolume * 100) / 32;
    }

    /**
     * Set the radios volume
     *
     * @param volume
     *            Radio volume: 0=mute, 32=max. volume
     * @throws IOException if communication with the radio failed, e.g. because the device is not reachable.
     */
    public void setVolumeAbsolute(int volume) throws IOException {
        final int newVolume = volume < 0 ? 0 : volume > 32 ? 32 : volume;
        final String params = "value=" + newVolume;
        conn.doRequest(REQUEST_SET_VOLUME, params);
        currentVolume = volume;
    }

    /**
     * Set the radios volume in percent
     *
     * @param volume
     *            Radio volume: 0=muted, 100=max. volume (100 corresponds 32 absolute value)
     * @throws IOException if communication with the radio failed, e.g. because the device is not reachable.
     */
    public void setVolumePercent(int volume) throws IOException {
        final int newVolumeAbsolute = (volume * 32) / 100;
        final String params = "value=" + newVolumeAbsolute;
        conn.doRequest(REQUEST_SET_VOLUME, params);
        currentVolume = volume;
    }

    /**
     * Increase radio volume by 1 step, max is 32.
     *
     * @throws IOException if communication with the radio failed, e.g. because the device is not reachable.
     */
    public void increaseVolumeAbsolute() throws IOException {
        if (currentVolume < 32) {
            setVolumeAbsolute(currentVolume + 1);
        }
    }

    /**
     * Decrease radio volume by 1 step.
     *
     * @throws IOException if communication with the radio failed, e.g. because the device is not reachable.
     */
    public void decreaseVolumeAbsolute() throws IOException {
        if (currentVolume > 0) {
            setVolumeAbsolute(currentVolume - 1);
        }
    }

    /**
     * Read the radios operating mode
     *
     * @return operating mode. On hama radio: 0="Internet Radio", 1=Spotify, 2=Player, 3="AUX IN"
     * @throws IOException if communication with the radio failed, e.g. because the device is not reachable.
     */
    public int getMode() throws IOException {
        FrontierSiliconRadioApiResult result = conn.doRequest(REQUEST_GET_MODE);
        return result.getValueU32AsInt();
    }

    /**
     * Set the radio operating mode
     *
     * @param mode
     *            On hama radio: 0="Internet Radio", 1=Spotify, 2=Player, 3="AUX IN"
     * @throws IOException if communication with the radio failed, e.g. because the device is not reachable.
     */
    public void setMode(int mode) throws IOException {
        final String params = "value=" + mode;
        conn.doRequest(REQUEST_SET_MODE, params);
    }

    /**
     * Read the Station info name, e.g. "WDR2"
     *
     * @return the station name, e.g. "WDR2"
     * @throws IOException if communication with the radio failed, e.g. because the device is not reachable.
     */
    public String getPlayInfoName() throws IOException {
        FrontierSiliconRadioApiResult result = conn.doRequest(REQUEST_GET_PLAY_INFO_NAME);
        return result.getValueC8ArrayAsString();
    }

    /**
     * read the stations radio text like the song name currently playing
     *
     * @return the radio info text, e.g. music title
     * @throws IOException if communication with the radio failed, e.g. because the device is not reachable.
     */
    public String getPlayInfoText() throws IOException {
        FrontierSiliconRadioApiResult result = conn.doRequest(REQUEST_GET_PLAY_INFO_TEXT);
        return result.getValueC8ArrayAsString();
    }

    /**
     * set a station preset. Tunes the radio to a preselected station.
     *
     * @param presetId
     * @throws IOException if communication with the radio failed, e.g. because the device is not reachable.
     */
    public void setPreset(Integer presetId) throws IOException {
        conn.doRequest(REQUEST_SET_PRESET, "value=1");
        conn.doRequest(REQUEST_SET_PRESET_ACTION, "value=" + presetId.toString());
        conn.doRequest(REQUEST_SET_PRESET, "value=0");
    }

    /**
     * read the muted state
     *
     * @return true: radio is muted, false: radio is not muted
     * @throws IOException if communication with the radio failed, e.g. because the device is not reachable.
     */
    public boolean getMuted() throws IOException {
        FrontierSiliconRadioApiResult result = conn.doRequest(REQUEST_GET_MUTE);
        return result.getValueU8AsBoolean();
    }

    /**
     * mute the radio volume
     *
     * @param muted
     *            true: mute the radio, false: unmute the radio
     * @throws IOException if communication with the radio failed, e.g. because the device is not reachable.
     */
    public void setMuted(boolean muted) throws IOException {
        final String params = "value=" + (muted ? "1" : "0");
        conn.doRequest(REQUEST_SET_MUTE, params);
    }
}
