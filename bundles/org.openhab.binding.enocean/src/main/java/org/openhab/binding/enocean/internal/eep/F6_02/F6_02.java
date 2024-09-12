/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.eep.F6_02;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.config.EnOceanChannelRockerSwitchConfigBase.SwitchMode;
import org.openhab.binding.enocean.internal.eep.Base._RPSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public abstract class F6_02 extends _RPSMessage {

    protected static final byte AI = 0;
    protected static final byte A0 = 1;
    protected static final byte BI = 2;
    protected static final byte B0 = 3;
    protected static final byte PRESSED = 16;
    protected static final byte PRESSED_SEC = 1;

    protected static final String DIR1 = "DIR1";
    protected static final String DIR2 = "DIR2";
    protected static final String NODIR = "-";

    int secondByte = -1;
    int secondStatus = -1;

    public F6_02() {
        super();
    }

    public F6_02(ERP1Message packet) {
        super(packet);
    }

    private String getChannelADir() {
        if ((bytes[0] >>> 5) == A0 && (bytes[0] & PRESSED) != 0) {
            return DIR1;
        } else if ((bytes[0] >>> 5) == AI && (bytes[0] & PRESSED) != 0) {
            return DIR2;
        } else {
            return NODIR;
        }
    }

    private String getChannelBDir() {
        if ((bytes[0] >>> 5) == B0 && (bytes[0] & PRESSED) != 0) {
            return DIR1;
        } else if ((bytes[0] >>> 5) == BI && (bytes[0] & PRESSED) != 0) {
            return DIR2;
        } else if (((bytes[0] & 0xf) >>> 1) == B0 && (bytes[0] & PRESSED_SEC) != 0) {
            return DIR1;
        } else if (((bytes[0] & 0xf) >>> 1) == BI && (bytes[0] & PRESSED_SEC) != 0) {
            return DIR2;
        } else {
            return NODIR;
        }
    }

    protected String getRockerSwitchAction(Configuration config) {
        String dirA = getChannelADir();
        String dirB = getChannelBDir();

        return dirA + "|" + dirB;
    }

    protected @Nullable String getChannelEvent(byte dir1, byte dir2) {
        if ((bytes[0] & PRESSED_SEC) != 0) {
            // Do not emit an event if channelA is pressed together with channelB as it is undetermined which one gets
            // fired first
            return null;
        } else if ((bytes[0] >>> 5) == dir1) {
            return ((bytes[0] & PRESSED) != 0) ? CommonTriggerEvents.DIR1_PRESSED : CommonTriggerEvents.DIR1_RELEASED;
        } else if ((bytes[0] >>> 5) == dir2) {
            return ((bytes[0] & PRESSED) != 0) ? CommonTriggerEvents.DIR2_PRESSED : CommonTriggerEvents.DIR2_RELEASED;
        } else {
            return null;
        }
    }

    protected State getState(byte dir1, byte dir2, boolean handleSecondAction, SwitchMode switchMode,
            String channelTypeId, State currentState) {
        // We are just listening on the pressed event here
        switch (switchMode) {
            case RockerSwitch:
                if ((bytes[0] >>> 5) == dir1) {
                    if (((bytes[0] & PRESSED) != 0)) {
                        return channelTypeId.equals(CHANNEL_ROCKERSWITCHLISTENERSWITCH) ? OnOffType.ON : UpDownType.UP;
                    }
                } else if ((bytes[0] >>> 5) == dir2) {
                    if (((bytes[0] & PRESSED) != 0)) {
                        return channelTypeId.equals(CHANNEL_ROCKERSWITCHLISTENERSWITCH) ? OnOffType.OFF
                                : UpDownType.DOWN;
                    }
                } else if (handleSecondAction && ((bytes[0] & 0xf) >>> 1) == dir1) {
                    if (((bytes[0] & PRESSED_SEC) != 0)) {
                        return channelTypeId.equals(CHANNEL_ROCKERSWITCHLISTENERSWITCH) ? OnOffType.ON : UpDownType.UP;
                    }
                } else if (handleSecondAction && ((bytes[0] & 0xf) >>> 1) == dir2) {
                    if (((bytes[0] & PRESSED_SEC) != 0)) {
                        return channelTypeId.equals(CHANNEL_ROCKERSWITCHLISTENERSWITCH) ? OnOffType.OFF
                                : UpDownType.DOWN;
                    }
                }
                break;
            case ToggleDir1:
                if ((bytes[0] >>> 5) == dir1) {
                    if (((bytes[0] & PRESSED) != 0)) {
                        return channelTypeId.equals(CHANNEL_ROCKERSWITCHLISTENERSWITCH)
                                ? (currentState == UnDefType.UNDEF ? OnOffType.ON : inverse((OnOffType) currentState))
                                : (currentState == UnDefType.UNDEF ? UpDownType.UP
                                        : inverse((UpDownType) currentState));
                    }
                } else if (handleSecondAction && ((bytes[0] & 0xf) >>> 1) == dir1) {
                    if (((bytes[0] & PRESSED_SEC) != 0)) {
                        return channelTypeId.equals(CHANNEL_ROCKERSWITCHLISTENERSWITCH)
                                ? (currentState == UnDefType.UNDEF ? OnOffType.ON : inverse((OnOffType) currentState))
                                : (currentState == UnDefType.UNDEF ? UpDownType.UP
                                        : inverse((UpDownType) currentState));
                    }
                }
                break;
            case ToggleDir2:
                if ((bytes[0] >>> 5) == dir2) {
                    if (((bytes[0] & PRESSED) != 0)) {
                        return channelTypeId.equals(CHANNEL_ROCKERSWITCHLISTENERSWITCH)
                                ? (currentState == UnDefType.UNDEF ? OnOffType.ON : inverse((OnOffType) currentState))
                                : (currentState == UnDefType.UNDEF ? UpDownType.UP
                                        : inverse((UpDownType) currentState));
                    }
                } else if (handleSecondAction && ((bytes[0] & 0xf) >>> 1) == dir2) {
                    if (((bytes[0] & PRESSED_SEC) != 0)) {
                        return channelTypeId.equals(CHANNEL_ROCKERSWITCHLISTENERSWITCH)
                                ? (currentState == UnDefType.UNDEF ? OnOffType.ON : inverse((OnOffType) currentState))
                                : (currentState == UnDefType.UNDEF ? UpDownType.UP
                                        : inverse((UpDownType) currentState));
                    }
                }
                break;
            default:
                break;
        }

        return UnDefType.UNDEF;
    }

    protected State inverse(OnOffType currentState) {
        return OnOffType.from(currentState != OnOffType.ON);
    }

    protected State inverse(UpDownType currentState) {
        return currentState == UpDownType.UP ? UpDownType.DOWN : UpDownType.UP;
    }

    @Override
    protected boolean validateData(byte[] bytes) {
        return super.validateData(bytes) && !getBit(bytes[0], 7);
    }
}
