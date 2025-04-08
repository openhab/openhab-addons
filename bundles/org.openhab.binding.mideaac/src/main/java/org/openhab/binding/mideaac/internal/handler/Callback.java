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
package org.openhab.binding.mideaac.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Response} performs the polling byte data stream decoding
 * The {@link CapabilitiesResponse} performs the capability byte data stream decoding
 *
 * @author Leo Siepel - Initial contribution
 * @author Bob Eckhoff - added Capabilities Callback
 */
@NonNullByDefault
public interface Callback {
    /**
     * Updates channels with a standard response.
     *
     * @param response The standard response from the device used to update channels.
     */
    void updateChannels(Response response);

    /**
     * Updates channels with a capabilities response.
     *
     * @param capabilitiesResponse The capabilities response from the device used to update properties.
     */
    void updateChannels(CapabilitiesResponse capabilitiesResponse);
}
