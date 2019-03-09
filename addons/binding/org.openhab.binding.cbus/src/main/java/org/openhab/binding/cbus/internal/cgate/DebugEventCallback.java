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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public class DebugEventCallback extends EventCallback {
    private Logger logger = LoggerFactory.getLogger(DebugEventCallback.class);

    @Override
    public boolean acceptEvent(int event_code) {
        return logger.isDebugEnabled(); // Accept all events if debug enabled
    }

    @Override
    public void processEvent(CGateSession cgate_session, int event_code, GregorianCalendar event_time, String event) {
        // logger.debug("event_code: " + event_code + ", event_time: " + event_time.toString() + ", event: " + event);
    }

}
