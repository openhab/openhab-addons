/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
        int sashState = bytes[1] & 127;
        if (sashState == 0) {
            return UnDefType.UNDEF;
        }
        if (sashState < 4) {
            return new StringType("CLOSED");
        } else if (sashState < 7) {
            return new StringType("OPEN");
        } else if (sashState < 10) {
            return new StringType("TILTED");
        }

        return UnDefType.UNDEF;
    }

    protected State getWindowHandleState() {
        int handleState = bytes[1] & 127;
        if (handleState == 1 || handleState == 4 || handleState == 7) {
            return new StringType("CLOSED");
        } else if (handleState == 2 || handleState == 5 || handleState == 8) {
            return new StringType("OPEN");
        } else if (handleState == 3 || handleState == 6 || handleState == 9) {
            return new StringType("TILTED");
        }

        return UnDefType.UNDEF;
    }

    @Override
    protected String convertToEventImpl(String channelId, String channelTypeId, String lastEvent,
            Configuration config) {
        if (bytes[0] == 2) {
            switch (channelId) {
                case CHANNEL_WINDOWBREACHEVENT:
                    if (bytes[1] == 1) {
                        return "ALARM";
                    }
            }
        }
        return null;
    }

    @Override
    public State convertToStateImpl(String channelId, String channelTypeId, Function<String, State> getCurrentStateFunc,
            Configuration config) {

        if (bytes[0] == 1) {
            switch (channelId) {
                case CHANNEL_WINDOWSASHSTATE:
                    return getWindowSashState();
                case CHANNEL_WINDOWHANDLESTATE:
                    return getWindowHandleState();
                case CHANNEL_BATTERY_LEVEL:
                    return new DecimalType(bytes[6] & 127);
                case CHANNEL_BATTERYLOW:
                    return getBit(bytes[6], 7) ? OnOffType.ON : OnOffType.OFF;
            }
        }

        return UnDefType.UNDEF;
    }

    @Override
    protected boolean validateData(byte[] bytes) {
        // Window status
        if (bytes[0] == 1) {
            return bytes.length == 8;
        }
        // Alarm
        if (bytes[0] == 2) {
            return bytes.length == 2;
        }
        // Calibration (not supported but valid)
        if (bytes[0] == 11) {
            return bytes.length == 2;
        }
        return false;
    }
}
