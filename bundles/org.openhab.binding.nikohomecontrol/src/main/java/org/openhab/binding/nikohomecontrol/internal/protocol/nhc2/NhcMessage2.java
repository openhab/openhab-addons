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
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc2;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * {@link NhcMessage2} represents a Niko Home Control II message. It is used when sending messages to the Connected
 * Controller or when parsing the message response json.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
class NhcMessage2 {
    static class NhcMessageParam {
        @Nullable
        List<NhcSystemInfo2> systemInfo;
        @Nullable
        List<NhcService2> services;
        @Nullable
        List<NhcDevice2> devices;
        @Nullable
        List<NhcNotification2> notifications;
        @Nullable
        List<NhcTimeInfo2> timeInfo;
    }

    @Nullable
    String method;
    String errCode = "";
    String errMessage = "";
    @Nullable
    List<NhcMessageParam> params;
}
