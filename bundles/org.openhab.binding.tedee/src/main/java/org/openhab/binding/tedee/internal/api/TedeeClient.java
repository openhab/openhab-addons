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

package org.openhab.binding.tedee.internal.api;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Provides operations for retrieving information about Tedee locks and
 * controlling their state.
 *
 * @author Alex Goll - Initial contribution
 */
@NonNullByDefault
public interface TedeeClient {

    /**
     * Retrieves information about a Tedee lock.
     *
     * @param id the identifier of the lock
     * @return the lock information
     * @throws TedeeApiException if the request fails or the response is invalid
     */
    TedeeLock getLock(int id) throws TedeeApiException;

    /**
     * Retrieves the list of Tedee locks accessible through this client.
     *
     * @return the list of accessible locks
     * @throws TedeeApiException if the request fails or the response is invalid
     */
    List<TedeeLock> getLocks() throws TedeeApiException;

    /**
     * Sends a command to lock the specified Tedee lock.
     *
     * @param id the identifier of the lock
     * @throws TedeeApiException if the command fails
     */
    void lock(int id) throws TedeeApiException;

    /**
     * Sends a command to unlock the specified Tedee lock.
     *
     * @param id the identifier of the lock
     * @throws TedeeApiException if the command fails
     */
    void unlock(int id) throws TedeeApiException;

    /**
     * Sends a command to unlock the specified Tedee lock without pulling the
     * latch.
     *
     * @param id the identifier of the lock
     * @throws TedeeApiException if the command fails
     */
    void unlockWithoutPull(int id) throws TedeeApiException;

    /**
     * Sends a command to pull the latch of the specified Tedee lock.
     *
     * @param id the identifier of the lock
     * @throws TedeeApiException if the command fails
     */
    void pull(int id) throws TedeeApiException;

    /**
     * Sends a command to unlock the specified Tedee lock or pull the latch,
     * according to the supported device operation.
     *
     * @param id the identifier of the lock
     * @throws TedeeApiException if the command fails
     */
    void unlockOrPull(int id) throws TedeeApiException;
}
