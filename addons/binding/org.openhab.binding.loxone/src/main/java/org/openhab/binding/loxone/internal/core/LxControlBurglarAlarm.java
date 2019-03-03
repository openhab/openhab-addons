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

    private static final String CMD_DELAYED_ON = "delayedon";
    private static final String CMD_DELAYED_ON_WITHOUT_MOVEMENT = "delayedon/0";
    private static final String CMD_DELAYED_ON_WITH_MOVEMENT = "delayedon/1";
    private static final String CMD_DISABLE_MOVEMENT = "dismv/0";
    private static final String CMD_ENABLE_MOVEMENT = "dismv/1";
    private static final String CMD_OFF = "off";
    private static final String CMD_ON = "on";
    private static final String CMD_ON_WITHOUT_MOVEMENT = "on/0";
    private static final String CMD_ON_WITH_MOVEMENT = "on/1";
    private static final String CMD_QUIT = "quit";

    /**
     * A name by which Miniserver refers to burglar alarm controls
     */
    public static final String TYPE_NAME = "alarm";

    /**
     * If the alarm control is armed
     */
    public static final String STATE_ARMED = "armed";

    /**
     * The id of the next alarm level
     */
    public static final String STATE_NEXT_LEVEL = "nextLevel";

    /**
     * The delay of the next level in seconds
     */
    public static final String STATE_NEXT_LEVEL_DELAY = "nextLevelDelay";

    /**
     * The total delay of the next level in seconds
     */
    public static final String STATE_NEXT_LEVEL_DELAY_TOTAL = "nextLevelDelayTotal";

    /**
     * The id of the current alarm level
     */
    public static final String STATE_LEVEL = "level";

    /**
     * Timestamp when alarm started
     */
    public static final String STATE_START_TIME = "startTime";

    /**
     * The delay of the alarm control being armed
     */
    public static final String STATE_ARMED_DELAY = "armedDelay";

    /**
     * The total delay of the alarm control being armed
     */
    public static final String STATE_ARMED_DELAY_TOTAL = "armedDelayTotal";

    /**
     * A string of sensors separated by a pipe
     */
    public static final String STATE_SENSOR = "sensors";

    /**
     * If the movement is disabled or not
     */
    public static final String STATE_DISABLED_MOVE = "disabledMove";

    LxControlBurglarAlarm(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room, LxCategory category) {
        super(client, uuid, json, room, category);

        addStateListener(STATE_ARMED, this);
    }

    /**
     * Set alarm to ON.
     * <p>
     * Sends a command to operate the alarm.
     *
     * @throws IOException
     *                         when something went wrong with communication
     */
    public void on() throws IOException {
        socketClient.sendAction(uuid, CMD_ON);
    }

    /**
     * Set alarm to ON with movement
     *
     * @throws IOException
     */
    public void onWithMovement() throws IOException {
        socketClient.sendAction(uuid, CMD_ON_WITH_MOVEMENT);
    }

    /**
     * Set alarm to ON without movement
     *
     * @throws IOException
     */
    public void onWithoutMovement() throws IOException {
        socketClient.sendAction(uuid, CMD_ON_WITHOUT_MOVEMENT);
    }

    /**
     * Set alarm delayed ON
     * <p>
     * Sends a command to operate the switch.
     *
     * @throws IOException
     *                         when something went wrong with communication
     */
    public void delayedOn() throws IOException {
        socketClient.sendAction(uuid, CMD_DELAYED_ON);
    }

    /**
     * Set alarm delayed ON without movement
     * <p>
     * Sends a command to operate the switch.
     *
     * @throws IOException
     *                         when something went wrong with communication
     */
    public void delayedOnWithoutMovement() throws IOException {
        socketClient.sendAction(uuid, CMD_DELAYED_ON_WITHOUT_MOVEMENT);
    }

    /**
     * Set alarm delayed ON with movement
     * <p>
     * Sends a command to operate the switch.
     *
     * @throws IOException
     *                         when something went wrong with communication
     */
    public void delayedOnWithMovement() throws IOException {
        socketClient.sendAction(uuid, CMD_DELAYED_ON_WITH_MOVEMENT);
    }

    /**
     * Set alarm to OFF.
     * <p>
     * Sends a command to operate the alarm.
     *
     * @throws IOException
     *                         when something went wrong with communication
     */
    public void off() throws IOException {
        socketClient.sendAction(uuid, CMD_OFF);
    }

    /**
     * Acknowledge alarm
     *
     * <p>
     * Sends a command to operate the alarm.
     *
     * @throws IOException
     *                         when something went wrong with communication
     */
    public void quit() throws IOException {
        socketClient.sendAction(uuid, CMD_QUIT);
    }

    /**
     * Enable movement
     *
     * @throws IOException
     *                         when something went wrong with communication
     */
    public void enableMovement() throws IOException {
        socketClient.sendAction(uuid, CMD_ENABLE_MOVEMENT);
    }

    /**
     * Disable movement
     *
     * @throws IOException
     *                         when something went wrong with communication
     */
    public void disableMovement() throws IOException {
        socketClient.sendAction(uuid, CMD_DISABLE_MOVEMENT);
    }

    public Double getState() {
        return getStateValue(STATE_ARMED);
    }

    @Override
    public void onStateChange(LxControlState state) {

    }

}
