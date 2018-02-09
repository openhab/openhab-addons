/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.globalcache.internal.command;

/**
 * The {@link ResponseMessage} class is responsible for storing the raw and parsed response from the
 * GlobalCache device.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class ResponseMessage {

    private String deviceReply;

    public ResponseMessage(String deviceReply) {
        this.deviceReply = deviceReply;
    }

    public String getDeviceReply() {
        return deviceReply;
    }
}
