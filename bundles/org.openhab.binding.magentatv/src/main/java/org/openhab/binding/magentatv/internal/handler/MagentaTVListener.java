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
package org.openhab.binding.magentatv.internal.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.magentatv.internal.MagentaTVException;

/**
 * The {@link MagentaTVListener} defines the interface to pass back the pairing
 * code and device events to the listener
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public interface MagentaTVListener {
    /**
     * Device returned pairing code
     *
     * @param pairingCode Code to be used for pairing process
     * @throws MagentaTVException
     */
    void onPairingResult(String pairingCode) throws MagentaTVException;

    /**
     * Device woke up (UPnP)
     *
     * @param discoveredProperties Properties from UPnP discovery
     * @throws MagentaTVException
     */
    void onWakeup(Map<String, String> discoveredProperties) throws MagentaTVException;

    /**
     * An event has been received from the MR
     *
     * @param playContent event information
     * @throws MagentaTVException
     */
    void onMREvent(String playContent) throws MagentaTVException;

    /**
     * A power-off was detected (SSDN message received)
     *
     * @throws MagentaTVException
     */
    void onPowerOff() throws MagentaTVException;
}
