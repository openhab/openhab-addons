/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ring.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ring.internal.data.Profile;

/**
 * The AccountHandler implements this interface to facilitate the
 * use of the common services.
 *
 * @author Wim Vissers - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */
@NonNullByDefault
public interface RingAccount {

    /**
     * Get the linked REST client.
     *
     * @return the REST client.
     */
    public @Nullable RestClient getRestClient();

    /**
     * Get the linked user profile.
     *
     * @return the user profile.
     */
    public @Nullable Profile getProfile();

    /**
     * Get the Account Handler Thing ID
     * *
     * 
     * @return the ring account thing id.
     */
    public String getThingId();
}
