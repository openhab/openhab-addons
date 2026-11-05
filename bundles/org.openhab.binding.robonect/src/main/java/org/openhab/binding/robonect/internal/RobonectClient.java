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
package org.openhab.binding.robonect.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.robonect.internal.model.cmd.ModeCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coordinates endpoint APIs sharing an HTTP transport for one Robonect module.
 *
 * @see <a href="http://robonect.de/viewtopic.php?f=10&amp;t=37">
 *      http://robonect.de/viewtopic.php?f=10&amp;t=37</a>. The API of the module is documented here.
 *
 * @author Marco Meyer - Initial contribution
 * @author Nico Brüttner - API and web interface support
 */
@NonNullByDefault
public class RobonectClient {

    private final RobonectMowerApi mower;
    private final RobonectDiagnosticsApi diagnostics;
    private final RobonectPushApi push;
    private final RobonectRemoteStartApi remoteStart;
    private final RobonectTimerApi timer;
    private final RobonectWeatherApi weather;

    /**
     * The {@link JobSettings} class holds the values required for starting a job.
     */
    public static class JobSettings {

        private final Logger logger = LoggerFactory.getLogger(RobonectClient.class);

        private ModeCommand.@Nullable RemoteStart remoteStart;
        private ModeCommand.@Nullable Mode after;
        private int duration;
        private @Nullable Integer start;
        private @Nullable Integer end;

        /**
         * Returns the remote-start setting, defaulting to {@link ModeCommand.RemoteStart#STANDARD}.
         *
         * @return - the remote start settings for the job.
         */
        public ModeCommand.RemoteStart getRemoteStart() {
            ModeCommand.@Nullable RemoteStart configuredRemoteStart = remoteStart;
            if (configuredRemoteStart != null) {
                return configuredRemoteStart;
            } else {
                logger.debug("No explicit remote start set. Return STANDARD.");
                return ModeCommand.RemoteStart.STANDARD;
            }
        }

        /**
         * Sets the desired 'remote start' settings for the job.
         *
         * @param remoteStart the remote-start setting, or {@code null} to use the default
         * @return this job settings instance
         */
        public JobSettings withRemoteStart(ModeCommand.@Nullable RemoteStart remoteStart) {
            this.remoteStart = remoteStart;
            return this;
        }

        /**
         * Returns the mode the mower should be set to after the job is complete.
         *
         * @return the mode to select after the job, or {@code null} if unset
         */
        public ModeCommand.@Nullable Mode getAfterMode() {
            return after;
        }

        /**
         * Sets the mode after the mower is complete with the job.
         *
         * @param after the mode to select after the job, or {@code null} to omit it
         * @return this job settings instance
         */
        public JobSettings withAfterMode(ModeCommand.@Nullable Mode after) {
            this.after = after;
            return this;
        }

        /**
         * Returns the requested job duration.
         *
         * @return the duration in minutes
         */
        public int getDuration() {
            return duration;
        }

        /**
         * Sets the requested job duration.
         *
         * @param duration the duration in minutes
         * @return this job settings instance
         */
        public JobSettings withDuration(int duration) {
            this.duration = duration;
            return this;
        }

        /**
         * Sets the job start time as minutes after midnight.
         *
         * @param start the start time, or {@code null} to omit it from the command
         * @return this job settings instance
         */
        public JobSettings withStart(@Nullable Integer start) {
            this.start = start;
            return this;
        }

        /**
         * Sets the job end time as minutes after midnight.
         *
         * @param end the end time, or {@code null} to omit it from the command
         * @return this job settings instance
         */
        public JobSettings withEnd(@Nullable Integer end) {
            this.end = end;
            return this;
        }

        /**
         * Formats the optional job start time.
         *
         * @return the time in {@code HH:mm} format, or {@code null} if unset
         */
        @Nullable
        String getStart() {
            return formatTime(start);
        }

        /**
         * Formats the optional job end time.
         *
         * @return the time in {@code HH:mm} format, or {@code null} if unset
         */
        @Nullable
        String getEnd() {
            return formatTime(end);
        }

        /**
         * Formats minutes after midnight as a time of day.
         *
         * @param minutesAfterMidnight the minute of the day, or {@code null} to omit the time
         * @return the time in {@code HH:mm} format, or {@code null} if unset
         * @throws IllegalArgumentException if the minute is outside the day
         */
        private @Nullable String formatTime(@Nullable Integer minutesAfterMidnight) {
            if (minutesAfterMidnight == null) {
                return null;
            }
            if (minutesAfterMidnight < 0 || minutesAfterMidnight >= 24 * 60) {
                throw new IllegalArgumentException("Time must be between 0 and 1439 minutes after midnight");
            }
            return "%02d:%02d".formatted(minutesAfterMidnight / 60, minutesAfterMidnight % 60);
        }
    }

    /**
     * Creates the endpoint APIs using a shared HTTP transport.
     *
     * @param httpClient the HTTP client to use for communication
     * @param endpoint the endpoint details for connecting and issuing commands
     */
    public RobonectClient(HttpClient httpClient, RobonectEndpoint endpoint) {
        RobonectTransport transport = new RobonectTransport(httpClient, endpoint);
        mower = new RobonectMowerApi(transport);
        diagnostics = new RobonectDiagnosticsApi(transport);
        push = new RobonectPushApi(transport);
        remoteStart = new RobonectRemoteStartApi(transport);
        timer = new RobonectTimerApi(transport);
        weather = new RobonectWeatherApi(transport);
    }

    /**
     * Returns the mower and job endpoint API.
     *
     * @return the mower and job API
     */
    public RobonectMowerApi mower() {
        return mower;
    }

    /**
     * Returns the diagnostic endpoint API.
     *
     * @return the diagnostic API
     */
    public RobonectDiagnosticsApi diagnostics() {
        return diagnostics;
    }

    /**
     * Returns the push and extension endpoint API.
     *
     * @return the push and extension API
     */
    public RobonectPushApi push() {
        return push;
    }

    /**
     * Returns the remote-start endpoint API.
     *
     * @return the remote-start API
     */
    public RobonectRemoteStartApi remoteStart() {
        return remoteStart;
    }

    /**
     * Returns the timer endpoint API.
     *
     * @return the timer API
     */
    public RobonectTimerApi timer() {
        return timer;
    }

    /**
     * Returns the weather endpoint API.
     *
     * @return the weather API
     */
    public RobonectWeatherApi weather() {
        return weather;
    }
}
