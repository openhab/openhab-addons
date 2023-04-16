/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.rainsoft.internal;

import org.openhab.binding.rainsoft.internal.data.Profile;

/**
 * The AccountHandler implements this interface to facilitate the
 * use of the common services.
 *
 * @author Ben Rosenblum - Initial contribution
 */
public interface RainSoftAccount {

    /**
     * Get the linked REST client.
     *
     * @return the REST client.
     */
    public RestClient getRestClient();

    /**
     * Get the linked user profile.
     *
     * @return the user profile.
     */
    public Profile getProfile();
}
