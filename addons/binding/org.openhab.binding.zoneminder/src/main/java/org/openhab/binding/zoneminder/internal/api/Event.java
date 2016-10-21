/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal.api;

public class Event extends ZoneMinderApiData {
    private String Id;

    public long getId() {
        return Long.valueOf(this.Id);
    }

    public void setId(String Id) {
        this.Id = Id;
    }

    private String MonitorId;

    public String getMonitorId() {
        return this.MonitorId;
    }

    public void setMonitorId(String MonitorId) {
        this.MonitorId = MonitorId;
    }

    private String Name;

    public String getName() {
        return this.Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    private String Cause;

    public String getCause() {
        return this.Cause;
    }

    public void setCause(String Cause) {
        this.Cause = Cause;
    }

    private String StartTime;

    public String getStartTime() {
        return this.StartTime;
    }

    public void setStartTime(String StartTime) {
        this.StartTime = StartTime;
    }

    private String EndTime;

    public String getEndTime() {
        return this.EndTime;
    }

    public void setEndTime(String EndTime) {
        this.EndTime = EndTime;
    }

    private String Width;

    public String getWidth() {
        return this.Width;
    }

    public void setWidth(String Width) {
        this.Width = Width;
    }

    private String Height;

    public String getHeight() {
        return this.Height;
    }

    public void setHeight(String Height) {
        this.Height = Height;
    }

    private String Length;

    public String getLength() {
        return this.Length;
    }

    public void setLength(String Length) {
        this.Length = Length;
    }

    private String Frames;

    public String getFrames() {
        return this.Frames;
    }

    public void setFrames(String Frames) {
        this.Frames = Frames;
    }

    private String AlarmFrames;

    public String getAlarmFrames() {
        return this.AlarmFrames;
    }

    public void setAlarmFrames(String AlarmFrames) {
        this.AlarmFrames = AlarmFrames;
    }

    private String TotScore;

    public String getTotScore() {
        return this.TotScore;
    }

    public void setTotScore(String TotScore) {
        this.TotScore = TotScore;
    }

    private String AvgScore;

    public String getAvgScore() {
        return this.AvgScore;
    }

    public void setAvgScore(String AvgScore) {
        this.AvgScore = AvgScore;
    }

    private String MaxScore;

    public String getMaxScore() {
        return this.MaxScore;
    }

    public void setMaxScore(String MaxScore) {
        this.MaxScore = MaxScore;
    }

    private String Archived;

    public String getArchived() {
        return this.Archived;
    }

    public void setArchived(String Archived) {
        this.Archived = Archived;
    }

    private String Videoed;

    public String getVideoed() {
        return this.Videoed;
    }

    public void setVideoed(String Videoed) {
        this.Videoed = Videoed;
    }

    private String Uploaded;

    public String getUploaded() {
        return this.Uploaded;
    }

    public void setUploaded(String Uploaded) {
        this.Uploaded = Uploaded;
    }

    private String Emailed;

    public String getEmailed() {
        return this.Emailed;
    }

    public void setEmailed(String Emailed) {
        this.Emailed = Emailed;
    }

    private String Messaged;

    public String getMessaged() {
        return this.Messaged;
    }

    public void setMessaged(String Messaged) {
        this.Messaged = Messaged;
    }

    private String Executed;

    public String getExecuted() {
        return this.Executed;
    }

    public void setExecuted(String Executed) {
        this.Executed = Executed;
    }

    private String Notes;

    public String getNotes() {
        return this.Notes;
    }

    public void setNotes(String Notes) {
        this.Notes = Notes;
    }
}