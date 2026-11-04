/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal.config;

/**
 * The {@link JobChannelConfig} class holds the channel configuration for the Job channel.
 *
 * @author Marco Meyer - Initial contribution
 */
public class JobChannelConfig {

    private String remoteStart;

    private String afterMode;

    private int duration;

    private Integer start;

    private Integer end;

    /**
     * Returns the job remote-start mode.
     *
     * @return the selected remote-start mode
     */
    public String getRemoteStart() {
        return remoteStart;
    }

    /**
     * Sets the job remote-start mode.
     *
     * @param remoteStart the remote-start mode
     */
    public void setRemoteStart(String remoteStart) {
        this.remoteStart = remoteStart;
    }

    /**
     * Returns the mode selected after a job.
     *
     * @return the configured after-job mode
     */
    public String getAfterMode() {
        return afterMode;
    }

    /**
     * Sets the mode selected after a job.
     *
     * @param afterMode the after-job mode
     */
    public void setAfterMode(String afterMode) {
        this.afterMode = afterMode;
    }

    /**
     * Returns the job duration.
     *
     * @return the duration in minutes
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Sets the job duration.
     *
     * @param duration the duration in minutes
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * Returns the optional job start time.
     *
     * @return the start time in minutes after midnight, or {@code null} if unset
     */
    public Integer getStart() {
        return start;
    }

    /**
     * Sets the optional job start time.
     *
     * @param start the start time in minutes after midnight, or {@code null} if unset
     */
    public void setStart(Integer start) {
        this.start = start;
    }

    /**
     * Returns the optional job end time.
     *
     * @return the end time in minutes after midnight, or {@code null} if unset
     */
    public Integer getEnd() {
        return end;
    }

    /**
     * Sets the optional job end time.
     *
     * @param end the end time in minutes after midnight, or {@code null} if unset
     */
    public void setEnd(Integer end) {
        this.end = end;
    }
}
