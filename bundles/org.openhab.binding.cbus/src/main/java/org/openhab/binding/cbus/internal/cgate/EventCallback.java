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
package org.openhab.binding.cbus.internal.cgate;

import java.util.GregorianCalendar;

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public abstract class EventCallback {
    /**
     *
     * @param event_code
     * @return
     */
    public abstract boolean acceptEvent(int event_code);

    /**
     *
     * @param cgate_session
     * @param event_time
     * @param event_code
     * @param event
     */
    public abstract void processEvent(CGateSession cgate_session, int event_code, GregorianCalendar event_time,
            String event);
}
