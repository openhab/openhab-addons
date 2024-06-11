/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.denonmarantz.internal.xml.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.openhab.binding.denonmarantz.internal.xml.dto.types.OnOffType;
import org.openhab.binding.denonmarantz.internal.xml.dto.types.StringType;
import org.openhab.binding.denonmarantz.internal.xml.dto.types.VolumeType;

/**
 * Holds information about the secondary zones of the receiver
 *
 * @author Jeroen Idserda - Initial contribution
 */
@XmlRootElement(name = "item")
@XmlAccessorType(XmlAccessType.FIELD)
public class ZoneStatus {

    private OnOffType power;

    @XmlElementWrapper(name = "inputFuncList")
    @XmlElement(name = "value")
    private List<String> inputFunctions;

    private StringType inputFuncSelect;

    private StringType volumeDisplay;

    private StringType surrMode;

    private VolumeType masterVolume;

    private OnOffType mute;

    public OnOffType getPower() {
        return power;
    }

    public void setPower(OnOffType power) {
        this.power = power;
    }

    public StringType getInputFuncSelect() {
        return inputFuncSelect;
    }

    public void setInputFuncSelect(StringType inputFuncSelect) {
        this.inputFuncSelect = inputFuncSelect;
    }

    public StringType getVolumeDisplay() {
        return volumeDisplay;
    }

    public void setVolumeDisplay(StringType volumeDisplay) {
        this.volumeDisplay = volumeDisplay;
    }

    public StringType getSurrMode() {
        return surrMode;
    }

    public void setSurrMode(StringType surrMode) {
        this.surrMode = surrMode;
    }

    public VolumeType getMasterVolume() {
        return masterVolume;
    }

    public void setMasterVolume(VolumeType masterVolume) {
        this.masterVolume = masterVolume;
    }

    public OnOffType getMute() {
        return mute;
    }

    public void setMute(OnOffType mute) {
        this.mute = mute;
    }

    public List<String> getInputFuncList() {
        return this.inputFunctions;
    }
}
