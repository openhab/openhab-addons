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
package org.openhab.binding.magentatv.internal.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.magentatv.internal.MagentaTVException;

/**
 * The {@link MagentaTVListener} defines the interface to pass back the pairing
 * code to the listener class
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public interface MagentaTVListener {
    public void onPairingResult(String pairingCode) throws MagentaTVException;

    public void onWakeup(Map<String, Object> discoveredProperties) throws MagentaTVException;

    public void onMREvent(String playContent) throws MagentaTVException;

    public void onPowerOff() throws MagentaTVException;
}
