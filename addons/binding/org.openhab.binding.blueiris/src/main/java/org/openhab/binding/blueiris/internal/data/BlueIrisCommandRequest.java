package org.openhab.binding.blueiris.internal.data;

import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

/**
 * Base class for all command requests to the blue iris connection.
 *
 * @author David Bennett - Initial Contribution
 */
public abstract class BlueIrisCommandRequest {
    @Expose
    private String cmd;
    @Expose
    private String session;
    @Expose
    private String response;

    public BlueIrisCommandRequest(String cmd) {
        this.cmd = cmd;
    }

    /**
     * @return The name of the command.
     */
    public String getCmd() {
        return cmd;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public abstract Object deserializeReply(InputStreamReader str, Gson gson);

    @Override
    public String toString() {
        return "BlueIrisCommandRequest [cmd=" + cmd + "]";
    }
}
