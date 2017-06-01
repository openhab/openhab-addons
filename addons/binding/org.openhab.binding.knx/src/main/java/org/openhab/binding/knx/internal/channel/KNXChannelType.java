/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.channel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.types.Type;

import com.google.common.collect.Sets;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.exception.KNXFormatException;

public class KNXChannelType {

    private final String channelTypeID;

    public KNXChannelType(String channelTypeID) {
        this.channelTypeID = channelTypeID;
    }

    public final String getChannelID() {
        return channelTypeID;
    }

    protected final Set<GroupAddress> getAddresses(Configuration configuration, Iterable<String> addresses)
            throws KNXFormatException {
        Set<GroupAddress> ret = new HashSet<>();
        for (String address : addresses) {
            if (configuration != null && configuration.get(address) != null) {
                ret.add(new GroupAddress((String) configuration.get(address)));
            }
        }
        return ret;
    }

    protected final boolean isEquals(Configuration configuration, String address, GroupAddress groupAddress)
            throws KNXFormatException {
        if (configuration != null && configuration.get(address) != null) {
            return Objects.equals(new GroupAddress((String) configuration.get(address)), groupAddress);
        }
        return false;
    }

    public String getDPT(GroupAddress groupAddress, Configuration configuration) throws KNXFormatException {
        return null;
    }

    protected Set<String> getReadAddressKeys() {
        return Collections.emptySet();
    }

    public final Set<GroupAddress> getReadAddresses(Configuration configuration) throws KNXFormatException {
        return getAddresses(configuration, getReadAddressKeys());
    }

    protected Set<String> getWriteAddressKeys(Type type) {
        return Collections.emptySet();
    }

    public final Set<GroupAddress> getWriteAddresses(Configuration configuration, Type type) throws KNXFormatException {
        return getAddresses(configuration, getWriteAddressKeys(type));
    }

    protected Set<String> getTransmitAddressKeys(Type type) {
        return Collections.emptySet();
    }

    public final Set<GroupAddress> getTransmitAddresses(Configuration configuration, Type type)
            throws KNXFormatException {
        return getAddresses(configuration, getTransmitAddressKeys(type));
    }

    protected Set<String> getUpdateAddressKeys(Type type) {
        return Collections.emptySet();
    }

    public Set<GroupAddress> getUpdateAddresses(Configuration configuration, Type type) throws KNXFormatException {
        return getAddresses(configuration, getUpdateAddressKeys(type));
    }

    protected final Set<String> asSet(String... values) {
        return Sets.newHashSet(values);
    }

    public Type convertType(Configuration configuration, Type type) {
        return type;
    }

    public boolean isSlave() {
        return false;
    }

    @Override
    public String toString() {
        return channelTypeID;
    }

}
