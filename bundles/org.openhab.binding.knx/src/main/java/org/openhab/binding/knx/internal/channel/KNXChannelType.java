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
import static org.openhab.binding.knx.internal.KNXBindingConstants.GA;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.client.InboundSpec;
import org.openhab.binding.knx.internal.client.OutboundSpec;
import org.openhab.binding.knx.internal.dpt.KNXCoreTypeMapper;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXFormatException;

/**
 * Meta-data abstraction for the KNX channel configurations.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public abstract class KNXChannelType {

    private static final Pattern PATTERN = Pattern.compile(
            "^((?<dpt>[1-9]{0,2}\\.[0-9]{3,4}):)?(?<read>\\<)?(?<mainGA>[0-9]{1,5}(/[0-9]{1,4}){0,2})(?<listenGAs>(\\+(\\<?[0-9]{1,5}(/[0-9]{1,4}){0,2}))*)$");

    private static final Pattern PATTERN_LISTEN = Pattern
            .compile("\\+((?<read>\\<)?(?<GA>[0-9]{1,5}(/[0-9]{1,4}){0,2}))");

    private final Logger logger = LoggerFactory.getLogger(KNXChannelType.class);
    private final Set<String> channelTypeIDs;
    private final Set<String> gaKeys;

    KNXChannelType(String... channelTypeIDs) {
        this(Set.of(GA), channelTypeIDs);
    }

    KNXChannelType(Set<String> gaKeys, String... channelTypeIDs) {
        this.gaKeys = gaKeys;
        this.channelTypeIDs = Set.of(channelTypeIDs);
    }

    final Set<String> getChannelIDs() {
        return channelTypeIDs;
    }

    @Nullable
    protected final ChannelConfiguration parse(@Nullable Object fancy) {
        if (!(fancy instanceof String)) {
            return null;
        }
        Matcher matcher = PATTERN.matcher(((String) fancy).replace(" ", ""));

        if (matcher.matches()) {
            // Listen GAs
            String input = matcher.group("listenGAs");
            Matcher m2 = PATTERN_LISTEN.matcher(input);
            List<GroupAddressConfiguration> listenGAs = new LinkedList<>();
            while (m2.find()) {
                listenGAs.add(new GroupAddressConfiguration(m2.group("GA"), m2.group("read") != null));
            }

            // Main GA
            GroupAddressConfiguration mainGA = new GroupAddressConfiguration(matcher.group("mainGA"),
                    matcher.group("read") != null);

            return new ChannelConfiguration(matcher.group("dpt"), mainGA, listenGAs);
        } else {
            logger.warn("Failed parsing channel configuration '{}'.", fancy);
        }

        return null;
    }

    private Set<GroupAddress> filterGroupAddresses(Configuration channelConfiguration,
            Function<ChannelConfiguration, List<GroupAddressConfiguration>> filter) {
        return gaKeys.stream().map(channelConfiguration::get).map(this::parse).filter(Objects::nonNull)
                .map(Objects::requireNonNull).map(filter).flatMap(List::stream).map(this::toGroupAddress)
                .flatMap(Optional::stream).collect(Collectors.toSet());
    }

    public final Set<GroupAddress> getAllGroupAddresses(Configuration channelConfiguration) {
        return filterGroupAddresses(channelConfiguration, ChannelConfiguration::getListenGAs);
    }

    public final Set<GroupAddress> getWriteAddresses(Configuration channelConfiguration) {
        return filterGroupAddresses(channelConfiguration, configuration -> List.of(configuration.getMainGA()));
    }

    private Optional<GroupAddress> toGroupAddress(GroupAddressConfiguration ga) {
        try {
            return Optional.of(new GroupAddress(ga.getGA()));
        } catch (KNXFormatException e) {
            logger.warn("Could not parse group address '{}'", ga.getGA());
        }
        return Optional.empty();
    }

    public final @Nullable OutboundSpec getCommandSpec(Configuration configuration, Type command)
            throws KNXFormatException {
        logger.trace("getCommandSpec checking keys '{}' for command '{}' ({})", gaKeys, command, command.getClass());
        for (String key : gaKeys) {
            ChannelConfiguration config = parse(configuration.get(key));
            if (config != null) {
                String dpt = Objects.requireNonNullElse(config.getDPT(), getDefaultDPT(key));
                Set<Class<? extends Type>> expectedTypeClass = KNXCoreTypeMapper.getAllowedTypes(dpt);
                if (expectedTypeClass.contains(command.getClass())) {
                    logger.trace(
                            "getCommandSpec key '{}' has expectedTypeClass '{}', matching command '{}' and dpt '{}'",
                            key, expectedTypeClass, command, dpt);
                    return new WriteSpecImpl(config, dpt, command);
                }
            }
        }
        logger.trace("getCommandSpec no Spec found!");
        return null;
    }

    public final List<InboundSpec> getReadSpec(Configuration configuration) throws KNXFormatException {
        return gaKeys.stream().map(key -> new ReadRequestSpecImpl(parse(configuration.get(key)), getDefaultDPT(key)))
                .filter(spec -> !spec.getGroupAddresses().isEmpty()).collect(toList());
    }

    public final @Nullable InboundSpec getListenSpec(Configuration configuration, GroupAddress groupAddress) {
        return gaKeys.stream().map(key -> new ListenSpecImpl(parse(configuration.get(key)), getDefaultDPT(key)))
                .filter(spec -> !spec.getGroupAddresses().isEmpty())
                .filter(spec -> spec.getGroupAddresses().contains(groupAddress)).findFirst().orElse(null);
    }

    public final @Nullable OutboundSpec getResponseSpec(Configuration configuration, GroupAddress groupAddress,
            Type value) {
        return gaKeys.stream()
                .map(key -> new ReadResponseSpecImpl(parse(configuration.get(key)), getDefaultDPT(key), value))
                .filter(spec -> spec.matchesDestination(groupAddress)).findFirst().orElse(null);
    }

    protected abstract String getDefaultDPT(String gaConfigKey);

    @Override
    public String toString() {
        return channelTypeIDs.toString();
    }
}
