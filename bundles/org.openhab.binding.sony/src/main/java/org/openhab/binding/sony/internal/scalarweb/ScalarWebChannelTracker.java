/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ChannelUID;

/**
 * This class will track what channels have been linked by category
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ScalarWebChannelTracker {
    /** The lock used to control access to the state */
    private final ReadWriteLock linkLock = new ReentrantReadWriteLock();

    /** The channel categories that have been linked to which web channels */
    private final Map<String, Set<ScalarWebChannel>> linkedChannelIds = new HashMap<>();

    /**
     * Notification that a channel has been linked
     *
     * @param channel the non-null channel that was linked
     */
    public void channelLinked(final ScalarWebChannel channel) {
        Objects.requireNonNull(channel, "channel cannot be null");

        final Lock writeLock = linkLock.writeLock();
        writeLock.lock();
        try {
            Set<ScalarWebChannel> channels = linkedChannelIds.get(channel.getCategory());
            if (channels == null) {
                channels = new HashSet<>();
                linkedChannelIds.put(channel.getCategory(), channels);
            }
            channels.add(channel);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Notification that a channel has been unlinked
     *
     * @param channelUID the non-null, non-empty channel UID that was unlinked
     * @return true, if successful
     */
    public boolean channelUnlinked(final ChannelUID channelUID) {
        Objects.requireNonNull(channelUID, "channelUID cannot be null");

        final Lock writeLock = linkLock.writeLock();
        writeLock.lock();
        try {
            final String channelId = channelUID.getId();

            boolean found = false;
            for (final String id : new HashSet<String>(linkedChannelIds.keySet())) {
                final Set<ScalarWebChannel> channels = linkedChannelIds.get(id);
                if (channels.removeIf(ch -> StringUtils.equalsIgnoreCase(ch.getChannelId(), channelId))) {
                    found = true;
                    if (channels.isEmpty()) {
                        linkedChannelIds.remove(id);
                    }
                    break;
                }
            }
            return found;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Checks if the specified channel has been linked
     *
     * @param channel the non-null channel to check
     * @return true, if is linked
     */
    public boolean isLinked(final ScalarWebChannel channel) {
        Objects.requireNonNull(channel, "channel cannot be null");

        final Lock readLock = linkLock.readLock();
        readLock.lock();
        try {
            final String channelId = channel.getChannelId();
            for (final Set<ScalarWebChannel> chnls : linkedChannelIds.values()) {
                for (final ScalarWebChannel chnl : chnls) {
                    if (StringUtils.equalsIgnoreCase(chnl.getChannelId(), channelId)) {
                        return true;
                    }
                }
            }
            return false;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Checks if any of the channel category has been linked (atleast one being linked will return true)
     *
     * @param categories one or more categories to check
     * @return true if linked, false otherwise
     */
    public boolean isCategoryLinked(final String... categories) {
        final Lock readLock = linkLock.readLock();
        readLock.lock();
        try {
            for (final String ctgy : categories) {
                if (linkedChannelIds.containsKey(ctgy)) {
                    return true;
                }
            }
            return false;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Checks if any of the channel category has been linked based on a filter
     *
     * @param ctgyFilter the non-null filter to use
     * @return true if linked, false otherwise
     */
    public boolean isCategoryLinked(final Parms ctgyFilter) {
        Objects.requireNonNull(ctgyFilter, "ctgyFilter cannot be null");
        final Lock readLock = linkLock.readLock();
        readLock.lock();
        try {
            for (final String ctgy : linkedChannelIds.keySet()) {
                if (ctgyFilter.isMatch(ctgy)) {
                    return true;
                }
            }
            return false;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Gets the linked channels for the given channel categories
     *
     * @param categories the categories to get the channels for
     * @return the non-null, possibly empty unmodifiable list of linked channels
     */
    public Set<ScalarWebChannel> getLinkedChannelsForCategory(final String... categories) {
        final Lock readLock = linkLock.readLock();
        readLock.lock();
        try {
            final Set<ScalarWebChannel> channels = new HashSet<>();
            for (final String ctgy : categories) {
                final Set<ScalarWebChannel> ctgyChannels = linkedChannelIds.get(ctgy);
                if (ctgyChannels != null) {
                    channels.addAll(ctgyChannels);
                }
            }
            return Collections.unmodifiableSet(channels);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Gets the linked channels for any category that passes the filter
     *
     * @param ctgyFilter the non-null filter to use
     * @return the non-null, possibly empty unmodifiable list of linked channels
     */
    public Set<ScalarWebChannel> getLinkedChannelsForCategory(final Parms ctgyFilter) {
        Objects.requireNonNull(ctgyFilter, "ctgyFilter cannot be null");
        final Lock readLock = linkLock.readLock();
        readLock.lock();
        try {
            final Set<ScalarWebChannel> channels = new HashSet<>();
            for (final Map.Entry<String, Set<ScalarWebChannel>> entry : linkedChannelIds.entrySet()) {
                if (ctgyFilter.isMatch(entry.getKey())) {
                    final Set<ScalarWebChannel> ctgyChannels = entry.getValue();
                    if (ctgyChannels != null) {
                        channels.addAll(ctgyChannels);
                    }
                }
            }
            return Collections.unmodifiableSet(channels);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Functional interface to define a category filter
     */
    @NonNullByDefault
    public interface Parms {
        /**
         * Returns true if the passed category matches
         *
         * @param ctgy a non-null category
         * @return true if matched, false if not
         */
        boolean isMatch(String ctgy);
    }
}
