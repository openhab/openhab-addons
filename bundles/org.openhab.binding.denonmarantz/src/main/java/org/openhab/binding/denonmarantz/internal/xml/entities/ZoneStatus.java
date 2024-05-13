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
package org.openhab.binding.denonmarantz.internal.xml.entities;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.denonmarantz.internal.xml.entities.types.OnOffType;
import org.openhab.binding.denonmarantz.internal.xml.entities.types.StringType;
import org.openhab.binding.denonmarantz.internal.xml.entities.types.VolumeType;

/**
 * Holds information about the secondary zones of the receiver
 *
 * @author Jeroen Idserda - Initial contribution
 */
@XmlRootElement(name = "item")
@XmlAccessorType(XmlAccessType.FIELD)
@NonNullByDefault
public class ZoneStatus {

    private @Nullable OnOffType power;

    @XmlElementWrapper(name = "inputFuncList")
    @XmlElement(name = "value")
    private @Nullable List<String> inputFunctions;

    private @Nullable StringType inputFuncSelect;

    private @Nullable StringType volumeDisplay;

    private @Nullable StringType surrMode;

    private @Nullable VolumeType masterVolume;

    private @Nullable OnOffType mute;

    public @Nullable OnOffType getPower() {
        return power;
    }

    public void setPower(OnOffType power) {
        this.power = power;
    }

    public @Nullable StringType getInputFuncSelect() {
        return inputFuncSelect;
    }

    public void setInputFuncSelect(StringType inputFuncSelect) {
        this.inputFuncSelect = inputFuncSelect;
    }

    public @Nullable StringType getVolumeDisplay() {
        return volumeDisplay;
    }

    public void setVolumeDisplay(StringType volumeDisplay) {
        this.volumeDisplay = volumeDisplay;
    }

    public @Nullable StringType getSurrMode() {
        return surrMode;
    }

    public void setSurrMode(StringType surrMode) {
        this.surrMode = surrMode;
    }

    public @Nullable VolumeType getMasterVolume() {
        return masterVolume;
    }

    public void setMasterVolume(VolumeType masterVolume) {
        this.masterVolume = masterVolume;
    }

    public @Nullable OnOffType getMute() {
        return mute;
    }

    public void setMute(OnOffType mute) {
        this.mute = mute;
    }

    public @Nullable List<String> getInputFuncList() {
        return this.inputFunctions;
    }
}
