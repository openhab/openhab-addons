/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.loxone.internal.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openhab.binding.loxone.internal.core.LxJsonApp3.LxJsonControl;

/**
 * Loxone Control that controls the Burglar Alarm
 *
 * @author Michael Mattan - Initial contribution
 *
 */
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
    public static final String CMD_DISABLE_MOVEMENT = "dismv/0";
    public static final String CMD_ENABLE_MOVEMENT = "dismv/1";
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

    private List<LxControlBurglarAlarmCommand> commands = new ArrayList<>();

    LxControlBurglarAlarm(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room, LxCategory category) {
        super(client, uuid, json, room, category);

        this.addSubControls();
    }

    /**
     * a map of commands with their corresponding label
     *
     * @return the available command mappings
     */
    private Map<String, String> getCommandMappings() {
        Map<String, String> commands = new HashMap<>();

        commands.put(CMD_DELAYED_ON_WITHOUT_MOVEMENT, "Delayed On");
        commands.put(CMD_DELAYED_ON_WITHOUT_MOVEMENT, "Delayed On / Without movement detection");
        commands.put(CMD_DELAYED_ON_WITH_MOVEMENT, "Delayed On / With movement detection");
        commands.put(CMD_ON_WITHOUT_MOVEMENT, "On / Without movement detection");
        commands.put(CMD_ON_WITH_MOVEMENT, "On / With movement detection");
        commands.put(CMD_QUIT, "Acknowledge alarm");

        return commands;
    }

    /**
     * Get a list of available commands for the Burglar Alarm
     *
     * @return the list of burglar alarms
     */
    private void addSubControls() {
        List<LxControlBurglarAlarmCommand> commands = this.getCommandMappings().entrySet().stream().map(e -> {
            LxJsonControl json = new LxJsonApp3().new LxJsonControl();
            json.name = e.getValue();
            json.type = "command-" + e.getKey();

            LxUuid commandUuid = new LxUuid(this.getUuid().toString() + "-" + json.type);
            return new LxControlBurglarAlarmCommand(commandUuid, json, this.getRoom(), this.getCategory(), e.getKey(),
                    e.getValue(), this);
        }).collect(Collectors.toList());

        for (LxControlBurglarAlarmCommand command : commands) {
            this.subControls.put(command.getUuid(), command);
        }
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
     * performs the given command on the burglar alarm controller
     *
     * @param command the command to perform
     * @throws IOException
     */
    public void executeCommand(String command) throws IOException {
        socketClient.sendAction(uuid, command);
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
