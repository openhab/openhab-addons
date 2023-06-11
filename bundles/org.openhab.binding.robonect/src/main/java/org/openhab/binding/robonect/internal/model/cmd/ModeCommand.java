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
package org.openhab.binding.robonect.internal.model.cmd;

import org.openhab.binding.robonect.internal.RobonectClient;

/**
 * 
 * The mode commands sets the mower into the corresponding mode. In addition to the mowers standard modes
 * (HOME, MAN, AUTO) the module supports following modes:
 * 
 * EOD (End Of Day): The mower is set into HOME mode until midnight. After midnight the module sets the mower in AUTO
 * mode
 * JOB: The JOB mode triggers a JOB and supports following additional parameter:
 * * remoteStart: where to start the job (STANDARD, REMOTE_1 or REMOTE_2)
 * * after: The mode to be set after the JOB is done. Allowed are all except JOB.
 * * start: the start time in the form HH:MM (H=Hour,M=Minute)
 * * end: the end time in the form HH:MM (H=Hour,M=Minute)
 * * duration: mowing time in minutes (in combination with start or end time)
 * 
 * @author Marco Meyer - Initial contribution
 */
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

        /**
         * Start at the configured remote 1 location.
         */
        REMOTE_1(1),

        /**
         * Start at the conifugred remote 2 location.
         */
        REMOTE_2(2);

        int code;

        RemoteStart(int code) {
            this.code = code;
        }
    }

    private Mode mode;

    private RemoteStart remoteStart;

    private Mode after;

    private String start;

    private String end;

    private Integer duration;

    public ModeCommand(Mode mode) {
        this.mode = mode;
    }

    /**
     * sets the desired remoteStart option.
     * 
     * @param remoteStart - the remoteStart option.
     * @return - the command instance.
     */
    public ModeCommand withRemoteStart(RemoteStart remoteStart) {
        this.remoteStart = remoteStart;
        return this;
    }

    /**
     * set the mode after the job is done.
     * 
     * @param afterMode - the desired mode after job execution.
     * @return - the command instance.
     */
    public ModeCommand withAfter(Mode afterMode) {
        this.after = afterMode;
        return this;
    }

    /**
     * The desired start time in the format HH:MM (H=Hour, M=Minute)
     * 
     * @param startTime - the start time.
     * @return - the command instance.
     */
    public ModeCommand withStart(String startTime) {
        this.start = startTime;
        return this;
    }

    /**
     * The desired end time in the format HH:MM (H=Hour, M=Minute)
     * 
     * @param endTime - the end time.
     * @return - the command instance.
     */
    public ModeCommand withEnd(String endTime) {
        this.end = endTime;
        return this;
    }

    /**
     * Sets the duration in minutes.
     * 
     * @param durationInMinutes - the duration in minutes.
     * @return - the command instance.
     */
    public ModeCommand withDuration(Integer durationInMinutes) {
        this.duration = durationInMinutes;
        return this;
    }

    /**
     * {@inheritDoc}
     * 
     * @param baseURL - will be passed by the {@link RobonectClient} in the form
     *            http://xxx.xxx.xxx/json?
     * @return
     */
    @Override
    public String toCommandURL(String baseURL) {
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
                if (remoteStart != null) {
                    sb.append("&remotestart=");
                    sb.append(remoteStart.code);
                }
                if (after != null) {
                    sb.append("&after=");
                    sb.append(after.code);
                }
                if (start != null) {
                    sb.append("&start=");
                    sb.append(start);
                }
                if (end != null) {
                    sb.append("&end=");
                    sb.append(end);
                }
                if (duration != null) {
                    sb.append("&duration=");
                    sb.append(duration);
                }
                break;
        }
        return sb.toString();
    }
}
