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
package org.openhab.binding.denonmarantz.internal.xml.dto.commands;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.denonmarantz.internal.xml.adapters.OnOffAdapter;
import org.openhab.binding.denonmarantz.internal.xml.adapters.VolumeAdapter;

/**
 * Response to a {@link CommandTx}
 *
 * @author Jeroen Idserda - Initial contribution
 */
@XmlRootElement(name = "cmd")
@XmlAccessorType(XmlAccessType.FIELD)
public class CommandRx {

    @XmlJavaTypeAdapter(OnOffAdapter.class)
    private Boolean zone1;

    @XmlJavaTypeAdapter(OnOffAdapter.class)
    private Boolean zone2;

    @XmlJavaTypeAdapter(OnOffAdapter.class)
    private Boolean zone3;

    @XmlJavaTypeAdapter(OnOffAdapter.class)
    private Boolean zone4;

    @XmlJavaTypeAdapter(value = VolumeAdapter.class)
    private BigDecimal volume;

    private String disptype;

    private String dispvalue;

    @XmlJavaTypeAdapter(OnOffAdapter.class)
    private Boolean mute;

    private String type;

    @XmlElement(name = "text")
    private List<Text> texts = new ArrayList<>();

    @XmlElementWrapper(name = "functionrename")
    @XmlElement(name = "list")
    private List<RenameSourceList> renameSourceLists;

    @XmlElementWrapper(name = "functiondelete")
    @XmlElement(name = "list")
    private List<DeletedSourceList> deletedSourceLists;

    private String playstatus;

    private String playcontents;

    private String repeat;

    private String shuffle;

    private String source;

    private String surround;

    public CommandRx() {
    }

    public Boolean getZone1() {
        return zone1;
    }

    public void setZone1(Boolean zone1) {
        this.zone1 = zone1;
    }

    public Boolean getZone2() {
        return zone2;
    }

    public void setZone2(Boolean zone2) {
        this.zone2 = zone2;
    }

    public Boolean getZone3() {
        return zone3;
    }

    public void setZone3(Boolean zone3) {
        this.zone3 = zone3;
    }

    public Boolean getZone4() {
        return zone4;
    }

    public void setZone4(Boolean zone4) {
        this.zone4 = zone4;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    public String getDisptype() {
        return disptype;
    }

    public void setDisptype(String disptype) {
        this.disptype = disptype;
    }

    public String getDispvalue() {
        return dispvalue;
    }

    public void setDispvalue(String dispvalue) {
        this.dispvalue = dispvalue;
    }

    public Boolean getMute() {
        return mute;
    }

    public void setMute(Boolean mute) {
        this.mute = mute;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPlaystatus() {
        return playstatus;
    }

    public void setPlaystatus(String playstatus) {
        this.playstatus = playstatus;
    }

    public String getPlaycontents() {
        return playcontents;
    }

    public void setPlaycontents(String playcontents) {
        this.playcontents = playcontents;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    public String getShuffle() {
        return shuffle;
    }

    public void setShuffle(String shuffle) {
        this.shuffle = shuffle;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSurround() {
        return surround;
    }

    public void setSurround(String surround) {
        this.surround = surround;
    }

    public @Nullable String getText(@NonNull String key) {
        for (Text text : texts) {
            if (key.equals(text.getId())) {
                return text.getValue();
            }
        }
        return null;
    }

    public List<RenameSourceList> getRenameSourceLists() {
        return renameSourceLists;
    }

    public List<DeletedSourceList> getDeletedSourceLists() {
        return deletedSourceLists;
    }
}
