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
package org.openhab.binding.dmx.internal.multiverse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Universe} represents a single DMX universes with all its channels and provides a buffer for sending by the
 * bridges
 *
 * @author Jan N. Klug - Initial contribution
 */
public class Universe {
    public static final int MIN_UNIVERSE_SIZE = 32;
    public static final int MAX_UNIVERSE_SIZE = 512;
    public static final int DEFAULT_REFRESH_TIME = 1000;

    private final Logger logger = LoggerFactory.getLogger(Universe.class);
    private final ReentrantLock universeLock = new ReentrantLock();

    private int universeId;
    private int bufferSize = MIN_UNIVERSE_SIZE;

    private final short[] buffer = new short[MAX_UNIVERSE_SIZE];
    private final short[] cie1931Curve = new short[DmxChannel.MAX_VALUE << 8 + 1];

    private long bufferChanged;
    private int refreshTime = DEFAULT_REFRESH_TIME;

    private final List<DmxChannel> channels = new ArrayList<>();
    private final List<Integer> applyCurve = new ArrayList<>();

    /**
     * universe constructor
     *
     * @param universeId the universe id
     */
    public Universe(int universeId) {
        this.universeId = universeId;
        fillDimCurveLookupTable();
    }

    /**
     * universe constructor with default universe 1
     */
    public Universe() {
        this(1);
    }

    /**
     * register a channel in the universe, create if not existing
     *
     * @param channel the channel represented by a {@link BaseDmxChannel} object
     * @param thing the thing to register this channel to
     * @return a full featured channel
     */
    public synchronized DmxChannel registerChannel(BaseDmxChannel baseChannel, Thing thing) {
        for (DmxChannel channel : channels) {
            if (baseChannel.compareTo(channel) == 0) {
                logger.trace("returning existing channel {}", channel);
                channel.registerThing(thing);
                return channel;
            }
        }
        DmxChannel channel = new DmxChannel(baseChannel, refreshTime);
        addChannel(channel);
        channel.registerThing(thing);
        logger.debug("creating and returning channel {}", channel);
        return channel;
    }

    /**
     * unregister thing from a channel (deletes channel if not used anymore)
     *
     * @param thing the thing to unregister
     */
    public synchronized void unregisterChannels(Thing thing) {
        universeLock.lock();
        try {
            Iterator<DmxChannel> channelIterator = channels.iterator();

            while (channelIterator.hasNext()) {
                DmxChannel channel = channelIterator.next();
                channel.unregisterThing(thing);
                if (!channel.hasRegisteredThings()) {
                    channelIterator.remove();
                    logger.trace("Removing channel {}, no more things", channel);
                }
            }
        } finally {
            universeLock.unlock();
        }
    }

    /**
     * add an existing channel to this universe
     *
     * @param channel a {@link DmxChannel} object within this universe
     */
    private void addChannel(DmxChannel channel) throws IllegalArgumentException {
        if (universeId == channel.getUniverseId()) {
            universeLock.lock();
            try {
                channels.add(channel);
                if (channel.getChannelId() > bufferSize) {
                    bufferSize = channel.getChannelId();
                }
            } finally {
                universeLock.unlock();
            }
        } else {
            throw new IllegalArgumentException(
                    String.format("Adding channel %s to universe %d not possible", channel.toString(), universeId));
        }
    }

    /**
     * get the timestamp of the last buffer change
     *
     * @return timestamp
     */
    public long getLastBufferChanged() {
        return bufferChanged;
    }

    /**
     * get size of the buffer
     *
     * @return value between {@link MIN_UNIVERSE_SIZE} and 512
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * get universe id
     *
     * @return this universe DMX id
     */
    public int getUniverseId() {
        return universeId;
    }

    /**
     * change universe id
     *
     * @param universeId new universe id
     */
    public void rename(int universeId) {
        logger.debug("Renaming universe {} to {}", this.universeId, universeId);
        this.universeId = universeId;
        for (DmxChannel channel : channels) {
            channel.setUniverseId(universeId);
        }
    }

    /**
     * calculate this universe buffer (run all channel actions) for a given time
     *
     * @param time the timestamp used for calculation
     */
    public void calculateBuffer(long time) {
        universeLock.lock();
        try {
            for (DmxChannel channel : channels) {
                logger.trace("calculating new value for {}", channel);
                int channelId = channel.getChannelId();
                int vx = channel.getNewHiResValue(time);
                int value;
                if (applyCurve.contains(channelId)) {
                    value = cie1931Curve[vx];
                } else {
                    value = vx >> 8;
                }
                if (buffer[channelId - 1] != value) {
                    buffer[channelId - 1] = (short) value;
                    bufferChanged = time;
                }
            }
        } finally {
            universeLock.unlock();
        }
    }

    /**
     * get the full universe buffer
     *
     * @return byte array with channel values
     */
    public byte[] getBuffer() {
        byte[] b = new byte[bufferSize];
        universeLock.lock();
        try {
            for (int i = 0; i < bufferSize; i++) {
                b[i] = (byte) buffer[i];
            }
        } finally {
            universeLock.unlock();
        }
        return b;
    }

    /**
     * set list of channels that should use the LED dim curve
     *
     * @param listString
     */
    public void setDimCurveChannels(String listString) {
        applyCurve.clear();
        for (BaseDmxChannel channel : BaseDmxChannel.fromString(listString, universeId)) {
            applyCurve.add(channel.getChannelId());
        }
        logger.debug("applying dim curve in universe {} to channels {}", universeId, applyCurve);
    }

    /**
     * calculate dim curve table for fast lookup
     */
    private void fillDimCurveLookupTable() {
        // formula taken from: Poynton, C.A.: “Gamma” and its Disguises: The Nonlinear Mappings of
        // Intensity in Perception, CRTs, Film and Video, SMPTE Journal Dec. 1993, pp. 1099 - 1108
        // inverted
        int maxValue = DmxChannel.MAX_VALUE << 8;
        for (int i = 0; i <= maxValue; i++) {
            float lLn = ((float) i) / maxValue;
            if (lLn <= 0.08) {
                cie1931Curve[i] = (short) Math.round(DmxChannel.MAX_VALUE * lLn / 9.033);
            } else {
                cie1931Curve[i] = (short) Math.round(DmxChannel.MAX_VALUE * Math.pow((lLn + 0.16) / 1.16, 3));
            }
        }
    }

    /**
     * set channel refresh time
     *
     * @param refreshTime time in ms between state updates for a DMX channel
     */
    public void setRefreshTime(int refreshTime) {
        this.refreshTime = refreshTime;
    }
}
