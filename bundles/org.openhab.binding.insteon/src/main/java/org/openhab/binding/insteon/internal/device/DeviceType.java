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
package org.openhab.binding.insteon.internal.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.transport.message.FieldException;
import org.openhab.binding.insteon.internal.transport.message.InvalidMessageTypeException;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.openhab.binding.insteon.internal.utils.HexUtils;

/**
 * The {@link DeviceType} represents a device type
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class DeviceType {
    private String name;
    private Map<String, Boolean> flags = new HashMap<>();
    private Map<String, FeatureEntry> features = new LinkedHashMap<>();
    private Map<String, DefaultLinkEntry> links = new LinkedHashMap<>();

    /**
     * Constructor
     *
     * @param name the name for this device type
     * @param flags the flags for this device type
     * @param features the features for this device type
     * @param links the default links for this device type
     */
    public DeviceType(String name, Map<String, Boolean> flags, Map<String, FeatureEntry> features,
            Map<String, DefaultLinkEntry> links) {
        this.name = name;
        this.flags = flags;
        this.features = features;
        this.links = links;
    }

    /**
     * Returns name
     *
     * @return the name for this device type
     */
    public String getName() {
        return name;
    }

    /**
     * Returns flags
     *
     * @return all flags for this device type
     */
    public Map<String, Boolean> getFlags() {
        return flags;
    }

    /**
     * Returns supported features
     *
     * @return all features that this device type supports
     */
    public List<FeatureEntry> getFeatures() {
        return features.values().stream().toList();
    }

    /**
     * Returns supported feature groups
     *
     * @return all feature groups that this device type supports
     */
    public List<FeatureEntry> getFeatureGroups() {
        return features.values().stream().filter(FeatureEntry::hasConnectedFeatures).toList();
    }

    /**
     * Returns default links
     *
     * @return all default links for this device type
     */
    public Map<String, DefaultLinkEntry> getDefaultLinks() {
        return links;
    }

    @Override
    public String toString() {
        String s = "name:" + name;
        if (!features.isEmpty()) {
            s += "|features:" + features.values().stream().map(FeatureEntry::toString).collect(Collectors.joining(","));
        }
        if (!flags.isEmpty()) {
            s += "|flags:" + flags.entrySet().stream().map(Entry::toString).collect(Collectors.joining(","));
        }
        if (!links.isEmpty()) {
            s += "|default-links:"
                    + links.values().stream().map(DefaultLinkEntry::toString).collect(Collectors.joining(","));
        }
        return s;
    }

    /**
     * Class that reflects a feature entry
     */
    public static class FeatureEntry {
        private String name;
        private String type;
        private Map<String, String> parameters;
        private List<String> connectedFeatures = new ArrayList<>();

        public FeatureEntry(String name, String type, Map<String, String> parameters) {
            this.name = name;
            this.type = type;
            this.parameters = parameters;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        public List<String> getConnectedFeatures() {
            return connectedFeatures;
        }

        public boolean hasConnectedFeatures() {
            return !connectedFeatures.isEmpty();
        }

        public void addConnectedFeature(String name) {
            connectedFeatures.add(name);
        }

        @Override
        public String toString() {
            String s = name + "->" + type;
            if (!connectedFeatures.isEmpty()) {
                s += "|connectedFeatures:" + connectedFeatures;
            }
            return s;
        }
    }

    /**
     * Class that reflects a default link entry
     */
    public static class DefaultLinkEntry {
        private String name;
        private boolean controller;
        private int group;
        private byte[] data;
        private List<CommandEntry> commands = new ArrayList<>();

        public DefaultLinkEntry(String name, boolean controller, int group, byte[] data) {
            this.name = name;
            this.controller = controller;
            this.group = group;
            this.data = data;
        }

        public boolean isController() {
            return controller;
        }

        public int getGroup() {
            return group;
        }

        public byte[] getData() {
            return data;
        }

        public List<CommandEntry> getCommands() {
            return commands;
        }

        public void addCommand(CommandEntry command) {
            commands.add(command);
        }

        @Override
        public String toString() {
            String s = name + "->";
            s += controller ? "CTRL" : "RESP";
            s += "|group:" + group;
            s += "|data1:" + HexUtils.getHexString(data[0]);
            s += "|data2:" + HexUtils.getHexString(data[1]);
            s += "|data3:" + HexUtils.getHexString(data[2]);
            if (!commands.isEmpty()) {
                s += "|commands:" + commands;
            }
            return s;
        }
    }

    /**
     * Class that reflects a command entry
     */
    public static class CommandEntry {
        private String name;
        private int ext;
        private byte cmd1;
        private byte cmd2;
        private byte[] data;

        public CommandEntry(String name, int ext, byte cmd1, byte cmd2, byte[] data) {
            this.name = name;
            this.ext = ext;
            this.cmd1 = cmd1;
            this.cmd2 = cmd2;
            this.data = data;
        }

        public @Nullable Msg getMessage(InsteonDevice device) {
            try {
                if (ext == 0) {
                    return Msg.makeStandardMessage(device.getAddress(), cmd1, cmd2);
                } else if (ext == 1) {
                    return Msg.makeExtendedMessage(device.getAddress(), cmd1, cmd2, data,
                            device.getInsteonEngine().supportsChecksum());
                } else if (ext == 2) {
                    return Msg.makeExtendedMessageCRC2(device.getAddress(), cmd1, cmd2, data);
                }
            } catch (FieldException | InvalidMessageTypeException e) {
            }
            return null;
        }

        @Override
        public String toString() {
            String s = name + "->";
            s += "ext:" + ext;
            s += "|cmd1:" + HexUtils.getHexString(cmd1);
            s += "|cmd2:" + HexUtils.getHexString(cmd2);
            s += "|data1:" + HexUtils.getHexString(data[0]);
            s += "|data2:" + HexUtils.getHexString(data[1]);
            s += "|data3:" + HexUtils.getHexString(data[2]);
            return s;
        }
    }
}
