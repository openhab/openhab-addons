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
package org.openhab.binding.nikohomecontrol.internal.protocol;

/**
 * Class {@link NhcMessageBase} used as base class for output from gson for cmd or event feedback from Niko Home
 * Control. This class only contains the common base fields required for the deserializer
 * {@link NikoHomeControlMessageDeserializer} to select the specific formats implemented in {@link NhcMessageMap},
 * {@link NhcMessageListMap}, {@link NhcMessageCmd}.
 * <p>
 *
 * @author Mark Herwege - Initial Contribution
 */
abstract class NhcMessageBase {

    private String cmd;
    private String event;

    String getCmd() {
        return this.cmd;
    }

    void setCmd(String cmd) {
        this.cmd = cmd;
    }

    String getEvent() {
        return this.event;
    }

    void setEvent(String event) {
        this.event = event;
    }
}
