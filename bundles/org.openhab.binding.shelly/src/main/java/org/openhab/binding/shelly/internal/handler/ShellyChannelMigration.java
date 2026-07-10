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
package org.openhab.binding.shelly.internal.handler;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.provider.ShellyChannelDefinitions;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ShellyChannelMigration} migrates legacy channel definitions of existing Things to the
 * current channel schema: renamed channels get their replacement created next to them and channels
 * with changed metadata are refreshed in place.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyChannelMigration {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShellyChannelMigration.class);

    private record ChannelMigrationRule(int version, String channelId, @Nullable String replacementChannelId,
            boolean refreshExistingChannel) {
    }

    private static final List<ChannelMigrationRule> CHANNEL_MIGRATION_RULES = List.of(
            new ChannelMigrationRule(5, mkWildcardChannelId(CHANNEL_GROUP_METER, CHANNEL_METER_CURRENTWATTS),
                    CHANNEL_METER_CURRENTPOWER, true),
            new ChannelMigrationRule(5, mkWildcardChannelId(CHANNEL_GROUP_METER, CHANNEL_METER_TOTALKWH),
                    CHANNEL_METER_TOTALENERGY, true),
            new ChannelMigrationRule(5, mkWildcardChannelId(CHANNEL_GROUP_METER, CHANNEL_EMETER_TOTALRET),
                    CHANNEL_EMETER_RETURNEDENERGY, true),
            new ChannelMigrationRule(5, mkWildcardChannelId(CHANNEL_GROUP_METER, CHANNEL_EMETER_REACTWATTS),
                    CHANNEL_EMETER_REACTPOWER, false),
            new ChannelMigrationRule(5, mkWildcardChannelId(CHANNEL_GROUP_METER, CHANNEL_METER_LASTMIN1),
                    CHANNEL_METER_ENERGYAVG1MIN, false),
            new ChannelMigrationRule(5, mkChannelId(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ACCUWATTS),
                    CHANNEL_DEVST_ACCUMULATEDPOWER, true),
            new ChannelMigrationRule(5, mkChannelId(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ACCUTOTAL),
                    CHANNEL_DEVST_TOTALENERGY, false),
            new ChannelMigrationRule(5, mkChannelId(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ACCURETURNED),
                    CHANNEL_DEVST_ACCURETURNEDENERGY, false),
            new ChannelMigrationRule(5, mkChannelId(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_TOTALKWH),
                    CHANNEL_DEVST_TOTALENERGY, false),
            new ChannelMigrationRule(5, mkChannelId(CHANNEL_GROUP_NMETER, CHANNEL_NMETER_MTRESHHOLD),
                    CHANNEL_NMETER_THRESHOLD, false),
            new ChannelMigrationRule(5, mkWildcardChannelId(CHANNEL_GROUP_RELAY_CONTROL, CHANNEL_OUTPUT), null, true));

    public static final int CHANNEL_SCHEMA_VERSION = CHANNEL_MIGRATION_RULES.stream()
            .mapToInt(ChannelMigrationRule::version).max().orElse(0);

    public static void migrateChannels(ShellyThingInterface thing) {
        int currentVersion = getChannelSchemaVersion(thing);
        if (currentVersion >= CHANNEL_SCHEMA_VERSION) {
            return;
        }

        boolean updated = false;
        for (ChannelMigrationRule rule : CHANNEL_MIGRATION_RULES) {
            if (currentVersion < rule.version()) {
                updated |= applyMigrationRule(thing, rule);
            }
        }
        if (updated) {
            thing.updateProperties(PROPERTY_CHANNEL_SCHEMA_VERSION, String.valueOf(CHANNEL_SCHEMA_VERSION));
            LOGGER.debug("{}: Channel definitions migrated to schema version {}", thing.getThingName(),
                    CHANNEL_SCHEMA_VERSION);
        } else if (!thing.getThing().getChannels().isEmpty()) {
            // Thing already has channels but none matched a rule (already on the current schema);
            // stamp the version anyway so this scan is skipped on every subsequent poll.
            thing.updateProperties(PROPERTY_CHANNEL_SCHEMA_VERSION, String.valueOf(CHANNEL_SCHEMA_VERSION));
        }
    }

    private static int getChannelSchemaVersion(ShellyThingInterface thing) {
        try {
            return Integer.parseInt(thing.getProperty(PROPERTY_CHANNEL_SCHEMA_VERSION));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static boolean applyMigrationRule(ShellyThingInterface thing, ChannelMigrationRule rule) {
        String thingName = thing.getThingName();
        List<Channel> existingChannels = thing.getThing().getChannels();
        List<Channel> matchingChannels = findChannels(existingChannels, rule.channelId());
        String replacementChannelName = rule.replacementChannelId();
        if (matchingChannels.isEmpty() && (replacementChannelName == null || replacementChannelName.isEmpty())) {
            return false;
        }

        Map<String, Channel> channelUpdates = new HashMap<>();
        Map<String, Channel> newOrReplacementChannels = new HashMap<>();

        for (Channel channel : matchingChannels) {
            String fullExistingId = channel.getUID().getId();
            int sep = fullExistingId.indexOf(ChannelUID.CHANNEL_GROUP_SEPARATOR);
            String groupPrefix = sep > 0 ? fullExistingId.substring(0, sep + 1) : "";

            if (rule.refreshExistingChannel()) {
                try {
                    Channel updatedChannel = ShellyChannelDefinitions.createChannel(thing.getThing(), fullExistingId);
                    if (updatedChannel != null && !getString(updatedChannel.getDescription())
                            .equals(getString(channel.getDescription()))) {
                        channelUpdates.put(fullExistingId, updatedChannel);
                    }
                } catch (IllegalArgumentException e) {
                    LOGGER.debug("{}: Cannot refresh channel definition for {}", thingName, fullExistingId, e);
                }
            }

            if (!groupPrefix.isEmpty()) {
                String newChannelId = (replacementChannelName != null && !replacementChannelName.isEmpty())
                        ? groupPrefix + replacementChannelName
                        : fullExistingId;
                if (findChannel(existingChannels, newChannelId) == null) {
                    try {
                        Channel newChannel = ShellyChannelDefinitions.createChannel(thing.getThing(), newChannelId);
                        if (newChannel != null) {
                            newOrReplacementChannels.put(newChannelId, newChannel);
                        }
                    } catch (IllegalArgumentException e) {
                        LOGGER.debug("{}: Cannot create replacement channel {}", thingName, newChannelId, e);
                    }
                }
            }
        }

        return thing.updateThingChannels(channelUpdates, newOrReplacementChannels);
    }

    /**
     * A rule's channelId is either "group#name" (matches that exact channel only) or
     * "groupPrefix*#name" (matches every channel whose group starts with groupPrefix and whose
     * name equals name — used for indexed groups: meter, meter1, meter2, ...). Every rule must use
     * one of these two explicit forms; there is no implicit bare-name/any-group matching.
     */
    static List<Channel> findChannels(List<Channel> channels, String channelId) {
        int sep = channelId.indexOf(ChannelUID.CHANNEL_GROUP_SEPARATOR);
        if (sep <= 0) {
            return List.of();
        }
        String channelName = channelId.substring(sep + 1);
        boolean wildcard = channelId.charAt(sep - 1) == '*';
        String groupPrefix = channelId.substring(0, wildcard ? sep - 1 : sep);

        List<Channel> result = new ArrayList<>();
        for (Channel channel : channels) {
            ChannelUID uid = channel.getUID();
            String fullId = uid.getId();
            int idSep = fullId.indexOf(ChannelUID.CHANNEL_GROUP_SEPARATOR);
            String group = idSep > 0 ? fullId.substring(0, idSep) : "";
            boolean groupMatches = wildcard ? group.startsWith(groupPrefix) : group.equals(groupPrefix);
            if (groupMatches && uid.getIdWithoutGroup().equals(channelName)) {
                result.add(channel);
            }
        }
        return result;
    }

    static @Nullable Channel findChannel(List<Channel> channels, String channelId) {
        List<Channel> matches = findChannels(channels, channelId);
        return matches.isEmpty() ? null : matches.get(0);
    }
}
