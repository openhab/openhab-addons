/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.roku.internal.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Maps the XML response from the Roku HTTP endpoint '/query/tv-active-channel' (Active TV channel information)
 *
 * @author Michael Lobstein - Initial contribution
 */

@NonNullByDefault
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "tv-channel")
public class TvChannel {
    @XmlElement
    private TvChannel.Channel channel = new Channel();

    public TvChannel.Channel getChannel() {
        return this.channel;
    }

    public void setChannel(TvChannel.Channel value) {
        this.channel = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Channel {
        @XmlElement(name = "number")
        private String number = "";

        @XmlElement(name = "name")
        private String name = "";

        @XmlElement(name = "type")
        private String type = "";

        @XmlElement(name = "user-hidden")
        private boolean userHidden = false;

        @XmlElement(name = "user-favorite")
        private boolean userFavorite = false;

        @XmlElement(name = "physical-channel")
        private int physicalChannel = 0;

        @XmlElement(name = "physical-frequency")
        private int physicalFrequency = 0;

        @XmlElement(name = "active-input")
        private boolean activeInput = false;

        @XmlElement(name = "signal-state")
        private String signalState = "";

        @XmlElement(name = "signal-mode")
        private String signalMode = "";

        @XmlElement(name = "signal-quality")
        private int signalQuality = 0;

        @XmlElement(name = "signal-strength")
        private int signalStrength = 0;

        @XmlElement(name = "signal-stalled-pts-cnt")
        private int signalStalledPtsCnt = 0;

        @XmlElement(name = "program-title")
        private String programTitle = "";

        @XmlElement(name = "program-description")
        private String programDescription = "";

        @XmlElement(name = "program-ratings")
        private String programRatings = "";

        @XmlElement(name = "program-is-blocked")
        private boolean programIsBlocked = false;

        @XmlElement(name = "program-analog-audio")
        private String programAnalogAudio = "";

        @XmlElement(name = "program-digital-audio")
        private String programDigitalAudio = "";

        @XmlElement(name = "program-audio-languages")
        private String programAudioLanguages = "";

        @XmlElement(name = "program-audio-formats")
        private String programAudioFormats = "";

        @XmlElement(name = "program-audio-language")
        private String programAudioLanguage = "";

        @XmlElement(name = "program-audio-format")
        private String programAudioFormat = "";

        @XmlElement(name = "program-has-cc")
        private boolean programHasCc = false;

        public String getNumber() {
            return number;
        }

        public void setNumber(String value) {
            this.number = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String value) {
            this.name = value;
        }

        public String getType() {
            return type;
        }

        public void setType(String value) {
            this.type = value;
        }

        public boolean isUserHidden() {
            return userHidden;
        }

        public void setUserHidden(boolean value) {
            this.userHidden = value;
        }

        public boolean isUserFavorite() {
            return userFavorite;
        }

        public void setUserFavorite(boolean value) {
            this.userFavorite = value;
        }

        public int getPhysicalChannel() {
            return physicalChannel;
        }

        public void setPhysicalChannel(int value) {
            this.physicalChannel = value;
        }

        public int getPhysicalFrequency() {
            return physicalFrequency;
        }

        public void setPhysicalFrequency(int value) {
            this.physicalFrequency = value;
        }

        public boolean isActiveInput() {
            return activeInput;
        }

        public void setActiveInput(boolean value) {
            this.activeInput = value;
        }

        public String getSignalState() {
            return signalState;
        }

        public void setSignalState(String value) {
            this.signalState = value;
        }

        public String getSignalMode() {
            return signalMode;
        }

        public void setSignalMode(String value) {
            this.signalMode = value;
        }

        public int getSignalQuality() {
            return signalQuality;
        }

        public void setSignalQuality(int value) {
            this.signalQuality = value;
        }

        public int getSignalStrength() {
            return signalStrength;
        }

        public void setSignalStrength(int value) {
            this.signalStrength = value;
        }

        public int getSignalStalledPtsCnt() {
            return signalStalledPtsCnt;
        }

        public void setSignalStalledPtsCnt(int value) {
            this.signalStalledPtsCnt = value;
        }

        public String getProgramTitle() {
            return programTitle;
        }

        public void setProgramTitle(String value) {
            this.programTitle = value;
        }

        public String getProgramDescription() {
            return programDescription;
        }

        public void setProgramDescription(String value) {
            this.programDescription = value;
        }

        public String getProgramRatings() {
            return programRatings;
        }

        public void setProgramRatings(String value) {
            this.programRatings = value;
        }

        public boolean isProgramIsBlocked() {
            return programIsBlocked;
        }

        public void setProgramIsBlocked(boolean value) {
            this.programIsBlocked = value;
        }

        public String getProgramAnalogAudio() {
            return programAnalogAudio;
        }

        public void setProgramAnalogAudio(String value) {
            this.programAnalogAudio = value;
        }

        public String getProgramDigitalAudio() {
            return programDigitalAudio;
        }

        public void setProgramDigitalAudio(String value) {
            this.programDigitalAudio = value;
        }

        public String getProgramAudioLanguages() {
            return programAudioLanguages;
        }

        public void setProgramAudioLanguages(String value) {
            this.programAudioLanguages = value;
        }

        public String getProgramAudioFormats() {
            return programAudioFormats;
        }

        public void setProgramAudioFormats(String value) {
            this.programAudioFormats = value;
        }

        public String getProgramAudioLanguage() {
            return programAudioLanguage;
        }

        public void setProgramAudioLanguage(String value) {
            this.programAudioLanguage = value;
        }

        public String getProgramAudioFormat() {
            return programAudioFormat;
        }

        public void setProgramAudioFormat(String value) {
            this.programAudioFormat = value;
        }

        public boolean isProgramHasCc() {
            return programHasCc;
        }

        public void setProgramHasCc(boolean value) {
            this.programHasCc = value;
        }
    }
}
