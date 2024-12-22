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
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc1;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Class {@link NhcMessageBase1} used as base class for output from gson for cmd or event feedback from Niko Home
 * Control. This class only contains the common base fields required for the deserializer
 * {@link NikoHomeControlMessageDeserializer1} to select the specific formats implemented in {@link NhcMessageMap1},
 * {@link NhcMessageList1}, {@link NhcMessageListMap1}, {@link NhcMessageCmd1}.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
abstract class NhcMessageBase1 {

    private @Nullable String cmd;
    private @Nullable String event;

    String getCmd() {
        String cmd = this.cmd;
        return ((cmd != null) ? cmd : "");
    }

    void setCmd(String cmd) {
        this.cmd = cmd;
    }

    String getEvent() {
        String event = this.event;
        return ((event != null) ? event : "");
    }

    void setEvent(String event) {
        this.event = event;
    }
}
