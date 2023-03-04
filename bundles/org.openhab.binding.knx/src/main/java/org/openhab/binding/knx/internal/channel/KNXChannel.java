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
package org.openhab.binding.knx.internal.channel;

import static java.util.stream.Collectors.*;
import static org.openhab.binding.knx.internal.KNXBindingConstants.CONTROL_CHANNEL_TYPES;
import static org.openhab.binding.knx.internal.KNXBindingConstants.GA;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.client.InboundSpec;
import org.openhab.binding.knx.internal.client.OutboundSpec;
import org.openhab.binding.knx.internal.dpt.KNXCoreTypeMapper;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.GroupAddress;

/**
 * Meta-data abstraction for the KNX channel configurations.
 *
 * @author Simon Kaufmann - initial contribution and API
 * @author Jan N. Klug - refactored from type definition to channel instance
 *
 */
@NonNullByDefault
public abstract class KNXChannel {
    private final Logger logger = LoggerFactory.getLogger(KNXChannel.class);
    private final Set<String> gaKeys;

    private final Map<String, GroupAddressConfiguration> groupAddressConfigurations = new HashMap<>();
    private final Set<GroupAddress> listenAddresses = new HashSet<>();
    private final Set<GroupAddress> writeAddresses = new HashSet<>();
    private final String channelType;
    private final ChannelUID channelUID;
    private final boolean isControl;

    KNXChannel(Channel channel) {
        this(Set.of(GA), channel);
    }

    private static final Map<String, Set<Class<? extends Type>>> ACCEPTED_TYPES = Map.ofEntries( //
            Map.entry("Switch", Set.of(OnOffType.class)), //
            Map.entry("Dimmer", Set.of(OnOffType.class, PercentType.class, IncreaseDecreaseType.class)), //
            Map.entry("Contact", Set.of(OpenClosedType.class)), //
            Map.entry("Color", Set.of(OnOffType.class, PercentType.class, HSBType.class, IncreaseDecreaseType.class)), //
            Map.entry("DateTime", Set.of(DateTimeType.class)), //
            Map.entry("Number", Set.of(DecimalType.class, QuantityType.class)), //
            Map.entry("Rollershutter", Set.of(PercentType.class, UpDownType.class, StopMoveType.class)), //
            Map.entry("String", Set.of(StringType.class)));

    KNXChannel(Set<String> gaKeys, Channel channel) {
        this.gaKeys = gaKeys;

        // this is safe because we already checked the presence of the ChannelTypeUID before
        this.channelType = Objects.requireNonNull(channel.getChannelTypeUID()).getId();
        this.channelUID = channel.getUID();
        this.isControl = CONTROL_CHANNEL_TYPES.contains(channelType);

        // build map of ChannelConfigurations and GA lists
        Configuration configuration = channel.getConfiguration();
        gaKeys.forEach(key -> {
            GroupAddressConfiguration groupAddressConfiguration = GroupAddressConfiguration
                    .parse(configuration.get(key));
            if (groupAddressConfiguration != null) {
                // check DPT configuration (if set) is compatible with item
                String dpt = groupAddressConfiguration.getDPT();
                if (dpt != null) {
                    Set<Class<? extends Type>> types = KNXCoreTypeMapper.getAllowedTypes(dpt);
                    String itemType = Objects.requireNonNull(channel.getAcceptedItemType());
                    if (itemType.contains(":")) {
                        itemType = itemType.substring(0, itemType.indexOf(":"));
                    }
                    Set<Class<? extends Type>> channelTypes = ACCEPTED_TYPES.get(itemType);
                    if (channelTypes == null) {
                        logger.debug(
                                "Could not determine accepted types for channel '{}', assuming DPT assignment is ok.",
                                channelUID);
                    } else if (!channelTypes.containsAll(types)) {
                        logger.warn("Configured DPT '{}' is incompatible with accepted item type '{}' for channel '{}'",
                                dpt, itemType, channelUID);
                    }
                }
                groupAddressConfigurations.put(key, groupAddressConfiguration);
                // store address configuration for re-use
                listenAddresses.addAll(groupAddressConfiguration.getListenGAs());
                writeAddresses.add(groupAddressConfiguration.getMainGA());
            }
        });
    }

    public String getChannelType() {
        return channelType;
    }

    public ChannelUID getChannelUID() {
        return channelUID;
    }

    public boolean isControl() {
        return isControl;
    }

    public final Set<GroupAddress> getAllGroupAddresses() {
        return listenAddresses;
    }

    public final Set<GroupAddress> getWriteAddresses() {
        return writeAddresses;
    }

    public final @Nullable OutboundSpec getCommandSpec(Type command) {
        logger.trace("getCommandSpec checking keys '{}' for command '{}' ({})", gaKeys, command, command.getClass());
        for (Map.Entry<String, GroupAddressConfiguration> entry : groupAddressConfigurations.entrySet()) {
            String dpt = Objects.requireNonNullElse(entry.getValue().getDPT(), getDefaultDPT(entry.getKey()));
            Set<Class<? extends Type>> expectedTypeClass = KNXCoreTypeMapper.getAllowedTypes(dpt);
            if (expectedTypeClass.contains(command.getClass())) {
                logger.trace("getCommandSpec key '{}' has expectedTypeClass '{}', matching command '{}' and dpt '{}'",
                        entry.getKey(), expectedTypeClass, command, dpt);
                return new WriteSpecImpl(entry.getValue(), dpt, command);
            }
        }
        logger.trace("getCommandSpec no Spec found!");
        return null;
    }

    public final List<InboundSpec> getReadSpec() {
        return groupAddressConfigurations.entrySet().stream()
                .map(entry -> new ReadRequestSpecImpl(entry.getValue(), getDefaultDPT(entry.getKey())))
                .filter(spec -> !spec.getGroupAddresses().isEmpty()).collect(toList());
    }

    public final @Nullable InboundSpec getListenSpec(GroupAddress groupAddress) {
        return groupAddressConfigurations.entrySet().stream()
                .map(entry -> new ListenSpecImpl(entry.getValue(), getDefaultDPT(entry.getKey())))
                .filter(spec -> spec.getGroupAddresses().contains(groupAddress)).findFirst().orElse(null);
    }

    public final @Nullable OutboundSpec getResponseSpec(GroupAddress groupAddress, Type value) {
        return groupAddressConfigurations.entrySet().stream()
                .map(entry -> new ReadResponseSpecImpl(entry.getValue(), getDefaultDPT(entry.getKey()), value))
                .filter(spec -> spec.matchesDestination(groupAddress)).findFirst().orElse(null);
    }

    protected abstract String getDefaultDPT(String gaConfigKey);
}
