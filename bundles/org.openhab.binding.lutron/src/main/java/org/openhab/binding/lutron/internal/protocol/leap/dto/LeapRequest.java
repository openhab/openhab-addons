/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.lutron.internal.protocol.leap.dto;

import com.google.gson.annotations.SerializedName;

/**
 * LeapRequest object
 *
 * @author Peter J Wojciechowski - Initial contribution
 */
public class LeapRequest {
    @SerializedName("CommuniqueType")
    public String communiqueType = "";
    @SerializedName("Header")
    public Header header;
    @SerializedName("Body")
    public Body body;

    public static class Body {
        @SerializedName("Command")
        public Command command;
    }

    public static class Command {
        @SerializedName("CommandType")
        public String commandType;
        @SerializedName("Parameter")
        public CommandParameter commandParameter;

        public static class CommandParameter {
            @SerializedName("Type")
            public String type;
            @SerializedName("Value")
            public String value;
        }
    }

    public static class Header {
        @SerializedName("Url")
        public String url;
    }
}
