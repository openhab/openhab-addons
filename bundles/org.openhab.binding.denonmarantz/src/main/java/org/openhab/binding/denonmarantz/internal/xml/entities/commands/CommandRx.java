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
package org.openhab.binding.denonmarantz.internal.xml.entities.commands;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Response to a {@link CommandTx}
 *
 * @author Jeroen Idserda - Initial contribution
 */
@XmlRootElement(name = "cmd")
@XmlAccessorType(XmlAccessType.FIELD)
@NonNullByDefault
public class CommandRx {

    private @Nullable String zone1;

    private @Nullable String zone2;

    private @Nullable String zone3;

    private @Nullable String zone4;

    private @Nullable String volume;

    private @Nullable String disptype;

    private @Nullable String dispvalue;

    private @Nullable String mute;

    private @Nullable String type;

    @XmlElement(name = "text")
    private List<Text> texts = new ArrayList<>();

    @XmlElementWrapper(name = "functionrename")
    @XmlElement(name = "list")
    private @Nullable List<RenameSourceList> renameSourceLists;

    @XmlElementWrapper(name = "functiondelete")
    @XmlElement(name = "list")
    private @Nullable List<DeletedSourceList> deletedSourceLists;

    private @Nullable String playstatus;

    private @Nullable String playcontents;

    private @Nullable String repeat;

    private @Nullable String shuffle;

    private @Nullable String source;

    public CommandRx() {
    }

    public @Nullable String getZone1() {
        return zone1;
    }

    public void setZone1(String zone1) {
        this.zone1 = zone1;
    }

    public @Nullable String getZone2() {
        return zone2;
    }

    public void setZone2(String zone2) {
        this.zone2 = zone2;
    }

    public @Nullable String getZone3() {
        return zone3;
    }

    public void setZone3(String zone3) {
        this.zone3 = zone3;
    }

    public @Nullable String getZone4() {
        return zone4;
    }

    public void setZone4(String zone4) {
        this.zone4 = zone4;
    }

    public @Nullable String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public @Nullable String getDisptype() {
        return disptype;
    }

    public void setDisptype(String disptype) {
        this.disptype = disptype;
    }

    public @Nullable String getDispvalue() {
        return dispvalue;
    }

    public void setDispvalue(String dispvalue) {
        this.dispvalue = dispvalue;
    }

    public @Nullable String getMute() {
        return mute;
    }

    public void setMute(String mute) {
        this.mute = mute;
    }

    public @Nullable String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public @Nullable String getPlaystatus() {
        return playstatus;
    }

    public void setPlaystatus(String playstatus) {
        this.playstatus = playstatus;
    }

    public @Nullable String getPlaycontents() {
        return playcontents;
    }

    public void setPlaycontents(String playcontents) {
        this.playcontents = playcontents;
    }

    public @Nullable String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    public @Nullable String getShuffle() {
        return shuffle;
    }

    public void setShuffle(String shuffle) {
        this.shuffle = shuffle;
    }

    public @Nullable String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public @Nullable String getText(String key) {
        for (Text text : texts) {
            if (key.equals(text.getId())) {
                return text.getValue();
            }
        }
        return null;
    }

    public @Nullable List<RenameSourceList> getRenameSourceLists() {
        return renameSourceLists;
    }

    public @Nullable List<DeletedSourceList> getDeletedSourceLists() {
        return deletedSourceLists;
    }
}
