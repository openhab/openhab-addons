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
package org.openhab.binding.amplipi.internal.model;

import com.google.gson.annotations.SerializedName;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A command to execute on a stream
 **/
@Schema(description = "A command to execute on a stream ")
public class Command {

    @Schema(required = true)
    @SerializedName("stream_id")
    /**
     * Stream to execute the command on
     **/
    private Integer streamId;

    @Schema(required = true)
    /**
     * Command to execute
     **/
    private String cmd;

    /**
     * Stream to execute the command on
     *
     * @return streamId
     **/
    public Integer getStreamId() {
        return streamId;
    }

    public void setStreamId(Integer streamId) {
        this.streamId = streamId;
    }

    public Command streamId(Integer streamId) {
        this.streamId = streamId;
        return this;
    }

    /**
     * Command to execute
     *
     * @return cmd
     **/
    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public Command cmd(String cmd) {
        this.cmd = cmd;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Command {\n");

        sb.append("    streamId: ").append(toIndentedString(streamId)).append("\n");
        sb.append("    cmd: ").append(toIndentedString(cmd)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private static String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
