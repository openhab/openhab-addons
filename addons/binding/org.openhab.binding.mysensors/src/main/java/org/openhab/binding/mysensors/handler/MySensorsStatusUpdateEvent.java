/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.handler;

import org.openhab.binding.mysensors.internal.MySensorsMessage;

/**
 * @author Tim Oberföll
 *
 *         If a new message from the gateway/bridge is received
 *         a MySensorsStatusUpdateEvent is generated containing the MySensors message
 */
public class MySensorsStatusUpdateEvent {
    private MySensorsMessage data;

    public MySensorsStatusUpdateEvent(MySensorsMessage data) {
        this.data = data;
    }

    public MySensorsMessage getData() {
        return data;
    }

    public void setData(MySensorsMessage data) {
        this.data = data;
    }

}
