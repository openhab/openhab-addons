/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.ircc.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// TODO: Auto-generated Javadoc
/**
 * The Class IrccRemoteCommands.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class IrccRemoteCommands {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(IrccRemoteCommands.class);

    /** The Constant IRCC. */
    public static final String IRCC = "ircc";

    /** The Constant URL. */
    public static final String URL = "url";

    /** The remote cmds. */
    private Map<String, IrccRemoteCommand> remoteCmds = new HashMap<String, IrccRemoteCommand>();

    /**
     * Instantiates a new ircc remote commands.
     *
     * @param codeLists the code lists
     */
    public IrccRemoteCommands(final List<IrccCodeList> codeLists) {

        for (final IrccCodeList codeList : codeLists) {
            for (final Entry<String, String> cmd : codeList.getCommands().entrySet()) {
                addRemoteCommand(new IrccRemoteCommand(cmd.getKey(), IRCC, cmd.getValue()));
            }
        }

        addRemoteCommand(new IrccRemoteCommand("Num1", IRCC, "AAAAAQAAAAEAAAAAAw=="));
        addRemoteCommand(new IrccRemoteCommand("Num2", IRCC, "AAAAAQAAAAEAAAABAw=="));
        addRemoteCommand(new IrccRemoteCommand("Num3", IRCC, "AAAAAQAAAAEAAAACAw=="));
        addRemoteCommand(new IrccRemoteCommand("Num4", IRCC, "AAAAAQAAAAEAAAADAw=="));
        addRemoteCommand(new IrccRemoteCommand("Num5", IRCC, "AAAAAQAAAAEAAAAEAw=="));
        addRemoteCommand(new IrccRemoteCommand("Num6", IRCC, "AAAAAQAAAAEAAAAFAw=="));
        addRemoteCommand(new IrccRemoteCommand("Num7", IRCC, "AAAAAQAAAAEAAAAGAw=="));
        addRemoteCommand(new IrccRemoteCommand("Num8", IRCC, "AAAAAQAAAAEAAAAHAw=="));
        addRemoteCommand(new IrccRemoteCommand("Num9", IRCC, "AAAAAQAAAAEAAAAIAw=="));
        addRemoteCommand(new IrccRemoteCommand("Num0", IRCC, "AAAAAQAAAAEAAAAJAw=="));
        addRemoteCommand(new IrccRemoteCommand("Num11", IRCC, "AAAAAQAAAAEAAAAKAw=="));
        addRemoteCommand(new IrccRemoteCommand("Num12", IRCC, "AAAAAQAAAAEAAAALAw=="));
        addRemoteCommand(new IrccRemoteCommand("Enter", IRCC, "AAAAAQAAAAEAAAALAw=="));
        addRemoteCommand(new IrccRemoteCommand("GGuide", IRCC, "AAAAAQAAAAEAAAAOAw=="));
        addRemoteCommand(new IrccRemoteCommand("ChannelUp", IRCC, "AAAAAQAAAAEAAAAQAw=="));
        addRemoteCommand(new IrccRemoteCommand("ChannelDown", IRCC, "AAAAAQAAAAEAAAARAw=="));
        addRemoteCommand(new IrccRemoteCommand("VolumeUp", IRCC, "AAAAAQAAAAEAAAASAw=="));
        addRemoteCommand(new IrccRemoteCommand("VolumeDown", IRCC, "AAAAAQAAAAEAAAATAw=="));
        addRemoteCommand(new IrccRemoteCommand("Mute", IRCC, "AAAAAQAAAAEAAAAUAw=="));
        addRemoteCommand(new IrccRemoteCommand("TvPower", IRCC, "AAAAAQAAAAEAAAAVAw=="));
        addRemoteCommand(new IrccRemoteCommand("Audio", IRCC, "AAAAAQAAAAEAAAAXAw=="));
        addRemoteCommand(new IrccRemoteCommand("MediaAudioTrack", IRCC, "AAAAAQAAAAEAAAAXAw=="));
        addRemoteCommand(new IrccRemoteCommand("Tv", IRCC, "AAAAAQAAAAEAAAAkAw=="));
        addRemoteCommand(new IrccRemoteCommand("Input", IRCC, "AAAAAQAAAAEAAAAlAw=="));
        addRemoteCommand(new IrccRemoteCommand("TvInput", IRCC, "AAAAAQAAAAEAAAAlAw=="));
        addRemoteCommand(new IrccRemoteCommand("TvAntennaCable", IRCC, "AAAAAQAAAAEAAAAqAw=="));
        addRemoteCommand(new IrccRemoteCommand("WakeUp", IRCC, "AAAAAQAAAAEAAAAuAw=="));
        addRemoteCommand(new IrccRemoteCommand("PowerOff", IRCC, "AAAAAQAAAAEAAAAvAw=="));
        addRemoteCommand(new IrccRemoteCommand("Sleep", IRCC, "AAAAAQAAAAEAAAAvAw=="));
        addRemoteCommand(new IrccRemoteCommand("Right", IRCC, "AAAAAQAAAAEAAAAzAw=="));
        addRemoteCommand(new IrccRemoteCommand("Left", IRCC, "AAAAAQAAAAEAAAA0Aw=="));
        addRemoteCommand(new IrccRemoteCommand("SleepTimer", IRCC, "AAAAAQAAAAEAAAA2Aw=="));
        addRemoteCommand(new IrccRemoteCommand("Analog2", IRCC, "AAAAAQAAAAEAAAA4Aw=="));
        addRemoteCommand(new IrccRemoteCommand("TvAnalog", IRCC, "AAAAAQAAAAEAAAA4Aw=="));
        addRemoteCommand(new IrccRemoteCommand("Display", IRCC, "AAAAAQAAAAEAAAA6Aw=="));
        addRemoteCommand(new IrccRemoteCommand("Jump", IRCC, "AAAAAQAAAAEAAAA7Aw=="));
        addRemoteCommand(new IrccRemoteCommand("PicOff", IRCC, "AAAAAQAAAAEAAAA+Aw=="));
        addRemoteCommand(new IrccRemoteCommand("PictureOff", IRCC, "AAAAAQAAAAEAAAA+Aw=="));
        addRemoteCommand(new IrccRemoteCommand("Teletext", IRCC, "AAAAAQAAAAEAAAA\\/Aw=="));
        addRemoteCommand(new IrccRemoteCommand("Video1", IRCC, "AAAAAQAAAAEAAABAAw=="));
        addRemoteCommand(new IrccRemoteCommand("Video2", IRCC, "AAAAAQAAAAEAAABBAw=="));
        addRemoteCommand(new IrccRemoteCommand("AnalogRgb1", IRCC, "AAAAAQAAAAEAAABDAw=="));
        addRemoteCommand(new IrccRemoteCommand("Home", IRCC, "AAAAAQAAAAEAAABgAw=="));
        addRemoteCommand(new IrccRemoteCommand("Exit", IRCC, "AAAAAQAAAAEAAABjAw=="));
        addRemoteCommand(new IrccRemoteCommand("PictureMode", IRCC, "AAAAAQAAAAEAAABkAw=="));
        addRemoteCommand(new IrccRemoteCommand("Confirm", IRCC, "AAAAAQAAAAEAAABlAw=="));
        addRemoteCommand(new IrccRemoteCommand("Up", IRCC, "AAAAAQAAAAEAAAB0Aw=="));
        addRemoteCommand(new IrccRemoteCommand("Down", IRCC, "AAAAAQAAAAEAAAB1Aw=="));
        addRemoteCommand(new IrccRemoteCommand("ClosedCaption", IRCC, "AAAAAgAAAKQAAAAQAw=="));
        addRemoteCommand(new IrccRemoteCommand("Component1", IRCC, "AAAAAgAAAKQAAAA2Aw=="));
        addRemoteCommand(new IrccRemoteCommand("Component2", IRCC, "AAAAAgAAAKQAAAA3Aw=="));
        addRemoteCommand(new IrccRemoteCommand("Wide", IRCC, "AAAAAgAAAKQAAAA9Aw=="));
        addRemoteCommand(new IrccRemoteCommand("EPG", IRCC, "AAAAAgAAAKQAAABbAw=="));
        addRemoteCommand(new IrccRemoteCommand("PAP", IRCC, "AAAAAgAAAKQAAAB3Aw=="));
        addRemoteCommand(new IrccRemoteCommand("TenKey", IRCC, "AAAAAgAAAJcAAAAMAw=="));
        addRemoteCommand(new IrccRemoteCommand("BSCS", IRCC, "AAAAAgAAAJcAAAAQAw=="));
        addRemoteCommand(new IrccRemoteCommand("Ddata", IRCC, "AAAAAgAAAJcAAAAVAw=="));
        addRemoteCommand(new IrccRemoteCommand("Stop", IRCC, "AAAAAgAAAJcAAAAYAw=="));
        addRemoteCommand(new IrccRemoteCommand("Pause", IRCC, "AAAAAgAAAJcAAAAZAw=="));
        addRemoteCommand(new IrccRemoteCommand("Play", IRCC, "AAAAAgAAAJcAAAAaAw=="));
        addRemoteCommand(new IrccRemoteCommand("Rewind", IRCC, "AAAAAgAAAJcAAAAbAw=="));
        addRemoteCommand(new IrccRemoteCommand("Forward", IRCC, "AAAAAgAAAJcAAAAcAw=="));
        addRemoteCommand(new IrccRemoteCommand("DOT", IRCC, "AAAAAgAAAJcAAAAdAw=="));
        addRemoteCommand(new IrccRemoteCommand("Rec", IRCC, "AAAAAgAAAJcAAAAgAw=="));
        addRemoteCommand(new IrccRemoteCommand("Return", IRCC, "AAAAAgAAAJcAAAAjAw=="));
        addRemoteCommand(new IrccRemoteCommand("Blue", IRCC, "AAAAAgAAAJcAAAAkAw=="));
        addRemoteCommand(new IrccRemoteCommand("Red", IRCC, "AAAAAgAAAJcAAAAlAw=="));
        addRemoteCommand(new IrccRemoteCommand("Green", IRCC, "AAAAAgAAAJcAAAAmAw=="));
        addRemoteCommand(new IrccRemoteCommand("Yellow", IRCC, "AAAAAgAAAJcAAAAnAw=="));
        addRemoteCommand(new IrccRemoteCommand("SubTitle", IRCC, "AAAAAgAAAJcAAAAoAw=="));
        addRemoteCommand(new IrccRemoteCommand("CS", IRCC, "AAAAAgAAAJcAAAArAw=="));
        addRemoteCommand(new IrccRemoteCommand("BS", IRCC, "AAAAAgAAAJcAAAAsAw=="));
        addRemoteCommand(new IrccRemoteCommand("Digital", IRCC, "AAAAAgAAAJcAAAAyAw=="));
        addRemoteCommand(new IrccRemoteCommand("Options", IRCC, "AAAAAgAAAJcAAAA2Aw=="));
        addRemoteCommand(new IrccRemoteCommand("Media", IRCC, "AAAAAgAAAJcAAAA4Aw=="));
        addRemoteCommand(new IrccRemoteCommand("Prev", IRCC, "AAAAAgAAAJcAAAA8Aw=="));
        addRemoteCommand(new IrccRemoteCommand("Next", IRCC, "AAAAAgAAAJcAAAA9Aw=="));
        addRemoteCommand(new IrccRemoteCommand("DpadCenter", IRCC, "AAAAAgAAAJcAAABKAw=="));
        addRemoteCommand(new IrccRemoteCommand("CursorUp", IRCC, "AAAAAgAAAJcAAABPAw=="));
        addRemoteCommand(new IrccRemoteCommand("CursorDown", IRCC, "AAAAAgAAAJcAAABQAw=="));
        addRemoteCommand(new IrccRemoteCommand("CursorLeft", IRCC, "AAAAAgAAAJcAAABNAw=="));
        addRemoteCommand(new IrccRemoteCommand("CursorRight", IRCC, "AAAAAgAAAJcAAABOAw=="));
        addRemoteCommand(new IrccRemoteCommand("ShopRemoteControlForcedDynamic", IRCC, "AAAAAgAAAJcAAABqAw=="));
        addRemoteCommand(new IrccRemoteCommand("FlashPlus", IRCC, "AAAAAgAAAJcAAAB4Aw=="));
        addRemoteCommand(new IrccRemoteCommand("FlashMinus", IRCC, "AAAAAgAAAJcAAAB5Aw=="));
        addRemoteCommand(new IrccRemoteCommand("AudioQualityMode", IRCC, "AAAAAgAAAJcAAAB7Aw=="));
        addRemoteCommand(new IrccRemoteCommand("DemoMode", IRCC, "AAAAAgAAAJcAAAB8Aw=="));
        addRemoteCommand(new IrccRemoteCommand("Analog", IRCC, "AAAAAgAAAHcAAAANAw=="));
        addRemoteCommand(new IrccRemoteCommand("Mode3D", IRCC, "AAAAAgAAAHcAAABNAw=="));
        addRemoteCommand(new IrccRemoteCommand("DigitalToggle", IRCC, "AAAAAgAAAHcAAABSAw=="));
        addRemoteCommand(new IrccRemoteCommand("DemoSurround", IRCC, "AAAAAgAAAHcAAAB7Aw=="));
        addRemoteCommand(new IrccRemoteCommand("*AD", IRCC, "AAAAAgAAABoAAAA7Aw=="));
        addRemoteCommand(new IrccRemoteCommand("AudioMixUp", IRCC, "AAAAAgAAABoAAAA8Aw=="));
        addRemoteCommand(new IrccRemoteCommand("AudioMixDown", IRCC, "AAAAAgAAABoAAAA9Aw=="));
        addRemoteCommand(new IrccRemoteCommand("Tv_Radio", IRCC, "AAAAAgAAABoAAABXAw=="));
        addRemoteCommand(new IrccRemoteCommand("SyncMenu", IRCC, "AAAAAgAAABoAAABYAw=="));
        addRemoteCommand(new IrccRemoteCommand("Hdmi1", IRCC, "AAAAAgAAABoAAABaAw=="));
        addRemoteCommand(new IrccRemoteCommand("Hdmi2", IRCC, "AAAAAgAAABoAAABbAw=="));
        addRemoteCommand(new IrccRemoteCommand("Hdmi3", IRCC, "AAAAAgAAABoAAABcAw=="));
        addRemoteCommand(new IrccRemoteCommand("Hdmi4", IRCC, "AAAAAgAAABoAAABdAw=="));
        addRemoteCommand(new IrccRemoteCommand("TopMenu", IRCC, "AAAAAgAAABoAAABgAw=="));
        addRemoteCommand(new IrccRemoteCommand("PopUpMenu", IRCC, "AAAAAgAAABoAAABhAw=="));
        addRemoteCommand(new IrccRemoteCommand("OneTouchTimeRec", IRCC, "AAAAAgAAABoAAABkAw=="));
        addRemoteCommand(new IrccRemoteCommand("OneTouchView", IRCC, "AAAAAgAAABoAAABlAw=="));
        addRemoteCommand(new IrccRemoteCommand("DUX", IRCC, "AAAAAgAAABoAAABzAw=="));
        addRemoteCommand(new IrccRemoteCommand("FootballMode", IRCC, "AAAAAgAAABoAAAB2Aw=="));
        addRemoteCommand(new IrccRemoteCommand("iManual", IRCC, "AAAAAgAAABoAAAB7Aw=="));
        addRemoteCommand(new IrccRemoteCommand("Netflix", IRCC, "AAAAAgAAABoAAAB8Aw=="));
        addRemoteCommand(new IrccRemoteCommand("Assists", IRCC, "AAAAAgAAAMQAAAA7Aw=="));
        addRemoteCommand(new IrccRemoteCommand("ActionMenu", IRCC, "AAAAAgAAAMQAAABLAw=="));
        addRemoteCommand(new IrccRemoteCommand("Help", IRCC, "AAAAAgAAAMQAAABNAw=="));
        addRemoteCommand(new IrccRemoteCommand("TvSatellite", IRCC, "AAAAAgAAAMQAAABOAw=="));
        addRemoteCommand(new IrccRemoteCommand("WirelessSubwoofer", IRCC, "AAAAAgAAAMQAAAB+Aw=="));
    }

    /**
     * Instantiates a new ircc remote commands.
     *
     * @param codeLists the code lists
     * @param cmdsXml the cmds xml
     */
    public IrccRemoteCommands(final List<IrccCodeList> codeLists, Document cmdsXml) {
        for (final IrccCodeList codeList : codeLists) {
            for (final Entry<String, String> cmd : codeList.getCommands().entrySet()) {
                addRemoteCommand(new IrccRemoteCommand(cmd.getKey(), IRCC, cmd.getValue()));
            }
        }

        final NodeList cmds = cmdsXml.getElementsByTagName("command");
        for (int i = cmds.getLength() - 1; i >= 0; i--) {
            final Element cmd = (Element) cmds.item(i);
            final String cmdName = cmd.getAttribute("name");
            final String cmdType = cmd.getAttribute("type");
            final String cmdValue = cmd.getAttribute("value").replace(":80:80", ":80"); // bug in some ircc
                                                                                        // systems

            addRemoteCommand(new IrccRemoteCommand(cmdName, cmdType, cmdValue));
        }
    }

    /**
     * Adds the remote command.
     *
     * @param cmd the cmd
     */
    private void addRemoteCommand(IrccRemoteCommand cmd) {
        // AVs seem to prefix some of the commands with stuff that's not
        // needed (ex "STR:") [STR-1030, etc]. Remove it if found.
        String lowerName = cmd.getName().toLowerCase();
        final int cmdPrefix = lowerName.indexOf(':');
        if (cmdPrefix >= 0) {
            lowerName = lowerName.substring(cmdPrefix + 1);
        }

        if (remoteCmds.containsKey(lowerName)) {
            logger.debug("Duplicate IRCC command found: {} vs existing value: {}", cmd, remoteCmds.get(cmd.getName()));
        } else {
            remoteCmds.put(lowerName, cmd);
        }

    }

    /**
     * Gets the remote commands.
     *
     * @return the remote commands
     */
    public Map<String, IrccRemoteCommand> getRemoteCommands() {
        return Collections.unmodifiableMap(remoteCmds);
    }

    /**
     * Gets the remote command.
     *
     * @param name the name
     * @return the remote command
     */
    public IrccRemoteCommand getRemoteCommand(String name) {
        return remoteCmds.get(name.toLowerCase());
    }

    /**
     * Gets the power on.
     *
     * @return the power on
     */
    public IrccRemoteCommand getPowerOn() {
        if (remoteCmds.containsKey("power on")) {
            return remoteCmds.get("power on");
        }

        if (remoteCmds.containsKey("poweron")) {
            return remoteCmds.get("poweron");
        }

        if (remoteCmds.containsKey("tvpower")) {
            return remoteCmds.get("tvpower");
        }

        if (remoteCmds.containsKey("powermain")) {
            return remoteCmds.get("powermain");
        }

        if (remoteCmds.containsKey("power")) {
            return remoteCmds.get("power");
        }

        // make a guess!
        for (final IrccRemoteCommand cmd : remoteCmds.values()) {
            if (cmd.getName().toLowerCase().contains("power on")) {
                return cmd;
            }
        }

        for (final IrccRemoteCommand cmd : remoteCmds.values()) {
            if (cmd.getName().toLowerCase().contains("power")) {
                return cmd;
            }
        }

        return null;
    }

    /**
     * Gets the power off.
     *
     * @return the power off
     */
    public IrccRemoteCommand getPowerOff() {
        if (remoteCmds.containsKey("power off")) {
            return remoteCmds.get("power off");
        }

        if (remoteCmds.containsKey("poweroff")) {
            return remoteCmds.get("poweroff");
        }

        if (remoteCmds.containsKey("tvpower")) {
            return remoteCmds.get("tvpower");
        }

        if (remoteCmds.containsKey("powermain")) {
            return remoteCmds.get("powermain");
        }

        if (remoteCmds.containsKey("power")) {
            return remoteCmds.get("power");
        }

        // make a guess!
        for (final IrccRemoteCommand cmd : remoteCmds.values()) {
            if (cmd.getName().toLowerCase().contains("power off")) {
                return cmd;
            }
        }

        for (final IrccRemoteCommand cmd : remoteCmds.values()) {
            if (cmd.getName().toLowerCase().contains("power")) {
                return cmd;
            }
        }

        return null;
    }
}