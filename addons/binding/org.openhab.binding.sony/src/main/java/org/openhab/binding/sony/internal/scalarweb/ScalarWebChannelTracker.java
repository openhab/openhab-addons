/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang.StringUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class ScalarWebChannelTracker.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class ScalarWebChannelTracker {

    /** The link lock. */
    // private Logger logger = LoggerFactory.getLogger(ScalarWebChannelTracker.class);
    private final ReadWriteLock linkLock = new ReentrantReadWriteLock();

    /** The channels. */
    private final Set<ScalarWebChannel> channels = new HashSet<ScalarWebChannel>();

    /** The channel ids. */
    private final Set<String> channelIds = new HashSet<String>();

    /**
     * Channel linked.
     *
     * @param channel the channel
     */
    public void channelLinked(ScalarWebChannel channel) {
        final Lock writeLock = linkLock.writeLock();
        writeLock.lock();
        try {
            channels.add(channel);
            channelIds.add(StringUtils.lowerCase(channel.getId()));
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Channel unlinked.
     *
     * @param channel the channel
     * @return true, if successful
     */
    public boolean channelUnlinked(ScalarWebChannel channel) {
        final Lock writeLock = linkLock.writeLock();
        writeLock.lock();
        try {
            final boolean found = channels.remove(channel);
            final String channelId = StringUtils.lowerCase(channel.getId());

            boolean otherIdFound = false;
            for (ScalarWebChannel ch : channels) {
                if (StringUtils.equals(ch.getId(), channelId)) {
                    otherIdFound = true;
                    break;
                }
            }

            if (!otherIdFound) {
                channelIds.remove(channelId);
            }

            return found;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Checks if is linked.
     *
     * @param channel the channel
     * @return true, if is linked
     */
    public boolean isLinked(ScalarWebChannel channel) {
        final Lock readLock = linkLock.readLock();
        readLock.lock();
        try {
            return channels.contains(channel);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Checks if is id linked.
     *
     * @param ids the ids
     * @return true, if is id linked
     */
    public boolean isIdLinked(String... ids) {
        final Lock readLock = linkLock.readLock();
        readLock.lock();
        try {
            for (String id : ids) {
                if (channelIds.contains(StringUtils.lowerCase(id))) {
                    return true;
                }
            }
            return false;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Gets the linked channels for id.
     *
     * @param ids the ids
     * @return the linked channels for id
     */
    public List<ScalarWebChannel> getLinkedChannelsForId(String... ids) {
        final List<ScalarWebChannel> foundChannels = new ArrayList<ScalarWebChannel>();
        final Lock readLock = linkLock.readLock();
        readLock.lock();
        try {

            for (ScalarWebChannel channel : channels) {
                for (String id : ids) {

                    if (StringUtils.equalsIgnoreCase(channel.getId(), id)) {
                        foundChannels.add(channel);
                    }
                }
            }
        } finally {
            readLock.unlock();
        }
        return foundChannels;
    }
}
