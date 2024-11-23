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
package org.openhab.binding.elerotransmitterstick.internal.stick;

import java.util.Arrays;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author Volker Bier - Initial contribution
 */
public class Command implements Delayed {
    public static final int TIMED_PRIORITY = 30;
    public static final int COMMAND_PRIORITY = 20;
    public static final int FAST_INFO_PRIORITY = 10;
    public static final int INFO_PRIORITY = 0;

    private Integer[] channelId;
    private CommandType commandType;

    protected int priority = COMMAND_PRIORITY;

    public Command(final CommandType cmd, final Integer... channels) {
        channelId = channels;
        commandType = cmd;
    }

    protected Command(final CommandType cmd, int priority, final Integer... channels) {
        this(cmd, channels);

        this.priority = priority;
    }

    @Override
    public String toString() {
        return "Command " + commandType + " on channels " + Arrays.toString(channelId) + " with priority " + priority;
    }

    @Override
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public int compareTo(Delayed delayed) {
        if (delayed == this) {
            return 0;
        }

        return Long.compare(0, delayed.getDelay(TimeUnit.MILLISECONDS));
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return 0;
    }

    public int getPriority() {
        return priority;
    }

    public Integer[] getChannelIds() {
        return channelId;
    }

    public CommandType getCommandType() {
        return commandType;
    }
}
