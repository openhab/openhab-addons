package org.openhab.binding.blueiris.internal.data;

import java.io.InputStreamReader;

import com.google.gson.Gson;

/**
 * Base class for all command requests to the blue iris connection.
 *
 * @author David Bennett - Initial Contribution
 */
public abstract class BlueIrisCommandRequest {
    private String cmd;

    public BlueIrisCommandRequest(String cmd) {
        this.cmd = cmd;
    }

    /**
     * @return The name of the command.
     */
    public String getCmd() {
        return cmd;
    }

    public abstract Object deserializeReply(InputStreamReader str, Gson gson);

    @Override
    public String toString() {
        return "BlueIrisCommandRequest [cmd=" + cmd + "]";
    }
}
