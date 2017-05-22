/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sensebox.internal;

import java.io.IOException;

/**
 * This is a simple cache implementation of a single key-value-pair.
 *
 * There must be provided an action in order to retrieve/calculate the value for any given key. This action will be
 * called only if the answer from the last calculation is not valid anymore, i.e. if the key is different or the last
 * result is expired.
 *
 * @author Simon Kaufmann - Initial contribution and API.
 *
 * @param
 *            <Q>the type of the key
 * @param <V> the type of the value
 */
public class ExpiringCache<Q, V> {

    private final int expiry;
    private final LoadAction<Q, V> action;
    private long lastUpdate;
    private Q lastQuery;
    private V lastValue;

    public interface LoadAction<Q, V> {
        V load(Q key) throws IOException;
    }

    /**
     * Create a new instance.
     *
     * @param expiry duration in milliseconds for how long a result stays valid
     * @param action
     */
    public ExpiringCache(int expiry, LoadAction<Q, V> action) {
        this.expiry = expiry;
        this.action = action;
    }

    /**
     * Get the value for the given key - possibly from the cache, if it is still valid.
     *
     * @param key the input parameter
     * @return the value for the given key
     * @throws IOException
     */
    public synchronized V get(Q key) throws IOException {
        if (lastQuery == null || !lastQuery.equals(key) || isExpired()) {
            lastValue = action.load(key);
            lastUpdate = System.currentTimeMillis();
        }
        return lastValue;
    }

    private boolean isExpired() {
        return lastUpdate + expiry < System.currentTimeMillis();
    }

}
