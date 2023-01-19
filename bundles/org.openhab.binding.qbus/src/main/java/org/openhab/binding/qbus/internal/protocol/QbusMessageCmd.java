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
package org.openhab.binding.qbus.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class {@link QbusMessageCmd} used as input to gson to send commands to Qbus. Extends
 * {@link QbusMessageBase}.
 * <p>
 * Example: <code>{"cmd":"executebistabiel","id":1,"value1":0}</code>
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
class QbusMessageCmd extends QbusMessageBase {

    QbusMessageCmd(String CTD) {
        super.setSn(CTD);
    }

    QbusMessageCmd(String CTD, String cmd) {
        this(CTD);
        this.cmd = cmd;
    }

    QbusMessageCmd withId(Integer id) {
        this.setId(id);
        return this;
    }

    QbusMessageCmd withState(int state) {
        this.setState(state);
        return this;
    }

    QbusMessageCmd withMode(int mode) {
        this.setMode(mode);
        return this;
    }

    QbusMessageCmd withSetPoint(Double setpoint) {
        this.setSetPoint(setpoint);
        return this;
    }

    QbusMessageCmd withSlatState(int slatState) {
        this.setSlatState(slatState);
        return this;
    }
}
