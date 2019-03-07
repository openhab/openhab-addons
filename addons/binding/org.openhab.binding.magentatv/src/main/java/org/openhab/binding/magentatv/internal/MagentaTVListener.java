/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.magentatv.internal;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link MagentaTVListener} defines the interface to pass back the pairing
 * code to the listener class
 *
 * @author Markus Michels - Initial contribution (markus7017)
 */
public interface MagentaTVListener {
    public void onPairingResult(@NonNull String pairingCode) throws Exception;

    public void onWakeup(@NonNull Map<String, Object> discoveredProperties) throws Exception;

    public void onStbEvent(@NonNull String playContent) throws Exception;

    public void onPowerOff() throws Exception;

    public void onHeartbeat() throws Exception;
}
