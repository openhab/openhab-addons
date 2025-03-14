/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgservices.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents a command definition used by the LG API, including details about the command,
 * associated data, and optional configurations for the command.
 *
 * <p>
 * This class contains the following properties:
 * <ul>
 * <li><b>command:</b> A string representing the command tag value that the API uses to launch
 * the command service.</li>
 * <li><b>data:</b> A map holding additional data related to the command.</li>
 * <li><b>cmdOptValue:</b> An optional value used only for LG ThinQ V1 commands.</li>
 * <li><b>isBinary:</b> A boolean indicating whether the command operates in binary mode,
 * used only for LG ThinQ V1 commands.</li>
 * <li><b>dataTemplate:</b> A template for data that needs to be sent to the LG API,
 * as defined in the device specification.</li>
 * <li><b>rawCommand:</b> A raw representation of the command in text format, which includes
 * the full command with placeholders and data used for sending requests to the LG API.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Usage example: A typical command definition might look like the following:
 *
 * <pre>
 * {
 *     "cmd": "Control",
 *     "cmdOpt": "Operation",
 *     "value": "Start",
 *     "data": "[{{Course}},{{Wash}},{{SpinSpeed}},{{WaterTemp}},{{RinseOption}},0,{{Reserve_Time_H}},{{Reserve_Time_M}},{{LoadItem}},{{Option1}},{{Option2}},0,{{SmartCourse}},0]",
 *     "encode": true
 * }
 * </pre>
 * </p>
 *
 * @author Nemer Daud - Initial contribution
 * @version 1.0
 */
@NonNullByDefault
public class CommandDefinition {

    /**
     * The command tag value used by the API to launch the command service.
     */
    private String command = "";

    /**
     * A map containing additional data related to the command.
     */
    private Map<String, Object> data = new HashMap<>();

    /**
     * An optional value used only for ThinQ V1 commands.
     */
    private String cmdOptValue = "";

    /**
     * A flag indicating whether the command operates in binary mode.
     * This is used only for ThinQ V1 commands.
     */
    private boolean isBinary;

    /**
     * The template in the device definition of data that must be sent to the LG API.
     * It complements the command by providing the necessary data for the command execution.
     */
    private String dataTemplate = "";

    /**
     * The raw command, as defined in the node command definition, which includes placeholders for data.
     */
    private String rawCommand = "";

    /**
     * Gets the raw command.
     *
     * @return the raw command string
     */
    public String getRawCommand() {
        return rawCommand;
    }

    /**
     * Sets the raw command.
     *
     * @param rawCommand the raw command string
     */
    public void setRawCommand(String rawCommand) {
        this.rawCommand = rawCommand;
    }

    /**
     * Gets the command tag value.
     *
     * @return the command tag value
     */
    public String getCommand() {
        return command;
    }

    /**
     * Sets the command tag value.
     *
     * @param command the command tag value
     */
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * Gets the additional data associated with the command.
     *
     * @return a map of data associated with the command
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * Sets the additional data associated with the command.
     *
     * @param data a map of data to associate with the command
     */
    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    /**
     * Gets the optional value used for ThinQ V1 commands.
     *
     * @return the cmdOpt value
     */
    public String getCmdOptValue() {
        return cmdOptValue;
    }

    /**
     * Sets the optional value used for ThinQ V1 commands.
     *
     * @param cmdOptValue the cmdOpt value
     */
    public void setCmdOptValue(String cmdOptValue) {
        this.cmdOptValue = cmdOptValue;
    }

    /**
     * Checks if the command operates in binary mode.
     *
     * @return true if the command is binary, false otherwise
     */
    public boolean isBinary() {
        return isBinary;
    }

    /**
     * Sets whether the command operates in binary mode.
     *
     * @param binary true if the command should operate in binary mode, false otherwise
     */
    public void setBinary(boolean binary) {
        isBinary = binary;
    }

    /**
     * Gets the data template that must be sent to the LG API.
     *
     * @return the data template string
     */
    public String getDataTemplate() {
        return dataTemplate;
    }

    /**
     * Sets the data template that must be sent to the LG API.
     *
     * @param dataTemplate the data template string
     */
    public void setDataTemplate(String dataTemplate) {
        this.dataTemplate = dataTemplate;
    }
}
