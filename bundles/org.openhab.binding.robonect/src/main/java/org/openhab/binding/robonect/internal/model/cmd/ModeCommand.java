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
package org.openhab.binding.robonect.internal.model.cmd;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * 
 * The mode commands sets the mower into the corresponding mode. In addition to the mowers standard modes
 * (HOME, MAN, AUTO) the module supports following modes:
 * 
 * EOD (End Of Day): The mower is set into HOME mode until midnight. After midnight the module sets the mower in AUTO
 * mode
 * JOB: The JOB mode triggers a JOB and supports following additional parameter:
 * * remoteStart: where to start the job (STANDARD, DOCKING_STATION or REMOTE_1 through REMOTE_5)
 * * after: The mode to be set after the JOB is done. Allowed are all except JOB.
 * * start: the start time in the form HH:MM (H=Hour,M=Minute)
 * * end: the end time in the form HH:MM (H=Hour,M=Minute)
 * * duration: mowing time in minutes (in combination with start or end time)
 * 
 * @author Marco Meyer - Initial contribution
 * @author Nico Brüttner - Docking station and expanded remote start options
 */
@NonNullByDefault
public class ModeCommand implements Command {

    /**
     * The available modes. See class documentation for the meanings.
     */
    public enum Mode {
        HOME(1, "home"),
        EOD(2, "eod"),
        MANUAL(3, "man"),
        AUTO(4, "auto"),
        JOB(5, "job");

        int code;
        String cmd;

        /**
         * Creates a mower mode with its protocol values.
         *
         * @param code numeric mode code
         * @param cmd mode command value
         */
        Mode(int code, String cmd) {
            this.code = code;
            this.cmd = cmd;
        }
    }

    /**
     * The available remoteStart values.
     */
    public enum RemoteStart {

        /**
         * Start immediatly at the docking station.
         */
        STANDARD(0),

        /** Start at the docking station. */
        DOCKING_STATION(1),

        /** Start at the configured remote 1 location. */
        REMOTE_1(2),

        /** Start at the configured remote 2 location. */
        REMOTE_2(3),

        /** Start at the configured remote 3 location. */
        REMOTE_3(4),

        /** Start at the configured remote 4 location. */
        REMOTE_4(5),

        /** Start at the configured remote 5 location. */
        REMOTE_5(6);

        int code;

        /**
         * Creates a remote-start option with its protocol code.
         *
         * @param code numeric remote-start code
         */
        RemoteStart(int code) {
            this.code = code;
        }
    }

    private Mode mode;

    private @Nullable RemoteStart remoteStart;

    private @Nullable Mode after;

    private @Nullable String start;

    private @Nullable String end;

    private @Nullable Integer duration;

    /**
     * Creates a mode command.
     *
     * @param mode the requested mower mode
     */
    public ModeCommand(Mode mode) {
        this.mode = mode;
    }

    /**
     * Sets the desired remote-start option.
     * 
     * @param remoteStart the desired remote-start option
     * @return - the command instance.
     */
    public ModeCommand withRemoteStart(@Nullable RemoteStart remoteStart) {
        this.remoteStart = remoteStart;
        return this;
    }

    /**
     * Sets the mode after the job finishes.
     * 
     * @param afterMode - the desired mode after job execution.
     * @return - the command instance.
     */
    public ModeCommand withAfter(@Nullable Mode afterMode) {
        this.after = afterMode;
        return this;
    }

    /**
     * Sets the desired start time in {@code HH:mm} format.
     * 
     * @param startTime - the start time.
     * @return - the command instance.
     */
    public ModeCommand withStart(@Nullable String startTime) {
        this.start = startTime;
        return this;
    }

    /**
     * Sets the desired end time in {@code HH:mm} format.
     * 
     * @param endTime - the end time.
     * @return - the command instance.
     */
    public ModeCommand withEnd(@Nullable String endTime) {
        this.end = endTime;
        return this;
    }

    /**
     * Sets the duration in minutes.
     * 
     * @param durationInMinutes - the duration in minutes.
     * @return - the command instance.
     */
    public ModeCommand withDuration(@Nullable Integer durationInMinutes) {
        this.duration = durationInMinutes;
        return this;
    }

    /**
     * Builds the mower mode URL for this {@link Command}.
     *
     * @param baseURL base URL to which the command parameters are appended
     * @return the complete request URL
     */
    @Override
    public String toCommandURL(String baseURL) {
        RemoteStart rs = remoteStart;
        Mode af = after;
        String st = start;
        String en = end;
        Integer dur = duration;

        StringBuilder sb = new StringBuilder(baseURL);
        sb.append("?cmd=mode&mode=");
        sb.append(mode.cmd);
        switch (mode) {
            case EOD:
            case MANUAL:
            case AUTO:
            case HOME:
                break;
            case JOB:
                if (rs != null) {
                    sb.append("&remotestart=");
                    sb.append(rs.code);
                }
                if (af != null) {
                    sb.append("&after=");
                    sb.append(af.code);
                }
                if (st != null) {
                    sb.append("&start=");
                    sb.append(st);
                }
                if (en != null) {
                    sb.append("&end=");
                    sb.append(en);
                }
                if (dur != null) {
                    sb.append("&duration=");
                    sb.append(dur);
                }
                break;
        }
        return sb.toString();
    }
}
