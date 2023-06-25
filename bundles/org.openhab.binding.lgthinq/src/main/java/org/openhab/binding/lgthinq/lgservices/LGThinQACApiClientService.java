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
package org.openhab.binding.lgthinq.lgservices;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACCanonicalSnapshot;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACCapability;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACTargetTmp;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ExtendedDeviceInfo;

/**
 * The {@link LGThinQACApiClientService}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public interface LGThinQACApiClientService extends LGThinQApiClientService<ACCapability, ACCanonicalSnapshot> {
    void changeOperationMode(String bridgeName, String deviceId, int newOpMode) throws LGThinqApiException;

    void changeFanSpeed(String bridgeName, String deviceId, int newFanSpeed) throws LGThinqApiException;

    void changeTargetTemperature(String bridgeName, String deviceId, ACTargetTmp newTargetTemp)
            throws LGThinqApiException;

    void turnCoolJetMode(String bridgeName, String deviceId, String modeOnOff) throws LGThinqApiException;

    void turnAirCleanMode(String bridgeName, String deviceId, String modeOnOff) throws LGThinqApiException;

    void turnAutoDryMode(String bridgeName, String deviceId, String modeOnOff) throws LGThinqApiException;

    void turnEnergySavingMode(String bridgeName, String deviceId, String modeOnOff) throws LGThinqApiException;

    ExtendedDeviceInfo getExtendedDeviceInfo(@NonNull String bridgeName, @NonNull String deviceId)
            throws LGThinqApiException;
}
