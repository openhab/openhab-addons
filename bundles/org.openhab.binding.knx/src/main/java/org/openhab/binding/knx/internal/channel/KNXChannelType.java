/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.KNXTypeMapper;
import org.openhab.binding.knx.internal.client.InboundSpec;
import org.openhab.binding.knx.internal.client.OutboundSpec;
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
            "^((?<dpt>[0-9]{1,3}\\.[0-9]{3,4}):)?(?<read>\\<)?(?<mainGA>[0-9]{1,5}(/[0-9]{1,4}){0,2})(?<listenGAs>(\\+(\\<?[0-9]{1,5}(/[0-9]{1,4}){0,2}))*)$");

    private static final Pattern PATTERN_LISTEN = Pattern
            .compile("\\+((?<read>\\<)?(?<GA>[0-9]{1,5}(/[0-9]{1,4}){0,2}))");

    private final Logger logger = LoggerFactory.getLogger(KNXChannelType.class);
    private final Set<String> channelTypeIDs;

    KNXChannelType(String... channelTypeIDs) {
        this.channelTypeIDs = new HashSet<>(Arrays.asList(channelTypeIDs));
    }

    final Set<String> getChannelIDs() {
        return channelTypeIDs;
    }

    @Nullable
    protected final ChannelConfiguration parse(@Nullable String fancy) {
        if (fancy == null) {
            return null;
        }
        Matcher matcher = PATTERN.matcher(fancy.replace(" ", ""));

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
        }
        return null;
    }

    protected abstract Set<String> getAllGAKeys();

    public final Set<GroupAddress> getListenAddresses(Configuration channelConfiguration) {
        Set<GroupAddress> ret = new HashSet<>();
        for (String key : getAllGAKeys()) {
            ChannelConfiguration conf = parse((String) channelConfiguration.get(key));
            if (conf != null) {
                ret.addAll(conf.getListenGAs().stream().map(this::toGroupAddress).collect(toSet()));
            }
        }
        return ret;
    }

    public final Set<GroupAddress> getReadAddresses(Configuration channelConfiguration) {
        Set<GroupAddress> ret = new HashSet<>();
        for (String key : getAllGAKeys()) {
            ChannelConfiguration conf = parse((String) channelConfiguration.get(key));
            if (conf != null) {
                ret.addAll(conf.getReadGAs().stream().map(this::toGroupAddress).collect(toSet()));
            }
        }
        return ret;
    }

    public final Set<GroupAddress> getWriteAddresses(Configuration channelConfiguration) {
        Set<GroupAddress> ret = new HashSet<>();
        for (String key : getAllGAKeys()) {
            ChannelConfiguration conf = parse((String) channelConfiguration.get(key));
            if (conf != null) {
                GroupAddress ga = toGroupAddress(conf.getMainGA());
                if (ga != null) {
                    ret.add(ga);
                }
            }
        }
        return ret;
    }

    private @Nullable GroupAddress toGroupAddress(GroupAddressConfiguration ga) {
        try {
            return new GroupAddress(ga.getGA());
        } catch (KNXFormatException e) {
            logger.warn("Could not parse group address '{}'", ga.getGA());
        }
        return null;
    }

    protected final Set<GroupAddress> getAddresses(@Nullable Configuration configuration, Iterable<String> addresses)
            throws KNXFormatException {
        Set<GroupAddress> ret = new HashSet<>();
        for (String address : addresses) {
            if (configuration != null && configuration.get(address) != null) {
                ret.add(new GroupAddress((String) configuration.get(address)));
            }
        }
        return ret;
    }

    protected final boolean isEquals(@Nullable Configuration configuration, String address, GroupAddress groupAddress)
            throws KNXFormatException {
        if (configuration != null && configuration.get(address) != null) {
            return Objects.equals(new GroupAddress((String) configuration.get(address)), groupAddress);
        }
        return false;
    }

    protected final Set<String> asSet(String... values) {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(values)));
    }

    public final @Nullable OutboundSpec getCommandSpec(Configuration configuration, KNXTypeMapper typeHelper,
            Type command) throws KNXFormatException {
        logger.trace("getCommandSpec testing Keys '{}' for command '{}'", getAllGAKeys(), command);
        for (String key : getAllGAKeys()) {
            ChannelConfiguration config = parse((String) configuration.get(key));
            if (config != null) {
                String dpt = config.getDPT();
                if (dpt == null) {
                    dpt = getDefaultDPT(key);
                }
                Class<? extends Type> expectedTypeClass = typeHelper.toTypeClass(dpt);
                if (expectedTypeClass != null) {
                    if (expectedTypeClass.isInstance(command)) {
                        logger.trace(
                                "getCommandSpec key '{}' uses expectedTypeClass '{}' witch isInstance for command '{}' and dpt '{}'",
                                key, expectedTypeClass, command, dpt);
                        return new WriteSpecImpl(config, dpt, command);
                    }
                }
            }
        }
        logger.trace("getCommandSpec no Spec found!");
        return null;
    }

    public final List<InboundSpec> getReadSpec(Configuration configuration) throws KNXFormatException {
        return getAllGAKeys().stream()
                .map(key -> new ReadRequestSpecImpl(parse((String) configuration.get(key)), getDefaultDPT(key)))
                .filter(spec -> !spec.getGroupAddresses().isEmpty()).collect(toList());
    }

    public final @Nullable InboundSpec getListenSpec(Configuration configuration, GroupAddress groupAddress) {
        Optional<ListenSpecImpl> result = getAllGAKeys().stream()
                .map(key -> new ListenSpecImpl(parse((String) configuration.get(key)), getDefaultDPT(key)))
                .filter(spec -> !spec.getGroupAddresses().isEmpty())
                .filter(spec -> spec.getGroupAddresses().contains(groupAddress)).findFirst();
        return result.isPresent() ? result.get() : null;
    }

    protected abstract String getDefaultDPT(String gaConfigKey);

    public final @Nullable OutboundSpec getResponseSpec(Configuration configuration, GroupAddress groupAddress,
            Type type) throws KNXFormatException {
        Optional<ReadResponseSpecImpl> result = getAllGAKeys().stream()
                .map(key -> new ReadResponseSpecImpl(parse((String) configuration.get(key)), getDefaultDPT(key), type))
                .filter(spec -> groupAddress.equals(spec.getGroupAddress())).findFirst();
        return result.isPresent() ? result.get() : null;
    }

    @Override
    public String toString() {
        return channelTypeIDs.toString();
    }
}
