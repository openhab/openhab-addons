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
package org.openhab.binding.monopriceaudio.internal.dto;

/**
 * Represents the data elements of a single zone of the Monoprice Whole House Amplifier
 *
 * @author Michael Lobstein - Initial contribution
 */
public class MonopriceAudioZoneDTO {

    private String zone;
    private String page;
    private String power;
    private String mute;
    private String dnd;
    private int volume;
    private int treble;
    private int bass;
    private int balance;
    private String source;
    private String keypad;

    public void setZone(String zone) {
        this.zone = zone;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getPage() {
        return this.page;
    }

    public boolean isPageActive() {
        return ("01").equals(this.page);
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getPower() {
        return this.power;
    }

    public boolean isPowerOn() {
        return ("01").equals(this.power);
    }

    public void setMute(String mute) {
        this.mute = mute;
    }

    public String getMute() {
        return this.mute;
    }

    public boolean isMuted() {
        return ("01").equals(this.mute);
    }

    public void setDnd(String dnd) {
        this.dnd = dnd;
    }

    public String getDnd() {
        return this.dnd;
    }

    public boolean isDndOn() {
        return ("01").equals(this.dnd);
    }

    public int getVolume() {
        return this.volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getTreble() {
        return this.treble;
    }

    public void setTreble(int treble) {
        this.treble = treble;
    }

    public int getBass() {
        return this.bass;
    }

    public void setBass(int bass) {
        this.bass = bass;
    }

    public int getBalance() {
        return this.balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setKeypad(String keypad) {
        this.keypad = keypad;
    }

    public String getKeypad() {
        return this.keypad;
    }

    public boolean isKeypadActive() {
        return ("01").equals(this.keypad);
    }

    @Override
    public String toString() {
        // Re-construct the original status message from the controller
        // This is used to determine if something changed from the last polling update
        return zone + page + power + mute + dnd + (String.format("%02d", volume)) + (String.format("%02d", treble))
                + (String.format("%02d", bass)) + (String.format("%02d", balance)) + source + keypad;
    }
}
