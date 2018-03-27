/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.imperihome.internal.handler;

import javax.servlet.http.HttpServletRequest;

import org.openhab.io.imperihome.internal.model.RoomList;
import org.openhab.io.imperihome.internal.processor.DeviceRegistry;

/**
 * Rooms list request handler.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class RoomListHandler {

    private final DeviceRegistry deviceRegistry;

    public RoomListHandler(DeviceRegistry deviceRegistry) {
        this.deviceRegistry = deviceRegistry;
    }

    public RoomList handle(HttpServletRequest req) {
        RoomList response = new RoomList();
        response.setRooms(deviceRegistry.getRooms());
        return response;
    }

}
