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
package org.openhab.binding.e3dc.internal.dto;

import java.util.Arrays;
import java.util.BitSet;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.binding.e3dc.internal.modbus.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EmergencyBlock} Data object for E3DC Info Block
 *
 * @author Bernd Weymann - Initial contribution
 */
public class EmergencyBlock implements Data {
    private final Logger logger = LoggerFactory.getLogger(EmergencyBlock.class);
    public static final StringType EP_NOT_SUPPORTED = new StringType("EP not supported");
    public static final StringType EP_ACTIVE = new StringType("EP active");
    public static final StringType EP_NOT_ACTIVE = new StringType("EP not active");
    public static final StringType EP_POSSIBLE = new StringType("EP possible");
    public static final StringType EP_SWITCH = new StringType("EP Switch in wrong position");
    public static final StringType EP_UNKOWN = new StringType("EP Status unknown");
    public static final StringType[] EP_STATUS_ARRAY = new StringType[] { EP_NOT_SUPPORTED, EP_ACTIVE, EP_NOT_ACTIVE,
            EP_POSSIBLE, EP_SWITCH };

    public StringType epStatus = EP_UNKOWN;
    public OnOffType batteryLoadingLocked = OnOffType.OFF;
    public OnOffType batterUnLoadingLocked = OnOffType.OFF;
    public OnOffType epPossible = OnOffType.OFF;
    public OnOffType weatherPredictedLoading = OnOffType.OFF;
    public OnOffType regulationStatus = OnOffType.OFF;
    public OnOffType loadingLockTime = OnOffType.OFF;
    public OnOffType unloadingLockTime = OnOffType.OFF;

    public EmergencyBlock(byte[] bArray) {
        int status = DataConverter.getIntValue(bArray, 0);
        if (status >= 0 && status < 5) {
            epStatus = EP_STATUS_ARRAY[status];
        } else {
            epStatus = EP_UNKOWN;
        }
        byte[] emsStatusBytes = Arrays.copyOfRange(bArray, 2, 4);
        BitSet bs = BitSet.valueOf(emsStatusBytes);
        batteryLoadingLocked = bs.get(0) ? OnOffType.ON : OnOffType.OFF;
        batterUnLoadingLocked = bs.get(0) ? OnOffType.ON : OnOffType.OFF;
        epPossible = bs.get(0) ? OnOffType.ON : OnOffType.OFF;
        weatherPredictedLoading = bs.get(0) ? OnOffType.ON : OnOffType.OFF;
        regulationStatus = bs.get(0) ? OnOffType.ON : OnOffType.OFF;
        loadingLockTime = bs.get(0) ? OnOffType.ON : OnOffType.OFF;
        unloadingLockTime = bs.get(0) ? OnOffType.ON : OnOffType.OFF;
    }
}
