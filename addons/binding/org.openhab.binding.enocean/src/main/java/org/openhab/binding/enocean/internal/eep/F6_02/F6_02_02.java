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
package org.openhab.binding.enocean.internal.eep.F6_02;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.enocean.internal.config.EnOceanChannelRockerSwitchConfigBase.Channel;
import org.openhab.binding.enocean.internal.config.EnOceanChannelVirtualRockerSwitchConfig;
import org.openhab.binding.enocean.internal.eep.Base._RPSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class F6_02_02 extends _RPSMessage {

    final byte AI = 0;
    final byte A0 = 1;
    final byte BI = 2;
    final byte B0 = 3;
    final byte PRESSED = 16;

    public F6_02_02() {
        super();
    }

    public F6_02_02(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected String convertToEventImpl(String channelId, String channelTypeId, String lastEvent,
            Configuration config) {
        if (!isValid()) {
            return null;
        }

        if (t21 && nu) {

            byte dir1 = channelId.equals(CHANNEL_ROCKERSWITCH_CHANNELA) ? AI : BI;
            byte dir2 = channelId.equals(CHANNEL_ROCKERSWITCH_CHANNELA) ? A0 : B0;

            if ((bytes[0] >>> 5) == dir1) {
                return ((bytes[0] & PRESSED) != 0) ? CommonTriggerEvents.DIR1_PRESSED
                        : CommonTriggerEvents.DIR1_RELEASED;
            } else if ((bytes[0] >>> 5) == dir2) {
                return ((bytes[0] & PRESSED) != 0) ? CommonTriggerEvents.DIR2_PRESSED
                        : CommonTriggerEvents.DIR2_RELEASED;
            }
        } else if (t21 && !nu) {
            if (lastEvent != null && lastEvent.equals(CommonTriggerEvents.DIR1_PRESSED)) {
                return CommonTriggerEvents.DIR1_RELEASED;
            } else if (lastEvent != null && lastEvent.equals(CommonTriggerEvents.DIR2_PRESSED)) {
                return CommonTriggerEvents.DIR2_RELEASED;
            }
        }

        return null;
    }

    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command command,
            Map<String, State> currentState, Configuration config) {

        if (command instanceof StringType) {

            StringType s = (StringType) command;

            if (s.equals(CommonTriggerEvents.DIR1_RELEASED) || s.equals(CommonTriggerEvents.DIR2_RELEASED)) {
                setStatus(_RPSMessage.T21Flag);
                setData((byte) 0x00);
                return;
            }

            byte dir1 = channelTypeId.equalsIgnoreCase(CHANNEL_VIRTUALROCKERSWITCHB) ? BI : AI;
            byte dir2 = channelTypeId.equalsIgnoreCase(CHANNEL_VIRTUALROCKERSWITCHB) ? B0 : A0;

            if (s.equals(CommonTriggerEvents.DIR1_PRESSED)) {
                setStatus((byte) (_RPSMessage.T21Flag | _RPSMessage.NUFlag));
                setData((byte) ((dir1 << 5) | PRESSED));
            } else if (s.equals(CommonTriggerEvents.DIR2_PRESSED)) {
                setStatus((byte) (_RPSMessage.T21Flag | _RPSMessage.NUFlag));
                setData((byte) ((dir2 << 5) | PRESSED));
            }
        }
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId, State currentState,
            Configuration config) {
        // this method is used by the classic device listener channels to convert an rocker switch message into an
        // appropriate item update
        if (!isValid()) {
            return UnDefType.UNDEF;
        }

        if (t21 && nu) {
            EnOceanChannelVirtualRockerSwitchConfig c = config.as(EnOceanChannelVirtualRockerSwitchConfig.class);
            byte dir1 = c.getChannel() == Channel.ChannelA ? AI : BI;
            byte dir2 = c.getChannel() == Channel.ChannelA ? A0 : B0;

            // We are just listening on the pressed event here
            switch (c.getSwitchMode()) {
                case RockerSwitch:
                    if ((bytes[0] >>> 5) == dir1) {
                        if (((bytes[0] & PRESSED) != 0)) {
                            return channelTypeId.equals(CHANNEL_ROCKERSWITCHLISTENERSWITCH) ? OnOffType.ON
                                    : UpDownType.UP;
                        }
                    } else if ((bytes[0] >>> 5) == dir2) {
                        if (((bytes[0] & PRESSED) != 0)) {
                            return channelTypeId.equals(CHANNEL_ROCKERSWITCHLISTENERSWITCH) ? OnOffType.OFF
                                    : UpDownType.DOWN;
                        }
                    }
                    break;
                case ToggleDir1:
                    if ((bytes[0] >>> 5) == dir1) {
                        if (((bytes[0] & PRESSED) != 0)) {
                            return channelTypeId.equals(CHANNEL_ROCKERSWITCHLISTENERSWITCH)
                                    ? (currentState == UnDefType.UNDEF ? OnOffType.ON
                                            : inverse((OnOffType) currentState))
                                    : (currentState == UnDefType.UNDEF ? UpDownType.UP
                                            : inverse((UpDownType) currentState));
                        }
                    }
                    break;
                case ToggleDir2:
                    if ((bytes[0] >>> 5) == dir2) {
                        if (((bytes[0] & PRESSED) != 0)) {
                            return channelTypeId.equals(CHANNEL_ROCKERSWITCHLISTENERSWITCH)
                                    ? (currentState == UnDefType.UNDEF ? OnOffType.ON
                                            : inverse((OnOffType) currentState))
                                    : (currentState == UnDefType.UNDEF ? UpDownType.UP
                                            : inverse((UpDownType) currentState));
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        return UnDefType.UNDEF;
    }

    private State inverse(OnOffType currentState) {
        return currentState == OnOffType.ON ? OnOffType.OFF : OnOffType.ON;
    }

    private State inverse(UpDownType currentState) {
        return currentState == UpDownType.UP ? UpDownType.DOWN : UpDownType.UP;
    }
}
