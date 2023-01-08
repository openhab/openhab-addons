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
package org.openhab.binding.enocean.internal.eep.D2_06;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.openhab.binding.enocean.internal.eep.Base._VLDMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Thomas Lauterbach - Initial contribution
 */
public class D2_06_50 extends _VLDMessage {

    public D2_06_50() {
        super();
    }

    public D2_06_50(ERP1Message packet) {
        super(packet);
    }

    protected State getWindowSashState() {
        int sashState = bytes[1] & 0x7f;
        if (sashState == 0x00) {
            return UnDefType.UNDEF;
        }
        if (sashState < 0x04) {
            return new StringType("CLOSED");
        } else if (sashState < 0x07) {
            return new StringType("OPEN");
        } else if (sashState < 0x0A) {
            return new StringType("TILTED");
        }

        return UnDefType.UNDEF;
    }

    protected State getWindowHandleState() {
        int handleState = bytes[1] & 0x7f;
        if (handleState == 0x01 || handleState == 0x04 || handleState == 0x07) {
            return new StringType("CLOSED");
        } else if (handleState == 0x02 || handleState == 0x05 || handleState == 0x08) {
            return new StringType("OPEN");
        } else if (handleState == 0x03 || handleState == 0x06 || handleState == 0x09) {
            return new StringType("TILTED");
        }

        return UnDefType.UNDEF;
    }

    protected State getCalibrationState() {
        int calibrationState = bytes[1] >>> 6;
        if (calibrationState == 0x00) {
            return new StringType("OK");
        } else if (calibrationState == 0x01) {
            return new StringType("ERROR");
        } else if (calibrationState == 0x02) {
            return new StringType("INVALID");
        }

        return UnDefType.UNDEF;
    }

    protected State getCalibrationStep() {
        int calibrationStep = bytes[1] & 0x3F;
        switch (calibrationStep) {
            case 0x00:
                return new StringType("NONE");
            case 0x01:
                return new StringType("SASH CLOSED HANDLE CLOSED");
            case 0x02:
                return new StringType("SASH CLOSED HANDLE OPEN");
            case 0x03:
                return new StringType("SASH CLOSED HANDLE TILTED");
            case 0x04:
                return new StringType("SASH OPEN HANDLE CLOSED");
            case 0x05:
                return new StringType("SASH OPEN HANDLE OPEN");
            case 0x06:
                return new StringType("SASH OPEN HANDLE TILTED");
            case 0x07:
                return new StringType("SASH TILTED HANDLE CLOSED");
            case 0x08:
                return new StringType("SASH TILTED HANDLE OPEN");
            case 0x09:
                return new StringType("SASH TILTED HANDLE TILTED");
            case 0x0A:
                return new StringType("FRAME MAGNET VALIDATION");
        }

        return UnDefType.UNDEF;
    }

    @Override
    protected String convertToEventImpl(String channelId, String channelTypeId, String lastEvent,
            Configuration config) {

        // Alarm
        if (bytes[0] == 0x02) {
            switch (channelId) {
                case CHANNEL_WINDOWBREACHEVENT:
                    if (bytes[1] == 0x01) {
                        return "ALARM";
                    }
            }
        }
        return null;
    }

    @Override
    public State convertToStateImpl(String channelId, String channelTypeId, Function<String, State> getCurrentStateFunc,
            Configuration config) {

        // Window status
        if (bytes[0] == 0x01) {
            switch (channelId) {
                case CHANNEL_WINDOWSASHSTATE:
                    return getWindowSashState();
                case CHANNEL_WINDOWHANDLESTATE:
                    return getWindowHandleState();
                case CHANNEL_BATTERY_LEVEL:
                    return new DecimalType(bytes[6] & 0x7f);
                case CHANNEL_BATTERYLOW:
                    return getBit(bytes[6], 7) ? OnOffType.ON : OnOffType.OFF;
            }
        }

        // Calibration
        if (bytes[0] == 0x11) {
            switch (channelId) {
                case CHANNEL_WINDOWCALIBRATIONSTATE:
                    return getCalibrationState();
                case CHANNEL_WINDOWCALIBRATIONSTEP:
                    return getCalibrationStep();
            }
        }

        return UnDefType.UNDEF;
    }
}
