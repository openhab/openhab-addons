/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.roomba.model;

import java.util.LinkedList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * POJO representation of a Roomba API payload
 *
 * Example: {"do":"set","args":["cmd",{"op":"start"}],"id":"1"}
 *
 * @author Stephen Liang
 *
 */
public class RoombaPayload {
    @SerializedName("do")
    public String doCommand;
    public List<Object> args; // The Roomba API mixes between strings and complex data structures
    public String id;

    public RoombaPayload(String doCommand, String operation) {
        this(doCommand, operation, null);
    }

    public RoombaPayload(String doCommand, String operation, RoombaOperation roombaCommand) {
        this.doCommand = doCommand;
        this.id = "1";
        this.args = new LinkedList<>();
        this.args.add(operation);

        if (roombaCommand != null) {
            this.args.add(roombaCommand);
        }
    }

    public RoombaPayload(Builder builder) {
        this.doCommand = builder.doCommand;
        this.args = builder.args;
        this.id = builder.id;
    }

    public String getDoCommand() {
        return doCommand;
    }

    public List<Object> getArgs() {
        return args;
    }

    public String getId() {
        return id;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        public String doCommand;
        public List<Object> args;
        public String id;

        public Builder() {
            this.args = new LinkedList<>();
        }

        public Builder setDoCommand(String doCommand) {
            this.doCommand = doCommand;

            return this;
        }

        public Builder addArg(Object arg) {
            this.args.add(arg);

            return this;
        }

        public Builder setId(String id) {
            this.id = id;

            return this;
        }

        public RoombaPayload build() {
            return new RoombaPayload(this);
        }
    }
}
