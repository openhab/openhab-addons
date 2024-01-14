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

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.config.EnOceanChannelRockerSwitchConfigBase.Channel;
import org.openhab.binding.enocean.internal.config.EnOceanChannelRockerSwitchListenerConfig;
import org.openhab.binding.enocean.internal.eep.Base._RPSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class F6_02_01 extends F6_02 {

    public F6_02_01() {
        super();
    }

    public F6_02_01(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected @Nullable String convertToEventImpl(String channelId, String channelTypeId, @Nullable String lastEvent,
            Configuration config) {
        if (t21 && nu) {
            if (CHANNEL_ROCKERSWITCH_ACTION.equals(channelTypeId)) {
                return getRockerSwitchAction(config);
            } else {
                byte dir1 = channelId.equals(CHANNEL_ROCKERSWITCH_CHANNELA) ? A0 : B0;
                byte dir2 = channelId.equals(CHANNEL_ROCKERSWITCH_CHANNELA) ? AI : BI;

                return getChannelEvent(dir1, dir2);
            }
        } else if (t21 && !nu) {
            if (CHANNEL_ROCKERSWITCH_ACTION.equals(channelTypeId)) {
                return CommonTriggerEvents.RELEASED;
            } else if (lastEvent != null) {
                if (lastEvent.equals(CommonTriggerEvents.DIR1_PRESSED)) {
                    return CommonTriggerEvents.DIR1_RELEASED;
                } else if (lastEvent.equals(CommonTriggerEvents.DIR2_PRESSED)) {
                    return CommonTriggerEvents.DIR2_RELEASED;
                }
            }
        }

        return null;
    }

    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command command,
            Function<String, State> getCurrentStateFunc, @Nullable Configuration config) {
        if (command instanceof StringType stringCommand) {
            String s = stringCommand.toString();

            if (s.equals(CommonTriggerEvents.DIR1_RELEASED) || s.equals(CommonTriggerEvents.DIR2_RELEASED)) {
                setStatus(_RPSMessage.T21_FLAG);
                setData((byte) 0x00);
                return;
            }

            byte dir1 = channelTypeId.equalsIgnoreCase(CHANNEL_VIRTUALROCKERSWITCHB) ? B0 : A0;
            byte dir2 = channelTypeId.equalsIgnoreCase(CHANNEL_VIRTUALROCKERSWITCHB) ? BI : AI;

            if (s.equals(CommonTriggerEvents.DIR1_PRESSED)) {
                setStatus((byte) (_RPSMessage.T21_FLAG | _RPSMessage.NU_FLAG));
                setData((byte) ((dir1 << 5) | PRESSED));
            } else if (s.equals(CommonTriggerEvents.DIR2_PRESSED)) {
                setStatus((byte) (_RPSMessage.T21_FLAG | _RPSMessage.NU_FLAG));
                setData((byte) ((dir2 << 5) | PRESSED));
            }
        }
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, @Nullable State> getCurrentStateFunc, Configuration config) {
        // this method is used by the classic device listener channels to convert a rocker switch message into an
        // appropriate item update
        State currentState = getCurrentStateFunc.apply(channelId);
        if (t21 && nu && currentState != null) {
            EnOceanChannelRockerSwitchListenerConfig c = config.as(EnOceanChannelRockerSwitchListenerConfig.class);
            byte dir1 = c.getChannel() == Channel.ChannelA ? A0 : B0;
            byte dir2 = c.getChannel() == Channel.ChannelA ? AI : BI;

            return getState(dir1, dir2, c.handleSecondAction, c.getSwitchMode(), channelTypeId, currentState);
        }

        return UnDefType.UNDEF;
    }

    @Override
    public boolean isValidForTeachIn() {
        if (t21) {
            // just treat press as teach in => DB0.4 has to be set
            if (!getBit(bytes[0], 4)) {
                return false;
            }
            // DB0.7 is never set for rocker switch message
            if (getBit(bytes[0], 7)) {
                return false;
            }
        } else {
            return false;
        }

        return true;
    }
}
