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
package org.openhab.binding.roku.internal.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Maps the XML response from the Roku HTTP endpoint '/query/tv-channels' (List of available TV channels)
 *
 * @author Michael Lobstein - Initial contribution
 */

@NonNullByDefault
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "tv-channels")
public class TvChannels {
    @XmlElement
    private List<TvChannels.Channel> channel = new ArrayList<>();

    public List<TvChannels.Channel> getChannel() {
        return this.channel;
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
    }
}
