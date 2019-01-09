/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    class NhcMessageParam {
        @Nullable
        List<NhcSystemInfo2> systemInfo;
        @Nullable
        List<NhcProfile2> profiles;
        @Nullable
        List<NhcService2> services;
        @Nullable
        List<NhcLocation2> locations;
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
