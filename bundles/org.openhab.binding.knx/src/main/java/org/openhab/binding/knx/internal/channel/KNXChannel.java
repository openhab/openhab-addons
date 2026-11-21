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
package org.openhab.binding.knx.internal.channel;

import static java.util.stream.Collectors.toList;
import static org.openhab.binding.knx.internal.KNXBindingConstants.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.client.InboundSpec;
import org.openhab.binding.knx.internal.client.OutboundSpec;
import org.openhab.binding.knx.internal.dpt.DPTUtil;
import org.openhab.binding.knx.internal.project.KnxConfiguredAddress;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.calimero.GroupAddress;

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
    private final List<String> gaKeys;

    private final Map<String, GroupAddressConfiguration> groupAddressConfigurations = new LinkedHashMap<>();
    private final List<GroupAddress> listenAddresses = new ArrayList<>();
    private final List<GroupAddress> writeAddresses = new ArrayList<>();
    private final String channelType;
    private final ChannelUID channelUID;
    private final boolean isControl;
    private final boolean isTrigger;
    private final Class<? extends Type> preferredType;

    KNXChannel(List<Class<? extends Type>> acceptedTypes, Channel channel) {
        this(List.of(GA), acceptedTypes, channel);
    }

    KNXChannel(List<String> gaKeys, List<Class<? extends Type>> acceptedTypes, Channel channel) {
        this.gaKeys = gaKeys;
        this.preferredType = acceptedTypes.getFirst();

        // this is safe because we already checked the presence of the ChannelTypeUID before
        this.channelType = Objects.requireNonNull(channel.getChannelTypeUID()).getId();
        this.channelUID = channel.getUID();
        this.isControl = CONTROL_CHANNEL_TYPES.contains(channelType);
        this.isTrigger = CHANNEL_TELEGRAM_TRIGGER.equals(channelType);

        // build map of ChannelConfigurations and GA lists
        Configuration configuration = channel.getConfiguration();
        gaKeys.forEach(key -> {
            GroupAddressConfiguration groupAddressConfiguration = GroupAddressConfiguration
                    .parse(configuration.get(key));
            if (groupAddressConfiguration != null) {
                // check DPT configuration (if set) is compatible with item
                String dpt = groupAddressConfiguration.getDPT();
                if (dpt != null) {
                    Set<Class<? extends Type>> types = DPTUtil.getAllowedTypes(dpt);
                    if (acceptedTypes.stream().noneMatch(types::contains)) {
                        logger.warn("Configured DPT '{}' is incompatible with accepted types '{}' for channel '{}'",
                                dpt, acceptedTypes, channelUID);
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

    /** {@return true for a channel that only fires events: it never reads, sends or answers} */
    public boolean isTrigger() {
        return isTrigger;
    }

    public Class<? extends Type> preferredType() {
        return preferredType;
    }

    public final List<GroupAddress> getAllGroupAddresses() {
        return listenAddresses;
    }

    public final List<GroupAddress> getWriteAddresses() {
        return writeAddresses;
    }

    public final @Nullable OutboundSpec getCommandSpec(Type command) {
        logger.trace("getCommandSpec checking keys '{}' for command '{}' ({})", gaKeys, command, command.getClass());
        // first check if there is a direct match for the provided command for all GAs
        for (Map.Entry<String, GroupAddressConfiguration> entry : groupAddressConfigurations.entrySet()) {
            String dpt = Objects.requireNonNullElse(entry.getValue().getDPT(), getDefaultDPT(entry.getKey()));
            Set<Class<? extends Type>> expectedTypeClasses = DPTUtil.getAllowedTypes(dpt);
            // find the first matching type that is assignable from the command
            if (expectedTypeClasses.contains(command.getClass())) {
                logger.trace(
                        "getCommandSpec key '{}' has one of the expectedTypeClasses '{}', matching command '{}' and dpt '{}'",
                        entry.getKey(), expectedTypeClasses, command, dpt);
                return new WriteSpecImpl(entry.getValue(), dpt, command);
            }
        }
        // if we didn't find a match, check if we find a subtype match
        for (Map.Entry<String, GroupAddressConfiguration> entry : groupAddressConfigurations.entrySet()) {
            String dpt = Objects.requireNonNullElse(entry.getValue().getDPT(), getDefaultDPT(entry.getKey()));
            Set<Class<? extends Type>> expectedTypeClasses = DPTUtil.getAllowedTypes(dpt);
            for (Class<? extends Type> expectedTypeClass : expectedTypeClasses) {
                if (command instanceof State state && State.class.isAssignableFrom(expectedTypeClass)) {
                    var subClass = expectedTypeClass.asSubclass(State.class);
                    if (state.as(subClass) != null) {
                        logger.trace(
                                "getCommandSpec command class '{}' is a sub-class of the expectedTypeClass '{}' for key '{}'",
                                command.getClass(), expectedTypeClass, entry.getKey());
                        Class<? extends State> expectedTypeAsStateClass = expectedTypeClass.asSubclass(State.class);
                        State convertedState = state.as(expectedTypeAsStateClass);
                        if (convertedState != null) {
                            return new WriteSpecImpl(entry.getValue(), dpt, convertedState);
                        }
                    }
                }
            }
        }
        logger.trace(
                "getCommandSpec could not match command class '{}' with expectedTypeClasses for any of the checked keys '{}', discarding command",
                command.getClass(), gaKeys);
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

    /**
     * {@return per configured key the main address, then its other listening addresses in address order, each
     * once; the type is the configured one or the key's default}
     */
    public final List<KnxConfiguredAddress> configuredAddresses() {
        List<KnxConfiguredAddress> addresses = new ArrayList<>();
        for (Map.Entry<String, GroupAddressConfiguration> entry : groupAddressConfigurations.entrySet()) {
            GroupAddressConfiguration configuration = entry.getValue();
            String dpt = Objects.requireNonNullElse(configuration.getDPT(), getDefaultDPT(entry.getKey()));
            String label = getChannelUID() + " " + entry.getKey();
            GroupAddress main = configuration.getMainGA();
            GroupAddress first = configuration.getFirstListenGA();
            GroupAddress status = main.equals(first) ? null : first;
            List<GroupAddress> listed = new ArrayList<>(List.of(main));
            configuration.getListenGAs().stream().filter(address -> !address.equals(main))
                    .sorted(Comparator.comparingInt(GroupAddress::getRawAddress)).forEach(listed::add);
            for (GroupAddress address : listed) {
                addresses.add(new KnxConfiguredAddress(address.getRawAddress(), dpt,
                        configuration.getReadGAs().contains(address), address.equals(main), address.equals(status),
                        label));
            }
        }
        return List.copyOf(addresses);
    }

    protected abstract String getDefaultDPT(String gaConfigKey);
}
