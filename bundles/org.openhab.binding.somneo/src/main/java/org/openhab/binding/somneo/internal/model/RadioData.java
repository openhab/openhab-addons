/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.somneo.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the radio state from the API.
 *
 * @author Michael Myrcik - Initial contribution
 */
@NonNullByDefault
public class RadioData {

    private static final String LABEL_TEMPLATE = "%s fm";

    private static final String CMD_SEEK_UP = "seekup";

    private static final String CMD_SEEK_DOWN = "seekdown";

    @SerializedName("fmfrq")
    private @Nullable String frequency;

    @SerializedName("fmcmd")
    private @Nullable String command;

    public State getFrequency() {
        final String frequency = this.frequency;
        if (frequency == null) {
            return UnDefType.NULL;
        }
        return new StringType(String.format(LABEL_TEMPLATE, frequency));
    }

    public void setCmdSeekUp() {
        this.command = CMD_SEEK_UP;
    }

    public void setCmdSeekDown() {
        this.command = CMD_SEEK_DOWN;
    }

    public boolean isSeeking() {
        return CMD_SEEK_UP.equals(command) || CMD_SEEK_DOWN.equals(command);
    }
}
