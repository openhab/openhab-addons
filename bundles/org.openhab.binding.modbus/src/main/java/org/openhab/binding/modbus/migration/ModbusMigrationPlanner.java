/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.modbus.migration;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** Builds a deterministic, non-mutating migration plan for legacy poller/data topology. */
@NonNullByDefault
public final class ModbusMigrationPlanner {

    public MigrationPreview preview(List<LegacyPoller> pollers, List<LegacyData> dataThings) {
        List<LegacyPoller> sortedPollers = pollers.stream().sorted(Comparator.comparing(LegacyPoller::uid)).toList();
        List<ManualMigrationWork> manualWork = new ArrayList<>();
        List<MigrationGroup> groups = new ArrayList<>();
        for (LegacyPoller poller : sortedPollers) {
            List<LegacyData> children = dataThings.stream().filter(data -> poller.uid().equals(data.pollerUid()))
                    .sorted(Comparator.comparing(LegacyData::uid)).toList();
            List<MigrationChannel> channels = new ArrayList<>();
            List<MigrationLink> links = new ArrayList<>();
            for (LegacyData data : children) {
                Optional<String> unsupportedReason = unsupportedReason(data.configuration());
                if (unsupportedReason.isPresent()) {
                    manualWork.add(new ManualMigrationWork(data.uid(), unsupportedReason.get()));
                    continue;
                }
                String channelId = data.uid().substring(data.uid().lastIndexOf(':') + 1);
                String itemType = data.links().isEmpty() ? "Number" : data.links().getFirst().itemType();
                Map<String, Object> channelConfiguration = Map.of("address", data.configuration().get("readStart"),
                        "valueType", data.configuration().get("readValueType"));
                channels.add(new MigrationChannel(data.uid(), channelId, channelConfiguration, itemType));
                String targetChannelUid = targetPollerUid(poller.uid()) + ":" + channelId;
                data.links().stream().sorted(Comparator.comparing(LegacyLink::channelUid))
                        .forEach(link -> links.add(new MigrationLink(link.itemName(), link.channelUid(), targetChannelUid)));
            }
            if (!channels.isEmpty()) {
                groups.add(new MigrationGroup(poller.uid(), targetPollerUid(poller.uid()), poller.bridgeUid(),
                        poller.configuration(), channels, links));
            }
        }
        manualWork.sort(Comparator.comparing(ManualMigrationWork::sourceUid));
        String yaml = yaml(groups, manualWork);
        String manifestJson = manifest(groups, manualWork);
        return new MigrationPreview(identity(manifestJson), groups, manualWork, yaml, manifestJson);
    }

    private static Optional<String> unsupportedReason(Map<String, Object> configuration) {
        Object writeTransform = configuration.get("writeTransform");
        if (writeTransform instanceof String value && value.contains("JSON")) {
            return Optional.of("JSON write transform requires manual migration");
        }
        if (configuration.containsKey("writeStart") || configuration.containsKey("writeType")
                || configuration.containsKey("writeValueType") || configuration.containsKey("writeTransform")) {
            return Optional.of("writes require manual migration");
        }
        Object readTransform = configuration.get("readTransform");
        if (readTransform instanceof String value && !value.isBlank() && !"default".equals(value)) {
            return Optional.of("custom readTransform requires manual migration");
        }
        if (!configuration.containsKey("readStart") || !configuration.containsKey("readValueType")) {
            return Optional.of("readStart and readValueType are required for migration");
        }
        return Optional.empty();
    }

    private static String targetPollerUid(String sourceUid) {
        return sourceUid.replace(":poller:", ":poller2:");
    }

    private static String yaml(List<MigrationGroup> groups, List<ManualMigrationWork> manualWork) {
        StringBuilder result = new StringBuilder("version: 1\nthings:\n");
        for (MigrationGroup group : groups) {
            result.append("  ").append(group.targetPollerUid()).append(":\n    type: modbus:poller2\n    bridge: ")
                    .append(group.targetBridgeUid()).append("\n    configuration:\n");
            group.configuration().entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> result.append("      ")
                    .append(entry.getKey()).append(": ").append(yamlValue(entry.getValue())).append('\n'));
            result.append("    channels:\n");
            for (MigrationChannel channel : group.channels()) {
                result.append("      ").append(channel.id()).append(":\n        itemType: ").append(channel.itemType())
                        .append("\n        configuration:\n");
                channel.configuration().entrySet().stream().sorted(Map.Entry.comparingByKey())
                        .forEach(entry -> result.append("          ").append(entry.getKey()).append(": ")
                                .append(yamlValue(entry.getValue())).append('\n'));
            }
        }
        if (!manualWork.isEmpty()) {
            result.append("# Manual work (not included above):\n");
            manualWork.forEach(work -> result.append("# - ").append(work.sourceUid()).append(": ").append(work.reason())
                    .append('\n'));
        }
        return result.toString();
    }

    private static String manifest(List<MigrationGroup> groups, List<ManualMigrationWork> manualWork) {
        List<String> mappings = new ArrayList<>();
        for (MigrationGroup group : groups) {
            for (MigrationChannel channel : group.channels()) {
                mappings.add("{\"sourceThing\":\"" + json(channel.sourceDataUid()) + "\",\"targetPoller\":\""
                        + json(group.targetPollerUid()) + "\",\"targetChannel\":\"" + json(group.targetPollerUid() + ":"
                                + channel.id())
                        + "\"}");
            }
            for (MigrationLink link : group.links()) {
                mappings.add("{\"item\":\"" + json(link.itemName()) + "\",\"sourceLink\":\""
                        + json(link.sourceChannelUid()) + "\",\"targetLink\":\"" + json(link.targetChannelUid()) + "\"}");
            }
        }
        List<String> manual = manualWork.stream().map(work -> "{\"sourceThing\":\"" + json(work.sourceUid())
                + "\",\"reason\":\"" + json(work.reason()) + "\"}").toList();
        return "{\"version\":1,\"mappings\":[" + String.join(",", mappings) + "],\"manualWork\":["
                + String.join(",", manual) + "]}";
    }

    private static String yamlValue(Object value) {
        return value instanceof Number || value instanceof Boolean ? value.toString()
                : "\"" + value.toString().replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String identity(String manifest) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(manifest.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required by the Java runtime", e);
        }
    }
}
