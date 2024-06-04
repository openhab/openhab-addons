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
package org.openhab.binding.linktap.protocol.frames;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link SetDeviceConfigReq} sets the configuration parameter specified for a particular device.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class SetDeviceConfigReq extends DeviceCmdReq {

    public SetDeviceConfigReq() {
    }

    public SetDeviceConfigReq(final String tag, final int value) {
        this.command = CMD_SET_CONFIGURATION;
        this.tag = tag;
        this.value = value;
    }

    /**
     * The value to send for the given tag
     */
    @SerializedName("value")
    @Expose
    public int value = 0;

    /**
     * The tag that the value suppied is to be used for
     */
    @SerializedName("tag")
    @Expose
    public String tag = EMPTY_STRING;

    public String isValid() {
        final String superRst = super.isValid();
        if (!superRst.isEmpty()) {
            return superRst;
        }

        switch (tag) {
            case CONFIG_VOLUME_LIMIT:
            case CONFIG_DURATION_LIMIT:
                break;
            default:
                return "Invalid tag \"" + tag + "\"";
        }

        return EMPTY_STRING;
    }

    /**
     * Config - Water Volume Limit
     */
    public static final String CONFIG_VOLUME_LIMIT = "volume_limit";

    /**
     * Config - Time Duration Limit
     */
    public static final String CONFIG_DURATION_LIMIT = "total_duration";
}
