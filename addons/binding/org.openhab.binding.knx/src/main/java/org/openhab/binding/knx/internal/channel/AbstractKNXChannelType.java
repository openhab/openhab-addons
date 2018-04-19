/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.knx.KNXTypeMapper;
import org.openhab.binding.knx.client.InboundSpec;
import org.openhab.binding.knx.client.OutboundSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXFormatException;

/**
 * Abstract base class for the meta-data abstraction for the KNX channel configurations.
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractKNXChannelType {

    protected final Set<String> channelTypeIDs;

    private final Logger logger = LoggerFactory.getLogger(AbstractKNXChannelType.class);

    protected AbstractKNXChannelType(String... channelTypeIDs) {
        this.channelTypeIDs = new HashSet<>(Arrays.asList(channelTypeIDs));
    }

    @Nullable
    protected abstract ChannelConfiguration parse(@Nullable String fancy);

    public final Set<String> getChannelIDs() {
        return channelTypeIDs;
    }

    protected final Set<String> asSet(String... values) {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(values)));
    }

    protected abstract Set<@NonNull String> getAllGAKeys();

    public final Set<GroupAddress> getListenAddresses(Configuration channelConfiguration) {
        Set<GroupAddress> ret = new HashSet<>();
        for (String key : getAllGAKeys()) {
            ChannelConfiguration conf = parse((String) channelConfiguration.get(key));
            if (conf != null) {
                ret.addAll(conf.getListenGAs().stream().map(this::toGroupAddress).filter(Objects::nonNull)
                        .collect(toSet()));
            }
        }
        return ret;
    }

    public final Set<GroupAddress> getReadAddresses(Configuration channelConfiguration) {
        Set<GroupAddress> ret = new HashSet<>();
        for (String key : getAllGAKeys()) {
            ChannelConfiguration conf = parse((String) channelConfiguration.get(key));
            if (conf != null) {
                ret.addAll(
                        conf.getReadGAs().stream().map(this::toGroupAddress).filter(Objects::nonNull).collect(toSet()));
            }
        }
        return ret;
    }

    public final Set<GroupAddress> getWriteAddresses(Configuration channelConfiguration) {
        Set<GroupAddress> ret = new HashSet<>();
        for (String key : getAllGAKeys()) {
            ChannelConfiguration conf = parse((String) channelConfiguration.get(key));
            if (conf != null && conf.getMainGA() != null) {
                GroupAddress ga = toGroupAddress(conf.getMainGA());
                if (ga != null) {
                    ret.add(ga);
                }
            }
        }
        return ret;
    }

    private @Nullable GroupAddress toGroupAddress(@Nullable GroupAddressConfiguration ga) {
        if (ga != null) {
            try {
                return new GroupAddress(ga.getGA());
            } catch (KNXFormatException e) {
                logger.warn("Could not parse group address '{}'", ga.getGA());
            }
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

    public final @Nullable OutboundSpec getCommandSpec(Configuration configuration, KNXTypeMapper typeHelper,
            @Nullable Type command) throws KNXFormatException {
        for (String key : getAllGAKeys()) {
            ChannelConfiguration config = parse((String) configuration.get(key));
            if (config != null) {
                String dpt = config.getDPT();
                if (dpt == null) {
                    dpt = getDefaultDPT(key);
                }

                Class<? extends Type> expectedTypeClass = typeHelper.toTypeClass(dpt);
                if (expectedTypeClass != null) {
                    logger.trace("The expected Type class for dpt '{}' is '{}'", dpt,
                            expectedTypeClass.getSimpleName());
                    Type convertedType = convertType(command, configuration);
                    if (convertedType != null) {
                        logger.trace("The converted type for command '{}' based on config '{}' is '{}' ('{}')", command,
                                configuration.get(key), convertedType, convertedType.getClass().getSimpleName());
                        if (expectedTypeClass.isInstance(convertedType)) {
                            logger.trace("The expected Type '{}' is an instance of the converted Type '{}'",
                                    expectedTypeClass.getSimpleName(), convertedType.getClass().getSimpleName());
                            return new WriteSpecImpl(config, dpt, convertedType);
                        } else {
                            logger.trace("The expected Type '{}' is NOT an instance of the converted Type '{}'",
                                    expectedTypeClass.getSimpleName(), convertedType.getClass().getSimpleName());
                        }
                    }
                }
            }
        }
        return null;
    }

    public final List<InboundSpec> getReadSpec(Configuration configuration) throws KNXFormatException {
        return getAllGAKeys().stream()
                .map(key -> new ReadRequestSpecImpl(parse((String) configuration.get(key)), getDefaultDPT(key)))
                .filter(spec -> !spec.getGroupAddresses().isEmpty()).collect(toList());
    }

    public final @Nullable InboundSpec getListenSpec(Configuration configuration, @Nullable GroupAddress groupAddress) {
        return getAllGAKeys().stream()
                .map(key -> new ListenSpecImpl(parse((String) configuration.get(key)), getDefaultDPT(key)))
                .filter(spec -> !spec.getGroupAddresses().isEmpty())
                .filter(spec -> spec.getGroupAddresses().contains(groupAddress)).findFirst().orElse(null);
    }

    protected abstract String getDefaultDPT(String gaConfigKey);

    public final @Nullable OutboundSpec getResponseSpec(Configuration configuration, GroupAddress groupAddress,
            Type type) throws KNXFormatException {
        return getAllGAKeys().stream()
                .map(key -> new ReadResponseSpecImpl(parse((String) configuration.get(key)), getDefaultDPT(key), type))
                .filter(spec -> groupAddress.equals(spec.getGroupAddress())).findFirst().orElse(null);
    }

    protected @Nullable Type convertType(@Nullable Type type, Configuration channelConfiguration) {
        return type;
    }

    @Override
    public String toString() {
        return channelTypeIDs.toString();
    }
}
