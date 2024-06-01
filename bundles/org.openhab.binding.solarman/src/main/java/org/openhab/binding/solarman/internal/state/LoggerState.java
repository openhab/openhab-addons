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
package org.openhab.binding.solarman.internal.state;

/**
 * @author Catalin Sanda - Initial contribution
 */
public class LoggerState {
    public final static Integer NO_FAILED_REQUESTS = 3;
    private State state = State.ONLINE; // Let's assume we're online initially
    private int offlineTryCount = 0;

    public void setOnline() {
        state = State.ONLINE;
        offlineTryCount = 0;
    }

    public void setPossiblyOffline() {
        state = ++offlineTryCount < NO_FAILED_REQUESTS ? State.LIMBO : State.OFFLINE;
    }

    public boolean isOffline() {
        return state == State.OFFLINE;
    }

    public boolean isJustBecameOffline() {
        return state == State.OFFLINE && offlineTryCount == NO_FAILED_REQUESTS;
    }

    public enum State {
        ONLINE,
        LIMBO,
        OFFLINE,
    }
}
