package org.openhab.binding.blueiris.internal.data;

/**
 * Base class for all command requests to the blue iris connection.
 *
 * @author David Bennett - Initial Contribution
 */
public class BlueIrisCommandRequest {
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

}
