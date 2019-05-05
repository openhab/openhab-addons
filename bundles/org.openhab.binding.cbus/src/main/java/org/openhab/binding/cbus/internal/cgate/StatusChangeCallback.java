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

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public abstract class StatusChangeCallback {

    /**
     *
     * @return true if callback is active
     */
    public abstract boolean isActive();

    /**
     *
     * @param cgate_session
     * @param status_change
     */
    public abstract void processStatusChange(CGateSession cgate_session, String status_change);
}
