/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.sonos.internal;

/**
 * The {@link SonosAlarm} is a datastructure to describe
 * alarms in the Sonos ecosystem
 *
 * @author Karel Goderis - Initial contribution
 */
public class SonosAlarm implements Cloneable {

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (Exception e) {
            return null;
        }
    }

    public int getID() {
        return ID;
    }

    public String getStartTime() {
        // public DateTime getStartTime() {
        return startTime;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getDuration() {
        return duration;
    }

    public String getRecurrence() {
        return recurrence;
    }

    public boolean getEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRoomUUID() {
        return roomUUID;
    }

    public String getProgramURI() {
        return programURI;
    }

    public String getProgramMetaData() {
        return programMetaData;
    }

    public String getPlayMode() {
        return playMode;
    }

    public int getVolume() {
        return volume;
    }

    public boolean getIncludeLinkedZones() {
        return includeLinkedZones;
    }

    private final int ID;
    private String startTime;
    private final String duration;
    private final String recurrence;
    private boolean enabled;
    private final String roomUUID;
    private final String programURI;
    private final String programMetaData;
    private final String playMode;
    private final int volume;
    private final boolean includeLinkedZones;

    public SonosAlarm(int ID, String startTime, String duration, String recurrence, boolean enabled, String roomUUID,
            String programURI, String programMetaData, String playMode, int volume, boolean includeLinkedZones) {
        this.ID = ID;
        this.startTime = startTime;
        this.duration = duration;
        this.recurrence = recurrence;
        this.enabled = enabled;
        this.roomUUID = roomUUID;
        this.programURI = programURI;
        this.programMetaData = programMetaData;
        this.playMode = playMode;
        this.volume = volume;
        this.includeLinkedZones = includeLinkedZones;
    }

    @Override
    public String toString() {
        return "SonosAlarm [ID=" + ID + ", start=" + startTime + ", duration=" + duration + ", enabled=" + enabled
                + ", UUID=" + roomUUID + "]";
    }
}
