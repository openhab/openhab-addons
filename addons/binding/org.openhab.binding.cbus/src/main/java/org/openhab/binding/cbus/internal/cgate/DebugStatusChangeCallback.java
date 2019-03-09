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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public class DebugStatusChangeCallback extends StatusChangeCallback {
    private Logger logger = LoggerFactory.getLogger(DebugStatusChangeCallback.class);

    @Override
    public boolean isActive() {
        return logger.isDebugEnabled();
    }

    @Override
    public void processStatusChange(CGateSession cgate_session, String status_change) {
        // logger.debug("status_change: " + status_change);
    }

}
