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
package org.openhab.binding.lgthinq.lgservices.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CommandDefinition}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class CommandDefinition {
    private static final Logger logger = LoggerFactory.getLogger(CommandDefinition.class);
    /**
     * This is the command tag value that is used by the API to launch the command service
     */
    private String dataKey = "";
    private String command = "";
    private Map<String, Object> data = new HashMap<>();

    // =========== Used only for thinq V1 commands =============
    private String cmdOpt = "";
    private String cmdOptValue = "";
    private boolean isBinary;
    // This is the template in the device definition of data that must be send to the LG API complementing the command
    private String dataTemplate = "";
    /*
     * holds the how command (in text) as defined in the node command definition. Ex: For Remote Start (WM):
     * {
     * "cmd":"Control",
     * "cmdOpt":"Operation",
     * "value":"Start",
     * "data":
     * "[{{Course}},{{Wash}},{{SpinSpeed}},{{WaterTemp}},{{RinseOption}},0,{{Reserve_Time_H}},{{Reserve_Time_M}},{{LoadItem}},{{Option1}},{{Option2}},0,{{SmartCourse}},0]",
     * "encode":true
     * }
     */
    private String rawCommand = "";

    // =========================================================

    public String getRawCommand() {
        return rawCommand;
    }

    public void setRawCommand(String rawCommand) {
        this.rawCommand = rawCommand;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getCmdOpt() {
        return cmdOpt;
    }

    public void setCmdOpt(String cmdOpt) {
        this.cmdOpt = cmdOpt;
    }

    public String getCmdOptValue() {
        return cmdOptValue;
    }

    public void setCmdOptValue(String cmdOptValue) {
        this.cmdOptValue = cmdOptValue;
    }

    public boolean isBinary() {
        return isBinary;
    }

    public void setBinary(boolean binary) {
        isBinary = binary;
    }

    public String getDataTemplate() {
        return dataTemplate;
    }

    public void setDataTemplate(String dataTemplate) {
        this.dataTemplate = dataTemplate;
    }
}
