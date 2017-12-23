/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.channel;

import static java.util.stream.Collectors.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.types.Command;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.exception.KNXFormatException;

@NonNullByDefault
public abstract class KNXChannelType {

    private static final Pattern PATTERN = Pattern.compile(
            "^((?<dpt>[0-9]{1,2}\\.[0-9]{3}):)?(?<read>\\<)?(?<mainGA>[0-9]{1,3}/[0-9]{1,3}/[0-9]{1,3})(?<listenGAs>(\\+(\\<?[0-9]{1,3}/[0-9]{1,3}/[0-9]{1,3}))*)$");

    private static final Pattern PATTERN_LISTEN = Pattern
            .compile("\\+((?<read>\\<)?(?<GA>[0-9]{1,3}/[0-9]{1,3}/[0-9]{1,3}))");

    @Nullable
    protected ChannelConfiguration parse(String fancy) {
        Matcher matcher = PATTERN.matcher(fancy);

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

    public Set<GroupAddress> getListenAddresses(Configuration channelConfiguration) {
        Set<GroupAddress> ret = new HashSet<>();
        for (String key : getAllGAKeys()) {
            ret.addAll(parse((String) channelConfiguration.get(key)).getListenGAs().stream()
                    .map(KNXChannelType::toGroupAddress).collect(toSet()));
        }
        return ret;
    }

    public Set<GroupAddress> getReadAddresses(Configuration channelConfiguration) {
        Set<GroupAddress> ret = new HashSet<>();
        for (String key : getAllGAKeys()) {
            ret.addAll(parse((String) channelConfiguration.get(key)).getReadGAs().stream()
                    .map(KNXChannelType::toGroupAddress).collect(toSet()));
        }
        return ret;
    }

    public Set<GroupAddress> getWriteAddresses(Configuration channelConfiguration) {
        Set<GroupAddress> ret = new HashSet<>();
        for (String key : getAllGAKeys()) {
            GroupAddress ga = toGroupAddress(parse((String) channelConfiguration.get(key)).getMainGA());
            if (ga != null) {
                ret.add(ga);
            }
        }
        return ret;
    }

    private static @Nullable GroupAddress toGroupAddress(GroupAddressConfiguration ga) {
        try {
            return new GroupAddress(ga.getGA());
        } catch (KNXFormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    private final String channelTypeID;

    KNXChannelType(String channelTypeID) {
        this.channelTypeID = channelTypeID;
    }

    final String getChannelID() {
        return channelTypeID;
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

    public abstract @Nullable CommandSpec getCommandSpec(Configuration configuration, Command command)
            throws KNXFormatException;

    protected final CommandSpec getDefaultCommandSpec(Configuration configuration, String key, Command command)
            throws KNXFormatException {
        return new CommandSpec(parse((String) configuration.get(key)), getDefaultDPT(key), command);
    }

    public final List<ReadSpec> getReadSpec(Configuration configuration) throws KNXFormatException {
        return getAllGAKeys().stream()
                .map(key -> new ReadSpec(parse((String) configuration.get(key)), getDefaultDPT(key)))
                .filter(spec -> !spec.getReadAddresses().isEmpty()).collect(toList());
    }

    public final @Nullable ListenSpec getListenSpec(Configuration configuration, GroupAddress groupAddress) {
        return getAllGAKeys().stream()
                .map(key -> new ListenSpec(parse((String) configuration.get(key)), getDefaultDPT(key)))
                .filter(spec -> !spec.getListenAddresses().isEmpty())
                .filter(spec -> spec.getListenAddresses().contains(groupAddress)).findFirst().orElse(null);
    }

    protected abstract String getDefaultDPT(String gaConfigKey);

    @Override
    public String toString() {
        return channelTypeID;
    }

}
