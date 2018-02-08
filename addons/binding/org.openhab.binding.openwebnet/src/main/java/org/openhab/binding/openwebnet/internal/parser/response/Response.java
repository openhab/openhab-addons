/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.internal.parser.response;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openwebnet.internal.listener.ResponseListener;

/**
 *
 * @author Antoine Laydier
 *
 */
@NonNullByDefault
public abstract class Response {

    protected abstract boolean check(String message);

    public abstract void process(String message, ResponseListener e);

    private static final List<Response> RESPONSE_LIST = new ArrayList<Response>() {

        private static final long serialVersionUID = 1L;

        {
            // try to order them, must used first
            add(new MsgAck());
            add(new MsgNotAck());
            add(new MsgBusyNack());
            add(new LightStatusChanged());
            add(new AutomationStatusChanged());
            add(new AutomationDetails());
            add(new ProductInformation());
            add(new FirmwareVersion());
            add(new HardwareVersion());
            add(new NumberOfProductDiscovered());
            add(new SupervisorOff());
            add(new SupervisorOn());
            add(new ZigBeeNetworkClosed());
            add(new ZigBeeNetworkJoined());
            add(new ZigBeeNetworkLeaved());
            add(new ZigBeeNetworkOpened());
        }
    };

    /**
     * Find the correct Answer to decode the message
     *
     * @param message to decode
     * @return Object that can decode the message or null
     */
    public static @Nullable Response find(String message) {
        for (Response a : RESPONSE_LIST) {
            if (a.check(message)) {
                return a;
            }
        }
        return null;
    }
}
