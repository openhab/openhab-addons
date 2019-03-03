package org.openhab.binding.loxone.internal.core;

import java.io.IOException;

import org.openhab.binding.loxone.internal.core.LxJsonApp3.LxJsonControl;

public class LxControlBurglarAlarm extends LxControl implements LxControlStateListener {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room, LxCategory category) {
            return new LxControlBurglarAlarm(client, uuid, json, room, category);
        }

        @Override
        String getType() {
            return TYPE_NAME;
        }
    }

    private static final String CMD_DELAYED_ON = "delayedOn";
    private static final String CMD_OFF = "off";
    private static final String CMD_ON = "on";
    private static final String CMD_QUIT = "quit";

    /**
     * A name by which Miniserver refers to burglar alarm controls
     */
    private static final String TYPE_NAME = "alarm";

    /**
     * If the alarm control is armed
     */
    private static final String STATE_ARMED = "armed";

    /**
     * The id of the next alarm level
     */
    private static final String STATE_NEXT_LEVEL = "nextLevel";

    /**
     * The delay of the next level in seconds
     */
    private static final String STATE_NEXT_LEVEL_DELAY = "nextLevelDelay";

    /**
     * The total delay of the next level in seconds
     */
    private static final String STATE_NEXT_LEVEL_DELAY_TOTAL = "nextLevelDelayTotal";

    /**
     * The id of the current alarm level
     */
    private static final String STATE_LEVEL = "level";

    /**
     * Timestamp when alarm started
     */
    private static final String STATE_START_TIME = "startTime";

    /**
     * The delay of the alarm control being armed
     */
    private static final String STATE_ARMED_DELAY = "armedDelay";

    /**
     * The total delay of the alarm control being armed
     */
    private static final String STATE_ARMED_DELAY_TOTAL = "armedDelayTotal";

    /**
     * A string of sensors separated by a pipe
     */
    private static final String STATE_SENSOR = "sensors";

    /**
     * If the movement is disabled or not
     */
    private static final String STATE_DISABLED_MOVE = "disabledMove";

    LxControlBurglarAlarm(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room, LxCategory category) {
        super(client, uuid, json, room, category);
    }

    /**
     * Set switch to ON.
     * <p>
     * Sends a command to operate the switch.
     *
     * @throws IOException
     *                         when something went wrong with communication
     */
    public void on() throws IOException {
        socketClient.sendAction(uuid, CMD_ON);
    }

    /**
     * Set switch to OFF.
     * <p>
     * Sends a command to operate the switch.
     *
     * @throws IOException
     *                         when something went wrong with communication
     */
    public void off() throws IOException {
        socketClient.sendAction(uuid, CMD_OFF);
    }

    public Double getState() {
        return getStateValue(STATE_ARMED);
    }

    @Override
    public void onStateChange(LxControlState state) {

    }

}
