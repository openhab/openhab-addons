package org.openhab.binding.amplipi.internal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A command to execute on a stream
 **/
@Schema(description = "A command to execute on a stream ")
public class Command {

    @Schema(required = true)
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
    @JsonProperty("stream_id")
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
    @JsonProperty("cmd")
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
