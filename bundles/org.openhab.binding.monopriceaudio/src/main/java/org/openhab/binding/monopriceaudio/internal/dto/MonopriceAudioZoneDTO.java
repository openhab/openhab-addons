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

import static org.openhab.binding.monopriceaudio.internal.MonopriceAudioBindingConstants.*;

/**
 * Represents the data elements of a single zone of a supported amplifier
 *
 * @author Michael Lobstein - Initial contribution
 */
public class MonopriceAudioZoneDTO {

    private String zone = EMPTY;
    private String page = EMPTY;
    private String power = EMPTY;
    private String mute = EMPTY;
    private String dnd = EMPTY;
    private int volume = NIL;
    private int treble = NIL;
    private int bass = NIL;
    private int balance = NIL;
    private String source = EMPTY;
    private String keypad = EMPTY;

    public MonopriceAudioZoneDTO() {
    }

    public MonopriceAudioZoneDTO(String zone) {
        this.zone = zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getZone() {
        return this.zone;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getPage() {
        return this.page;
    }

    public boolean isPageActive() {
        return this.page.contains(ONE);
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getPower() {
        return this.power;
    }

    public boolean isPowerOn() {
        return this.power.contains(ONE);
    }

    public void setMute(String mute) {
        this.mute = mute;
    }

    public String getMute() {
        return this.mute;
    }

    public boolean isMuted() {
        return this.mute.contains(ONE);
    }

    public void setDnd(String dnd) {
        this.dnd = dnd;
    }

    public String getDnd() {
        return this.dnd;
    }

    public boolean isDndOn() {
        return this.dnd.contains(ONE);
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
        return this.keypad.contains(ONE);
    }

    @Override
    public String toString() {
        // This is used to determine if something changed from the last polling update
        return zone + page + power + mute + dnd + volume + treble + bass + balance + source + keypad;
    }
}
