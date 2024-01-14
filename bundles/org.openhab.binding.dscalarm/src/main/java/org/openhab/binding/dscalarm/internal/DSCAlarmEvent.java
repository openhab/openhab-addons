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
package org.openhab.binding.dscalarm.internal;

import java.util.EventObject;

/**
 * Event for Receiving API Messages.
 *
 * @author Russell Stephens - Initial Contribution
 */
public class DSCAlarmEvent extends EventObject {

    private static final long serialVersionUID = 1L;
    private DSCAlarmMessage dscAlarmMessage;

    /**
     * Constructor.
     *
     * @param source
     */
    public DSCAlarmEvent(Object source) {
        super(source);
    }

    /**
     * Adds the the received API Message to the event.
     *
     * @param dscAlarmMessage
     */
    public void dscAlarmEventMessage(DSCAlarmMessage dscAlarmMessage) {
        this.dscAlarmMessage = dscAlarmMessage;
    }

    /**
     * Returns the API Message event from the DSC Alarm System.
     *
     * @return apiMessage
     */
    public DSCAlarmMessage getDSCAlarmMessage() {
        return dscAlarmMessage;
    }
}
