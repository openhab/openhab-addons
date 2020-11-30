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
 * Class {@link QbusMessageBase} used as base class for output from gson for cmd or event feedback from the Qbus server.
 * This class only contains the common base fields required for the deserializer
 * {@link QbusMessageDeserializer} to select the specific formats implemented in {@link QbusMessageMap},
 * {@link QbusMessageListMap}, {@link QbusMessageCmd}.
 * <p>
 *
 * @author Koen Schockaert - Initial Contribution
 */

abstract class QbusMessageBase {

    private String cmd = "";
    private String event1 = "";
    private String sn = "";

    String getCmd() {
        return this.cmd;
    }

    void setCmd(String cmd) {
        this.cmd = cmd;
    }

    void setSn(String sn) {
        this.sn = sn;
    }

    String getEvent() {
        return this.event1;
    }

    void setEvent(String event) {
        this.event1 = event;
    }

    String getSn() {
        return this.sn;
    }
}
