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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link JobChannelConfig} class holds the channel configuration for the Job channel.
 *
 * @author Marco Meyer - Initial contribution
 * @author Nico Brüttner - Optional job timing configuration
 */
@NonNullByDefault
public class JobChannelConfig {

    private @Nullable String remoteStart;

    private @Nullable String afterMode;

    private @Nullable Integer durationMinutes;

    private @Nullable String startTime;

    private @Nullable String endTime;

    /**
     * Returns the job remote-start mode.
     *
     * @return the selected remote-start mode
     */
    public @Nullable String getRemoteStart() {
        return remoteStart;
    }

    /**
     * Sets the job remote-start mode.
     *
     * @param remoteStart the remote-start mode
     */
    public void setRemoteStart(@Nullable String remoteStart) {
        this.remoteStart = remoteStart;
    }

    /**
     * Returns the mode selected after a job.
     *
     * @return the configured after-job mode
     */
    public @Nullable String getAfterMode() {
        return afterMode;
    }

    /**
     * Sets the mode selected after a job.
     *
     * @param afterMode the after-job mode
     */
    public void setAfterMode(@Nullable String afterMode) {
        this.afterMode = afterMode;
    }

    /**
     * Returns the job duration.
     *
     * @return the duration in minutes, or {@code null} if unset
     */
    public @Nullable Integer getDurationMinutes() {
        return durationMinutes;
    }

    /**
     * Sets the job duration.
     *
     * @param durationMinutes the duration in minutes
     */
    public void setDurationMinutes(@Nullable Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    /**
     * Returns the optional job start time.
     *
     * @return the start time in {@code HH:mm} format, or {@code null} if unset
     */
    public @Nullable String getStartTime() {
        return startTime;
    }

    /**
     * Sets the optional job start time.
     *
     * @param startTime the start time in {@code HH:mm} format, or {@code null} if unset
     */
    public void setStartTime(@Nullable String startTime) {
        this.startTime = startTime;
    }

    /**
     * Returns the optional job end time.
     *
     * @return the end time in {@code HH:mm} format, or {@code null} if unset
     */
    public @Nullable String getEndTime() {
        return endTime;
    }

    /**
     * Sets the optional job end time.
     *
     * @param endTime the end time in {@code HH:mm} format, or {@code null} if unset
     */
    public void setEndTime(@Nullable String endTime) {
        this.endTime = endTime;
    }
}
