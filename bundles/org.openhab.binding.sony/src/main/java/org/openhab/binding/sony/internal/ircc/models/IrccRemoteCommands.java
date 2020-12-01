/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.ircc.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * This class represents the deserialized results of an IRCC remote command list. The following is an example of the
 * results that will be deserialized.
 *
 * <pre>
 * {@code
     <?xml version="1.0"?>
     <remoteCommandList>
          <command name="Confirm" type="ircc" value="AAAAAQAAAAEAAABlAw==" />
          <command name="Up" type="ircc" value="AAAAAQAAAAEAAAB0Aw==" />
          <command name="Down" type="ircc" value="AAAAAQAAAAEAAAB1Aw==" />
          <command name="Right" type="ircc" value="AAAAAQAAAAEAAAAzAw==" />
          <command name="Left" type="ircc" value="AAAAAQAAAAEAAAA0Aw==" />
          <command name="Home" type="ircc" value="AAAAAQAAAAEAAABgAw==" />
          <command name="Options" type="ircc" value="AAAAAgAAAJcAAAA2Aw==" />
          <command name="Return" type="ircc" value="AAAAAgAAAJcAAAAjAw==" />
          <command name="Num1" type="ircc" value="AAAAAQAAAAEAAAAAAw==" />
     </remoteCommandList>
 * }
 * </pre>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
@XStreamAlias("remoteCommandList")
public class IrccRemoteCommands {
    /** The logger used by the client */
    private final Logger logger = LoggerFactory.getLogger(IrccRemoteCommands.class);

    /**
     * The map of remote commands by the remote command name (lowercased)
     */
    private final Map<String, IrccRemoteCommand> remoteCmds = new HashMap<>();

    /**
     * Constructs the remote commands using the {@link #getDefaultCommands()}
     */
    public IrccRemoteCommands() {
        this(null);
    }

    /**
     * Constructs the remote commands from the given cmds (or the {@link #getDefaultCommands()} if null or empty)
     *
     * @param cmds a possibly null, possibly empty map of commands
     */
    private IrccRemoteCommands(final @Nullable Map<String, IrccRemoteCommand> cmds) {
        remoteCmds.putAll(cmds == null || cmds.isEmpty() ? getDefaultCommands() : cmds);
    }

    /**
     * Constructs the default list of commands (that many of the IRCC systems obey)
     *
     * @return a non-null, non-empty map of {@link IrccRemoteCommand}
     */
    public static Map<String, IrccRemoteCommand> getDefaultCommands() {
        final Map<String, IrccRemoteCommand> remoteCmds = new HashMap<>();
        remoteCmds.put("num1", new IrccRemoteCommand("Num1", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAAAAw=="));
        remoteCmds.put("num2", new IrccRemoteCommand("Num2", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAABAw=="));
        remoteCmds.put("num3", new IrccRemoteCommand("Num3", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAACAw=="));
        remoteCmds.put("num4", new IrccRemoteCommand("Num4", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAADAw=="));
        remoteCmds.put("num5", new IrccRemoteCommand("Num5", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAAEAw=="));
        remoteCmds.put("num6", new IrccRemoteCommand("Num6", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAAFAw=="));
        remoteCmds.put("num7", new IrccRemoteCommand("Num7", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAAGAw=="));
        remoteCmds.put("num8", new IrccRemoteCommand("Num8", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAAHAw=="));
        remoteCmds.put("num9", new IrccRemoteCommand("Num9", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAAIAw=="));
        remoteCmds.put("num0", new IrccRemoteCommand("Num0", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAAJAw=="));
        remoteCmds.put("num11", new IrccRemoteCommand("Num11", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAAKAw=="));
        remoteCmds.put("num12", new IrccRemoteCommand("Num12", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAALAw=="));
        remoteCmds.put("enter", new IrccRemoteCommand("Enter", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAALAw=="));
        remoteCmds.put("gguide", new IrccRemoteCommand("GGuide", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAAOAw=="));
        remoteCmds.put("channelup", new IrccRemoteCommand("ChannelUp", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAAQAw=="));
        remoteCmds.put("channeldown",
                new IrccRemoteCommand("ChannelDown", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAARAw=="));
        remoteCmds.put("volumeup", new IrccRemoteCommand("VolumeUp", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAASAw=="));
        remoteCmds.put("volumedown",
                new IrccRemoteCommand("VolumeDown", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAATAw=="));
        remoteCmds.put("mute", new IrccRemoteCommand("Mute", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAAUAw=="));
        remoteCmds.put("tvpower", new IrccRemoteCommand("TvPower", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAAVAw=="));
        remoteCmds.put("audio", new IrccRemoteCommand("Audio", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAAXAw=="));
        remoteCmds.put("mediaaudiotrack",
                new IrccRemoteCommand("MediaAudioTrack", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAAXAw=="));
        remoteCmds.put("tv", new IrccRemoteCommand("Tv", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAAkAw=="));
        remoteCmds.put("input", new IrccRemoteCommand("Input", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAAlAw=="));
        remoteCmds.put("tvinput", new IrccRemoteCommand("TvInput", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAAlAw=="));
        remoteCmds.put("tvantennacable",
                new IrccRemoteCommand("TvAntennaCable", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAAqAw=="));
        remoteCmds.put("wakeup", new IrccRemoteCommand("WakeUp", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAAuAw=="));
        remoteCmds.put("poweroff", new IrccRemoteCommand("PowerOff", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAAvAw=="));
        remoteCmds.put("sleep", new IrccRemoteCommand("Sleep", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAAvAw=="));
        remoteCmds.put("right", new IrccRemoteCommand("Right", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAAzAw=="));
        remoteCmds.put("left", new IrccRemoteCommand("Left", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAA0Aw=="));
        remoteCmds.put("sleeptimer",
                new IrccRemoteCommand("SleepTimer", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAA2Aw=="));
        remoteCmds.put("analog2", new IrccRemoteCommand("Analog2", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAA4Aw=="));
        remoteCmds.put("tvanalog", new IrccRemoteCommand("TvAnalog", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAA4Aw=="));
        remoteCmds.put("display", new IrccRemoteCommand("Display", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAA6Aw=="));
        remoteCmds.put("jump", new IrccRemoteCommand("Jump", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAA7Aw=="));
        remoteCmds.put("picoff", new IrccRemoteCommand("PicOff", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAA+Aw=="));
        remoteCmds.put("pictureoff",
                new IrccRemoteCommand("PictureOff", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAA+Aw=="));
        remoteCmds.put("teletext", new IrccRemoteCommand("Teletext", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAA\\/Aw=="));
        remoteCmds.put("video1", new IrccRemoteCommand("Video1", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAABAAw=="));
        remoteCmds.put("video2", new IrccRemoteCommand("Video2", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAABBAw=="));
        remoteCmds.put("analogrgb1",
                new IrccRemoteCommand("AnalogRgb1", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAABDAw=="));
        remoteCmds.put("home", new IrccRemoteCommand("Home", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAABgAw=="));
        remoteCmds.put("exit", new IrccRemoteCommand("Exit", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAABjAw=="));
        remoteCmds.put("picturemode",
                new IrccRemoteCommand("PictureMode", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAABkAw=="));
        remoteCmds.put("confirm", new IrccRemoteCommand("Confirm", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAABlAw=="));
        remoteCmds.put("up", new IrccRemoteCommand("Up", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAB0Aw=="));
        remoteCmds.put("down", new IrccRemoteCommand("Down", IrccRemoteCommand.IRCC, "AAAAAQAAAAEAAAB1Aw=="));
        remoteCmds.put("closedcaption",
                new IrccRemoteCommand("ClosedCaption", IrccRemoteCommand.IRCC, "AAAAAgAAAKQAAAAQAw=="));
        remoteCmds.put("component1",
                new IrccRemoteCommand("Component1", IrccRemoteCommand.IRCC, "AAAAAgAAAKQAAAA2Aw=="));
        remoteCmds.put("component2",
                new IrccRemoteCommand("Component2", IrccRemoteCommand.IRCC, "AAAAAgAAAKQAAAA3Aw=="));
        remoteCmds.put("wide", new IrccRemoteCommand("Wide", IrccRemoteCommand.IRCC, "AAAAAgAAAKQAAAA9Aw=="));
        remoteCmds.put("epg", new IrccRemoteCommand("EPG", IrccRemoteCommand.IRCC, "AAAAAgAAAKQAAABbAw=="));
        remoteCmds.put("pap", new IrccRemoteCommand("PAP", IrccRemoteCommand.IRCC, "AAAAAgAAAKQAAAB3Aw=="));
        remoteCmds.put("tenkey", new IrccRemoteCommand("TenKey", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAAAMAw=="));
        remoteCmds.put("bscs", new IrccRemoteCommand("BSCS", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAAAQAw=="));
        remoteCmds.put("ddata", new IrccRemoteCommand("Ddata", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAAAVAw=="));
        remoteCmds.put("stop", new IrccRemoteCommand("Stop", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAAAYAw=="));
        remoteCmds.put("pause", new IrccRemoteCommand("Pause", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAAAZAw=="));
        remoteCmds.put("play", new IrccRemoteCommand("Play", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAAAaAw=="));
        remoteCmds.put("rewind", new IrccRemoteCommand("Rewind", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAAAbAw=="));
        remoteCmds.put("forward", new IrccRemoteCommand("Forward", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAAAcAw=="));
        remoteCmds.put("dot", new IrccRemoteCommand("DOT", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAAAdAw=="));
        remoteCmds.put("rec", new IrccRemoteCommand("Rec", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAAAgAw=="));
        remoteCmds.put("return", new IrccRemoteCommand("Return", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAAAjAw=="));
        remoteCmds.put("blue", new IrccRemoteCommand("Blue", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAAAkAw=="));
        remoteCmds.put("red", new IrccRemoteCommand("Red", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAAAlAw=="));
        remoteCmds.put("green", new IrccRemoteCommand("Green", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAAAmAw=="));
        remoteCmds.put("yellow", new IrccRemoteCommand("Yellow", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAAAnAw=="));
        remoteCmds.put("subtitle", new IrccRemoteCommand("SubTitle", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAAAoAw=="));
        remoteCmds.put("cs", new IrccRemoteCommand("CS", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAAArAw=="));
        remoteCmds.put("bs", new IrccRemoteCommand("BS", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAAAsAw=="));
        remoteCmds.put("digital", new IrccRemoteCommand("Digital", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAAAyAw=="));
        remoteCmds.put("options", new IrccRemoteCommand("Options", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAAA2Aw=="));
        remoteCmds.put("media", new IrccRemoteCommand("Media", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAAA4Aw=="));
        remoteCmds.put("prev", new IrccRemoteCommand("Prev", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAAA8Aw=="));
        remoteCmds.put("next", new IrccRemoteCommand("Next", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAAA9Aw=="));
        remoteCmds.put("dpadcenter",
                new IrccRemoteCommand("DpadCenter", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAABKAw=="));
        remoteCmds.put("cursorup", new IrccRemoteCommand("CursorUp", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAABPAw=="));
        remoteCmds.put("cursordown",
                new IrccRemoteCommand("CursorDown", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAABQAw=="));
        remoteCmds.put("cursorleft",
                new IrccRemoteCommand("CursorLeft", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAABNAw=="));
        remoteCmds.put("cursorright",
                new IrccRemoteCommand("CursorRight", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAABOAw=="));
        remoteCmds.put("shopremotecontrolforceddynamic", new IrccRemoteCommand("ShopRemoteControlForcedDynamic",
                IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAABqAw=="));
        remoteCmds.put("flashplus", new IrccRemoteCommand("FlashPlus", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAAB4Aw=="));
        remoteCmds.put("flashminus",
                new IrccRemoteCommand("FlashMinus", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAAB5Aw=="));
        remoteCmds.put("audioqualitymode",
                new IrccRemoteCommand("AudioQualityMode", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAAB7Aw=="));
        remoteCmds.put("demomode", new IrccRemoteCommand("DemoMode", IrccRemoteCommand.IRCC, "AAAAAgAAAJcAAAB8Aw=="));
        remoteCmds.put("analog", new IrccRemoteCommand("Analog", IrccRemoteCommand.IRCC, "AAAAAgAAAHcAAAANAw=="));
        remoteCmds.put("mode3d", new IrccRemoteCommand("Mode3D", IrccRemoteCommand.IRCC, "AAAAAgAAAHcAAABNAw=="));
        remoteCmds.put("digitaltoggle",
                new IrccRemoteCommand("DigitalToggle", IrccRemoteCommand.IRCC, "AAAAAgAAAHcAAABSAw=="));
        remoteCmds.put("demosurround",
                new IrccRemoteCommand("DemoSurround", IrccRemoteCommand.IRCC, "AAAAAgAAAHcAAAB7Aw=="));
        remoteCmds.put("*ad", new IrccRemoteCommand("*AD", IrccRemoteCommand.IRCC, "AAAAAgAAABoAAAA7Aw=="));
        remoteCmds.put("audiomixup",
                new IrccRemoteCommand("AudioMixUp", IrccRemoteCommand.IRCC, "AAAAAgAAABoAAAA8Aw=="));
        remoteCmds.put("audiomixdown",
                new IrccRemoteCommand("AudioMixDown", IrccRemoteCommand.IRCC, "AAAAAgAAABoAAAA9Aw=="));
        remoteCmds.put("tv_radio", new IrccRemoteCommand("Tv_Radio", IrccRemoteCommand.IRCC, "AAAAAgAAABoAAABXAw=="));
        remoteCmds.put("syncmenu", new IrccRemoteCommand("SyncMenu", IrccRemoteCommand.IRCC, "AAAAAgAAABoAAABYAw=="));
        remoteCmds.put("hdmi1", new IrccRemoteCommand("Hdmi1", IrccRemoteCommand.IRCC, "AAAAAgAAABoAAABaAw=="));
        remoteCmds.put("hdmi2", new IrccRemoteCommand("Hdmi2", IrccRemoteCommand.IRCC, "AAAAAgAAABoAAABbAw=="));
        remoteCmds.put("hdmi3", new IrccRemoteCommand("Hdmi3", IrccRemoteCommand.IRCC, "AAAAAgAAABoAAABcAw=="));
        remoteCmds.put("hdmi4", new IrccRemoteCommand("Hdmi4", IrccRemoteCommand.IRCC, "AAAAAgAAABoAAABdAw=="));
        remoteCmds.put("topmenu", new IrccRemoteCommand("TopMenu", IrccRemoteCommand.IRCC, "AAAAAgAAABoAAABgAw=="));
        remoteCmds.put("popupmenu", new IrccRemoteCommand("PopUpMenu", IrccRemoteCommand.IRCC, "AAAAAgAAABoAAABhAw=="));
        remoteCmds.put("onetouchtimerec",
                new IrccRemoteCommand("OneTouchTimeRec", IrccRemoteCommand.IRCC, "AAAAAgAAABoAAABkAw=="));
        remoteCmds.put("onetouchview",
                new IrccRemoteCommand("OneTouchView", IrccRemoteCommand.IRCC, "AAAAAgAAABoAAABlAw=="));
        remoteCmds.put("dux", new IrccRemoteCommand("DUX", IrccRemoteCommand.IRCC, "AAAAAgAAABoAAABzAw=="));
        remoteCmds.put("footballmode",
                new IrccRemoteCommand("FootballMode", IrccRemoteCommand.IRCC, "AAAAAgAAABoAAAB2Aw=="));
        remoteCmds.put("imanual", new IrccRemoteCommand("iManual", IrccRemoteCommand.IRCC, "AAAAAgAAABoAAAB7Aw=="));
        remoteCmds.put("netflix", new IrccRemoteCommand("Netflix", IrccRemoteCommand.IRCC, "AAAAAgAAABoAAAB8Aw=="));
        remoteCmds.put("assists", new IrccRemoteCommand("Assists", IrccRemoteCommand.IRCC, "AAAAAgAAAMQAAAA7Aw=="));
        remoteCmds.put("actionmenu",
                new IrccRemoteCommand("ActionMenu", IrccRemoteCommand.IRCC, "AAAAAgAAAMQAAABLAw=="));
        remoteCmds.put("help", new IrccRemoteCommand("Help", IrccRemoteCommand.IRCC, "AAAAAgAAAMQAAABNAw=="));
        remoteCmds.put("tvsatellite",
                new IrccRemoteCommand("TvSatellite", IrccRemoteCommand.IRCC, "AAAAAgAAAMQAAABOAw=="));
        remoteCmds.put("wirelesssubwoofer",
                new IrccRemoteCommand("WirelessSubwoofer", IrccRemoteCommand.IRCC, "AAAAAgAAAMQAAAB+Aw=="));
        return remoteCmds;
    }

    /**
     * Constructs a new {@link IrccRemoteCommands} that is a merge between this object and the given
     * {@link IrccCodeList}. If the {@link IrccCodeList} is null, the current instance is returned
     *
     * @param codeList a possibly null {@link IrccCodeList}
     * @return a non-null {@link IrccRemoteCommands}
     */
    public IrccRemoteCommands withCodeList(final @Nullable IrccCodeList codeList) {
        if (codeList == null) {
            return this;
        }

        final Map<String, IrccRemoteCommand> cmds = new HashMap<>();
        cmds.putAll(remoteCmds);
        for (final IrccCode cmd : codeList.getCommands()) {
            final String cmdName = cmd.getCommand();
            final String cmdValue = cmd.getValue();

            if (StringUtils.isNotEmpty(cmdName) && StringUtils.isNotEmpty(cmdValue)) {
                final Optional<IrccRemoteCommand> existingCmd = cmds.values().stream()
                        .filter(rc -> StringUtils.equals(cmdValue, rc.getCmd())).findFirst();
                if (existingCmd.isPresent()) {
                    logger.debug("Cannot add code list {} to commands as command {} already exists for {}", cmdName,
                            cmdValue, existingCmd.get().getName());
                } else {
                    cmds.put(cmdName.toLowerCase(), new IrccRemoteCommand(cmdName, IrccRemoteCommand.IRCC, cmdValue));
                }
            }
        }

        return new IrccRemoteCommands(cmds);
    }

    /**
     * Gets the remote commands by the remote command name (lowercased)
     *
     * @return the non-null, non-empty map of remote commands
     */
    public Map<String, IrccRemoteCommand> getRemoteCommands() {
        return Collections.unmodifiableMap(remoteCmds);
    }

    /**
     * Gets the {@link IrccRemoteCommand} that represents the power on command
     *
     * @return the {@link IrccRemoteCommand} representing power on or null if not found
     */
    public @Nullable IrccRemoteCommand getPowerOn() {
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

        if (remoteCmds.containsKey("sleep")) {
            return remoteCmds.get("sleep");
        }

        // make a guess if something contains power on or power
        IrccRemoteCommand powerCmd = null;
        for (final IrccRemoteCommand cmd : remoteCmds.values()) {
            final String name = cmd.getName();
            if (StringUtils.containsIgnoreCase(name, "power on")) {
                return cmd;
            } else if (StringUtils.containsIgnoreCase(name, "power")) {
                powerCmd = cmd;
                break;
            }
        }

        return powerCmd;
    }

    /**
     * Gets the {@link IrccRemoteCommand} that represents the power off command
     *
     * @return the {@link IrccRemoteCommand} representing power off or null if not found
     */
    public @Nullable IrccRemoteCommand getPowerOff() {
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

        // make a guess if something contains power off or power
        IrccRemoteCommand powerCmd = null;
        for (final IrccRemoteCommand cmd : remoteCmds.values()) {
            final String name = cmd.getName();
            if (StringUtils.containsIgnoreCase(name, "power off")) {
                return cmd;
            } else if (StringUtils.containsIgnoreCase(name, "power")) {
                powerCmd = cmd;
                break;
            }
        }

        return powerCmd;
    }

    /**
     * The converter used to unmarshal the {@link IrccRemoteCommands}. Please note this should only be used to unmarshal
     * XML (marshaling will throw a {@link NotImplementedException})
     *
     * @author Tim Roberts - Initial contribution
     */
    @NonNullByDefault
    static class IrccRemoteCommandsConverter implements Converter {
        @Override
        public boolean canConvert(@SuppressWarnings("rawtypes") final @Nullable Class clazz) {
            return IrccRemoteCommands.class.equals(clazz);
        }

        @Override
        public void marshal(final @Nullable Object obj, final @Nullable HierarchicalStreamWriter writer,
                final @Nullable MarshallingContext context) {
            throw new NotImplementedException();
        }

        @Override
        public Object unmarshal(final @Nullable HierarchicalStreamReader reader,
                final @Nullable UnmarshallingContext context) {
            Objects.requireNonNull(reader, "reader cannot be null");
            Objects.requireNonNull(context, "context cannot be null");

            final Map<String, IrccRemoteCommand> cmds = new HashMap<>();
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                final IrccRemoteCommand cmd = (IrccRemoteCommand) context.convertAnother(this, IrccRemoteCommand.class);
                final String cmdName = cmd == null ? null : cmd.getName();

                if (cmd != null && cmdName != null) {
                    cmds.put(cmdName.toLowerCase(), cmd);
                }

                reader.moveUp();
            }
            return new IrccRemoteCommands(cmds);
        }
    }
}
