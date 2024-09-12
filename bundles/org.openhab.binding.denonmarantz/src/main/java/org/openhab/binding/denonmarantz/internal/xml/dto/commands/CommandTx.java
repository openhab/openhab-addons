/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.denonmarantz.internal.xml.dto.commands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * Individual commands that can be sent to a Denon/Marantz receiver to request specific information.
 *
 * @author Jeroen Idserda - Initial contribution
 */
@XmlRootElement(name = "cmd")
@XmlAccessorType(XmlAccessType.FIELD)
public class CommandTx {

    private static final String DEFAULT_ID = "1";

    public static final CommandTx CMD_ALL_POWER = of("GetAllZonePowerStatus");

    public static final CommandTx CMD_VOLUME_LEVEL = of("GetVolumeLevel");

    public static final CommandTx CMD_MUTE_STATUS = of("GetMuteStatus");

    public static final CommandTx CMD_SOURCE_STATUS = of("GetSourceStatus");

    public static final CommandTx CMD_SURROUND_STATUS = of("GetSurroundModeStatus");

    public static final CommandTx CMD_ZONE_NAME = of("GetZoneName");

    public static final CommandTx CMD_NET_STATUS = of("GetNetAudioStatus");

    public static final CommandTx CMD_RENAME_SOURCE = of("GetRenameSource");

    public static final CommandTx CMD_DELETED_SOURCE = of("GetDeletedSource");

    @XmlAttribute(name = "id")
    private String id;

    @XmlValue
    private String value;

    public CommandTx() {
    }

    public CommandTx(String value) {
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static CommandTx of(String command) {
        CommandTx cmdTx = new CommandTx(command);
        cmdTx.setId(DEFAULT_ID);
        return cmdTx;
    }
}
