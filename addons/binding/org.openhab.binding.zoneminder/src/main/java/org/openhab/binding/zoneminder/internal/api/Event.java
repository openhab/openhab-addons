/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal.api;

/**
 * The Class Event Wraps JSON data from ZoneMinder API call.
 *
 * @author Martin S. Eskildsen
 */
public class Event extends ZoneMinderApiData {

    /** The Id. */
    private String Id;

    /**
     * Gets the id.
     *
     * @return the id
     */
    public long getId() {
        return Long.valueOf(this.Id);
    }

    /**
     * Sets the id.
     *
     * @param Id the new id
     */
    public void setId(String Id) {
        this.Id = Id;
    }

    /** The Monitor id. */
    private String MonitorId;

    /**
     * Gets the monitor id.
     *
     * @return the monitor id
     */
    public String getMonitorId() {
        return this.MonitorId;
    }

    /**
     * Sets the monitor id.
     *
     * @param MonitorId the new monitor id
     */
    public void setMonitorId(String MonitorId) {
        this.MonitorId = MonitorId;
    }

    /** The Name. */
    private String Name;

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return this.Name;
    }

    /**
     * Sets the name.
     *
     * @param Name the new name
     */
    public void setName(String Name) {
        this.Name = Name;
    }

    /** The Cause. */
    private String Cause;

    /**
     * Gets the cause.
     *
     * @return the cause
     */
    public String getCause() {
        return this.Cause;
    }

    /**
     * Sets the cause.
     *
     * @param Cause the new cause
     */
    public void setCause(String Cause) {
        this.Cause = Cause;
    }

    /** The Start time. */
    private String StartTime;

    /**
     * Gets the start time.
     *
     * @return the start time
     */
    public String getStartTime() {
        return this.StartTime;
    }

    /**
     * Sets the start time.
     *
     * @param StartTime the new start time
     */
    public void setStartTime(String StartTime) {
        this.StartTime = StartTime;
    }

    /** The End time. */
    private String EndTime;

    /**
     * Gets the end time.
     *
     * @return the end time
     */
    public String getEndTime() {
        return this.EndTime;
    }

    /**
     * Sets the end time.
     *
     * @param EndTime the new end time
     */
    public void setEndTime(String EndTime) {
        this.EndTime = EndTime;
    }

    /** The Width. */
    private String Width;

    /**
     * Gets the width.
     *
     * @return the width
     */
    public String getWidth() {
        return this.Width;
    }

    /**
     * Sets the width.
     *
     * @param Width the new width
     */
    public void setWidth(String Width) {
        this.Width = Width;
    }

    /** The Height. */
    private String Height;

    /**
     * Gets the height.
     *
     * @return the height
     */
    public String getHeight() {
        return this.Height;
    }

    /**
     * Sets the height.
     *
     * @param Height the new height
     */
    public void setHeight(String Height) {
        this.Height = Height;
    }

    /** The Length. */
    private String Length;

    /**
     * Gets the length.
     *
     * @return the length
     */
    public String getLength() {
        return this.Length;
    }

    /**
     * Sets the length.
     *
     * @param Length the new length
     */
    public void setLength(String Length) {
        this.Length = Length;
    }

    /** The Frames. */
    private String Frames;

    /**
     * Gets the frames.
     *
     * @return the frames
     */
    public String getFrames() {
        return this.Frames;
    }

    /**
     * Sets the frames.
     *
     * @param Frames the new frames
     */
    public void setFrames(String Frames) {
        this.Frames = Frames;
    }

    /** The Alarm frames. */
    private String AlarmFrames;

    /**
     * Gets the alarm frames.
     *
     * @return the alarm frames
     */
    public String getAlarmFrames() {
        return this.AlarmFrames;
    }

    /**
     * Sets the alarm frames.
     *
     * @param AlarmFrames the new alarm frames
     */
    public void setAlarmFrames(String AlarmFrames) {
        this.AlarmFrames = AlarmFrames;
    }

    /** The Tot score. */
    private String TotScore;

    /**
     * Gets the tot score.
     *
     * @return the tot score
     */
    public String getTotScore() {
        return this.TotScore;
    }

    /**
     * Sets the tot score.
     *
     * @param TotScore the new tot score
     */
    public void setTotScore(String TotScore) {
        this.TotScore = TotScore;
    }

    /** The Avg score. */
    private String AvgScore;

    /**
     * Gets the avg score.
     *
     * @return the avg score
     */
    public String getAvgScore() {
        return this.AvgScore;
    }

    /**
     * Sets the avg score.
     *
     * @param AvgScore the new avg score
     */
    public void setAvgScore(String AvgScore) {
        this.AvgScore = AvgScore;
    }

    /** The Max score. */
    private String MaxScore;

    /**
     * Gets the max score.
     *
     * @return the max score
     */
    public String getMaxScore() {
        return this.MaxScore;
    }

    /**
     * Sets the max score.
     *
     * @param MaxScore the new max score
     */
    public void setMaxScore(String MaxScore) {
        this.MaxScore = MaxScore;
    }

    /** The Archived. */
    private String Archived;

    /**
     * Gets the archived.
     *
     * @return the archived
     */
    public String getArchived() {
        return this.Archived;
    }

    /**
     * Sets the archived.
     *
     * @param Archived the new archived
     */
    public void setArchived(String Archived) {
        this.Archived = Archived;
    }

    /** The Videoed. */
    private String Videoed;

    /**
     * Gets the videoed.
     *
     * @return the videoed
     */
    public String getVideoed() {
        return this.Videoed;
    }

    /**
     * Sets the videoed.
     *
     * @param Videoed the new videoed
     */
    public void setVideoed(String Videoed) {
        this.Videoed = Videoed;
    }

    /** The Uploaded. */
    private String Uploaded;

    /**
     * Gets the uploaded.
     *
     * @return the uploaded
     */
    public String getUploaded() {
        return this.Uploaded;
    }

    /**
     * Sets the uploaded.
     *
     * @param Uploaded the new uploaded
     */
    public void setUploaded(String Uploaded) {
        this.Uploaded = Uploaded;
    }

    /** The Emailed. */
    private String Emailed;

    /**
     * Gets the emailed.
     *
     * @return the emailed
     */
    public String getEmailed() {
        return this.Emailed;
    }

    /**
     * Sets the emailed.
     *
     * @param Emailed the new emailed
     */
    public void setEmailed(String Emailed) {
        this.Emailed = Emailed;
    }

    /** The Messaged. */
    private String Messaged;

    /**
     * Gets the messaged.
     *
     * @return the messaged
     */
    public String getMessaged() {
        return this.Messaged;
    }

    /**
     * Sets the messaged.
     *
     * @param Messaged the new messaged
     */
    public void setMessaged(String Messaged) {
        this.Messaged = Messaged;
    }

    /** The Executed. */
    private String Executed;

    /**
     * Gets the executed.
     *
     * @return the executed
     */
    public String getExecuted() {
        return this.Executed;
    }

    /**
     * Sets the executed.
     *
     * @param Executed the new executed
     */
    public void setExecuted(String Executed) {
        this.Executed = Executed;
    }

    /** The Notes. */
    private String Notes;

    /**
     * Gets the notes.
     *
     * @return the notes
     */
    public String getNotes() {
        return this.Notes;
    }

    /**
     * Sets the notes.
     *
     * @param Notes the new notes
     */
    public void setNotes(String Notes) {
        this.Notes = Notes;
    }
}