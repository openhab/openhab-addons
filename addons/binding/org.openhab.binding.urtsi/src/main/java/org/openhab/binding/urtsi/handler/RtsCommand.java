package org.openhab.binding.urtsi.handler;

/**
 * The {@code RtsCommand} provides the available commands due to Somfy's RTS protocol.
 *
 * @author Oliver Libutzki - Initial contribution
 *
 */
public enum RtsCommand {
    UP("U"),
    DOWN("D"),
    STOP("S");

    private String actionKey;

    private RtsCommand(String actionKey) {
        this.actionKey = actionKey;
    }

    /**
     * Returns the action key which is used for communicating with the URTSI II device.
     *
     * @return the action key
     */
    public String getActionKey() {
        return actionKey;
    }

}
