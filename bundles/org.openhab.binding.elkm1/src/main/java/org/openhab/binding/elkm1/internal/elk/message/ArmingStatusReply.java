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

package org.openhab.binding.elkm1.internal.elk.message;

import org.openhab.binding.elkm1.internal.elk.ElkAlarmAreaState;
import org.openhab.binding.elkm1.internal.elk.ElkAlarmArmUpState;
import org.openhab.binding.elkm1.internal.elk.ElkAlarmArmedState;
import org.openhab.binding.elkm1.internal.elk.ElkCommand;
import org.openhab.binding.elkm1.internal.elk.ElkMessage;
import org.openhab.binding.elkm1.internal.elk.ElkMessageFactory;

/**
 * Although this is technically a status reply, it can come at any time when the status changes.
 *
 * @author David Bennett - Initial Contribution
 *
 */
public class ArmingStatusReply extends ElkMessage {
    private ElkAlarmArmUpState[] armedUp;
    private ElkAlarmAreaState[] state;
    private ElkAlarmArmedState[] armed;

    public ArmingStatusReply(String data) {
        super(ElkCommand.ArmingStatusRequestReply);
        byte[] dataBytes = data.getBytes();
        armedUp = new ElkAlarmArmUpState[ElkMessageFactory.MAX_AREAS];
        state = new ElkAlarmAreaState[ElkMessageFactory.MAX_AREAS];
        armed = new ElkAlarmArmedState[ElkMessageFactory.MAX_AREAS];
        for (int i = 0; i < dataBytes.length && i < ElkMessageFactory.MAX_AREAS; i++) {
            int pos = dataBytes[i] - 0x30;
            armed[i] = ElkAlarmArmedState.values()[pos];
            armedUp[i] = ElkAlarmArmUpState.values()[dataBytes[i + 8] - 0x30];
            state[i] = ElkAlarmAreaState.values()[dataBytes[i + 16] - 0x30];
        }
    }

    public ElkAlarmArmUpState[] getArmedUp() {
        return armedUp;
    }

    public ElkAlarmAreaState[] getState() {
        return state;
    }

    public ElkAlarmArmedState[] getArmed() {
        return armed;
    }

    @Override
    protected String getData() {
        return null;
    }

}
