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
package org.openhab.binding.mybmw.internal.dto.charge;

import java.util.List;

/**
 * The {@link ChargingSessions} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class ChargingSessions {
    private String total;// ": "~ 218 kWh",
    private String numberOfSessions;// ": "17",
    private String chargingListState;// ": "HAS_SESSIONS",
    private List<ChargingSession> sessions;

    /**
     * @return the total
     */
    public String getTotal() {
        return total;
    }

    /**
     * @return the numberOfSessions
     */
    public String getNumberOfSessions() {
        return numberOfSessions;
    }

    /**
     * @return the chargingListState
     */
    public String getChargingListState() {
        return chargingListState;
    }

    /**
     * @return the sessions
     */
    public List<ChargingSession> getSessions() {
        return sessions;
    }
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */

    @Override
    public String toString() {
        return "ChargingSessions [total=" + total + ", numberOfSessions=" + numberOfSessions + ", chargingListState="
                + chargingListState + ", sessions=" + sessions + "]";
    }
}
