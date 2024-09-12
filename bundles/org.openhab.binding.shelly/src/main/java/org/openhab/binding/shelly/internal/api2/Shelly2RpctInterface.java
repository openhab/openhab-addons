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
package org.openhab.binding.shelly.internal.api2;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcNotifyEvent;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcNotifyStatus;

/**
 * The {@link Shelly2RpctInterface} is responsible for interfacing the Websocket.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public interface Shelly2RpctInterface {

    void onConnect(String deviceIp, boolean connected);

    void onMessage(String decodedmessage);

    void onNotifyStatus(Shelly2RpcNotifyStatus message);

    void onNotifyEvent(Shelly2RpcNotifyEvent message);

    void onClose(int statusCode, String reason);

    void onError(Throwable cause);
}
