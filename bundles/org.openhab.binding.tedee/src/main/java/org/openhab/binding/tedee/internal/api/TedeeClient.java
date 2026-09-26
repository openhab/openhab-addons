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
 * 
 * Exception thrown when communication with the Tedee Bridge API fails.
 *
 * @author Alex Goll - Initial contribution
 */

@NonNullByDefault
public interface TedeeClient {

    TedeeLock getLock(int id) throws TedeeApiException;

    List<TedeeLock> getLocks() throws TedeeApiException;

    void lock(int id) throws TedeeApiException;

    void unlock(int id) throws TedeeApiException;

    void unlockWithoutPull(int id) throws TedeeApiException;

    void pull(int id) throws TedeeApiException;

    void unlockOrPull(int id) throws TedeeApiException;
}
