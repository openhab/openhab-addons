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
package org.openhab.binding.qbus.internal.protocol;

/**
 * Class {@link QbusMessageCmd} used as input to gson to send commands to Qbus. Extends
 * {@link QbusMessageBase}.
 * <p>
 * Example: <code>{"cmd":"executebistabiel","id":1,"value1":0}</code>
 *
 * @author Koen Schockaert - Initial Contribution
 */

@SuppressWarnings("unused")
class QMessageCmd extends QbusMessageBase {

    private int id;
    private Integer pos;
    private Integer value1;
    private Integer value2;
    private Integer mode;
    private Double setpoint;
    private String ctdsn;

    QMessageCmd(String cmd) {
        super.setCmd(cmd);
    }

    QMessageCmd(String cmd, int id) {
        this(cmd);
        this.id = id;
    }

    QMessageCmd(String cmd, int id, Integer value1) {
        this(cmd, id);
        this.value1 = value1;
    }

    QMessageCmd(String cmd, int id, Integer value1, Integer value2) {
        this(cmd, id, value1);
        this.value2 = value2;
    }

    QMessageCmd withMode(Integer mode) {
        this.mode = mode;
        return this;
    }

    QMessageCmd withSetpoint(Double d) {
        this.setpoint = d;
        return this;
    }

    QMessageCmd withSn(String sn) {

        this.ctdsn = sn;
        return this;
    }
}
