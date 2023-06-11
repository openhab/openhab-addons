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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Maps the XML response from the Roku HTTP endpoint '/query/media-player' (Current stream playback meta-data)
 *
 * @author Michael Lobstein - Initial contribution
 */

@NonNullByDefault
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "player")
public class Player {
    @XmlElement(name = "plugin")
    private Player.Plugin plugin = new Plugin();

    @XmlElement(name = "format")
    private Player.Format format = new Format();

    @XmlElement(name = "buffering")
    private Player.Buffering buffering = new Buffering();

    @XmlElement(name = "new_stream")
    private Player.NewStream newStream = new NewStream();

    @XmlElement(name = "position")
    private String position = "";

    @XmlElement(name = "duration")
    private String duration = "";

    @XmlElement(name = "is_live")
    private boolean isLive = false;

    @XmlElement(name = "runtime")
    private String runtime = "";

    @XmlElement(name = "stream_segment")
    private Player.StreamSegment streamSegment = new StreamSegment();

    @XmlAttribute(name = "error")
    private Boolean error = false;

    @XmlAttribute(name = "state")
    private String state = "";

    public Player.Plugin getPlugin() {
        return plugin;
    }

    public void setPlugin(Player.Plugin value) {
        this.plugin = value;
    }

    public Player.Format getFormat() {
        return format;
    }

    public void setFormat(Player.Format value) {
        this.format = value;
    }

    public Player.Buffering getBuffering() {
        return buffering;
    }

    public void setBuffering(Player.Buffering value) {
        this.buffering = value;
    }

    public Player.NewStream getNewStream() {
        return newStream;
    }

    public void setNewStream(Player.NewStream value) {
        this.newStream = value;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String value) {
        this.position = value;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String value) {
        this.duration = value;
    }

    public boolean isIsLive() {
        return isLive;
    }

    public void setIsLive(boolean value) {
        this.isLive = value;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String value) {
        this.runtime = value;
    }

    public Player.StreamSegment getStreamSegment() {
        return streamSegment;
    }

    public void setStreamSegment(Player.StreamSegment value) {
        this.streamSegment = value;
    }

    public Boolean isError() {
        return error;
    }

    public void setError(Boolean value) {
        this.error = value;
    }

    public String getState() {
        return state;
    }

    public void setState(String value) {
        this.state = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Buffering {
        @XmlAttribute(name = "current")
        private int current = -1;

        @XmlAttribute(name = "max")
        private int max = -1;

        @XmlAttribute(name = "target")
        private int target = -1;

        public int getCurrent() {
            return current;
        }

        public void setCurrent(int value) {
            this.current = value;
        }

        public int getMax() {
            return max;
        }

        public void setMax(int value) {
            this.max = value;
        }

        public int getTarget() {
            return target;
        }

        public void setTarget(int value) {
            this.target = value;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Format {
        @XmlAttribute(name = "audio")
        private String audio = "";

        @XmlAttribute(name = "captions")
        private String captions = "";

        @XmlAttribute(name = "container")
        private String container = "";

        @XmlAttribute(name = "drm")
        private String drm = "";

        @XmlAttribute(name = "video")
        private String video = "";

        @XmlAttribute(name = "video_res")
        private String videoRes = "";

        public String getAudio() {
            return audio;
        }

        public void setAudio(String value) {
            this.audio = value;
        }

        public String getCaptions() {
            return captions;
        }

        public void setCaptions(String value) {
            this.captions = value;
        }

        public String getContainer() {
            return container;
        }

        public void setContainer(String value) {
            this.container = value;
        }

        public String getDrm() {
            return drm;
        }

        public void setDrm(String value) {
            this.drm = value;
        }

        public String getVideo() {
            return video;
        }

        public void setVideo(String value) {
            this.video = value;
        }

        public String getVideoRes() {
            return videoRes;
        }

        public void setVideoRes(String value) {
            this.videoRes = value;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class NewStream {
        @XmlAttribute(name = "speed")
        private String speed = "";

        public String getSpeed() {
            return speed;
        }

        public void setSpeed(String value) {
            this.speed = value;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Plugin {
        @XmlAttribute(name = "bandwidth")
        private String bandwidth = "";

        @XmlAttribute(name = "id")
        private int id = -1;

        @XmlAttribute(name = "name")
        private String name = "";

        public String getBandwidth() {
            return bandwidth;
        }

        public void setBandwidth(String value) {
            this.bandwidth = value;
        }

        public int getId() {
            return id;
        }

        public void setId(int value) {
            this.id = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String value) {
            this.name = value;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class StreamSegment {
        @XmlAttribute(name = "bitrate")
        private int bitrate = -1;

        @XmlAttribute(name = "height")
        private int height = -1;

        @XmlAttribute(name = "media_sequence")
        private int mediaSequence = -1;

        @XmlAttribute(name = "segment_type")
        private String segmentType = "";

        @XmlAttribute(name = "time")
        private int time = -1;

        @XmlAttribute(name = "width")
        private int width = -1;

        public int getBitrate() {
            return bitrate;
        }

        public void setBitrate(int value) {
            this.bitrate = value;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int value) {
            this.height = value;
        }

        public int getMediaSequence() {
            return mediaSequence;
        }

        public void setMediaSequence(int value) {
            this.mediaSequence = value;
        }

        public String getSegmentType() {
            return segmentType;
        }

        public void setSegmentType(String value) {
            this.segmentType = value;
        }

        public int getTime() {
            return time;
        }

        public void setTime(int value) {
            this.time = value;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int value) {
            this.width = value;
        }
    }
}
